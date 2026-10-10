package jsr305;

import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;

public class TestOverridesSuper {

    @OverrideMustInvoke
    void f() {
    }

    @OverrideMustInvoke
    void g() {
    }

    static class SubClass extends TestOverridesSuper {
        @Override
        void f() {
            super.f();
        }

        @Override
        void g() {

        }
    }

}
