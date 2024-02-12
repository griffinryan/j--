// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.GOTO;

/**
 * The AST node for a try-catch-finally statement.
 */
class JTryStatement extends JStatement {
    // The try block.
    private JBlock tryBlock;

    // The catch parameters.
    private ArrayList<JFormalParameter> parameters;

    // The catch blocks.
    private ArrayList<JBlock> catchBlocks;

    // The finally block.
    private JBlock finallyBlock;

    /**
     * Constructs an AST node for a try-statement.
     */
    public JTryStatement(int line, JBlock tryBlock, ArrayList<JFormalParameter> parameters,
                         ArrayList<JBlock> catchBlocks, JBlock finallyBlock) {
        super(line);
        this.tryBlock = tryBlock;
        this.parameters = parameters;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;
    }

    @Override
    public JTryStatement analyze(Context context) {
        // Analyze the try block
        tryBlock = tryBlock.analyze(context);

        // Each catch block creates a new scope
        for (int i = 0; i < catchBlocks.size(); i++) {
            JFormalParameter param = parameters.get(i);
            param.setType(param.type().resolve(context));
            catchBlocks.set(i, catchBlocks.get(i).analyze(context));
        }

        // Finally block analysis
        if (finallyBlock != null) {
            finallyBlock = finallyBlock.analyze(context);
        }

        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        String startTryLabel = output.createLabel();
        String endTryLabel = output.createLabel();
        ArrayList<String> catchLabels = new ArrayList<>();
        String startFinallyLabel = finallyBlock != null ? output.createLabel() : null;
        String endFinallyLabel = finallyBlock != null ? output.createLabel() : null;

        output.addLabel(startTryLabel);
        tryBlock.codegen(output);
        output.addLabel(endTryLabel);

        // Jump to finally or end
        if (finallyBlock != null) {
            output.addBranchInstruction(GOTO, startFinallyLabel);
        }

        // Generate catch blocks
        for (int i = 0; i < catchBlocks.size(); i++) {
            String catchLabel = output.createLabel();
            catchLabels.add(catchLabel);
            output.addLabel(catchLabel);

            // Assuming catch parameters are exceptions
            parameters.get(i).codegen(output);

            catchBlocks.get(i).codegen(output);

            // Jump to finally or end
            if (finallyBlock != null) {
                output.addBranchInstruction(GOTO, startFinallyLabel);
            }
        }

        // Finally block generation
        if (finallyBlock != null) {
            output.addLabel(startFinallyLabel);
            finallyBlock.codegen(output);
            output.addLabel(endFinallyLabel);
        }

        // Exception table setup
        for (int i = 0; i < catchBlocks.size(); i++) {
            output.addExceptionHandler(startTryLabel, endTryLabel, catchLabels.get(i),
                    parameters.get(i).type().jvmName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JTryStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("TryBlock", e1);
        tryBlock.toJSON(e1);
        if (catchBlocks != null) {
            for (int i = 0; i < catchBlocks.size(); i++) {
                JFormalParameter param = parameters.get(i);
                JBlock catchBlock = catchBlocks.get(i);
                JSONElement e2 = new JSONElement();
                e.addChild("CatchBlock", e2);
                String s = String.format("[\"%s\", \"%s\"]", param.name(), param.type() == null ?
                        "" : param.type().toString());
                e2.addAttribute("parameter", s);
                catchBlock.toJSON(e2);
            }
        }
        if (finallyBlock != null) {
            JSONElement e2 = new JSONElement();
            e.addChild("FinallyBlock", e2);
            finallyBlock.toJSON(e2);
        }
    }

}
