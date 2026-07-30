package sfBugs;

import org.jspecify.annotations.NonNull;



public class Bug3587164 {
    @NonNull
    static final String field1 = "yyyyMMdd";

    @NonNull
    static final String field2 = field1.toLowerCase();

}
