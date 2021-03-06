import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a C-- program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have. All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
// Subclass Children
// -------- ----
// ProgramNode DeclListNode
// DeclListNode linked list of DeclNode
// DeclNode:
// VarDeclNode TypeNode, IdNode, int
// FnDeclNode TypeNode, IdNode, FormalsListNode, FnBodyNode
// FormalDeclNode TypeNode, IdNode
// StructDeclNode IdNode, DeclListNode
//
// FormalsListNode linked list of FormalDeclNode
// FnBodyNode DeclListNode, StmtListNode
// StmtListNode linked list of StmtNode
// ExpListNode linked list of ExpNode
//
// TypeNode:
// IntNode -- none --
// BoolNode -- none --
// VoidNode -- none --
// StructNode IdNode
//
// StmtNode:
// AssignStmtNode AssignNode
// PostIncStmtNode ExpNode
// PostDecStmtNode ExpNode
// ReadStmtNode ExpNode
// WriteStmtNode ExpNode
// IfStmtNode ExpNode, DeclListNode, StmtListNode
// IfElseStmtNode ExpNode, DeclListNode, StmtListNode,
// DeclListNode, StmtListNode
// WhileStmtNode ExpNode, DeclListNode, StmtListNode
// RepeatStmtNode ExpNode, DeclListNode, StmtListNode
// CallStmtNode CallExpNode
// ReturnStmtNode ExpNode
//
// ExpNode:
// IntLitNode -- none --
// StrLitNode -- none --
// TrueNode -- none --
// FalseNode -- none --
// IdNode -- none --
// DotAccessNode ExpNode, IdNode
// AssignNode ExpNode, ExpNode
// CallExpNode IdNode, ExpListNode
// UnaryExpNode ExpNode
// UnaryMinusNode
// NotNode
// BinaryExpNode ExpNode ExpNode
// PlusNode
// MinusNode
// TimesNode
// DivideNode
// AndNode
// OrNode
// EqualsNode
// NotEqualsNode
// LessNode
// GreaterNode
// LessEqNode
// GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, or
// internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
// IntNode, BoolNode, VoidNode, IntLitNode, StrLitNode,
// TrueNode, FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
// DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of children:
// ProgramNode, VarDeclNode, FnDeclNode, FormalDeclNode,
// StructDeclNode, FnBodyNode, StructNode, AssignStmtNode,
// PostIncStmtNode, PostDecStmtNode, ReadStmtNode, WriteStmtNode
// IfStmtNode, IfElseStmtNode, WhileStmtNode, RepeatStmtNode,
// CallStmtNode
// ReturnStmtNode, DotAccessNode, AssignExpNode, CallExpNode,
// UnaryExpNode, BinaryExpNode, UnaryMinusNode, NotNode,
// PlusNode, MinusNode, TimesNode, DivideNode,
// AndNode, OrNode, EqualsNode, NotEqualsNode,
// LessNode, GreaterNode, LessEqNode, GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

/**
 * Nodes involved in the name analysis traversal
 */
abstract interface Visitable {
    /**
     * Visit node with a state symbol table to track validity of declarations
     * 
     * @param symbolTable current scopes state to track and verify symbols
     * @throws EmptySymTableException
     */
    public void visit(SymTable symbolTable) throws EmptySymTableException;
}


/**
 * Get subtree children nodes - for further iteration over the sub-nodes
 */
abstract interface Iterable {
    /**
     * Retrieve iteration configuration for children nodes of current parent
     * node
     * 
     * @return iterator of configurations for scoping for each list of child
     *         nodes.
     */
    public Iterator<Traverser.Config> getChildren();
}


/**
 * Class holding flags related to the interfaces defined for Visitable and
 * Iterable
 * 
 * (Note: I'm not sure how to share fields of interfaces using Java, so I just
 * extended the root class)
 */
class Flag {
    // flag to indicate whether to iterate over child nodes
    public boolean traverseSubtree = true;
}


abstract class ASTnode extends Flag {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void addIndentation(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++)
            p.print(" ");
    }

    /**
     * Default implementation - used only for subclasses implementing the
     * Visitable interface
     * 
     * @param SymTable
     */
    public void visit(SymTable symbolTable) throws EmptySymTableException {
        // ignore
        // System.out.println("› visiting: " + this.getClass().getName());
    }

    /**
     * Default implementation - used by subclasses implmenting the Iterable
     * interface
     * 
     * @return
     */
    public Iterator<Traverser.Config> getChildren() {
        // empty iterator
        return (new ArrayList<Traverser.Config>()).iterator();
    }

}

// **********************************************************************
// ProgramNode, DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************


class ProgramNode extends ASTnode implements Iterable {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myDeclList));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    private DeclListNode myDeclList;
}


class DeclListNode extends ASTnode implements Iterable {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public List<DeclNode> getDeclarationList() {
        return myDecls;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println(
                    "unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        scope.add(new Traverser.Config(myDecls, false));

        return scope.iterator();
    }

    private List<DeclNode> myDecls;
}


class FormalsListNode extends ASTnode implements Iterable {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        scope.add(new Traverser.Config(myFormals, false));

        return scope.iterator();
    }

    public List<FormalDeclNode> getFormalList() {
        return myFormals;
    }

    private List<FormalDeclNode> myFormals;
}


class FnBodyNode extends ASTnode implements Iterable {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList =
                new ArrayList<>(List.of(myDeclList, myStmtList));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


class StmtListNode extends ASTnode implements Iterable {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        scope.add(new Traverser.Config(myStmts, false));

        return scope.iterator();
    }

    private List<StmtNode> myStmts;
}


class ExpListNode extends ASTnode implements Iterable {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        scope.add(new Traverser.Config(myExps, false));

        return scope.iterator();
    }

    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************


abstract class DeclNode extends ASTnode {

    /**
     * Generic implementation used by VarDeclNode & FormalDeclNode
     */
    public void visit(SymTable symbolTable, IdNode myId, TypeNode myType,
            int mySize) throws EmptySymTableException {
        super.visit(symbolTable);

        TSym symbol = null;
        TSym typeSymbol; // for struct types
        TSym localSymbol;

        // create symbol object
        switch (myType.getType()) {
            case "struct":
                typeSymbol = symbolTable
                        .lookupGlobal(((StructNode) myType).getId().getName());
                // must exist & must be of struct type
                if (typeSymbol == null || typeSymbol.getType() != "struct") {
                    ErrMsg.fatal(myId.getPosition(), 6);
                    break;
                }

                symbol = new VarSym(myType.getType(),
                        ((StructNode) myType).getId().getName(), mySize,
                        (StructSym) typeSymbol);
                break;
            case "int":
            case "bool":
                symbol = new VarSym(myType.getType());
                break;
            case "void":
            default:
                // report and skip symbol
                ErrMsg.fatal(myId.getPosition(), 5);
                break;
        }

        /**
         * - If a declaration is both "bad" (e.g., a non-function declared void)
         * and is a declaration of a name that has already been declared in the
         * same scope, you should give two error messages (first the "bad"
         * declaration error, then the "multiply declared" error).
         */
        localSymbol = symbolTable.lookupLocal(myId.getName());
        if (localSymbol != null)
            ErrMsg.fatal(myId.getPosition(), 1);

        // if no symbol was created
        if (symbol == null)
            return;

        // add declaration to symbol table
        try {
            symbolTable.addDecl(myId.getName(), symbol);
        } catch (DuplicateSymException e) {
            // suppress (handled by a previous check)
        }
    }

}


class VarDeclNode extends DeclNode implements Visitable {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }

    public void visit(SymTable symbolTable) throws EmptySymTableException {
        int mySize = -1; // unknown size
        super.visit(symbolTable, myId, myType, mySize);
    }

    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}


class FnDeclNode extends DeclNode implements Visitable, Iterable {
    public FnDeclNode(TypeNode type, IdNode id, FormalsListNode formalList,
            FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent + 4);
        p.println("}\n");
    }

    public void visit(SymTable symbolTable) throws EmptySymTableException {
        super.visit(symbolTable);

        ArrayList<String> paramTypeList = new ArrayList<>();
        for (FormalDeclNode f : myFormalsList.getFormalList())
            paramTypeList.add(f.getType());
        FnSym symbol = new FnSym(paramTypeList, myType.getType());

        // add declaration to symbol table
        try {
            symbolTable.addDecl(myId.getName(), symbol);
        } catch (DuplicateSymException e) {
            ErrMsg.fatal(myId.getPosition(), 1);
        }
    }


    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myFormalsList, myBody));
            scope.add(new Traverser.Config(nodeList, true));
        }

        return scope.iterator();
    }

    private TypeNode myType; // return type of function
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}


class FormalDeclNode extends DeclNode implements Visitable {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public String getType() {
        return myType.getType();
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    public void visit(SymTable symbolTable) throws EmptySymTableException {
        int mySize = -1; // unknown size
        super.visit(symbolTable, myId, myType, mySize);
    }

    private TypeNode myType;
    private IdNode myId;
}


/**
 * - a recommended approach is to have a separate symbol table associated with
 * each struct definition and to store this symbol table in the symbol for the
 * name of the struct type.
 */
class StructDeclNode extends DeclNode implements Visitable, Iterable {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
        p.println("{");
        myDeclList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("};\n");

    }

    public void visit(SymTable symbolTable) throws EmptySymTableException {
        // Override subtree traversal - do not traverse over child nodes as part
        // of the main traversal
        this.traverseSubtree = false;

        super.visit(symbolTable);

        // validate struct type Id/name
        TSym localSymbol = symbolTable.lookupLocal(myId.getName());
        if (localSymbol != null) {
            ErrMsg.fatal(myId.getPosition(), 1);
            return;
        }

        // create struct fields symbol table
        SymTable fieldST = new SymTable(1); // table with no scopes
        // link global to the struct local symbol table for providing access to
        // the main symbol tables chain
        fieldST.linkScope(symbolTable);
        // iterate over field child nodes and add the to the fields table
        Traverser.traverseConfig(this.getChildren(), fieldST);
        // create the struct symbol
        StructSym symbol = new StructSym(myId.getName(), fieldST);

        // add declaration to main symbol table
        try {
            symbolTable.addDecl(myId.getName(), symbol);
        } catch (DuplicateSymException e) {
            // suppress - handled by a previous check
        }
    }

    /**
     * NOTE: This method is not used in the main iteration of the AST struct
     * node, only internally by visit method. The way it is avoided is by
     * setting the "traverseSubtree" flag to false, thus marking that this
     * node's childrens shouldn't be iterated over as part of the node's
     * traversal.
     */
    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        { // fields nodes
            ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myDeclList));
            scope.add(new Traverser.Config(nodeList, false));
        }

        return scope.iterator();
    }

    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************


abstract class TypeNode extends ASTnode {
    /**
     * String representing the type
     * 
     * @return int | bool | void | struct
     */
    abstract public String getType();
}


class IntNode extends TypeNode {
    private static String type = "int";

    public String getType() {
        return type;
    }

    public IntNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}


class BoolNode extends TypeNode {
    private static String type = "bool";

    public String getType() {
        return type;
    }

    public BoolNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(type);
    }
}


class VoidNode extends TypeNode {
    private static String type = "void";

    public String getType() {
        return type;
    }

    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(type);
    }
}


class StructNode extends TypeNode {
    private static String type = "struct";

    public String getType() {
        return type;
    }

    public IdNode getId() {
        return myId;
    }

    public StructNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(type + " ");
        myId.unparse(p, 0);
    }

    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************


abstract class StmtNode extends ASTnode {
}


class AssignStmtNode extends StmtNode implements Iterable {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myAssign));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    private AssignNode myAssign;
}


class PostIncStmtNode extends StmtNode implements Iterable {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
        scope.add(new Traverser.Config(nodeList, false));
        return scope.iterator();
    }

    private ExpNode myExp;
}


class PostDecStmtNode extends StmtNode implements Iterable {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    private ExpNode myExp;
}


class ReadStmtNode extends StmtNode implements Iterable {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    // 1 child (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}


class WriteStmtNode extends StmtNode implements Iterable {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    private ExpNode myExp;
}


class IfStmtNode extends StmtNode implements Iterable {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        {
            ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
            scope.add(new Traverser.Config(nodeList, false));
        }
        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myDeclList, myStmtList));
            scope.add(new Traverser.Config(nodeList, true));
        }

        return scope.iterator();
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


class IfElseStmtNode extends StmtNode implements Iterable {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1, StmtListNode slist1,
            DeclListNode dlist2, StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
        addIndentation(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        {
            ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
            scope.add(new Traverser.Config(nodeList, false));
        }
        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myThenDeclList, myThenStmtList));
            scope.add(new Traverser.Config(nodeList, true));
        }
        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myElseStmtList, myElseDeclList));
            scope.add(new Traverser.Config(nodeList, true));
        }
        return scope.iterator();
    }

    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}


class WhileStmtNode extends StmtNode implements Iterable {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        {
            ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
            scope.add(new Traverser.Config(nodeList, false));
        }
        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myDeclList, myStmtList));
            scope.add(new Traverser.Config(nodeList, true));
        }

        return scope.iterator();
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


class RepeatStmtNode extends StmtNode implements Iterable {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("repeat (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        {
            ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
            scope.add(new Traverser.Config(nodeList, false));
        }
        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myDeclList, myStmtList));
            scope.add(new Traverser.Config(nodeList, true));
        }

        return scope.iterator();
    }

    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


class CallStmtNode extends StmtNode implements Iterable {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myCall));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    private CallExpNode myCall;
}


class ReturnStmtNode extends StmtNode implements Iterable {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        if (myExp != null) {
            ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
            scope.add(new Traverser.Config(nodeList, false));
        }

        return scope.iterator();
    }

    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************


abstract class ExpNode extends ASTnode {
}


class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}


class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}


class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myCharNum;
}


class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}


class IdNode extends ExpNode implements Visitable {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * - change the unparse method so that every use of an ID has its type (in
     * parentheses) after its name. (The point of this is to help you to see
     * whether your name analyzer is working correctly; i.e., does it correctly
     * match each use of a name to the corresponding declaration, and does it
     * correctly set the link from the IdNode to the information in the symbol
     * table.)
     */
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);

        if (symbol != null)
            p.print("(" + symbol.toString() + ")");
    }

    public String getName() {
        return myStrVal;
    }

    public int[] getPosition() {
        return new int[] {myLineNum, myCharNum};
    }

    public TSym getSymbol() {
        return this.symbol;
    }

    /**
     * Link the current reference with the declaration symbol
     * 
     * @param s declaration symbol found matching the name reference
     */
    public void setSymbol(TSym s) {
        this.symbol = s;
    }

    public void visit(SymTable symbolTable) throws EmptySymTableException {
        TSym s = symbolTable.lookupGlobal(this.getName());
        if (s == null)
            ErrMsg.fatal(this.getPosition(), 2);
        else
            setSymbol(s);

    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    // link to type symcorresponding to this name node declaration. (to link
    // the node with the corresponding symbol-table entry)
    private TSym symbol = null;
}


class DotAccessExpNode extends ExpNode implements Visitable {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        if (!(this.myLoc instanceof IdNode))
            p.print("(");
        myLoc.unparse(p, 0);
        if (!(this.myLoc instanceof IdNode))
            p.print(")");
        p.print(".");
        myId.unparse(p, 0);
    }

    public IdNode getId() {
        return myId;
    }

    /**
     * Get LHS IdNode
     */
    private IdNode getLHS_IdNode(ExpNode lhs) {
        if (lhs instanceof DotAccessExpNode)
            return ((DotAccessExpNode) lhs).getId();
        else
            return ((IdNode) lhs);
    }

    /**
     * Get LHS symbol
     */
    private TSym getLHS_symbol(ExpNode lhs) {
        IdNode id = getLHS_IdNode(lhs);
        return id.getSymbol();
    }

    public void visit(SymTable symbolTable) throws EmptySymTableException {
        ExpNode lhs = myLoc;
        IdNode rhs = (IdNode) myId;

        // visit first the left expression/Id nodes to fill in the LHS symbols
        lhs.visit(symbolTable);

        TSym symbol = getLHS_symbol(lhs);

        // if LHS not a StructSym object
        if (symbol == null) // undeclared
            return; // do not proceed with fields validation
        if (!(symbol instanceof VarSym) || symbol.getType() != "struct") {
            ErrMsg.fatal(getLHS_IdNode(lhs).getPosition(), 3);
            return;
        }

        // get corresponding struct declaration symbol to validate fields
        StructSym structSymbol = ((VarSym) symbol).getStructDeclSym();
        SymTable structField = structSymbol.getField(); // get field table
        // check RHS in LHS symbol table
        TSym s = structField.lookupLocal(rhs.getName());

        if (s == null)
            ErrMsg.fatal(getLHS_IdNode(lhs).getPosition(), 4);
        else
            rhs.setSymbol(s);
    }

    private ExpNode myLoc;
    private IdNode myId;
}


class AssignNode extends ExpNode implements Iterable {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)
            p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)
            p.print(")");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myLhs, myExp));
            scope.add(new Traverser.Config(nodeList, false));
        }

        return scope.iterator();
    }

    private ExpNode myLhs;
    private ExpNode myExp;
}


class CallExpNode extends ExpNode implements Iterable {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        {
            ArrayList<ASTnode> nodeList =
                    new ArrayList<>(List.of(myId, myExpList));
            scope.add(new Traverser.Config(nodeList, false));
        }

        return scope.iterator();
    }

    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}


abstract class UnaryExpNode extends ExpNode implements Iterable {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    protected ExpNode myExp;
}


abstract class BinaryExpNode extends ExpNode implements Iterable {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public Iterator<Traverser.Config> getChildren() {
        ArrayList<Traverser.Config> scope = new ArrayList<>();

        ArrayList<ASTnode> nodeList = new ArrayList<>(List.of(myExp1, myExp2));
        scope.add(new Traverser.Config(nodeList, false));

        return scope.iterator();
    }

    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************


class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}


class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************


class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}


class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
