package exloran.ascendia;

import exloran.ascendia.config.AscendiaConfig;
import net.fabricmc.api.ClientModInitializer;

public class AscendiaClient implements ClientModInitializer {

    public static AscendiaConfig CONFIG;

    /** "Düzenle" butonuna basıldığında açılıp kapanan global düzenleme modu. */
    public static boolean editMode = false;

    @Override
    public void onInitializeClient() {
        CONFIG = AscendiaConfig.load();
    }
}
