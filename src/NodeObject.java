/*
    A NodeObject represents a structured aggregate type, akin to a JSON Object.
    The sub-nodes of a NodeObject are all NodeFields.
*/

import java.util.ArrayList;
import java.util.EnumSet;

public class NodeObject extends NodeScope {
    public NodeObject(NodeScope parent, Token token) {
        super(parent, token);
    }

    ArrayList<NodeMapping> fields;

    public boolean typecheck(Class hint_type) {
        valueType = String.class;
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        sb.append("{\n");
        for (var field : fields) {
            field.serialize(sb);
            sb.append(",\n");
        }
        sb.append("}");
        return true;
    }

    public Object evaluate() {
        assert(flags.contains(Flags.TYPECHECKED));
        Object value = null;
        try {
            value = valueType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return value;
    }
}
