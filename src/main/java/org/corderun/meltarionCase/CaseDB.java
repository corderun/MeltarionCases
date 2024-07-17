package org.corderun.meltarionCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CaseDB {

    static MeltarionCase plugin;

    public static void initClass(MeltarionCase plugin) {
        CaseDB.plugin = plugin;
    }

    public static void connectToDatabase(){
        String host = plugin.getConfig().getString("mysql.host");
        int port = plugin.getConfig().getInt("mysql.port");
        String database = plugin.getConfig().getString("mysql.database");
        String user = plugin.getConfig().getString("mysql.user");
        String password = plugin.getConfig().getString("mysql.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            plugin.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удается подключиться в базе данной: " + e.getMessage());
        }
    }

    public static void disconnectFromDatabase() {
        if (plugin.connection != null) {
            try {
                plugin.connection.close();
                plugin.getLogger().info("Отключение от базы данных.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Не удалось отключиться от базы данных: " + e.getMessage());
            }
        }
    }

    public static Connection reconnectToDatabase() {
        try {
            if (plugin.connection != null && !plugin.connection.isClosed()) {
                plugin.connection.close();
            }
            CaseDB.connectToDatabase();
            return plugin.connection;
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось переподключиться к базе данных: " + e.getMessage());
            return null;
        }
    }

    public static void createTable(){
        String sql = "CREATE TABLE IF NOT EXISTS mcase_keys ("
                + "player VARCHAR(255) NOT NULL,"
                + "case_name VARCHAR(255) NOT NULL,"
                + "keys_count INT NOT NULL"
                + ")";

        try (Statement stmt = plugin.connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось создать таблицу: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        return plugin.connection;
    }

}
