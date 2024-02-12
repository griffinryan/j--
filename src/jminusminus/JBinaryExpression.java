// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for a binary expression --- an expression with a binary
 * operator and two operands: lhs and rhs.
 */
abstract class JBinaryExpression extends JExpression {
    /**
     * The binary operator.
     */
    protected String operator;

    /**
     * The lhs operand.
     */
    protected JExpression lhs;

    /**
     * The rhs operand.
     */
    protected JExpression rhs;

    /**
     * Constructs an AST node for a binary expression.
     *
     * @param line     line in which the binary expression occurs in the source file.
     * @param operator the binary operator.
     * @param lhs      the lhs operand.
     * @param rhs      the rhs operand.
     */
    protected JBinaryExpression(int line, String operator, JExpression lhs, JExpression rhs) {
        super(line);
        this.operator = operator;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBinaryExpression:" + line, e);
        e.addAttribute("operator", operator);
        e.addAttribute("type", type == null ? "" : type.toString());
        JSONElement e1 = new JSONElement();
        e.addChild("Operand1", e1);
        lhs.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Operand2", e2);
        rhs.toJSON(e2);
    }
}

/**
 * The AST node for a multiplication (*) expression.
 */
class JMultiplyOp extends JBinaryExpression {
    /**
     * Constructs an AST for a multiplication expression.
     *
     * @param line line in which the multiplication expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JMultiplyOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "*", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IMUL);
    }
}

/**
 * The AST node for a plus (+) expression. In j--, as in Java, + is overloaded to denote addition
 * for numbers and concatenation for Strings.
 */
class JPlusOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an addition expression.
     *
     * @param line line in which the addition expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JPlusOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "+", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        if (lhs.type() == Type.STRING || rhs.type() == Type.STRING) {
            return (new JStringConcatenationOp(line, lhs, rhs)).analyze(context);
        } else if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            type = Type.INT;
        } else {
            type = Type.ANY;
            JAST.compilationUnit.reportSemanticError(line(), "Invalid operand types for +");
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IADD);
    }
}

/**
 * The AST node for a subtraction (-) expression.
 */
class JSubtractOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a subtraction expression.
     *
     * @param line line in which the subtraction expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JSubtractOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "-", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(ISUB);
    }
}

/**
 * The AST node for a division (/) expression.
 */
class JDivideOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a division expression.
     *
     * @param line line in which the division expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JDivideOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "/", lhs, rhs);
    }

    /**
     * Analyzes the lhs and rhs operands to determine their types, apply type promotion,
     * and set the type for this expression.
     */
    @Override
    public JExpression analyze(Context context) {
        // Analyze lhs and rhs to resolve their types
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        // Apply type promotion rules
        if (lhs.type().equals(Type.DOUBLE) || rhs.type().equals(Type.DOUBLE)) {
            type = Type.DOUBLE; // Promote to double if either operand is double
        } else if (lhs.type().equals(Type.FLOAT) || rhs.type().equals(Type.FLOAT)) {
            type = Type.FLOAT; // Promote to float if either operand is float
        } else {
            type = Type.INT; // Default to int for division (assuming no longs for simplicity)
        }

        // Ensure operands are of compatible types
        lhs.type().mustMatchExpected(line(), type);
        rhs.type().mustMatchExpected(line(), type);

        return this;
    }

    /**
     * Generates JVM bytecode for performing the division operation.
     */
    public void codegen(CLEmitter output) {
        // Assume type has been analyzed and set correctly
        lhs.codegen(output);
        rhs.codegen(output);

        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(IDIV);
        } else if (type.equals(Type.FLOAT)) {
            output.addNoArgInstruction(FDIV);
        } else if (type.equals(Type.DOUBLE)) {
            output.addNoArgInstruction(DDIV);
        } else {
            // Potentially throw an exception or handle the case where type is not supported
            throw new IllegalStateException("Unsupported type for division: " + type);
        }
    }

}


/**
 * The AST node for a remainder (%) expression.
 */
class JRemainderOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a remainder expression.
     *
     * @param line line in which the remainder expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JRemainderOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "%", lhs, rhs);
    }

    /**
     * Analyzes the lhs and rhs operands to determine their types, apply type promotion,
     * and set the type for this expression based on Java's type promotion rules.
     */
    @Override
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        // Apply type promotion and ensure operands are compatible
        if (lhs.type().equals(Type.DOUBLE) || rhs.type().equals(Type.DOUBLE)) {
            type = Type.DOUBLE;
        } else if (lhs.type().equals(Type.FLOAT) || rhs.type().equals(Type.FLOAT)) {
            type = Type.FLOAT;
        } else {
            // Default to int if no floating-point types are involved
            type = Type.INT;
        }

        // Ensure operands are of compatible types
        lhs.type().mustMatchExpected(line(), type);
        rhs.type().mustMatchExpected(line(), type);

        return this;
    }

    /**
     * Generates JVM bytecode for performing the remainder operation.
     */
    @Override
    public void codegen(CLEmitter output) {
        // Generate code for evaluating the lhs and rhs expressions
        lhs.codegen(output);
        rhs.codegen(output);

        // Emit the appropriate remainder instruction based on the type
        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(IREM);
        } else if (type.equals(Type.FLOAT)) {
            output.addNoArgInstruction(FREM);
        } else if (type.equals(Type.DOUBLE)) {
            output.addNoArgInstruction(DREM);
        } else {
            // Handle error or unexpected type
            throw new IllegalStateException("Unsupported type for remainder: " + type);
        }
    }
}


/**
 * The AST node for an inclusive or (|) expression.
 */
class JOrOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an inclusive OR expression.
     *
     * @param line line in which the inclusive OR expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JOrOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "|", lhs, rhs);
    }

    /**
     * Analyzes the lhs and rhs operands to determine their types, apply type promotion,
     * and set the type for this expression based on the operation's rules.
     */
    @Override
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        // Assuming | operation is for integers (and possibly longs) for bitwise OR
        if (lhs.type().equals(Type.INT) && rhs.type().equals(Type.INT)) {
            type = Type.INT;
        } else if (lhs.type().equals(Type.LONG) || rhs.type().equals(Type.LONG)) {
            // Promote to long if either operand is long
            type = Type.LONG;
        } else {
            JAST.compilationUnit.reportSemanticError(line(),
                    "Invalid operand types for | operator");
            // Set to ANY to avoid further errors in case of invalid types
            type = Type.ANY;
        }

        return this;
    }

    /**
     * Generates JVM bytecode for performing the inclusive OR operation.
     */
    @Override
    public void codegen(CLEmitter output) {
        // Generate code for evaluating the lhs and rhs expressions
        lhs.codegen(output);
        rhs.codegen(output);

        // Emit the appropriate inclusive OR instruction based on the type
        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(IOR);
        } else if (type.equals(Type.LONG)) {
            output.addNoArgInstruction(LOR);
        } else {
            // Error handling or logging for unsupported types
            throw new IllegalStateException("Unsupported type for JOrOp codegen: " + type);
        }
    }
}


/**
 * The AST node for an exclusive or (^) expression.
 */
class JXorOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an exclusive OR expression.
     *
     * @param line line in which the exclusive OR expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JXorOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "^", lhs, rhs);
    }

    /**
     * Analyzes the lhs and rhs operands to determine their types, apply type promotion,
     * and set the type for this expression based on the operation's rules.
     */
    @Override
    public JExpression analyze(Context context) {
        lhs = lhs.analyze(context);
        rhs = rhs.analyze(context);

        // Type checking to ensure operands are integer or long for bitwise XOR
        if (lhs.type().equals(Type.INT) && rhs.type().equals(Type.INT)) {
            type = Type.INT;
        } else if (lhs.type().equals(Type.LONG) || rhs.type().equals(Type.LONG)) {
            // Promote to long if either operand is long
            type = Type.LONG;
        } else {
            // Report error for invalid operand types
            JAST.compilationUnit.reportSemanticError(line(),
                    "Invalid operand types for ^ operator");
            // Set to ANY to avoid further errors in case of invalid types
            type = Type.ANY;
        }

        return this;
    }

    /**
     * Generates JVM bytecode for performing the exclusive OR operation.
     */
    @Override
    public void codegen(CLEmitter output) {
        // Generate code for evaluating the lhs and rhs expressions
        lhs.codegen(output);
        rhs.codegen(output);

        // Emit the appropriate exclusive OR instruction based on the type
        if (type.equals(Type.INT)) {
            output.addNoArgInstruction(IXOR);
        } else if (type.equals(Type.LONG)) {
            output.addNoArgInstruction(LXOR);
        } else {
            // Error handling or logging for unsupported types
            throw new IllegalStateException("Unsupported type for JXorOp codegen: " + type);
        }
    }
}


/**
 * The AST node for an and (&amp;) expression.
 */
class JAndOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an and expression.
     *
     * @param line line in which the and expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JAndOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "&", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
    }
}

/**
 * The AST node for an arithmetic left shift (&lt;&lt;) expression.
 */
class JALeftShiftOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an arithmetic left shift expression.
     *
     * @param line line in which the arithmetic left shift expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JALeftShiftOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "<<", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
    }
}

/**
 * The AST node for an arithmetic right shift (&rt;&rt;) expression.
 */
class JARightShiftOp extends JBinaryExpression {
    /**
     * Constructs an AST node for an arithmetic right shift expression.
     *
     * @param line line in which the arithmetic right shift expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JARightShiftOp(int line, JExpression lhs, JExpression rhs) {
        super(line, ">>", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
    }
}

/**
 * The AST node for a logical right shift (&rt;&rt;&rt;) expression.
 */
class JLRightShiftOp extends JBinaryExpression {
    /**
     * Constructs an AST node for a logical right shift expression.
     *
     * @param line line in which the logical right shift expression occurs in the source file.
     * @param lhs  the lhs operand.
     * @param rhs  the rhs operand.
     */
    public JLRightShiftOp(int line, JExpression lhs, JExpression rhs) {
        super(line, ">>>", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        // TODO
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        // TODO
    }
}

class JBitwiseAndOp extends JBinaryExpression {
    public JBitwiseAndOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "&", lhs, rhs);
    }

    public JExpression analyze(Context context) {
        // Type checking and analysis code here
        return this;
    }

    public void codegen(CLEmitter output) {
        // Bytecode generation code here
    }
}
