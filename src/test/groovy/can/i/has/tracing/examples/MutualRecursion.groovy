package can.i.has.tracing.examples

import can.i.has.tracing.Trace

class MutualRecursion {
    @Trace
    int f(int x) {
        assert x>=0
        x == 0 ? 1 : x - m(f(x-1))
    }

    @Trace
    int m(int x) {
        assert x>=0
        x == 0 ? 0 : x - f(m(x-1))
    }
}
