// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a switch-statement.
 */
public class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups
    private ArrayList<SwitchStatementGroup> stmtGroup;

    public JSwitchStatement(int line, JExpression condition, ArrayList<SwitchStatementGroup> stmtGroup) {
        super(line);
        this.condition = condition;
        this.stmtGroup = stmtGroup;
    }

    @Override
    public JStatement analyze(Context context) {
        condition = condition.analyze(context);
        // Check for String, int (char is subsumed under int in this context)
        if (condition.type() != Type.STRING && condition.type() != Type.INT) {
            JAST.compilationUnit.reportSemanticError(line,
                    "Switch condition must be of type int or String, found type: %s",
                    condition.type());
        }

        for (SwitchStatementGroup group : stmtGroup) {
            group.analyze(context, condition.type());
        }
        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        // First, generate code for the switch condition and store it in a local variable.
        condition.codegen(output);

        // Assuming index 1 (or another specific index) is available for use.
        // This assumes you're not in a static method with no other local variables declared before this point.
        // If this is not the case, adjust the index accordingly.
        int conditionIndex = 1; // Adjust based on actual method context
        output.addOneArgInstruction(ASTORE, conditionIndex);

        String endSwitchLabel = output.createLabel();
        ArrayList<String> caseLabels = new ArrayList<>();

        for (int i = 0; i < stmtGroup.size(); i++) {
            caseLabels.add(output.createLabel());
        }

        // Load the condition from the local variable for each comparison
        for (int i = 0; i < stmtGroup.size(); i++) {
            output.addOneArgInstruction(ALOAD, conditionIndex);

            // Insert comparison logic here, similar to previously discussed, adjusting for String vs. int

            // For each case label...
            // Note: Actual comparison logic will vary based on the type (String vs. int)
            // and needs to be inserted here.
        }

        // Code generation for each case's block of statements
        for (int i = 0; i < stmtGroup.size(); i++) {
            output.addLabel(caseLabels.get(i));
            for (JStatement statement : stmtGroup.get(i).getBlock()) {
                statement.codegen(output);
            }
            output.addBranchInstruction(GOTO, endSwitchLabel);
        }

        output.addLabel(endSwitchLabel);
    }


    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : stmtGroup) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch statement group consists of case labels and a block of statements.
 */
class SwitchStatementGroup {
    // Case labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels case labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    public ArrayList<JExpression> getSwitchLabels() {
        return switchLabels;
    }

    public ArrayList<JStatement> getBlock() {
        return block;
    }

    /**
     * Analyzes the switch statement group.
     *
     * @param context    the current context.
     * @param switchType the type of the switch expression.
     */
    public void analyze(Context context, Type switchType) {
        // Analyze each label
        for (JExpression label : switchLabels) {
            if (label != null) { // Skip for default case
                label = label.analyze(context);
                // Ensure label type matches the switch expression's type
                if (!label.type().equals(switchType)) {
                    JAST.compilationUnit.reportSemanticError(label.line(),
                            "Switch label type %s does not match switch expression type %s.",
                            label.type(), switchType);
                }
            }
        }

        // Analyze each statement in the block
        for (JStatement statement : block) {
            statement.analyze(context);
        }
    }

    /**
     * Generates the bytecode for the switch statement group.
     *
     * @param output      the CLEmitter for generating bytecode.
     * @param switchExpr  the switch expression, needed for comparison in case of strings.
     * @param defaultLabel the label to jump to if no case matches; only used if this group represents the default case.
     * @param endLabel    the label to jump to after executing the block.
     */
    public void codegen(CLEmitter output, JExpression switchExpr, String defaultLabel, String endLabel) {
        for (JExpression label : switchLabels) {
            if (label != null) {
                // Comparison logic for int and String types
                switchExpr.codegen(output);
                label.codegen(output);

                if (switchExpr.type() == Type.STRING) {
                    // Invoke String.equals for String type labels
                    output.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                    output.addBranchInstruction(IFNE, defaultLabel);
                } else {
                    // Direct comparison for int type labels
                    output.addBranchInstruction(IF_ICMPEQ, defaultLabel);
                }
            }
        }

        // Generate bytecode for the block statements
        for (JStatement statement : block) {
            statement.codegen(output);
        }

        // Jump to end of switch after block execution, unless it's a fall-through case
        output.addBranchInstruction(GOTO, endLabel);
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
