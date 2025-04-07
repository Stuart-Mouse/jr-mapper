import java.util.EnumSet;

public class NodeString extends Node {
    NodeString(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
        value = token.text();
    }

    String value;

    Class<?> _typecheck(Class<?> hint_type) {
        return String.class;
    }

    void _serialize(StringBuilder sb) {
        sb.append("\"").append(value).append("\"");
    }

    Object _evaluate(Object hint_value) {
        return value;
    }
}
    