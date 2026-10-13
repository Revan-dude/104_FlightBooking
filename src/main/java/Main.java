import Enums.BookingStatus;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Main {

    static void main(String[] args) {


        ObjectMapper mapper = new ObjectMapper();

        Airline airline = mapper.readValue(
                Airline.class.getResourceAsStream("airline.json"),
                Airline.class);


        // Recreating references

        Set<CrewMember> crewMemberSet = airline.getFlights().stream()
                .flatMap(flight -> flight.getCrew().stream())
                .collect(Collectors.toSet());

        crewMemberSet.forEach(c -> c.setFlights(new ArrayList<>()));

        airline.getFlights().forEach(flight -> {
            flight.getCrew().forEach(c -> crewMemberSet.stream()
                    .filter(c::equals)
                    .findFirst()
                    .orElseThrow()
                    .getFlights()
                    .add(flight));
        });

        airline.getFlights().forEach(flight -> {
            List<CrewMember> member =  new ArrayList<>();
            member.addAll(crewMemberSet.stream()
                    .filter(c -> c.getFlights().contains(flight))
                    .collect(Collectors.toCollection(ArrayList::new)));


            flight.setCrew(member);
        });

        Map<Integer, Passenger> passengerMap = new HashMap<>();

        airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream())
                .forEach(b -> {
                    Passenger p = passengerMap.computeIfAbsent(b.getPassenger().getId(), id -> {
                        Passenger first = b.getPassenger();
                        first.setBookings(new ArrayList<>());
                        return first;
                    });

                    b.setPassenger(p);
                    p.addBooking(b);
                });



        // All flights

        System.out.println();
        System.out.println("All Flights: ");

        airline.getFlights().forEach(System.out::println);

        // All flights to Vienna

        System.out.println();
        System.out.println("All flights to Vienna: ");

        airline.getFlights().stream()
                .filter(flight -> flight.getArrivalAirport()
                        .code()
                        .equalsIgnoreCase("vie"))
                .forEach(System.out::println);

        // All flights sorted by departure time

        System.out.println();
        System.out.println("All flights sorted by departure time: ");

        airline.getFlights().stream()
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .forEach(System.out::println);

        // The flight with the most bookings

        System.out.println();
        System.out.println("The flight with the most bookings: ");

        Flight mostBookedFlight = airline.getFlights().stream()
                .max(Comparator.comparing(flight -> flight.getBookings().size()))
                .orElseThrow();

        System.out.println(mostBookedFlight + " - Bookings: " + mostBookedFlight.getBookings().size());

        // All confirmed bookings

        System.out.println();
        System.out.println("All confirmed bookings: ");

        List<Booking> confirmedBookings = airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED))
                .toList();

        confirmedBookings.forEach(System.out::println);

        // Total revenue of all confirmed bookings

        double totalRevenue = confirmedBookings.stream().mapToDouble(Booking::totalCost).sum();

        System.out.printf("Total Revenue: %.1f €", totalRevenue);

        // Revenue per flight:

        Map<Flight, Double> revenuePerFlight = new HashMap<>();

        airline.getFlights().forEach(f -> {
            double revenue = f.getBookings().stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                    .mapToDouble(Booking::totalCost)
                    .sum();

            revenuePerFlight.put(f, revenue);
        });

        System.out.println();
        System.out.println("Revenue per flight: ");
        revenuePerFlight.forEach((f, d) -> System.out.printf("%n%s: %.1f €", f.getFlightNumber(), d));

        // Avg Price

        double averagePrice = confirmedBookings.stream().mapToDouble(Booking::totalCost).average().getAsDouble();

        System.out.printf("%n%nAvg Price: %.2f €", averagePrice);


        // All crewmembers sorted by lastname

        System.out.println();
        System.out.println();
        System.out.println("All Crewmembers sorted by lastname: ");

        Set<CrewMember> crewMembers = airline.getFlights().stream()
                .flatMap(f -> f.getCrew().stream())
                .collect(Collectors.toSet());

        crewMembers.stream()
                .sorted(Comparator.comparing(CrewMember::getLastname))
                .forEach(System.out::println);

        // Crewmember with most flights

        System.out.println();
        System.out.println("Crewmember with most flights: ");

        CrewMember mostFlightsCrewMember = airline.getFlights().stream()
                .flatMap(f -> f.getCrew().stream())
                .sorted(Comparator.comparing(CrewMember::getFirstname))
                .max(Comparator.comparing(c -> c.getFlights().size()))
                .get();

        System.out.printf("%s: %d flights%n", mostFlightsCrewMember, mostFlightsCrewMember.getFlights().size());

        // Passenger with the most bookings

        System.out.println();
        System.out.println("Passenger with the most bookings: ");

        airline.getFlights().stream()
                .flatMap(f -> f.getBookings().stream())
                .map(Booking::getPassenger)
                .max(Comparator.comparing(p -> p.getBookings().size()))
                .ifPresent(p -> System.out.printf("%s: %d bookings%n", p, p.getBookings().size()));

        // Bookings per Arrival Airport

        System.out.println();
        System.out.println("Bookings per Arrival Airport: ");

        Map<String, Integer> bookingsPerArrivalAirport = new HashMap<>();

        airline.getFlights().stream()
                .flatMap(f -> f.getBookings().stream())
                .forEach(b -> {
                    bookingsPerArrivalAirport.compute(b.getFlight().getArrivalAirport().city(), (k, v) -> v == null ? 1 : v + 1);
                });

        bookingsPerArrivalAirport.forEach((k, v) -> System.out.printf("%s: %d bookings%n", k, v));

        // The most favorite additional cost

        System.out.println();
        System.out.println("Most favorite Additional:");

        Map<String, Integer> additionalCosts = new HashMap<>();

        airline.getFlights().stream()
                .flatMap(f -> f.getBookings().stream())
                .flatMap(b -> b.getAdditionalCosts().keySet().stream())
                .forEach(c -> additionalCosts.compute(c, (k, v) -> v == null ? 1 : v + 1));

        System.out.println(additionalCosts.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .map(e -> String.format("%s: %d times", e.getKey(), e.getValue()))
                .orElse("No additional costs found"));

        // Deletion of all cancelled Bookings

        airline.getFlights().stream().forEach(f -> f.getBookings()
                .removeIf(b -> b.getStatus() == BookingStatus.CANCELLED));

        // All bookings of the passenger Julia should get the additional cost Priority for free (0.0)

        airline.getFlights()
                .forEach(f -> f.getBookings().stream()
                        .forEach(b -> {
                            if (b.getPassenger().getFirstname().equalsIgnoreCase("julia")) {
                                b.getAdditionalCosts().put("Priority", 0.0);
                            }
                        })
                );

        // All bookings of the flight OS303 should get an additional cost Meal of 12.50.

        airline.getFlights().stream()
                .filter(f -> f.getFlightNumber().equals("OS303"))
                .forEach(f -> f.getBookings().stream()
                        .forEach(b -> b.getAdditionalCosts().put("Meal", 12.50))
                );

        // All bookings of the flight OS202 should become 10% cheaper

        airline.getFlights().stream()
                .filter(f -> f.getFlightNumber().equals("OS202"))
                .forEach(f -> f.getBookings().stream()
                        .forEach(b -> b.setPrice(b.getPrice() * 0.9))
                );

        // Add the following crew member to the flight OS303: Lisa Schmidt (id 5, Flight Attendant)

        airline.getFlights().stream()
                .filter(f -> f.getFlightNumber().equals("OS303"))
                .findFirst()
                .ifPresent(f -> f.getCrew().add(CrewMember.builder()
                                .id(5)
                                .firstname("Lisa")
                                .lastname("Schmidt")
                                .role("Flight Attendant")
                                .build()));

        // Add the following booking to the flight OS101:
        //B1004 | David Weber | Seat 16A | 210 € | CONFIRMED | Baggage=35.00

        airline.getFlights().stream()
                .filter(f -> f.getFlightNumber().equals("OS101"))
                .findFirst()
                .ifPresent(f -> {

                    Booking booking = Booking.builder()
                            .bookingNumber("B1004")
                            .passenger(Passenger.builder()
                                    .firstname("David")
                                    .lastname("Weber")
                                    .build())
                            .seat("16A")
                            .price(210.0)
                            .status(BookingStatus.CONFIRMED)
                            .additionalCosts(Map.of("Baggage", 35.0))
                            .build();

                    booking.getPassenger().setBookings(List.of(booking));
                    f.getBookings().add(booking);
                });

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(Path.of(System.getProperty("user.dir"),"src", "main", "resources", "updated.json"),
                        airline);
    }
}
