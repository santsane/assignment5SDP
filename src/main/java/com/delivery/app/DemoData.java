package com.delivery.app;

import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Location;
import com.delivery.model.Route;
import com.delivery.model.TrafficLevel;

import java.util.List;

/**
 * Demo data: 8 candidate routes (R8 is closed) and a few requests.
 *
 * Each route is good at something different, so different strategies pick different routes:
 *   R2 fastest, R5 cheapest, R4 greenest, R3 shortest, R7 best compromise.
 */
public final class DemoData {

    private DemoData() {
    }

    public static final Location WAREHOUSE = new Location("Central Warehouse", 50.00, 70.00);
    public static final Location CUSTOMER = new Location("Customer Address", 50.05, 70.10);

    public static List<Route> routes() {
        return List.of(
                // id, km, minutes, base cost, CO2, traffic, weather min, fees, available
                route("R1", 28, 30, 4200, 5.2, TrafficLevel.HIGH, 0, 600, true),   // highway with toll, jammed
                route("R2", 24, 27, 4800, 4.6, TrafficLevel.LOW, 0, 400, true),    // ring road, fast but expensive
                route("R3", 15, 35, 3000, 3.2, TrafficLevel.HIGH, 0, 0, true),     // shortest, city centre
                route("R4", 22, 42, 3400, 1.4, TrafficLevel.LOW, 0, 0, true),      // green corridor
                route("R5", 31, 55, 1800, 3.9, TrafficLevel.LOW, 0, 0, true),      // cheap back roads
                route("R6", 36, 48, 2400, 4.1, TrafficLevel.MEDIUM, 5, 150, true), // long loop, rain
                route("R7", 20, 40, 2800, 2.2, TrafficLevel.LOW, 0, 0, true),      // riverside, good compromise
                route("R8", 18, 25, 3900, 3.0, TrafficLevel.LOW, 0, 0, false)      // airport bypass, CLOSED
        );
    }

    private static Route route(String id, double km, int minutes, double cost, double co2,
                               TrafficLevel traffic, int weather, double fees, boolean available) {
        return new Route(id, WAREHOUSE, CUSTOMER, km, minutes, cost, co2, traffic, weather, fees, available);
    }

    /** Request A: urgent medical delivery, money is not a problem. */
    public static DeliveryRequest requestA() {
        return new DeliveryRequest("A", DeliveryPriority.URGENT, 10000, 60, false);
    }

    /** Request B: normal cheap delivery, small budget. */
    public static DeliveryRequest requestB() {
        return new DeliveryRequest("B", DeliveryPriority.ECONOMY, 3000, 120, false);
    }

    /** Request C: customer wants the green option. */
    public static DeliveryRequest requestC() {
        return new DeliveryRequest("C", DeliveryPriority.ECO, 6000, 90, true);
    }

    /** Request D (#105): standard delivery, used for comparison mode. */
    public static DeliveryRequest requestD() {
        return new DeliveryRequest("105", DeliveryPriority.STANDARD, 8000, 90, false);
    }
}
