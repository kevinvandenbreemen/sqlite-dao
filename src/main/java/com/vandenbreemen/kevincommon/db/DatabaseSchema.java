package com.vandenbreemen.kevincommon.db;

public class DatabaseSchema {

    private static final String METADATA_TABLE_NAME = "_metadata";

    /**
     * DAO
     */
    private SQLiteDAO dao;

    public DatabaseSchema(SQLiteDAO dao) {
        this.dao = dao;
    }

    void setVersionNumber(int versionNumber) {
        setupMetadata();
        dao.update("UPDATE " + METADATA_TABLE_NAME + " SET version=?", new Object[]{versionNumber});
    }

    private void setupMetadata() {
        if (!dao.tableExists(METADATA_TABLE_NAME)) {
            dao.createTable("CREATE TABLE " + METADATA_TABLE_NAME + "( version INT )");
            dao.insert("INSERT INTO " + METADATA_TABLE_NAME + "(version) VALUES (?)", new Object[]{0});
        }
    }


    int getVersionNumber() {
        setupMetadata();
        return (int)dao.query("SELECT version FROM " + METADATA_TABLE_NAME, new Object[]{}).get(0).get("version");
    }

    /**
     * Applies the given change to the database
     * @param version       Version number to set the database to after the change has been applied
     * @param changeSQL     SQL to execute
     * @return              True if the change was applied otherwise false if the database is already at or above the given version
     */
    public boolean addDatabaseChange(int version, String changeSQL) {
        if(getVersionNumber() >= version) {
            return false;
        }
        dao.createTable(changeSQL);
        setVersionNumber(version);
        return true;
    }

}
