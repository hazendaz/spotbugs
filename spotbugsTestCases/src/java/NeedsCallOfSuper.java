import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;

import edu.umd.cs.findbugs.annotations.ExpectWarning;

public class NeedsCallOfSuper {
    public static class GenericClass<X > {
        @OverrideMustInvoke
        public void genericMethod2( X obj ) {
        }
    }

    public class ConcreteClass extends GenericClass<String > {
    }

    public class DerivedClass extends ConcreteClass {
        @Override
        public void genericMethod2( String obj ) {
            // no call to super.genericMethod( obj )
        }
    }
}
