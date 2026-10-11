import CustomDeserializers.CustomAdditionalCostDeserializer;
import CustomSerializers.CustomAdditionalCostSerializer;
import Enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Booking {
    private String bookingNumber;

    private String seat;

    private double price;

    @JsonSerialize(using = CustomAdditionalCostSerializer.class)
    @JsonDeserialize(using = CustomAdditionalCostDeserializer.class)
    private Map<String, Double> additionalCosts = new LinkedHashMap<>();

    private BookingStatus status;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Passenger passenger;

    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Flight flight;

    public double getAdditionalCostTotal() {
        return additionalCosts.values().stream()
                .reduce(0.0, Double::sum);
    }

    public double totalCost() {
        return price + getAdditionalCostTotal();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | Seat %s | %.1f €",
                bookingNumber, passenger , seat, totalCost());
    }
}
