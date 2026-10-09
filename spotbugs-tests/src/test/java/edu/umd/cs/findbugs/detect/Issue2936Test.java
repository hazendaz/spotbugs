package edu.umd.cs.findbugs.detect;

import edu.umd.cs.findbugs.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class Issue2936Test extends AbstractIntegrationTest {

    @Test
    void testIlInfiniteLoopFalsePositiveForAlwaysFalseCondition() {
        performAnalysis("ghIssues/Issue2936.class");

        // No 'IL_INFINITE_LOOP' should be reported
        assertBugTypeCount("IL_INFINITE_LOOP", 0);
    }
}
