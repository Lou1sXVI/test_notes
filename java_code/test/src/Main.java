//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.

        int a = 1;
        for (int i = 1; i <= 5; i++) {

            int b = 0;
            b = a++;
            System.out.println(b);
            System.out.println("i = " + i);
        }
        a = 1;
        for (int i = 1; i <= 5; ++i) {
            int b = 0;
            b = ++a;
            System.out.println(b);
            System.out.println("i = " + i);
        }
    }
}