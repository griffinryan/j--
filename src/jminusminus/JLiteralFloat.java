package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a float literal.
 */
class JLiteralFloat extends JExpression {
    // String representation of the literal.
    private String text;

    /**
     * Constructs an AST node for a float literal given its line number and string representation.
     *
     * @param line line in which the literal occurs in the source file.
     * @param text string representation of the literal.
     */
    public JLiteralFloat(int line, String text) {
        super(line);
        this.text = text;
    }

    /**
     * Returns the literal as a float.
     *
     * @return the literal as a float.
     */
    public float toFloat() {
        return Float.parseFloat(text);
    }

    /**
     * Analyzes the float literal. In this context, analysis could simply set the correct type and
     * possibly check for any semantic rules specific to float literals.
     *
     * @param context context in which names are resolved.
     * @return the analyzed (and possibly modified) AST subtree.
     */
    public JExpression analyze(Context context) {
        type = Type.FLOAT; // Ensure the type is set to FLOAT.
        return this;
    }

    /**
     * Generates the code for loading the float literal on the stack. The specific bytecode
     * instruction will depend on the value of the literal.
     *
     * @param output the code emitter (basically an abstraction over bytecode generation).
     */
    public void codegen(CLEmitter output) {
        // Example for generating bytecode to load a float constant onto the stack.
        // You might need to use FCONST_0, FCONST_1, or FCONST_2 for 0f, 1f, and 2f,
        // or LDC for other float values. This is a simplification.
        output.addLDCInstruction(toFloat());
    }

    /**
     * Converts this AST node into a JSON object for visualization or debugging purposes.
     *
     * @param json the JSON object this node's representation should be added to.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JLiteralFloat:" + line, e);
        e.addAttribute("type", type == null ? "" : type.toString());
        e.addAttribute("value", text);
    }
}
