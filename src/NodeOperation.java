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

        // If either left or right type is string, we treat both as a string
        if (left_type.equals(String.class) || right_type.equals(String.class)) {
            return String.class;
        }

        // For numbers, we have some additional logic to coerce to the hint_type or wider type.
        if (NodeNumber.isNumericType(left_type) && NodeNumber.isNumericType(right_type)) {
            if (NodeNumber.areMatchingTypes(left_type, right_type)) {
                if (hint_type != null && NodeNumber.isNumericType(hint_type)) {
                    return hint_type;
                }
            } else {
                return NodeNumber.getWiderType(left_type, right_type);
            }
        }

        if (left_type.equals(right_type)) {
            return left_type;
        }

        throw new RuntimeException(location() + ": Error: left and right types of operation do not match: " + left_type + " vs " + right_type + ".");
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
            throw new RuntimeException(location() + ": Error: invalid string operator: " + operator.printName);
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
            if (result == null) {
                throw new RuntimeException(location() + ": Error: failed to execute operation " + operator.printName + " on number types " + left.valueType + ", " + right.valueType);
            }
            return NodeNumber.coerceNumber(valueType, result);
        }

        throw new RuntimeException(location() + ": Error: tried to execute unknown operation " + operator.printName + " with types " + left.valueType + ", " + right.valueType);
    }
}