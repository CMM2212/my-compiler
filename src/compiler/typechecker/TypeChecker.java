package compiler.typechecker;

import compiler.errors.*;
import compiler.lexer.Tag;
import compiler.lexer.tokens.*;
import compiler.parser.Parser;
import compiler.parser.ast.ASTVisitor;
import compiler.parser.ast.nodes.*;
import compiler.parser.ast.nodes.declarations.*;
import compiler.parser.ast.nodes.expressions.*;
import compiler.parser.ast.nodes.expressions.operations.*;
import compiler.parser.ast.nodes.statements.*;
import compiler.parser.ast.nodes.structures.ProgramNode;
import compiler.parser.ast.nodes.terminals.*;
import compiler.symbols.Env;
import compiler.symbols.Symbol;

public class TypeChecker implements ASTVisitor {
    public Parser parser;
    public ProgramNode program;
    public Env env = null;

    public TypeChecker(Parser parser) {
        this.parser = parser;
        program = parser.program;
        visit(program);
    }

    public Boolean isLogicOperator(String operator) {
        return operator.equals("&&") || operator.equals("||");
    }

    public Boolean isComparisonOperator(String operator) {
        return operator.equals("==") || operator.equals("!=") || operator.equals("<") || operator.equals("<=") ||
                operator.equals(">") || operator.equals(">=");
    }

    public Boolean isArithmeticOperator(String operator) {
        return operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/");
    }

    @Override
    public void visit(ProgramNode n) {
        n.block.accept(this);
    }

    @Override
    public void visit(BlockNode n) {
        env = n.table;
        for (DeclNode decl : n.decls)
            decl.accept(this);
        for (StatementNode stmt : n.statements)
            stmt.accept(this);
        env = env.prev;
    }

    @Override
    public void visit(DeclNode n) {
        n.type.accept(this);
        n.id.accept(this);
    }

    @Override
    public void visit(TypeNode n) {
        n.type.accept(this);
        if (n.array != null) {
            n.array.accept(this);
        }
    }

    @Override
    public void visit(ArrayTypeNode n) {
        n.size.accept(this);
        if (n.type != null) {
            n.type.accept(this);
        }
    }

    @Override
    public void visit(AssignmentNode n) {
        n.left.accept(this);
        TypeNode left = n.left.getType();
        n.expression.accept(this);
        TypeNode rightTypeNode = n.expression.getType();

        if (left.getDepth() != rightTypeNode.getDepth()) {
            throw new TypeException("array dimensions do not match: cannot assign '" + rightTypeNode +
                    "' = '" + left + "'" , n.getLine());
        }
        if (left.type.type == Type.Float && rightTypeNode.type.type == Type.Int) {
            return;
        }
        if (left.type.type != rightTypeNode.type.type) {
            throw new TypeException("type mismatch: cannot assign '" + rightTypeNode + "' to '"
                    + left + "'", n.getLine());
        }
    }

    @Override
    public void visit(IfNode n) {
        n.expression.accept(this);
        n.thenStatement.accept(this);
        if (n.elseStatement != null) {
            n.elseStatement.accept(this);
        }
    }

    @Override
    public void visit(WhileNode n) {
        n.expression.accept(this);
        n.body.accept(this);
    }

    @Override
    public void visit(DoWhileNode n) {
        n.body.accept(this);
        n.expression.accept(this);
    }

    @Override
    public void visit(LocNode n) {
        n.id.accept(this);
        TypeNode declaredType = n.id.getType();

        // Count dimensions being accessed.
        int dimensionsLeft = getDimensionsLeft(n, declaredType);

        // Create a new type node with the correct type and dimensions.
        TypeNode newTypeNode = new TypeNode();
        newTypeNode.type = declaredType.type; // Keep the base type.

        // Attach remaining dimensions if any.
        if (dimensionsLeft > 0) {
            ArrayTypeNode currentArrayType = new ArrayTypeNode(); // Start the chain.
            newTypeNode.array = currentArrayType; // Link the first dimension.

            for (int i = 1; i < dimensionsLeft; i++) {
                currentArrayType.type = new ArrayTypeNode(); // Create a new dimension.
                currentArrayType = currentArrayType.type;    // Move to the next dimension.
            }
        }

        n.setType(newTypeNode);
        if (n.array != null) {
            n.array.accept(this);
        }
    }

    private static int getDimensionsLeft(LocNode n, TypeNode declaredType) {
        int dimensionsAccessed = n.getDepth();
        int dimensionsDeclared = declaredType.getDepth();
        int dimensionsLeft = dimensionsDeclared - dimensionsAccessed;

        // Check that dimensions accessed is not more than the declared array dimensions.
        if (dimensionsLeft < 0) {
            if (dimensionsDeclared == 0) {
                throw new TypeException("'" + n.id.w +
                        "' cannot be accessed as an array; it is type '" + declaredType + "'", n.getLine());
            } else {
                throw new TypeException("cannot access '" + n.id.w + "' as a " + dimensionsAccessed +
                        " dimensional array; it is only a " + dimensionsDeclared + " dimensional array", n.getLine());
            }
        }
        return dimensionsLeft;
    }

    @Override
    public void visit(ArrayLocNode n) {
        n.expression.accept(this);

        TypeNode expressionType = n.expression.getType();
        if (expressionType.type.type != Type.Int || expressionType.getDepth() > 0) {
            throw new TypeException("array index must be an integer, not '" + n.expression.getType() + "'", n.getLine());
        }

        if (n.array != null) {
            n.array.accept(this);
        }
    }

    @Override
    public void visit(BinaryExpressionNode n) {
        n.left.accept(this);
        TypeNode left = n.left.getType();
        n.setType(left);
        if (n.right == null) {
            return;
        }
        n.right.accept(this);
        TypeNode right = n.right.getType();

        Type leftType = left.type.type;
        Type rightType = right.type.type;

        if (left.getDepth() > 0 || right.getDepth() > 0) {
            throw new TypeException("binary operator cannot be applied to an array", n.getLine());
        }
        if (rightType == Type.Float) {
            n.setType(right);
        } else if (leftType == Type.Float) {
            n.setType(left);
        }

        if (isLogicOperator(n.operator)) {
            if (leftType != Type.Bool || rightType != Type.Bool) {
                throw new TypeException("logical operator '" + n.operator +
                        "' expects boolean types, not '" + left + "' and '" + right + "'", n.getLine());
            }
        } else if (isComparisonOperator(n.operator)) {
            if (leftType != rightType) {
                throw new TypeException("comparison operator '" + n.operator +
                        "' expects same types, not '" + left + "' and '" + right + "'", n.getLine());
            }
            // Set type of node to be boolean because comparisons return a boolean.
            TypeNode newBoolType = new TypeNode();
            newBoolType.type = new BasicNode();
            newBoolType.type.type = Type.Bool;
            n.setType(newBoolType);
        } else if (isArithmeticOperator(n.operator)) {
            if (!Type.numeric(leftType) || !Type.numeric(rightType)) {
                throw new TypeException("arithmetic operator '" + n.operator +
                        "' expects numeric types, not '" + left + "' and '" + right + "'", n.getLine());
            }
        }
    }

    @Override
    public void visit(UnaryNode n) {
        n.right.accept(this);
        n.setType(n.right.getType());

        if (n.op == null) {
            return;
        }

        if (n.getType().getDepth() > 0) {
            throw new TypeException("unary operator cannot be applied to an array", n.getLine());
        }

        if (n.op.tag == Tag.NOT) {
            if (n.getType().type.type != Type.Bool) {
                throw new TypeException("'!' operator expects boolean type, not '" +
                        n.getType() + "'", n.getLine());
            }
        } else if (n.op.tag == Tag.SUB) {
            if (!Type.numeric(n.getType().type.type)) {
                throw new TypeException("unary '-' operator expects numeric type, not '" +
                        n.getType() + "'", n.getLine());
            }
        }
    }

    @Override
    public void visit(ParenthesisNode n) {
        n.expression.accept(this);
        n.setType(n.expression.getType());
    }
    @Override
    public void visit(FalseNode n) {
        TypeNode newType = new TypeNode();
        newType.type = new BasicNode();
        newType.type.type = Type.Bool;
        n.setType(newType);
    }

    @Override
    public void visit(IdNode n) {
        Symbol s = env.get(n.w);
        if (s == null) {
            // Parser should have already caught this.
            throw new SyntaxException("variable '" + n.id + "' not declared");
        }
        n.setType(s.type);
    }

    @Override
    public void visit(NumNode n) {
        TypeNode newType = new TypeNode();
        newType.type = new BasicNode();
        newType.type.type = Type.Int;
        n.setType(newType);
    }

    @Override
    public void visit(RealNode n) {
        TypeNode newType = new TypeNode();
        newType.type = new BasicNode();
        newType.type.type = Type.Float;
        n.setType(newType);
    }

    @Override
    public void visit(TrueNode n) {
        TypeNode newType = new TypeNode();
        newType.type = new BasicNode();
        newType.type.type = Type.Bool;
        n.setType(newType);
    }
}
