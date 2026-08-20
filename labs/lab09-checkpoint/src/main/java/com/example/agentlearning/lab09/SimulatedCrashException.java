package com.example.agentlearning.lab09;

/**
 * 模拟 JVM 崩溃。用于演示：任务执行到一半调用抛出它，进程中断，
 * 之后通过 checkpoint 恢复。
 */
public final class SimulatedCrashException extends RuntimeException {

    public SimulatedCrashException(String message) {
        super(message);
    }
}
