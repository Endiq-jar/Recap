package com.replaymod.replay.gui.screen;

import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.render.gui.GuiRenderQueue;
import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.render.utils.RenderJob;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.function.CharHandler;
import de.johni0702.minecraft.gui.function.CharInput;
import de.johni0702.minecraft.gui.function.Click;
import de.johni0702.minecraft.gui.function.KeyHandler;
import de.johni0702.minecraft.gui.function.KeyInput;
import de.johni0702.minecraft.gui.versions.Image;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Formatting;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.gui.GuiReplaySettings;
import com.replaymod.core.gui.RecapButton;
import com.replaymod.core.gui.RecapTheme;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.core.versions.MCVer.Keyboard;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import de.johni0702.minecraft.gui.container.AbstractGuiContainer;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.element.*;
import de.johni0702.minecraft.gui.element.advanced.AbstractGuiResourceLoadingList;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import de.johni0702.minecraft.gui.utils.Colors;
import de.johni0702.minecraft.gui.utils.Consumer;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.replaymod.replay.ReplayModReplay.LOGGER;
import static de.johni0702.minecraft.gui.versions.MCVer.getFontRenderer;

//#if MC>=11400
import net.minecraft.text.TranslatableText;
//#else
//$$ import net.minecraft.client.resources.I18n;
//#endif

public class GuiReplayViewer extends GuiScreen {
    private final ReplayModReplay mod;

    public final GuiReplayList list = new GuiReplayList(this).onSelectionChanged(this::updateButtons).onSelectionDoubleClicked(() -> {
        if (this.loadButton.isEnabled()) {
            this.loadButton.onClick(new Click(-1, -1, 0, 0));
        }
    });

    public final GuiButton loadButton = new RecapButton().onClick(new Runnable() {
        private boolean loading = false;

        @Override
        public void run() {
            // Prevent the player from opening the replay twice at the same time
            if (loading) {
                return;
            }
            loading = true;
            loadButton.setDisabled(); // visual hint

            List<GuiReplayEntry> selected = list.getSelected();
            if (selected.size() == 1) {
                File file = selected.get(0).file;
                LOGGER.info("Opening replay in viewer: " + file);
                try {
                    mod.startReplay(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Iterator<Pair<File, List<RenderJob>>> replays = selected.stream()
                        .filter(it -> !it.renderQueue.isEmpty())
                        .map(it -> Pair.of(it.file, it.renderQueue))
                        .iterator();
                GuiRenderQueue.processMultipleReplays(GuiReplayViewer.this, mod, replays, () -> {
                    loading = false;
                    updateButtons();
                    display();
                });
            }
        }
    }).setSize(150, 20);

    public final GuiButton folderButton = new RecapButton().setPrimary(false).onClick(new Runnable() {
        @Override
        public void run() {
            try {
                File folder = mod.getCore().folders.getReplayFolder().toFile();

                MCVer.openFile(folder);
            } catch (IOException e) {
                mod.getLogger().error("Cannot open file", e);
            }
        }
    }).setSize(150, 20).setI18nLabel("replaymod.gui.viewer.replayfolder");

    public final GuiButton renameButton = new RecapButton().setPrimary(false).onClick(new Runnable() {
        @Override
        public void run() {
            final Path path = list.getSelected().get(0).file.toPath();
            String name = Utils.fileNameToReplayName(path.getFileName().toString());
            final GuiTextField nameField = new GuiTextField().setSize(200, 20).setFocused(true).setText(name);
            final GuiYesNoPopup popup = GuiYesNoPopup.open(GuiReplayViewer.this,
                    new GuiLabel().setI18nText("replaymod.gui.viewer.rename.name").setColor(RecapTheme.TEXT_COLOR),
                    nameField
            ).setYesI18nLabel("replaymod.gui.rename").setNoI18nLabel("replaymod.gui.cancel");
            ((VerticalLayout) popup.getInfo().getLayout()).setSpacing(7);
            nameField.onEnter(new Runnable() {
                @Override
                public void run() {
                    if (popup.getYesButton().isEnabled()) {
                        popup.getYesButton().onClick(new Click(-1, -1, 0, 0));
                    }
                }
            }).onTextChanged(obj -> {
                popup.getYesButton().setEnabled(!nameField.getText().isEmpty()
                        && Files.notExists(Utils.replayNameToPath(path.getParent(), nameField.getText())));
            });
            popup.onAccept(() -> {
                // Sanitize their input
                String newName = nameField.getText().trim();
                // This file is what they want
                Path targetPath = Utils.replayNameToPath(path.getParent(), newName);
                try {
                    // Finally, try to move it
                    Files.move(path, targetPath);
                } catch (IOException e) {
                    // We failed (might also be their OS)
                    e.printStackTrace();
                    getMinecraft().openScreen(new NoticeScreen(
                            //#if MC>=11400
                            GuiReplayViewer.this::display,
                            new TranslatableText("replaymod.gui.viewer.delete.failed1"),
                            new TranslatableText("replaymod.gui.viewer.delete.failed2")
                            //#else
                            //$$ I18n.format("replaymod.gui.viewer.delete.failed1"),
                            //$$ I18n.format("replaymod.gui.viewer.delete.failed2")
                            //#endif
                    ));
                    return;
                }
                list.load();
            });
        }
    }).setSize(73, 20).setI18nLabel("replaymod.gui.rename").setDisabled();
    public final GuiButton deleteButton = new RecapButton().setPrimary(false).onClick(() -> {
        for (GuiReplayEntry entry : list.getSelected()) {
            String name = entry.name.getText();
            GuiYesNoPopup.open(GuiReplayViewer.this,
                    new GuiLabel().setI18nText("replaymod.gui.viewer.delete.linea").setColor(RecapTheme.TEXT_COLOR),
                    new GuiLabel().setI18nText("replaymod.gui.viewer.delete.lineb", name + Formatting.RESET).setColor(RecapTheme.TEXT_COLOR)
            ).setYesI18nLabel("replaymod.gui.delete").setNoI18nLabel("replaymod.gui.cancel").onAccept(() -> {
                try {
                    FileUtils.forceDelete(entry.file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                list.load();
            });
        }
    }).setSize(73, 20).setI18nLabel("replaymod.gui.delete").setDisabled();

    public final RecapButton settingsButton = new RecapButton(this);
    {
        settingsButton.setPrimary(false);
        settingsButton.setSize(84, 20);
        settingsButton.setI18nLabel("replaymod.gui.settings");
        settingsButton.setTooltip(new GuiTooltip().setI18nText("replaymod.gui.settings"));
        settingsButton.onClick(() ->
                new GuiReplaySettings(toMinecraft(), getMod().getCore().getSettingsRegistry()).display());
    }

    public final GuiButton cancelButton = new RecapButton().setPrimary(false).onClick(new Runnable() {
        @Override
        public void run() {
            getMinecraft().openScreen(null);
        }
    }).setSize(73, 20).setI18nLabel("replaymod.gui.cancel");

    public final List<GuiButton> replaySpecificButtons = new ArrayList<>();
    { replaySpecificButtons.addAll(Arrays.asList(renameButton)); }
    public final GuiPanel editorButton = new GuiPanel();

    public final GuiPanel upperButtonPanel = new GuiPanel().setLayout(new HorizontalLayout().setSpacing(5))
            .addElements(null, loadButton);
    public final GuiPanel lowerButtonPanel = new GuiPanel().setLayout(new HorizontalLayout().setSpacing(5))
            .addElements(null, renameButton, deleteButton, editorButton, cancelButton);
    public final GuiPanel buttonPanel = new GuiPanel(this).setLayout(new VerticalLayout().setSpacing(5))
            .addElements(null, upperButtonPanel, lowerButtonPanel);

    public GuiReplayViewer(ReplayModReplay mod) {
        this.mod = mod;

        try {
            list.setFolder(mod.getCore().folders.getReplayFolder().toFile());
        } catch (IOException e) {
            throw new CrashException(CrashReport.create(e, "Getting replay folder"));
        }

        // Recap Mod: the mod draws its own header, so the vanilla blur title is not used.
        setBackground(Background.NONE);

        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                pos(buttonPanel, 18, height - 14 - height(buttonPanel));

                pos(list, 18, 58);
                size(list, width - 36, y(buttonPanel) - 12 - y(list));

                pos(settingsButton, width - width(settingsButton) - 18, 12);
            }
        });

        updateButtons();
    }

    public ReplayModReplay getMod() {
        return mod;
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            RecapTheme.screen(renderer, size.getWidth(), size.getHeight());
            RecapTheme.header(renderer, size.getWidth(),
                    net.minecraft.client.resource.language.I18n.translate("replaymod.gui.replayviewer"),
                    net.minecraft.client.resource.language.I18n.translate("replaymod.gui.viewer.subtitle"),
                    null);
            RecapTheme.footer(renderer, size.getWidth(), size.getHeight(),
                    net.minecraft.client.resource.language.I18n.translate("replaymod.gui.viewer.footer"));
        }
        super.draw(renderer, size, renderInfo);
    }

    private void updateButtons() {
        List<GuiReplayEntry> selected = list.getSelected();
        int count = selected.size();

        replaySpecificButtons.forEach(b -> b.setEnabled(count == 1));
        deleteButton.setEnabled(count > 0);
        if (count > 1) {
            Set<RenderJob> jobs = selected.stream().flatMap(entry -> entry.renderQueue.stream()).collect(Collectors.toSet());
            String[] tooltipLines = jobs.stream().map(RenderJob::getName).toArray(String[]::new);
            loadButton.setI18nLabel("replaymod.gui.viewer.bulkrender", jobs.size());
            loadButton.setTooltip(new GuiTooltip().setText(tooltipLines));
            loadButton.setEnabled(!jobs.isEmpty());

            String[] compatError = VideoRenderer.checkCompat(jobs.stream().map(RenderJob::getSettings));
            if (compatError != null) {
                loadButton.setDisabled().setTooltip(new GuiTooltip().setText(compatError));
            }
        } else {
            loadButton.setI18nLabel("replaymod.gui.load");
            loadButton.setTooltip(null);
            loadButton.setEnabled(count == 1 && !selected.get(0).incompatible);
        }
    }

    private static final GuiImage DEFAULT_THUMBNAIL = new GuiImage().setTexture(Utils.DEFAULT_THUMBNAIL);

    public static class GuiSelectReplayPopup extends AbstractGuiPopup<GuiSelectReplayPopup> {
        public static GuiSelectReplayPopup openGui(GuiContainer container, File folder) {
            GuiSelectReplayPopup popup = new GuiSelectReplayPopup(container, folder);
            popup.list.load();
            popup.open();
            return popup;
        }

        private final SettableFuture<File> future = SettableFuture.create();

        private final GuiReplayList list = new GuiReplayList(popup);

        private final GuiButton acceptButton = new GuiButton(popup).setI18nLabel("gui.done").setSize(50, 20).setDisabled();

        private final GuiButton cancelButton = new GuiButton(popup).setI18nLabel("gui.cancel").setSize(50, 20);


        public GuiSelectReplayPopup(GuiContainer container, File folder) {
            super(container);

            list.setFolder(folder);

            list.onSelectionChanged(() -> {
                acceptButton.setEnabled(list.getSelected() != null);
            }).onSelectionDoubleClicked(() -> {
                close();
                future.set(list.getSelected().get(0).file);
            });
            acceptButton.onClick(() -> {
                future.set(list.getSelected().get(0).file);
                close();
            });
            cancelButton.onClick(() -> {
                future.set(null);
                close();
            });

            popup.setLayout(new CustomLayout<GuiPanel>() {
                @Override
                protected void layout(GuiPanel container, int width, int height) {
                    pos(cancelButton, width - width(cancelButton), height - height(cancelButton));
                    pos(acceptButton, x(cancelButton) - 5 - width(acceptButton), y(cancelButton));
                    pos(list, 0, 5);
                    size(list, width, height - height(cancelButton) - 10);
                }

                @Override
                public ReadableDimension calcMinSize(GuiContainer container) {
                    return new Dimension(330, 200);
                }
            });
        }

        public SettableFuture<File> getFuture() {
            return future;
        }

        public GuiReplayList getList() {
            return list;
        }

        public GuiButton getAcceptButton() {
            return acceptButton;
        }

        public GuiButton getCancelButton() {
            return cancelButton;
        }

        @Override
        protected GuiSelectReplayPopup getThis() {
            return this;
        }
    }

    public static class GuiReplayList extends AbstractGuiResourceLoadingList<GuiReplayList, GuiReplayEntry> implements KeyHandler, CharHandler {
        private File folder = null;

        // Not actually a child of this element, we just use it for text manipulation
        private final GuiTextField filterTextField = new GuiTextField()
                .setFocused(true);

        public GuiReplayList(GuiContainer container) {
            super(container);
        }

        {
            onLoad((Consumer<Supplier<GuiReplayEntry>> results) -> {
                File[] files = folder.listFiles((FileFilter) new SuffixFileFilter(".mcpr", IOCase.INSENSITIVE));
                if (files == null) {
                    LOGGER.warn("Failed to list files in {}", folder);
                    return;
                }
                Map<File, Long> lastModified = new HashMap<>();
                Arrays.sort(files, Comparator.<File>comparingLong(f -> lastModified.computeIfAbsent(f, File::lastModified)).reversed());
                for (final File file : files) {
                    if (Thread.interrupted()) break;
                    try (ReplayFile replayFile = ReplayMod.instance.files.open(file.toPath())) {
                        final Image thumb = Optional.ofNullable(replayFile.getThumbBytes().orNull()).flatMap(stream -> {
                            try (InputStream in = stream) {
                                return Optional.of(Image.read(in));
                            } catch (IOException e) {
                                e.printStackTrace();
                                return Optional.empty();
                            }
                        }).orElse(null);
                        final ReplayMetaData metaData = replayFile.getMetaData();
                        List<RenderJob> renderQueue = RenderJob.readQueue(replayFile);

                        if (metaData != null) {
                            results.consume(() -> new GuiReplayEntry(file, metaData, thumb, renderQueue) {
                                @Override
                                public ReadableDimension calcMinSize() {
                                    if (isFiltered(this)) {
                                        return new Dimension(-4, -4);
                                    }
                                    return super.calcMinSize();
                                }
                            });
                        }
                    } catch (Exception e) {
                        LOGGER.error("Could not load Replay File {}", file.getName(), e);
                    }
                }
            }).setDrawShadow(true).setDrawSlider(true);
        }

        public void setFolder(File folder) {
            this.folder = folder;
        }

        private boolean isFiltered(GuiReplayEntry entry) {
            String filter = filterTextField.getText().toLowerCase();
            if (filter.isEmpty()) {
                return false;
            }
            return !entry.name.getText().toLowerCase().contains(filter);
        }

        @Override
        public boolean handleKey(KeyInput keyInput) {
            if (keyInput.key == Keyboard.KEY_F1) {
                SettingsRegistry reg = ReplayMod.instance.getSettingsRegistry();
                reg.set(Setting.SHOW_SERVER_IPS, !reg.get(Setting.SHOW_SERVER_IPS));
                reg.save();
                load();
            }

            boolean filterHasPriority = !filterTextField.getText().isEmpty();
            if (filterHasPriority && filterTextField.handleKey(keyInput)) {
                scrollY(0); // ensure we scroll to top if most entries are filtered
                return true;
            }

            if (super.handleKey(keyInput)) {
                return true;
            }

            if (!filterHasPriority && filterTextField.handleKey(keyInput)) {
                scrollY(0); // ensure we scroll to top if most entries are filtered
                return true;
            }

            return false;
        }

        @Override
        public boolean handleChar(CharInput charInput) {
            if (filterTextField.handleChar(charInput)) {
                scrollY(0); // ensure we scroll to top if most entries are filtered
                return true;
            }

            return false;
        }

        @Override
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            super.draw(renderer, size, renderInfo);

            String filter = filterTextField.getText();
            if (!filter.isEmpty()) {
                boolean anyMatches = getListPanel().calcMinSize().getHeight() > 0;

                TextRenderer fontRenderer = getFontRenderer();
                int filterTextWidth = fontRenderer.getWidth(filter);
                int filterTextHeight = fontRenderer.fontHeight;
                RecapTheme.roundedRect(renderer,
                        size.getWidth() - 3 - 2 - filterTextWidth - 4,
                        size.getHeight() - 3 - 2 - filterTextHeight - 4,
                        2 + filterTextWidth + 6,
                        2 + filterTextHeight + 6,
                        anyMatches ? RecapTheme.CARD : 0xFF3A1620);
                renderer.drawString(
                        size.getWidth() - 3 - 2 - filterTextWidth,
                        size.getHeight() - 3 - 2 - filterTextHeight,
                        anyMatches ? RecapTheme.TEXT : RecapTheme.RECORDING,
                        filter
                );
            }
        }

        @Override
        protected GuiReplayList getThis() {
            return this;
        }
    }

    public static class GuiReplayEntry extends AbstractGuiContainer<GuiReplayEntry> implements Comparable<GuiReplayEntry> {
        public final File file;
        public final GuiLabel name = new GuiLabel();
        public final GuiLabel server = new GuiLabel().setColor(RecapTheme.TEXT_DIM_COLOR);
        public final GuiLabel date = new GuiLabel().setColor(RecapTheme.TEXT_DIM_COLOR);
        public final GuiPanel infoPanel = new GuiPanel(this).setLayout(new VerticalLayout().setSpacing(2))
                .addElements(null, name, server, date);
        public final GuiLabel version = new GuiLabel(this).setColor(RecapTheme.RECORDING_COLOR);
        public final GuiImage thumbnail;
        public final GuiLabel duration = new GuiLabel();
        public final GuiPanel durationPanel = new GuiPanel().setBackgroundColor(RecapTheme.HUD_COLOR)
                .addElements(null, duration).setLayout(new CustomLayout<GuiPanel>() {
                    @Override
                    protected void layout(GuiPanel container, int width, int height) {
                        pos(duration, 2, 2);
                    }

                    @Override
                    public ReadableDimension calcMinSize(GuiContainer<?> container) {
                        ReadableDimension dimension = duration.calcMinSize();
                        return new Dimension(dimension.getWidth() + 2, dimension.getHeight() + 2);
                    }
                });
        public final GuiImage renderQueueIcon = new GuiImage()
                .setSize(10, 10)
                .setTexture(ReplayMod.TEXTURE, 40, 0, 20, 20);

        private final long dateMillis;
        private final boolean incompatible;
        private final List<RenderJob> renderQueue;

        public GuiReplayEntry(File file, ReplayMetaData metaData, Image thumbImage, List<RenderJob> renderQueue) {
            this.file = file;
            this.renderQueue = renderQueue;

            name.setText(Formatting.UNDERLINE + Utils.fileNameToReplayName(file.getName()));
            if (!StringUtils.isEmpty(metaData.getCustomServerName())) {
                server.setText(metaData.getCustomServerName());
            } else if (StringUtils.isEmpty(metaData.getServerName())
                    || !ReplayMod.instance.getSettingsRegistry().get(Setting.SHOW_SERVER_IPS)) {
                server.setI18nText("replaymod.gui.iphidden").setColor(Colors.DARK_RED);
            } else {
                server.setText(metaData.getServerName());
            }
            incompatible = !ReplayMod.isCompatible(metaData.getFileFormatVersion(), metaData.getRawProtocolVersionOr0());
            if (incompatible) {
                version.setText("Minecraft " + metaData.getMcVersion());
            }
            dateMillis = metaData.getDate();
            date.setText(new SimpleDateFormat().format(new Date(dateMillis)));
            if (thumbImage == null) {
                thumbnail = new GuiImage(DEFAULT_THUMBNAIL).setSize(30 * 16 / 9, 30);
                addElements(null, thumbnail);
            } else {
                thumbnail = new GuiImage(this).setTexture(thumbImage).setSize(30 * 16 / 9, 30);
            }
            duration.setText(Utils.convertSecondsToShortString(metaData.getDuration() / 1000));
            addElements(null, durationPanel);

            if (!renderQueue.isEmpty()) {
                renderQueueIcon.setTooltip(new GuiTooltip()
                        .setText(renderQueue.stream().map(RenderJob::getName).toArray(String[]::new)));
                addElements(null, renderQueueIcon);
            }

            setLayout(new CustomLayout<GuiReplayEntry>() {
                @Override
                protected void layout(GuiReplayEntry container, int width, int height) {
                    pos(thumbnail, 0, 0);
                    x(durationPanel, width(thumbnail) - width(durationPanel));
                    y(durationPanel, height(thumbnail) - height(durationPanel));

                    pos(infoPanel, width(thumbnail) + 5, 0);
                    pos(version, width - width(version), 0);

                    if (renderQueueIcon.getContainer() != null) {
                        pos(renderQueueIcon, width(thumbnail) - width(renderQueueIcon), 0);
                    }
                }

                @Override
                public ReadableDimension calcMinSize(GuiContainer<?> container) {
                    return new Dimension(300, thumbnail.getMinSize().getHeight());
                }
            });
        }

        @Override
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            if (renderInfo.getLayer() == 0) {
                int w = size.getWidth();
                int h = size.getHeight();

                Point mouse = new Point(renderInfo.getMouseX(), renderInfo.getMouseY());
                if (getContainer() != null) {
                    getContainer().convertFor(this, mouse);
                }
                boolean hovered = mouse.getX() >= 0 && mouse.getY() >= 0
                        && mouse.getX() < w && mouse.getY() < h;

                // Recap Mod: every replay is a card with an accent rail on hover.
                RecapTheme.roundedRect(renderer, 0, 0, w, h, hovered ? RecapTheme.ROW_HOVER : RecapTheme.ROW);
                renderer.drawRect(0, 0, 2, h, hovered ? RecapTheme.ACCENT : RecapTheme.BORDER);
                renderer.drawRect(2, h - 1, w - 2, 1, RecapTheme.withAlpha(RecapTheme.BORDER, 0.6f));
            }
            super.draw(renderer, size, renderInfo);
        }

        @Override
        protected GuiReplayEntry getThis() {
            return this;
        }

        @Override
        public int compareTo(GuiReplayEntry o) {
            return Long.compare(o.dateMillis, dateMillis);
        }
    }
}
