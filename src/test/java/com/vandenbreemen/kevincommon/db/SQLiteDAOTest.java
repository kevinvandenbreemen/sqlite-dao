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
    public void shouldRaiseAnErrorWhenHandlingBadlyFormedQuery() {
        SQLiteDAO dao = new SQLiteDAO("databases/local3"+System.currentTimeMillis());
        dao.createTable("create table person (id integer, name string, lastname string)");
        dao.insert("insert into person (id, name, lastname) values (?, ?, ?)", new Object[]{ 1, "Kevin", "Tester"});
        try {
            dao.query("SELECT * from p where id=?", new Object[]{1});
            fail("Bad table name, should not have worked");
        } catch (RuntimeException rex) {
            rex.printStackTrace();
        }
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

    @Test
    public void shouldEnforceForeignKeys() {
        SQLiteDAO dao = new SQLiteDAO("databases/local6"+System.currentTimeMillis());
        dao.createTable("create table person (id integer PRIMARY KEY, name string, lastname string)");
        dao.createTable("CREATE TABLE person_addr(person_id integer, name string, constraint fk_person FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE)");
        dao.insert("INSERT INTO person(id, name) VALUES (?, ?)", new Object[]{1, "Kevin"});
        try {
            dao.insert("INSERT INTO person_addr(person_id, name) VALUES (?, ?)", new Object[]{1, "Someplace"});
        } catch (RuntimeException rex){
            rex.printStackTrace();
        }
        dao.delete("DELETE FROM person WHERE id=?", new Object[]{1});
        List<Map<String, Object>> records = dao.query("SELECT * from person_addr", new Object[0]);
        assertTrue(records.isEmpty());
    }
}