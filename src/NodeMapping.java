/*
    NodeMapping is our basic DOM-style node for representing data fields in the mapper file.
    It is much like a JSON Node in that it's just a simple key/value pair.
    The value can be a general single-valued expression, or some aggregate type like a NodeObject or NodeArray.
*/

public class NodeMapping extends Node {
    public NodeMapping(NodeScope parent, Token token, String name) {
        super(parent, token);
        this.name = name;
    }

    public String name;
    public Node   valueNode;
    public Object value;


    public boolean typecheck(Class hint_type) {
        if (!valueNode.typecheck(hint_type))  return false;
        valueType = valueNode.getValueType();
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        sb.append(name).append(": ");
        valueNode.serialize(sb);
        return true;
    }

    public Object evaluate(Object hint_value) {
        // NOTE: we only need to evaluate this once, then we can just return the same value
        if (value == null)  value = valueNode.evaluate(hint_value);
        return value;
    }
}
