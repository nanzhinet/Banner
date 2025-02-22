package com.mohistmc.banner;

import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.banner.libraries.CustomLibraries;
import com.mohistmc.banner.libraries.DefaultLibraries;
import com.mohistmc.banner.network.download.UpdateUtils;
import com.mohistmc.banner.plugins.BannerProxySelector;
import com.mohistmc.banner.util.EulaUtil;
import com.mohistmc.banner.util.I18n;
import com.mohistmc.i18n.i18n;
import io.izzel.arclight.mixin.injector.EjectorInfo;
import java.net.ProxySelector;
import java.util.Scanner;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class BannerMCStart {

    public static i18n I18N;
    public static final Logger LOGGER = LogManager.getLogger("BannerMC");
    public static final float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));

    public static void run() throws Exception {
        InjectionInfo.register(EjectorInfo.class);
        BannerConfigUtil.copyBannerConfig();
        BannerConfigUtil.lang();
        BannerConfigUtil.i18n();
        BannerConfigUtil.initAllNeededConfig();
        if (BannerConfigUtil.showLogo()) {
            LOGGER.info(" _____       ___   __   _   __   _   _____   _____   ");
            LOGGER.info("|  _  \\     /   | |  \\ | | |  \\ | | | ____| |  _  \\  ");
            LOGGER.info("| |_| |    / /| | |   \\| | |   \\| | | |__   | |_| |  ");
            LOGGER.info("|  _  {   / / | | | |\\   | | |\\   | |  __|  |  _  /  ");
            LOGGER.info("| |_| |  / /  | | | | \\  | | | \\  | | |___  | | \\ \\  ");
            LOGGER.info("|_____/ /_/   |_| |_|  \\_| |_|  \\_| |_____| |_|  \\_\\ ");
            LOGGER.info(I18n.as("banner.launch.welcomemessage") + " - " + getVersion() + ", Java " + javaVersion);
        }
        if (System.getProperty("log4j.configurationFile") == null) {
            System.setProperty("log4j.configurationFile", "log4j2_banner.xml");
        }
        if (BannerConfigUtil.CHECK_UPDATE()) UpdateUtils.versionCheck();
        DefaultLibraries.proposeFabricLibs();
        CustomLibraries.loadCustomLibs();
        if (!EulaUtil.hasAcceptedEULA()) {
            System.out.println(I18n.as("eula"));
            while (!"true".equals(new Scanner(System.in).next()));
            EulaUtil.writeInfos();
        }
        ProxySelector.setDefault(new BannerProxySelector(ProxySelector.getDefault()));
    }

    public static String getVersion() {
      return FabricLoader.getInstance().getModContainer("banner").get().getMetadata().getVersion().getFriendlyString();
    }
}
