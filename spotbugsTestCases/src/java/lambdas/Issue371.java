package lambdas;
import org.jspecify.annotations.Nullable;

public class Issue371 {

    @Nullable
    private String returnsNull() {
        return null;
    }

    public void dereferenceWithLambda() {
        returnsNull().chars().map(x -> 42);
    }
}
