// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a conditional expression.
 */
public class JConditionalExpression extends JExpression {
    private JExpression condition;
    private JExpression truePart;
    private JExpression falsePart;

    public JConditionalExpression(int line, JExpression condition, JExpression truePart, JExpression falsePart) {
        super(line);
        this.condition = condition;
        this.truePart = truePart;
        this.falsePart = falsePart;
    }

    @Override
    public JExpression analyze(Context context) {
        // Analyze the condition and ensure it's a boolean expression
        condition = condition.analyze(context);
        if (condition.type() != Type.BOOLEAN) {
            JAST.compilationUnit.reportSemanticError(line(),
                    "Condition in conditional expression must be of type boolean.");
        }

        // Analyze both branches
        truePart = truePart.analyze(context);
        falsePart = falsePart.analyze(context);

        // Ensure the true and false parts have compatible types
        if (!truePart.type().equals(falsePart.type())) {
            JAST.compilationUnit.reportSemanticError(line(),
                    "True and false parts of the conditional expression must have compatible types.");
        }

        // The type of the conditional expression is the type of the true (or false) part
        type = truePart.type();

        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        String trueLabel = output.createLabel();
        String endLabel = output.createLabel();

        // Generate bytecode for the condition and compare it with zero (false)
        condition.codegen(output, trueLabel, false);

        // If condition is false, jump to the false part
        falsePart.codegen(output);
        output.addBranchInstruction(GOTO, endLabel);

        // Label for the true part
        output.addLabel(trueLabel);
        truePart.codegen(output);

        // Label for the end of the conditional expression
        output.addLabel(endLabel);
    }

    @Override
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JConditionalExpression:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        JSONElement condElement = new JSONElement();
        condition.toJSON(condElement);
        e.addChild("Condition", condElement);
        JSONElement truePartElement = new JSONElement();
        truePart.toJSON(truePartElement);
        e.addChild("TruePart", truePartElement);
        JSONElement falsePartElement = new JSONElement();
        falsePart.toJSON(falsePartElement);
        e.addChild("FalsePart", falsePartElement);
    }
}
