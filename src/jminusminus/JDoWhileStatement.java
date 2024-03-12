package jminusminus;

public class JDoWhileStatement extends JStatement {
    private JStatement body;
    private JExpression condition;

    public JDoWhileStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    @Override
    public JStatement analyze(Context context) {
        body = (JStatement) body.analyze(context);
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        return this;
    }

    @Override
    public void codegen(CLEmitter output) {
        String startLoop = output.createLabel();
        String endLoop = output.createLabel();

        output.addLabel(startLoop);
        body.codegen(output);

        condition.codegen(output, startLoop, true);
        output.addLabel(endLoop);
    }

}
