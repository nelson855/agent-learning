package com.example.agentlearning.lab02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * 自实现四则运算求值器的确定性测试。
 */
class CalculatorTest {

    @Test
    void basicArithmetic() {
        assertEquals(7, Calculator.evaluate("1+2*3"), 1e-9);
        assertEquals(1.5, Calculator.evaluate("3/2"), 1e-9);
    }

    @Test
    void parenthesesChangePrecedence() {
        assertEquals(9, Calculator.evaluate("(1+2)*3"), 1e-9);
        assertEquals(7, Calculator.evaluate("(1+2)*(3-2)+4"), 1e-9);
    }

    @Test
    void unarySignAndDecimal() {
        assertEquals(-5, Calculator.evaluate("-2-3"), 1e-9);
        assertEquals(1.1, Calculator.evaluate("0.5 + 0.6"), 1e-9);
        assertEquals(2.5, Calculator.evaluate("+2.5"), 1e-9);
    }

    @Test
    void whitespaceIsIgnored() {
        assertEquals(9, Calculator.evaluate("  ( 1 + 2 ) * 3 "), 1e-9);
    }

    @Test
    void invalidExpressionThrows() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.evaluate("1+"));
        assertThrows(IllegalArgumentException.class, () -> Calculator.evaluate("(1+2"));
        assertThrows(IllegalArgumentException.class, () -> Calculator.evaluate("abc"));
    }
}
