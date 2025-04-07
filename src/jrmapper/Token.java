package jrmapper;

public record Token(int type, int line, int column, String text) {
    public String location() {
        return "(" + line + ":" + column + ")";
    }

    // Token types are all just ints for now because java enums are retarded
    // The single-character tokens just use their actual char value, and the multi-character tokens begin at 256 and go up from there.
    public static final int EOF = '\0';

    // single-character tokens
    public static final int PLUS           = '+';
    public static final int MINUS          = '-';
    public static final int STAR           = '*';
    public static final int SLASH          = '/';
    public static final int EQUALS         = '=';
    public static final int LESS_THAN      = '<';
    public static final int GREATER_THAN   = '>';
    public static final int OPEN_PAREN     = '(';
    public static final int CLOSE_PAREN    = ')';
    public static final int COMMA          = ',';
    public static final int COLON          = ':';
    public static final int SEMICOLON      = ';';
    public static final int QUESTION_MARK  = '?';
    public static final int DOT            = '.';
    public static final int OPEN_BRACE     = '{';
    public static final int CLOSE_BRACE    = '}';
    public static final int OPEN_BRACKET   = '[';
    public static final int CLOSE_BRACKET  = ']';
    public static final int BITWISE_OR     = '|';
    public static final int BITWISE_AND    = '&';
    public static final int BITWISE_NOT    = '~';
    public static final int CARAT          = '^'; // BITWISE_XOR
    public static final int DOLLAR         = '$';

    // general tokens
    public static final int ERROR      = 256;
    public static final int IDENTIFIER = 256 + 1;
    public static final int STRING     = 256 + 2;
    public static final int NUMBER     = 256 + 3;

    // 2-character tokens
    public static final int DOUBLE_EQUAL              = 256 + 101;  // ==
    public static final int LESS_THAN_OR_EQUAL_TO     = 256 + 102;  // <=
    public static final int GREATER_THAN_OR_EQUAL_TO  = 256 + 103;  // >=
    public static final int LOGICAL_AND               = 256 + 106;  // &&
    public static final int LOGICAL_OR                = 256 + 107;  // ||
    public static final int SPREAD                    = 256 + 108;  // ..

    // keywords
    public static final int DECL_VAR    = 256 + 201;
    public static final int DECL_INPUT  = 256 + 202;
    public static final int DECL_OUTPUT = 256 + 203;
    public static final int CHOOSE      = 256 + 204;
    public static final int WHEN        = 256 + 205;
    public static final int ELSE        = 256 + 206;
}