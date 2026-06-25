package exloran.ascendia.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AscendiaConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ascendia.json");

    public int buttonColor      = 0xCC1B1B2A;
    public int buttonHoverColor = 0xE02A2A55;
    public int buttonBorderColor= 0xFF00E5FF;
    public int textColor        = 0xFFE8FAFF;

    public List<String> trashItems = new ArrayList<>(List.of(
            "minecraft:dirt",
            "minecraft:string",
            "minecraft:chain",
            "minecraft:gravel",
            "minecraft:sand",
            "minecraft:rotten_flesh",
            "minecraft:cobweb",
            "minecraft:poisonous_potato",
            "minecraft:bone",
            "minecraft:chainmail_helmet",
            "minecraft:chainmail_chestplate",
            "minecraft:chainmail_leggings",
            "minecraft:chainmail_boots"
    ));

    public boolean protectEnchantedItems = true;

    public List<String> pvTitleKeywords = new ArrayList<>(List.of("Vault", "Kasa", "PV"));

    public Map<String, int[]> buttonOffsets = new HashMap<>();

    public String presetName = null;
    public Map<Integer, String> presetSlots = new LinkedHashMap<>();

    public static AscendiaConfig load() {
        if (Files.exists(PATH)) {
            try (var r = Files.newBufferedReader(PATH)) {
                AscendiaConfig cfg = GSON.fromJson(r, AscendiaConfig.class);
                if (cfg != null) {
                    if (cfg.presetSlots == null) cfg.presetSlots = new LinkedHashMap<>();
                    return cfg;
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
        AscendiaConfig fresh = new AscendiaConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (var w = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public int[] getOffset(String id) {
        return buttonOffsets.getOrDefault(id, new int[]{0, 0});
    }

    public void setOffset(String id, int dx, int dy) {
        buttonOffsets.put(id, new int[]{dx, dy});
    }
}
