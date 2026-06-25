package exloran.ascendia.mixin;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.action.InventoryActions;
import exloran.ascendia.config.AscendiaConfig;
import exloran.ascendia.gui.AscendiaButton;
import exloran.ascendia.mixin.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

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
        int bgH = acc.ascendia$getBackgroundHeight();

        final int btnWidth = 70;
        final int btnHeight = 16;
        final int spacing = 4;
        final int totalWidth = btnWidth * 3 + spacing * 2;
        final int startX = bgX + bgW / 2 - totalWidth / 2;
        final int rowY = bgY + bgH + 4;

        AscendiaButton editBtn = AscendiaButton.create(
                startX, rowY, btnWidth, btnHeight,
                Text.literal("Düzenle"), "inv_edit", false,
                btn -> {
                    AscendiaClient.editMode = !AscendiaClient.editMode;
                    btn.setMessage(Text.literal(AscendiaClient.editMode ? "Kaydet" : "Düzenle"));
                }
        );

        AscendiaButton autoEquipBtn = AscendiaButton.create(
                startX + btnWidth + spacing, rowY, btnWidth, btnHeight,
                Text.literal("Oto Ekipman"), "inv_autoequip", true,
                btn -> InventoryActions.autoEquip(MinecraftClient.getInstance())
        );

        AscendiaButton dropAllBtn = AscendiaButton.create(
                startX + (btnWidth + spacing) * 2, rowY, btnWidth, btnHeight,
                Text.literal("Herşeyi At"), "inv_dropall", true,
                btn -> InventoryActions.dropAllPlayerInventory(MinecraftClient.getInstance())
        );

        editBtn.applyStoredOffset(cfg);
        autoEquipBtn.applyStoredOffset(cfg);
        dropAllBtn.applyStoredOffset(cfg);

        this.addDrawableChild(editBtn);
        this.addDrawableChild(autoEquipBtn);
        this.addDrawableChild(dropAllBtn);
    }
}
