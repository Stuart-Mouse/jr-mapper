/*
    Abstract interface for all nodes which introduce a scope for declarations.
    Currently not a subclass of Node for the simple reason that we may want to have some language constructs which have scope but which are not Nodes.
    Not sure if that's really the right choice long term but we just gotta do something for now.
*/

import java.util.ArrayList;

public class NodeScope extends Node {
    public NodeScope(NodeScope parent, Token token) {
        super(parent, token);
    }

    public ArrayList<NodeDeclaration> declarations = new ArrayList<NodeDeclaration>();

    // returns existing declaration if identifier is already declared in this scope
    public NodeDeclaration addDeclaration(NodeDeclaration decl) {
        var other = resolveDeclaration(decl.name);
        if (other != null) return other;
        declarations.add(decl);
        return null;
    }
    public NodeDeclaration resolveDeclaration(String identifier) {
        for (var decl: declarations) {
            if (decl.name.equals(identifier)) {
                return decl;
            }
        }
        return null;
    }

    // dummy implementations for these methods until I figure out something better to do
    public boolean typecheck(Class hint_type) {
        flags.add(Flags.TYPECHECKED);
        return true;
    }
    public boolean serialize(StringBuilder sb) {
        return true;
    }
    public Object evaluate(Object hint_value) {
        return null;
    }
}