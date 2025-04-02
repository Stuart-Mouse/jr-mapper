import java.lang.reflect.Field;
import java.util.EnumSet;

public class NodeIdentifier extends Node {
    NodeIdentifier(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
        name = token.text();
    }

    String          name;
    NodeDeclaration resolvedDeclaration;
    Field           resolvedField;

    Class<?> _typecheck(Class<?> hint_type) {
        resolvedDeclaration = parentScope.resolveDeclaration(name);
        if (resolvedDeclaration == null) {
            throw new RuntimeException(location() + ": Error: failed to resolve declaration for identifier '" + name + "'.");
        }
        return resolvedDeclaration.typecheck(null);
    }

    void _serialize(StringBuilder sb) {
        sb.append(name);
    }

    Object _evaluate(Object hint_value) {
        return resolvedDeclaration.evaluate(hint_value);
    }
}
