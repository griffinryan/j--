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

/**
 * The AST node for an enhanced for-statement.
 */
class JEnhancedForStatement extends JStatement {
    // The loop variable declaration.
    private JVariableDeclaration declaration;

    // The iterable expression.
    private JExpression iterable;

    // The body of the loop.
    private JStatement body;

    /**
     * Constructs an AST node for an enhanced for-statement.
     *
     * @param line         line in which the enhanced for-statement occurs in the source file.
     * @param declaration  the loop variable declaration.
     * @param iterable     the iterable expression.
     * @param body         the body of the loop.
     */
    public JEnhancedForStatement(int line, JVariableDeclaration declaration,
                                 JExpression iterable, JStatement body) {
        super(line);
        this.declaration = declaration;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public JAST analyze(Context context) {
        Context loopContext = new LocalContext(context);

        declaration.analyze(loopContext);
        iterable = iterable.analyze(loopContext);

        // Check if iterable is an array
        if (!iterable.type().isArray()) {
            // Since direct isIterable() check isn't available, you might limit
            // enhanced for-loops to arrays, or implement your own check here
            // based on your type system's capabilities.
            JAST.compilationUnit.reportSemanticError(line,
                    "The right-hand side of a for-each loop must be an array in this simplified j--.");
        }

        body.analyze(loopContext);

        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        // Assume indices for local variables based on your method's layout
        // For simplicity, let's assume:
        // 0 - "this" reference (for non-static methods)
        // 1 - first method argument or first local variable (if no arguments)
        // Adjust these indices according to your actual method context

        int arrayRefIndex = 1; // Index for the array reference
        int index = 2; // Index for the loop counter

        // Start: Code to push the array onto the stack
        iterable.codegen(output);
        // Store the array reference in a local variable
        output.addOneArgInstruction(ASTORE, arrayRefIndex);

        // Initialize loop counter to 0
        output.addNoArgInstruction(ICONST_0);
        output.addOneArgInstruction(ISTORE, index);

        String test = output.createLabel();
        String end = output.createLabel();

        // Condition check start
        output.addLabel(test);

        // Load loop counter and array length, compare them
        output.addOneArgInstruction(ALOAD, arrayRefIndex);
        output.addNoArgInstruction(ARRAYLENGTH);
        output.addOneArgInstruction(ILOAD, index);
        output.addBranchInstruction(IF_ICMPGE, end);

        // Load the current array element into the loop variable
        output.addOneArgInstruction(ALOAD, arrayRefIndex);
        output.addOneArgInstruction(ILOAD, index);
        output.addNoArgInstruction(AALOAD);

        // Assuming the loop variable is directly after the index
        int loopVarIndex = 3; // Adjust based on your context
        output.addOneArgInstruction(ASTORE, loopVarIndex);

        // Body of the loop
        body.codegen(output);

        // Increment the loop counter
        output.addIINCInstruction(index, 1);

        // Jump back to the condition check
        output.addBranchInstruction(GOTO, test);

        // Condition check end
        output.addLabel(end);
    }

    @Override
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JEnhancedForStatement:" + line, e);
        JSONElement eDecl = new JSONElement();
        e.addChild("Declaration", eDecl);
        declaration.toJSON(eDecl);
        JSONElement eIter = new JSONElement();
        e.addChild("Iterable", eIter);
        iterable.toJSON(eIter);
        JSONElement eBody = new JSONElement();
        e.addChild("Body", eBody);
        body.toJSON(eBody);
    }
}