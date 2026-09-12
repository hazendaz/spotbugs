package sfBugs;

import org.jspecify.annotations.Nullable;

public class Bug1828288 {

    @Nullable
    public Object field;

    @Override
    public String toString() {
        return field.toString(); // (*)
    }

}
