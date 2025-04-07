package jrmapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeMethodCall extends Node {
    public NodeMethodCall(Parser owningParser, NodeScope parent, Token token) {
        super(owningParser, parent, token);
    }

    public NodeIdentifier  identifier;
    public Method          resolvedMethod;
    public Constructor<?>  resolvedConstructor;
    public ArrayList<Node> specifiedParameters;

    public static ArrayList<Method> getAllObjectMethods(Class<?> type) {
        var methods = new ArrayList<Method>();
        if (type != null) {
            methods.addAll(Arrays.asList(type.getDeclaredMethods()));
            for (var anInterface: type.getInterfaces()) {
                var interface_methods = getAllObjectMethods(anInterface);
                methods.addAll(interface_methods);
            }
            var interface_methods = getAllObjectMethods(type.getSuperclass());
            methods.addAll(interface_methods);
        }
        return methods;
    }

    // NOTE: hint_type here is the base object type, not the result type.
    public Class<?> _typecheck(Class<?> hint_type) {
        for (var node: specifiedParameters) {
            node.tryTypecheck(null);
        }

        // if hint_type here is null, then this method call is actually a constructor and should be resolved as such
        if (hint_type == null) {
            try {
                valueType = Class.forName(identifier.name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            var constructors = Arrays.asList(valueType.getConstructors());

            // filter by method name and number of parameters
            // this feels bad... there's gotta be a better way to do this.
            // can't we just get the stream back to an arraylist?
            {
                constructors = new ArrayList<>(
                        constructors.stream().filter(
                                m -> m.getName().equals(identifier.name)
                                        && m.getParameterCount() == specifiedParameters.size()
                        ).toList()
                );
            }

            System.out.println("contructors: " + constructors);

            if (constructors.size() > 1) {
                // now for manual filtering, we want to check that all parameters for which we already have resolved types are valid
                var it_method = constructors.iterator();
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

            if (constructors.isEmpty()) {
                throw new RuntimeException(location() + ": Error: unable to resolve contructor for '" + identifier.name + "'.");
            }

            assert(constructors.size() == 1);
            resolvedConstructor = constructors.getFirst();

            // do second pass over arguments to verify types and re-typecheck with new type hint if necessary
            var resolved_formal_parameters = List.of(resolvedConstructor.getGenericParameterTypes());
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
            return valueType;
        }


        var methods = getAllObjectMethods(hint_type);

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

    public void _serialize(StringBuilder sb) {
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
    public Object _evaluate(Object hint_value) {
        var parameters = new ArrayList<Object>();
        for (var node: specifiedParameters) {
            parameters.add(node.evaluate(null));
        }
        try {
            if (resolvedConstructor != null) {
                return resolvedConstructor.newInstance(parameters.toArray());
            } else {
                return resolvedMethod.invoke(hint_value, parameters.toArray());
            }
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
