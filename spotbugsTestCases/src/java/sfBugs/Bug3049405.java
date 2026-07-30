package sfBugs;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import edu.umd.cs.findbugs.annotations.DesireNoWarning;

public class Bug3049405 {
    @Nullable
    final Object o = new Object();

    @DesireNoWarning("NP_NULL_ON_SOME_PATH")
    public void foo(@NonNull Object o) {
        this.o.toString();
    }
}
