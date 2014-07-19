package can.i.has.tracing.fixtures

import can.i.has.tracing.Trace


class Clazz1 {
    @Trace
    String foo(int x, int y) {
        "${bar(x, baz(y))}"
    }

    @Trace
    int bar(Integer x, int y) {
        x*y + (x % y)
    }

    @Trace
    int baz(int x, boolean recursive=true){
        (x-3)*(x-2) + (recursive?baz(x+2, false):0)
    }

    @Trace
    int factorial(int i) {
        assert i>=0
        i<2 ? 1 : i*factorial(i-1)
    }
}
