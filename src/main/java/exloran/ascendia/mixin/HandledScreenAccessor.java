package exloran.ascendia.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x") int ascendia$getX();
    @Accessor("y") int ascendia$getY();
    @Accessor("backgroundWidth") int ascendia$getBackgroundWidth();
    @Accessor("backgroundHeight") int ascendia$getBackgroundHeight();
}
