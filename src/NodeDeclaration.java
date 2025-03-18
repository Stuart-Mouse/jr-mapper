
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
        valueType = valueNode.valueType;
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        sb.append("var ").append(name).append(": ");
        valueNode.serialize(sb);
        return true;
    }

    public Object evaluate() {
        // NOTE: we only need to evaluate this once, then we can just return the same value
        if (value == null)  value = valueNode.evaluate();
        return value;
    }
}
