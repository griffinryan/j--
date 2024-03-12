package jminusminus;

import static jminusminus.CLConstants.ATHROW;

/**
 * The AST node for a throw statement.
 */
class JThrowStatement extends JStatement {

    // The expression representing the exception to throw.
    private JExpression expr;

    /**
     * Constructs an AST node for a throw statement.
     *
     * @param line the line in which the throw statement occurs
     *             in the source file.
     * @param expr the expression representing the exception to throw.
     */
    public JThrowStatement(int line, JExpression expr) {
        super(line);
        this.expr = expr;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        expr = expr.analyze(context);
        expr.type().mustMatchExpected(line(), Type.THROWABLE);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        expr.codegen(output);
        output.addNoArgInstruction(ATHROW);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JThrowStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Expression", e1);
        expr.toJSON(e1);
    }
}
