/*
    A NodeObject represents a structured aggregate type, akin to a JSON Object.
    The sub-nodes of a NodeObject are all NodeFields.
*/

import java.lang.reflect.Field;
import java.util.ArrayList;

public class NodeObject extends NodeScope {
    public NodeObject(NodeScope parent, Token token) {
        super(parent, token);
    }

    public boolean typecheck(Class hint_type) {
        // A type hint is required for objects at this time.
        // This type hint should be provided by the output object to which the NodeObject is bound.
        // In the future, we may also support an explicit type identifier using the `Type.{}` syntax or similar.
        if (hint_type == null) return false;
        valueType = hint_type;

        for (var field_node: declarations) {
            Class field_hint_type = null;
            if (field_node instanceof NodeMapping mapping) {
                try {
                    field_hint_type = valueType.getDeclaredField(mapping.name).getType();
                } catch (NoSuchFieldException e) {
                    System.out.println(mapping.location() + ": Warning: no such field '" + mapping.name + "' on object of type " + valueType + ".");
                }
            }
            if (!field_node.typecheck(field_hint_type)) return false;
        }

        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        sb.append("{\n");
        if (declarations != null) {
            for (var decl: declarations) {
                decl.serialize(sb);
                sb.append(",\n");
            }
        }
        sb.append("}");
        return true;
    }

    public Object evaluate(Object hint_value) {
        assert(flags.contains(Flags.TYPECHECKED));
//        assert(hint_value.getClass().equals(valueType));
        Object value = hint_value;

        // only need to initialize new object if none was provided
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
            decl.evaluate(null);
            if (decl instanceof NodeMapping mapping) {
                try {
                    Field field = valueType.getDeclaredField(mapping.name);
                    assert(field.getType().equals(mapping.valueType));
                    field.setAccessible(true);
                    field.set(value, mapping.value);
                } catch (NoSuchFieldException e) {
                    System.out.println(mapping.location() + ": Error: no such field '" + mapping.name + "' on object of type " + valueType + ".");
                } catch (IllegalAccessException e) {
                    System.out.println(mapping.location() + ": Error: field '" + mapping.name + "' on object of type '" + valueType + "' is not accessible.");
                    return false;
                }
            }
        }

        return value;
    }
}
