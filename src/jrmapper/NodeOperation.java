package jrmapper;

public class NodeOperation extends Node {
    public NodeOperation(Parser owningParser, NodeScope parent, Token token, Operator operator, Node left, Node right) {
        super(owningParser, parent, token);
        this.operator = operator;
        this.left     = left;
        this.right    = right;
    }

    public Operator  operator;
    public Node      left;
    public Node      right;

    public Class<?> _typecheck(Class<?> hint_type) {
        // TODO: unary operations

        Class<?>  left_type =  left.typecheck(hint_type);
        Class<?> right_type = right.typecheck(hint_type);

        // If either left or right type is string, we treat both as a string
        if (left_type.equals(String.class) || right_type.equals(String.class)) {
            if (!operator.equals(Operator.ADD)) {
                throw new RuntimeException(location() + ": Error: unknown operation " + operator.printName + " with types " + left.valueType + ", " + right.valueType);
            }
            return String.class;
        }

        // For numbers, we have some additional logic to coerce to the hint_type or wider type.
        if (NodeNumber.isNumericType(left_type) && NodeNumber.isNumericType(right_type)) {
            switch (operator) {
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                    if (NodeNumber.areMatchingTypes(left_type, right_type)) {
                        if (hint_type != null && NodeNumber.isNumericType(hint_type)) {
                            return hint_type;
                        }
                    } else {
                        return NodeNumber.getWiderType(left_type, right_type);
                    }
                    break;

                case COMPARE_EQUAL:
                case LESS_THAN_OR_EQUAL_TO:
                case GREATER_THAN_OR_EQUAL_TO:
                case LESS_THAN:
                case GREATER_THAN:
                    return Boolean.class;
            }
            throw new RuntimeException(location() + ": Error: unknown operation " + operator.printName + " with types " + left.valueType + ", " + right.valueType);
        }

        if (left_type.equals(right_type)) {
            return left_type;
        }

        throw new RuntimeException(location() + ": Error: left and right types of operation do not match: " + left_type + " vs " + right_type + ".");
    }

    public void _serialize(StringBuilder sb) {
        left.serialize(sb);
        sb.append(" " + operator.printName + " ");
        right.serialize(sb);
    }

    public Object _evaluate(Object hint_value) {
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
            Object result = null;
            if (NodeNumber.isFloaty(left.valueType)) {
                switch (operator) {
                    case ADD -> { result = ln.doubleValue() + rn.doubleValue(); }
                    case SUB -> { result = ln.doubleValue() - rn.doubleValue(); }
                    case MUL -> { result = ln.doubleValue() * rn.doubleValue(); }
                    case DIV -> { result = ln.doubleValue() / rn.doubleValue(); }
                    
                    case COMPARE_EQUAL            -> { result = ln.doubleValue() == rn.doubleValue(); }
                    case LESS_THAN_OR_EQUAL_TO    -> { result = ln.doubleValue() <= rn.doubleValue(); }
                    case GREATER_THAN_OR_EQUAL_TO -> { result = ln.doubleValue() >= rn.doubleValue(); }
                    case LESS_THAN                -> { result = ln.doubleValue() <  rn.doubleValue(); }
                    case GREATER_THAN             -> { result = ln.doubleValue() >  rn.doubleValue(); }
                }
            } else {
                switch (operator) {
                    case ADD -> { result = ln.longValue() + rn.longValue(); }
                    case SUB -> { result = ln.longValue() - rn.longValue(); }
                    case MUL -> { result = ln.longValue() * rn.longValue(); }
                    case DIV -> { result = ln.longValue() / rn.longValue(); }
                    
                    case COMPARE_EQUAL            -> { result = ln.longValue() == rn.longValue(); }
                    case LESS_THAN_OR_EQUAL_TO    -> { result = ln.longValue() <= rn.longValue(); }
                    case GREATER_THAN_OR_EQUAL_TO -> { result = ln.longValue() >= rn.longValue(); }
                    case LESS_THAN                -> { result = ln.longValue() <  rn.longValue(); }
                    case GREATER_THAN             -> { result = ln.longValue() >  rn.longValue(); }
                }
            }
            if (result == null) {
                throw new RuntimeException(location() + ": Error: failed to execute operation " + operator.printName + " on number types " + left.valueType + ", " + right.valueType);
            }
            if (result instanceof Number number) {
                return NodeNumber.coerceNumber(valueType, number);
            } 
            return result;
        }

        throw new RuntimeException(location() + ": Error: tried to execute unknown operation " + operator.printName + " with types " + left.valueType + ", " + right.valueType);
    }
}