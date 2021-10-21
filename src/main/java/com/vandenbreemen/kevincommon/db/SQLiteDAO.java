package com.vandenbreemen.kevincommon.db;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SQLiteDAO {

    private static final Logger logger = Logger.getLogger(SQLiteDAO.class);

    private String filePath;

    /**
     * Single-threaded access for now in order to side-step any issues with how the file system will handle concurrent reads/writes
     */
    private Semaphore access;

    public SQLiteDAO(String filePath) {
        this.filePath = filePath;
        access = new Semaphore(1);
    }

    public void createTable(String createSQL) {
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(createSQL);
        } catch (Exception ex) {
            logger.error("Could not create table", ex);
        }
    }

    public void insert(String sql, Object[] values) {
        try {
            access.acquire();
            try(Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath); PreparedStatement statement = connection.prepareStatement(sql);) {
                int index = 1;
                for(Object v : values) {
                    setValueAtStatementIndex(statement, index, v);
                    index++;
                }

                statement.executeUpdate();

            } catch (Exception ex) {
                logger.error("Could not perform insert\n"+sql, ex);
            }
        } catch (InterruptedException inter) {
            logger.error("Failed to acquire database lock", inter);
        }
        finally {
            access.release();
        }

    }

    public void performSimpleInsert(String tableName, String[] columnNames, Object[] values) {

        StringBuilder prepared = new StringBuilder("INSERT INTO ").append(tableName).append(" ( ").append(String.join(", ", columnNames)).append(")");

        prepared.append(" VALUES (");
        for(int i=0; i<values.length; i++) {
            prepared.append("?");
            if(i<values.length - 1){
                prepared.append(", ");
            }
        }
        prepared.append(")");

        insert(prepared.toString(), values);
    }

    private void setValueAtStatementIndex(PreparedStatement statement, int index, Object v) throws SQLException {
        if(v instanceof Integer) {
            statement.setInt(index, (Integer) v);
        }
        if( v instanceof  String) {
            statement.setString(index, v.toString());
        }
    }

    public List<Map<String, Object>> query(String sql, Object[] values) {

        try {
            access.acquire();

            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath); PreparedStatement statement = connection.prepareStatement(sql);) {

                int index = 1;
                for (Object v : values) {
                    setValueAtStatementIndex(statement, index, v);
                    index++;
                }
                ResultSet rs = statement.executeQuery();

                String[] columnsToFetch = new String[rs.getMetaData().getColumnCount()];
                for (int i = 0; i < columnsToFetch.length; i++) {
                    columnsToFetch[i] = rs.getMetaData().getColumnName(i + 1);
                }

                ArrayList<Map<String, Object>> result = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (String column : columnsToFetch) {
                        row.put(column, rs.getObject(column));
                    }
                    result.add(row);
                }

                return result;
            } catch (Exception ex) {
                logger.error("Could not perform query\n" + sql, ex);
            }
        } catch (InterruptedException interruptedException) {
            logger.error("Failed to acquire database lock", interruptedException);
        } finally {
            access.release();
        }
        return Collections.emptyList();
    }

    public List<Map<String, Object>> performSimpleQuery(String tableName, String[] columns, String columnName, Object value) {

        StringBuilder prepared = new StringBuilder("SELECT ").append(String.join(", ", columns)).append(" FROM ").append(tableName).append(" WHERE ").append(columnName).append(" = ?");

        return query(prepared.toString(), new Object[]{value});
    }

    public void performSimpleUpdate(String tableName, String[] columns, Object[] newValues, String whereColumn, Object equalsValue) {
        StringBuilder prepared = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        for(int i=0; i<columns.length; i++) {
            prepared.append(columns[i]).append(" = ?");
            if(i < columns.length-1) {
                prepared.append(", ");
            }
        }
        prepared.append(" WHERE ").append(whereColumn).append(" = ?");


        Object[] paramsAndValues = new Object[newValues.length+1];
        System.arraycopy(newValues, 0, paramsAndValues, 0, newValues.length);
        paramsAndValues[newValues.length] = equalsValue;
        update(prepared.toString(), paramsAndValues);

    }

    /**
     * This method is just another update but is provided in order to allow for code that is easier to read
     * @param sql
     * @param paramsAndValues
     */
    public void delete(String sql, Object[] paramsAndValues) {
        update(sql, paramsAndValues);
    }

    public void update(String sql, Object[] paramsAndValues) {
        try {
            access.acquire();
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + filePath); PreparedStatement statement = connection.prepareStatement(sql);) {
                int index = 1;
                for (Object v : paramsAndValues) {
                    setValueAtStatementIndex(statement, index, v);
                    index++;
                }

                statement.executeUpdate();

            } catch (Exception ex) {
                logger.error("Could not perform insert\n" + sql, ex);
            }
        } catch (InterruptedException interruptedException) {
            logger.error("Failed to acquire database lock", interruptedException);
        } finally {
            access.release();
        }
    }
}
