package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a long literal.
 */
class JLiteralLong extends JExpression {
    // String representation of the literal.
    private String text;

    /**
     * Constructs an AST node for a long literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public JLiteralLong(int line, String text) {
        super(line);
        this.text = text;
    }

    /**
     * Analyzes the long literal. In this context, analysis could simply set the correct type.
     *
     * @param context context in which names are resolved.
     * @return this AST node.
     */
    public JExpression analyze(Context context) {
        type = Type.LONG;
        return this;
    }

    /**
     * Code generation for long literals involves pushing the literal onto the stack.
     *
     * @param output the code emitter (basically an abstraction over bytecode generation).
     */
    public void codegen(CLEmitter output) {
        long value = Long.parseLong(text);
        // Example codegen: this might involve LDC or other instructions for long values.
    }

    /**
     * Writing this AST node to a JSON representation.
     *
     * @param json the JSON object this node's representation should be added to.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JLiteralLong:" + line, e);
        e.addAttribute("type", "LONG");
        e.addAttribute("value", text);
    }
}
