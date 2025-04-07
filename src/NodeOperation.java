import java.util.EnumSet;

public class NodeOperation extends Node {
    NodeOperation(Parser owningParser, NodeScope parent, Token token, Operator operator, Node left, Node right) {
        super(owningParser, parent, token);
        this.operator = operator;
        this.left     = left;
        this.right    = right;
    }

    Operator  operator;
    Node      left;
    Node      right;

    Class<?> _typecheck(Class<?> hint_type) {
        // TODO: unary operations

        Class<?>  left_type =  left.typecheck(hint_type);
        Class<?> right_type = right.typecheck(hint_type);

        // NOTE: if either left or right type is string, we treat both as a string
        if (left_type.equals(String.class) || right_type.equals(String.class)) {
            return String.class;
        }

        if (NodeNumber.isNumericType(left_type) && NodeNumber.isNumericType(right_type)) {
            if (NodeNumber.areMatchingTypes(left_type, right_type)) {
                if (hint_type != null && NodeNumber.isNumericType(hint_type)) {
                    return hint_type;
                }
            } else {
                return NodeNumber.getWiderType(left_type, right_type);
            }
        }

        if (!left_type.equals(right_type)) {
            // TODO: if left and right types don't match, we can try to coerce to the wider of the two types.
            throw new RuntimeException(location() + ": Error: left and right types of operation do not match: " + left_type + " vs " + right_type + ".");
        }

        // TODO: verify that we can always assume that an operation will have the same type as its operands.
        return left_type;
    }

    void _serialize(StringBuilder sb) {
        left.serialize(sb);
        sb.append(" " + operator.printName + " ");
        right.serialize(sb);
    }

    Object _evaluate(Object hint_value) {
        var lv =  left.evaluate(null);
        var rv = right.evaluate(null);

        // NOTE: if either left or right type is string, we treat both as a string
        if (lv instanceof String || rv instanceof String) {
            switch (operator) {
                case ADD: return lv.toString() + rv.toString();
            }
        }

        if (lv instanceof Number ln && rv instanceof Number rn) {
            Number result = null;
            if (NodeNumber.isFloaty(left.valueType)) {
                switch (operator) {
                    case ADD -> { result = Double.valueOf( ln.doubleValue() + rn.doubleValue() ); }
                    case SUB -> { result = Double.valueOf( ln.doubleValue() - rn.doubleValue() ); }
                    case MUL -> { result = Double.valueOf( ln.doubleValue() * rn.doubleValue() ); }
                    case DIV -> { result = Double.valueOf( ln.doubleValue() / rn.doubleValue() ); }
                }
            } else { // else the number must be an integer type
                switch (operator) {
                    case ADD -> { result = Long.valueOf( ln.longValue() + rn.longValue() ); }
                    case SUB -> { result = Long.valueOf( ln.longValue() - rn.longValue() ); }
                    case MUL -> { result = Long.valueOf( ln.longValue() * rn.longValue() ); }
                    case DIV -> { result = Long.valueOf( ln.longValue() / rn.longValue() ); }
                }
            }
            assert(result != null);
            return NodeNumber.coerceNumber(valueType, result);
        }

        assert(false);
        return null;
    }
}