package can.i.has.tracing.format

import groovy.transform.Canonical

import java.text.DateFormat

@Canonical
class DateEnhancer extends TraceEnhancer{

    final DateFormat dateFormat = DateFormat.timeInstance

//    DateEnhancer(TraceFormatter formatter, DateFormat dateFormat = DateFormat.timeInstance) {
//        super(formatter)
//        this.dateFormat=dateFormat
//    }

    @Override
    String enhance(String msg) {
        "${dateFormat.format(new Date())} $msg"
    }
}
