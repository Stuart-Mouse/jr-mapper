/*
    NodeField represents a single-valued mapping, akin to a basic JSON field.
    Although the NodeField is technically also a NodeScope, this scope is not ever used.


*/
import java.util.EnumSet;
public class NodeField extends NodeMapping {
    public NodeField(NodeScope parent, Token token) {
        super(parent, token);
        value = token.text();
    }

    Node valueNode;

    public boolean typecheck(EnumSet<TypecheckingFlags> check_flags, Class hint_type) {
        // resolve the identifier within parent Mapping

        // typecheck the valueNode and match to the type of resolved identifier

        valueType = String.class;
        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        sb.append(name + ": ");
        valueNode.serialize(sb);
        return true;
    }

    public Object evaluate() {
        assert(flags.contains(Flags.TYPECHECKED));
        return value;
    }
}
