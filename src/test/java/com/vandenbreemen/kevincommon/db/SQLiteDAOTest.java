package com.vandenbreemen.kevincommon.db;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

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
        SQLiteDAO dao = new SQLiteDAO("databases/local1");
        dao.createTable("create table person (id integer, name string)");

    }
}