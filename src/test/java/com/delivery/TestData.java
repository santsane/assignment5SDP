package com.delivery;

import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Location;
import com.delivery.model.Route;
import com.delivery.model.TrafficLevel;

import java.util.List;

/**
 * Shared test fixtures. Same 8 routes as the demo, but tests keep their own copy
 * so a change in the demo data does not silently break the tests.
 *
 * Expected winners for a normal request on these routes:
 *   fastest R2 (27 min), cheapest R5 (2420), eco R4 (score 2.5), balanced R7, shortest R3 (15 km).
 * R8 would be fastest overall but it is closed.
 */
public final class TestData {

    private TestData() {
    }

    public static final Location FROM = new Location("Warehouse", 50.0, 70.0);
    public static final Location TO = new Location("Customer", 50.05, 70.1);

    public static List<Route> routes() {
        return List.of(
                full("R1", 28, 30, 4200, 5.2, TrafficLevel.HIGH, 0, 600, true),
                full("R2", 24, 27, 4800, 4.6, TrafficLevel.LOW, 0, 400, true),
                full("R3", 15, 35, 3000, 3.2, TrafficLevel.HIGH, 0, 0, true),
                full("R4", 22, 42, 3400, 1.4, TrafficLevel.LOW, 0, 0, true),
                full("R5", 31, 55, 1800, 3.9, TrafficLevel.LOW, 0, 0, true),
                full("R6", 36, 48, 2400, 4.1, TrafficLevel.MEDIUM, 5, 150, true),
                full("R7", 20, 40, 2800, 2.2, TrafficLevel.LOW, 0, 0, true),
                full("R8", 18, 25, 3900, 3.0, TrafficLevel.LOW, 0, 0, false)
        );
    }

    /** Simple route: light traffic, no fees, available. */
    public static Route route(String id, double km, int minutes, double cost, double co2) {
        return new Route(id, FROM, TO, km, minutes, cost, co2);
    }

    public static Route full(String id, double km, int minutes, double cost, double co2,
                             TrafficLevel traffic, int weather, double fees, boolean available) {
        return new Route(id, FROM, TO, km, minutes, cost, co2, traffic, weather, fees, available);
    }

    public static DeliveryRequest request(DeliveryPriority priority, double budget, int minutes) {
        return new DeliveryRequest("T-" + priority, priority, budget, minutes, false);
    }

    /** Relaxed request: big budget, long deadline. */
    public static DeliveryRequest standard() {
        return request(DeliveryPriority.STANDARD, 8000, 90);
    }
}
