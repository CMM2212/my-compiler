package compiler.parser.ast.nodes.statements;

import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.StatementNode;
import compiler.parser.ast.nodes.declarations.DeclNode;
import compiler.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.List;

/**
 * A node representing a block of code.
 *
 * Blocks are enclosed in braces and optionally contain a list of declarations
 * followed by a list of statements. Each block is its own scope and has its own
 * symbol table.
 */
public class BlockNode implements StatementNode {
    // Declaration of variable: int x; int y;
    public List<DeclNode> decls = new ArrayList<>();
    // Statements within the block: x = 5; y = 3;
    public List<StatementNode> statements = new ArrayList<>();

    // Symbol table stores the declared variables in this block.
    public SymbolTable table;

    /**
     * Creates an empty BlockNode.
     *
     * After constructed, the decls and statements fields should be set to the appropriate values.
     */
    public BlockNode() {
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}