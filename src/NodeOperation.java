import java.util.EnumSet;

public class NodeOperation extends Node {
    public NodeOperation(NodeScope parent, Token token, Operator operator, Node left, Node right) {
        super(parent, token);
        this.operator = operator;
        this.left     = left;
        this.right    = right;
    }

    Operator  operator;
    Node      left;
    Node      right;

    public boolean typecheck(Class hint_type) {
        // TODO: unary operations

        // NOTE: commented out the below because realistically we will always have a type hint provided.
//        Class  left_type =  left.getValueType();
//        Class right_type = right.getValueType();
//
//        if ( left_type != null && Number.class.isAssignableFrom( left_type)
//         && right_type != null && Number.class.isAssignableFrom(right_type)) {
//            // TODO: pre-emptively get wider of two Number types and set as hint_type in below calls to typecheck()
//        }

        if (! left.typecheck(hint_type)) {
            System.out.println(left.location() + ": Error: failed to typecheck left side of binary operation.");
            return false;
        }
        if (!right.typecheck(hint_type)) {
            System.out.println(right.location() + ": Error: failed to typecheck right side of binary operation.");
            return false;
        }

        Class  left_type =  left.getValueType();
        Class right_type = right.getValueType();

        if (left_type != right_type) {
            // TODO: if left and right types don't match, we can try to coerce to the wider of the two types.
            System.out.println("Error: left and right types of operation do not match: " + left_type + " vs " + right_type + ".");
            return false;
        }

        // TODO: verify that we can always assume that an operation will have the same type as its operands.
        valueType = left_type;

        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        if (flags.contains(Flags.PARENTHESIZED)) sb.append("(");
        left.serialize(sb);
        sb.append(" " + operator.printName + " ");
        right.serialize(sb);
        if (flags.contains(Flags.PARENTHESIZED)) sb.append(")");
        return true;
    }

    public Object evaluate(Object hint_value) {
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