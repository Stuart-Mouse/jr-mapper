package jrmapper;

import java.lang.reflect.Method;

public class NodeDot extends Node {
    public NodeDot(Parser owningParser, NodeScope parentScope, Token token) {
        super(owningParser, parentScope, token);
    }

    public Node   left, right;
    public Method resolvedMethod;

    public Class<?> _typecheck(Class<?> hint_type) {
        var left_type = left.typecheck(null);
        if (left_type == null) {
            throw new RuntimeException(left.location() + ": Error: failed to get left type in binary dot.");
        }

        if (right instanceof NodeIdentifier identifier) {
            // We manually typecheck right side identifier in this case. Perks of not making everythign private.
            identifier.resolvedField = NodeObject.resolveObjectField(left_type, identifier.name);
            if (identifier.resolvedField == null) {
                throw new RuntimeException(identifier.location() + ": Error: no such field '" + identifier.name + "' on object of type " + left_type + ".");
            }
            identifier.valueType = identifier.resolvedField.getType();
            identifier.setTypechecked();
        }
        else if (right instanceof NodeMethodCall method_call) {
            // NOTE: we hint with left type for method calls, since the method call obviously needs to know the base object type to which the method belongs.
            right.typecheck(left_type);
        }
        else {
            throw new RuntimeException(right.location() + ": Error: invalid node type on right-hand side of NodeDot.");
        }
        
        return right.valueType;
    }

    public void _serialize(StringBuilder sb) {
        left.serialize(sb);
        sb.append(".");
        right.serialize(sb);
    }

    public Object _evaluate(Object hint_value) {
        var left_value = left.evaluate(null);

        if (right instanceof NodeIdentifier identifier) {
            assert(identifier.resolvedField != null);
            try {
                identifier.resolvedField.setAccessible(true);
                return identifier.resolvedField.get(left_value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(location() + ": Error: field '" + ((NodeIdentifier)right).name + "' on object of type '" + left.valueType + "' is not accessible.");
            }
        }
        else if (right instanceof NodeMethodCall method_call) {
             return method_call.evaluate(left_value);
        }
        
        assert(false);
        return null;
    }
}

