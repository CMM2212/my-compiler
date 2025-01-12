package compiler.lexer.tokens;

import compiler.lexer.Tag;

/**
 * Token representing a type in the source code (e.g. int, float, char, bool).
 *
 * Includes static instances for all the basic types.
 */
public class Type extends Word {
    // The storage size in bytes.
    public final int width;

    // Basic Types
    public static final Type
        Int = new Type("int", Tag.BASIC, 4),
        Float = new Type("float", Tag.BASIC, 8),
        Char = new Type("char", Tag.BASIC, 1),
        Bool = new Type("bool", Tag.BASIC, 1);

    /**
     * Creates a new type token with a given lexeme, tag, and width.
     *
     * @param lexeme The string representation of the type.
     * @param tag The tag for the type (e.g. Tag.BASIC).
     * @param width The storage size in bytes.
     */
    public Type(String lexeme, int tag, int width) {
        super(lexeme, tag);
        this.width = width;
    }

    /**
     * Returns true if the type is numeric (int, float, char).
     *
     * @param type The type to check.
     * @return True if the type is numeric, false otherwise.
     */
    public static boolean isNumeric(Type type) {
        return type == Type.Char || type == Type.Int || type == Type.Float;
    }
}