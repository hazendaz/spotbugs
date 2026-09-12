package sfBugs;

import jakarta.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.DesireNoWarning;

import org.jspecify.annotations.Nullable;

public class Bug3049405 {
    @Nullable
    final Object o = new Object();

    @DesireNoWarning("NP_NULL_ON_SOME_PATH")
    public void foo(@Nonnull Object o) {
        this.o.toString();
    }
}
