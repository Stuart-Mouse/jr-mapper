package jrmapper;

/*
    This is a relatively simple lexer implementation, somewhat complicated by Java because it's a bad language.
    Currently, there are only two real interface methods, getToken() and peekToken().
    Both of these methods return the same value, but getToken will consume the token and advance the lexer, while peekToken will not consume the token.
    If the semantics of the language remain relatively simple, there should be no need to keep a larger buffer of tokens or support multi-token lookahead.
*/

import java.util.ArrayList;

public class Lexer {
    public Lexer() { }
    public Lexer(String input) { init(input); }

    public void init(String input) {
        sourceText       = input;
        sourceTextCursor = 0;
        sourceLine       = 1;
        sourceColumn     = 1;
        nextToken        = lexNextToken(); // we need lex the first token on init in order for the lexer to actually pull a new token from the input text.
    }

    private int     sourceLine;
    private int     sourceColumn;
    private String  sourceText;
    private int     sourceTextCursor;
    private Token   nextToken;

    String location() {
        return "(" + sourceLine + ":" + sourceColumn + ")";
    }

    public Token expectToken(int token_type) {
        if (peekToken().type() == token_type) {
            return getToken();
        }
        return null;
    }

    public Token getToken() {
        if (nextToken == null) return new Token(Token.ERROR, 0, 0, "nextToken was null!");

        Token currentToken = nextToken;
        if (nextToken.type() != Token.ERROR) {
            nextToken = lexNextToken();
        }
        return currentToken;
    }

    public Token peekToken() {
        return nextToken;
    }

    private static final String singleCharTokens = "+-*/=<>(),:;?.[]{}|&~^$";
    private static Token makeSingleCharToken(char c, int line, int column) {
        return new Token((int)c, line, column, Character.toString(c));
    }

    private boolean isEOF() {
        return sourceTextCursor >= sourceText.length() || sourceText.charAt(sourceTextCursor) == '\0';
    }

    private static boolean isWhitespace(char c) {
        return " \n\t\r".indexOf(c) != -1;
    };

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    };

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z');
    };

    private static boolean beginsIdentifier(char c) {
        return isAlpha(c) || c == '_';
    }

    private static boolean continuesIdentifier(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // wraps indexing of sourceText string so that we can index from the sourceTextCursor position
    // return null on OOB index, since this should be checked anyhow using isEOF() when it actually matters
    private char nextChar(int offset) {
        if (sourceTextCursor + offset < 0 || sourceTextCursor + offset >= sourceText.length()) return '\0';
        return sourceText.charAt(sourceTextCursor + offset);
    }

    // advance sourceTextCursor and update source location accordingly
    // returns the new nextChar
    private char advance(int amount) {
        amount = Math.min(amount, sourceText.length());
        for (int i = 0; i < amount; i+=1) {
            if (nextChar(i) == '\n') {
                sourceLine  += 1;
                sourceColumn = 0;
            }
            sourceColumn += 1;
        }
        sourceTextCursor += amount;
        return nextChar(0);
    }


    // tokenize the entire input string
    public ArrayList<Token> getAllTokens() {
        // should we save and restore source cursor?
        sourceTextCursor = 0;

        var tokens = new ArrayList<Token>();

        while (true) {
            Token next_token = getToken();
            tokens.add(next_token);

            if (next_token.type() == Token.EOF) break;
            if (next_token.type() == Token.ERROR) {
                System.out.println("Lexer Error: " + next_token.text() + " Line: " + next_token.line() + " Col: " + next_token.column());
                break;
            }
        }
        return tokens;
    }

    private Token lexNextToken() {
        if (!skipWhitespaceAndComments()) {
            System.out.println("Error: Unexpected EOF in the middle of a comment.");
            return new Token(Token.ERROR, sourceLine, sourceColumn, "Unexpected EOF in the middle of a comment.");
        }
        // System.out.println("after skip whitespace: " + sourceTextCursor + ", '" + nextChar(0) + "'");

        // grab source location after skipping whitespace and comments
        int line   = sourceLine;
        int column = sourceColumn;

        if (isEOF()) return new Token(Token.EOF, line, column, "");

        char c  = nextChar(0);

        // 2-char tokens
        {
            char c0 = c;
            char c1 = nextChar(1);
            if (c0 == '<' && c1 == '=') return new Token(Token.LESS_THAN_OR_EQUAL_TO,    line, column, sourceText.substring(sourceTextCursor, sourceTextCursor+2));
            if (c0 == '>' && c1 == '=') return new Token(Token.GREATER_THAN_OR_EQUAL_TO, line, column, sourceText.substring(sourceTextCursor, sourceTextCursor+2));
            if (c0 == '=' && c1 == '=') return new Token(Token.DOUBLE_EQUAL,             line, column, sourceText.substring(sourceTextCursor, sourceTextCursor+2));
            if (c0 == '&' && c1 == '&') return new Token(Token.LOGICAL_AND,              line, column, sourceText.substring(sourceTextCursor, sourceTextCursor+2));
            if (c0 == '|' && c1 == '|') return new Token(Token.LOGICAL_OR,               line, column, sourceText.substring(sourceTextCursor, sourceTextCursor+2));
        }

        // single-char tokens
        if (singleCharTokens.indexOf(c) != -1) {
            advance(1);
            return makeSingleCharToken(c, line, column);
        }

        // parse an identifier
        if (beginsIdentifier(c)) {
            int start = sourceTextCursor;
            int end   = sourceTextCursor + 1;
            c = advance(1);

            while (continuesIdentifier(c)) {
                end += 1;
                c = advance(1);
            }

            // handle if identifier is actually a reserved word
            String identifier = sourceText.substring(start, end);
            switch (identifier) {
                case "var":    return new Token(Token.DECL_VAR,    line, column, identifier);
                case "input":  return new Token(Token.DECL_INPUT,  line, column, identifier);
                case "output": return new Token(Token.DECL_OUTPUT, line, column, identifier);
            }

            return new Token(Token.IDENTIFIER, line, column, identifier);
        }

        // parse a number
        if (isDigit(nextChar(0))) {
            int start = sourceTextCursor;
            int end   = sourceTextCursor + 1; // +1 since we've already consumed first char of number
            c = advance(1);

            boolean after_dot = false;
            while (!isEOF()) {
                if (!isDigit(c)) {
                    if (c != '.' || after_dot)  break;
                    // if (nextChar(1) == '.') break; // break on double dot, as this is spread operator
                    after_dot = true;
                }
                end += 1;
                c = advance(1);
            }

            return new Token(Token.NUMBER, line, column, sourceText.substring(start, end));
        }

        // parse a string
        if (c == '"' || c == '`') {
            char quote_char = c;

            c = advance(1);
            if (isEOF()) return new Token(Token.ERROR, line, column, "Unexpected EOF while parsing string.");

            int start = sourceTextCursor;
            int end   = sourceTextCursor;

            while (c != quote_char) {
                int advance_amount = 1 + (c == '\\' ? 1 : 0);
                end += advance_amount;
                c = advance(advance_amount);
                if (isEOF()) return new Token(Token.ERROR, line, column, "Unexpected EOF while parsing string.");
            }
            c = advance(1);

            var token_type = (quote_char == '`') ? Token.IDENTIFIER : Token.STRING;
            return new Token(token_type, line, column, sourceText.substring(start, end));
        }

        return new Token(Token.ERROR, line, column, "Unexpected character encountered.");
    }

    private boolean skipWhitespaceAndComments() {
        if (isEOF()) return true;

        // while (true) {
        while (isWhitespace(nextChar(0))) {
            advance(1);
            if (isEOF())  break;
        }
        // TODO: do comments later
        // }
        return true;
    }
}