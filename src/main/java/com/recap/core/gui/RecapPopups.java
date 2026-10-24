package com.recap.core.gui;

import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.popup.GuiYesNoPopup;

/**
 * Factory methods for the mod's confirmation dialogs.
 *
 * <p>jGui's own {@code GuiYesNoPopup.open} paints a translucent black slab. These wrappers use it (so the
 * behaviour - escape handling, focus, button order - stays exactly as before) but swap in the Recap surface,
 * which is what makes every confirmation in the mod look like it belongs to the same application.</p>
 */
public final class RecapPopups {
    private RecapPopups() {
    }

    /** Opens a themed yes/no dialog. */
    public static GuiYesNoPopup confirm(GuiContainer container, GuiElement... info) {
        GuiYesNoPopup popup = GuiYesNoPopup.open(container, info);
        popup.setBackgroundColor(RecapTheme.CARD_COLOR);
        return popup;
    }
}
