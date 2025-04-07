
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



So In my attempt to just do a basic test, but having only one external object to reference, I created a fun little circular dependency which is actually a bit complex.
We have some object 'Obj' with members A and B.
Then we have some other variable E which is declared outside the object.
A has a dependency on E and E has a dependency on B.
Since E is outside Obj, it must refer to B as Obj.B
In resolving the Dot node, we have to first evaluate Obj since we cannot access a member of an object unless we have the object itself.
but we cannot finish evaluating Obj until we have the value of A, since A depends on E
Really, we should be able to untangle this, probably by resolving the dependency on the Dot more specifically, so that it ultimately references Obj.B directly and does not require the complete value of Obj
But do we actually care enough to make this work?
It seems like there is just too much we end up having to disentangle in order to make that happen.
To even get as far as the evaluation issue, I first had to do a bit of a hack on the typechecking to implement a separate getValueType method
    which is able to sometimes return a hinted value type before the actual typechecking for a node is fully complete
    we can do similar for evaluation, getting the object reference for the left side of a dot expression before actually evaluating that object
    but then we need some way of knowing that the referenced member has been evaluated already
        maybe we could have a step to linearize nodes as a pre-pass for evaluation, so that at least 
        could add nodes to some global array as they pass typechecking, and if I'm thinking correctly, the order would be a valid way in which to evaluate the nodes
            push nodes onto queue open starting typecheck, pop off queue and add to completed list when done
            so we track dependencies and sort nodes for evaluation in the one pass over AST

for dot nodes which are dependent only on the right side member of some object
maybe we want to have some resolveddeclaration ptr like we have for dientifiers, and just treat the dot node like an identifier to that member node
    but there could potentially be some tricky shit with how this would play with constructors or other method calls which can affect the state of an object
    the simple fact that everything in java is object oriented does not really play super nice with the declarative nature of the mappers...

whatever we ultimately decide to do for evaluation, th etypechekcing will have to match
    in the sense that we should (ideally) never typecheck successfully and then fail to evaluate
    it seems that we should probably just go ahead and separate the external / interal procs for typechecking and evaluation so that
        if we decide to do the more complex dependency evaluation, we can do so without changing the interface all over the place
    because already I need to clean up the mess I made with getValueType()
        need to figure out if there are situations where we would want to call typehceck directly instead of getValueType
            looking at NodeDeclaration line 23 for example...
        somewhat tangential, but also need to figure out more idiomatic error handling, will probabyl just hav eto use exxceptions for typechecking and evlauation errors
            at least then I'll get some stack trace I guess...
    
    














var messageType: choose {
    when $input.MTI == "0100": "Authorization Request",
    when $input.MTI == "0110": "Authorization Response",
    when $input.MTI == "0200": "Financial Request",
    when $input.MTI == "0210": "Financial Response",
}









doing another big refactor

    removing NodeMapping
    making NodeScope abstract again
    adding dependency cycle detection in typechecking
    refactoring declaration types
        will require parsing changes
    owningParser now stored on Node
        currently just so that we can access a common stack for typechecking nodes
    
    now using getValueType as common case around typecheck()
        typecheck now returns value type which gets assigned to node, or throws exception on error


input and output declarations always have their types imposed by setVariable
    this may have to change later, since I think in theory we want to be able to compile a mapper without needing to explicitly pass int he types of teh output objects
    but in any case, we will at least need to know what types we have available and what their identifiers are
    this part may require some more thinking
    
variables currently have their types inferred from the type of the valueNode
    again, it would probably help to be able to use an explicit type here, but that will require more time to think out


declaration type of FIELD is currently sort of a hack I put in place since we do not yet have the required structure to get the parent declaration's type
    we may just pass this down while parsing since we will know declaration type at parse time
    this may also help to enable/disable specific syntax at the parsing level depending on the kind of scope/declaration context we are in
    but idk, maybe this is actually fine as is for now
    



just thought of a big issue....
how to handle polymorphic fields on objects?
we would need some kind of explicit type on the field declaration in order to know which fields can validly be set in the value object node
    which seems like good reason to do the `name: Type() = value;` syntax for declarations, since that would give us syntactic space for types and constructors
I'm quite confident this is not something that the existing mapper is able to handle...


now that I've simplified the declaration syntax a bit, it should actually be relatively simple to add constructors and type expressions to declarations



we can probably remove the `=` in declarations and use only a colon, but this will require always checking for a constructor expression before a value expression, and handling the fac tthat there is no syntactic separation between the two
which again, should be fine since constructor expressions are all very simple and follow the form of `identifier(..parameters)`
the only problem we may have here is that if we want to have utility functions, either those or constructors will have to be syntactically marked as such so there is no confusion.
    because those need to be differentiable syntactically, not just in typechecking

I will leave the = for now while I implement constructors and method calls, but in the future we will want to workshop this syntax


we can probably remove the need for hint_value if we change how output objects work slightly.
    we will need explicit types on the output objects and then we can just have some getOutput() method on the parser that allows the user to retrieve results after executing the mapper
    but then again, maybe we will still need hint_value so that we can have constructors and value expressions on objects assign to the same object...




Roadmap:
    
    Constructors
        basically working, will fix things as needed
    Choice expressions
    Interfaces
    Testing
        test with nontrivial object types, various complex expressions
        make sure fully-qualified class names work
            need to figure out how just resolve identifiers as packages so that we don't need the backticked quotes parsing hack
    Integration
        clean up parser interface
        make interface type for custom member binding
        interface for exposing utility functions
    Demo?
    Transpile to Java
    Integration into MDE
    Testing
    


