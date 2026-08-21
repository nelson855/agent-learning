package com.example.agentlearning.lab10;

import java.util.List;

/**
 * LLM Evaluator 返回的结构化反馈。
 *
 * <p>只承载<b>语义判断</b>的结果：是否解释异常、建议是否可执行。程序校验（结构、类型、
 * 必填）不属于这里——那些已在 {@link ProgramValidator} 完成。
 *
 * @param pass   是否通过
 * @param score  0~4 分（完整性 / 可执行性各 0~2）
 * @param issues 未通过的具体问题，作为反馈回灌给 Generator 重试
 */
public record EvaluatorFeedback(boolean pass, int score, List<String> issues) {

    /** 用于某个语义点未评审时的占位反馈。 */
    public static EvaluatorFeedback failed(int score, String issue) {
        return new EvaluatorFeedback(false, score, List.of(issue));
    }
}