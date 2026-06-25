package exloran.ascendia.gui;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.action.InventoryActions;
import exloran.ascendia.config.AscendiaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Map;

public class PresetMenuWidget {

    private static final int BG        = 0xEE0D0D1A;
    private static final int BORDER    = 0xFF00E5FF;
    private static final int BTN_SAVE  = 0xFF1A6B1A;
    private static final int BTN_DEL   = 0xFF6B1A1A;
    private static final int BTN_CLOSE = 0xFF2A2A55;
    private static final int BTN_HOV   = 0xFF00E5FF;
    private static final int TEXT_COL  = 0xFFE8FAFF;
    private static final int INFO_COL  = 0xFFFFD700;

    private boolean visible = false;
    private int menuX, menuY;

    private final int W = 160;
    private final int H = 110;
    private final int BTN_W = 68;
    private final int BTN_H = 16;

    private TextFieldWidget nameField;

    public void show(int x, int y, Screen screen) {
        this.visible = true;
        this.menuX = x;
        this.menuY = y;
        AscendiaConfig cfg = AscendiaClient.CONFIG;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        nameField = new TextFieldWidget(tr, menuX + 8, menuY + 28, W - 16, 14, Text.literal(""));
        nameField.setMaxLength(24);
        nameField.setText(cfg.presetName != null ? cfg.presetName : "");
        nameField.setPlaceholder(Text.literal("Preset adı..."));
    }

    public void hide() {
        visible = false;
        nameField = null;
    }

    public boolean isVisible() { return visible; }

    public void render(DrawContext ctx, int mouseX, int mouseY) {
        if (!visible) return;

        ctx.fill(menuX, menuY, menuX + W, menuY + H, BG);
        ctx.drawBorder(menuX, menuY, W, H, BORDER);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        ctx.drawTextWithShadow(tr, Text.literal("§b⚙ Slot Dizeni"), menuX + 8, menuY + 8, TEXT_COL);

        if (nameField != null) nameField.render(ctx, mouseX, mouseY, 0);

        AscendiaConfig cfg = AscendiaClient.CONFIG;
        String info = cfg.presetName != null
            ? "§aMevcut: §e" + cfg.presetName + " §7(" + cfg.presetSlots.size() + " slot)"
            : "§7Kayıtlı preset yok";
        ctx.drawTextWithShadow(tr, Text.literal(info), menuX + 8, menuY + 48, INFO_COL);

        int bY  = menuY + 62;
        int bX1 = menuX + 8;
        int bX2 = menuX + W - BTN_W - 8;

        boolean hovSave  = isHov(mouseX, mouseY, bX1, bY, BTN_W, BTN_H);
        boolean hovDel   = isHov(mouseX, mouseY, bX2, bY, BTN_W, BTN_H);

        ctx.fill(bX1, bY, bX1 + BTN_W, bY + BTN_H, hovSave ? BTN_HOV : BTN_SAVE);
        ctx.drawBorder(bX1, bY, BTN_W, BTN_H, BORDER);
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§a✔ Kaydet"), bX1 + BTN_W / 2, bY + 4, TEXT_COL);

        ctx.fill(bX2, bY, bX2 + BTN_W, bY + BTN_H, hovDel ? BTN_HOV : BTN_DEL);
        ctx.drawBorder(bX2, bY, BTN_W, BTN_H, BORDER);
        ctx.drawCenteredTextWithShadow(tr, Text.literal("§c✖ Sil"), bX2 + BTN_W / 2, bY + 4, TEXT_COL);

        int closeY = menuY + H - BTN_H - 6;
        boolean hovClose = isHov(mouseX, mouseY, menuX + 8, closeY, W - 16, BTN_H);
        ctx.fill(menuX + 8, closeY, menuX + W - 8, closeY + BTN_H, hovClose ? BTN_HOV : BTN_CLOSE);
        ctx.drawBorder(menuX + 8, closeY, W - 16, BTN_H, BORDER);
        ctx.drawCenteredTextWithShadow(tr, Text.literal("Kapat"), menuX + W / 2, closeY + 4, TEXT_COL);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible) return false;

        if (nameField != null) nameField.mouseClicked(mx, my, button);

        int bY  = menuY + 62;
        int bX1 = menuX + 8;
        int bX2 = menuX + W - BTN_W - 8;
        int closeY = menuY + H - BTN_H - 6;

        if (isHov(mx, my, bX1, bY, BTN_W, BTN_H)) {
            String name = nameField != null ? nameField.getText().trim() : "";
            if (name.isEmpty()) name = "Preset";
            AscendiaConfig cfg = AscendiaClient.CONFIG;
            cfg.presetName = name;
            Map<Integer, String> captured = InventoryActions.captureHotbar(MinecraftClient.getInstance());
            if (captured.isEmpty()) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§cEnvanter boş!"), true);
            } else {
                cfg.presetSlots = new java.util.LinkedHashMap<>(captured);
                cfg.save();
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§a'" + name + "' kaydedildi! (" + captured.size() + " slot)"), true);
                hide();
            }
            return true;
        }

        if (isHov(mx, my, bX2, bY, BTN_W, BTN_H)) {
            AscendiaConfig cfg = AscendiaClient.CONFIG;
            cfg.presetName = null;
            cfg.presetSlots.clear();
            cfg.save();
            if (MinecraftClient.getInstance().player != null)
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§cPreset silindi."), true);
            hide();
            return true;
        }

        if (isHov(mx, my, menuX + 8, closeY, W - 16, BTN_H)) {
            hide();
            return true;
        }

        if (mx < menuX || mx > menuX + W || my < menuY || my > menuY + H) {
            hide();
            return false;
        }

        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible || nameField == null) return false;
        return nameField.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!visible || nameField == null) return false;
        return nameField.charTyped(chr, modifiers);
    }

    private static boolean isHov(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
