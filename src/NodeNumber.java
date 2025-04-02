public class NodeNumber extends Node {
    NodeNumber(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
        // NOTE: for now, we only store as Double or Long, based on whether the source text contained a '.'
        //       we also tentatively set the valueType, though this may be altered during typechecking to coerce to a different number type.
        // TODO: emit a warning if the number textually represented cannot be actually stored in either a Double or Long without loss of data.
        if (token.text().indexOf('.') != -1) {
            valueType = Double.class;
            value     = Double.parseDouble(token.text());
        } else {
            valueType = Long.class;
            value     = Long.parseLong(token.text());
        }
    }

    // need to be able to store either an integer or float losslessly depending on what was lexed
    // but also need to be able to coerce to whatever other type is needed when we typecheck the node
    Number value;

    // TODO: we could use better checking here to detect narrowing conversions and emit warnings
    //       it may actually be wiser to just move the dynamic number casts out to their own utility function,
    //       similar to how I do it in the data packer's remap_data functions.
    Class _typecheck(Class hint_type) {
        if (hint_type == null) return valueType;

        if (valueType == Double.class && !isFloaty(hint_type)) {
            System.out.println(this.location() + ": Warning: potential loss of information in conversion from Double to '" + hint_type + "'.");
        }
        if (valueType == Long.class && !isIntegeresque(hint_type)) {
            System.out.println(this.location() + ": Warning: potential loss of information in conversion from Long to '" + hint_type + "'.");
        }

        // we seemingly cannot use a switch here on the type, not sure why Java doesn't like that...
        if (isDouble(hint_type)) {
            value     = value.doubleValue();
            valueType = Double.class;
        }
        else if (isFloat(hint_type)) {
            value     = value.floatValue();
            valueType = Float.class;
        }
        else if (isLong(hint_type)) {
            value     = value.longValue();
            valueType = Long.class;
        }
        else if (isInteger(hint_type)) {
            value     = value.intValue();
            valueType = Integer.class;
        }
        else if (isShort(hint_type)) {
            value     = value.shortValue();
            valueType = Short.class;
        }
        else if (isByte(hint_type)) {
            value     = value.byteValue();
            valueType = Byte.class;
        }

        return valueType;
    }

    void _serialize(StringBuilder sb) {
        sb.append(value.toString());
    }

    Object _evaluate(Object hint_value) {
        return value;
    }

    static boolean isNumericType(Class type) {
        return isFloaty(type) || isIntegeresque(type) || type.equals(Number.class);
    }

    static boolean isFloaty(Class type) {
        return isDouble(type) || isFloat(type);
    }
    
    static boolean isIntegeresque(Class type) {
        return isLong(type) || isInteger(type) || isShort(type) || isByte(type);
    }
    
    static boolean isDouble  (Class type) { return type.equals( Double.class) || type.equals(double.class); }
    static boolean isFloat   (Class type) { return type.equals(  Float.class) || type.equals( float.class); }
    static boolean isLong    (Class type) { return type.equals(   Long.class) || type.equals(  long.class); }
    static boolean isInteger (Class type) { return type.equals(Integer.class) || type.equals(   int.class); }
    static boolean isShort   (Class type) { return type.equals(  Short.class) || type.equals( short.class); }
    static boolean isByte    (Class type) { return type.equals(   Byte.class) || type.equals(  byte.class); }
    
    static boolean areMatchingTypes(Class type_a, Class type_b) {
        return isDouble (type_a) && isDouble (type_b) || 
               isFloat  (type_a) && isFloat  (type_b) || 
               isLong   (type_a) && isLong   (type_b) || 
               isInteger(type_a) && isInteger(type_b) || 
               isShort  (type_a) && isShort  (type_b) || 
               isByte   (type_a) && isByte   (type_b);
    }
}