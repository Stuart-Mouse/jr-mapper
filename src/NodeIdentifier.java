import java.util.EnumSet;

public class NodeIdentifier extends Node {
    public NodeIdentifier(NodeScope parent, Token token) {
        super(parent, token);
        name = token.text();
    }

    String name;
    NodeDeclaration  resolvedDeclaration;

    @Override
    Object getValue() {
        if (valueType == null) {
            typecheck(null);
        }
        return resolvedDeclaration.getValue();
    }

    public boolean typecheck(Class hint_type) {
        resolvedDeclaration = parentScope.resolveDeclaration(name);
        if (resolvedDeclaration == null) {
            System.out.println(location() + ": Error: failed to resolve declaration for identifier '" + name + "'.");
            return false;
        }
        valueType = resolvedDeclaration.getValueType();
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        if (flags.contains(Flags.PARENTHESIZED)) sb.append("(");
        sb.append(name);
        if (flags.contains(Flags.PARENTHESIZED)) sb.append(")");
        return true;
    }

    public Object evaluate(Object hint_value) {
        return resolvedDeclaration.evaluate(hint_value);
    }
}
    