package com.recap.pathing.gui;

import com.recap.core.gui.RecapButton;
import com.recap.core.gui.RecapPanel;
import com.recap.core.gui.RecapPopups;
import com.recap.core.gui.RecapTheme;
import com.google.common.util.concurrent.SettableFuture;
import com.recap.core.RecapMod;
import com.recap.core.utils.Utils;
import com.recap.core.versions.MCVer;
import com.recap.render.gui.GuiRenderQueue;
import com.recap.render.gui.GuiRenderSettings;
import com.recap.replay.ReplayHandler;
import com.recap.replay.RecapModReplay;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.replay.ReplayFile;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.AbstractGuiClickableContainer;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.container.GuiVerticalList;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.element.GuiTextField;
import de.johni0702.minecraft.gui.function.Click;
import de.johni0702.minecraft.gui.function.Closeable;
import de.johni0702.minecraft.gui.function.KeyHandler;
import de.johni0702.minecraft.gui.function.KeyInput;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.GridLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import de.johni0702.minecraft.gui.utils.Colors;
import de.johni0702.minecraft.gui.utils.Consumer;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.johni0702.minecraft.gui.versions.MCVer.getClipboardString;
import static de.johni0702.minecraft.gui.versions.MCVer.setClipboardString;

/**
 * Gui for loading and saving {@link Timeline Timelines}.
 */
public class GuiKeyframeRepository extends GuiScreen implements Closeable, KeyHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public final GuiPanel contentPanel = new RecapPanel().setHeaderEnabled(false);
    public final GuiLabel title = new GuiLabel(contentPanel).setI18nText("recap.gui.keyframerepository.title")
            .setColor(RecapTheme.TEXT_COLOR);
    public final GuiVerticalList list = new GuiVerticalList(contentPanel).setDrawShadow(true).setDrawSlider(true);
    public final GuiButton overwriteButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY).onClick(new Runnable() {
        @Override
        public void run() {
            RecapPopups.confirm(GuiKeyframeRepository.this,
                    new GuiLabel().setI18nText("recap.gui.keyframerepo.overwrite").setColor(RecapTheme.TEXT_COLOR)
            ).setYesI18nLabel("gui.yes").setNoI18nLabel("gui.no").onAccept(() -> {
                for (Entry entry : selectedEntries) {
                    timelines.put(entry.name, currentTimeline);
                }
                overwriteButton.setDisabled();
                save();
            });
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.overwrite").setDisabled();
    public final GuiButton saveAsButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY).onClick(new Runnable() {
        @Override
        public void run() {
            final GuiTextField nameField = new GuiTextField().setSize(200, 20).setFocused(true);
            final GuiYesNoPopup popup = RecapPopups.confirm(GuiKeyframeRepository.this,
                    new GuiLabel().setI18nText("recap.gui.saveas").setColor(RecapTheme.TEXT_COLOR),
                    nameField
            ).setYesI18nLabel("recap.gui.save").setNoI18nLabel("recap.gui.cancel");
            popup.getYesButton().setDisabled();
            ((VerticalLayout) popup.getInfo().getLayout()).setSpacing(7);
            nameField.onEnter(new Runnable() {
                @Override
                public void run() {
                    if (popup.getYesButton().isEnabled()) {
                        popup.getYesButton().onClick(new Click(-1, -1, 0, 0));
                    }
                }
            }).onTextChanged(new Consumer<String>() {
                @Override
                public void consume(String obj) {
                    popup.getYesButton().setEnabled(!nameField.getText().isEmpty()
                            && !timelines.containsKey(nameField.getText()));
                }
            });
            popup.onAccept(() -> {
                String name = nameField.getText();
                timelines.put(name, currentTimeline);
                list.getListPanel().addElements(null, new Entry(name));
                save();
            });
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.saveas");
    public final GuiButton loadButton = new RecapButton().onClick(new Runnable() {
        @Override
        public void run() {
            getMinecraft().openScreen(null);
            try {
                Timeline timeline = timelines.get(selectedEntries.iterator().next().name);
                for (Path path : timeline.getPaths()) {
                    path.updateAll();
                }
                future.set(timeline);
            } catch (Throwable t) {
                future.setException(t);
            }
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.load").setDisabled();
    public final GuiButton renameButton = new RecapButton().onClick(new Runnable() {
        @Override
        public void run() {
            Entry selectedEntry = selectedEntries.iterator().next();
            final GuiTextField nameField = new GuiTextField().setSize(200, 20).setFocused(true).setText(selectedEntry.name);
            final GuiYesNoPopup popup = RecapPopups.confirm(GuiKeyframeRepository.this,
                    new GuiLabel().setI18nText("recap.gui.rename").setColor(RecapTheme.TEXT_COLOR),
                    nameField
            ).setYesI18nLabel("recap.gui.done").setNoI18nLabel("recap.gui.cancel");
            popup.getYesButton().setDisabled();
            ((VerticalLayout) popup.getInfo().getLayout()).setSpacing(7);
            nameField.onEnter(new Runnable() {
                @Override
                public void run() {
                    if (popup.getYesButton().isEnabled()) {
                        popup.getYesButton().onClick(new Click(-1, -1, 0, 0));
                    }
                }
            }).onTextChanged(new Consumer<String>() {
                @Override
                public void consume(String obj) {
                    popup.getYesButton().setEnabled(!nameField.getText().isEmpty()
                            && !timelines.containsKey(nameField.getText()));
                }
            });
            popup.onAccept(() -> {
                String name = nameField.getText();
                timelines.put(name, timelines.remove(selectedEntry.name));
                selectedEntry.name = name;
                selectedEntry.label.setText(name);
                save();
            });
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.rename").setDisabled();
    public final GuiButton removeButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY).onClick(new Runnable() {
        @Override
        public void run() {
            RecapPopups.confirm(GuiKeyframeRepository.this,
                    new GuiLabel().setI18nText("recap.gui.keyframerepo.delete").setColor(RecapTheme.TEXT_COLOR)
            ).setYesI18nLabel("recap.gui.delete").setNoI18nLabel("recap.gui.cancel").onAccept(() -> {
                for (Entry entry : selectedEntries) {
                    timelines.remove(entry.name);
                    list.getListPanel().removeElement(entry);
                }

                selectedEntries.clear();
                updateButtons();
                save();
            });
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.remove").setDisabled();

    public final GuiButton copyButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY).onClick(new Runnable() {
        @Override
        public void run() {
            Map<String, Timeline> toBeSerialized = new HashMap<>();
            for (Entry entry : selectedEntries) {
                toBeSerialized.put(entry.name, timelines.get(entry.name));
            }
            try {
                TimelineSerialization serialization = new TimelineSerialization(registry, null);
                setClipboardString(serialization.serialize(toBeSerialized));
            } catch (Throwable t) {
                t.printStackTrace();
                CrashReport report = CrashReport.create(t, "Copying timeline(s)");
                Utils.error(LOGGER, GuiKeyframeRepository.this, report, () -> {});
            }
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.copy").setDisabled();

    public final GuiButton pasteButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY).onClick(new Runnable() {
        @Override
        public void run() {
            try {
                TimelineSerialization serialization = new TimelineSerialization(registry, null);
                for (Map.Entry<String, Timeline> entry : serialization.deserialize(getClipboardString()).entrySet()) {
                    String name = entry.getKey();
                    while (timelines.containsKey(name)) {
                        name += " (Copy)";
                    }
                    timelines.put(name, entry.getValue());
                    list.getListPanel().addElements(null, new Entry(name));
                }
                save();
            } catch (Throwable t) {
                // Intentionally not making a fuzz about it cause this will likely happen when they have anything
                // else in their clipboard.
                // If it's actually not working, they'll go complain and we'll just check the log.
                t.printStackTrace();
            }
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.paste");

    public final GuiButton addToQueueButton = new RecapButton().onClick(new Runnable() {
        @Override
        public void run() {
            ReplayHandler replayHandler = RecapModReplay.instance.getReplayHandler();
            GuiRenderQueue queue = new GuiRenderQueue(GuiKeyframeRepository.this, replayHandler, null);
            queue.open();

            Set<String> selected = selectedEntries.stream().map(e -> e.name).collect(Collectors.toSet());
            ArrayDeque<Map.Entry<String, Timeline>> toBeAdded = new ArrayDeque<>();
            // Iterating over timelines to get consistent ordering (cause selectedEntries is an unordered set)
            // and we need to get the Timelines anyway.
            for (Map.Entry<String, Timeline> entry : timelines.entrySet()) {
                if (selected.contains(entry.getKey())) {
                    toBeAdded.offerLast(entry);
                }
            }
            new Runnable() {
                @Override
                public void run() {
                    Map.Entry<String, Timeline> entry = toBeAdded.pollFirst();
                    if (entry == null) {
                        return;
                    }
                    String name = entry.getKey();
                    Timeline timeline = entry.getValue();
                    GuiRenderSettings settingsGui = queue.addJob(timeline);
                    settingsGui.buttonPanel.removeElement(settingsGui.renderButton);
                    settingsGui.setOutputFileBaseName(name);
                    Consumer<Click> orgOnClick = settingsGui.queueButton.getOnClick();
                    settingsGui.queueButton.onClick(click -> {
                        orgOnClick.consume(click);
                        this.run();
                    });
                    settingsGui.open();
                }
            }.run();
        }
    }).setSize(75, 20).setI18nLabel("recap.gui.rendersettings.addtoqueue");

    public final GuiPanel buttonPanel = new GuiPanel(contentPanel)
            .setLayout(new GridLayout()
                    .setColumns(4)
                    .setSpacingX(5)
                    .setSpacingY(5))
            .addElements(null,
                    overwriteButton, saveAsButton, renameButton, removeButton,
                    loadButton, addToQueueButton, copyButton, pasteButton);

    private final Map<String, Timeline> timelines = new LinkedHashMap<>();
    private final Timeline currentTimeline;
    private final SettableFuture<Timeline> future = SettableFuture.create();
    private final PathingRegistry registry;
    private final ReplayFile replayFile;

    private final Set<Entry> selectedEntries = new HashSet<>();

    {
        setBackground(Background.NONE);
        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                pos(contentPanel, width / 2 - width(contentPanel) / 2, height / 2 - height(contentPanel) / 2);
            }
        });
        contentPanel.setLayout(new CustomLayout<GuiPanel>() {
            @Override
            protected void layout(GuiPanel container, int width, int height) {
                pos(title, 14, 12);
                pos(list, 14, y(title) + height(title) + 8);
                size(list, width - 28, height - 20 - height(buttonPanel) - 8 - y(list));
                pos(buttonPanel, 14, y(list) + height(list) + 8);
            }

            @Override
            public ReadableDimension calcMinSize(GuiContainer<?> container) {
                ReadableDimension screenSize = getMinSize();
                return new Dimension(screenSize.getWidth() - 10, screenSize.getHeight() - 10);
            }
        });
    }

    public GuiKeyframeRepository(PathingRegistry registry, ReplayFile replayFile, Timeline currentTimeline) throws IOException {
        this.registry = registry;
        this.replayFile = replayFile;
        this.currentTimeline = currentTimeline;

        timelines.putAll(replayFile.getTimelines(registry));

        for (Map.Entry<String, Timeline> entry : timelines.entrySet()) {
            if (entry.getKey().isEmpty()) continue; // don't show auto-save slot
            list.getListPanel().addElements(null, new Entry(entry.getKey()));
        }

        updateButtons();
    }

    private void updateButtons() {
        int selected = selectedEntries.size();

        overwriteButton.setEnabled(selected >= 1);
        loadButton.setEnabled(selected == 1);
        renameButton.setEnabled(selected == 1);
        removeButton.setEnabled(selected >= 1);
        copyButton.setEnabled(selected >= 1);
        addToQueueButton.setEnabled(selected >= 1);
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            RecapTheme.screen(renderer, size.getWidth(), size.getHeight());
        }
        super.draw(renderer, size, renderInfo);
    }

    @Override
    public void display() {
        super.display();
        RecapModReplay.instance.getReplayHandler().getOverlay().setVisible(false);
    }

    @Override
    public void close() {
        RecapModReplay.instance.getReplayHandler().getOverlay().setVisible(true);
    }

    public SettableFuture<Timeline> getFuture() {
        return future;
    }

    public void save() {
        try {
            replayFile.writeTimelines(registry, timelines);
        } catch (IOException e) {
            e.printStackTrace();
            RecapMod.instance.printWarningToChat("Error saving timelines: " + e.getMessage());
        }
    }

    @Override
    public boolean handleKey(KeyInput keyInput) {
        if (keyInput.hasCtrl()) {
            switch (keyInput.key) {
                case MCVer.Keyboard.KEY_A:
                    if (selectedEntries.size() < timelines.size()) {
                        for (GuiElement<?> child : list.getListPanel().getChildren()) {
                            if (child instanceof Entry) {
                                selectedEntries.add((Entry) child);
                            }
                        }
                    } else {
                        selectedEntries.clear();
                    }
                    updateButtons();
                    return true;
                case MCVer.Keyboard.KEY_C:
                    copyButton.onClick(new Click(-1, -1, 0, 0));
                    return true;
                case MCVer.Keyboard.KEY_V:
                    pasteButton.onClick(new Click(-1, -1, 0, 0));
                    return true;
            }
        }
        return false;
    }

    public class Entry extends AbstractGuiClickableContainer<Entry> {
        public final GuiLabel label = new GuiLabel(this);
        private String name;

        public Entry(String name) {
            this.name = name;

            setLayout(new CustomLayout<Entry>() {
                @Override
                protected void layout(Entry container, int width, int height) {
                    pos(label, 5, height / 2 - height(label) / 2);
                }

                @Override
                public ReadableDimension calcMinSize(GuiContainer<?> container) {
                    return new Dimension(buttonPanel.calcMinSize().getWidth(), 16);
                }
            });
            label.setText(name);
        }

        @Override
        protected void onClick(Click click) {
            if (!click.hasCtrl()) {
                selectedEntries.clear();
            }
            if (selectedEntries.contains(this)) {
                selectedEntries.remove(this);
            } else {
                selectedEntries.add(this);
            }
            updateButtons();
        }

        @Override
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            if (renderInfo.getLayer() == 0) {
                int width = size.getWidth();
                int height = size.getHeight();
                Point mouse = new Point(renderInfo.getMouseX(), renderInfo.getMouseY());
                if (getContainer() != null) {
                    getContainer().convertFor(this, mouse);
                }
                boolean hovered = mouse.getX() >= 0 && mouse.getY() >= 0
                        && mouse.getX() < width && mouse.getY() < height;
                boolean selected = selectedEntries.contains(this);

                RecapTheme.roundedRect(renderer, 0, 0, width, height,
                        selected ? RecapTheme.ROW_ACTIVE : hovered ? RecapTheme.ROW_HOVER : RecapTheme.ROW);
                renderer.drawRect(0, 2, 3, height - 4,
                        selected ? RecapTheme.ACCENT : RecapTheme.withAlpha(RecapTheme.ACCENT, 0.25f));
            }
            super.draw(renderer, size, renderInfo);
        }

        @Override
        protected Entry getThis() {
            return this;
        }
    }
}
