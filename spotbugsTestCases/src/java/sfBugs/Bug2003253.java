package sfBugs;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import edu.umd.cs.findbugs.annotations.ExpectWarning;

public class Bug2003253 {
    @NonNull
    private Object foo;

    //
    // The InconsistentAnnotations detector should report
    // a warning here.
    //
    @ExpectWarning("NP")
    public void report1(@Nullable Object bar) {
        this.foo = bar;
    }

    //
    // FindNullDeref should report a warning here.
    //
    @ExpectWarning("NP")
    public void report2(@Nullable Object bar) {
        this.foo = bar;
    }

    //
    // FindNullDeref should report a warning here.
    //
    @ExpectWarning("NP")
    public int report3(@Nullable Object bar) {
        return bar.hashCode();
    }

    //
    // FindNullDeref should report a warning here.
    //
    @ExpectWarning("NP")
    public int report4(@Nullable Object bar) {
        return nonnull(bar);
    }

    public int nonnull(@NonNull Object bar) {
        return bar.hashCode();
    }
}
