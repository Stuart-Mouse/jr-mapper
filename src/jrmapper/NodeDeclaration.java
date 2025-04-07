package jrmapper;
/*
    TODO: we can probably make subclasses for each declaration type instead of using an enum, 
          but this was faster to implement for the mean time.
*/

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class NodeDeclaration extends Node {
    public NodeDeclaration(Parser owningParser, NodeScope parentScope, Token token, String name, DeclarationType declarationType) {
        super(owningParser, parentScope, token);
        this.name = name;
        this.declarationType = declarationType;
    }
    
    public String name;
    public Node   valueNode;
    public Object value;
    
    // constructorNode can be either a simple type expression, or an actual constructor call
    public Node           constructorNode;
    public Constructor<?> resolvedConstructor;
    
    // only used if the node is a field within an enclosing object
    public Field resolvedField;
    
    public DeclarationType declarationType;
    public enum DeclarationType { FIELD, VARIABLE, INPUT, OUTPUT };
    
    public Class<?> _typecheck(Class<?> hint_type) {
        valueType = hint_type;
        if (constructorNode != null) {
            valueType = constructorNode.typecheck(null);
            hint_type = valueType;
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

    public void _serialize(StringBuilder sb) {
        switch (declarationType) {
            case DeclarationType.VARIABLE:  sb.append("var ");    break;
            case DeclarationType.INPUT:     sb.append("input ");  break;
            case DeclarationType.OUTPUT:    sb.append("output "); break;
        }
        sb.append(name);
        if (constructorNode != null) {
            sb.append(": ");
            constructorNode.serialize(sb);
            sb.append(" = ");
        } else {
            sb.append(" := ");
        }
        valueNode.serialize(sb);
    }

    // NOTE: no longer using hint_value here. Will have to check other uses cases before removing parameter entirely.
    public Object _evaluate(Object hint_value) {
        if (!isEvaluated()) {
            if (constructorNode != null) {
                value = constructorNode.evaluate(null);
            }
            value = valueNode.evaluate(value);
        }
        return value;
    }
}
