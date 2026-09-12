package sfBugs;

import jakarta.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.ExpectWarning;

import org.jspecify.annotations.Nullable;

public class Bug3431688 {

    public interface MyInterface {
        @Nonnull
        Object foo(@Nullable Object o);
    }

    public static class MyImplementation implements MyInterface {
        @Override
        @ExpectWarning("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
        public Object foo(Object o) {
            return o;
        }
    }
}
