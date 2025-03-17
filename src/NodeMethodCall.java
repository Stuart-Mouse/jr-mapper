import java.lang.reflect.Method;
import java.util.ArrayList;

public class NodeMethodCall extends Node {
    public NodeMethodCall(NodeScope parent, Token token) {
        super(parent, token);
    }

    public NodeIdentifier  methodIdentifier;
    public Method          resolvedMethod;
    public ArrayList<Node> parameters;

    // NOTE: type_hint here is the base object type, not the result type.
    public boolean typecheck(Class type_hint) {
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        methodIdentifier.serialize(sb);
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
