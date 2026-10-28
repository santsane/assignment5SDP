package com.delivery.planner;

import com.delivery.TestData;
import com.delivery.app.StrategyConfig;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.strategy.FastestRouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyComparatorTest {

    private final StrategyComparator comparator = new StrategyComparator(StrategyConfig.allStrategies());

    /** Finds the winning route id of one strategy in the report. */
    private static String winnerOf(ComparisonReport report, String strategyName) {
        for (ComparisonEntry entry : report.entries()) {
            if (entry.strategyName().equals(strategyName)) {
                return entry.decision().selectedRoute().id();
            }
        }
        throw new AssertionError("Strategy not in report: " + strategyName);
    }

    @Test
    void everyStrategyAnswersTheSameRequest() {
        ComparisonReport report = comparator.compare(TestData.routes(), TestData.standard());

        assertEquals(StrategyConfig.allStrategies().size(), report.entries().size());
        assertTrue(report.entries().stream().allMatch(ComparisonEntry::succeeded));
    }

    @Test
    void strategiesCanReturnDifferentRoutesForTheSameRequest() {
        ComparisonReport report = comparator.compare(TestData.routes(), TestData.standard());

        assertEquals("R2", winnerOf(report, "Fastest"));
        assertEquals("R5", winnerOf(report, "Cheapest"));
        assertEquals("R4", winnerOf(report, "Eco-Friendly"));
        assertEquals("R7", winnerOf(report, "Balanced"));
        assertNotEquals(winnerOf(report, "Fastest"), winnerOf(report, "Cheapest"));
    }

    @Test
    void impossibleRequestDoesNotCrashComparison() {
        DeliveryRequest impossible = TestData.request(DeliveryPriority.STANDARD, 10, 90);

        ComparisonReport report = comparator.compare(TestData.routes(), impossible);

        assertTrue(report.entries().stream().noneMatch(ComparisonEntry::succeeded));
        assertFalse(report.entries().get(0).errorMessage().isBlank());
    }

    @Test
    void reportTextContainsRequestAndEveryStrategy() {
        DeliveryRequest request = new DeliveryRequest("105", DeliveryPriority.STANDARD, 8000, 90, false);

        String text = comparator.compare(TestData.routes(), request).format();

        assertTrue(text.contains("DELIVERY REQUEST #105"));
        assertTrue(text.contains("Fastest Strategy:"));
        assertTrue(text.contains("Cheapest Strategy:"));
        assertTrue(text.contains("Eco-Friendly Strategy:"));
        assertTrue(text.contains("Balanced Strategy:"));
    }

    @Test
    void comparisonNeedsAtLeastOneStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new StrategyComparator(List.of()));
    }

    @Test
    void comparisonWorksWithASingleStrategy() {
        StrategyComparator single = new StrategyComparator(List.of(new FastestRouteStrategy()));

        assertEquals(1, single.compare(TestData.routes(), TestData.standard()).entries().size());
    }
}
