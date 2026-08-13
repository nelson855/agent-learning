package com.example.agentlearning.lab07;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void greetingContainsModuleName() {
        assertTrue(Main.greeting().contains("lab07-rag-context"));
    }
}
