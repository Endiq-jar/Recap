package com.recap.extras.playeroverview;

import com.recap.core.gui.RecapCheckbox;
import com.recap.core.gui.RecapPanel;
import com.recap.core.gui.RecapTheme;
import com.recap.replay.RecapModReplay;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiClickable;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.container.GuiVerticalList;
import de.johni0702.minecraft.gui.element.GuiCheckbox;
import de.johni0702.minecraft.gui.element.GuiImage;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.element.GuiTooltip;
import de.johni0702.minecraft.gui.element.IGuiCheckbox;
import de.johni0702.minecraft.gui.function.Click;
import de.johni0702.minecraft.gui.function.Closeable;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

//#if MC>=10904
import net.minecraft.entity.effect.StatusEffects;
//#else
//$$ import net.minecraft.potion.Potion;
//#endif

//#if MC>=10800
import net.minecraft.client.render.entity.PlayerModelPart;
//#endif

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayerOverviewGui extends GuiScreen implements Closeable {
    protected static final int ENTRY_WIDTH = 200;

    public final GuiPanel contentPanel = new RecapPanel().setHeaderEnabled(false);
    public final GuiLabel spectateLabel = new GuiLabel(contentPanel)
            .setI18nText("recap.gui.playeroverview.spectate");
    public final GuiLabel visibleLabel = new GuiLabel(contentPanel)
            .setI18nText("recap.gui.playeroverview.visible");
    public final GuiVerticalList playersScrollable = new GuiVerticalList(contentPanel)
            .setDrawSlider(true).setDrawShadow(true);
    public final GuiCheckbox saveCheckbox = new RecapCheckbox(contentPanel)
            .setTooltip(new GuiTooltip().setI18nText("recap.gui.playeroverview.remembersettings.description"))
            .setI18nLabel("recap.gui.playeroverview.remembersettings");
    public final GuiCheckbox checkAll = new RecapCheckbox(contentPanel){
        @Override
        public void onClick(Click click) {
            playersScrollable.invokeAll(IGuiCheckbox.class, e -> e.setChecked(true));
        }
    }.setLabel("").setChecked(true).setTooltip(new GuiTooltip().setI18nText("recap.gui.playeroverview.showall"));
    public final GuiCheckbox uncheckAll = new RecapCheckbox(contentPanel){
        @Override
        public void onClick(Click click) {
            playersScrollable.invokeAll(IGuiCheckbox.class, e -> e.setChecked(false));
        }
    }.setLabel("").setChecked(false).setTooltip(new GuiTooltip().setI18nText("recap.gui.playeroverview.hideall"));

    {
        setBackground(Background.NONE);
        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                size(contentPanel, ENTRY_WIDTH + 34, height - 88);
                pos(contentPanel, width / 2 - width(contentPanel) / 2, 78);
            }
        });
        contentPanel.setLayout(new CustomLayout<GuiPanel>() {
            @Override
            protected void layout(GuiPanel container, int width, int height) {
                pos(spectateLabel, 12, 12);
                pos(visibleLabel, width - 12 - width(visibleLabel), 12);
                pos(playersScrollable, 12, y(spectateLabel) + height(spectateLabel) + 6);
                size(playersScrollable, width - 24, height - 20 - height(saveCheckbox) - y(playersScrollable));
                pos(saveCheckbox, 12, height - 10 - height(saveCheckbox));
                pos(uncheckAll, width - width(uncheckAll) - 10, height - height(uncheckAll) - 10);
                pos(checkAll, x(uncheckAll) - 3 - width(checkAll), y(uncheckAll));
            }
        });
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            RecapTheme.chrome(renderer, size.getWidth(), size.getHeight(),
                    net.minecraft.client.resource.language.I18n.translate("recap.input.playeroverview"),
                    net.minecraft.client.resource.language.I18n.translate("recap.gui.playeroverview.hint"));
        }
        super.draw(renderer, size, renderInfo);
    }

    private final PlayerOverview extra;

    public PlayerOverviewGui(final PlayerOverview extra, List<PlayerEntity> players) {
        this.extra = extra;

        Collections.sort(players, new PlayerComparator()); // Sort by name, spectators last
        for (final PlayerEntity p : players) {
            if (!(p instanceof AbstractClientPlayerEntity)) continue;
            final Identifier texture = ((AbstractClientPlayerEntity) p).getSkinTexture();
            final GuiClickable panel = new GuiClickable().setLayout(new HorizontalLayout().setSpacing(2)).addElements(
                    new HorizontalLayout.Data(0.5), new GuiImage() {
                        @Override
                        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
                            renderer.bindTexture(texture);
                            renderer.drawTexturedRect(0, 0, 8, 8, 16, 16, 8, 8, 64, 64);
                            //#if MC>=10809
                            if (p.isPartVisible(PlayerModelPart.HAT)) {
                            //#else
                            //#if MC>=10800
                            //$$ if (p.func_175148_a(EnumPlayerModelParts.HAT)) {
                            //#else
                            //$$ {
                            //#endif
                            //#endif
                                renderer.drawTexturedRect(0, 0, 40, 8, size.getWidth(), size.getHeight(), 8, 8, 64, 64);
                            }
                        }
                    }.setSize(16, 16),
                    new GuiLabel().setText(
                            //#if MC>=11400
                            p.getName().getString()
                            //#else
                            //#if MC>=10800
                            //$$ p.getName()
                            //#else
                            //$$ p.getDisplayName()
                            //#endif
                            //#endif
                    ).setColor(isSpectator(p) ? RecapTheme.TEXT_FAINT_COLOR : RecapTheme.TEXT_COLOR)
            ).onClick(new Runnable() {
                @Override
                public void run() {
                    RecapModReplay.instance.getReplayHandler().spectateEntity(p);
                }
            });
            final GuiCheckbox checkbox = new RecapCheckbox() {
                @Override
                public GuiCheckbox setChecked(boolean checked) {
                    extra.setHidden(p.getUuid(), !checked);
                    return super.setChecked(checked);
                }
            }.setChecked(!extra.isHidden(p.getUuid()));
            new GuiPanel(playersScrollable.getListPanel()).setLayout(new CustomLayout<GuiPanel>() {
                @Override
                protected void layout(GuiPanel container, int width, int height) {
                    pos(panel, 5, 0);
                    pos(checkbox, width - width(checkbox) - 5, height / 2 - height(checkbox) / 2);
                }

                @Override
                public ReadableDimension calcMinSize(GuiContainer<?> container) {
                    return new Dimension(ENTRY_WIDTH, panel.getMinSize().getHeight());
                }
            }).addElements(null, panel, checkbox);
        }
        saveCheckbox.setChecked(extra.isSavingEnabled()).onClick(new Runnable() {
            @Override
            public void run() {
                extra.setSavingEnabled(saveCheckbox.isChecked());
            }
        });

        RecapModReplay.instance.getReplayHandler().getOverlay().setVisible(false);
    }

    @Override
    public void close() {
        RecapModReplay.instance.getReplayHandler().getOverlay().setVisible(true);
        extra.saveHiddenPlayers();
    }

    private static boolean isSpectator(PlayerEntity e) {
        //#if MC>=10904
        return e.isInvisible() && e.getStatusEffect(StatusEffects.INVISIBILITY) == null;
        //#else
        //$$ return e.isInvisible() && e.getActivePotionEffect(Potion.invisibility) == null;
        //#endif
    }

    private static final class PlayerComparator implements Comparator<PlayerEntity> {
        @Override
        public int compare(PlayerEntity o1, PlayerEntity o2) {
            if (isSpectator(o1) && !isSpectator(o2)) return 1;
            if (isSpectator(o2) && !isSpectator(o1)) return -1;
            //#if MC>=11400
            return o1.getName().getString().compareToIgnoreCase(o2.getName().getString());
            //#else
            //#if MC>=10800
            //$$ return o1.getName().compareToIgnoreCase(o2.getName());
            //#else
            //$$ return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
            //#endif
            //#endif
        }
    }
}
