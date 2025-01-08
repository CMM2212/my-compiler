package compiler.lexer.tokens;

import compiler.lexer.Tag;

/**
 * Represents a type in the source code (e.g. int, float, char, bool).
 */
public class Type extends Word {
    public final int width; // Storage size in bytes

    public static final Type
        Int = new Type("int", Tag.BASIC, 4),
        Float = new Type("float", Tag.BASIC, 8),
        Char = new Type("char", Tag.BASIC, 1),
        Bool = new Type("bool", Tag.BASIC, 1);

    /**
     * Constructor.
     * @param s The string representation of the type.
     * @param tag The tag for the type.
     * @param w The storage size in bytes.
     */
    public Type(String s, int tag, int w) {
        super(s, tag);
        width = w;
    }

    /**
     * Returns true if the type is numeric (int, float, char).
     * @param p The type to check.
     * @return True if the type is numeric, false otherwise.
     */
    public static boolean numeric(Type p) {
        return p == Type.Char || p == Type.Int || p == Type.Float;
    }
}