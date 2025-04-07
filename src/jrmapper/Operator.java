package jrmapper;

public enum Operator {
    // ASSIGN_EQUAL              (Type.BINARY, 1, Token.EQUALS),
    // PLUS_EQUALS               (Type.BINARY, 1, Token.PLUS_EQUALS),
    // MINUS_EQUALS              (Type.BINARY, 1, Token.MINUS_EQUALS),

    COMPARE_EQUAL             (Type.BINARY, 3, "==", Token.DOUBLE_EQUAL),
    LESS_THAN_OR_EQUAL_TO     (Type.BINARY, 3, "<=", Token.LESS_THAN_OR_EQUAL_TO),
    GREATER_THAN_OR_EQUAL_TO  (Type.BINARY, 3, ">=", Token.GREATER_THAN_OR_EQUAL_TO),
    LESS_THAN                 (Type.BINARY, 3, "<",  Token.LESS_THAN),
    GREATER_THAN              (Type.BINARY, 3, ">",  Token.GREATER_THAN),

    ADD                       (Type.BINARY, 5, "+", Token.PLUS),
    SUB                       (Type.BINARY, 5, "-", Token.MINUS),
    MUL                       (Type.BINARY, 6, "*", Token.STAR),
    DIV                       (Type.BINARY, 6, "/", Token.SLASH);

    public final Type    type;
    public final int     precedence;
    public final int     tokenType;
    public final String  printName;

    public enum Type { UNARY, BINARY, ASSIGNMENT, };
    private Operator(Type type, int precedence, String printName, int tokenType) {
        this.type       = type;
        this.precedence = precedence;
        this.printName  = printName;
        this.tokenType  = tokenType;
    };

    static final Operator[] cachedValues = values();
    
    public static Operator fromToken(Token token, boolean expect_unary) {
        for (var v: cachedValues) {
            if (v.tokenType == token.type() && ((v.type == Type.UNARY) == expect_unary)) {
                return v;
            }
        }
        return null;
    }
}

    