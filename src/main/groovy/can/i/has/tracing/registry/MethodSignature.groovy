package can.i.has.tracing.registry

import groovy.transform.Canonical

@Canonical
class MethodSignature {
    Class[] paramTypes
    boolean varargs

    boolean matches(Class[] types){
        if (!varargs && types.size() != paramTypes.size())
            return false
        if (types.size()==0)
            return true
        for (int i=0; i<paramTypes.size()-1; i++)
            if (! isAssignable(paramTypes[i], types[i]))
                return false;
        int idx = paramTypes.size()-1
        if (!varargs)
            return isAssignable(paramTypes[idx], types[idx])
        Class varargsComponentClass = paramTypes[idx].componentType
        assert varargsComponentClass!=null
        for (; idx<types.size(); idx++)
            if (! isAssignable(varargsComponentClass, paramTypes[idx]))
                return false
        true
    }

    protected static final Map<Class, Class> PRIMITIVE_TO_WRAPPER = [
        (boolean): Boolean,
        (byte): Byte,
        (char): Character,
        (double): Double,
        (float): Float,
        (int): Integer,
        (long): Long,
        (short): Short,
        (void): Void

    ]

    protected static boolean isAssignable(Class targetClass, Class valueClass){
        if (targetClass.primitive)
            targetClass = PRIMITIVE_TO_WRAPPER[targetClass]
        if (valueClass.primitive)
            valueClass = PRIMITIVE_TO_WRAPPER[valueClass]
        targetClass.isAssignableFrom(valueClass)
    }
}
