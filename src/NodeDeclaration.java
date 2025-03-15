
public class NodeDeclaration extends Node {
    public NodeDeclaration(NodeScope parent, Token token, String name) {
        super(parent, token);

    }

    String name;
    Node   initExpression;

    public boolean typecheck(Class hint_type) {
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
