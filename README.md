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

A little bit longer manual
----------------------

The whole lib is created for situations, when you need to debug something, and configuring debugger would be difficult
and using full-blown AOP framework to trace several methods would be too much.

By "tracing" we mean "gathering information of calling chosen methods". We may want to gather information about
level of nesting of each call, time of call, arguments and returned/throwed value.

It may not be best practice to use code like this in production, but when you're frustrated, because following your code
through the whole MOP (in Groovy) is too long, or when you're trying to run something on application server and
debugging is harder - this may come handy.

Annotating your code with Trace (can.i.haz.tracing.@Trace) has almost no effect - it only adds metadata to runtime
(since annotation has retention level RUNTIME). This means that you can easily annotate methods you often trace while
debugging and leave that even in production code - unless you activate tracing literally NOTHING happens because of this
library on classpath.

In future annotation will probably be in different component, so you can even throw tracing logic out of main dependencies
and keep it only in test dependencies.

### Model (simplified)

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

    package some.package.you.have

    class A {
        def foo(args) {...}
        def bar(args) {...}
    }

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

##### 1.1. Customizing trace behaviour

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

OK, you're happy, because you can get nice trace on Slf4j (I assume you've read this docs at least once now). But let's
assume that you need to trace in on custom level with Log4j. Also, you need only package name name of class which method
was called. And... let's add uppercase method name to this...

Let's generalize a little: you want to change the way of STORING gathered trace and/or you want to change the way
trace is formatted. This is where two interfaces (and their implementations) come to play. Please, welcome `TraceDestination`
and `TraceFormatter`!

##### 2.1.1. can.i.haz.tracing.destination.TraceDestination

The interface has only one method: `void trace(String msg)`.

> Yeah, I know - one-method interfaces in Groovy with it's Closures? Yep, I did it. And I had good reason!
> When does casting Closures to one-method interfaces is useful? When real implementation would be one-method too.
> But what if we would like to implement some HTTP (for example REST) destination? Or maybe you'd like to delegate to
> syslog? You'd probably end up with a little bigger class (not too big too, but still - more then one method). Now
> you'd need to provide argument with method closured (like `implementation.&someMethod`). Now you can use simple object,
> and Groovy will still cast closures to this interface!

It is quite straight-forward - semantics of this method are "gather another line of performed trace". That's actually it.

There are some provided implementations too (all in package `can.i.haz.tracing.destination`):

* NullDestination - Empty method implementation. Useful for stubbing and some formatter-based hacks
* StringBufferDestination - Implementation pushing every line to StringBuffer (joining them with newline) for which there
    are accessors
* StdOutDestination - Basically identical to System.out.&println.
* FileTraceDestination - Each call to trace(String) appends argument to file (specified as instance attribute).
* Slf4jTraceDestination, CommonsTraceDestination - logging frameworks destinations for Slf4j and Apache Commons
    (respectively). They are parametrized with logger instance (defaulting to logger for destination class) and level
    (defaulting to TRACE; in future implementations, as other frameworks are planned too, it will be the same or similiar
    if absent).

> There is nothing that would stop you from implementing your own destination. I will be happy to pull any request with
> implementation from anyone.

##### 2.1.2. TraceFormatter and further part of 2.1 - WIP

> WIP, AS STATED.

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

> THIS SECTION IS WIP AND FOR NOW YOU DO NEED ANNOTATIONS.

### 4. Profit - or do anything with trace

So, you've called your code with trace enabled. As `Tracer.DEFAULT` uses `Slf4jTraceDestination`
(`can.i.has.tracing.destination.Slf4jTraceDestination`), your call trace should be logged to Slf4j logger
for this destination class with level "trace".

> I'm gonna try to use Apache Commons in all CanIHas projects for the sake of Spring integration
> Truth is, Apache Commons are hell lot of easier to configure. Truth is that, as this is realy early stage
> of development, I'll stick with Slf4j here and I'll keep Apache Commons destination implemented, yet horribly
> untested. Sorry folks, crucial matters first ;)

In general - at this point you'll have your trace where you want it and how you want it. See (2.1) to read more on
different destinations and formatters.

Full-blown example
------------------

Example is available on GIT repository. It is located in test sourceset (/src/test/groovy).

### can.i.has.tracing.examples.MutualRecursion // traced class

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

### can.i.has.tracing.example.MutualRecursionExample // tracing logic

    package can.i.has.tracing.examples

    import can.i.has.tracing.Tracer

    Tracer.DEFAULT.withPackageTraced("can.i.has.tracing")._ {
        def instance = new MutualRecursion()
        instance.f(5)
    }

### simplelogger.properties // default logger configuration

    org.slf4j.simpleLogger.defaultLogLevel=trace
    org.slf4j.simpleLogger.showLogName=true
    org.slf4j.simpleLogger.showDateTime=false
    org.slf4j.simpleLogger.levelInBrackets=true

    #org.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss|SSS

    org.slf4j.simpleLogger.showShortLogName=false
    org.slf4j.simpleLogger.showThreadName=false

### Expected result log


    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination - MutualRecursion#f called with args: [5]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -   MutualRecursion#f called with args: [4]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#f called with args: [3]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f called with args: [2]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#m called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -             MutualRecursion#m called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -             MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -             MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -             MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#m called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f returned result: 2
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m called with args: [2]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#m called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -           MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#f returned result: 2
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#m called with args: [2]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#m returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -   MutualRecursion#f returned result: 3
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -   MutualRecursion#m called with args: [3]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#m called with args: [2]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#m returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#f called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m called with args: [1]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f called with args: [0]
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -         MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -       MutualRecursion#m returned result: 0
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -     MutualRecursion#f returned result: 1
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination -   MutualRecursion#m returned result: 2
    [TRACE] can.i.has.tracing.destination.Slf4jTraceDestination - MutualRecursion#f returned result: 3

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
