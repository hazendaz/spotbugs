package ghIssues.issue374;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

public class MethodLevel {
    public String method() {
        return methodNullable(null);
    }

    private String methodNullable(final @Nullable String test) {
        return methodNonNull(test);
    }

    @NullMarked
    private String methodNonNull(final String test) {
        return test;
    }
}
