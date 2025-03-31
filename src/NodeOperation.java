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

    Class _typecheck(Class hint_type) {
        // TODO: unary operations

        Class  left_type =  left.typecheck(hint_type);
        Class right_type = right.typecheck(hint_type);

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

        if (lv instanceof Number && rv instanceof Number) {
            switch (operator) {
                case ADD: return Double.valueOf( ((Number)lv).doubleValue() + ((Number)rv).doubleValue() );
                case SUB: return Double.valueOf( ((Number)lv).doubleValue() - ((Number)rv).doubleValue() );
                case MUL: return Double.valueOf( ((Number)lv).doubleValue() * ((Number)rv).doubleValue() );
                case DIV: return Double.valueOf( ((Number)lv).doubleValue() / ((Number)rv).doubleValue() );
            }
        }

        if (lv instanceof String && rv instanceof String) {
            switch (operator) {
                case ADD: return ((String)lv) + ((String)rv);
            }
        }

        // TODO: maybe just throw an error here for invalid operands?
        //       not sure what to return in this case.
        return Boolean.valueOf(false);
    }
}