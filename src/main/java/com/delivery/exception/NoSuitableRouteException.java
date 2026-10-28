package com.delivery.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when no route can be used for a delivery
 * (empty list, or every route breaks a hard constraint).
 * We throw this instead of returning null so the caller always knows what happened.
 */
public class NoSuitableRouteException extends RuntimeException {

    private final Map<String, String> rejectedRoutes;

    public NoSuitableRouteException(String message) {
        this(message, Map.of());
    }

    public NoSuitableRouteException(String message, Map<String, String> rejectedRoutes) {
        super(message);
        this.rejectedRoutes = Collections.unmodifiableMap(new LinkedHashMap<>(rejectedRoutes));
    }

    /** route id -> reason why this route was rejected. */
    public Map<String, String> getRejectedRoutes() {
        return rejectedRoutes;
    }
}
