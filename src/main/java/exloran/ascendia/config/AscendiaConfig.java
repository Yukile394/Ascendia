package exloran.ascendia.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ascendia config dosyası.
 * config/ascendia.json içinde saklanır, oyun açıkken elle düzenlenip
 * F3+T (resource reload) ile değil ama oyunu yeniden başlatarak güncellenebilir.
 */
public class AscendiaConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ascendia.json");

    // ---- Renkler (0xAARRGGBB formatında ARGB) ----
    public int buttonColor = 0xCC1B1B2A;
    public int buttonHoverColor = 0xE02A2A55;
    public int buttonBorderColor = 0xFF00E5FF;
    public int textColor = 0xFFE8FAFF;

    // ---- Çöp olarak kabul edilen item ID'leri (Çöpleri At butonu) ----
    public List<String> trashItems = new ArrayList<>(List.of(
            "minecraft:dirt",
            "minecraft:string",
            "minecraft:chain",
            "minecraft:gravel",
            "minecraft:sand",
            "minecraft:rotten_flesh",
            "minecraft:cobweb",
            "minecraft:poisonous_potato",
            "minecraft:bone"
    ));

    // true ise: büyülü (örn. Koruma 1+) hiçbir item asla çöp olarak atılmaz (güvenlik önlemi)
    public boolean protectEnchantedItems = true;

    // /pv (PlayerVaults benzeri) kasaları tanımak için sandık başlığında aranacak kelimeler
    public List<String> pvTitleKeywords = new ArrayList<>(List.of("Vault", "Kasa", "PV"));

    // Düzenle modunda sürüklenen butonların varsayılan konuma göre farkı (id -> [dx, dy])
    public Map<String, int[]> buttonOffsets = new HashMap<>();

    public static AscendiaConfig load() {
        if (Files.exists(PATH)) {
            try (var reader = Files.newBufferedReader(PATH)) {
                AscendiaConfig cfg = GSON.fromJson(reader, AscendiaConfig.class);
                if (cfg != null) {
                    return cfg;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AscendiaConfig fresh = new AscendiaConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (var writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getOffset(String id) {
        return buttonOffsets.getOrDefault(id, new int[]{0, 0});
    }

    public void setOffset(String id, int dx, int dy) {
        buttonOffsets.put(id, new int[]{dx, dy});
    }
}
