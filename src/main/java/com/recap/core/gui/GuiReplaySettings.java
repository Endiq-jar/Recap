package com.recap.core.gui;

import com.recap.core.SettingsRegistry;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiVerticalList;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
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
 * <p>Layout: a category rail on the left and a scrollable stack of cards on the right. Selecting a category
 * filters the cards, which keeps the screen usable no matter how many settings the mod (or an add-on)
 * registers. All chrome, rows and controls use the {@link RecapTheme}, so the screen looks the same on
 * every supported Minecraft version.</p>
 */
public class GuiReplaySettings extends RecapScreen<GuiReplaySettings> {

    private static final int OUTER_PADDING = 18;
    private static final int RAIL_WIDTH = 138;
    private static final int RAIL_GAP = 12;
    private static final int MAX_CONTENT_WIDTH = 620;

    /** Categories which have a dedicated translation, everything else is derived from the category name. */
    private static final Map<String, String> CATEGORY_KEYS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYS.put("core", "recap.gui.settings.category.core");
        CATEGORY_KEYS.put("recording", "recap.gui.settings.category.recording");
        CATEGORY_KEYS.put("replay", "recap.gui.settings.category.replay");
        CATEGORY_KEYS.put("render", "recap.gui.settings.category.render");
        CATEGORY_KEYS.put("simplepathing", "recap.gui.settings.category.pathing");
        CATEGORY_KEYS.put("advanced", "recap.gui.settings.category.advanced");
    }

    private final GuiVerticalList list = new GuiVerticalList().setDrawShadow(false).setDrawSlider(true);
    private final GuiPanel cardsPanel = new GuiPanel().setLayout(new VerticalLayout().setSpacing(10));
    private final GuiPanel rail = new GuiPanel().setLayout(new VerticalLayout().setSpacing(3));
    private final List<RecapCard> cards = new ArrayList<>();
    private final Map<String, List<RecapCard>> cardsByCategory = new LinkedHashMap<>();
    private final List<CategoryEntry> categoryEntries = new ArrayList<>();

    private String selectedCategory;
    /** Width available to the cards; updated from the layout pass. */
    private int contentWidth = 440;

    public GuiReplaySettings(final net.minecraft.client.gui.screen.Screen parent,
                             final SettingsRegistry settingsRegistry) {
        setHeader(I18n.translate("recap.gui.settings.title"),
                I18n.translate("recap.gui.settings.subtitle"));
        setFooterHint(I18n.translate("recap.gui.settings.hint"));

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
                card.setTag(String.valueOf(card.getRowCount()));
                cards.add(card);
                cardsByCategory.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(card);
            }
        }

        buildRail();
        rebuildCards();

        final RecapButton doneButton = new RecapButton(this).setVariant(RecapButton.Variant.PRIMARY).setSize(160, 22);
        doneButton.setI18nLabel("gui.done");
        // Note: must be an anonymous class with a block body - the openScreen -> gui.setScreen
        // pattern of the preprocessor is only applied to statements inside a block.
        doneButton.onClick(new Runnable() {
            @Override
            public void run() {
                getMinecraft().openScreen(parent);
            }
        });

        list.getListPanel().addElements(null, cardsPanel);

        setLayout(new CustomLayout<GuiReplaySettings>() {
            @Override
            protected void layout(GuiReplaySettings container, int width, int height) {
                int availableWidth = Math.min(MAX_CONTENT_WIDTH, width - OUTER_PADDING * 2);
                int left = (width - availableWidth) / 2;
                int top = CONTENT_TOP;
                int bottom = height - FOOTER_SPACE;

                pos(rail, left, top + 2);
                size(rail, RAIL_WIDTH, Math.max(20, bottom - top - 4));

                int cardSpace = availableWidth - RAIL_WIDTH - RAIL_GAP;
                pos(list, left + RAIL_WIDTH + RAIL_GAP, top);
                size(list, cardSpace, Math.max(20, bottom - top));
                GuiReplaySettings.this.contentWidth = cardSpace;
                for (RecapCard card : cards) {
                    card.setCardWidth(cardSpace);
                    card.setSize(cardSpace, card.getPreferredHeight());
                }

                pos(doneButton, left + availableWidth - 160, height - 32);
            }
        });
    }

    /** Builds the category navigation: "All" followed by one entry per category that has settings. */
    private void buildRail() {
        CategoryEntry all = new CategoryEntry(I18n.translate("recap.gui.settings.category.all"), null);
        categoryEntries.add(all);
        for (String category : cardsByCategory.keySet()) {
            categoryEntries.add(new CategoryEntry(categoryTitle(category), category));
        }
        for (CategoryEntry entry : categoryEntries) {
            entry.button.onClick(new Runnable() {
                @Override
                public void run() {
                    selectedCategory = entry.category;
                    for (CategoryEntry other : categoryEntries) {
                        other.button.setActive(java.util.Objects.equals(other.category, selectedCategory));
                    }
                    rebuildCards();
                }
            });
            rail.addElements(new VerticalLayout.Data(0), entry.button);
        }
        categoryEntries.get(0).button.setActive(true);
    }

    /** Shows the cards of the selected category (or all of them). */
    private void rebuildCards() {
        for (GuiElement child : new ArrayList<>(cardsPanel.getChildren())) {
            cardsPanel.removeElement(child);
        }
        List<RecapCard> visible = selectedCategory == null
                ? cards
                : cardsByCategory.getOrDefault(selectedCategory, cards);
        for (RecapCard card : visible) {
            card.setCardWidth(contentWidth);
            card.setSize(contentWidth, card.getPreferredHeight());
            cardsPanel.addElements(new VerticalLayout.Data(0), card);
        }
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
            final RecapToggle<String> toggle = new RecapToggle<>();
            toggle.setSize(RecapCard.CONTROL_WIDTH, RecapCard.CONTROL_HEIGHT);
            toggle.setValues(I18n.translate("options.on"), I18n.translate("options.off"));
            toggle.setSelected(settingsRegistry.get(booleanKey) ? 0 : 1);
            toggle.onClick(new Runnable() {
                @Override
                public void run() {
                    settingsRegistry.set(booleanKey, toggle.getSelected() == 0);
                    settingsRegistry.save();
                }
            });
            return toggle;
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
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            // Rail background: a slightly recessed column behind the category buttons.
            int railX = (size.getWidth() - Math.min(MAX_CONTENT_WIDTH, size.getWidth() - OUTER_PADDING * 2)) / 2;
            RecapTheme.roundedRect(renderer, railX - 6, CONTENT_TOP - 6, RAIL_WIDTH + 12,
                    Math.max(20, size.getHeight() - FOOTER_SPACE - CONTENT_TOP + 8), 0x66101523);
            renderer.drawRect(railX + RAIL_WIDTH + 6, CONTENT_TOP, 1,
                    Math.max(1, size.getHeight() - FOOTER_SPACE - CONTENT_TOP), RecapTheme.BORDER);
        }
        super.draw(renderer, size, renderInfo);
    }

    @Override
    protected GuiReplaySettings getThis() {
        return this;
    }

    /** Convenience accessor so that other code can add its own section to this screen. */
    public GuiPanel getListPanel() {
        return list.getListPanel();
    }

    /** A single category button in the left rail. */
    private static final class CategoryEntry {
        private final RecapButton button;
        private final String category;

        private CategoryEntry(String label, String category) {
            this.category = category;
            this.button = new RecapButton().setVariant(RecapButton.Variant.GHOST).setSize(RAIL_WIDTH, 22);
            this.button.setLabel(label);
        }
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
