import java.util.EnumSet;

public class NodeNumber extends Node {
    public NodeNumber(NodeScope parent, Token token) {
        super(parent, token);
        this.kind = Kind.NUMBER;
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
        // we seemingly cannot use a switch here on the type, not sure why Java doesn't like that...
        if (hint_type == Double.class) {
            value     = value.doubleValue();
            valueType = Double.class;
        }
        else if (hint_type == Long.class) {
            value     = value.longValue();
            valueType = Long.class;
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

    public Object evaluate() {
        assert(flags.contains(Flags.TYPECHECKED));
        return value;
    }
}