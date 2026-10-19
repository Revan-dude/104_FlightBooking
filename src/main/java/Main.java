import Enums.BookingStatus;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

        crewMemberSet.forEach(member -> member.setFlights(new ArrayList<>()));

        airline.getFlights().forEach(flight -> {
            flight.getCrew().forEach(member -> {
                CrewMember crewMember = crewMemberSet.stream()
                        .filter(member::equals)
                        .findFirst()
                        .orElseThrow();

                crewMember.getFlights().add(flight);
            });
        });

        airline.getFlights().forEach(flight -> {
            List<CrewMember> members = crewMemberSet.stream()
                    .filter(member -> member.getFlights().contains(flight))
                    .collect(Collectors.toCollection(ArrayList::new));

            flight.setCrew(members);
        });

        Map<Integer, Passenger> passengerMap = new HashMap<>();

        airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream())
                .forEach(booking -> {
                    Passenger passenger = booking.getPassenger();
                    Integer passengerId = passenger.getId();

                    Passenger existingPassenger = passengerMap.get(passengerId);

                    if (existingPassenger == null) {
                        passenger.setBookings(new ArrayList<>());
                        passengerMap.put(passengerId, passenger);
                        existingPassenger = passenger;
                    }

                    booking.setPassenger(existingPassenger);
                    existingPassenger.addBooking(booking);
                });

        // All flights

        System.out.println();
        System.out.println("All Flights: ");

        airline.getFlights().forEach(System.out::println);

        // All flights to Vienna

        System.out.println();
        System.out.println("All flights to Vienna: ");

        airline.getFlights().stream()
                .filter(flight -> flight.getArrivalAirport().code()
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
                .max(Comparator.comparingInt(flight -> flight.getBookings().size()))
                .orElseThrow();

        System.out.println(mostBookedFlight + " - Bookings: "
                + mostBookedFlight.getBookings().size());

        // All confirmed bookings

        System.out.println();
        System.out.println("All confirmed bookings: ");

        List<Booking> confirmedBookings = airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream())
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        confirmedBookings.forEach(System.out::println);

        // Total revenue of all confirmed bookings

        double totalRevenue = confirmedBookings.stream()
                .mapToDouble(Booking::totalCost)
                .sum();

        System.out.printf("Total Revenue: %.1f €", totalRevenue);

        // Revenue per flight:

        Map<Flight, Double> revenuePerFlight = new HashMap<>();

        airline.getFlights().forEach(flight -> {
            double revenue = flight.getBookings().stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                    .mapToDouble(Booking::totalCost)
                    .sum();

            revenuePerFlight.put(flight, revenue);
        });

        System.out.println();
        System.out.println("Revenue per flight: ");

        revenuePerFlight.forEach((flight, revenue) ->
                System.out.printf("%n%s: %.1f €", flight.getFlightNumber(), revenue));

        // Avg Price

        double averagePrice = confirmedBookings.stream()
                .mapToDouble(Booking::totalCost)
                .average()
                .getAsDouble();

        System.out.printf("%n%nAvg Price: %.2f €", averagePrice);

        // All crewmembers sorted by lastname

        System.out.println();
        System.out.println();
        System.out.println("All Crewmembers sorted by lastname: ");

        Set<CrewMember> crewMembers = airline.getFlights().stream()
                .flatMap(flight -> flight.getCrew().stream())
                .collect(Collectors.toSet());

        crewMembers.stream()
                .sorted(Comparator.comparing(CrewMember::getLastname))
                .forEach(System.out::println);

        // Crewmember with most flights

        System.out.println();
        System.out.println("Crewmember with most flights: ");

        CrewMember mostFlightsCrewMember = airline.getFlights().stream()
                .flatMap(flight -> flight.getCrew().stream())
                .sorted(Comparator.comparing(CrewMember::getFirstname))
                .max(Comparator.comparingInt(member -> member.getFlights().size()))
                .orElseThrow();

        System.out.printf("%s: %d flights%n",
                mostFlightsCrewMember,
                mostFlightsCrewMember.getFlights().size());

        // Passenger with the most bookings

        System.out.println();
        System.out.println("Passenger with the most bookings: ");

        airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream())
                .map(Booking::getPassenger)
                .distinct()
                .max(Comparator.comparingInt(passenger -> passenger.getBookings().size()))
                .ifPresent(passenger -> System.out.printf("%s: %d bookings%n",
                        passenger, passenger.getBookings().size()));

        // Bookings per Arrival Airport

        System.out.println();
        System.out.println("Bookings per Arrival Airport: ");

        Map<String, Integer> bookingsPerArrivalAirport = new HashMap<>();

        airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream())
                .forEach(booking -> {
                    String city = booking.getFlight().getArrivalAirport().city();
                    int count = bookingsPerArrivalAirport.getOrDefault(city, 0);

                    bookingsPerArrivalAirport.put(city, count + 1);
                });

        bookingsPerArrivalAirport.forEach((city, count) ->
                System.out.printf("%s: %d bookings%n", city, count));

        // The most favorite additional cost

        System.out.println();
        System.out.println("Most favorite Additional:");

        Map<String, Integer> additionalCosts = new HashMap<>();

        airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream())
                .flatMap(booking -> booking.getAdditionalCosts().keySet().stream())
                .forEach(cost -> {
                    int count = additionalCosts.getOrDefault(cost, 0);
                    additionalCosts.put(cost, count + 1);
                });

        System.out.println(additionalCosts.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .map(entry -> String.format("%s: %d times",
                        entry.getKey(), entry.getValue()))
                .orElse("No additional costs found"));

        // Deletion of all canceled Bookings

        airline.getFlights().forEach(flight ->
                flight.getBookings().removeIf(
                        booking -> booking.getStatus() == BookingStatus.CANCELLED));

        // All bookings of the passenger Julia should get the additional cost Priority for free (0.0)

        airline.getFlights().forEach(flight ->
                flight.getBookings().forEach(booking -> {
                    if (booking.getPassenger().getFirstname()
                            .equalsIgnoreCase("julia")) {
                        booking.getAdditionalCosts().put("Priority", 0.0);
                    }
                }));

        // All bookings of the flight OS303 should get an additional cost Meal of 12.50.

        airline.getFlights().stream()
                .filter(flight -> flight.getFlightNumber().equals("OS303"))
                .forEach(flight -> flight.getBookings().forEach(
                        booking -> booking.getAdditionalCosts().put("Meal", 12.50)));

        // All bookings of the flight OS202 should become 10% cheaper

        airline.getFlights().stream()
                .filter(flight -> flight.getFlightNumber().equals("OS202"))
                .forEach(flight -> flight.getBookings().forEach(
                        booking -> booking.setPrice(booking.getPrice() * 0.9)));

        // Add the following crew member to the flight OS303: Lisa Schmidt (id 5, Flight Attendant)

        airline.getFlights().stream()
                .filter(flight -> flight.getFlightNumber().equals("OS303"))
                .findFirst()
                .ifPresent(flight -> flight.getCrew().add(
                        CrewMember.builder()
                                .id(5)
                                .firstname("Lisa")
                                .lastname("Schmidt")
                                .role("Flight Attendant")
                                .build()));

        // Add the following booking to the flight OS101:
        // B1004 | David Weber | Seat 16A | 210 € | CONFIRMED | Baggage=35.00

        airline.getFlights().stream()
                .filter(flight -> flight.getFlightNumber().equals("OS101"))
                .findFirst()
                .ifPresent(flight -> {
                    Booking booking = Booking.builder()
                            .bookingNumber("B1004")
                            .passenger(Passenger.builder()
                                    .firstname("David")
                                    .lastname("Weber")
                                    .id(-1)
                                    .build())
                            .seat("16A")
                            .price(210.0)
                            .status(BookingStatus.CONFIRMED)
                            .additionalCosts(Map.of("Baggage", 35.0))
                            .build();

                    booking.getPassenger().setBookings(List.of(booking));
                    flight.getBookings().add(booking);
                });

        List<Passenger> distinctPassengers = airline.getFlights().stream()
                .flatMap(flight -> flight.getBookings().stream())
                .map(Booking::getPassenger)
                .distinct()
                .toList();

        AtomicInteger numOfMissingIdPassengers = new AtomicInteger(
                (int) distinctPassengers.stream()
                        .filter(passenger -> passenger.getId() == -1)
                        .count());

        distinctPassengers.forEach(passenger -> {
            if (passenger.getId() == -1) {
                passenger.setId(
                        distinctPassengers.size()
                                - numOfMissingIdPassengers.getAndDecrement());
            }
        });

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        Path.of(System.getProperty("user.dir"),
                                "src", "main", "resources", "updated.json"),
                        airline);
    }
}
