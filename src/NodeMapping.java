/*
    NodeMapping is our basic DOM-style node for representing data fields in the mapper file.
    It is much like a JSON Node in that it's just a simple key/value pair.
    The value can be a general single-valued expression, or some aggregate type like a NodeObject or NodeArray.
*/

public class NodeMapping extends NodeDeclaration {
    public NodeMapping(NodeScope parent, Token token, String name) {
        super(parent, token, name);
    }

    public boolean typecheck(Class hint_type) {
        if (valueType == null && hint_type == null) {
            System.out.println(location() + ": Error: unable to resolve type for mapping node. If this is a root-level output node, then you must call setVariable to provide an output object before typechecking the file.");
            return false;
        }
        if (valueType != null)  hint_type = valueType;
        else valueType = hint_type;

        if (!valueNode.typecheck(hint_type))  return false;
        if (valueType != null)  assert(valueNode.getValueType() == valueType);
        else valueType = valueNode.getValueType();

        flags.add(Flags.TYPECHECKED);
        return true;
    }

    @Override
    public boolean serialize(StringBuilder sb) {
        sb.append(name).append(": ");
        valueNode.serialize(sb);
        return true;
    }
}
