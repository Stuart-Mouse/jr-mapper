import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NodeDot extends Node {
    NodeDot(NodeScope parentScope, Token token) {
        super(parentScope, token);
    }

    Node left, right;
    Field  resolvedField;
    Method resolvedMethod;

    boolean typecheck(Class hint_type) {
        // NOTE: currently just reaching in and grabbing the left valueType directly.
        //       this is just a temporary state of affairs, but it presents an interesting problem,
        //       that is, we cannot mark the object node as typechecked until after ALL of its internals have been typechecked
        //       but, because all objects need to be type hinted anyhow, it's actually fine to grab the valueType BEFORE the typechecked flag is set
        //       in the longer term we need to codify more concretely how this situation will resolve
        var left_type = left.getValueType();
        if (left_type == null) {
            System.out.println(left.location() + ": Error: failed to get left type in binary dot.");
            return false;
        }

        // Looks like we will actually not have a method call as right side here, instead we would have the NodeDot in the methodExpression slot of the method call.
        // Unless of course we just make the parsing such that a method expression binds closer than a dot expression.
        // Because fields and methods can overload with one another in Java, we really do need to know that the right side of a Dot expression is a Method call before we can really resolve the Dot
        // But this also means that we will need to make sure that any method call expressions are only used in the context of a dot expression.
        // And perhaps method calls will need to take the base object type as their type hint instead of the result type.

        if (right instanceof NodeIdentifier identifier) {
            try {
                resolvedField = left_type.getDeclaredField(identifier.name);
                right.valueType = resolvedField.getType();
                right.flags.add(Flags.TYPECHECKED);
            } catch (NoSuchFieldException e) {
                System.out.println(identifier.location() + ": Error: no such field '" + identifier.name + "' on object of type '" + left.valueType + "'.");
                return false;
            }
        }
        else if (right instanceof NodeMethodCall method_call) {
            // TODO: This logic for this case should actually be handled in NodeMethodCall.typecheck(), which should set some member for the resolved method which we just check here.
            System.out.println("Error: method calls not yet implemented.");
            // if (!method_call.typecheck(left.valueType)); // left.valueType is used as hint_type for method call since method calls need to know the base type, not result type.
            return false;
        }

        valueType = right.getValueType();
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    boolean serialize(StringBuilder sb) {
        if (flags.contains(Flags.PARENTHESIZED)) sb.append("(");
        left.serialize(sb);
        sb.append(".");
        right.serialize(sb);
        if (flags.contains(Flags.PARENTHESIZED)) sb.append(")");
        return true;
    }

    Object evaluate(Object hint_value) {
        // TODO: technically, we should only have to get the value reference here, not actually evaluate the entire left side value.
        //       but this will be complicated to implement, I think
        var left_value = left.evaluate(null);
        if (left_value == null) return null;

        if (right instanceof NodeIdentifier identifier) {
            try {
                resolvedField.setAccessible(true); // TODO: do we want to do this?
                return resolvedField.get(left_value);
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

