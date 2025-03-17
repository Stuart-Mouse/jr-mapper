/*
    Abstract interface for all nodes which introduce a scope for declarations.
    Currently not a subclass of Node for the simple reason that we may want to have some language constructs which have scope but which are not Nodes.
    Not sure if that's really the right choice long term but we just gotta do something for now.
*/

import java.util.HashMap;

public class NodeScope extends Node {
    public NodeScope(NodeScope parent, Token token) { super(parent, token); }

    private HashMap<String, NodeDeclaration> declarations = new HashMap<String, NodeDeclaration>();

    // returns false if a declaration with the same name was already present in map
    public NodeDeclaration addDeclaration(NodeDeclaration decl) {
        return declarations.putIfAbsent(decl.name, decl);
    }
    public HashMap<String, NodeDeclaration> getDeclarations() {
        return declarations;
    }
    public NodeDeclaration resolveDeclaration(String identifier) {
        return declarations.get(identifier);
    }

    // dummy implementations for these methods until I figure out something better to do
    public boolean typecheck(Class hint_type) {
        flags.add(Flags.TYPECHECKED);
        return true;
    }
    public boolean serialize(StringBuilder sb) {
        return true;
    }
    public Object evaluate() {
        return null;
    }
}