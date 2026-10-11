import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CrewMember {

    private int id;

    private String firstname;

    private String lastname;

    private String role;

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Flight> flights;

    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s)",
                firstname, lastname, role);
    }
}
