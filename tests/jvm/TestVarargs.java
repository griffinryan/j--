/**
 * Illustrate a method using variable arity parameters.
 * Griffin Ryan
 * */
public class TestVarargs {

    public static void printAll(String... strings) {
        for (String str : strings) {
            System.out.println(str);
        }
    }

    public static void main(String[] args) {
        printAll("Hello", "World", "with", "Varargs");
    }

}
