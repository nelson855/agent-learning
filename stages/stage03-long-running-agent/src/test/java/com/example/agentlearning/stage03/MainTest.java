package com.example.agentlearning.stage03;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void greetingContainsModuleName() {
        assertTrue(Main.greeting().contains("stage03-long-running-agent"));
    }
}
