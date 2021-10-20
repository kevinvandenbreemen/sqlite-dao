package com.vandenbreemen.kevincommon.db;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteDAOTest {

    @BeforeAll
    public static void setup() {
        File dbDir = new File("databases");
        if(dbDir.mkdir()){
            System.out.println("Test Dir Created");
            dbDir.deleteOnExit();
        }
    }

    @Test
    public void shouldCreateTable() {
        SQLiteDAO dao = new SQLiteDAO("databases/local1"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string)");

    }

    @Test
    public void shouldInsertRecordIntoTable() {
        SQLiteDAO dao = new SQLiteDAO("databases/local2"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string)");
        dao.insert("person", new String[]{ "id", "name" }, new Object[]{1, "Test"});
        List<Map<String, Object>> records = dao.query("person", new String[]{ "name" }, "id", 1);
        assertEquals(1, records.size());
        assertEquals("Test", records.get(0).get("name"));
    }

    @Test
    public void shouldUpdateRecord() {
        SQLiteDAO dao = new SQLiteDAO("databases/local3"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string)");
        dao.insert("person", new String[]{ "id", "name" }, new Object[]{1, "Test"});
        dao.update("person", new String[]{ "name" }, new Object[]{ "Kevin" }, "id", 1);
        List<Map<String, Object>> records = dao.query("person", new String[]{ "name" }, "id", 1);
        assertEquals(1, records.size());
        assertEquals("Kevin", records.get(0).get("name"));
    }
}