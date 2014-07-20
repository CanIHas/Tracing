Tracing
=======

Simple tracing for situations, when AOP is too much and by-hand logging at the beginning and end of each method
is not enough.

Shortest manual possible
------------------------

* Annotate wanted methods with @Traced, configuring each of them with proper boolean attributes (like withArgs or onExit)
* Somehow obtain and configure Tracer - you may change formatter ("how does trace look like?") and trace destination ("where is trace stored?")
* Register some packages or single classes and run closure with trace; optionally, specify classes and packages to register in running methods arguments
* Inspect (or not, like with logging destinations) trace
* Profit

A little longer manual
----------------------

The whole lib is created for situations, when you need to debug something, and configuring debugger would be difficult
and using full-blown AOP framework to trace several methods would be too much.

By "tracing" we mean "gathering information of calling chosen methods". We may want to gather information about
level of nesting of each call, time of call, arguments and returned/throwed value.

It may not be best practice to use code like this in production, but when you're frustrated, because following your code
through the whole MOP (in Groovy) is too long, or when you're trying to run something on application server and
debugging is harder - this may come handy.

Annotating your code with Trace (can.i.haz.tracing.@Trace) has almost no effect - it only adds metadata to runtime
(since annotation has retention level RUNTIME).

### Model

* TraceTargetRegistry (`can.i.haz.tracing.registry.TraceTargetRegistry`) - singleton keeping track of all classes with activated tracing,
            that is, all classes that has at least one annotation on its methods while those methods are "registered" -
            specifically stated, that annotation should be treated as note "trace mne".
* @Trace (`can.i.haz.tracing.Trace`) - annotation stating that in proper conditions (when targets owners class is registered)
            target methods should be traced. Also enables customization of tracing - which facts should be gathered
            (calling, returning, throwing) and how specific they should be. See below for further explanation.
* TraceConfig (`can.i.haz.tracing.registry.TraceConfig`) - POJO containing the same options as specified in @Trace. Registry
            keeps binding class -> method -> TraceConfig to recognize traced methods, so you can programmatically
            (un)register some methods without adnotating them, just by calling `registry.registerMethod(method, <Trace Config instance>)`
* Tracer (`can.i.haz.tracing.Tracer`) - main usable component. You can create your own Tracer (by specifying its formatting
            and destination), or use Tracer.DEFAULT. Also, you can specify some tracer as Tracer.GLOBAL (static), so you
            can use the same instance in different places in code (GLOBAL defaults to DEFAULT). You can obtain
            TracingContext with this, or just call some closure with tracing configured manually (it delegates to
            empty context, so if you're using call() and similiar on Tracer, you have to take care of (un)registering
            yourself).
* TracerContext (`can.i.haz.tracing.Tracer.TracingContext` - protected, non-static) - definition of classes and instances
            that should be traced while calling methods. It's reason for existence is activating and deactivating tracing
            for call of some closure only (by registering classes and doing magic on instances before calling closure
            and unregistering them and un-magicking instances after call, so state of environment before and after call
            is the same).

### Tracing by example

> If this is first time you're reading this, skip subpoints and read only top-level points.
> Full points are describing basic concepts, while subpoints describe them in more details.

Lets consider class:
`package some.package.you.have

class A {
    def foo(args) {...}
    def bar(args) {...}
}`
Assume that foo and bar call each other recursively, but you can't understand where is problem with that.

It may be easier to see whole tree of calls, instead of debugging the whole thing - so you know what called what and with what.

Using AOP here would be overkill, and adding log.debug(...) at the beginning and end of foo and bar would be... well, ugly
and it would force you to mix logic of methods with logic of debugging. So, instead, we use `can.i.has.tracing`.

#### 1. Configure trace targets

    First, you must state which methods you want to trace - and how do you want them to be traced. For now, just annotate
    your methods with `@Trace`:

        class A {
            @Trace
            def foo(args) {...}
            @Trace
            def bar(args) {...}
        }

##### 1.1. *Customizing trace behaviour*

As you may have spotted, @Trace annotation has several parameters. They are all boolean, and have meaning as follows:

| Parameter      | Meaning                                                       |
|----------------|---------------------------------------------------------------|
| onEnter        | Should fact of calling target method be gathered?             |
| withArgs       | If not onEnter - ignored; else - should arguments be gathered together with fact of method call? |
| onReturn       | Should fact of target method returning be gathered?           |
| withResult     | If not onReturn - ignored; else - should result of the call be gathered together with fact of method returning? |
| onThrow        | Should fact of target method throwing anything be gathered?   |
| withException  | If not onThrow - ignored; else - should thrown exception (throwable in general) be gathered together with fact of method throwing? |
| withStackTrace | If not onThrow - ignored; else - should throwables stack tracebe gathered together with fact of method throwing? |

### 2. Obtain Tracer

For this example, we'll use preformatted tracer - Tracer.DEFAULT:

    def tracer = Tracer.DEFAULT

And that's basically it for this moment

#### 2.1. Custom Tracer

> THIS SECTIONS IS WIP

### 3. Everybody do the flo... trace!

Now it's time to perform your tracing. Lets do this in easiest way:

    tracer.withPackageTraced("some.package.you")({ // no args here; package must contain class A
        def a = new A()
        a.foo(1, 2, 3)
    })

`Trace#withPackageTraced(String)` creates `TracingContext` object, remembering proper package. As context is callable
(with Closure parameter), we call it with what we want to run with trace. `TracingContext#call(Closure)` registers
all annotated methods found in remembered packages in `TraceTargetRegistry` instance, calls closure, and then
unregisters everything it registered itself. There is a little more to this, but unless you'll try to outsmart simple
mechanism - everything will be fine.

There are some API-fluency-hacks for calling closure. Method `<T> TracingContext#call(Closure<T>)` has following aliases:
    * `<T> TracingContext#_(Closure<T>)`
    * `<T> TracingContext#leftShift(Closure<T>)`

In the end, there is no difference between `someContext.call({...})`, `someContext({...})`, `someContext._ {...}`,
`someContext._({...})` and `someContext << {...}`.

#### 3.1. Do I really need annotations?

No, you don't. But this is WIP, so...

> THIS SECTION IS WIP

### 4. Profit - or do anything with trace

So, you've called your code with trace enabled. As `Tracer.DEFAULT` uses `CommonsTraceDestination`
(`can.i.has.tracing.destination.CommonsTraceDestination`), your call trace should be logged to Apache Commons logger
for this destination class with level "trace".

In general - at this point you'll have your trace where you want it and how you want it. See (2.1) to read more on
different destinations and formatters.

Full-blown example
------------------

> You probably haven't seen anything as WIP as this...

More to come...
---------------

> For real


Version
-------

0.0.5 - This SOMETIMES works. I still need to give this a lot of time, but it goes in good direction.

CanIHaz
=======

CanIHaz project is set of different libraries created for fun, or during writing something else.
They are stored in repos of CanIHaz organization, and are written with fluent API and UNIX paradigm ("do one thing only,
 but do it right") in mind.

 This is just a stub of CanIHaz description. Time is finite, unfortunately...
