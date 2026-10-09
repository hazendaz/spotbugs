package ghIssues;

/**
 * Test cases for GitHub issue #2936:
 * IL_INFINITE_LOOP false positive when loop cannot be entered.
 */
public class Issue2936 {

    public void issue2936() {
        // some random last index.
        int lastIndex = 5;

        // start index is always higher than start index.
        int startIndex = lastIndex + 1;

        // Loop can't be entered because start is higher.
        for (int i = startIndex; i <= lastIndex; i++) {
            System.out.println("Loop can't be entered. So can never go infinite.");
        }
    }

}
