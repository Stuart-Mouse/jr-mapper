import java.util.EnumSet;

public class NodeDeclaration extends Node {
    public NodeDeclaration(NodeScope parent, Token token) {
        super(parent, token);

        name = token.text();
    }

    String name;
    Node   initExpression;

    public boolean typecheck(EnumSet<TypecheckingFlags> check_flags, Class hint_type) {
        if (check_flags != null && check_flags.contains(TypecheckingFlags.EXPECT_LVALUE)) {
            // log error message
            return false;
        }
        
        // TODO: check for duplicate declaration in scope
        
//        if (!initExpression.typecheck(, null))  return false;
//        if initExpression.valueType

        
        
        
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
