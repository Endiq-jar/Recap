package com.replaymod.core.gui;

import com.replaymod.core.SettingsRegistry;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiVerticalList;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.element.GuiToggleButton;
import de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.utils.Consumer;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.resource.language.I18n;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Recap Mod settings screen.
 *
 * <p>Settings are grouped into cards by their registry category instead of being spread over two bare
 * columns, so related options stay together and the screen scales with the number of settings. The list
 * is scrollable and all chrome (header, cards, button) is drawn by the {@link RecapTheme}.</p>
 */
public class GuiReplaySettings extends RecapScreen<GuiReplaySettings> {

    private static final int OUTER_PADDING = 18;
    private static final int HEADER_SPACE = 52;
    private static final int FOOTER_SPACE = 52;
    private static final int MAX_LIST_WIDTH = 560;

    /** Categories which have a dedicated translation, everything else is derived from the category name. */
    private static final Map<String, String> CATEGORY_KEYS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYS.put("core", "replaymod.gui.settings.category.core");
        CATEGORY_KEYS.put("recording", "replaymod.gui.settings.category.recording");
        CATEGORY_KEYS.put("replay", "replaymod.gui.settings.category.replay");
        CATEGORY_KEYS.put("render", "replaymod.gui.settings.category.render");
        CATEGORY_KEYS.put("simplepathing", "replaymod.gui.settings.category.pathing");
        CATEGORY_KEYS.put("advanced", "replaymod.gui.settings.category.advanced");
    }

    private final GuiVerticalList list = new GuiVerticalList().setDrawShadow(false).setDrawSlider(true);
    private final List<RecapCard> cards = new ArrayList<>();

    public GuiReplaySettings(final net.minecraft.client.gui.screen.Screen parent,
                             final SettingsRegistry settingsRegistry) {
        setHeader(I18n.translate("replaymod.gui.settings.title"),
                I18n.translate("replaymod.gui.settings.subtitle"));
        setFooterHint(I18n.translate("replaymod.gui.settings.hint"));

        Map<String, List<SettingsRegistry.SettingKey<?>>> byCategory = new LinkedHashMap<>();
        for (SettingsRegistry.SettingKey<?> key : settingsRegistry.getSettings()) {
            if (key.getDisplayString() == null) {
                continue;
            }
            String category = key.getCategory() == null ? "core" : key.getCategory();
            byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(key);
        }

        for (Map.Entry<String, List<SettingsRegistry.SettingKey<?>>> entry : byCategory.entrySet()) {
            RecapCard card = new RecapCard(categoryTitle(entry.getKey()));
            for (SettingsRegistry.SettingKey<?> key : entry.getValue()) {
                GuiElement<?> control = createControl(settingsRegistry, key);
                if (control != null) {
                    card.addRow(I18n.translate(key.getDisplayString()), control);
                }
            }
            if (card.getRowCount() > 0) {
                cards.add(card);
                list.getListPanel().addElements(null, card);
            }
        }

        RecapButton doneButton = new RecapButton().setI18nLabel("gui.done").setSize(160, 20);
        doneButton.onClick(() -> getMinecraft().openScreen(parent));

        setLayout(new CustomLayout<GuiReplaySettings>() {
            @Override
            protected void layout(GuiReplaySettings container, int width, int height) {
                int listWidth = Math.min(MAX_LIST_WIDTH, width - OUTER_PADDING * 2);
                pos(list, (width - listWidth) / 2, HEADER_SPACE);
                size(list, listWidth, height - HEADER_SPACE - FOOTER_SPACE);

                int cardWidth = listWidth - 12;
                for (RecapCard card : cards) {
                    card.setCardWidth(cardWidth);
                    card.setSize(cardWidth, card.getPreferredHeight());
                }

                pos(doneButton, width / 2 - 80, height - 34);
            }
        });
    }

    private static String categoryTitle(String category) {
        String translationKey = CATEGORY_KEYS.get(category);
        if (translationKey != null) {
            String translated = I18n.translate(translationKey);
            if (!translated.equals(translationKey)) {
                return translated;
            }
        }
        return Character.toUpperCase(category.charAt(0)) + category.substring(1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static GuiElement<?> createControl(SettingsRegistry settingsRegistry,
                                               final SettingsRegistry.SettingKey<?> key) {
        if (key.getDefault() instanceof Boolean) {
            final SettingsRegistry.SettingKey<Boolean> booleanKey = (SettingsRegistry.SettingKey<Boolean>) key;
            final GuiToggleButton button = new GuiToggleButton<>()
                    .setSelected(settingsRegistry.get(booleanKey) ? 0 : 1)
                    .setValues(I18n.translate("options.on"), I18n.translate("options.off"));
            button.onClick(() -> {
                settingsRegistry.set(booleanKey, button.getSelected() == 0);
                settingsRegistry.save();
            });
            return button;
        } else if (key instanceof SettingsRegistry.MultipleChoiceSettingKey) {
            final SettingsRegistry.MultipleChoiceSettingKey<?> multipleChoiceKey =
                    (SettingsRegistry.MultipleChoiceSettingKey<?>) key;
            List<?> values = multipleChoiceKey.getChoices();
            MultipleChoiceDropdownEntry[] entries = new MultipleChoiceDropdownEntry[values.size()];
            int selected = 0;
            Object currentValue = settingsRegistry.get(multipleChoiceKey);
            for (int i = 0; i < entries.length; i++) {
                Object value = values.get(i);
                entries[i] = new MultipleChoiceDropdownEntry(value, I18n.translate(value.toString()));
                if (currentValue.equals(value)) {
                    selected = i;
                }
            }
            final GuiDropdownMenu<MultipleChoiceDropdownEntry> menu =
                    new GuiDropdownMenu<MultipleChoiceDropdownEntry>() {
                        @Override
                        protected ReadableDimension calcMinSize() {
                            ReadableDimension size = super.calcMinSize();
                            if (size.getWidth() > RecapCard.CONTROL_WIDTH) {
                                return new Dimension(RecapCard.CONTROL_WIDTH, size.getHeight());
                            }
                            return size;
                        }
                    }.setValues(entries);
            menu.setSelected(selected).onSelection(new Consumer<Integer>() {
                @Override
                public void consume(Integer index) {
                    settingsRegistry.set((SettingsRegistry.SettingKey) multipleChoiceKey,
                            menu.getSelectedValue().value);
                    settingsRegistry.save();
                }
            });
            return menu;
        }
        return null;
    }

    @Override
    protected GuiReplaySettings getThis() {
        return this;
    }

    /** Convenience accessor so that other code can add its own section to this screen. */
    public GuiPanel getListPanel() {
        return list.getListPanel();
    }

    private static class MultipleChoiceDropdownEntry {
        private final Object value;
        private final String text;

        private MultipleChoiceDropdownEntry(Object value, String text) {
            this.value = value;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
