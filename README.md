# sqlite-dao
Java-based DAO for SQLite.  Provides a simple API for CRUD that doesn't rely on mapping libs like Hibernate, etc.

# Basic Usage
## This library provides a simple database version control system so you can also apply changes at runtime:

Here is an example program:

```
        SQLiteDAO dao = new SQLiteDAO("path/to/database");
        DatabaseSchema schema = new DatabaseSchema(dao);

        assertTrue(schema.addDatabaseChange(1, "CREATE TABLE page (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, age INT)"));
        assertTrue(schema.addDatabaseChange(2, "ALTER TABLE page ADD chapter TEXT"));

        dao.insert("INSERT INTO page(name, age, chapter) VALUES (?, ?, ?)", new Object[]{"page 1", 10, "Chapter 1"});
        dao.insert("INSERT INTO page(name, age, chapter) VALUES (?, ?, ?)", new Object[]{"page 44", 12, "Chapter 12"});
```

The SQLiteDAO is a thread-safe object that provides a basic CRUD API.

The DatabaseSchema class allows you to make schema changes to your database.  Schemas are versioned, allowing you to run the same batch of setup instructions each time your 
program is run without needing to worry about potential conflicts.

# A note about Foreign Keys
Currently the library has foreign keys enabled by default for SQLite connections.  There is no argument when creating DAOs for disabling this but if you wish you can update the SQLiteDAO class to configure details about the SQLiteConfig object that is generated whenever a new connection is opened.