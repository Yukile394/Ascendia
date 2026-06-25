package exloran.ascendia.action;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.config.AscendiaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

public class InventoryActions {

    private InventoryActions() {}

    public static void dropAllPlayerInventory(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        for (Slot slot : handler.slots) {
            int id = slot.id;
            if (id < 1 || id > 45) continue;
            if (slot.getStack().isEmpty()) continue;
            client.interactionManager.clickSlot(handler.syncId, id, 1, SlotActionType.THROW, player);
        }
    }

    private static int containerSize(ScreenHandler handler) {
        return Math.max(0, handler.slots.size() - 36);
    }

    public static void dropAllContainer(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        int size = containerSize(handler);
        for (int id = 0; id < size; id++) {
            Slot slot = handler.getSlot(id);
            if (slot.getStack().isEmpty()) continue;
            client.interactionManager.clickSlot(handler.syncId, id, 1, SlotActionType.THROW, player);
        }
    }

    public static void takeAllFromContainer(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        int size = containerSize(handler);
        for (int id = 0; id < size; id++) {
            Slot slot = handler.getSlot(id);
            if (slot.getStack().isEmpty()) continue;
            client.interactionManager.clickSlot(handler.syncId, id, 0, SlotActionType.QUICK_MOVE, player);
        }
        boolean still = false;
        for (int id = 0; id < size; id++) {
            if (!handler.getSlot(id).getStack().isEmpty()) { still = true; break; }
        }
        if (still) player.sendMessage(Text.literal("§cEnvanterinde yeterli yer yok!"), true);
    }

    public static void putAllToContainer(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        int size = containerSize(handler);
        int total = handler.slots.size();
        for (int id = size; id < total; id++) {
            Slot slot = handler.getSlot(id);
            if (slot.getStack().isEmpty()) continue;
            client.interactionManager.clickSlot(handler.syncId, id, 0, SlotActionType.QUICK_MOVE, player);
        }
    }

    public static void dropTrashFromContainer(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        AscendiaConfig cfg = AscendiaClient.CONFIG;
        int size = containerSize(handler);
        int dropped = 0;

        for (int id = 0; id < size; id++) {
            Slot slot = handler.getSlot(id);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            String idStr = Registries.ITEM.getId(stack.getItem()).toString();
            boolean isChainmail = idStr.startsWith("minecraft:chainmail_");
            boolean isProt1Only = isProtection1Only(stack);
            boolean isTrash = cfg.trashItems.contains(idStr) || isChainmail || isProt1Only;
            if (!isTrash) continue;

            if (!isChainmail && !isProt1Only && cfg.protectEnchantedItems && stack.hasEnchantments()) continue;

            client.interactionManager.clickSlot(handler.syncId, id, 1, SlotActionType.THROW, player);
            dropped++;
        }

        if (dropped == 0) player.sendMessage(Text.literal("§eAtılacak çöp bulunamadı."), true);
    }

    private static boolean isProtection1Only(ItemStack stack) {
        if (!stack.hasEnchantments()) return false;
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        boolean isArmor = path.endsWith("_helmet") || path.endsWith("_chestplate")
                       || path.endsWith("_leggings") || path.endsWith("_boots");
        if (!isArmor) return false;

        ItemEnchantmentsComponent enchComp = stack.getOrDefault(
                DataComponentTypes.ENCHANTMENTS,
                ItemEnchantmentsComponent.DEFAULT
        );
        if (enchComp.getSize() != 1) return false;

        for (var entry : enchComp.getEnchantmentEntries()) {
            // RegistryKey.toString() formatı: "minecraft:protection" içerir
            String key = entry.getKey().toString();
            if (key.contains("minecraft:protection") && entry.getIntValue() == 1) return true;
        }
        return false;
    }

    public static Map<Integer, String> captureHotbar(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return Map.of();
        ScreenHandler handler = player.currentScreenHandler;
        Map<Integer, String> result = new LinkedHashMap<>();
        for (Slot slot : handler.slots) {
            int id = slot.id;
            if (id < 9 || id > 44) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            result.put(id, Registries.ITEM.getId(stack.getItem()).toString());
        }
        return result;
    }

    public static void applyPreset(MinecraftClient client) {
        AscendiaConfig cfg = AscendiaClient.CONFIG;
        if (cfg.presetSlots == null || cfg.presetSlots.isEmpty()) {
            if (client.player != null)
                client.player.sendMessage(Text.literal("§cKayıtlı bir slot dizeni yok!"), true);
            return;
        }
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;

        java.util.List<String> missing = new java.util.ArrayList<>();

        for (Map.Entry<Integer, String> entry : cfg.presetSlots.entrySet()) {
            int targetSlot = entry.getKey();
            String wantedItem = entry.getValue();

            Slot target = handler.getSlot(targetSlot);
            if (!target.getStack().isEmpty() &&
                Registries.ITEM.getId(target.getStack().getItem()).toString().equals(wantedItem)) {
                continue;
            }

            int sourceSlot = -1;
            for (Slot slot : handler.slots) {
                int id = slot.id;
                if (id < 9 || id > 44) continue;
                if (id == targetSlot) continue;
                if (slot.getStack().isEmpty()) continue;
                if (Registries.ITEM.getId(slot.getStack().getItem()).toString().equals(wantedItem)) {
                    sourceSlot = id;
                    break;
                }
            }

            if (sourceSlot == -1) {
                missing.add(wantedItem.replace("minecraft:", ""));
                continue;
            }

            client.interactionManager.clickSlot(handler.syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
            client.interactionManager.clickSlot(handler.syncId, targetSlot, 0, SlotActionType.PICKUP, player);
            if (!handler.getCursorStack().isEmpty()) {
                client.interactionManager.clickSlot(handler.syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
            }
        }

        if (!missing.isEmpty()) {
            player.sendMessage(Text.literal("§eEksik itemler: §c" + String.join(", ", missing)), true);
        } else {
            player.sendMessage(Text.literal("§aDüzenleme uygulandı!"), true);
        }
    }

    private static final int SLOT_HEAD  = 5;
    private static final int SLOT_CHEST = 6;
    private static final int SLOT_LEGS  = 7;
    private static final int SLOT_FEET  = 8;

    public static void autoEquip(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        equipBest(client, handler, SLOT_HEAD,  "_helmet");
        equipBest(client, handler, SLOT_CHEST, "_chestplate");
        equipBest(client, handler, SLOT_LEGS,  "_leggings");
        equipBest(client, handler, SLOT_FEET,  "_boots");
    }

    private static int tierRank(String path) {
        if (path.startsWith("netherite_")) return 5;
        if (path.startsWith("diamond_"))   return 4;
        if (path.startsWith("iron_"))      return 3;
        if (path.startsWith("chainmail_")) return 2;
        if (path.startsWith("golden_") || path.startsWith("gold_")) return 1;
        if (path.startsWith("leather_"))   return 0;
        return -1;
    }

    private static void equipBest(MinecraftClient client, ScreenHandler handler, int armorSlotId, String suffix) {
        PlayerEntity player = client.player;
        int bestRank = -1, bestSlotId = -1;
        for (Slot slot : handler.slots) {
            int id = slot.id;
            if (id < 9 || id > 44) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            String path = Registries.ITEM.getId(stack.getItem()).getPath();
            if (!path.endsWith(suffix)) continue;
            int rank = tierRank(path);
            if (rank > bestRank) { bestRank = rank; bestSlotId = id; }
        }
        if (bestSlotId == -1) return;
        int currentRank = -1;
        ItemStack cur = handler.getSlot(armorSlotId).getStack();
        if (!cur.isEmpty()) currentRank = tierRank(Registries.ITEM.getId(cur.getItem()).getPath());
        if (currentRank >= bestRank) return;
        client.interactionManager.clickSlot(handler.syncId, armorSlotId, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(handler.syncId, bestSlotId, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(handler.syncId, armorSlotId, 0, SlotActionType.PICKUP, player);
    }
            }
