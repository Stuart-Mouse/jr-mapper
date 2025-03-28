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
        // get all overloaded methods by name
        // filter by number of arguments first
        // then do the manual checks per argument and determine cast distance for each possible overload
        // then select best overload
        // second pass over arguments to maybe wrap them for implicit casts or coerce type
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

    public Object evaluate(Object hint_value) {
        // evaluate method expression
        // evaluate all parameters
        // collect parameters into list of Objects
        // use getMethod().invoke() or whatever
        return null;
    }
}
