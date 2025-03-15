/*
    NodeMapping is our basic DOM-style node for representing data fields in the mapper file.
    It is much like a JSON Node in that it's just a simple key/value pair.
    The value can be a general single-valued expression, or some aggregate type like a NodeObject or NodeArray.
*/

public class NodeMapping extends NodeScope {
    public NodeMapping(NodeScope parent, Token token, String name) {
        super(parent, token);
        this.name = name;
    }

    public String name;
    public Node   valueNode;
    public Object value;
}
