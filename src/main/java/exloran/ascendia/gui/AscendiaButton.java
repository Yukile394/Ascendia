package exloran.ascendia.gui;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.config.AscendiaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Ascendia butonu.
 * - Renkleri config'den okur (renk ayarı oradan değiştirilebilir).
 * - draggable = true ise, AscendiaClient.editMode true olduğunda mouse ile
 *   sürüklenebilir ve serbest bırakıldığında konumunu config'e kaydeder.
 */
public class AscendiaButton extends ButtonWidget {

    private final String id;
    private final boolean draggable;
    private final int defaultX;
    private final int defaultY;

    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;

    protected AscendiaButton(int x, int y, int width, int height, Text message, String id, boolean draggable, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.id = id;
        this.draggable = draggable;
        this.defaultX = x;
        this.defaultY = y;
    }

    public static AscendiaButton create(int x, int y, int width, int height, Text message, String id, boolean draggable, PressAction onPress) {
        return new AscendiaButton(x, y, width, height, message, id, draggable, onPress);
    }

    /** Config'de kayıtlı sürükleme farkını (varsa) varsayılan konuma uygular. */
    public void applyStoredOffset(AscendiaConfig config) {
        int[] off = config.getOffset(id);
        this.setX(this.defaultX + off[0]);
        this.setY(this.defaultY + off[1]);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (draggable && AscendiaClient.editMode && button == 0 && this.isMouseOver(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = (int) mouseX - this.getX();
            dragOffsetY = (int) mouseY - this.getY();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            this.setX((int) mouseX - dragOffsetX);
            this.setY((int) mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragging) {
            dragging = false;
            AscendiaConfig cfg = AscendiaClient.CONFIG;
            cfg.setOffset(id, this.getX() - defaultX, this.getY() - defaultY);
            cfg.save();
        }
        super.onRelease(mouseX, mouseY);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        AscendiaConfig cfg = AscendiaClient.CONFIG;
        int bg = this.isHovered() || dragging ? cfg.buttonHoverColor : cfg.buttonColor;

        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), bg);
        context.drawBorder(this.getX(), this.getY(), this.getWidth(), this.getHeight(), cfg.buttonBorderColor);

        int textY = this.getY() + (this.getHeight() - 8) / 2;
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                this.getMessage(),
                this.getX() + this.getWidth() / 2,
                textY,
                cfg.textColor
        );
    }
}
