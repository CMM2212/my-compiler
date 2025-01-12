package compiler.lexer;

/**
 * Represents the tags for tokens.
 *
 * Tags are unique integers that represent the token type. They begin at
 * 256 to avoid conflicts with ASCII values.
 */
public class Tag {
    public final static int
    NUM = 256, ID = 257, TRUE = 258, FALSE = 259, BASIC=260,
    IF = 261, ELSE = 262, WHILE = 263, DO = 264, BREAK = 265,
    EQ = 266, NE = 267, LT = 268, LE = 269, GT = 270, GE = 271,
    AND = 272, OR = 273, REAL = 274, ADD = 275, SUB = 276, MUL = 277,
    DIV = 278, ASSIGN = 279, NOT = 280, SEMICOLON = 281, LPAREN = 282,
    RPAREN = 283, LBRACE = 284, RBRACE = 285, LBRACKET = 286, RBRACKET=287,
    EOF = 288;
}
