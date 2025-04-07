package jrmapper;
/*
    A NodeObject represents a structured aggregate type, akin to a JSON Object.
    The sub-nodes of a NodeObject are all NodeFields.
*/

import java.lang.reflect.Field;

public class NodeObject extends NodeScope {
    public NodeObject(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
    }

    // Returns null if no such field was found, so that caller can throw with source location information.
    public static Field resolveObjectField(Class<?> type, String field_name) {
        if (type != null) {
            try {
                return type.getDeclaredField(field_name);
            } catch (NoSuchFieldException e) { }

            for (var anInterface: type.getInterfaces()) {
                return resolveObjectField(anInterface, field_name);
            }
            return resolveObjectField(type.getSuperclass(), field_name);
        }
        return null;
    }

    public Class<?> _typecheck(Class<?> hint_type) {
        if (hint_type == null) {
            throw new RuntimeException(location() + ": Error: no hint type provided to NodeObject.typecheck().");
        }
        // We go ahead and manually set valueType here since we refer to it below.
        valueType = hint_type;

        for (var field_node: declarations) {
            field_node.resolvedField = resolveObjectField(valueType, field_node.name);
            if (field_node.resolvedField == null) {
                throw new RuntimeException(field_node.location() + ": Error: no such field '" + field_node.name + "' on object of type " + valueType + ".");
            }
            field_node.typecheck(field_node.resolvedField.getType());
            // TODO: not yet sure if we need to check the result of the above typechecking call.
        }
        return valueType;
    }

    public void _serialize(StringBuilder sb) {
        sb.append("{\n");
        if (declarations != null) {
            for (var decl: declarations) {
                decl.serialize(sb);
                sb.append(",\n");
            }
        }
        sb.append("}");
    }

    public Object _evaluate(Object hint_value) {
        // NOTE: for now we are asserting that a NodeObject is not evaluated more than once,
        //       since only Identifiers and Declarations should ever be executed multiple times.
        assert(!isEvaluated()); 
        setEvaluated();

        // only need to initialize new object if none was provided
        Object value = hint_value;
        if (value == null) {
            try {
                value = valueType.getDeclaredConstructor().newInstance();
                // TODO: is there some way we can create an object without needing to invoke the constructor?
                //       if not, we will probably want to add to the typechecking step some logic that ensures all constructor fields are declared
                //       perhaps we can even have some special syntax for constructor parameters as opposed to normal fields
            } catch (Exception e) {
                System.out.println(location() + ": Exception while attempting to construct object of type " + valueType);
                throw new RuntimeException(e);
            }
        }

        for (var decl: declarations) {
            try {
                var decl_value = decl.evaluate(null);
                decl.resolvedField.setAccessible(true);
                decl.resolvedField.set(value, decl_value);
            } catch (IllegalAccessException e) {
                System.out.println(decl.location() + ": Error: field '" + decl.name + "' on object of type '" + valueType + "' is not accessible.");
                return false;
            }
        }

        return value;
    }
}
