package com.example.agentlearning.lab04;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void greetingContainsModuleName() {
        assertTrue(Main.greeting().contains("lab04-workflow-vs-agent"));
    }
}
