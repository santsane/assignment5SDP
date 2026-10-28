package com.delivery.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The answer of a strategy: which route won, why it won,
 * and which routes were thrown out (with the reason).
 *
 * We return this instead of a bare Route so every strategy can explain itself.
 *
 * @param score          the score of the winner (lower is better for all our strategies)
 * @param reasons        explanation lines of the winning route
 * @param rejectedRoutes route id -> why it was rejected by a hard constraint
 */
public record RouteDecision(
        String strategyName,
        Route selectedRoute,
        double score,
        List<String> reasons,
        Map<String, String> rejectedRoutes
) {

    public RouteDecision {
        reasons = List.copyOf(reasons);
        rejectedRoutes = Collections.unmodifiableMap(new LinkedHashMap<>(rejectedRoutes));
    }

    /** Human readable explanation, printed by the demo. */
    public String explain() {
        StringBuilder sb = new StringBuilder();
        sb.append("Selected: ").append(selectedRoute.id())
                .append(" (").append(strategyName).append(" strategy)\n");
        sb.append("Reason:\n");
        for (String line : reasons) {
            sb.append("  ").append(line).append("\n");
        }
        for (Map.Entry<String, String> entry : rejectedRoutes.entrySet()) {
            sb.append("Route ").append(entry.getKey()).append(" rejected:\n");
            sb.append("  ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
