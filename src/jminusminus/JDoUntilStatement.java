package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a do-until statement.
 */
class JDoUntilStatement extends JStatement {

    // The body of the do-until loop.
    private JBlock body;

    // The loop's termination condition.
    private JExpression condition;

    /**
     * Constructs an AST node for a do-until statement.
     *
     * @param line      line in which the do-until statement occurs
     *                  in the source file.
     * @param body      the loop's body
     * @param condition the loop's termination condition
     */
    public JDoUntilStatement(int line, JBlock body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    /**
     * Analyzes the do-until statement, checking types and analyzing the body
     * and condition expressions.
     */
    @Override
    public JAST analyze(Context context) {
        // Analyze the body and condition within the loop's scope
        body = (JBlock) body.analyze(context);
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        return this;
    }

    /**
     * Generates the bytecode for the do-until statement. It creates a loop
     * that continues executing as long as the condition evaluates to false.
     */
    @Override
    public void codegen(CLEmitter output) {
        String startLabel = output.createLabel();
        String endLabel = output.createLabel();

        output.addLabel(startLabel);
        body.codegen(output);

        // Evaluate the condition; jump to startLabel if false (since it's a do-until loop)
        condition.codegen(output, endLabel, false);

        // Loop back to the beginning of the loop body
        output.addBranchInstruction(GOTO, startLabel);

        // Label marking the end of the loop
        output.addLabel(endLabel);
    }

    @Override
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoUntilStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
