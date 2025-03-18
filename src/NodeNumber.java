public class NodeNumber extends Node {
    public NodeNumber(NodeScope parent, Token token) {
        super(parent, token);
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
    public Number value;

    // TODO: we could use better checking here to detect narrowing conversions and emit warnings
    //       it may actually be wiser to just move the dynamic number casts out to their own utility function,
    //       similar to how I do it in the data packer's remap_data functions.
    public boolean typecheck(Class hint_type) {
        if (valueType == Double.class && !isFloaty(hint_type)) {
            System.out.println(this.location() + ": Warning: potential loss of information in conversion from Double to '" + hint_type + "'.");
        }
        if (valueType == Long.class && !isIntegeresque(hint_type)) {
            System.out.println(this.location() + ": Warning: potential loss of information in conversion from Long to '" + hint_type + "'.");
        }

        // we seemingly cannot use a switch here on the type, not sure why Java doesn't like that...
        if (hint_type == Double.class || hint_type == double.class) {
            value     = value.doubleValue();
            valueType = Double.class;
        }
        else if (hint_type == Float.class || hint_type == float.class) {
            value     = value.floatValue();
            valueType = Float.class;
        }
        else if (hint_type == Long.class || hint_type == long.class) {
            value     = value.longValue();
            valueType = Long.class;
        }
        else if (hint_type == Integer.class || hint_type == int.class) {
            value     = value.intValue();
            valueType = Integer.class;
        }
        else if (hint_type == Short.class || hint_type == short.class) {
            value     = value.shortValue();
            valueType = Short.class;
        }
        else if (hint_type == Byte.class || hint_type == byte.class) {
            value     = value.byteValue();
            valueType = Byte.class;
        }

        flags.add(Flags.TYPECHECKED);
        return true;
    }

    public boolean serialize(StringBuilder sb) {
        if (flags.contains(Flags.PARENTHESIZED)) sb.append("(");
        sb.append(value.toString());
        if (flags.contains(Flags.PARENTHESIZED)) sb.append(")");
        return true;
    }

    public Object evaluate(Object hint_value) {
        assert(flags.contains(Flags.TYPECHECKED));
        return value;
    }


    private boolean isFloaty(Class type) {
        return type == Double.class
            || type == Float.class
            || type == double.class
            || type == float.class;
    }
    private boolean isIntegeresque(Class type) {
        return type == Integer.class
            || type == Short.class
            || type == Long.class
            || type == Byte.class
            || type == int.class
            || type == short.class
            || type == long.class
            || type == byte.class;
    }
}