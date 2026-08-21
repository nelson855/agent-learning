package com.example.agentlearning.lab10;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 一份可读的 JSON 周报（Generator 的目标输出，也是 ProgramValidator 的校验对象）。
 *
 * <p>字段设计服务于两层校验的教学：
 * <ul>
 *   <li>{@code week} / {@code totalTasks} / {@code completedTasks} / {@code summary} /
 *       {@code recommendations} 为<b>必填</b>，缺失即被 ProgramValidator 拒绝；</li>
 *   <li>{@code summary} 是否解释异常、{@code recommendations} 是否可执行，属于<b>语义</b>，
 *       交给 LLM Evaluator 判断。</li>
 * </ul>
 *
 * @param week               ISO 周标识，例如 {@code 2026-W33}
 * @param totalTasks         总任务数
 * @param completedTasks     已完成任务数
 * @param failedTasks        失败任务数
 * @param abnormalRatio      异常占比
 * @param avgDurationMinutes 平均时长（分钟）
 * @param summary            本周总结；应覆盖整体结果与失败/异常原因
 * @param recommendations    可执行的改进建议，至少一条
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeeklyReport(
        String week,
        int totalTasks,
        int completedTasks,
        int failedTasks,
        double abnormalRatio,
        int avgDurationMinutes,
        String summary,
        List<String> recommendations) {

    public static WeeklyReport empty() {
        return new WeeklyReport(null, 0, 0, 0, 0.0, 0, null, List.of());
    }
}