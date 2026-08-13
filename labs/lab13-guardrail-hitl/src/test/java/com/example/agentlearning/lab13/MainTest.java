package com.example.agentlearning.lab13;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void greetingContainsModuleName() {
        assertTrue(Main.greeting().contains("lab13-guardrail-hitl"));
    }
}
