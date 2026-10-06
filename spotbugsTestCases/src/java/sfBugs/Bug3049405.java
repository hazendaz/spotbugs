package sfBugs;

import edu.umd.cs.findbugs.annotations.DesireNoWarning;

import org.jspecify.annotations.NonNull;

public class Bug3049405 {
    final Object o = new Object();

    @DesireNoWarning("NP_NULL_ON_SOME_PATH")
    public void foo(@NonNull Object o) {
        this.o.toString();
    }
}
