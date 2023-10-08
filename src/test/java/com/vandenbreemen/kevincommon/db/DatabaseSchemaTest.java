package com.vandenbreemen.kevincommon.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseSchemaTest {

    File file;

    @BeforeEach
    public void setup() throws IOException {
        file = File.createTempFile("testDatabase", "temp");
    }

    @AfterEach
    public void tearDown() {
        file.delete();
    }

    @Test
    public void shouldSetAndGetVersionNumber() {
        SQLiteDAO dao = new SQLiteDAO(file.getAbsolutePath());
        DatabaseSchema schema = new DatabaseSchema(dao);

        schema.setVersionNumber(1);
        assertEquals(1, schema.getVersionNumber());
    }

    @Test
    public void shouldOnlyApplyChangeOnce() {
        SQLiteDAO dao = new SQLiteDAO(file.getAbsolutePath());
        DatabaseSchema schema = new DatabaseSchema(dao);

        assertTrue(schema.addDatabaseChange(1, "CREATE TABLE page (id INT PRIMARY KEY NOT NULL, name TEXT, age INT)"));
        assertFalse(schema.addDatabaseChange(1, "CREATE TABLE page (id INT PRIMARY KEY NOT NULL, name TEXT, age INT)"));
    }

    @Test
    public void shouldProvideForAlteringTables() {
        SQLiteDAO dao = new SQLiteDAO(file.getAbsolutePath());
        DatabaseSchema schema = new DatabaseSchema(dao);

        assertTrue(schema.addDatabaseChange(1, "CREATE TABLE page (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, age INT)"));
        assertTrue(schema.addDatabaseChange(2, "ALTER TABLE page ADD chapter TEXT"));

        dao.insert("INSERT INTO page(name, age, chapter) VALUES (?, ?, ?)", new Object[]{"page 1", 10, "Chapter 1"});
        dao.insert("INSERT INTO page(name, age, chapter) VALUES (?, ?, ?)", new Object[]{"page 44", 12, "Chapter 12"});
        System.out.println(dao.query("SELECT * FROM page", new Object[]{}));
    }

    @Test
    public void shouldNotIncrementVersionOnChangeFailure() {
        SQLiteDAO dao = new SQLiteDAO(file.getAbsolutePath());
        DatabaseSchema schema = new DatabaseSchema(dao);
        schema.addDatabaseChange(1, "CREATE TABLE fasdf WITH");
        assertEquals(0, schema.getVersionNumber());
    }

    @Test
    public void shouldThrowErrorOnBadInsertOrUpdate() {
        SQLiteDAO dao = new SQLiteDAO(file.getAbsolutePath());
        DatabaseSchema schema = new DatabaseSchema(dao);

        assertTrue(schema.addDatabaseChange(1, "CREATE TABLE page (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, age INT)"));
        assertTrue(schema.addDatabaseChange(2, "ALTER TABLE page ADD chapter TEXT"));
        assertTrue(schema.addDatabaseChange(3, "CREATE UNIQUE INDEX uc_name ON page(name)"));

        dao.insert("INSERT INTO page(name, age, chapter) VALUES (?, ?, ?)", new Object[]{"page 1", 10, "Chapter 1"});

        try {
            dao.insert("INSERT INTO page(name, age, chapter) VALUES (?, ?, ?)", new Object[]{"page 1", 12, "Chapter 12"});
            fail("Violation of constraint should fail");
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

}