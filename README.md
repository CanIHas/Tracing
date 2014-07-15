Tracing
=======

Simple tracing for situations, when AOP is too much and by-hand logging on the beginning and end of each method
is not enough.

Shortest manual possible
------------------------

* Annotate wanted methods with @Traced, configuring each of them with proper boolean attributes (like withArgs or onExit)
* Somehow obtain and configure Tracer - you may change formatter ("how does trace look like?") and trace destination ("where is trace stored?")
* Register some packages or single classes and run closure with trace; optionally, specify classes and packages to register in running methods arguments
* Inspect (or not, like with logging destinations) trace
* Profit

Version
-------

0.0.0-PROTOTYPING - I've just copied my old, ugly code to this repo, and haven't started debugging, cleaning and refactoring yet.
