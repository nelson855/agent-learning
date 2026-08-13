package com.example.agentlearning.lab03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * SQLite 存储层测试：schema 自动初始化 + 基本读写。
 */
class TaskStoreTest {

    @Test
    void schemaAutoInitialized() {
        try (TaskStore store = new TaskStore("jdbc:sqlite::memory:")) {
            assertTrue(store.tableExists());
        }
    }

    @Test
    void insertThenFindByIdRoundTrips() {
        try (TaskStore store = new TaskStore("jdbc:sqlite::memory:")) {
            store.insert(new Task("t-1", "写周报", "pending", "2026-08-13T10:00:00Z"));

            assertTrue(store.findById("t-1").isPresent());
            assertEquals("写周报", store.findById("t-1").orElseThrow().title());
            assertEquals(1, store.findAll().size());
        }
    }

    @Test
    void findByIdMissingReturnsEmpty() {
        try (TaskStore store = new TaskStore("jdbc:sqlite::memory:")) {
            assertTrue(store.findById("t-nope").isEmpty());
        }
    }
}
