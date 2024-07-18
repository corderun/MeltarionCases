package org.corderun.meltarionCase;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class CasePlaceholders extends PlaceholderExpansion {

    MeltarionCase plugin;

    public CasePlaceholders(MeltarionCase plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "MeltarionCase";
    }

    @Override
    public @NotNull String getAuthor() {
        return "CorderuN";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        File directory = new File("plugins/MeltarionCase/cases");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String caseName = file.getName().replace(".yml", "");
                        if (identifier.equals("key_" + caseName)) {
                            try (Connection connection = plugin.getConnection()) {
                                String selectSql = "SELECT keys_count FROM mcase_keys WHERE player = ? AND case_name = ?";
                                try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
                                    selectPstmt.setString(1, player.getName());
                                    selectPstmt.setString(2, file.getName().replace(".yml", ""));
                                    try (ResultSet resultSet = selectPstmt.executeQuery()) {
                                        if (resultSet.next()) {
                                            int keysCount = resultSet.getInt("keys_count");
                                            return String.valueOf(keysCount);
                                        } else {
                                            return "0";
                                        }
                                    }
                                }
                            } catch (SQLException ex) {
                                plugin.getLogger().severe("Не удалось записать данные: " + ex.getMessage());
                            }
                            return "0";
                        }
                    }
                }
            }
        }

        return null;
    }

}
