package CustomSerializers;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomAdditionalCostSerializer extends ValueSerializer<Map<String, Double>> {
    @Override
    public void serialize(Map<String, Double> value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        List<String> objects = new ArrayList<>();

        Iterator<Map.Entry<String, Double>> iterator = value.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            objects.add(String.format("%s=%.2f", entry.getKey(), entry.getValue()));
        }

        gen.writeString(String.join("; ", objects));
    }
}
