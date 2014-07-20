package can.i.has.tracing.examples

import can.i.has.tracing.Tracer

Tracer.DEFAULT.withPackageTraced("can.i.has.tracing")._ {
    def instance = new MutualRecursion()
    instance.f(5)
}