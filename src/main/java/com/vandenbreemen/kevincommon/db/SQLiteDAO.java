package com.vandenbreemen.kevincommon.db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteDAO {

    private static final Logger logger = Logger.getLogger(SQLiteDAO.class);

    private String filePath;

    public SQLiteDAO(String filePath) {
        this.filePath = filePath;
    }

    public void createTable(String createSQL) {
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(createSQL);
        } catch (Exception ex) {
            logger.error("Could not perform operation", ex);
        }
    }
}
