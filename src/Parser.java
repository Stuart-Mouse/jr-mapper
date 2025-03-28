/*
    TODO:
        implement basic expression parsing for builtin types
        implement member access generically, then through some interface
        implement array subscripting, probably just a generalization on the above where the indexing expression is more dynamic

*/

import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    public Parser() { }

    public Lexer      lexer             = new Lexer();
    public NodeObject root;
    public NodeScope  currentScope;
    public MetaData   metaData;

    // Used during typechecking to detect dependency cycles between declarations and mappings.
    public Stack<Node> dependencyChain   = new Stack<Node>();

    public static class MetaData {
        public String  name;
        public String  extension;
        public int     id;
    };

    public boolean setVariable(String name, Object value, Class type) {
        var declaration = root.resolveDeclaration(name);
        if (declaration == null || declaration.declarationType != null) {
            System.out.println("Error: attempt to set variable '" + name + "' invalidly.");
            return false;
        }
        declaration.declarationType = NodeDeclaration.DeclarationType.EXTERNAL;
        declaration.valueType = type;
        declaration.value = value;
//        assert(value instanceof type); // any way to do this?
        return true;
    }

    public Node parseExpression(String expr) {
        lexer.init(expr);
        return parseExpression(0);
    }

    public NodeMapping getMetaNode() {
        for (var node: root.declarations) {
            if (node instanceof NodeMapping mapping) {
                if (mapping.name.equals("meta")) {
                    return mapping;
                }
            }
        }
        return null;
    }

    public Node parseFile(String input) {
        lexer.init(input);
        root = new NodeObject(null, null);
        currentScope = root;
        if (lexer.expectToken(Token.CLOSE_BRACE) == null) {
            root.declarations = parseDeclarations(Token.CLOSE_BRACE);
            if (root.declarations == null) {
                System.out.println("Error while trying to parse file.");
                return null;
            }
        }
        return root;
    }

    /*
        We parse in recursive-descent, using an algorithm that alternates between iteration and recursion to naturally handle operator precedence (thanks Jon).
        Parsing expressions iteratively naturally produces left-leaning trees, while parsing recursively produces left-leaning trees.
        By taking advantage of this fact, we can implement binary expressions with operator precedence trivially,
        since all we need to do is parse recursively while precedence is increasing and iteratively while it is decreasing or staying the same.
    */
    private Node parseExpression(int min_prec) {
        System.out.println("enter parseExpression");
        Node left = parseLeaf();
        if (left == null) {
            System.out.println("parseExpression returning null");
            return null;
        }

        while (true) {
            Node node = parseBinary(left, min_prec);
            if (node == null) {
                System.out.println("parseExpression returning null");
                return null;
            }
            if (node == left) break;
            left = node;
        }

        System.out.println("parseExpression returning left");
        return left;
    }

    // returns null on failure, empty list if no expressions parsed
    private ArrayList<Node> parseCommaSeparatedExpressions() {
        ArrayList<Node> exprs = new ArrayList<Node>();
        while (true) {
            var node = parseExpression(0);
            if (node == null) return null;
            exprs.add(node);

            var comma = lexer.peekToken();
            if (comma.type() != Token.COMMA) break;
            lexer.getToken(); // consume peeked comma
        }
        return exprs;
    }

    private Node parseBinary(Node left, int min_prec) {
        System.out.println("enter parseBinary");
        Token token = lexer.peekToken();

         switch (token.type()) {
             case Token.DOT: // binary dot, for member access of method calls
                 lexer.getToken(); // eat the dot
                 var dot = new NodeDot(currentScope, token);
                 dot.left = left;
                 var identifier_token = lexer.getToken();
                 if (identifier_token.type() != Token.IDENTIFIER) {
                     System.out.println(identifier_token.location() + ": Error: expected an identifier after dot.");
                     return null;
                 }
                 dot.right = new NodeIdentifier(currentScope, identifier_token);
                 return dot;

             //   case Token.OPEN_PAREN:
             //     // parse argument list and create procedure node
             //   case Token.OPEN_BRACKET:
             //     // array indexer
         }

        Operator op = Operator.fromToken(token, false);
        if (op != null && op.precedence > min_prec) {
            lexer.getToken(); // consume the peeked operator token
            var right = parseExpression(op.precedence);
            if (right == null) return null;

            var node = new NodeOperation(currentScope, token, op, left, right);
            // if (op.type == Operator.Type.ASSIGNMENT) {
            //     node.flags.add(Node.Flags.MUST_BE_STATEMENT_ROOT);
            // }
            System.out.println("parseBinary returning new NodeOperation");
            return node;
        }

        System.out.println("parseBinary returning left");
        return left;
    }

    private Node parseLeaf() {
        System.out.println("enter parseLeaf");
        Token token = lexer.getToken();
        System.out.println("token is " + token);

        Operator op = Operator.fromToken(token, true);
        if (op != null) {
            var node = parseExpression(op.precedence);
            if (node == null) return null;

            var unary = new NodeOperation(currentScope, token, op, node, null);
        }

        switch (token.type()) {
            case Token.IDENTIFIER: {
                return new NodeIdentifier(currentScope, token);
            }
            case Token.NUMBER: {
                return new NodeNumber(currentScope, token);
            }
            case Token.OPEN_PAREN: {
                var node = parseExpression(0); // reset min_prec since we are in parens
                if (node == null) return null;
                node.flags.add(Node.Flags.PARENTHESIZED);

                var close_paren = lexer.getToken();
                if (close_paren.type() != Token.CLOSE_PAREN) {
                    System.out.println("Error: expected closing paren at " + close_paren.location() + " to match open paren at " + token.location());
                    return null;
                }
                return node;
            }
            case Token.STRING: {
                return new NodeString(currentScope, token);
            }
//            case Token.DOT: {
//                return new NodeDot();
//            }
            case Token.OPEN_BRACE: {
                var node = new NodeObject(currentScope, token);
                currentScope = node;
                if (lexer.expectToken(Token.CLOSE_BRACE) == null) {
                    // TODO: if we allow variable declarations in objects, we should probably inline parseDeclarations here so that we can put variable decls and mapping fields into separate arrays.
                    node.declarations = parseDeclarations(Token.CLOSE_BRACE);
                    if (node.declarations == null) {
                        System.out.println("Error while trying to parse inner declarations of NodeObject.");
                        currentScope = node.parentScope;
                        return null;
                    }
                }
                currentScope = node.parentScope;
                return node;
            }
            case Token.OPEN_BRACKET: {
                var node = new NodeArray(currentScope, token);
                currentScope = node;
                if (lexer.expectToken(Token.CLOSE_BRACKET) != null) {
                    node.valueNodes = parseCommaSeparatedExpressions();
                    if (node.valueNodes == null) {
                        System.out.println("Error while trying to parse inner declarations of NodeObject.");
                        currentScope = node.parentScope;
                        return null;
                    }
                    var close_bracket = lexer.getToken();
                    if (close_bracket.type() != Token.CLOSE_BRACKET) {
                        System.out.println("Error: expected closing bracket at " + close_bracket.location() + " to end array at " + node.location());
                        currentScope = node.parentScope;
                        return null;
                    }
                }
                currentScope = node.parentScope;
                return node;
            }
        }

        System.out.println("parseLeaf returning null");
        return null;
    }

    public NodeDeclaration parseDeclaration(String expr) {
        lexer.init(expr);
        return parseDeclaration();
    }

    private NodeDeclaration parseDeclaration() {
        // declaration must begin with some type identifier  or 'var'
        // OR, the declaration is a NodeMapping, in which case the first token should be an identifier that resolves to something in the current output object(s) scope
        // so I guess we just do parseExpression here and the first thing better be some keyword or identifier
        NodeDeclaration node;

        Token token = lexer.getToken();
        switch (token.type()) {
            case Token.DECL_VAR:
                var var_token = token;

                token = lexer.getToken();
                if (token.type() != Token.IDENTIFIER) {
                    System.out.println("Error: expected identifier after keyword 'var' in variable declaration at " + token.location() + ".");
                    return null;
                }
                Token identifier = token;

                token = lexer.getToken();
                if (token.type() != Token.COLON) {
                    System.out.println("Error: expected colon after identifier in variable declaration at " + token.location() + ".");
                    return null;
                }

                var declaration = new NodeDeclaration(currentScope, var_token, identifier.text());
                declaration.declarationType = NodeDeclaration.DeclarationType.INTERNAL;

                var other = currentScope.addDeclaration(declaration);
                if (other != null) {
                    System.out.println(identifier.location() + ": Error: redeclaration of '" + identifier.text() + "'. Previously declared at " + other.location() + ".");
                    return null;
                }

                declaration.valueNode = parseExpression(0);
                if (declaration.valueNode == null) {
                    System.out.println("Error: failed while trying to parse value expression in variable declaration at " + token.location() + ".");
                    return null;
                }

                node = declaration;
                break;

            case Token.IDENTIFIER:
                identifier = token;

                // NOTE: later we may allow more complex syntax here before then colon
                //       for example, range-based initialization on array types
                token = lexer.getToken();
                if (token.type() != Token.COLON) {
                    System.out.println("Error: expected colon after identifier in mapping declaration at " + token.location() + ".");
                    return null;
                }

                NodeMapping mapping = new NodeMapping(currentScope, token, identifier.text());

                mapping.valueNode = parseExpression(0);
                if (mapping.valueNode == null) {
                    System.out.println("Error: failed while trying to parse value expression of mapping at " + token.location() + ".");
                    return null;
                }

                node = mapping;
                break;

            default:
                System.out.println(token.location() + ": Error: Unexpected token '" + token.text() + "'.");
                return null;
        }

        // comma after declaration value expression is optional if the expression was an object or array
        // otherwise, we need to see either the comma or end of scope
        if (node.valueNode instanceof NodeObject || node.valueNode instanceof NodeArray) {
            lexer.expectToken(Token.COMMA);
        } else {
            var next_token = lexer.peekToken();
            switch (next_token.type()) {
                case Token.CLOSE_BRACE, Token.CLOSE_BRACKET: break;
                case Token.COMMA: lexer.getToken(); break;
                default:
                    System.out.println(next_token.location() + ": Error: unexpected token '" + next_token.text() + "'. Expected a comma or end of scope to terminate expression.");
                    return null;
            }
        }
        return node;
    }

    private ArrayList<NodeDeclaration> parseDeclarations(int break_token_type) {
        var declarations = new ArrayList<NodeDeclaration>();
        while (true) {
            if (lexer.expectToken(break_token_type) != null) break;
            if (lexer.expectToken(Token.EOF) != null) break;
            var decl = parseDeclaration();
            if (decl == null) return null;
            declarations.add(decl);
        }
        return declarations;
    }

    public boolean typecheck() {
        for (var field_node: root.declarations) {
            if (!field_node.typecheck(null)) return false;
        }
        return true;
    }

    public boolean evaluate() {
        for (var field_node: root.declarations) {
            if (field_node.evaluate(null) == null) return false;
        }
        return true;
    }
}