package com.example.agentlearning.lab02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * SQLite 存储层测试：schema 自动初始化 + 基本 CRUD。
 */
class TaskStoreTest {

    private final TaskStore store = new TaskStore("jdbc:sqlite::memory:");

    @AfterEach
    void tearDown() {
        store.close();
    }

    /** 验收点：构造即自动建表，不需要外部先执行 DDL。 */
    @Test
    void schemaAutoInitialized() {
        assertTrue(store.tableExists());
    }

    @Test
    void insertThenFindByIdRoundTrips() {
        store.insert(new Task("t-1", "写周报", "pending", "2026-08-13T10:00:00Z"));

        assertTrue(store.findById("t-1").isPresent());
        Task task = store.findById("t-1").orElseThrow();
        assertEquals("写周报", task.title());
        assertEquals("pending", task.status());
    }

    @Test
    void findByIdMissingReturnsEmpty() {
        assertTrue(store.findById("t-nope").isEmpty());
    }

    @Test
    void findAllReturnsInsertedRowsInOrder() {
        store.insert(new Task("t-1", "a", "pending", "2026-08-13T10:00:00Z"));
        store.insert(new Task("t-2", "b", "pending", "2026-08-13T10:00:01Z"));

        List<Task> tasks = store.findAll();
        assertEquals(2, tasks.size());
        assertEquals("a", tasks.get(0).title());
        assertEquals("b", tasks.get(1).title());
    }
}
