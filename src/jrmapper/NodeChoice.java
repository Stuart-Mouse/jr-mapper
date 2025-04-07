package jrmapper;

import java.util.ArrayList;

// TODO: could use a NodeScope here to allow placing variable declarations inside the choice block...
public class NodeChoice extends Node {
    public NodeChoice(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
        this.whenNodes = new ArrayList<>();
    }

    public ArrayList<Node> whenNodes;
    
    public Class<?> _typecheck(Class<?> hint_type) {
        if (whenNodes.isEmpty()) {
            throw new RuntimeException(location() + ": Error: An empty choice block is not permitted.");
        }
        
        for (var whenNode: whenNodes) {
            // if (whenNode.elseNode != null) {
            //     throw new RuntimeException(whenNode.elseNode.location() + ": Error: when statement cannot contain an else clause when used inside a choice block.");
            // }
            var vt = whenNode.typecheck(hint_type);
            if (valueType == null) {
                valueType = vt;
            } else if (!vt.equals(valueType)) {
                throw new RuntimeException(location() + ": Error: types of when expressions do not match. " + valueType + " vs " + vt);
            }
        }
        return valueType;
    }

    public void _serialize(StringBuilder sb) {
        sb.append("choose {\n");
        for (var whenNode: whenNodes) {
            whenNode.serialize(sb);
            sb.append("\n");
        }
        sb.append("}\n");
    }

    public Object _evaluate(Object hint_value) {
        Object result = null;
        for (var whenNode: whenNodes) {
            result = whenNode.evaluate(null);
            if (result != null) break;
        }
        return result;
    }

}
