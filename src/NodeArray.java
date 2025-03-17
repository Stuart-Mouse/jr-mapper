/*
    A NodeObject represents a structured aggregate type, akin to a JSON Object.
    The sub-nodes of a NodeObject are all NodeFields.
*/

import java.util.ArrayList;

public class NodeArray extends NodeScope {
    public NodeArray(NodeScope parent, Token token) {
        super(parent, token);
    }

    ArrayList<Node> valueNodes;

    public boolean typecheck(Class hint_type) {
        valueType = String.class;
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        sb.append("{\n");
        for (var node: valueNodes) {
            node.serialize(sb);
            sb.append(",\n");
        }
        sb.append("}");
        return true;
    }

    public Object evaluate() {
        assert(flags.contains(Flags.TYPECHECKED));
        // depending on valueType determined during typechecking, we may need to append elements differently
        // or maybe there's some general enough container methods that we can call, since basically all we need is some .append() method
        // iterate over all fields, executing them and assigning their results to the correct members of 'value'
        return null;
    }
}
