package org.corderun.meltarionCase;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class EventListener implements Listener {

    MeltarionCase plugin;
    private boolean cdCase = false;

    public EventListener(MeltarionCase plugin) {
        super();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractCase(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null || !e.getAction().name().contains("RIGHT_CLICK_BLOCK")) {
            return;
        }
        File directory = new File("plugins/MeltarionCase/cases");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        YamlConfiguration caseFile = YamlConfiguration.loadConfiguration(file);
                        Location caseLoc = caseFile.getLocation("Location");
                        if (caseLoc != null && caseLoc.equals(e.getClickedBlock().getLocation())) {
                            e.setCancelled(true);
                            if(!cdCase){
                                try (Connection connection = plugin.getConnection()) {
                                    String selectSql = "SELECT keys_count FROM mcase_keys WHERE player = ? AND case_name = ?";
                                    try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
                                        selectPstmt.setString(1, player.getName());
                                        selectPstmt.setString(2, file.getName().replace(".yml", ""));
                                        try (ResultSet resultSet = selectPstmt.executeQuery()) {
                                            if (resultSet.next()) {
                                                int keysCount = resultSet.getInt("keys_count");
                                                if(keysCount >= 1){
                                                    cdCase = true;
                                                    plugin.openCase(player, file);
                                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> cdCase = false, 20L);
                                                } else{
                                                    plugin.getLogger().info("ОНО НЕ НАШЛО КОЛИЧЕСТВО!");
                                                    player.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.use.no-keys")).replace("&", "§"));
                                                    return;
                                                }
                                            } else {
                                                player.sendMessage(Objects.requireNonNull(plugin.langConfig.getString("case.use.no-keys")).replace("&", "§"));
                                                return;
                                            }
                                        }
                                    }
                                } catch (SQLException ex) {
                                    plugin.getLogger().severe("Не удалось записать данные: " + ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
