import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeMethodCall extends Node {
    NodeMethodCall(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
    }

    NodeIdentifier identifier;
    Method          resolvedMethod;
    ArrayList<Node> specifiedParameters;

    // NOTE: hint_type here is the base object type, not the result type.
    Class<?> _typecheck(Class<?> hint_type) {
        for (var node: specifiedParameters) {
            node.tryTypecheck(null);
        }
        
        var methods = new ArrayList<Method>();
        
        // get all available methods on class and superclasses recursively
        {
            Class<?> clazz = hint_type;
            while (clazz != null) {
                methods.addAll(Arrays.asList(clazz.getMethods()));
                clazz = clazz.getSuperclass();
            }
        }

        // filter by method name and number of parameters
        // this feels bad... there's gotta be a better way to do this.
        // can't we just get the stream back to an arraylist?
        {
            methods = new ArrayList<>(
                methods.stream().filter(
                    m -> m.getName().equals(identifier.name)
                            && m.getParameterCount() == specifiedParameters.size()
                ).toList()
            );
        }

        if (methods.size() > 1) {
            // now for manual filtering, we want to check that all parameters for which we already have resolved types are valid
            var it_method = methods.iterator();
            while (it_method.hasNext()) {
                var method = it_method.next();
                var formal_parameters = List.of(method.getGenericParameterTypes());
                assert(formal_parameters.size() == specifiedParameters.size());
                for (int i = 0; i < formal_parameters.size(); i++) {
                    var specified = specifiedParameters.get(i);
                    var formal    = (Class<?>)formal_parameters.get(i);
                    if (specified.valueType != null && !formal.isAssignableFrom(specified.valueType)) {
                        it_method.remove();
                        break;
                    }
                }
            }
        }
        
        if (methods.isEmpty()) {
            throw new RuntimeException(location() + ": Error: unable to resolve method '" + identifier.name + "' for class " + hint_type + ".");
        }
        
        assert(methods.size() == 1);
        resolvedMethod = methods.getFirst();
        
        // do second pass over arguments to verify types and re-typecheck with new type hint if necessary
        var resolved_formal_parameters = List.of(resolvedMethod.getGenericParameterTypes());
        for (int i = 0; i < resolved_formal_parameters.size(); i++) {
            var specified = specifiedParameters.get(i);
            var formal    = (Class<?>)resolved_formal_parameters.get(i);
            if (!formal.isAssignableFrom(specified.valueType) && !NodeNumber.areMatchingTypes(formal, specified.valueType)) {
                var final_type = specified.typecheck(formal);
                if (!formal.isAssignableFrom(specified.valueType) && !NodeNumber.areMatchingTypes(formal, specified.valueType)) {
                    throw new RuntimeException(location() + ": Error: mismatched types on parameter " + (i+1) + " of call to method '" + identifier.name + "'. Expected '" + formal + "', but got '" + specified.valueType + "'.");
                }
            }
        }
        
        return (Class<?>)resolvedMethod.getGenericReturnType();
    }

    void _serialize(StringBuilder sb) {
        identifier.serialize(sb);
        sb.append("(");
        boolean first = true;
        for (var param: specifiedParameters) {
            if (!first) sb.append(", ");
            param.serialize(sb);
        }
        sb.append(")");
    }

    // NOTE: hint_value here should be the object on which we are invoking the method call.
    Object _evaluate(Object hint_value) {
        var parameters = new ArrayList<Object>();
        for (var node: specifiedParameters) {
            parameters.add(node.evaluate(null));
        }
        try {
            return resolvedMethod.invoke(hint_value, parameters.toArray());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
