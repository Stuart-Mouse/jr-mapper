package jrmapper;

public class NodeWhen extends Node {
    public NodeWhen(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
    }

    public Node conditionNode;
    public Node valueNode;
    // public Node elseNode; // cannot be used inside of choice block
    
    // when node primarily expects a boolean expression, but if another object type is returned, we do a simple "not-null" test
    public Class<?> _typecheck(Class<?> hint_type) {
        // We don't actually need to check this value type, since any type is valid here.
        conditionNode.typecheck(null);
        valueType = valueNode.typecheck(null);
        // if (elseNode != null) {
        //     var else_type = elseNode.typecheck(null);
        //     if (!else_type.equals(valueType)) {
        //         throw new RuntimeException(location() + ": Error: types of value expression and else expression do not match. " + valueType + " vs " + else_type);
        //     }
        // }
        return valueType;
    }
    
    public void _serialize(StringBuilder sb) {
        sb.append("when ");
        conditionNode.serialize(sb);
        sb.append(": ");
        valueNode.serialize(sb);
        // if (elseNode != null) {
        //     sb.append(" else ");
        //     elseNode.serialize(sb);
        // }
    }
    
    public Object _evaluate(Object hint_value) {
        boolean condition_result;
        if (conditionNode.valueType.equals(Boolean.class) || conditionNode.valueType.equals(boolean.class)) {
            condition_result = ((Boolean)conditionNode.evaluate(null)).booleanValue();
        } else {
            condition_result = conditionNode.evaluate(null) != null;
        }
        if (condition_result) {
            return valueNode.evaluate(null);
        }
        // else if (elseNode != null) {
        //     return elseNode.evaluate();
        // }
        return null;
    }
}
