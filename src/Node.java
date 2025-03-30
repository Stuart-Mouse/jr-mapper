/*
    All node types must implement the basic methods:
        typecheck
        serialize
        evaluate

    We will probably remove evaluate or stub it out for the more complex node types that will come later,
    since I don't currently know of a way to do dyncall-like dynamic function calls in Java.
        Nevermind, seems like it should be relatively trivial:
            https://stackoverflow.com/questions/3050967/java-dynamic-function-calling
    But, for the time being, it is useful to have an evaluate method on nodes so that we can test the basics in the repl.

    Serialization should work without nodes needing to be typechecked.
    The entire AST as produced by the Parser is initially untyped, and then all types are resolved in a second pass.

    Evaluation will require that nodes be typechecked first, otherwise we will assert.

    Typechecking procedures should only return false if they individually failed to typecheck properly, that is, if the node's valueType was not set.
    If typecheck() returns true, it is understood that the node has a valid valueType and has set its .TYPECHECKED flag.
    In general, it is fine for a node not to match the provided hint_type, and then it is up the caller to determine if the actual valueType on the node is acceptable.


    I will probably later also add a method for declaration resolution on this base Node type,
        so that we can always recurse up any node type.
    Then again, this may not play nice when we want to check scopes, so perhaps we will have a NodeScope for that purpose,
        and parent will always have to point to a NodeScope.
*/


import java.util.EnumSet;

public abstract class Node {
    Node(NodeScope parentScope, Token token) {
        this.parentScope = parentScope;
        if (token != null) {
            this.line = token.line();
            this.column = token.column();
        }
    }

    NodeScope       parentScope;
    EnumSet<Flags>  flags = EnumSet.noneOf(Flags.class);
    Class           valueType;

    // source location info, copied from token in constructor
    int line;
    int column;

    String location() {
        return "(" + line + ":" + column + ")";
    }

    enum Flags {
        TYPECHECKED,
        EVALUATED,
        PARENTHESIZED;

        public static final EnumSet<Flags> ALL = EnumSet.allOf(Flags.class);
    }

    Class getValueType() { return valueType; };
    Object getValue() { return null; };

    abstract boolean typecheck(Class hint_type);
    abstract boolean serialize(StringBuilder sb);
    abstract Object  evaluate(Object hint_value);
}