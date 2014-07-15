package can.i.has.tracing.format

import groovy.transform.Canonical

@Canonical
@Singleton(strict = false)
class TraceLevel {
    private int lvl = 0

    int getLvl() {
        return lvl
    }

    void enter(){
        lvl++
    }

    void leave(){
        lvl--
        assert lvl>=0
    }

    @Canonical
    static class Enhancer extends TraceEnhancer{
        final String leftBracket = "["
        final String rightBracket = "]"

//        Enhancer(TraceFormatter formatter, String leftBracket="[", String rightBracket="]") {
//            super(formatter)
//            this.leftBracket=leftBracket
//            this.rightBracket=rightBracket
//        }

        @Override
        String enhance(String msg) {
            "$leftBracket${TraceLevel.instance.lvl}$rightBracket $msg"
        }
    }

    @Canonical
    static class IndentEnhancer extends TraceEnhancer{
        final String indent = "  "

//        IndentEnhancer(TraceFormatter formatter, String indent="  ") {
//            super(formatter)
//            this.indent=indent
//        }

        @Override
        String enhance(String msg) {
            "${indent*(TraceLevel.instance.lvl)}$msg"
        }
    }
}
