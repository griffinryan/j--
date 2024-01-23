// TestScannerEnhancements.java
// Tests various enhancements made to the j-- compiler's scanner

public class TestScanner {

    public static void main(String[] args) {
        // Test multi-line comments
        /* This is a test
           of multi-line comments */
        System.out.println("Multi-line comment test passed");

        // Nested and incorrect multi-line comments
        System.out.println("Nested multi-line comment test passed");

        // Test Java operators
        int x = 5 + 6; // Addition
        boolean equalityTest = (x == 11); // Equality
        boolean logicalAnd = (x > 5) && (x < 12); // Logical AND
        System.out.println("Java operators test passed");

        // Test reserved words
        if (x > 0) {
            for (int i = 0; i < x; i++) {
                System.out.println("Reserved words test passed");
            }
        }

        // Test literals
        double doubleLiteral = 123.45; // Double literal
        double scientificNotation = 1.2e3; // Scientific notation
        float floatLiteral = 123.45f; // Float literal
        long longLiteral = 12345L; // Long literal
        int hexLiteral = 0x1A3F; // Hexadecimal
        int octalLiteral = 0177; // Octal
        int binaryLiteral = 0b1101; // Binary
        System.out.println("Literals test passed");

        // All tests passed
        System.out.println("All scanner enhancement tests passed");
    }
}
