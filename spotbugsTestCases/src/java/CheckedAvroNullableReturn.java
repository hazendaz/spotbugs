import org.jspecify.annotations.Nullable;

public class CheckedAvroNullableReturn {
    @Nullable
    String foo() {
        return null;
    }

    void bar() {
        String foo = foo();
        if (foo != null) {
            System.out.println(foo.hashCode());
        }
    }
}
