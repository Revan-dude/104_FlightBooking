import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Passenger {

    private int id;

    private String firstname;

    private String lastname;

    @JsonIgnore
    private List<Booking> bookings;

    public void addBooking(Booking booking) {
        this.bookings.add(booking);
    }

    @Override
    public String toString() {
        return String.format("%s %s",
                firstname, lastname);
    }
}
