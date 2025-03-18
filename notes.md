
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

