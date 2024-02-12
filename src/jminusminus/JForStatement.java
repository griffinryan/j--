package jminusminus;

import java.util.ArrayList;
import static jminusminus.CLConstants.*;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization statements.
    private ArrayList<JStatement> init;

    // Test expression.
    private JExpression condition;

    // Update statements.
    private ArrayList<JStatement> update;

    // The body of the loop.
    private JStatement body;

    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      initialization statements.
     * @param condition the test expression.
     * @param update    update statements.
     * @param body      the body of the loop.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition,
                         ArrayList<JStatement> update, JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    /**
     * Analyzes the for-statement, including its initialization, condition, update, and body.
     */
    @Override
    public JForStatement analyze(Context context) {
        Context loopContext = new LocalContext(context);

        if (init != null) {
            for (JStatement stmt : init) {
                stmt.analyze(loopContext);
            }
        }

        if (condition != null) {
            condition = condition.analyze(loopContext);
            condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        }

        if (update != null) {
            for (JStatement stmt : update) {
                stmt.analyze(loopContext);
            }
        }

        if (body != null) {
            body.analyze(loopContext);
        }

        return this;
    }

    /**
     * Generates the bytecode for the for-statement, including loops and conditions.
     */
    @Override
    public void codegen(CLEmitter output) {
        String startLoopLabel = output.createLabel();
        String endLoopLabel = output.createLabel();

        if (init != null) {
            for (JStatement stmt : init) {
                stmt.codegen(output);
            }
        }

        output.addLabel(startLoopLabel);

        if (condition != null) {
            condition.codegen(output, endLoopLabel, false);
        }

        if (body != null) {
            body.codegen(output);
        }

        if (update != null) {
            for (JStatement stmt : update) {
                stmt.codegen(output);
            }
        }

        output.addBranchInstruction(GOTO, startLoopLabel);
        output.addLabel(endLoopLabel);
    }

    /**
     * Writes this AST node in a JSON format for debugging and visualization purposes.
     */
    @Override
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JForStatement:" + line, e);

        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }

        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }

        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }

        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
