package com.example.agentlearning.lab05;

import java.util.List;

/**
 * 一份结构化计划：目标 + 步骤列表。
 *
 * <p>用结构化数据而不是一段 Markdown 存计划，程序才能更新每步状态、
 * 定位失败步骤、把"已完成/失败"喂给 Replanner。
 */
public final class Plan {

    private final String goal;
    private final List<PlanStep> steps;

    public Plan(String goal, List<PlanStep> steps) {
        this.goal = goal;
        this.steps = List.copyOf(steps);
    }

    public String goal() {
        return goal;
    }

    public List<PlanStep> steps() {
        return steps;
    }
}
