/*
    TODO:
        implement basic expression parsing for builtin types
        implement member access generically, then through some interface
        implement array subscripting, probably just a generalization on the above where the indexing expression is more dynamic

*/

import java.util.ArrayList;

public class Parser {
    public Parser() { }

    public Lexer      lexer = new Lexer();
    public Node       root;             // TODO: we should set this on init as a block of some sort, and also set it as currentScope
    public NodeScope  globalScope;
    public NodeScope  currentScope;     // TODO: maybe this should be passed as param instead?

    public Node parseExpression(String expr) {
        lexer.init(expr);
        return parseExpression(0);
    }

    // Parse in recursive-descent, using an algorithm that alternates between iteration and recursion to naturally handle operator precedence (thanks Jon).
    // Parsing expressions iteratively naturally produces left-leaning trees, while parsing recursively produces left-leaning trees.
    // By taking advantage of this fact, we can implement binary expressions with operator precedence trivially,
    // since all we need to do is parse recursively while precedence is increasing and iteratively while it is decreasing or staying the same.
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

    private ArrayList<Node> parseCommaSeparatedExpressions() {
        ArrayList<Node> exprs = new ArrayList<Node>();

        Node node;
        while (true) {
            node = parseExpression(0);
            if (node == null) break;
            exprs.add(node);
        }

        return exprs;
    }

    private Node parseBinary(Node left, int min_prec) {
        System.out.println("enter parseBinary");
        Token token = lexer.peekToken();

        // switch (token.type) {
        //   case Token.OPEN_PAREN:
        //     // parse argument list and create procedure node
        //   case Token.DOT:
        //     // binary dot, probably only to be used for member access in this language
        //   case Token.OPEN_BRACKET:
        //     // array indexer
        // }

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
            case Token.IDENTIFIER:
                return new NodeIdentifier(currentScope, token);

            case Token.NUMBER:
                return new NodeNumber(currentScope, token);

            case Token.OPEN_PAREN:
                var node = parseExpression(0); // reset min_prec since we are in parens
                if (node == null) return null;
                node.flags.add(Node.Flags.PARENTHESIZED);

                var close_paren = lexer.getToken();
                if (close_paren == null || close_paren.type() != Token.CLOSE_PAREN) {
                    // log Expected closing paren 
                    return null;
                }
                return node;

            case Token.STRING:
                return new NodeString(currentScope, token);

            // case Token.DOT:
            //   return new NodeDot();
        }

        System.out.println("parseLeaf returning null");
        return null;
    }

    public Node parseDeclaration(String expr) {
        lexer.init(expr);
        return parseDeclaration();
    }

    private Node parseDeclaration() {
        // declaration must begin with some type identifier  or 'var'
        // OR, the declaration is a NodeMapping, in which case the first token should be an identifier that resolves to something in the current output object(s) scope
        // so I guess we just do parseExpression here and the first thing better be some keyword or identifier
        Token token = lexer.getToken();
        switch (token.type()) {
            case Token.DECL_VAR:
                var var_token = token; // saved so that we have this location to initialize NodeMapping with

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

                var declaration = new NodeDeclaration(currentScope, token, identifier.text());

                declaration.initExpression = parseExpression(0);
                if (declaration.initExpression == null) {
                    System.out.println("Error: failed while trying to parse init expression in variable declaration at " + token.location() + ".");
                    return null;
                }

                return declaration;

            case Token.IDENTIFIER:
                identifier = token;

                token = lexer.getToken();
                if (token.type() != Token.COLON) {
                    System.out.println("Error: expected colon after identifier in Mapping declaration at " + token.location() + ".");
                    return null;
                }

                NodeMapping mapping = new NodeMapping(currentScope, token, identifier.text());
                // then assert next token is a colon
                //      note that later we may allow more complex syntax here, for example, for range-based initialization on array types
                // then like above we call parseexpression,

        }


        return null;
    }
}