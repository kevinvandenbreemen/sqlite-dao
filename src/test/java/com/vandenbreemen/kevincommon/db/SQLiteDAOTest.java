package com.vandenbreemen.kevincommon.db;

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
    public void shouldCheckIfTableExists() {
        SQLiteDAO dao = new SQLiteDAO("databases/local10"+System.currentTimeMillis());
        assertFalse(dao.tableExists("person"));
        dao.createTable("create table person (id integer, name string)");
        assertTrue(dao.tableExists("person"));

    }

    @Test
    public void shouldInsertRecordIntoTable() {
        SQLiteDAO dao = new SQLiteDAO("databases/local2"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string)");
        dao.performSimpleInsert("person", new String[]{ "id", "name" }, new Object[]{1, "Test"});
        List<Map<String, Object>> records = dao.performSimpleQuery("person", new String[]{ "name" }, "id", 1);
        assertEquals(1, records.size());
        assertEquals("Test", records.get(0).get("name"));
    }

    @Test
    public void shouldUpdateRecord() {
        SQLiteDAO dao = new SQLiteDAO("databases/local3"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string)");
        dao.performSimpleInsert("person", new String[]{ "id", "name" }, new Object[]{1, "Test"});
        dao.performSimpleUpdate("person", new String[]{ "name" }, new Object[]{ "Kevin" }, "id", 1);
        List<Map<String, Object>> records = dao.performSimpleQuery("person", new String[]{ "name" }, "id", 1);
        assertEquals(1, records.size());
        assertEquals("Kevin", records.get(0).get("name"));
    }

    @Test
    public void shouldGracefullyHandleErrorQuerying() {
        SQLiteDAO dao = new SQLiteDAO("databases/local3"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string, lastname string)");
        dao.insert("insert into person (id, name, lastname) values (?, ?, ?)", new Object[]{ 1, "Kevin", "Tester"});
        List<Map<String, Object>> rec = dao.query("SELECT * from p where id=?", new Object[]{1});
        assertEquals(0, rec.size());
    }

    @Test
    public void shouldModifyMultipleColumns() {
        SQLiteDAO dao = new SQLiteDAO("databases/local4"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string, lastname string)");
        dao.performSimpleInsert("person", new String[]{"id", "name", "lastname"}, new Object[]{1, "Kevin", "tester"});
        dao.performSimpleUpdate("person", new String[]{"name", "lastname"}, new Object[]{"first", "last"}, "id", 1);
        List<Map<String, Object>> records = dao.performSimpleQuery("person", new String[]{"name", "lastname"}, "id", 1);
        assertEquals(1, records.size());
        assertEquals("first", records.get(0).get("name"));
        assertEquals("last", records.get(0).get("lastname"));
    }

    @Test
    public void shouldDoDelete() {
        SQLiteDAO dao = new SQLiteDAO("databases/local4"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string, lastname string)");
        dao.performSimpleInsert("person", new String[]{"id", "name", "lastname"}, new Object[]{1, "Kevin", "tester"});
        dao.delete("DELETE FROM person WHERE id=?", new Object[]{1});
        List<Map<String, Object>> records = dao.performSimpleQuery("person", new String[]{"name", "lastname"}, "id", 1);
        assertEquals(0, records.size());
    }
}