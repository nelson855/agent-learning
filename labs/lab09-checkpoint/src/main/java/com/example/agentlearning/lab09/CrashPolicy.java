package com.example.agentlearning.lab09;

/**
 * 崩溃策略：给定"即将执行"的步骤下标，决定是否在该步骤<b>执行前</b>抛出
 * {@link SimulatedCrashException} 模拟中断。默认从不崩溃。
 *
 * <p>把崩溃点做成可注入的接口，让单元测试与 {@code --demo} 都能确定性地
 * 指定"在第几步崩溃"，从而精确验证 checkpoint/resume。
 */
@FunctionalInterface
public interface CrashPolicy {

    CrashPolicy NEVER = stepIndex -> false;

    boolean shouldCrashBefore(int stepIndex);
}
