/*
    Abstract interface for all nodes which introduce a scope for declarations.
    Currently not a subclass of Node for the simple reason that we may want to have some language constructs which have scope but which are not Nodes.
    Not sure if that's really the right choice long term but we just gotta do something for now.
*/

import java.util.ArrayList;
import java.util.EnumSet;

public class NodeScope extends Node {
    public NodeScope(NodeScope parent, Token token) { super(parent, token); }

    private ArrayList<NodeDeclaration> declarations = new ArrayList<NodeDeclaration>();
    
    public void addDeclaration(NodeDeclaration decl) {
        declarations.add(decl);
    }
    public ArrayList<NodeDeclaration> getDeclarations() {
        return declarations;
    }

    // dummy implementations for these methods until I figure out something better to do
    public boolean typecheck(EnumSet<TypecheckingFlags> check_flags, Class hint_type) {
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