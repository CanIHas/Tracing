package can.i.has.tracing.destination

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.lang.reflect.Method

@ToString
@EqualsAndHashCode
class Slf4jTraceDestination implements TraceDestination{
    Logger logger
    private String level

    Slf4jTraceDestination(Logger logger=null, String level=null) {
        if (logger==null)
            logger = LoggerFactory.getLogger(this.class)
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
