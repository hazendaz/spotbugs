package sfBugs;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import javax.annotation.meta.TypeQualifierDefault;

import edu.umd.cs.findbugs.annotations.ExpectWarning;

public class Bug2311502 {

    @Documented
    @NonNull
    @TypeQualifierDefault(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReturnValuesAreNonnullByDefault {
    }

    /**
     * Should flag code as unsafe.
     */
    public static class NonNullFalseNegative {

        @Nullable
        private Object junkField;

        public void setJunk(Object junk) {
            this.junkField = junk;
        }

        public final class BadInnerClass {
            @ExpectWarning("NP")
            public void badMethod() {
                System.out.println(junkField.hashCode()); // should be caught as
                                                          // a bug
            }
        }

    }

    public static @ReturnValuesAreNonnullByDefault
    class NPNonNullReturnViolationBug {

        @Nullable
        private Object junkField;

        public void setJunk(Object junk) {
            this.junkField = junk;
        }

        public final class InnerClass {
            /**
             * Prints out {@link NPNonNullReturnViolationBug#junkField}, if it's
             * currently not <code>null</code>.
             */
            @ExpectWarning("IMA_INEFFICIENT_MEMBER_ACCESS")
            public void printJunk() {
                Object temp = junkField;
                if (temp != null) { // should be perfectly safe
                    System.out.println(temp.hashCode());
                }
            }
        }

    }

}
