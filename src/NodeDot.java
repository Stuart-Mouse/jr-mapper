import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NodeDot extends Node {
    public NodeDot(NodeScope parentScope, Token token) {
        super(parentScope, token);
    }

    Node left, right;

    public boolean typecheck(Class hint_type) {
        if (!left.typecheck(null)) return false;

        // Looks like we will actually not have a method call as right side here, instead we would have the NodeDot in the methodExpression slot of the method call.
        // Unless of course we just make the parsing such that a method expression binds closer than a dot expression.
        // Because fields and methods can overload with one another in Java, we really do need to know that the right side of a Dot expression is a Method call before we can really resolve the Dot
        // But this also means that we will need to make sure that any method call expressions are only used in the context of a dot expression.
        // And perhaps method calls will need to take the base object type as their type hint instead of the result type.

        if (right instanceof NodeIdentifier identifier) {
            try {
                // here we just check that the field exists.
                // maybe we should store it somewhere though, so that we don't have to do this again in evaluate().
                Field field = left.valueType.getField(identifier.name);
            } catch (NoSuchFieldException e) {
                System.out.println(identifier.location() + ": Error: no such field '" + identifier.name + "' on object of type '" + left.valueType + "'.");
                return false;
            }
        }
        else if (right instanceof NodeMethodCall method_call) {
            // TODO: This logic for this case should actually be handled in NodeMethodCall.typecheck(), which should set some member for the resolved method which we just check here.
            //       Likewise, it would also be nice if we saved the resolved Field so that we don't have to find it again in evaluate(), but this is not too important at the moment.
            System.out.println("Error: method calls not yet implemented.");
            // if (!method_call.typecheck(left.valueType)); // left.valueType is used as hint_type for method call since method calls need to know the base type, not result type.
            return false;
        }

        valueType = right.valueType;
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        if (flags.contains(Flags.PARENTHESIZED)) sb.append("(");
        left.serialize(sb);
        sb.append(".");
        right.serialize(sb);
        if (flags.contains(Flags.PARENTHESIZED)) sb.append(")");
        return true;
    }

    public Object evaluate() {
        var left_value = left.evaluate();
        if (left_value == null) return null;

        if (right instanceof NodeIdentifier identifier) {
            var right_value = right.evaluate();
            if (right_value == null) return null;
            try {
                Field field = left.valueType.getField(identifier.name);
                // field.setAccessible(true); // TODO: do we want to do this?
                field.set(left_value, right_value);
            } catch (NoSuchFieldException e) {
                System.out.println(identifier.location() + ": Error: no such field '" + identifier.name + "' on object of type '" + left.valueType + "'.");
                return false;
            } catch (IllegalAccessException e) {
                System.out.println(identifier.location() + ": Error: field '" + identifier.name + "' on object of type '" + left.valueType + "' is not accessible.");
                return false;
            }
        }
        else if (right instanceof NodeMethodCall method_call) {
            // method_call.resolvedMethod.invoke();
        }
        return null;
    }
}

