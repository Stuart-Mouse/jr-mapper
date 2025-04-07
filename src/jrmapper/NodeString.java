package jrmapper;

public class NodeString extends Node {
    public NodeString(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
        value = token.text();
    }

    public String value;

    public Class<?> _typecheck(Class<?> hint_type) {
        return String.class;
    }

    public void _serialize(StringBuilder sb) {
        sb.append("\"").append(value).append("\"");
    }

    public Object _evaluate(Object hint_value) {
        return value;
    }
}
    