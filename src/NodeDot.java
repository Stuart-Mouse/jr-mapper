import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NodeDot extends Node {
    NodeDot(Parser owningParser, NodeScope parentScope, Token token) {
        super(owningParser, parentScope, token);
    }

    Node   left, right;
    Field  resolvedField;
    Method resolvedMethod;

    Class _typecheck(Class hint_type) {
        var left_type = left.typecheck(null);
        if (left_type == null) {
            throw new RuntimeException(left.location() + ": Error: failed to get left type in binary dot.");
        }

        if (right instanceof NodeIdentifier identifier) {
            // We manually typecheck right side identifier in this case. Perks of not making everythign private.
            try {
                resolvedField = left_type.getDeclaredField(identifier.name);
                right.valueType = resolvedField.getType();
                right.setTypechecked();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(identifier.location() + ": Error: no such field '" + identifier.name + "' on object of type '" + left.valueType + "'.");
            }
        }
        else if (right instanceof NodeMethodCall method_call) {
            // TODO: This logic for this case should actually be handled in NodeMethodCall.typecheck(), which should set some member for the resolved method which we just check here.
            // if (!method_call.typecheck(left.valueType)); // left.valueType is used as hint_type for method call since method calls need to know the base type, not result type.
            throw new RuntimeException(right.location() + ": Error: method calls not yet implemented.");
        }
        else {
            throw new RuntimeException(right.location() + ": Error: invalid node type on right-hand side of NodeDot.");
        }
        
        return right.valueType;
    }

    void _serialize(StringBuilder sb) {
        left.serialize(sb);
        sb.append(".");
        right.serialize(sb);
    }

    Object _evaluate(Object hint_value) {
        // TODO: technically, we should only have to get the value reference here, not actually evaluate the entire left side value.
        //       but this will be complicated to implement, I think
        var left_value = left.evaluate(null);

        if (resolvedField != null) {
            try {
                resolvedField.setAccessible(true);
                return resolvedField.get(left_value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(location() + ": Error: field '" + ((NodeIdentifier)right).name + "' on object of type '" + left.valueType + "' is not accessible.");
            }
        }
        else if (resolvedMethod != null) {
            // method_call.resolvedMethod.invoke();
        }
        
        assert(false);
        return null;
    }
}

