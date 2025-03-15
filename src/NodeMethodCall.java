import java.util.ArrayList;

public class NodeMethodCall extends Node {
    public NodeMethodCall(NodeScope parent, Token token) {
        super(parent, token);
    }

    public Node methodExpression;
    public ArrayList<Node> parameters;

    public boolean typecheck(Class type_hint) {
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        methodExpression.serialize(sb);
        sb.append("(");
        boolean first = true;
        for (var param: parameters) {
            if (!first) sb.append(", ");
            param.serialize(sb);
        }
        sb.append(")");
        return true;
    }

    public Object evaluate() {
        // evaluate method expression
        // evaluate all parameters
        // collect parameters into list of Objects
        // use getMethod().invoke() or whatever
        return null;
    }
}
