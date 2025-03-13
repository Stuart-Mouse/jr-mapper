/*
    NodeMapping is our basic DOM-style node for representing data fields in the mapper file.
    It is extended by NodeField, NodeObject, and NodeArray.
    Every NodeMapping also serves as a scope, although this scope does not really mean much in the case of the NodeField.
*/

public abstract class NodeMapping extends NodeScope {
    public NodeMapping(NodeScope parent, Token token) {
        super(parent, token);
    }

    public String name;
    public Object value;
}
