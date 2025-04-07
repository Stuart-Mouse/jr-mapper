package jrmapper;
/*
    Abstract interface for all nodes which introduce a scope for declarations.
    Currently not a subclass of Node for the simple reason that we may want to have some language constructs which have scope but which are not Nodes.
    Not sure if that's really the right choice long term but we just gotta do something for now.
*/

import java.util.ArrayList;

public abstract class NodeScope extends Node {
    public NodeScope(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
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
        if (parentScope != null) {
            return parentScope.resolveDeclaration(identifier);
        }
        return null;
    }
}