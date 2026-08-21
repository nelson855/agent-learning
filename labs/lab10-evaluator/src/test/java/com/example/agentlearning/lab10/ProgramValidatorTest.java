package com.example.agentlearning.lab10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * ProgramValidator：确定性（程序）校验的各规则。
 *
 * <p>这些判断<b>不依赖模型</b>，也<b>不调用 Evaluator</b>——教学重点 7.3：能由程序确定的
 * 判断，优先由程序判断。
 */
class ProgramValidatorTest {

    private final ProgramValidator validator = new ProgramValidator();

    private static String validJson() {
        return """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"本周 12 个失败，集中在支付模块接口超时。",
                 "recommendations":["为支付接口增加超时重试"]}""";
    }

    @Test
    void validJsonAccepted() {
        ProgramValidator.ValidationResult r = validator.validate(validJson());
        assertTrue(r.valid());
        assertEquals("2026-W33", r.report().week());
        assertEquals(182, r.report().completedTasks());
        assertEquals(1, r.report().recommendations().size());
    }

    @Test
    void codeFenceWrappedJsonAccepted() {
        ProgramValidator.ValidationResult r = validator.validate("```json\n" + validJson() + "\n```");
        assertTrue(r.valid(), "应容忍 ```json 代码块包裹");
    }

    @Test
    void missingRecommendationsRejected() {
        String missing = """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"数据统计完成。"}""";
        ProgramValidator.ValidationResult r = validator.validate(missing);
        assertFalse(r.valid());
        assertTrue(r.errors().stream().anyMatch(e -> e.contains("recommendations")));
    }

    @Test
    void emptyRecommendationsRejected() {
        String empty = """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"数据统计完成。",
                 "recommendations":[]}""";
        assertFalse(validator.validate(empty).valid());
    }

    @Test
    void malformedJsonRejected() {
        assertFalse(validator.validate("{ not valid json ").valid());
    }

    @Test
    void wrongNumberTypeRejected() {
        String wrongType = """
                {"week":"2026-W33","totalTasks":"二百一十","completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"数据统计完成。",
                 "recommendations":["改进"]}""";
        ProgramValidator.ValidationResult r = validator.validate(wrongType);
        assertFalse(r.valid());
        assertTrue(r.errors().stream().anyMatch(e -> e.contains("totalTasks")));
    }

    @Test
    void missingRequiredStringRejected() {
        String missingString = """
                {"totalTasks":210,"completedTasks":182,"failedTasks":12,
                 "abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"数据统计完成。",
                 "recommendations":["改进"]}""";
        ProgramValidator.ValidationResult r = validator.validate(missingString);
        assertFalse(r.valid());
        assertTrue(r.errors().stream().anyMatch(e -> e.contains("week")));
    }

    @Test
    void blankInputRejected() {
        assertFalse(validator.validate("   ").valid());
        assertFalse(validator.validate(null).valid());
    }
}