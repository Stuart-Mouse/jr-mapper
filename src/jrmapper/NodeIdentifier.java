package jrmapper;

import java.lang.reflect.Field;

public class NodeIdentifier extends Node {
    public NodeIdentifier(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
        name = token.text();
    }

    public String          name;
    public NodeDeclaration resolvedDeclaration;
    public Field           resolvedField;

    public Class<?> _typecheck(Class<?> hint_type) {
        resolvedDeclaration = parentScope.resolveDeclaration(name);
        if (resolvedDeclaration == null) {
            throw new RuntimeException(location() + ": Error: failed to resolve declaration for identifier '" + name + "'.");
        }
        return resolvedDeclaration.typecheck(null);
    }

    public void _serialize(StringBuilder sb) {
        sb.append(name);
    }

    public Object _evaluate(Object hint_value) {
        return resolvedDeclaration.evaluate(hint_value);
    }
}
