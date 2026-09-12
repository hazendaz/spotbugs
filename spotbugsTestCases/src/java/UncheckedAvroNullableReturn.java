import org.apache.avro.reflect.Nullable;

import org.jspecify.annotations.Nullable;

public class UncheckedAvroNullableReturn {
    @Nullable
    String foo() {
        return null;
    }

    void bar() {
        System.out.println(foo().hashCode());
    }
}
