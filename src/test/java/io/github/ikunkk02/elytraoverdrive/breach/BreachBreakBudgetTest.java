package io.github.ikunkk02.elytraoverdrive.breach;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class BreachBreakBudgetTest {
	@Test
	void airPositionsDoNotConsumeBreakBudget() {
		var candidates = IntStream.range(0, 15).boxed().toList();
		var budget = new BreachBreakBudget(5);

		for (int position : candidates) {
			if (position >= 10) {
				budget.recordSuccessfulBreak();
			}
			if (!budget.canAttemptBreak()) {
				break;
			}
		}

		assertEquals(5, budget.successfulBreaks());
	}
}
