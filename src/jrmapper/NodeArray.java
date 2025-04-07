// /*
//     A NodeArray represents a structured aggregate type, akin to a JSON Array.
//     The sub-nodes of a NodeArray are all general expressions.
// */

// import java.util.ArrayList;

// public class NodeArray extends NodeScope {
//     NodeArray(Parser owningParser, NodeScope parent, Token token) {
//         super(owningParser, parent, token);
//     }

//     ArrayList<Node> valueNodes;

//     boolean typecheck(Class hint_type) {
//         for (var node: valueNodes) {
//             if (!node.typecheck(hint_type)) {

//             }
//         }
//         valueType = hint_type;
//         flags.add(Flags.TYPECHECKED);
//         return true;
//     }

//     void serialize(StringBuilder sb) {
//         sb.append("[\n");
//         for (var node: valueNodes) {
//             node.serialize(sb);
//             sb.append(",\n");
//         }
//         sb.append("]");
//         return true;
//     }

//     Object evaluate(Object hint_value) {
//         assert(flags.contains(Flags.TYPECHECKED));
//         // depending on valueType determined during typechecking, we may need to append elements differently
//         // or maybe there's some general enough container methods that we can call, since basically all we need is some .append() method
//         // iterate over all fields, executing them and assigning their results to the correct members of 'value'
//         return null;
//     }
// }
