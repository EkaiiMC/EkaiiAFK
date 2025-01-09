package fr.ekaii.ekaiiafk;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class EkaiiAFK extends JavaPlugin {

    public static EkaiiAFK instance;
    public static Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        register();
    }

    private void register() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> new AfkWatcher().run(), 10L, 100L);
    }
}
