package googleAnnotations;

import com.google.common.base.Nullable;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class TestNullable {
    static void foo(@Nullable Object o) {
    }

    static void bar() {
        foo(null);
    }

    static void foo2(@org.jspecify.annotations.Nullable Object o) {
    }

    static void bar2() {
        foo2(null);
    }
}
