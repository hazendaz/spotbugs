package edu.umd.cs.findbugs.detect;

import edu.umd.cs.findbugs.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class Issue2936Test extends AbstractIntegrationTest {

    @Test
    void testIlInfiniteLoopFalsePositiveForAlwaysFalseCondition() {
        performAnalysis("ghIssues/Issue2936.class");

        assertBugCount("ghIssues.Issue2936", "issue2936", 0);
    }
}
