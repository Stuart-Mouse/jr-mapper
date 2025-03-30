
public class NodeDeclaration extends Node {
    NodeDeclaration(NodeScope parentScope, Token token, String name) {
        super(parentScope, token);
        this.name = name;
    }

    String name;
    Node   valueNode;
    Object value;

    // DeclarationType is used only by this class atm, not by NodeMapping. Some refactoring will probably be necessary later...
    // this does not get set in constructor, but it does need to be resolved during typechecking at the latest
    DeclarationType declarationType;
    public enum DeclarationType { INTERNAL, EXTERNAL };

    boolean typecheck(Class hint_type) {
        if (declarationType == null) {
            System.out.println(location() + ": Error: declarationType was null in typecheck().");
            return false;
        }

        if (!valueNode.typecheck(hint_type))  return false;
        valueType = valueNode.getValueType();
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    @Override
    Object getValue() {
        return value;
    }

    // NodeDeclaration overrides getValueType to allow forward-referencing identifiers.
    // TODO: we will need to implement some checking for circular references here.
    @Override
    Class getValueType() {
        if (valueType == null) {
            typecheck(null);
        }
        return valueType;
    }

    boolean serialize(StringBuilder sb) {
        sb.append("var ").append(name).append(": ");
        valueNode.serialize(sb);
        return true;
    }

    Object evaluate(Object hint_value) {
        if (flags.contains(Flags.EVALUATED)) return value;
        if (value != null) hint_value = value;  // TODO: is this a bad hack? we have to do this in order to use the value that was set on a mapping node manually by setVariable
        else value = hint_value;
        value = valueNode.evaluate(hint_value);
        flags.add(Flags.EVALUATED);
        return value;
    }
}
