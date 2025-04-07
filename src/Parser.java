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

    public Lexer      lexer = new Lexer();
    public NodeObject root;
    public NodeScope  currentScope;

    // Used during typechecking to detect dependency cycles between declarations and mappings.
    // When declaration nodes are popped from the stack, they'll be pushed onto the end of the evaluationBuffer. 
    // By this method, we end up with a valid evalaution order that respects dependencies between nodes.
    public Stack<Node> typecheckingStack = new Stack<>();
    public ArrayList<NodeDeclaration> evaluationBuffer = new ArrayList<>();

    public boolean setVariable(String name, Object value, Class<?> type) {
        var declaration = root.resolveDeclaration(name);
        if (declaration == null || declaration.declarationType == NodeDeclaration.DeclarationType.VARIABLE) {
            System.out.println("Error: attempt to set variable '" + name + "' invalidly.");
            return false;
        }
        declaration.valueType = type;
        declaration.value     = value;
        return true;
    }

    public Node parseExpression(String expr) {
        lexer.init(expr);
        return parseExpression(0);
    }

    public Node parseFile(String input) {
        lexer.init(input);
        root = new NodeObject(this, null, null);
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
                 var dot = new NodeDot(this, currentScope, token);
                 dot.left = left;
                 dot.right = parseExpression(999);
                 if (dot.right == null) {
                     System.out.println(dot.location() + "Error: unable to parse expression on right-hand side of dot.");
                     return null;
                 }
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

            var node = new NodeOperation(this, currentScope, token, op, left, right);
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

            var unary = new NodeOperation(this, currentScope, token, op, node, null);
        }

        switch (token.type()) {
            case Token.IDENTIFIER -> {
                var identifier = new NodeIdentifier(this, currentScope, token);

                // NOTE: note sure if this is the best place for parsing method calls...
                Token open_paren = lexer.expectToken(Token.OPEN_PAREN);
                if (open_paren == null) {
                    return identifier;
                } else {
                    var method_call = new NodeMethodCall(this, currentScope, token);
                    method_call.identifier = identifier;

                    // we have to check if close paren is directly after open paren, otherwise parseCommaSeparatedExpressions will be sad
                    Token close_paren = lexer.expectToken(Token.CLOSE_PAREN);
                    if (close_paren == null) {
                        method_call.specifiedParameters = parseCommaSeparatedExpressions();
                        if (method_call.specifiedParameters == null) {
                            System.out.println(method_call.location() + ": Error: unable to parse parameter expressions in method call.");
                            return null;
                        }

                        close_paren = lexer.expectToken(Token.CLOSE_PAREN);
                        if (close_paren == null) {
                            System.out.println(lexer.location() + ": Error: expected close parenthesis after method call parameter list, to match open parenthesis at " + open_paren.location() + ".");
                            return null;
                        }
                    } else {
                        method_call.specifiedParameters = new ArrayList<>();
                    }

                    return method_call;
                }
            }
            case Token.NUMBER -> {
                return new NodeNumber(this, currentScope, token);
            }
            case Token.OPEN_PAREN -> {
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
            case Token.STRING -> {
                return new NodeString(this, currentScope, token);
            }
            case Token.OPEN_BRACE -> {
                var node = new NodeObject(this, currentScope, token);
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
            // case Token.OPEN_BRACKET -> {
            //     var node = new NodeArray(currentScope, token);
            //     currentScope = node;
            //     if (lexer.expectToken(Token.CLOSE_BRACKET) != null) {
            //         node.valueNodes = parseCommaSeparatedExpressions();
            //         if (node.valueNodes == null) {
            //             System.out.println("Error while trying to parse inner declarations of NodeArray.");
            //             currentScope = node.parentScope;
            //             return null;
            //         }
            //         var close_bracket = lexer.getToken();
            //         if (close_bracket.type() != Token.CLOSE_BRACKET) {
            //             System.out.println("Error: expected closing bracket at " + close_bracket.location() + " to end array at " + node.location());
            //             currentScope = node.parentScope;
            //             return null;
            //         }
            //     }
            //     currentScope = node.parentScope;
            //     return node;
            // }
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
        NodeDeclaration.DeclarationType decl_type = null;
        
        Token keyword = lexer.peekToken();
        switch (keyword.type()) {
            case Token.DECL_VAR:
                decl_type = NodeDeclaration.DeclarationType.VARIABLE;
                lexer.getToken();
                break;

            case Token.DECL_INPUT:
                decl_type = NodeDeclaration.DeclarationType.INPUT;
                lexer.getToken();
                break;

            case Token.DECL_OUTPUT:
                decl_type = NodeDeclaration.DeclarationType.OUTPUT;
                lexer.getToken();
                break;

            case Token.IDENTIFIER:
                decl_type = NodeDeclaration.DeclarationType.FIELD;
                break;
                
            default:
                System.out.println(keyword.location() + ": Error: Unexpected token '" + keyword.text() + "'.");
                return null;
        }
        
        Token identifier = lexer.getToken();
        if (identifier.type() != Token.IDENTIFIER) {
            System.out.println("Error: expected identifier after keyword '" + keyword.text() + "' in declaration at " + keyword.location() + ".");
            return null;
        }

        var declaration = new NodeDeclaration(this, currentScope, keyword, identifier.text(), decl_type);

        var other = currentScope.addDeclaration(declaration);
        if (other != null) {
            System.out.println(identifier.location() + ": Error: redeclaration of '" + identifier.text() + "'. Previously declared at " + other.location() + ".");
            return null;
        }

        // early return on INPUT declaration since these do not accept a value expression
        if (declaration.declarationType == NodeDeclaration.DeclarationType.INPUT) {
            if (lexer.expectToken(Token.COMMA) == null) {
                System.out.println(declaration.location() + ": Error: expected comma after input declaration. (Input declarations do not accept a value expression, as they receive their values externally and are not modifiable.)");
                return null;
            }
            return declaration;
        }

        Token colon = lexer.getToken();
        if (colon.type() != Token.COLON) {
            System.out.println("Error: expected colon after identifier in variable declaration at " + colon.location() + ".");
            return null;
        }

        if (lexer.peekToken().type() != Token.EQUALS) {
            declaration.constructorNode = parseExpression(0);
        }

        Token equal_sign = lexer.getToken();
        if (equal_sign.type() != Token.EQUALS) {
            System.out.println("Error: expected equal sign after colon in variable declaration at " + equal_sign.location() + ".");
            return null;
        }
        
        declaration.valueNode = parseExpression(0);
        if (declaration.valueNode == null) {
            System.out.println("Error: failed while trying to parse value expression in declaration at " + declaration.location() + ".");
            return null;
        }
        
        // comma after declaration value expression is optional if the expression was an object or array
        // otherwise, we need to see either the comma or end of scope
        if (declaration.valueNode instanceof NodeObject) {
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

        return declaration;
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
            field_node.typecheck(null);
        }
        return true;
    }

    public boolean evaluate() {
        for (var decl: evaluationBuffer) {
            decl.evaluate(null);
        }
        return true;
    }
}