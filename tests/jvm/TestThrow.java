// Testing class for new j-- functionality.

public class TestThrow {

    public static void main(String[] args) {
        try {
            throw new RuntimeException("Exception thrown");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

}
