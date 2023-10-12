package java8;

class RunFinalizersOnExit {

    int f() {
        System.runFinalizersOnExit(true);
        return 42;
    }

}
