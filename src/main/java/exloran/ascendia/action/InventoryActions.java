package exloran.ascendia.action;

import exloran.ascendia.AscendiaClient;
import exloran.ascendia.config.AscendiaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Ascendia butonlarının arkasındaki tüm envanter/kasa işlemleri.
 * Hepsi normal "click slot" paketleri ile çalışır (vanilla shift-click / Q
 * tuşu ile aynı davranış), bu yüzden sunucu tarafında hiçbir şeye gerek yoktur.
 */
public class InventoryActions {

    private InventoryActions() {}

    // ===================== ENVANTER (Oyuncu Envanteri) =====================

    /** Zırh + kalkan slotu + ana envanter + hotbar dahil her şeyi yere atar. */
    public static void dropAllPlayerInventory(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        for (Slot slot : handler.slots) {
            int id = slot.id;
            if (id < 1 || id > 45) continue; // 0 = crafting çıktı slotu, dokunma
            if (slot.getStack().isEmpty()) continue;
            client.interactionManager.clickSlot(handler.syncId, id, 1, SlotActionType.THROW, player);
        }
    }

    // ===================== ENDER CHEST / PV KASASI =====================

    private static int containerSize(ScreenHandler handler) {
        // GenericContainerScreenHandler'da son 36 slot her zaman oyuncu envanteri + hotbar'dır.
        return Math.max(0, handler.slots.size() - 36);
    }

    /** Açık olan kasadaki (oyuncu envanteri HARİÇ) her şeyi yere atar. */
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

    /** Açık olan kasadaki her şeyi envantere alır. Yer yoksa uyarı mesajı gösterir. */
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

        boolean stillHasItems = false;
        for (int id = 0; id < size; id++) {
            if (!handler.getSlot(id).getStack().isEmpty()) {
                stillHasItems = true;
                break;
            }
        }
        if (stillHasItems) {
            player.sendMessage(Text.literal("§cEnvanterinde yeterli yer yok! Bazı eşyalar kasada kaldı."), true);
        }
    }

    /** Oyuncu envanterindeki her şeyi açık olan kasaya koyar. */
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

    /** Açık olan kasadaki, config'de tanımlı çöp itemlerini yere atar. Büyülü itemlere dokunmaz. */
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
            if (cfg.protectEnchantedItems && stack.hasEnchantments()) continue; // korumalı (örn. Koruma 1+) set parçası, atlama

            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            if (cfg.trashItems.contains(itemId.toString())) {
                client.interactionManager.clickSlot(handler.syncId, id, 1, SlotActionType.THROW, player);
                dropped++;
            }
        }

        if (dropped == 0) {
            player.sendMessage(Text.literal("§eAtılacak çöp bulunamadı."), true);
        }
    }

    // ===================== OTO EKİPMAN =====================

    private static final int SLOT_HEAD = 5;
    private static final int SLOT_CHEST = 6;
    private static final int SLOT_LEGS = 7;
    private static final int SLOT_FEET = 8;

    /** Envanterdeki en iyi zırh setini (netherite > elmas > demir > zincir > altın > deri) otomatik giyer. */
    public static void autoEquip(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;
        ScreenHandler handler = player.currentScreenHandler;
        equipBest(client, handler, SLOT_HEAD, "_helmet");
        equipBest(client, handler, SLOT_CHEST, "_chestplate");
        equipBest(client, handler, SLOT_LEGS, "_leggings");
        equipBest(client, handler, SLOT_FEET, "_boots");
    }

    private static int tierRank(String itemPath) {
        if (itemPath.startsWith("netherite_")) return 5;
        if (itemPath.startsWith("diamond_")) return 4;
        if (itemPath.startsWith("iron_")) return 3;
        if (itemPath.startsWith("chainmail_")) return 2;
        if (itemPath.startsWith("golden_") || itemPath.startsWith("gold_")) return 1;
        if (itemPath.startsWith("leather_")) return 0;
        return -1;
    }

    private static void equipBest(MinecraftClient client, ScreenHandler handler, int armorSlotId, String suffix) {
        PlayerEntity player = client.player;
        int bestRank = -1;
        int bestSlotId = -1;

        for (Slot slot : handler.slots) {
            int id = slot.id;
            if (id < 9 || id > 44) continue; // sadece ana envanter + hotbar taranır
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            String path = Registries.ITEM.getId(stack.getItem()).getPath();
            if (!path.endsWith(suffix)) continue;
            int rank = tierRank(path);
            if (rank > bestRank) {
                bestRank = rank;
                bestSlotId = id;
            }
        }

        if (bestSlotId == -1) return; // envanterde uygun zırh parçası yok

        Slot currentArmor = handler.getSlot(armorSlotId);
        ItemStack currentStack = currentArmor.getStack();
        int currentRank = -1;
        if (!currentStack.isEmpty()) {
            currentRank = tierRank(Registries.ITEM.getId(currentStack.getItem()).getPath());
        }

        if (currentRank >= bestRank) return; // üzerindeki zaten aynı veya daha iyi

        // 3 tıklamalı klasik swap: zırh slotunu boşalt -> yeni item ile değiştir -> zırh slotuna koy
        client.interactionManager.clickSlot(handler.syncId, armorSlotId, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(handler.syncId, bestSlotId, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(handler.syncId, armorSlotId, 0, SlotActionType.PICKUP, player);
    }
}
