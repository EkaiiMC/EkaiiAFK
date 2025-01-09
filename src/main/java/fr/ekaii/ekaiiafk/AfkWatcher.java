package fr.ekaii.ekaiiafk;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

import static fr.ekaii.ekaiiafk.EkaiiAFK.logger;

public class AfkWatcher implements Runnable {

    private static final ConcurrentHashMap<Player, Object[]> statHashMap = new ConcurrentHashMap<>();

    @Override
    public void run () {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScheduler().run(EkaiiAFK.instance, task -> {
                Object[] lastData = statHashMap.get(player);


                // Not in stats: Create player stats
                if (lastData == null) {
                    createStat(player);
                    return;
                }

                // Changed pos: refresh pos
                if (!lastData[0].equals(player.getLocation())) {
                    refreshPos(player);
                    return;
                }

                // Same pos: set AFK if needed
                if (!(boolean) lastData[1] && System.currentTimeMillis() - (long) lastData[2] >= 60000L) {
                    setAFK(player);
                    lastData[1] = true;
                }
            }, null);
        }
    }

    public void createStat(Player player) {
        statHashMap.put(player, new Object[]{player.getLocation(), false, System.currentTimeMillis()});
    }

    public void refreshPos(Player player) {
        statHashMap.get(player)[0] = player.getLocation();
        statHashMap.get(player)[2] = System.currentTimeMillis();
        if ((boolean) statHashMap.get(player)[1]) {
            statHashMap.get(player)[1] = false;
            setNotAFK(player);
        }
    }

    private void setAFK(Player player) {
        player.setSleepingIgnored(true);
        try {
            TabAPI tabAPI = TabAPI.getInstance();
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            TabListFormatManager manager = tabAPI.getTabListFormatManager();
            if (manager != null) {
                logger.info("Setting player " + tabPlayer.getName() + " AFK");
                manager.setPrefix(tabPlayer, "&7&o");
                manager.setSuffix(tabPlayer, " | AFK&r");
            }
        } catch (Exception e) {
            if (e instanceof IllegalStateException) { // TAB API not loaded, fallback to default
                player.playerListName(Component.text(player.getName(), NamedTextColor.GRAY));
            } else {
                logger.warning("An error occurred while setting player AFK: " + e.getMessage());
            }
        }
    }

    private void setNotAFK(Player player) {
        player.setSleepingIgnored(false);
        try {
            TabAPI tabAPI = TabAPI.getInstance();
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            TabListFormatManager manager = tabAPI.getTabListFormatManager();
            if (manager != null) {
                manager.setPrefix(tabPlayer, null);
                manager.setSuffix(tabPlayer, null);
            }
        } catch (Exception e) {
            if (e instanceof IllegalStateException) { // TAB API not loaded, fallback to default
                player.playerListName(Component.text(player.getName(), NamedTextColor.WHITE));
            } else {
                logger.warning("An error occurred while setting player not AFK: " + e.getMessage());
            }
        }
    }
}