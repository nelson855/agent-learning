package com.example.agentlearning.stage02;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void greetingContainsModuleName() {
        assertTrue(Main.greeting().contains("stage02-stateful-agent"));
    }
}
