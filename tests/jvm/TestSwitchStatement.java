/**
 * Test switch-statement with string case labels.
 * Griffin Ryan
 * */
public class TestSwitchStatement {

    public static void main(String[] args) {
        String day = "MONDAY";
        switch (day) {
            case "MONDAY":
                System.out.println("Start of work week");
                break;
            case "FRIDAY":
                System.out.println("End of work week");
                break;
            default:
                System.out.println("Midweek days");
        }
    }

}
