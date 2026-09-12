package androidAnnotations;

import org.jspecify.annotations.Nullable;

public class UncheckedNullableReturn {

    @Nullable String foo() {
        return null;
    }

    void bar() {
        System.out.println(foo().hashCode());
    }
}
