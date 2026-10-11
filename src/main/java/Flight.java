import Enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Flight {

    private String flightNumber;

    @JsonProperty("departure")
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime departureTime;

    private Aircraft aircraft;

    private Airport arrivalAirport;

    private Airport departureAirport;

    @JsonManagedReference
    private List<Booking> bookings;

    private List<CrewMember> crew;

    public void addBooking(Booking booking) {
        this.bookings.add(booking);
    }

    public void addCrewMember(CrewMember crewMember) {
        this.crew.add(crewMember);
    }

    public double getOccupancy() {
        double confirmedBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .toList()
                .size();

        return confirmedBookings / aircraft.getSeats();
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s | %sT%s",
                flightNumber, departureAirport.getCode(), arrivalAirport.getCode(),
                departureTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                departureTime.format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}
