
## DOM Notes

In addition to the Node types which are used for general expression parsing and evaluation, 
we have a certain subset of Nodes which are more akin to JSON Nodes than they are to traditional AST expression nodes.

The main node of this variety is the NodeField. 
This is the basic structural node in our mapper, which represents essentially an object or object member value.
For aggregates we have the NodeObject and NodeArray, which correspond to JSON objects and arrays accordingly.


### Node Types

#### Expression Nodes
- NodeNumber
- NodeString
- NodeOperation
- NodeMethodCall
- NodeIdentfier

#### Declaration Nodes
- NodeDeclaration
  - Node

    
#### Mapping Nodes
Mapping nodes all correspond directly to data in the output object(s) or to the values of variables.

## Document Structure

### Top-Level Declarations

#### Mapping Metadata


#### Input and Output Objects
All input and output objects must be declared explicitly. 
There can be multiple input and output objects.

#### Variables 
I am currently thinking that variables should be presumed constant so that the entire language is declarative. 
This may prove to be too restrictive, such that it precludes certain important use cases, 
but for now I'd like to pursue it this way so that we can potentially support order-independent evaluation of the mapping.

### Structured Data
The bulk of the mapping file is structured data that represents the output object or objects.
This structured data is made up of structural nodes, which all inherit from NodeMapping.

#### Rambling
Currently, I can't quite figure out what is the optimal design for the data nodes
I am currently using a NodeMapping as a base type that NodeObject, NodeArray, and NodeField extend,
however the distinction of fields, objects, and arrays is not quite as simple as in JSON
because I think in this case we also wan tot be able to use the object and array nodes within general expressions
this will allow us to use some choice expression to select between multiple possible object nodes
For now, I think I'll just do that in whatever way seems most straightforward, 
but we may have to refactor halfway through if things don't work out nicely...

### Expressions
The language will support generalized expression parsing and evaluation.
All expressions must be typechecked before they can be evaluated.
Currently, only the basic numeric types and String are supported, but eventually objects and arrays will be supported as well.


### Declarations

Three types:
- Var
- Input
- Output

Var declares a new variable and assigns a value in the same statement.
Variables can only be assigned once and are then immutable.
The types of variables are currently inferred from the initialization expression.

Input declares a variable whose value will be provided externally to the mapper. 
The type need not be provided explicitly in the mapper file, but it must be known at compile time.

Output declares an output object which can be assigned to in the main body of the mapper file.
`ouput OutputObject,`


## Random Questions

Do we allow general declarations (like variables) to be declared within the scope of a NodeMapping?
    probably yes, but this will require some thought.

If we use NodeObject and NodeArray in the context of a choice expression, there's no direct output value we can map to that node...
    do we just propagate the eventual NodeField's output mapping up to the object/array nodes in the choice?
    or do we just resolve them simply to objects in evaluate and pass them back up
        probably this, we can just know to pass up the value if there's not internal/output binding set 

## TODO

implement some means to poke identifiers into the Mapping context
    input and output declarations
        declaration is separate from use as in mapping node
implement member access with dot operator
implement method access and evaluation

need to create some helper function to get fields on a class bc getDeclaredFields does not return inherited fields and getFields only returns public fields.
    so we will need to make out own version which actually gets all fields on an object.

may be able to implement separate declaration / definition of output object by using a real identifier node on NodeMapping instead of just storing name as string
then we can resolve the bare identifier to the output node to get type 
maybe then constructor can be done on initial declaration and setting of additional members done in body of transform

will need to be able to resolve identifiers of types/constructors
maybe we should require that output object of transform calls a constructor for the object

if we make the language entirely declarative, another benefit is that we will be able to optimize certain expressions by pre-evaluating the results
or by simply inlining referenced nodes in place of their identifiers
    for example, with numbers, we can coerce them at the usage site to whatever specific number type is desired
    but, on second thought, maybe we don't want to do this if it would make the java code we output more obscure.
    it will probably be easier to debug through the emitted code if we just leave the identifiers in place.
    in any case, we can make this sort of feature optional/configurable
        maybe we want to enable some inlining or pre-evaluation (e.g. string concatenation) but not enable number inlining


provide warnings and error messages for
    variable shadowing
    circular references
    use of variable in its own declaration


I would like to probably simplify the ontology of nodes a bit
    maybe mapping and declaration can just be the same thing, with an enum to denote declaration type (variable, input, output)
    or we should make the declaration abstract, implemented by variable/input/output subclasses

with a nodemapping being a subclass of declaration, we now have a slight conundrum for output objects
    if we have a nodemapping, then I suppose we just lookup the identifier as an output object and somehow link those together
    maybe we just remove nodemapping and replace it with a nodeoutputDeclaration

the output definition must then link back to the output declaration

perhaps we can have a statement type which can be a declaration or definition


before refactoring how nodes are structured, we need to establish the semantics of declarations 
    which means figuring out how we want to do constructors 

we could use walrus operator syntax to provide type/constructor separate from value

field_name: Object() = {};

or 

field_name: Object();
field_name = {};

if we have slots on some mapping or declaration node for both type/constructor and value, then when we resolve a mapping value
we can just stick that mapping node onto the original declaration, even if the two are lexically separated
we could even do lexically split or multiple assignment expressions, so long as individual fields are not multiply assigned
but maybe this would just be too weird in both semantics and implementation...

field_name = { a = 5 };
field_name = { b = 3 };



NodeMapping
    Class value_type;
    Node type_expression;
    ArrayList<Node> constructor_parameters;
    Node value_expression;


Get rid of getValueType(), have typecheck() return the type
    Or, make getValueType() common to all nodes and make typecheck() private
    that way, we have only a single entry point for nodes to get types from one antoerh
    then, make getValueType and typecheck push/pop from a stack of dependent nodes
    that way we can detect and report dependency cycles

I can't really figure out a proper solution until I talk with Nathan about how they currently deal with constructors
it may be the case that they just don't really use constructors in mappers (which would honestly be preferable)



do something else for now then..
dot member access

running into some interesting problems with cycles here

in typehcekcing, we have these weird loops were in some cases we are fine to get a value type from a node bfore that node is actually flagged as typechecked

likewise, we will want to be able to get an object reference for an object which has not yet completed its evaluation.
    this one is a bit more problematic, since we don't pre-allocate everything as we do for data bindings in GON / LSD










