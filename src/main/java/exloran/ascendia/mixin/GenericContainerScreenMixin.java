package exloran.ascendia.mixin;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.action.InventoryActions;
import exloran.ascendia.config.AscendiaConfig;
import exloran.ascendia.gui.AscendiaButton;
import exloran.ascendia.gui.ContainerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ender Chest ve /pv (PlayerVaults benzeri) kasalarını başlığından tanıyıp
 * sağ tarafa simetrik 4 buton ekler: Herşeyi At, Herşeyi Al, Herşeyi Koy, Çöpleri At.
 * Normal sandık / shulker kutusu gibi diğer GenericContainerScreen'lere DOKUNMAZ.
 */
@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> {

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    public GenericContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ascendia$addButtons(CallbackInfo ci) {
        AscendiaConfig cfg = AscendiaClient.CONFIG;
        ContainerType type = ascendia$detectType(cfg);
        if (type == ContainerType.NONE) {
            return; // ender chest veya pv değil, butonları ekleme
        }

        final int btnWidth = 90;
        final int btnHeight = 16;
        final int spacing = 4;
        final int startX = this.x + this.backgroundWidth + 6;
        final int startY = this.y + 8;

        String[] labels = {"Herşeyi At", "Herşeyi Al", "Herşeyi Koy", "Çöpleri At"};
        String[] ids = {"ctr_dropall", "ctr_takeall", "ctr_putall", "ctr_droptrash"};

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            int by = startY + i * (btnHeight + spacing);

            AscendiaButton btn = AscendiaButton.create(
                    startX, by, btnWidth, btnHeight,
                    Text.literal(labels[i]), ids[i], true,
                    b -> ascendia$runAction(index)
            );
            btn.applyStoredOffset(cfg);
            this.addDrawableChild(btn);
        }
    }

    private void ascendia$runAction(int index) {
        MinecraftClient client = MinecraftClient.getInstance();
        switch (index) {
            case 0 -> InventoryActions.dropAllContainer(client);
            case 1 -> InventoryActions.takeAllFromContainer(client);
            case 2 -> InventoryActions.putAllToContainer(client);
            case 3 -> InventoryActions.dropTrashFromContainer(client);
            default -> {}
        }
    }

    private ContainerType ascendia$detectType(AscendiaConfig cfg) {
        String titleStr = this.getTitle().getString();
        String enderTitle = Text.translatable("container.enderchest").getString();

        if (titleStr.equalsIgnoreCase(enderTitle)) {
            return ContainerType.ENDER_CHEST;
        }

        for (String keyword : cfg.pvTitleKeywords) {
            if (titleStr.toLowerCase().contains(keyword.toLowerCase())) {
                return ContainerType.PLAYER_VAULT;
            }
        }

        return ContainerType.NONE;
    }
}
