package com.vandenbreemen.kevincommon.db;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

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
            logger.error("Could not create table", ex);
        }
    }

    public void insert(String tableName, String[] columnNames, Object[] values) {

        StringBuilder prepared = new StringBuilder("INSERT INTO ").append(tableName).append(" ( ").append(String.join(", ", columnNames)).append(")");

        prepared.append(" VALUES (");
        for(int i=0; i<values.length; i++) {
            prepared.append("?");
            if(i<values.length - 1){
                prepared.append(", ");
            }
        }
        prepared.append(")");

        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath); PreparedStatement statement = connection.prepareStatement(prepared.toString());) {

            int index = 1;
            for(Object v : values) {
                setValueAtStatementIndex(statement, index, v);
                index++;
            }

            statement.executeUpdate();

        } catch (Exception ex) {
            logger.error("Could not perform insert\n"+prepared, ex);
        }
    }

    private void setValueAtStatementIndex(PreparedStatement statement, int index, Object v) throws SQLException {
        if(v instanceof Integer) {
            statement.setInt(index, (Integer) v);
        }
        if( v instanceof  String) {
            statement.setString(index, v.toString());
        }
    }

    public List<Map<String, Object>> query(String tableName, String[] columns, String columnName, Object value) {

        StringBuilder prepared = new StringBuilder("SELECT ").append(String.join(", ", columns)).append(" FROM ").append(tableName).append(" WHERE ").append(columnName).append(" = ?");


        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath); PreparedStatement statement = connection.prepareStatement(prepared.toString());) {
            setValueAtStatementIndex(statement, 1, value);
            ResultSet rs = statement.executeQuery();

            ArrayList<Map<String, Object>> result = new ArrayList<>();
            while(rs.next())
            {
                Map<String, Object> row = new HashMap<>();
                for(String column : columns) {
                    row.put(column, rs.getObject(column));
                }
                result.add(row);

            }

            return result;
        }catch (Exception ex) {
            logger.error("Could not perform query\n"+prepared, ex);
        }
        return Collections.emptyList();
    }

    public void update(String tableName, String[] columns, Object[] newValues, String whereColumn, Object equalsValue) {
        StringBuilder prepared = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        for(int i=0; i<columns.length; i++) {
            prepared.append(columns[i]).append(" = ?");
            if(i < columns.length-1) {
                prepared.append(", ");
            }
        }
        prepared.append(" WHERE ").append(whereColumn).append(" = ?");


        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath); PreparedStatement statement = connection.prepareStatement(prepared.toString());) {

            int index = 1;
            for(Object v : newValues) {
                setValueAtStatementIndex(statement, index, v);
                index++;
            }

            setValueAtStatementIndex(statement, index, equalsValue);

            statement.executeUpdate();

        } catch (Exception ex) {
            logger.error("Could not perform insert\n"+prepared, ex);
        }

    }
}
