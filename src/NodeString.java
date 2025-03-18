import java.util.EnumSet;

public class NodeString extends Node {
    public NodeString(NodeScope parent, Token token) {
        super(parent, token);
        value = token.text();
    }

    String value;

    public boolean typecheck(Class hint_type) {
        valueType = String.class;
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        if (flags.contains(Flags.PARENTHESIZED)) sb.append("(");
        sb.append("\"" + value + "\"");
        if (flags.contains(Flags.PARENTHESIZED)) sb.append(")");
        return true;
    }

    public Object evaluate(Object hint_value) {
        assert(flags.contains(Flags.TYPECHECKED));
        return value;
    }
}
    