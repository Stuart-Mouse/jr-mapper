import java.util.EnumSet;

public class NodeIdentifier extends Node {
    public NodeIdentifier(NodeScope parent, Token token) {
        super(parent, token);
        name = token.text();
    }

    String name;
    // NodeDeclaration  resolvedDeclaration;
    // we won't have the same complication that LS has where we don't have internal declarations for external variables and procedures. instead everything will need to have some internal declaration that it resolves to, I think.
    // then again not really because of member accessors and such...

    public boolean typecheck(EnumSet<TypecheckingFlags> check_flags, Class hint_type) {
        if (check_flags != null && check_flags.contains(TypecheckingFlags.EXPECT_LVALUE)) {
            // log error message
            return false;
        }
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        if (flags.contains(Flags.PARENTHESIZED)) sb.append("(");
        sb.append(name);
        if (flags.contains(Flags.PARENTHESIZED)) sb.append(")");
        return true;
    }

    public Object evaluate() {
        // TODO: we actually need to implement identifier resolution and return object referenced by identifier
        return name;
    }
}
    