// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a throw-statement.
 */
class JThrowStatement extends JStatement {
    private JExpression expr;

    public JThrowStatement(int line, JExpression expr) {
        super(line);
        this.expr = expr;
    }

    @Override
    public JStatement analyze(Context context) {
        expr = expr.analyze(context);
        // Example of a naming convention check, not ideal but a workaround
        String typeName = expr.type().toString();
        if (!(typeName.endsWith("Exception") || typeName.endsWith("Error"))) {
            JAST.compilationUnit.reportSemanticError(line,
                    "Expression in a throw statement must be an exception type, found: %s",
                    typeName);
        }
        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        // Generate code for the expression
        expr.codegen(output);
        // Throws the exception
        output.addNoArgInstruction(ATHROW);
    }

    @Override
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JThrowStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Expression", e1);
        expr.toJSON(e1);
    }
}
