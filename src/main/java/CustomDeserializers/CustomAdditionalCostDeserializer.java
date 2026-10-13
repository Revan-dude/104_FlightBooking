package CustomDeserializers;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.HashMap;
import java.util.Map;

public class CustomAdditionalCostDeserializer extends ValueDeserializer<Map<String, Double>> {
    @Override
    public Map<String, Double> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {

        Map<String, Double> additionalCosts = new HashMap<>();

        if(p.readValueAs(String.class).isBlank()){
            return additionalCosts;
        }

        String[] values = p.readValueAs(String.class).split(";");

        for(String element : values) {
            String[] splitElement = element.strip().split("=");
            additionalCosts.put(splitElement[0], Double.parseDouble(splitElement[1]));
        }
;
        return additionalCosts;
    }
}
