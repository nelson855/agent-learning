package com.example.agentlearning.lab10;

/**
 * SQLite 聚合出的任务统计摘要。
 *
 * <p>由 {@link TaskRepository} 根据 task 表实时聚合生成，模拟「一周任务运行情况」。
 *
 * @param totalTasks         总任务数
 * @param completedTasks     已完成数
 * @param failedTasks        失败任务数
 * @param abnormalRatio      异常占比（例如 0.057 = 5.7%）
 * @param avgDurationMinutes 平均执行时长（分钟）
 * @param degradedTasks      退化/超时任务数
 */
public record TaskStats(
        int totalTasks,
        int completedTasks,
        int failedTasks,
        double abnormalRatio,
        int avgDurationMinutes,
        int degradedTasks) {

    /** 供 ProgramValidator 校验的 iso-week 标识。 */
    public String week() {
        return "2026-W33";
    }
}