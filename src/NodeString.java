import java.util.EnumSet;

public class NodeString extends Node {
    public NodeString(NodeScope parent, Token token) {
        super(parent, token);
        value = token.text();
    }

    String value;

    public boolean typecheck(EnumSet<TypecheckingFlags> check_flags, Class hint_type) {
        if (check_flags != null && check_flags.contains(TypecheckingFlags.EXPECT_LVALUE)) {
            System.out.println("Error: A string cannot be used as an lvalue.");
            return false;
        }
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

    public Object evaluate() {
        assert(flags.contains(Flags.TYPECHECKED));
        return value;
    }
}
    