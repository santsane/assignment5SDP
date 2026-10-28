package com.delivery.model;

import java.util.Set;

/**
 * Everything the business wants for one delivery.
 * The values here are what makes different requests pick different routes.
 *
 * @param maxCarbonKg        emission limit in kg (infinity = no limit)
 * @param prohibitedRouteIds routes that must not be used for this delivery
 */
public record DeliveryRequest(
        String requestId,
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        boolean ecoPreferred,
        double maxCarbonKg,
        Set<String> prohibitedRouteIds
) {

    public DeliveryRequest {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request id is missing");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Delivery priority is missing");
        }
        if (!(maxBudget > 0)) {
            throw new IllegalArgumentException("Max budget must be positive but was " + maxBudget);
        }
        if (maxDeliveryMinutes <= 0) {
            throw new IllegalArgumentException("Max delivery time must be positive but was " + maxDeliveryMinutes);
        }
        if (!(maxCarbonKg > 0)) {
            throw new IllegalArgumentException("Max carbon limit must be positive but was " + maxCarbonKg);
        }
        prohibitedRouteIds = (prohibitedRouteIds == null) ? Set.of() : Set.copyOf(prohibitedRouteIds);
    }

    /** Normal request: no emission limit and no prohibited routes. */
    public DeliveryRequest(String requestId, DeliveryPriority priority,
                           double maxBudget, int maxDeliveryMinutes, boolean ecoPreferred) {
        this(requestId, priority, maxBudget, maxDeliveryMinutes, ecoPreferred,
                Double.POSITIVE_INFINITY, Set.of());
    }

    /** Copy of this request with an emission limit. */
    public DeliveryRequest withMaxCarbonKg(double limit) {
        return new DeliveryRequest(requestId, priority, maxBudget, maxDeliveryMinutes,
                ecoPreferred, limit, prohibitedRouteIds);
    }

    /** Copy of this request where the given routes are not allowed. */
    public DeliveryRequest withProhibitedRoutes(String... routeIds) {
        return new DeliveryRequest(requestId, priority, maxBudget, maxDeliveryMinutes,
                ecoPreferred, maxCarbonKg, Set.of(routeIds));
    }
}
