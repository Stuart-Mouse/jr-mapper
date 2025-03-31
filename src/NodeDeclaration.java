/*
    TODO: we can probably make subclasses for each declaration type instead of using an enum, but this was faster to implement for the mean time.
*/

import java.lang.reflect.Field;

public class NodeDeclaration extends Node {
    NodeDeclaration(Parser owningParser, NodeScope parentScope, Token token, String name, DeclarationType declarationType) {
        super(owningParser, parentScope, token);
        this.name = name;
        this.declarationType = declarationType;
    }
    
    String name;
    Node   valueNode;
    Object value;
    
    // only used if the node is a field within an enclosing object
    Field resolvedField;
    
    DeclarationType declarationType;
    public enum DeclarationType { FIELD, VARIABLE, INPUT, OUTPUT };

    Class _typecheck(Class hint_type) {
        // For now, we assume that our parsing is correct and all INPUT and OUTPUT declarations will have their valueType pre-set.
        // For VARIABLE declarations, the valueType is inferred from the valueNode.
        switch (declarationType) {
            case DeclarationType.INPUT:
            case DeclarationType.OUTPUT:
                hint_type = valueType;
                break;

            case DeclarationType.FIELD:
                valueType = hint_type;
                break;
        }
        var received_type = valueNode.typecheck(hint_type);

        // NOTE: valueType will only be null here in the VARIABLE case
        if (valueType == null) {
            return received_type;
        }
        if (!isAssignableFrom(received_type)) {
            throw new RuntimeException("Error: mismatched types in declaration. Type " + valueType + " is not assignable from " + valueNode.valueType);
        }
        return valueType;
    }
    
    void _serialize(StringBuilder sb) {
        switch (declarationType) {
            case DeclarationType.VARIABLE:  sb.append("var ");    break;
            case DeclarationType.INPUT:     sb.append("input ");  break;
            case DeclarationType.OUTPUT:    sb.append("output "); break;
        }
        sb.append(name).append(": ");
        valueNode.serialize(sb);
    }
    
    Object _evaluate(Object hint_value) {
        if (!isEvaluated()) {
            switch (declarationType) {
                case DeclarationType.INPUT:
                case DeclarationType.OUTPUT:
                    hint_value = value;
                    break;
            }
            value = valueNode.evaluate(hint_value);
            setEvaluated();
        }
        return value;
    }
}
