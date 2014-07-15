package can.i.has.tracing.destination

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.lang.reflect.Method

@ToString
@EqualsAndHashCode
class CommonsTraceDestination implements TraceDestination{
    Log logger
    private String level

    CommonsTraceDestination(Log logger=null, String level=null) {
        if (logger==null)
            logger = LogFactory.getLog(this.class)
        if (level==null)
            level = "trace"

        this.logger = logger
        setLevel(level)
    }

    String getLevel() {
        return level
    }

    void setLevel(String level) {
        assert level in logger.class.methods.collect {Method m -> m.name}
        this.level = level
    }

    @Override
    void trace(String msg) {
        logger."$level" msg
    }
}
