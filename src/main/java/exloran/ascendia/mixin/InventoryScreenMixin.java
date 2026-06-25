package exloran.ascendia.mixin;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.action.InventoryActions;
import exloran.ascendia.config.AscendiaConfig;
import exloran.ascendia.gui.AscendiaButton;
import exloran.ascendia.gui.PresetMenuWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    @Unique private final PresetMenuWidget ascendia$presetMenu = new PresetMenuWidget();

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ascendia$addButtons(CallbackInfo ci) {
        AscendiaConfig cfg = AscendiaClient.CONFIG;
        HandledScreenAccessor acc = (HandledScreenAccessor) this;

        int bgX = acc.ascendia$getX();
        int bgY = acc.ascendia$getY();
        int bgW = acc.ascendia$getBackgroundWidth();

        final int btnW  = 90;
        final int btnH  = 16;
        final int gap   = 4;
        final int startX = bgX + bgW + 6;
        final int startY = bgY + 8;

        String[] labels = {"Herşeyi At", "Oto Ekipman", "Herşeyi Koy", "Herşeyi Al", "Çöpleri At"};
        String[] ids    = {"inv_dropall", "inv_autoequip", "inv_putall", "inv_takeall", "inv_trash"};

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            int by = startY + i * (btnH + gap);
            AscendiaButton btn = AscendiaButton.create(
                    startX, by, btnW, btnH,
                    Text.literal(labels[i]), ids[i], true,
                    b -> ascendia$runInvAction(idx)
            );
            btn.applyStoredOffset(cfg);
            this.addDrawableChild(btn);
        }

        int editY = startY + labels.length * (btnH + gap) + 4;
        AscendiaButton editBtn = AscendiaButton.create(
                startX, editY, btnW, btnH,
                Text.literal("Düzenle"), "inv_edit", false,
                b -> InventoryActions.applyPreset(MinecraftClient.getInstance())
        );
        editBtn.applyStoredOffset(cfg);
        this.addDrawableChild(editBtn);
    }

    @Unique
    private void ascendia$runInvAction(int idx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        switch (idx) {
            case 0 -> InventoryActions.dropAllPlayerInventory(mc);
            case 1 -> InventoryActions.autoEquip(mc);
            case 2 -> {}
            case 3 -> {}
            case 4 -> {}
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ascendia$onMouseClicked(double mx, double my, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ascendia$presetMenu.isVisible()) {
            boolean consumed = ascendia$presetMenu.mouseClicked(mx, my, button);
            if (consumed) { cir.setReturnValue(true); return; }
        }

        if (button == 1) {
            HandledScreenAccessor acc = (HandledScreenAccessor) this;
            int bgX = acc.ascendia$getX();
            int bgW = acc.ascendia$getBackgroundWidth();
            int bgY = acc.ascendia$getY();
            final int btnH = 16, gap = 4;
            int startX = bgX + bgW + 6;
            int editY = bgY + 8 + 5 * (btnH + gap) + 4;

            if (mx >= startX && mx <= startX + 90 && my >= editY && my <= editY + btnH) {
                ascendia$presetMenu.show(startX - 168, editY - 30, (net.minecraft.client.gui.screen.Screen)(Object)this);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void ascendia$renderOverlay(DrawContext ctx, int mx, int my, float delta, CallbackInfo ci) {
        ascendia$presetMenu.render(ctx, mx, my);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void ascendia$keyPressed(int key, int scan, int mods, CallbackInfoReturnable<Boolean> cir) {
        if (ascendia$presetMenu.keyPressed(key, scan, mods)) cir.setReturnValue(true);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void ascendia$charTyped(char c, int mods, CallbackInfoReturnable<Boolean> cir) {
        if (ascendia$presetMenu.charTyped(c, mods)) cir.setReturnValue(true);
    }
}
