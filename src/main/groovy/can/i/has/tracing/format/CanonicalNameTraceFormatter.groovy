package can.i.has.tracing.format

import groovy.transform.InheritConstructors

@InheritConstructors
class CanonicalNameTraceFormatter extends DefaultTraceFormatter {
    String className(Class clazz){
        clazz.canonicalName
    }
}
