/**
 * Test try-catch-finally blocks and throw statements.
 * Griffin Ryan
 * */
public class TestTryCatchFinally {

    public static void main(String[] args) {
        try {
            int result = 10 / 0;
            System.out.println(result);
        } catch (ArithmeticException e) {
            System.err.println("Arithmetic Error: " + e.getMessage());
        } finally {
            System.out.println("This block always executes.");
        }
    }

}
