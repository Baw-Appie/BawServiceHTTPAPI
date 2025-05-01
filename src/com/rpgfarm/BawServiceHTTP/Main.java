package com.rpgfarm.BawServiceHTTP;

import java.util.logging.Logger;

import com.rpgfarm.BawServiceHTTP.apis.HTTPAPI;
import com.rpgfarm.BawServiceHTTP.apis.SocketAPI;
import com.rpgfarm.BawServiceHTTP.commands.MonetizeCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main singleInstance;
    public FileConfiguration config;

    public void onEnable() {
        singleInstance = this;
        Logger logger = getLogger();
        logger.info("Minehub Monetize API 플러그인 사용을 환영합니다.");
        config = getConfig();
        config.addDefault("setting.api-key", "MinehubMonetizeAPI_KEY");
        config.addDefault("setting.port", 21080);
        config.options().copyDefaults(true);
        saveConfig();
        saveDefaultConfig();
        logger.info("현재 Minehub Monetize HTTP API " + this.getDescription().getVersion() + "(을)를 사용하고 있습니다.");

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, HTTPAPI::runWebCheck, 0L, 1200L);
        Bukkit.getServer().getPluginManager().registerEvents(new MonetizeCommand(), this);
        SocketAPI.startSocketThread();
    }

    public void onDisable() {
        SocketAPI.stopSocketThread();
        getLogger().info("Minehub Monetize API Listener를 이용해주셔서 감사합니다.");
    }
}
