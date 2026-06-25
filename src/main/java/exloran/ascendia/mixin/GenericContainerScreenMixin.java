package exloran.ascendia.mixin;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.action.InventoryActions;
import exloran.ascendia.config.AscendiaConfig;
import exloran.ascendia.gui.AscendiaButton;
import exloran.ascendia.gui.ContainerType;
import exloran.ascendia.gui.PresetMenuWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> {

    @Unique private final PresetMenuWidget ascendia$presetMenu = new PresetMenuWidget();

    public GenericContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ascendia$addButtons(CallbackInfo ci) {
        AscendiaConfig cfg = AscendiaClient.CONFIG;
        ContainerType type = ascendia$detectType(cfg);
        if (type == ContainerType.NONE) return;

        // Sandık arka planının sağ üst köşesine hizala
        // x, y: HandledScreen'in protected alanları — accessor ile alıyoruz
        HandledScreenAccessor acc = (HandledScreenAccessor) this;
        int bgX = acc.ascendia$getX();
        int bgY = acc.ascendia$getY();
        int bgW = acc.ascendia$getBackgroundWidth();

        final int btnW   = 90;
        final int btnH   = 16;
        final int gap    = 4;
        final int startX = bgX + bgW + 4;
        final int startY = bgY;  // Sandık arka planının tam üstünden başla

        String[] labels = {"Herşeyi At", "Oto Ekipman", "Herşeyi Koy", "Herşeyi Al", "Çöpleri At"};
        String[] ids    = {"ctr_dropall", "ctr_autoequip", "ctr_putall", "ctr_takeall", "ctr_trash"};

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            int by = startY + i * (btnH + gap);
            AscendiaButton btn = AscendiaButton.create(
                    startX, by, btnW, btnH,
                    Text.literal(labels[i]), ids[i], true,
                    b -> ascendia$runAction(idx)
            );
            btn.applyStoredOffset(cfg);
            this.addDrawableChild(btn);
        }

        // Düzenle butonu — 5 butonun hemen altında
        int editY = startY + labels.length * (btnH + gap) + 4;
        AscendiaButton editBtn = AscendiaButton.create(
                startX, editY, btnW, btnH,
                Text.literal("Düzenle"), "ctr_edit", false,
                b -> InventoryActions.applyPreset(MinecraftClient.getInstance())
        );
        editBtn.applyStoredOffset(cfg);
        this.addDrawableChild(editBtn);
    }

    @Unique
    private void ascendia$runAction(int idx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        switch (idx) {
            case 0 -> InventoryActions.dropAllContainer(mc);
            case 1 -> InventoryActions.autoEquip(mc);
            case 2 -> InventoryActions.putAllToContainer(mc);
            case 3 -> InventoryActions.takeAllFromContainer(mc);
            case 4 -> InventoryActions.dropTrashFromContainer(mc);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void ascendia$renderOverlay(DrawContext ctx, int mx, int my, float delta, CallbackInfo ci) {
        ascendia$presetMenu.render(ctx, mx, my);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (ascendia$presetMenu.isVisible()) {
            boolean consumed = ascendia$presetMenu.mouseClicked(mx, my, button);
            if (consumed) return true;
        }

        if (button == 1) {
            HandledScreenAccessor acc = (HandledScreenAccessor) this;
            int bgX = acc.ascendia$getX();
            int bgW = acc.ascendia$getBackgroundWidth();
            int bgY = acc.ascendia$getY();
            final int btnH = 16, gap = 4;
            int startX = bgX + bgW + 4;
            int editY = bgY + 5 * (btnH + gap) + 4;

            if (mx >= startX && mx <= startX + 90 && my >= editY && my <= editY + btnH) {
                ascendia$presetMenu.show(startX - 168, (int) my - 30,
                        (net.minecraft.client.gui.screen.Screen)(Object) this);
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ascendia$presetMenu.keyPressed(key, scan, mods)) return true;
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (ascendia$presetMenu.charTyped(c, mods)) return true;
        return super.charTyped(c, mods);
    }

    @Unique
    private ContainerType ascendia$detectType(AscendiaConfig cfg) {
        String title = this.getTitle().getString().toLowerCase().trim();

        // Ender Chest — hem İngilizce hem Türkçe kontrol
        if (title.equals("ender chest") || title.equals("ender kasası")
                || title.contains("enderchest") || title.contains("ender chest")) {
            return ContainerType.ENDER_CHEST;
        }

        // PV / PlayerVault sandıkları
        for (String kw : cfg.pvTitleKeywords) {
            if (title.contains(kw.toLowerCase())) {
                return ContainerType.PLAYER_VAULT;
            }
        }

        return ContainerType.NONE;
    }
}
