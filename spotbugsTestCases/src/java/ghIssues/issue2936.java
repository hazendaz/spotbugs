package ghIssues;

public class issue2936 {
    public void issue2936() {
        int lastIndex = 5;
        int startIndex = lastIndex + 1;

        for (int i = startIndex; i <= lastIndex; i++) {
            System.out.println("This loop cannot execute.");
        }
    }
}
