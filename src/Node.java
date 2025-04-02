/*
    Serialization should work without nodes needing to be typechecked.
    The entire AST as produced by the Parser is initially untyped, and then all types are resolved in a second pass.

    Evaluation will require that nodes be typechecked first, otherwise we will assert.

    Typechecking procedures should only return false if they individually failed to typecheck properly, that is, if the node's valueType was not set.
    If typecheck() returns true, it is understood that the node has a valid valueType and has set its .TYPECHECKED flag.
    In general, it is fine for a node not to match the provided hint_type, and then it is up the caller to determine if the actual valueType on the node is acceptable.
*/


import java.util.EnumSet;

public abstract class Node {
    Node(Parser owningParser, NodeScope parentScope, Token token) {
        this.owningParser = owningParser;
        this.parentScope  = parentScope;
        if (token != null) {
            this.line = token.line();
            this.column = token.column();
        }
    }

    Parser          owningParser;
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
    
    boolean isTypechecked()   { return flags.contains(Flags.TYPECHECKED); }
    boolean isParenthesized() { return flags.contains(Flags.PARENTHESIZED); }
    boolean isEvaluated()     { return flags.contains(Flags.EVALUATED); }

    boolean setTypechecked()   { return flags.add(Flags.TYPECHECKED); }
    boolean setParenthesized() { return flags.add(Flags.PARENTHESIZED); }
    boolean setEvaluated()     { return flags.add(Flags.EVALUATED); }

    final void pushDependency() {
        var stack = owningParser.typecheckingStack;
        if (stack.contains(this)) {
            var sb = new StringBuilder();
            sb.append("Circular dependency detected while typechecking:\n");
            for (var node: stack) {
                sb.append("\t").append(node.location()).append(" ");
                if (node instanceof NodeDeclaration decl) {
                    sb.append("DECL: ").append(decl.name);
                } else {
                    node.serialize(sb);
                }
                sb.append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
        stack.push(this);
    }
    
    final void popDependency() {
        // This assert makes sure that we don't forget to call pop for each matching push.
        var last_pushed = owningParser.typecheckingStack.pop();
        assert(last_pushed == this);
        if (this instanceof NodeDeclaration declaration) {
            owningParser.evaluationBuffer.add(declaration);
        }
    }
    
    // implemented for the sake of method parameter typechecking, where we may fail due to a missing type hint which can be provided in the second pass. 
    final Class<?> tryTypecheck(Class<?> hint_type) {
        try { return typecheck(hint_type); } catch(Exception e) { return null; }
    }
    
    final Class<?> typecheck(Class<?> hint_type) {
        if (isTypechecked()) {
            if (hint_type != null && hint_type != valueType) {
                System.out.println(location() + ": Info: typecheck() called with new type hint on node previously typechecked. Attempting to re-evaluating type...");
                flags.remove(Flags.TYPECHECKED); // manually remove so that we don't trip the dependency cycle detection.
            } else {
                System.out.println(location() + ": Info: typecheck() called on node previously typechecked. Returning result of previous typecheck.");
                return valueType;
            }
        }
        pushDependency();
        try {
            // TODO: is there really no way to defer the pop so that we don't have to duplicate to all control paths?
            valueType = _typecheck(hint_type);
            if (valueType == null) {
                throw new RuntimeException(location() + ": Error: typecheck() returned null.");
            }
        } finally {
            popDependency();
            setTypechecked(); // set typechecked flag whether or not we succeed. That way, we at least detect re-typechecking as above.
        }
        return valueType;
    }
    
    final void serialize(StringBuilder sb) {
        if (isParenthesized()) sb.append("(");
        _serialize(sb);
        if (isParenthesized()) sb.append(")");
    }
    
    final Object evaluate(Object hint_value) {
        assert(isTypechecked());
        var result = _evaluate(hint_value);
        setEvaluated();
        return result;
    }

    boolean isAssignableFrom(Class<?> type) {
        return valueType.isAssignableFrom(type) || NodeNumber.areMatchingTypes(valueType, type);
    }
    
    // specific case must be implemented for each subclass
    abstract Class<?> _typecheck(Class<?> hint_type);
    abstract void     _serialize(StringBuilder sb);
    abstract Object   _evaluate(Object hint_value);
    
    // overload provided for convenience when serializing root node of some expression
    public final String toString() {
        var sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }
}