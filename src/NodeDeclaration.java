
public class NodeDeclaration extends Node {
    public NodeDeclaration(NodeScope parentScope, Token token, String name) {
        super(parentScope, token);
        this.name = name;
    }

    String name;
    Node   valueNode;
    Object value;

    public enum DeclarationType { VAR, INPUT, OUTPUT };

    public boolean typecheck(Class hint_type) {
        if (!valueNode.typecheck(hint_type))  return false;
        valueType = valueNode.getValueType();
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    // NodeDeclaration overrides getValueType to allow forward-referencing identifiers.
    // TODO: we will need to implement some checking for circular references here.
    @Override
    public Class getValueType() {
        if (!flags.contains(Flags.TYPECHECKED)) {
            typecheck(null);
        }
        return valueType;
    }

    public boolean serialize(StringBuilder sb) {
        sb.append("var ").append(name).append(": ");
        valueNode.serialize(sb);
        return true;
    }

    public Object evaluate(Object hint_value) {
        // NOTE: we only need to evaluate this once, then we can just return the same value
        if (value == null)  value = valueNode.evaluate(hint_value);
        return value;
    }
}
