package com.example.agentlearning.lab02;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册表：登记工具、按名分派、执行前校验、生成给模型的工具说明。
 *
 * <p>分派流程（本章核心）：
 * <ol>
 *   <li>按工具名查找 —— 找不到返回 {@code UNKNOWN_TOOL} 失败（模型可能编造一个不存在的工具）；</li>
 *   <li>参数校验 —— 缺失或类型错误返回校验失败（模型可能给错参数）；</li>
 *   <li>执行工具 —— 业务代码返回 {@link ToolResult}。</li>
 * </ol>
 * 模型只负责"建议"执行哪个工具，程序负责这个动作能否执行、如何执行。
 */
public final class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 未注册工具时返回的错误标记，供测试与排障识别。 */
    public static final String UNKNOWN_TOOL = "UNKNOWN_TOOL";

    public ToolRegistry register(Tool tool) {
        tools.put(tool.definition().name(), tool);
        return this;
    }

    /** 分派一个工具调用；找不到工具或参数不合法时返回失败结果，而不是抛异常。 */
    public ToolResult execute(ToolCall call) {
        Tool tool = tools.get(call.name());
        if (tool == null) {
            return ToolResult.fail(UNKNOWN_TOOL + ": " + call.name() + "（模型建议了一个未注册的工具）");
        }
        String error = ArgumentValidator.validate(tool.definition(), call.arguments());
        if (error != null) {
            return ToolResult.fail("参数校验失败: " + error);
        }
        return tool.execute(call.arguments());
    }

    /** 所有已注册工具的定义（保持注册顺序）。 */
    public List<ToolDefinition> definitions() {
        return tools.values().stream().map(Tool::definition).toList();
    }

    /** 生成给模型看的工具说明（JSON 数组文本），写入 system prompt。 */
    public String toolsInstruction() {
        try {
            return objectMapper.writeValueAsString(definitions());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化工具说明失败", e);
        }
    }
}
