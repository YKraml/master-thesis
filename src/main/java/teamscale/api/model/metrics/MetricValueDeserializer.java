package teamscale.api.model.metrics;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class MetricValueDeserializer extends StdDeserializer<Value> {

    public MetricValueDeserializer() {
        this(null);
    }

    protected MetricValueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Value deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);

        if (treeNode instanceof DoubleNode doubleNode) {
            return new ValueDouble(doubleNode.doubleValue());
        }

        if (treeNode instanceof ObjectNode objectNode) {
            if (objectNode.values().next() instanceof ArrayNode arrayNode) {
                double a = arrayNode.get(0).asDouble();
                double b = arrayNode.get(1).asDouble();
                double c = arrayNode.get(2).asDouble();
                double d = arrayNode.get(3).asDouble();
                double e = arrayNode.get(4).asDouble();
                return new ValueMapping(a, b, c, d, e);
            }
        }

        return null;
    }
}
