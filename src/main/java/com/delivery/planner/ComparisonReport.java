package com.delivery.planner;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.util.Fmt;

import java.util.List;

/**
 * All strategies' answers to the same request, ready to print.
 */
public record ComparisonReport(DeliveryRequest request, List<ComparisonEntry> entries) {

    public ComparisonReport {
        entries = List.copyOf(entries);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELIVERY REQUEST #").append(request.requestId())
                .append(" (").append(request.priority())
                .append(", budget ").append(Fmt.money(request.maxBudget()))
                .append(", deadline ").append(request.maxDeliveryMinutes()).append(" min)\n");

        for (ComparisonEntry entry : entries) {
            sb.append("\n").append(entry.strategyName()).append(" Strategy:\n");
            if (!entry.succeeded()) {
                sb.append("  No route: ").append(entry.errorMessage()).append("\n");
                continue;
            }
            Route route = entry.decision().selectedRoute();
            sb.append("  Route ").append(route.id()).append("\n");
            sb.append("  Time: ").append(route.effectiveMinutes()).append(" min\n");
            sb.append("  Cost: ").append(Fmt.money(route.totalCost(request.priority()))).append("\n");
            sb.append("  CO2: ").append(Fmt.dec2(route.carbonEmission())).append(" kg\n");
            sb.append("  Distance: ").append(Fmt.dec1(route.distanceKm())).append(" km\n");
            sb.append("  Score: ").append(Fmt.dec2(entry.decision().score())).append("\n");
        }
        return sb.toString();
    }
}
