/*
  This file is part of AstC2C.

  Copyright (c) STMicroelectronics, 2013.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of STMicroelectronics nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  Authors: Thierry Lepley
*/

/* ******************************************************************
DESCRIPTION:
   Grammar working on the AST Tree which builds the table of symbol
   with associates type.
   + It performs checks related to symbols and type
   + It propagates types over expression
   + It propagates constant values over expressions
   + It decorates ID nodes with a reference to the corresponding
     symbol and type
****************************************************************** */

header {
package parser;
import ir.base.*;
import ir.literals.*;
import ir.literals.c.*;
import ir.literals.ocl.*;
import ir.symboltable.*;
import ir.symboltable.symbols.*;
import ir.types.*;
import ir.types.c.*;
import ir.types.c.Void;
import ir.types.ocl.*;
import builtins.BuiltinManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.math.BigInteger;

import common.CompilerError;
}

class SymbolTableBuilder extends TreeParser;

options {
  importVocab  = GNUC;
  buildAST     = true;
  ASTLabelType = "NodeAST";
}
{
  // Associate an external error module to the tree parser
  public SymbolTableBuilder(CompilerError cp) {
    compilerError = cp;
    // Set AST node type to NodeAST or it does not work (a voir ...)
    setASTNodeClass(NodeAST.class.getName());
    // default is C language
    builtinManager = BuiltinManager.getFromName("C");
  }

  // ##################################################################
  // Various variables
  // ##################################################################

  // Error manager
  private CompilerError compilerError = null;

  // Type manager
  private TypeManager typeManager = new TypeManager();
  
  // Builtin function manager (by default C language)
  private BuiltinManager builtinManager=null;

  // Variable containing the return type of the function. It is used
  // for type checking of 'return' statements in the function body
  private Type currentFunctionDefReturnType=null;

  // Current function information
  private boolean currentFunctionIsKernel=false;
  protected Symbol currentFunctionSymbol=null;

  // For declaration spliting
  boolean is_for_declaration=false;



  // ##################################################################
  // Stack size computation (OpenCL related)
  // ##################################################################

  private boolean unsized_stack_requested=false;

  public boolean isUnsizedStackSizeRequested() {
    return unsized_stack_requested;
  }


  // ##################################################################
  // Input language management
  // ##################################################################

  private boolean oclLanguage = false;
  private boolean oclCCompatibilityOption = false;

  public void setOclCCompatibility() {
    oclCCompatibilityOption=true;
  }

  public void setOclLanguage() {
    oclLanguage=true;
    typeManager.setOclLanguage();
    // Switch to OpenCL builtins
    builtinManager
    = BuiltinManager.getFromName("OpenCL");
  }
  
  private boolean vxLanguage = false;
  
  public void setVxLanguage() {
    vxLanguage=true;
    typeManager.setVxLanguage();
    // Switch to OpenCL builtins
    builtinManager
    = BuiltinManager.getFromName("OpenVX");
  }
  

  // ##################################################################
  // Public execution interface
  // ##################################################################

  // ******************************************************************
  // run :
  //
  // Runs the tree parser builds a symbol table, and annotates the AST
  // with references to the symbol table
  // ******************************************************************
  public void run(AST tree) {
    try { translationUnit(tree); }
    catch (Exception e) { 
        System.err.println("Fatal error while building the symbol table:\n"+e);
        e.printStackTrace();
        System.exit(1);
    }
    compilerError.exitIfError();
  }

  // ******************************************************************
  // runSplit :
  //
  // Runs the tree parser, split declarations that can be split (out of
  // expressions), builds a symbol table, and annotate the AST with
  // references to the symbol table
  // ******************************************************************
  public void runSplit(AST tree) {
    is_split = true;
    run(tree);
  }

  // ******************************************************************
  // runExtraction :
  //
  // Runs the tree parser, split declarations that can be split (out of
  // expressions), builds a symbol table, annotate the AST with
  // references to the symbol table and extract instance data (global
  // scope + static local)
  // ******************************************************************
  public void runExtraction(AST tree) {
    //  public void runExtraction(AST tree) {
    instance_data_extraction = true;

    // Build the symbol table and split declarations
    runSplit(tree);

    // Continue the processing
    extractTypeTagsForInstanceData();
  }


  // ##################################################################
  // Instance data extraction management
  // ##################################################################

  protected boolean instance_data_extraction = false;

  // ******************************************************************
  // isExtractionMode :
  //
  // Returns 'true' if we are in 'instance data extraction' mode
  //
  // ******************************************************************
  protected boolean isExtractionMode() {
    return(instance_data_extraction);
  }


  // ******************************************************************
  // Exctraction lists
  // ******************************************************************
  protected LinkedList<Symbol> extracted_type_tag_list 
    = new LinkedList<Symbol>();
  protected LinkedList<ObjectLabel> instance_data_list 
    = new LinkedList<ObjectLabel>();
  protected LinkedList<ObjectLabel> extern_declaration_list
    = new LinkedList<ObjectLabel>();

  public LinkedList<Symbol> getExtractedTypeTagList() {
    return(extracted_type_tag_list);
  }
  public LinkedList<ObjectLabel> getInstanceDataList() {
    return(instance_data_list);
  }
  public LinkedList<ObjectLabel> getExternDeclarationList() {
    return(extern_declaration_list);
  }


  // ******************************************************************
  // isInstanceDataDefinition :
  //
  // Returns 'true' if the symbol is an instance data 'definition' 
  // (not declaraton)
  // ******************************************************************
  protected boolean isInstanceDataDefinition(Symbol symbol) {
    // Only extract definition or declaration of instance data
    if (symbol instanceof ObjectLabel) {

      if (symbol.getScopeDepth()==0) {
	// This is a global variable definition or declaration
	if (
	    (symbol.isExtern())
	    )  {
	  // + It's an external data 'declaration' +
	  // We do not know yet if it is the declaration to an instance
	  // data or to a global variable (which is not defined in the
	  // component)
	  // -> We will know at the component link stage
	  return(false);
	}
	// + It's a global variable 'definition' (static or not) +
	// It is the definition of an instance data which must be extracted
	return(true);
      }
      else {
	// This is a local variable (function parameter or compound variable)
	if (symbol.isStatic()) {
	  // + It's a static local variable definition +
	  // It is the definition of an instance data which must be extracted
	  return(true);
	}
	else if (symbol.isExtern()) {
	  // it's an external declaration in a local scope
	  // -> It is the declaration of a variable at scope 0
	  //    (global or static)
	  //    It must not be extracted
	  // ex: 
	  // static int a=2;
	  // int main() {
	  //   int a=1;
	  //   if (a) {    /* references the local variable */
	  //    extern int a;
	  //    return(a); /* references the static variable */
	  //  }
	  // If we remove the extern:
	  // static int a=2;
	  // int main() {
	  //   int a=1;
	  //   if (a) {    /* references the local variable */
	  //    return(a); /* references the local variable */
	  //  }
	  return(false);
	}
	// It's a local scope variable definition
	return(false);
      }
    } // Otherwhise, it is necessarily a Typedef
    return(false);
  }


  // ******************************************************************
  // extractInstanceData :
  //
  // Function which mangles instance data (for local extraction) and
  // put the AST declaration to relevant global lists of the parser:
  //  - 'extern_declaration_list' for an extern declaration
  //  - 'instance_data_list' for an instance data declaration
  //
  // Note: The function assumes that the symbol has all relevant
  //       information set
  // ******************************************************************
  protected void extractInstanceData(Symbol symbol) {
    if (!instance_data_extraction) {
      InternalError("(extractInstanceData) 1");
    }
    if (symbol instanceof ObjectLabel) {
      ObjectLabel obj_symbol=(ObjectLabel)symbol;
      
      // This is a variable or an array
      if (obj_symbol.getScopeDepth()==0) {
	// This is a global variable
	if (
	    (obj_symbol.isExtern())
	    )  {
	  InternalError("(extractInstanceData) 2");
	}
	else {
	  // This is global variable declaration (static or not)
	  // -> This is the declaration of an instance data
	  if (obj_symbol.isStatic()) {
	    // Mangle it
	    obj_symbol.reName(getMangling(obj_symbol.getName()));
	    // remove static information from the declaration
	    removeStatic(obj_symbol.getDeclarationNode());
	  }

	  // keep the declaration
	  instance_data_list.add(obj_symbol);
	}
      }

      else {
	// This is a local variable (to function or compound)
	if (obj_symbol.isStatic()) {
	  // This is a static local variable declaration
	  // -> This is the declaration of an instance data

	  // Perform local mangling, since the declaration is extracted
	  // from its local scope to the global scope
	  obj_symbol.reName(getMangling(obj_symbol.getName()));
	  obj_symbol.setScopeDepth(0);
	  obj_symbol.setFunctionScope(currentFunctionSymbol);
	  // keep the declaration
	  instance_data_list.add(obj_symbol);
	  // remove static information from the declaration
	  removeStatic(obj_symbol.getDeclarationNode());
	}
	else if (obj_symbol.isExtern()) {
	  InternalError("(extractInstanceData) 3");

	}
	else {
	  InternalError("(extractInstanceData) 4");
	}
      }
    } // Otherwhise, it is necessarily a Typedef
    else {
      InternalError("(extractInstanceData) 5");
    }
  }


  // ##################################################################
  // Builtin functions management
  // ##################################################################

  private boolean is_external_builtin_mode = false;

  public boolean isExternalBuiltinMode() {
    return is_external_builtin_mode;
  }
  public void setExternalBuiltinMode() {
    is_external_builtin_mode=true;
  }
  public void unsetExternalBuiltinMode() {
    is_external_builtin_mode=false;
  }


  // ##################################################################
  // Mangled functions management
  // ##################################################################

  private boolean is_mangling_mode = false;

  public void setManglingMode() {
    is_mangling_mode=true;
  }
  public void unsetManglingMode() {
    is_mangling_mode=false;
  }


  // ##################################################################
  // CL extensions
  // ##################################################################

  private boolean cl_ext_private_vla = false;

  public void setCLExtPrivateVariableLengthArray() {
    cl_ext_private_vla=true;
  }
  public boolean isCLExtPrivateVariableLengthArray() {
    return cl_ext_private_vla;
  }


  // ##################################################################
  // Declaration split management
  // ##################################################################

  private boolean is_split = false;

  // ******************************************************************
  // isDeclarationSplitRequested :
  //
  // Returns true if the declaration split has been requested
  // ******************************************************************
  boolean isDeclarationSplitRequested() {
    return is_split;
  }


  // ******************************************************************
  //  extractTypeTagsForInstanceData:
  //
  // Generate the AST for type tags declarations necessary to instance
  // data declarations
  // ******************************************************************
  private void extractTypeTagsForInstanceData() {
    HashSet<Symbol>    to_schedule
      = new HashSet<Symbol>();
    LinkedList<Symbol> schedulable
      = new LinkedList<Symbol>();
    LinkedList<Symbol> ordered_list
      = new LinkedList<Symbol>();
    
    // Insert type tags necessary for instance data declaration
    for(ObjectLabel symb:instance_data_list) {
      putParentsInSet(to_schedule,symb);
    }

    // Schedule 'type tags' declaration into 'ordered_list'
    while(!to_schedule.isEmpty()) {
      // Update schedulable
      for(Symbol symb:to_schedule) {
	java.util.Vector<Symbol> parents=symb.getParents();

	// Note: builtin types are not in the list. Since we just
	// look at 'to_schedule' nodes, missing parents do not
	// end-up to deadlock 
	boolean b=true;
	if (parents!=null) {
	  for(Symbol dep:parents) {
	    if (to_schedule.contains(dep)) {
	      // not schedulable yet
	      b=false;
	      break;
	    }
	  }
	}
	if (b) {
	  // The node is schedulable

	  // Extract only user defined types (not builtin types)
	  if (
	      (!(symb instanceof Typedef)) ||
	      (!(((Typedef)symb).isExternalBuiltinType()))
	     ) {
	    schedulable.add(symb);
	  }
	}
      }
      for(Symbol symb:schedulable) {
	to_schedule.remove(symb);
	ordered_list.add(symb);
      }
      schedulable.clear();
    }


    // Extract the declaration from the AST
    for(Symbol symb:ordered_list) {
      if (symb instanceof FunctionLabel) {
	FunctionLabel func_symb=(FunctionLabel)symb;
	if (func_symb.isStatic()) {
	  if (func_symb.isPrototype()) {
	    // the symbol moves to a upper scope, rename it
	    moveSymbolDeclaration(func_symb,0);

	    // The extracted function can not be static and anyway does not
	    // need to be static anymore
	    removeStatic(func_symb.getDeclarationNode());

	    // Substitute the declaration in the AST by a #[NNoDeclaration]
	    // node
	    NodeAST decl=func_symb.getDeclarationNode();
	    NodeAST new_decl_head   = (NodeAST) astFactory.dup(decl);
	    NodeAST new_declaration =  #( new_decl_head, decl.getFirstChild());
	    func_symb.setDeclarationNode(new_declaration);
	    decl.setType(NNoDeclaration);
	    decl.setFirstChild(null);
	  }
	  else {
	    // [TBW] not implemented yet
	    InternalError(func_symb.getDeclarationNode(),
			  "Function proto/def extraction not implemented yet");
	  }
	}
	// Else no need for mangling nor duplication
      }
      else {
	// the symbol moves to a upper scope, rename it
	moveSymbolDeclaration(symb,0);

	// Substitute the declaration in the AST by a #[NNoDeclaration] node
	NodeAST decl=symb.getDeclarationNode();
	NodeAST new_decl_head   = (NodeAST) astFactory.dup(decl);
	NodeAST new_declaration =  #( new_decl_head, decl.getFirstChild());
	symb.setDeclarationNode(new_declaration);
	decl.setType(NNoDeclaration);
	decl.setFirstChild(null);
      }
    }


    for(Symbol symb:ordered_list) {
      extracted_type_tag_list.add(symb);
    }
  }

  // ******************************************************************
  // removeStatic
  //
  // Remove the static node from the AST tree given in parameter
  // Since the AST is not doubly chained, the easiest way to remove
  // the node is to keep it and set an empty text (for C regeneration)
  //
  // ******************************************************************
  private void removeStatic(NodeAST tn) {
    Symbol s;

    // Processes the node
    if (tn.getType()==LITERAL_static) {
      tn.setText("");
    }
    // Process children
    if (tn.getFirstChild()!=null) {
      removeStatic((NodeAST)tn.getFirstChild());
    }
    // Process siblings
    if (tn.getNextSibling()!=null) {
      removeStatic((NodeAST)tn.getNextSibling());
    }
  }



  // ##################################################################
  // buildGlobalAST 
  // ##################################################################

  private void putInSet(HashSet<Symbol> hs, Symbol symbol) {
    if (hs.contains(symbol)) {
      // the symbol already in the table
      return;
    }

    // Put only non builtin types
    if (
	(!(symbol instanceof Typedef)) ||
	(!(((Typedef)symbol).isExternalBuiltinType()))
	) {
      // Put it now to prevent problems with cycles (struct A {struct A*a;} )
      hs.add(symbol);

      // Put parents
      java.util.Vector<Symbol> parents=symbol.getParents();
      if (parents!=null) {
	for(Symbol p:parents) {
	  putInSet(hs,p);
	}
      }
    }
  }

  private void putParentsInSet(HashSet<Symbol> hs, Symbol symbol) {
    java.util.Vector<Symbol> parents=symbol.getParents();
    if (parents!=null) {
       for(Symbol p:parents) {
	putInSet(hs,p);
      }
    }
  }

  private void putBrothersInSet(HashSet<Symbol> hs, Symbol symbol) {
    java.util.Vector<Symbol> brothers=symbol.getBrothers();
    if (brothers!=null) {
      for(Symbol b:brothers) {
        if (b!=symbol) {
          hs.add(b);
        }
      }
    }

    // Put parents
    java.util.Vector<Symbol> parents=symbol.getParents();
    if (parents!=null) {
      for(Symbol p:parents) {
        putBrothersInSet(hs,p);
      }
    }
  }

 
  // ******************************************************************
  // getParents
  //
  // Put in 'v' compile time resolvable symbols referenced in the
  // 'tn' AST (Tags, Typedef, function label)
  //
  // ******************************************************************
  private void getParents(java.util.Vector<Symbol> v, NodeAST tn) {
    Symbol s;

    // Processes the node
    while(tn!=null) {
	s=tn.getReference();
	if (s!=null) {
	    // Takes the enum for the enum field
	    if (s instanceof EnumConstant) {
		s=s.getParents().firstElement();
	    }
	    // Only put compile known symbols
	    if ( (s instanceof TagSymbol) ||
		 (s instanceof Typedef) ||
		 (s instanceof FunctionLabel)) {
		v.add(s);
	    }
	}
	// Process children
	if (tn.getFirstChild()!=null) {
	    getParents(v,(NodeAST)tn.getFirstChild());
	}

	// Next sibling
	tn=(NodeAST)tn.getNextSibling();
    }
  }


  // ******************************************************************
  // getParentForInitializers
  //
  // Put in 'v' all symbols referenced in the 'tn' AST
  //
  // ******************************************************************
  private void getParentsFromInitializer(java.util.Vector<Symbol> v, NodeAST tn) {
    Symbol s;

    // Processes the node
    while(tn!=null) {
      s=tn.getReference();
      if (s!=null) {
	// Takes the enum for the enum field
	if (s instanceof EnumConstant) {
	  s=s.getParents().firstElement();
	}
	v.add(s);
      }
      // Process children
      if (tn.getFirstChild()!=null) {
	getParents(v,(NodeAST)tn.getFirstChild());
      }
      
      // Next sibling
      tn=(NodeAST)tn.getNextSibling();
    }
  }


  // ******************************************************************
  // moveSymbolDeclaration:
  //
  // Manage the movement of a symbol from a scope to an other one
  // ******************************************************************
  private void moveSymbolDeclaration(Symbol s, int scope_depth) {
    // the symbol moves to a upper scope, rename it
    s.reName(getMangling(s.getName()));
    s.setScopeDepth(scope_depth);
    // Special processing for enumerates, since we must also
    // rename enumerate fields
    if (s instanceof EnumTag) {
      EnumTag s_enum=(EnumTag)s;
      if (s_enum.getNbChildren()!=0) {
	java.util.Vector<EnumConstant> children_vector=s_enum.getChildren();
	for(EnumConstant child:children_vector) {
	  child.reName(getSameMangling(child.getName()));
	  child.setScopeDepth(scope_depth);
	} // For
      }
    }
  }



  // ##################################################################
  // Error management
  // ##################################################################


  // ******************************************************************
  // Message :
  //
  // Prints a warning message for AST node 'tn' (from which the line
  // number is taken).
  // ******************************************************************
  protected void Message(NodeAST tn, String message) {
    compilerError.raiseMessage(tn,message);
  }
  protected void Message(int level, NodeAST tn, String message) {
    compilerError.raiseMessage(level,tn,message);
  }

  // ******************************************************************
  // Warning :
  //
  // Prints a warning message for AST node 'tn' (from which the line
  // number is taken).
  // ******************************************************************
  protected void Warning(NodeAST tn, String message) {
    compilerError.raiseWarning(tn,message);
  }
  protected void Warning(int level, NodeAST tn, String message) {
    compilerError.raiseWarning(level,tn,message);
  }

  // ******************************************************************
  // Error :
  //
  // Prints an error message for AST node 'tn' (from which the line
  // number is taken). Exit after 5 errors.
  // ******************************************************************
  protected void Error(NodeAST tn, String message) {
    compilerError.raiseError(tn,message);
  }

  // ******************************************************************
  // FatalError :
  //
  // Prints an error message and exit directly
  // ******************************************************************
  protected void FatalError(NodeAST tn, String message) {
    compilerError.raiseFatalError(tn,message);
  }

  // ******************************************************************
  // InternalError :
  //
  // Prints an error message and exit directly
  // ******************************************************************
  protected void InternalError(NodeAST tn, String message) {
    compilerError.raiseInternalError(tn, message);
  }

  // ******************************************************************
  // InternalError :
  //
  // Prints an error message and exit directly
  // ******************************************************************
  protected void InternalError(String message) {
    compilerError.raiseInternalError(message);
  }


  // ##################################################################
  // Complex Literal table
  // ##################################################################

  // List of literals
  private LinkedList<Literal> literalList = new  LinkedList<Literal>();

  // Returns the list of literals
  public LinkedList<Literal> getLiteralList() {
    return(literalList);
  };


  // ##################################################################
  // Symbol table management
  // ##################################################################

  // Access to symbol table
  protected SymbolTable symbolTable = new SymbolTable();

  // Returns the symbol table
  public SymbolTable getSymbolTable() {
    return(symbolTable);
  };


  // ******************************************************************
  // Add([Label|Tag|Enumfield|label|])Symbol:
  //
  // Add to the symbol table a (tag) symbol whose name is 'name'.
  // Tag Note: the name is automatically prefixed with "tag "
  // pre-requist: 'symbol' must be correct
  //
  // ******************************************************************
  private void addCodeLabel(String name, CodeLabel symbol) {
    symbolTable.addCodeLabel(name,symbol,compilerError);
  }
  private void addTagSymbol(String name, TagSymbol symbol) {
    symbolTable.addTag(name,symbol,compilerError);
  }
  private void addObjectLabelSymbol(String name, ObjectLabel symbol) {
    // Set the symbol as builtin in case it is a function symbol and we are in 'builtin' mode
    symbolTable.addObjectLabel(name,symbol,compilerError);
  }
  private void addEnumConstantSymbol(String name, EnumConstant symbol) {
    // Set the symbol as builtin in case it is a function symbol and we are in 'builtin' mode
    symbolTable.addEnumConstant(name,symbol,compilerError);
  }
  private void addTypedefSymbol(String name, Typedef symbol) {
    // Set the symbol as builtin in case we are in 'builtin' mode
    if (isExternalBuiltinMode()) {
      symbol.setExternalBuiltinType();
    }
    symbolTable.addTypedef(name,symbol,compilerError);
  }
  private void addFunctionSymbol(String name, FunctionLabel symbol) {
    // Set the symbol as builtin in case we are in 'builtin' mode
    if (isExternalBuiltinMode()) {
      symbol.setExternalBuiltinFunction();
    }
    // Set the symbol as mangled in case we are in 'mangling' mode
    if (is_mangling_mode) {
      symbol.setMangledFunction();
    }

    symbolTable.addFunction(name,symbol,compilerError);
  }


  // ******************************************************************
  // lookup(Tag)AndSetReference :
  //
  // Look for (tag) symbol named 'tn->getText()' in the scope hierarchy
  // of the symbol table (starting from the current one).
  // The symbol exists ?
  //   - YES: put a reference to the symbol as 'reference' attributes 
  //          of AST node 'tn' (attribute with name "REFERENCE")
  //   - NO : raise an error
  //
  // ******************************************************************
  private Symbol lookupAndSetReference(NodeAST tn) {
    String str=tn.getText();
    Symbol symbol = symbolTable.lookupName(str);
    if (symbol==null) {
      // GCC specific symbols
      if ( tn.getText().equals("__FUNCTION__") ||
	   tn.getText().equals("__PRETTY_FUNCTION__") ) {
	Warning(1,tn, "Undefined symbol '" + str +
		"', assuming that it is a backend compiler builtin variable");

	// Create a dummy 'int' symbol
	Variable var=new Variable(tn.getText());
	// Sets the type
	var.setType(new Pointer(IntegerScalar.Tschar));
	// Add to symbol table
	addObjectLabelSymbol(str,var);

	// Sets reference in the AST
	tn.setReference(var);

	symbol=var;
      }
      else {
	FatalError(tn, " symbol '" + str + "' not defined");
      }
    }

    // Sets a reference to the symbol
    tn.setReference(symbol);

    return(symbol);
  }


  // ******************************************************************
  // lookupAndSetReferenceToFunction :
  //
  // Look for function symbol named 'tn->getText()' in the scope
  // hierarchy of the symbol table (starting from the current one).
  // The symbol exists ?
  //   - YES: put a reference to the symbol as 'reference' attributes 
  //          of AST node 'tn' (attribute with name "REFERENCE")
  //   - NO : It is not an error since a function can be called without
  //          prototype in C. It simply creates a a 'int f()' prototype 
  //
  // ******************************************************************
  private Symbol lookupAndSetReferenceToFunction(NodeAST tn) {
    String str=tn.getText();
    Symbol symbol;

    if (builtinManager.isBuiltinFunctionName(str)) {
      FunctionLabel func_symbol=new FunctionLabel();
      func_symbol.setName(str);
      func_symbol.setCompilerBuiltinFunction();

      // Sets reference in the AST
      tn.setReference(func_symbol);

      symbol=func_symbol;
    }
    else {
      symbol = symbolTable.lookupName(str);
      if (symbol!=null) {
	tn.setReference(symbol);
      }
      else {
	// It may be builtin or simply a non declared function
	// Do like if we add a prototype defined:
	// Function taking no parameter and returning an int
	Warning(1,tn, "Undefined symbol '" + str +
		"', assuming that it is a backend compiler builtin function");
	
	// Create a symbol with 'int f()' type
	FunctionLabel func_symbol=new FunctionLabel();
	func_symbol.setName(str);
	func_symbol.setPrototype();
	// Sets the type
	Function fp=new Function();
	fp.setReturnType(IntegerScalar.Tsint);
	func_symbol.setType(fp);
	// Add to symbol table
	addFunctionSymbol(str,func_symbol);
	
	// Sets reference in the AST
	tn.setReference(func_symbol);
	
	symbol=func_symbol;
      }
    }
    return(symbol);
  }



  //------------------------------------------------------------------
  // Tag lookup
  //------------------------------------------------------------------

  // Lookup tag
  private TagSymbol lookupTagAndSetReference(NodeAST tn) {
    TagSymbol symbol
      = symbolTable.lookupTagName(tn.getText());
    if (symbol!=null) {
      tn.setReference(symbol);
    }
    else {
      FatalError(tn, " tag symbol '" + tn.getText() + "' not defined");
    }
    return(symbol);
  }

  // Lookup tag
  private TagSymbol lookupTag(NodeAST tn) {
    TagSymbol symbol
      = symbolTable.lookupTagName(tn.getText());
    return(symbol);
  }

  // Lookup tag
  private TagSymbol lookupTagInCurrentScope(NodeAST tn) {
    TagSymbol symbol
      = symbolTable.lookupTagNameInCurrentScope(tn.getText());
    return(symbol);
  }



  //------------------------------------------------------------------
  // Code label lookup
  //------------------------------------------------------------------

  // Lookup label and set reference
  private CodeLabel lookupCodeLabelAndSetReference(NodeAST tn) {
    CodeLabel symbol
      = symbolTable.lookupCodeLabel(tn.getText());
    if (symbol!=null) {
      tn.setReference(symbol);
    }
    // Since forward reference is allowed for labels, no check here
    return(symbol);
  }

  // Lookup label
  private CodeLabel lookupCodeLabel(NodeAST tn) {
    CodeLabel symbol
      = symbolTable.lookupCodeLabel(tn.getText());
    // Since forward reference is allowed for labels, no check here
    return(symbol);
  }


  // ##################################################################
  // External C99 builtin types
  // ##################################################################

  public Type getType_size_t() {
    // Expecting size_t to be a typedef
    Symbol symbol = symbolTable.lookupName("size_t");
    if (symbol==null) {
      // Get it from the ABI
      return Type.getSourceABI().getEquivalent_size_t();
    }
    return(symbol.getType());
  };

  public Type getType_ptrdiff_t() {
    // Expecting ptrdiff_t to be a typedef
    Symbol symbol = symbolTable.lookupName("ptrdiff_t");
    if (symbol==null) {
      // Get it from the ABI
      return Type.getSourceABI().getEquivalent_ptrdiff_t();
    }
    return(symbol.getType());
  };

  public Type getType_intptr_t() {
    // Expecting intptr_t to be a typedef
    Symbol symbol = symbolTable.lookupName("intptr_t");
    if (symbol==null) {
      // Get it from the ABI
      return Type.getSourceABI().getEquivalent_intptr_t();
    }
    return(symbol.getType());
  };

  public Type getType_uintptr_t() {
    // Expecting uintptr_t to be a typedef
    Symbol symbol = symbolTable.lookupName("uintptr_t");
    if (symbol==null) {
      // Get it from the ABI
      return Type.getSourceABI().getEquivalent_uintptr_t();
    }
    return(symbol.getType());
  };


  // ##################################################################
  // Function parameter scope management
  // ##################################################################

  // ******************************************************************
  // Unique number for parameters of function prototypes
  // ******************************************************************

  // Counter
  private int paramlist_number=0;

  private int getParamlistNumber() {
    return paramlist_number++;
  }

  private int functiondef_number=0;
  private int getFunctionDefNumber() {
    return functiondef_number;
  }
  private int getAndIncrementFunctionDefNumber() {
    return functiondef_number++;
  }



  // ##################################################################
  // Symbol mangling Management
  //
  // Note: management now done inside the symbol table to avoid problem
  // with mangling when a symbol table is shared by several
  // 'symboltableBuilders'
  // ##################################################################

  // ******************************************************************
  // Mangling counter management 
  // ******************************************************************
  public int getManglingCounter() {
    return symbolTable.getManglingCounter();
  }
  public void setManglingCounter(int m) {
    symbolTable.setManglingCounter(m);
  }

  // ******************************************************************
  // getMangling :
  //
  // Returns the mangled version of 's', from a counter which is
  // incremented. The resulting name is unique
  // ******************************************************************
  protected String getMangling(String s) {
    return symbolTable.getMangling(s);
  }

  // ******************************************************************
  // getSameMangling :
  //
  // Returns the mangled version of 's', , from a counter which is not
  // incremented
  // ******************************************************************
  protected String getSameMangling(String s) {
    return symbolTable.getSameMangling(s);
  }

  // ******************************************************************
  // getMangling :
  //
  // Returns a new symbol name from a counter which is incremented.
  // The resulting name is unique
  // ******************************************************************
  private String getNewName() {
    return symbolTable.getNewName();
  }

}


//##################################################################
//                      Grammar entry point
//##################################################################



translationUnit  options {
  defaultErrorHandler=false;
}
        :       ( externalList )? 
        ;

externalList
        :       ( externalDef )+
        ;


externalDef
        :       declaration
        |       functionDef
        |       SEMI
        |       asm_expr
// Should never come here: managed by GnuCParser
//        |       typelessDeclaration
        |       pragma
        ;

pragma :
		p:PRAGMA
		{
		  // Remove #pragma
		  String s=p.getText().substring(8).trim();  // To be changed
		  String[] tokenList=s.split(" ");
		  int length=tokenList.length; // Length at least 1
		  if (tokenList[0].equals("")) {
		    Warning(1,(NodeAST)p, "Pragma without option name");
		  }
		  else {
		    if (tokenList[0].equals("ast_mangling")) {
		      if (length!=2) {
			Error((NodeAST)p, "expecting one and only one value to pragma option '"+tokenList[0]+"'");
		      }
		      else if (tokenList[1].equals("on")) {
			setManglingMode();
		      }
		      else if (tokenList[1].equals("off")) {
			unsetManglingMode();
		      }
		      else {
			Error((NodeAST)p, "Unknown value '"+tokenList[1]+"' to pragma option '"+tokenList[0]+"'");
		      }
		      // This pragma should not be regenerated
		      ##.setText("");
		    }
		    else if (tokenList[0].equals("ast_ext_builtin")) {
		      if (length!=2) {
			Error((NodeAST)p, "expecting one and only one value to pragma option '"+tokenList[0]+"'");
		      }
		      else if (tokenList[1].equals("on")) {
			setExternalBuiltinMode();
		      }
		      else if (tokenList[1].equals("off")) {
			unsetExternalBuiltinMode();
		      }
		      else {
			Error((NodeAST)p, "Unknown value '"+tokenList[1]+"' to pragma option '"+tokenList[0]+"'");
		      }
		      // This pragma should not be regenerated
		      ##.setText("");
		    }
		    // OpenCL extensions
		    else if (tokenList[0].equals("cl_ST_set_private_variable_length_array")) {
		      if (length!=1) {
			Error((NodeAST)p, "expecting no value to pragma option '"+tokenList[0]+"'");
		      }
		      setCLExtPrivateVariableLengthArray();
		      // This pragma should not be regenerated
		      ##.setText("");
		    }
		    // Unrecognized pragma
		    else {
		      // Simply pass the pragma option to the backend compiler
		      Warning(1,(NodeAST)p, "Unknown pragma option passed to the backend compiler '"+s+"'");
		    }
		  }
		}
        ;

asm_expr
	{
	  EnrichedType etype=null;
	}
        :       #( "asm" ( "volatile" )? LCURLY etype=expr RCURLY )
        ;

declaration
        : 
	 declarationStd | declarationNoInitDecl | null_decl
 	;

for_declaration
        : 
	 declarationStd_body | declarationNoInitDecl_body
 	;

null_decl: NNoDeclaration
        ;



//##################################################################
//                      Standard definitions
//##################################################################


declarationStd
        : declarationStd_body
        ;

declarationStd_body
 	   {
	     // Management of C99 for-loop declaration. No split is requested here since
	     // the variable do not be to be extracted (not an instance variable)
	     boolean old_split=is_split;
	     if (is_for_declaration) {
	       is_split=false;
	     }

	     // From Specifiers
	     LinkedList<Symbol> new_def
	       = new LinkedList<Symbol>();
	     LinkedList<Symbol> tag_ref
	       = new LinkedList<Symbol>();
	     // From InitDecl
	     LinkedList<Symbol> new_def_param
	       = new LinkedList<Symbol>();
	     LinkedList<Symbol> std_def
	       = new LinkedList<Symbol>();

	     // symbol table element to be filled by 'declSpecifiers'
	     TypeSpecifierQualifier specifier_qualifier
	       = new TypeSpecifierQualifier();
	     StorageClass storageclass=new StorageClass();

	     // No tag split
	     boolean tagSplit = false;
	     if (isDeclarationSplitRequested()) {
	       tagSplit = true;
	     }
	   }
        :
	       #( NDeclaration
		  // Request the split of tag declarations of specifiers
		  //   - put tag declaration into new_def
		  //   - put referenced tags into tag_ref
		  // specifier_qualifier get the partial symbol definition
		  // corresponding to the specifier
		  ds:declSpecifiers[false,
				    tagSplit, // split tag declarations
				    false, // this is not a function parameter
				    specifier_qualifier, // written
				    storageclass,        // written
				    new_def,tag_ref,     // written
				    new_def_param        // written
				    ]
		  // Request the split definitions of the declarator and
		  // potential tag declarations of function parameters list
		  // of the declarator
		  //   - put declarator definition into std_def
		  //   - put tag declarations as in function parameter list
		  //     into new_def_param
		  (  {isDeclarationSplitRequested()}? 
		     initDeclList_split[tagSplit,  // split tag declarations
					specifier_qualifier.getType(ds,compilerError), // read-only
					storageclass,            // read-only
					astFactory.dupList(#ds), // read-only
					new_def_param,std_def	 // written
					]
		     | initDeclList[tagSplit,  // split tag declarations
				    specifier_qualifier.getType(ds,compilerError), // read-only
				    storageclass,            // read-only
				    astFactory.dupList(#ds), // read-only
				    new_def_param,std_def    // written
				    ]
		   )?
		)
	   {
	     // Here, if split requested, all symbol declared in the statement
	     // (type tags, typedef, variables, function label) have been
	     // put in lists
	     //
	     // + new_def:
	     //   -------
	     //   list of tag symbols defined in specifiers 
	     //   ('struct', 'union' and 'enum' type tags)
	     //
	     // + new_def_param:
	     //   -------------
	     //   list of tag symbols defined in specifiers of function
	     //   parameter list of the declarator
	     //   ('struct', 'union' and 'enum' type tags)
	     //
	     // + std_def:
	     //   -------
	     //   list of symbols defined in a declarator
	     //   (typedef, variables, function)
	     //
	     //
	     // Additionnal information are available in all cases:
	     //
	     // + tag_ref:
	     //   -------
	     //   list of type tags symbols referenced in the definition
	     //   specifier
	     //

	     // New declaration of function specifiers.
	     if (isDeclarationSplitRequested()) {
	       // Occurs only in case of declaration split requested

	       // We do not keep the 'NDeclaration' tree since it contains
	       // Only the specifier (which has been duplicated to create
	       // new split declarations)

	       // Reset 'currentAST'
	       currentAST.root  = null;
	       currentAST.child = null;

	       // new_def first since occuring in specifiers
	       // Add struct/union or enum symbol definitions to the AST
	       // 'new_def'
	       while ( new_def.size() != 0 ) {
		 Symbol symbol = new_def.removeFirst();
		 astFactory.addASTChild(currentAST,symbol.getDeclarationNode());
	       }

	       // new_def_param second since occuring in declarators
	       // Add struct/union or enum symbol definitions which was located in
	       // parameter function specifiers. The scope of the symbol is changed
	       // so that a specific action (in particular mangling) must be performed
	       while ( new_def_param.size() != 0 ) {
		 // It can only be the declaration of a struct, an union or an enum
		 Symbol s = new_def_param.removeFirst();
		 // The symbol moves to a upper scope, rename it
		 // current scope is not inside a tag
		 moveSymbolDeclaration(s,symbolTable.getCurrentScopeDepth());
		 // Put it in the AST
		 astFactory.addASTChild(currentAST,s.getDeclarationNode());
	       }

	       // The result of Declaration is a list, there is no root
	       // -> leave root at null
	     }


	     //* Add the definition of all symbols declared in the declarator to
	     //* the AST and decorate these symbols with brother/parents information
	     HashSet<Symbol> brothers = new HashSet<Symbol>(); 
	     for(Symbol ref_symb:tag_ref) {
	       putBrothersInSet(brothers, ref_symb);
	     }

	     while ( std_def.size() != 0 ) {
	       Symbol symbol = std_def.removeFirst();

	       // All referenced type tags are parents
	       for(Symbol ref_symb:tag_ref) {
		 symbol.addParent(ref_symb);
	       }
 
	       // Add all brothers of parents declared at this point also 
	       // (for uncomplete struct/unions ...)
	       for(Symbol brother_symb:brothers) {
		 symbol.addParent(brother_symb);
	       }

	       // Manage the declaration: should it be put in the AST or not ?
	       if (isExtractionMode()) {
		 if (isInstanceDataDefinition(symbol)) {
		   // Perform relevant mangling and put the symbol declaration
		   // into relevant global lists
		   extractInstanceData(symbol);
		 }
		 else {
		   if ( (symbol instanceof ObjectLabel) && symbol.isExtern()) {
		     // It is a potential reference to an instance data
		     // => Put the declaration in a list. At the link stage, we will
		     //    know if it is or not a instance data declaration (extern)
		     extern_declaration_list.add((ObjectLabel)symbol);
		   }
		   // The declaration must be kept in the AST (for the moment)
		   astFactory.addASTChild(currentAST,symbol.getDeclarationNode());
		 }
	       }
	       else if (isDeclarationSplitRequested()) {
		 astFactory.addASTChild(currentAST,symbol.getDeclarationNode());
	       }
	       
	     }

	     // Note: No need to clean 'tag_ref', it will be done automatically
	     // by the garbage collector


	     // Management of for-loop declaration for which split is never done
	     if (is_for_declaration) {
	       is_split=old_split;
	     }
	   }
        ;

declarationNoInitDecl
        : declarationNoInitDecl_body
        ;

declarationNoInitDecl_body
 	   {
	     // Management of C99 for-loop declaration. No split is requested here since
	     // the variable do not be to be extracted (not an instance variable)
	     boolean old_split=is_split;
	     if (is_for_declaration) {
	       is_split=false;
	     }

	     // From Specifiers
	     LinkedList<Symbol> new_def
	       = new LinkedList<Symbol>();
	     // From Specifiers to InitDecl
	     LinkedList<Symbol> tag_ref
	       = new LinkedList<Symbol>();
	     // From InitDecl of specifiers
	     LinkedList<Symbol> new_def_param
	       = new LinkedList<Symbol>();

	     // symbol table element to be filled by 'declSpecifiers'
	     TypeSpecifierQualifier specifier_qualifier
	       = new TypeSpecifierQualifier();
	     StorageClass storageclass=new StorageClass();

	     // Tag split
	     boolean tagSplit = false;
	     if (isDeclarationSplitRequested()) {
	       tagSplit = true;
	     }
	   }
	   :  #( NDeclarationNoInitDecl
		   // Request the split of specifiers (int new def)
		   declSpecifiers[true,
				  tagSplit,// split tag declarations
				  false, // this is not a function parameter
				  specifier_qualifier,// written
				  storageclass,       // written
				  new_def,tag_ref,    // written
				  new_def_param       // written
				  ]
		   // No declarator, so no symbol. It is useless here to create
		   // the specifier type 
		 )
	{
	  // Here, if requested , all symbol declared in the statement
	  // (type tags, typedef, variables, function label) have been
	  // put in lists
	  //
	  // + new_def:
	  //   -------
	  //   list of tag symbols defined in specifiers 
	  //   ('struct', 'union' and 'enum' type tags)
	  //
	  // + new_def_param:
	  //   -------------
	  //   list of tag symbols defined in specifiers of function
	  //   parameter list ('struct', 'union' and 'enum' type tags)
	  //
	  // Additionnal information are available in all cases:
	  //
	  // + tag_ref:
	  //   -------
	  //   list of type tags symbols referenced in the definition
	  //   specifier
	  //
	  if (isDeclarationSplitRequested()) {
	    // We do not keep the 'NDeclarationNoInitDecl' tree since it
	    // contains only the specifier (which has been duplicated to
	    // create new split declarations)
	    
	    // Reset 'currentAST'
	    currentAST.root  = null;
	    currentAST.child = null;
	    
	    // All struct/union or enum symbol definitions are in
	    // 'new_def'
	    while ( new_def.size() != 0 ) {
	      Symbol symbol = new_def.removeFirst();
	      astFactory.addASTChild(currentAST,symbol.getDeclarationNode());
	    }
	    
	    // Add struct/union or enum symbol definitions which was located in
	    // parameter function specifiers. The scope of the symbol is changed
	    // so that a specific action (in particular mangling) must be performed
	    while ( new_def_param.size() != 0 ) {
	      // It can only be the declaration of a struct, an union or an enum
	      Symbol s = new_def_param.removeFirst();
	      // The symbol moves to a upper scope, rename it
	      // current scope is not inside a tag

	      moveSymbolDeclaration(s,symbolTable.getCurrentScopeDepth());
	      // Put it in the AST
	      astFactory.addASTChild(currentAST,s.getDeclarationNode());
	    }

	    // The result of DeclarationNoInitDecl is a list, there is no root
	    // -> leave root at null

	     // Management of for-loop declaration for which split is never done
	     if (is_for_declaration) {
	       is_split=old_split;
	     }
	  }
	}
        ;


//##################################################################
//                 Initdecl (Declarator + initializer)
//##################################################################


//------------------------------------------------------------------
// initDeclList:
//
// List of declarator, with an optional initializer, completing the
// definition of some 'variable', 'typedef' or 'function prototype'
// symbols
//
// Input parameters provide specifiers information
//  - tag_split    : tells if tag declarations must be split
//  - specifier_qualifier_type : type of the specifier/qualifier
//  - storageClass : storage class
//  - ds           : copy of the tree declaring specifiers
//
// Returned data are:
//  - new_def_param : push in this list symbols defined in specifiers
//		      of function parameter list of the declarator
//		      ('struct', 'union' and 'enum' type tags)
//                    [when tag_split is 'true']
//  - std_def       : list of symbols defined in a declarator
//		      (typedef, variables, function)
//------------------------------------------------------------------
initDeclList[boolean tag_split,
	     Type specifier_qualifier_type,
	     StorageClass storageclass,
	     AST ds,
	     LinkedList<Symbol> new_def_param,
	     LinkedList<Symbol> std_def]
	:       
	  initDecl[tag_split,
		   specifier_qualifier_type,storageclass,
		   ds,new_def_param,std_def]
	  (
	   (attributeSpecifierList)?
	    initDecl[tag_split,
		     specifier_qualifier_type,storageclass,
		     ds,new_def_param,std_def]
	   )*
        ;

//------------------------------------------------------------------
// initDeclList_split :
//
// Same as 'initDeclList' but symbol declarators and initializers
// are extracted and an empty tree is returned by the grammar rule
// method in the parser
//------------------------------------------------------------------
initDeclList_split[boolean tag_split,
		   Type specifier_qualifier_type,
		   StorageClass storageclass,
		   AST ds,
		   LinkedList<Symbol> new_def_param,
		   LinkedList<Symbol> std_def]
	:!
	  initDecl[tag_split,
		   specifier_qualifier_type,storageclass,
		   ds,new_def_param,std_def]
	  (
	    (attributeSpecifierList)?
	    initDecl[tag_split,
		     specifier_qualifier_type,storageclass,
		     ds,new_def_param,std_def]
	   )*
        ;


//------------------------------------------------------------------
// initDecl:
//
// Declarator, with an optional initializer, completing the
// definition of some 'variable', 'typedef' or 'function prototype'
// symbols
//
// Input parameters provide specifiers information
//  - tag_split    : tells if tag declarations must be split
//  - specifier_qualifier_type : type of the specifier/qualifier
//  - storageClass : storage class
//  - ds           : copy of the tree declaring specifiers
//
// Returned values are:
//  - new_def_param : list of symbols defined in specifiers of function
//                    parameter list of the declarator ('struct', 
//		      'union' and 'enum' type tags)
//                    [when tag_split is 'true']
//  - std_def       : list of symbols defined in a declarator
//		      (typedef, variables, function)
//------------------------------------------------------------------
initDecl[boolean tag_split,
	 Type specifier_qualifier_type,
	 StorageClass storageclass,
	 AST ds,
	 LinkedList<Symbol> new_def_param,
	 LinkedList<Symbol> std_def]
	{
	  Literal literal=null;
	  AST id_node; 
	  LinkedList<Symbol> tag_ref  = new LinkedList<Symbol>();

	  boolean isInitializer=false;

	  int count=0;
	  NodeAST a=null;

	  // Create the symbol
	  Symbol decl_symbol=new Symbol(storageclass);
	}
        : #( NInitDecl

	     id_node = declarator[tag_split,
				  decl_symbol,false,
				  new_def_param,tag_ref,
				  specifier_qualifier_type]
	     {
          // Complete the symbol information with the declarator
          // ---------------------------------------------------

          // Get the symbol name
          if (id_node==null) {
            // should never happen
            InternalError("(initDecl) no symbol");
            throw new RuntimeException();
          }

          //-- Manage the symbol --
          //-----------------------

          // Sets the symbol name
          String declName=id_node.getText();
          decl_symbol.setName(declName);

          // Type already set

          // Set pointers to AST
          decl_symbol.setIdNode((NodeAST)#id_node);


          //##################################################################
          // Specific to declaration extraction 
          //##################################################################

          //-- Manage tag references of the declarator
          for(Symbol s:tag_ref) {
            decl_symbol.addParent(s);
          }

          // Manage optional declaration split
          if (isDeclarationSplitRequested()) {
            //--   Create a variable declaration   --
            //---------------------------------------
            AST   ds1 = astFactory.dupList(ds);
            NodeAST tn = #( #[NDeclaration], ds1, #initDecl );

            // Complete the declaration with pointers to the AST
            // declaration node
            decl_symbol.setDeclarationNode(tn);
          }

          //##################################################################
          // Create the final symbol and put it in the symbol table
          //
          // Note: the symbol can be referenced in the initialization, so that
          // it must be put in the symbol table at this stage)
          //##################################################################

          // Sets the family
          if (decl_symbol.getStorageClass().isTypedef()) {
            // It's a typedef
            Typedef td=new Typedef(decl_symbol);
            addTypedefSymbol(declName,td);
            decl_symbol=td;
          }
          else if (decl_symbol.getType().isFunction()) {
            // Not, a function prototype shall not have any initialization
            // So no need to connect to initialization
            FunctionLabel fl=new FunctionLabel(decl_symbol);
            fl.setPrototype();
            addFunctionSymbol(declName,fl);
            decl_symbol=fl;
          }
          else if (decl_symbol.getType().isArray()) {
            // It's an array label
            ArrayLabel al=new ArrayLabel(decl_symbol);
            addObjectLabelSymbol(declName,al);
            decl_symbol=al;
          }
          else {
            // It's a variable
            Variable v=new Variable(decl_symbol);
            addObjectLabelSymbol(declName,v);
            decl_symbol=v;
          }

          // Link -> symbol
          //---------------

          // AST node -> symbol
          ((NodeAST)#id_node).setDefinition(decl_symbol);

          // 'std_def' -> symbol
          std_def.add(decl_symbol);


          //##################################################################
          //-- Check storage class
          //##################################################################

          // No multiple storage class allowed
          StorageClass sc=decl_symbol.getStorageClass();
          if (sc.isMultipleStorageClass()) {
            Error((NodeAST)id_node,
                "multiple storage classes in declaration of `"
                    +declName+"'");
          }
          // C99 specific
          if ( sc.isInline() && (decl_symbol instanceof ObjectLabel) ) {
            Warning((NodeAST)id_node,"variable `"+
                declName+"' declared `inline'");
          }
          // No register storage class authorized for function prototypes
          // auto gives just a warning
          if (decl_symbol.getType().isFunction()) {
            if (sc.isRegister()) {
              Error((NodeAST)id_node,"invalid storage class for function `"+declName+"'");
            }
            else if (sc.isAuto()) {
              Warning((NodeAST)id_node,"invalid storage class for function `"+declName+"'");
            }
          }
          // Check auto and register at top symbol table level
          if (symbolTable.isTopLevel()) {
            if (sc.isRegister()) {
              Error((NodeAST)id_node,"top-level declaration of `"+declName+
                  "' specifies `register'");
            }
            else if (sc.isAuto()) {
              Warning((NodeAST)id_node,"top-level declaration of `"+declName+
                  "' specifies `auto'");
            }
          }
          // For-loop declaration check
          if (is_for_declaration) {
            if (decl_symbol instanceof ObjectLabel) {
              if (sc.isStatic()) {
                Error((NodeAST)id_node,"declaration of 'static' variable '"+declName+
                    "' in for loop initial declaration");
              }
              if (sc.isExtern()) {
                Error((NodeAST)id_node,"declaration of 'extern' variable '"+declName+
                    "' in for loop initial declaration");
              }
            }
            else {
              Error((NodeAST)id_node,"declaration of non-variable '"+declName+
                  "' in for loop initial declaration");
            }
          }


          //##################################################################
          //-- builtin function check
          //##################################################################
          if (decl_symbol instanceof FunctionLabel) {
            // Check for builtin function
            if (builtinManager.isBuiltinFunctionName(declName)) {
              Error((NodeAST)id_node,"function '" + id_node.getText() +
                  "' is a compiler builtin function and can not be declared");
            }
          }


          //##################################################################
          //-- OCL checks
          //##################################################################
          if (oclLanguage) {
            if (decl_symbol instanceof ObjectLabel) {
              Type the_type=decl_symbol.getType();
              if (symbolTable.isTopLevel()) {
                // We are at program scope level
                if (!oclCCompatibilityOption) {
                  AddressSpace as=AddressSpace.NO;
                  if (the_type.isQualified()) {
                    as=(the_type.getQualifier()).getAddressSpace();
                  }
                  switch(as) {
                  case NO:
                  case PRIVATE:
                    Error((NodeAST)id_node,"variable '"+declName+
                        "': OCL forbids declaring __private variables at program scope level");
                    break;
                  case LOCAL:
                    Error((NodeAST)id_node,"variable '"+declName+
                        "': OCL forbids declaring __local variables at program scope level");
                    break;
                  case GLOBAL:
                    Error((NodeAST)id_node,"variable '"+declName+
                        "': OCL forbids declaring __global variables");
                    break;
                  case CONSTANT:
                    // OK, nothing to do
                  }
                }
              }
              else {
                // We are in kernel or function
                if (!oclCCompatibilityOption) {
                  AddressSpace as=AddressSpace.NO;
                  if (the_type.isQualified()) {
                    as=(the_type.getQualifier()).getAddressSpace();
                  }
                  switch(as) {
                  case LOCAL:
                    if (currentFunctionIsKernel) {
                      // Kernel
                      if (symbolTable.getCurrentScopeDepth()>1) {
                        // Local declaration only allowed at kernel scope level
                        Error((NodeAST)id_node,"variable '"+declName+
                            "': OCL allows declaring __local variables in kernel only at kernel scope level");
                      }
                      // Else OK, allowed
                    }
                    else {
                      // Standard function, not kernel
                      Error((NodeAST)id_node,"variable '"+declName+
                          "': OCL forbids declaring __local variables in non kernel functions");
                    }
                    break;
                  case GLOBAL:
                    Error((NodeAST)id_node,"variable '"+declName+
                        "': OCL forbids declaring __global variables");
                    break;
                  case CONSTANT:
                    Error((NodeAST)id_node,"variable '"+declName+
                        "': OCL forbids declaring __constant variables in kernel and functions");
                    break;
                  case NO:
                  case PRIVATE:
                    // OK, nothing to do
                  }
                }

                if (the_type.isQualified()) {
                  Qualifier q=the_type.getQualifier();
                  if (q.getAddressSpace()==AddressSpace.NO) {
                    // By default, it is private
                    q.setAddressSpace(AddressSpace.PRIVATE);
                  }
                }
                else {
                  // By default, the type is considered in the private address space
                  decl_symbol.setType(new Qualifier(AddressSpace.PRIVATE,the_type));
                }
              }
            }
          }

          //##################################################################
          //-- VX checks
          //##################################################################
          if (vxLanguage) {
            if (decl_symbol instanceof ObjectLabel) {
              Type the_type=decl_symbol.getType();
              if (symbolTable.isTopLevel()) {
                // We are at program scope level
                Error((NodeAST)id_node,"variable '"+declName+
                    "': VX forbids declaring variables at program scope level");
              }
              else {
                // Local static variables are forbidden
                if (decl_symbol.getStorageClass().isStatic()) {
                  Error((NodeAST)id_node,"variable '"+declName+
                      "': VX forbids declaring local static variables");               
                }
              }
            }
          }
   
	     }

	     (attributeSpecifierList)?

	     ( 
	       ( 
		 
		 //================================================
	         // With initializer
		 //================================================
		 (
		   // Don't put the initializer in case of instance data 
		   ! {isExtractionMode()&&isInstanceDataDefinition(decl_symbol)}? 
		     ASSIGN literal=a1:initializer[decl_symbol.referencesCompileTimeAllocatedEntity(),
						   decl_symbol.getType(),0]
		     {
		       a=#a1;

		       if (decl_symbol instanceof ObjectLabel) {
			 Type type=decl_symbol.getType().unqualify();
			 if (type.isArray()) {
			   Array the_type=(Array)type;
			   if (the_type.hasSizeDefined()) {
			     // potentially need to complete the uncomplete array declaration
			     ((ArrayLabel)decl_symbol).setInitArraySize(the_type.getNbElements());
			   }
			   // Else an error should have already been raised
			 }
		       }

		       else {
			 // It is an error, only an object (variable, array) can be initialized

			 if (decl_symbol instanceof Typedef) {
			   // A typedef can not have any initializer
			   Error((NodeAST)id_node, "typedef `" + id_node.getText()
				 +"' is initialized");
			   
			 }
			 else if (decl_symbol instanceof FunctionLabel) {
			   // A function can not have any initializer
			   Error((NodeAST)id_node, "function `" + id_node.getText()
				 +"' is initialized like a variable");
			 }
			 else {
			   InternalError((NodeAST)id_node, "InitDecl: Unknown type initialized");
			 }

		       }
		     }
		 
		   | ASSIGN literal=a2:initializer[decl_symbol.referencesCompileTimeAllocatedEntity(),
						   decl_symbol.getType(),0]
		     {
		       a=#a2; 

		       if (!(decl_symbol instanceof ObjectLabel)) {
			 // It is an error

			 if (decl_symbol instanceof Typedef) {
			   // A typedef can not have any initializer
			   Error((NodeAST)id_node, "typedef `" + id_node.getText()
				 +"' is initialized");
			   
			 }
			 else if (decl_symbol instanceof FunctionLabel) {
			   // A function can not have any initializer
			   Error((NodeAST)id_node, "function `" + id_node.getText()
				 +"' is initialized like a variable");
			 }
			 else {
			   InternalError((NodeAST)id_node, "InitDecl: Unknown type initialized");
			 }
		       }

		       else if (isExtractionMode()) {
			 // In case of extract mode, a array size node is automatically added to
			 // arrays for which the first dimension is not set, even when they are
			 // not concerned by the extraction. This node must be completed 
			 Type type=decl_symbol.getType().unqualify();
			 if (type.isArray()) {
			   Array the_type=(Array)type;
			   if (the_type.hasSizeDefined()) {
			     // potentially need to complete the uncomplete array declaration
			     ((ArrayLabel)decl_symbol).setInitArraySize(the_type.getNbElements());
			   }
			 }
		       }


		     }
		  )
	           //  Set the size of an uncomplete array thanks to its initialization
		  {
		   // Here, it is necessarily an ObjectLabel
		   ObjectLabel object_decl_symbol = (ObjectLabel)decl_symbol;

		   // An extern variable should not be initialized
		   if (object_decl_symbol.isExtern()) {
		     Warning((NodeAST)id_node, "`" + id_node.getText()
		         +"' has both `extern' and initializer");
		   }

		   // set the initilization node to the symbol
		   object_decl_symbol.setInitializationNode(a);

		   // Compute TAG dependencies with regard to initialization
		   java.util.Vector<Symbol> v = new java.util.Vector<Symbol>();
		   getParentsFromInitializer(v,a);
		   for(Symbol s:v) {
		     // Avoid circular dependencies in case of 
		     if (s!=object_decl_symbol) {
		       object_decl_symbol.addParent(s);
		     }
		   }

		   if (!(literal instanceof ExprLiteral)) {
		     // The literal is not a put yet in the literal list since it is not
		     // in an expression
		     literal.setParents(v);  // tags/func/typedef from which it depends
		     literalList.add(literal); // Put in the literal table
		     a.setLiteral(literal); // Link AST -> literal
		   }


		   //##################################################################
		   //-- OCL checks
		   //##################################################################
		   if (oclLanguage && (!oclCCompatibilityOption)) {
		     if (decl_symbol instanceof ObjectLabel) {
		       Type the_type=decl_symbol.getType();
		       AddressSpace as=AddressSpace.NO;
		       if (the_type.isQualified()) {
		         as=(the_type.getQualifier()).getAddressSpace();
		       }
		       if (as==AddressSpace.LOCAL) {
		         // A local variable can only be declared in a kernel
		         // and must not be initialized
		         Error((NodeAST)id_node,"variable '"+decl_symbol.getName()+
		             "': OCL forbids initialization of __local variables");
		       }
		     }
		   }
		  }
		 )
	       |
		 //================================================
	     // No initializer
		 //================================================
	       {
	         // [OCL] checks
	         if (oclLanguage) {
	           if (decl_symbol instanceof ObjectLabel) {
	             Type the_type=decl_symbol.getType();
	             AddressSpace as=AddressSpace.NO;
	             if (the_type.isQualified()) {
	               as=(the_type.getQualifier()).getAddressSpace();
	             }
	             // __constant data must be initialized
	             if (as==AddressSpace.CONSTANT) {
	               String declName=id_node.getText();
	               Warning((NodeAST)id_node,"variable '"+declName+
	                   "': OCL requires __constant variables to be initialized");
	             }
	           }
	         }
	       }
	       )
	     )
	     {
	       String declName=id_node.getText();
	       StorageClass sc=decl_symbol.getStorageClass();

	       // Check storage size (only if not extern and typedef)
	       // Note: this check can only be done after initialization since
	       // uncomplete arrays can be completed by the initialization in C
	       // ex:  int tab[]={1,2,3};
	       if ( (!sc.isExtern()) &&
		    (!sc.isTypedef()) ) {
		 if (decl_symbol.getType().isVoid()) {
		   Error((NodeAST)id_node,"variable '"+declName+"' declared void");
		 }
		 else if (!decl_symbol.getType().isComplete()) {
		   // Tentative array definitions are allowed, eaning that a non complete
		   // array definition is allowed if if is later redefined completely
		   // TODO: We should check when poping a scope in the symbol table
		   // that all definitions of arrays have been completed. If not, we should
		   // raise a error
		   if (!decl_symbol.getType().isArray()){
		     Error((NodeAST)id_node,"storage size of '"+declName+"' isn't known");
		   }
		 }
	       }
	     }
        ;



//##################################################################
// Management of initialization
//##################################################################




initializer[boolean const_requested, Type type, int init_level]
returns [Literal literal]
 {
   EnrichedType t_expr=null;
   literal=null;
 }
        : #( NInitializer 
	     (
	       t_expr=e:expr
	       {
		 Type t_init=t_expr.getType().unqualify();
		 
		 if (type.isArray()) {
		   
		   if (
		       type.getElementType().isCharScalar() &&
		       t_init.isArray() &&t_init.getElementType().isCharScalar() &&
		       t_expr.isConstantString()
		       ) {
		     // Specific case for array of chars which can be initialized
		     // through a constant string which is an expr and not a
		     // lcurlyInitializer
		     
		     // It can only be a constant string, otherwise, we would have gone
		     // to lcurlyInitializer
		     Array the_type=(Array)type.unqualify();
		     if (the_type.hasSizeDefined()) {
		       // Check string size
		       if (((Array)t_init).getNbElements()>the_type.getNbElements()) {
			 Warning(e, "initializer-string for array of chars is too long");
		       }
		     }
		     else {
		       // Set the array size in case of incomplete array at level 0
		       if (init_level==0) {
			 // Add 1 for the '\0' terminal character
			 the_type.setNbElements(((Array)t_init).getNbElements()+1);
		       }
		     }
		     
		     // Will create an expr literal (even if it is an array,
		     // since the string is given as an unique token)
		     // [TBW] should create an array literal)
		   }
		   else {
		     // Not a string literal
		     Error(##,"invalid initializer for array");
		   }
		 }
		 
		 else if (type.isStructOrUnion()) {
		   // For struct and union, it should be lcurlyInitializer
		   // or an object of the same type
		   if (type.unqualify()!=t_init) {
		     Error(##,"invalid initializer for struct or union type");
		   }
		 }
		 
		 else if (type.isVector()) {
		   // It is a vector type. It can be assigned from an expression
		   typeManager.checkAssignOperands(e,compilerError,
						   type,t_init,"initialization");
		   
		   // [TBW] Check if possible non constant initializer is allowed
		   //       -> when constant vectors will be handled
		   //if (const_requested && (!t_expr.isConstantScalar())) {
		   //  Error(##,"initializer element is not constant");
		   //}
		 }
		 else  {
		   // It is a scalar type. It can be assigned from an expression
		   typeManager.checkAssignOperands(e,compilerError,
						   type,t_expr,"initialization");
		   // Check if possible non constant initializer is allowed
		   if (const_requested && (!t_expr.isConstantScalar())) {
		     Error(##,"initializer element is not constant 1");
		   }
		   
		 }
		 // Create an expression literal
		 literal=new ExprLiteral(type);
		 ((ExprLiteral)literal).setEnrichedType(t_expr);
		 literal.setAstNode(e); // Link to AST
	       }
	     | literal=lcurlyInitializer[const_requested,type,init_level]
	     )
	   )
	 {
	   // Sets the type of the node as the target type of the initializer
	   ##.setDataType(new EnrichedType(type));
	 }
        ;

// Not an initializer, because only used in expressions, but syntactically close
// to 'lcurlyInitializer'
lparenthesisInitializer [boolean const_requested, Vector vector]
returns [VectorLiteral literal]
 {
   EnrichedType t_expr=null;
   Type vectorBaseType=vector.getBaseType();
   literal=new VectorLiteral(vector);
   int vector_size=vector.getNbElements();
   int index=0;
   boolean flag=false;
 }
	:  #( nl:NLparenthesisInitializer 
	        (
		 t_expr=e:expr 
		 {
		   Type t_init=t_expr.getType().unqualify();

		   if (t_init.isVector()) {
		     // Allowed only if same base type. The size may be different here
		     if (((Vector)t_init).getBaseType()!=vectorBaseType) {
		       Error(e,"incompatible types in vector literal ([OCL] no implicit conversion between vectors)");
		     }

		     // [TBW] we should copy all elements of the sub-literal into the
		     // current literal. Today, we put nothing.
		     // pb: it can be the result of an expression, so the case is
		     //     different than for arrays and structs which have a
		     //     C syntax to specify sub-literals
		     index+=((Vector)t_init).getNbElements();

		     // No need for out-of-bound check (will be done later)

		     // Add the sub-literal to the vector literal
		     VectorSubLiteral subLiteral=new VectorSubLiteral((Vector)t_init);
		     subLiteral.setEnrichedType(t_expr);
		     subLiteral.setAstNode(e); // Link to AST
		     literal.add(subLiteral);
		   }
		   else {
		     // Check for type compatibility
		     typeManager.checkAssignOperands(e,compilerError,
		         vectorBaseType,t_init,
		         "vector literal");
		     // Check if possible non constant initializer is allowed
		     if (const_requested && (!t_expr.isConstantScalar())) {
		       Error(e,"vector initializer element is not constant");
		     }

		     if (index<vector_size) {
		       // Create the sub-literal only if the index is within the
		       // vector size

		       // Create a literal. It is necessarily a scalar since the
		       // base type of vectors is always a scalar
		       ExprLiteral subLiteral=new ExprLiteral(vectorBaseType);
		       subLiteral.setEnrichedType(t_expr);
		       subLiteral.setAstNode(e); // Link to AST

		       // Add the sub-literal to the vector literal
		       literal.add(subLiteral);
		     }
		     index++;
		   }
		 }
		 )+
                RPAREN
            )
            {
	      if (index!=vector_size) {
		if (index==1) {
		  // Only one scalar element has been specified, so it's ok
		}
		else if (index>vector_size) {
		  // Error: too many elements in the literal
		  Error(nl, "excess elements in vector literal");
		}
		else {
		  // Error: not enough elements in the literal
		  Error(nl, "missing elements in vector literal");
		}
	      }
	    }
        ;

lcurlyInitializer[boolean const_requested, Type type, int level]
returns [Literal literal]
 {
   literal=null;
 }
	:  #( n:NLcurlyInitializer

              (   {type.isArray()}? 
		  literal=array_initializerList [const_requested,
						 (Array) (type.unqualify()) ,level] 
	          {
		    // Sets the AST node corresponding to the literal
		    literal.setAstNode(n);
	          }
	        | {type.isStruct()}?
		  literal=struct_initializerList[const_requested,
						 (Struct)(type.unqualify()) ,level]
	          {
		    // Sets the AST node corresponding to the literal
		    literal.setAstNode(n);
	          }
	        | {type.isUnion()}?
		  literal=union_initializerList [const_requested,
						 (Union) (type.unqualify()) ,level]
	          {
		    // Sets the AST node corresponding to the literal
		    literal.setAstNode(n);
	          }
		| literal=scalar_initializerList[const_requested,
						 (type.unqualify()) ,level]
	      )
              RCURLY
            )
        ;


array_initializerList[boolean const_requested, Array type, int level]
returns [AggregateLiteral literal]
 {
   int index=0;
   int nb_elements=0;
   Type subtype=type.getElementType();
   literal=new ArrayLiteral(type);
   Range range=null;
   EnrichedType t_expr=null;
   boolean isRange=false;
   Literal subLiteral=null;
 }
	: ( 
	    (
	      // More convenient to inline 'initializerElementLabel'
	      #( n:NInitializerElementLabel
                 (
		    // For arrays
		   ( l:LBRACKET
		      ( 

		        range=rangeExpr["initializer index"]
			{
			  isRange=true;
			  if ((range.getInfValue()<0)||(range.getSupValue()<0)) {
			    Error(n,"negative index in initializer");
			    // reset index
			    index=0;
			  }
			  else {
			    // Update the index to the last range value
			    index=range.getSupValue();
			  }
			}

		      | t_expr=constExpr
			{
			  if (!t_expr.getType().isIntegralScalar()) {
			    Error(n,"non integral value for index in initializer");
			    // dummy index
			    index=0;
			  }
			  else if (!t_expr.isConstantIntegral()) {
			    Error(n,"non constant value for index in initializer");
			    // dummy index
			    index=0;
			  }
			  else {
			    // Update the index to the last range value
			    index=t_expr.getConstantIntegralValue().intValue();
			    // for negative index
			    if (index<0) {
			      Error(n,"negative index in initializer");
			      // Dummy index
			      index=0;
			    }
			  }
			}
		      )
		      RBRACKET (ASSIGN)?
		      {
			if (type.hasSizeDefined()) {
			  if (index>=type.getNbElements()) {
			    // Succeptible to create compiler internal errors
			    FatalError(l,"array index in initializer exceeds array bounds");
			  }
			}
		      }
		    )

		    // For struct and union
                  | ( ID COLON | DOT ID ASSIGN)
		      {
			Error(##,"field name not in struct or union initializer");
			// reset index
			index=0;
		      }
                 )
	       )
	      {
		Warning(2,n, "ISO C89 forbids specifying subobject to initialize");
	      }
	    )?

	    subLiteral=init:initializer[const_requested, subtype, level+1]
	    {
	      // Check that we are not out of bound (only for arrays with
	      // size defined)
	      if (type.hasSizeDefined()) {
		if (index>=type.getNbElements()) {
		  Warning(init, "excess elements in array initializer");
		}
	      }

	      // Sets the literal
	      if (isRange) {
		literal.addAtIndexRange(range.getInfValue(),
					range.getSupValue(),
					subLiteral);
	      }
	      else {
		literal.addAtIndex(index,subLiteral);
	      }

	      // Increments index for next initializer
	      index++;

	      // Compute the number of elements
	      if (index>nb_elements) {
		nb_elements=index;
	      }
	      
	      // Reset isRange
	      isRange=false;
	    }
	  )*
	  {
	    // Potentially need to complete the incomplete array declaration
	    if (level==0) {
	      if (!type.hasSizeDefined()) {
		type.setNbElements(nb_elements);
	      }
	    }
	  }
        ;


struct_initializerList[boolean const_requested, Struct type, int level]
returns [AggregateLiteral literal]
 {
   Type subtype=type; // temporary
   String field_name=null;
   int fieldNumber=0;
   literal=new StructOrUnionLiteral(type);
   Range range;
   EnrichedType null_expr;
   Literal subLiteral=null;
 }
	: ( 
	    (
	      // More convenient to inline 'initializerElementLabel'
	      #( n:NInitializerElementLabel
                 (
		    // For arrays
                    (
		      LBRACKET
		      ( range=rangeExpr["initializer"] | null_expr=constExpr)
		      RBRACKET (ASSIGN)?
		    )
		      { Error(##,"array index in non-array initializer"); }

		    // For struct and union
                    | ( id1:ID COLON {field_name=id1.getText();}
     		         {
			   Warning(2,id1, "ISO C89 forbids specifying subobject to initialize");
			   field_name=id1.getText();
			   fieldNumber=type.getFieldNumber(field_name);
			   if (fieldNumber<0) {
			     FatalError(id1,"unknown field '" + field_name +
					"' specified in struct initializer");
			   }
			 }
		       | DOT id2:ID ASSIGN
     		         {
			   Warning(2,id2, "ISO C89 forbids specifying subobject to initialize");
			   field_name=id2.getText();
			   fieldNumber=type.getFieldNumber(field_name);
			   if (fieldNumber<0) {
			     FatalError(id2,"unknown field '" + field_name +
					"' specified in struct initializer");
			   }
			 }
			)
		  )
	       )
	      {
		Warning(2,n, "ISO C89 forbids specifying subobject to initialize");
	      }
	    )?
	    {
	      // Fatal error in case of wrong field. Here, fieldNumber is
	      // greater or equal than 0
	      subtype=type.getFieldType(fieldNumber);
	      // subtype is null if it exceeds the number of elements
	    }
	    ( {subtype!=null}? subLiteral=initializer[const_requested, subtype,level+1]
	      		       {
				 // Sets the literal
				 literal.addAtIndex(fieldNumber,subLiteral);
			       }
	      |                init:initializer_nocheck
	                       { Warning(init, "excess elements in struct initializer"); }
	    )
	    {
	      // Position to the next field
	      fieldNumber++;
	    }
	  )*
        ;



union_initializerList[boolean const_requested, Union type, int level]
returns [AggregateLiteral literal]
 {
   int count=0;
   Type subtype=type; // temporary  
   String field_name=null;
   int fieldNumber=0;
   literal=new StructOrUnionLiteral(type);
   Range range;
   EnrichedType null_expr;
   Literal subLiteral=null;
 }
	: ( 
	    (
	      // More convenient to inline 'initializerElementLabel'
	      #( n:NInitializerElementLabel
                 (
		    // For arrays
                    ( 
		      LBRACKET 
		      ( range=rangeExpr["initializer"] | null_expr=constExpr)
		      RBRACKET (ASSIGN)?
		    )
		      { Error(##,"array index in non-array initializer"); }

		    // For struct and union
                    | ( id1:ID COLON
     		         {
			   field_name=id1.getText();
			   fieldNumber=type.getFieldNumber(field_name);
			   if (fieldNumber<0) {
			     FatalError(id1,"unknown field '" + field_name +
					"' specified in union initializer");
			   }
			 }
		       | DOT id2:ID ASSIGN
     		         {
			   field_name=id2.getText();
			   fieldNumber=type.getFieldNumber(field_name);
			   if (fieldNumber<0) {
			     FatalError(id2,"unknown field '" + field_name +
					"' specified in union initializer");
			   }
			 }
		       )
                   )
	       )
	      {
		Warning(2,n, "ISO C89 forbids specifying subobject to initialize");
	      }
	    )?
	    {
	      // Fatal error in case of wrong field. Here, fieldNumber is
	      // greater or equal than 0
	      subtype=type.getFieldType(fieldNumber);
	      // subtype is null if it exceeds the number of elements
	    }
	    ( {subtype!=null}? subLiteral=init1:initializer[const_requested,
							    subtype,level+1]
	      	  {
		    if (count>0) {
		      Warning(init1,"excess elements in union initializer");
		    }
		    
		    // Sets the literal
		    literal.addAtIndex(fieldNumber,subLiteral);
		  }
	      |  init2:initializer_nocheck
	         { Warning(init2, "excess elements in union initializer"); }
	    )
	    {
	      count++;

	      // Position to the next field
	      fieldNumber++;
	    }
	  )*
        ;


scalar_initializerList[boolean const_requested, Type type, int level]
returns [Literal literal]
 {
   int count=0;
   literal=null;
   Range range;
   EnrichedType null_expr;
 }
	: ( 
	    (
	      // More convenient to inline 'initializerElementLabel'
              #( NInitializerElementLabel
                 (
		    // For arrays
                    ( 
		      LBRACKET
		      ( range=rangeExpr["initializer"] | null_expr=constExpr)
		      RBRACKET (ASSIGN)?
                     )
		      {	Error(##,"array index in non-array initializer"); }

		    // For struct and union
                  | (ID COLON | DOT ID ASSIGN)
		      { Error(##,"field name not in struct or union initializer"); }
                 )
	       )
	    )? 
	    ( {count==0}? literal=initializer[const_requested,type,level+1]
	      |           init:initializer_nocheck
	                  { Warning(init, "excess elements in scalar initializer"); }
	    )
	    {
	      count++;
	    }
	  )*
        ;



//---------------------------------------------
//--  No check for exceeding initialization  --
//---------------------------------------------
initializer_nocheck
{ EnrichedType t_expr; }
        :  #( NInitializer 
	      ( t_expr=expr| lcurlyInitializer_nocheck )
	     )
        ;

lcurlyInitializer_nocheck
        :  #( NLcurlyInitializer
                initializerList_nocheck
                RCURLY
            )
        ;

initializerElementLabel_nocheck
{ EnrichedType t_expr; }
        :   #( NInitializerElementLabel
                (
                    ( LBRACKET t_expr=expr RBRACKET (ASSIGN)? )
                    | ID COLON
                    | DOT ID ASSIGN
                )
            )
        ;

initializerList_nocheck
	:  ( 
	       (initializerElementLabel_nocheck)? 
	       initializer_nocheck
	    )*
        ;




//##################################################################
// Management of goto
//##################################################################

statementBody[boolean input_for_declaration]
returns [EnrichedType etype]
{
  // By default, the statement has type 'void'
  etype=new EnrichedType(Void.Tvoid);
  // Other etypes
  EnrichedType null_etype;
  EnrichedType t_expr=null;

  // For 'return' statement
  boolean flag=false;
  // For 'case' statment
  Range range=null;
  // For 'for' statement
  boolean for_declaration=false;
}
        :       SEMI                    // Empty statements

        |       null_etype=compoundStatement[input_for_declaration] // Group of statements

        |       #(NStatementExpr etype=expr)   // Expressions

// Iteration statements:

        |       #( "while" t_expr=expr 
	    	    {
		      // the condition must be a scalar
		      if (!t_expr.getType().isScalarOrLabel()) {
			Error(##,"invalid type for test condition of 'while' statement");
		      }
		    }
		   null_etype=statement[false]
		 )

        |       #( "do"
		   null_etype=statement[false]
		   t_expr=expr
	    	    {
		      // the condition must be a scalar
		      if (!t_expr.getType().isScalarOrLabel()) {
			Error(##,"invalid type for test condition of 'do-while' statement");
		      }
		    }
		 )

        |      #( "for"
                   (   null_etype=expr
		     |
		       {
			 symbolTable.pushScope();
			 for_declaration=true;
			 is_for_declaration=true;

		       }
		       for_declaration
		       {
			 is_for_declaration=false;
		       }
                   )
		   t_expr=expr
	    	    {
		      // the condition must be a scalar
		      if (!t_expr.getType().isScalarOrLabel() && !t_expr.getType().isVoid()) {
			Error(##,"invalid type for test condition of 'for' statement");
		      }
		    }
		   null_etype=expr
                   null_etype=statement[for_declaration]
	           {
		     if (for_declaration) {
		       symbolTable.popScope();
		     }
		   }
                 )

// Jump statements:

	| #( "goto" id_ref:ID )
	     {
	       // Lookup in the symbol table and set 'id_ref'
	       Symbol s=lookupCodeLabelAndSetReference(#id_ref);
	       
	       if (s==null) {
		 // No symbol yet defined. Since he forward reference is allowed for
		 // labels, create a symbol
		 
		 // Create a new symbol
		 String declName = #id_ref.getText();
		 CodeLabel symbol = new CodeLabel(declName);
		 
		 // It shall be a forward reference to a label that is
		 // defined later
		 symbol.setReference();
		 
		 // Sets the 'void *' type
		 symbol.setType(new Pointer(Void.Tvoid));
		 
		 // Link to AST
		 symbol.setIdNode((NodeAST)#id_ref);
		 
		 // Add the symbol to the symbol table
		 addCodeLabel(declName,symbol);
		 
		 // Link AST -> symbol
		 ((NodeAST)#id_ref).setReference(symbol);
	       }
	     }

        | "continue" 

        | "break"

        | #( "return" ( t_expr=expr {flag=true;} )? )
	     {
	       if (flag) {
		 // An expression is returned, check the type of the expression with
		 // regard to the function prototype
		 if (currentFunctionDefReturnType.isVoid()) {
		   // The function is not supposed to return a value
		   Warning(##,"`return' with a value, in function returning void");
		 }
		 else {
		   // Check compatibility
		   typeManager.checkAssignOperands(##,compilerError,
						   currentFunctionDefReturnType,t_expr,
						   "return");
		 }
	       }
	       else {
		 // No expression is returned
		 if (!currentFunctionDefReturnType.isVoid()) {
		   // The function is not supposed to return a value
		   Warning(##,"`return' with no value, in function returning non-void");
		 }
		 // else ok
	       }
	     }

// Labeled statements:
	| #( NLabel id_node:ID (null_etype=statement[false])? )
	  {
	    CodeLabel s=lookupCodeLabel(#id_node);
	    if (s==null) {
	      // Create a new symbol
	      String declName = id_node.getText();
	      CodeLabel symbol = new CodeLabel(declName);
	      
	      // Sets 'void *' type
	      symbol.setType(new Pointer(Void.Tvoid));
	      
	      // Link -> AST
	      symbol.setIdNode((NodeAST)#id_node);
	      
	      // Add the symbol to the symbol table
	      addCodeLabel(declName,symbol);
	      
	      // Link AST -> symbol
	      ((NodeAST)#id_node).setDefinition(symbol);
	    }
	    
	    else {
	      // A symbol already exists
	      if (s.isDefinition()) {
		Message(id_node,"redefinition of label '" + id_node.getText() + "'");
		Error(s.getIdNode(),"label '" + id_node.getText() +
		      "' previously declared here");
	      }
	      else {
		// It's a reference
		// Changes its id-node reference
		s.setIdNode((NodeAST)#id_node);
		// sets it as a definition
		s.setDefinition();
		// Sets the definition
		((NodeAST)#id_node).setDefinition(s);
	      }
	    }
		}
        | #( "case" 
	     (
	      range=rangeExpr["case label"]
	      // Check for constant integral value already done in the range
	      | t_expr=ce:constExpr
	        {
		  // Check for constant integral value
		  if (!t_expr.getType().isIntegralScalar()) {
		    Error(#ce,"non integral value in case label");
		  }
		  else if (!t_expr.isConstantIntegral()) {
		    Error(#ce,"non constant value in case label");
		  }
		}
	     )
	     (null_etype=statement[false])?
	   )
	  // Additional checks [TBW]

        | #( "default" (null_etype=statement[false])? )



// Selection statements:

        | #( "if"
              t_expr=expr 
	      {
		// The test condition must be a scalar
		if (!t_expr.getType().isScalarOrLabel()) {
		  Error(##,"invalid operand for test condition");
		}
	      }
	      null_etype=statement[false]  
              ( "else" null_etype=statement[false] )?
           )
        | #( "switch" t_expr=expr 
	      {
		// The 'switch quantity' must be integral
		if (!t_expr.getType().isIntegralScalar()) {
		  Error(##,"switch quantity not an integer");
		}
	      }
	     null_etype=statement[false]
	   )
        ;




//##################################################################
// Specifiers and qualifiers for function, variables and typedefs
//##################################################################


// 'alone' tells if a declarator is associated to the specifier
declSpecifiers[boolean alone,
	       boolean tag_split,
	       boolean isParamList,
	       TypeSpecifierQualifier specifier_qualifier,
	       StorageClass storageclass,
	       LinkedList<Symbol> new_def,
	       LinkedList<Symbol> tag_ref,
	       LinkedList<Symbol> new_def_param]
        :
	        (   storageClassSpecifier[storageclass]
                  | typeQualifier[specifier_qualifier]
		  | typeSpecifier[alone,
				  tag_split,
				  isParamList,
				  specifier_qualifier,
				  new_def,tag_ref,new_def_param]
		  | attributeSpecifier
                )+
        ;

functionDeclSpecifiers[boolean tag_split,
		       FunctionLabel function_symbol,
		       TypeSpecifierQualifier specifier_qualifier,
		       StorageClass storageclass,
		       LinkedList<Symbol> new_def,
		       LinkedList<Symbol> tag_ref,
		       LinkedList<Symbol> new_def_param]
        :
              (   functionStorageClassSpecifier[storageclass]
                | typeQualifier[specifier_qualifier]
		| typeSpecifier[false,
				tag_split,
				false,
				specifier_qualifier,
				new_def,tag_ref,new_def_param]
		| oclFunctionQualifier[function_symbol]
		| attributeSpecifier
              )+
        ;

oclFunctionQualifier[FunctionLabel function_symbol]

        :       "__kernel" ( kernelAttributeSpecifierList[function_symbol] )?
		{
		  function_symbol.setKernel();
		}
        ;

//-----------------------  kernel attribute  ---------------------------

kernelAttributeSpecifierList[FunctionLabel function_symbol]
        :! ( kernelAttributeSpecifier[function_symbol] ) +
        ;

kernelAttributeSpecifier[FunctionLabel function_symbol]
        :
        #( "__attribute"
           kernelAttribute[function_symbol]
        )
        ;

kernelAttribute[FunctionLabel function_symbol]
        {
	  BigInteger b0,b1,b2,s;
	  int d0=-1,d1=-1,d2=-1;
	}
	:  id:ID
	   (
	     {id.getText().equals("vec_type_hint")}?
	       ( (LPAREN builtinTypeName RPAREN) =>
		 LPAREN builtinTypeName RPAREN
		 | (.)*
		 { Error(id,"Wrong parameter(s) for kernel attribute '"+id.getText()+"'");}
	       )
	   | {id.getText().equals("work_group_size_int")}?
	       ( (LPAREN integerNumber COMMA integerNumber COMMA integerNumber RPAREN) =>
		  LPAREN b0=integerNumber COMMA b1=integerNumber COMMA b2=integerNumber RPAREN
		  {
		    d0=b0.intValue();d1=b1.intValue();d2=b2.intValue();
		    function_symbol.setAttribute_work_group_size_hint(d0,d1,d2);
		  }
               | (.)*
		 { Error(id,"Wrong parameter(s) for kernel attribute '"+id.getText()+"'");}
	       )
	   | {id.getText().equals("reqd_work_group_size")}?
	       ( (LPAREN integerNumber COMMA integerNumber COMMA integerNumber RPAREN) =>
		  LPAREN b0=integerNumber COMMA b1=integerNumber COMMA b2=integerNumber RPAREN
		 {
		   d0=b0.intValue();d1=b1.intValue();d2=b2.intValue();
		   function_symbol.setAttribute_reqd_work_group_size(d0,d1,d2);
		 }
               | (.)*
		 { Error(id,"Wrong parameter(s) for kernel attribute '"+id.getText()+"'");}
	       )
	   | {id.getText().equals("stack_size")}?
	       ( (LPAREN integerNumber RPAREN) =>
		 LPAREN s=integerNumber RPAREN
	         {
		   // The stack size must be aligned on int
		   int intSize=Type.getTargetABI().getIntSize();
		   long stackSize=s.longValue();
		   if (stackSize%intSize != 0) {
		     Error(id,"The stack size must be a multiple of "+intSize+" bytes");
		   }
		   function_symbol.setAttribute_stack_size(stackSize);
		 }
		 | (.)*
		 { Error(id,"Wrong parameter(s) for kernel attribute '"+id.getText()+"'");}
		 )
	   | {id.getText().equals("stack_in_extmem")}?
	       ( (.)+
		 { Error(id,"attribute '"+id.getText()+"' does not take any parameter");}
	       | 
	         { unsized_stack_requested=true;}		 
	       )
	   | (.)*
	     { Error(id,"Unknown kernel attribute '"+id.getText()+"'");}
	   )
        ;

integerNumber
returns [BigInteger value]
{
  value=BigInteger.ZERO;
}
	: n:IntegralNumber
	{
	  EnrichedType etype=typeManager.getIntegralNumberEnrichedType(n,compilerError,n.getText());
	  // Some checking
	  if (!etype.getType().isIntScalar()) {
	    Error(n,"expecting an int constant");
	  }
	  else {
	    value=etype.getConstantIntegralValue();
	  }
	}
        ;

builtinTypeName
returns [Type type] 
	{
	  type=null;
	  TypeSpecifierQualifier specifier_qualifier
	    = new TypeSpecifierQualifier();
	}
        : ds:builtinTypeSpecifier[specifier_qualifier]
	  {
	    type=specifier_qualifier.getType(ds,compilerError);
	  }
         ;

//----------------------------------------------------------------------


storageClassSpecifier[StorageClass storageclass]
	:       a:"auto"
		{
		  storageclass.setAuto(a,compilerError);

		  // OCL does not support 'auto'
		  if ((oclLanguage)&&(!oclCCompatibilityOption)) {
		    Error(#a, "OCL does not support the 'auto' storage class specifier");
		  }
		}
	|       r:"register"
		{
		  storageclass.setRegister(r,compilerError);
		  // OCL does not support 'register'
		  if ((oclLanguage)&&(!oclCCompatibilityOption)) {
		    Error(#r, "OCL does not support the 'register' storage class specifier");
		  }
		}
	|       e:"extern"
		{
		  storageclass.setExtern(e,compilerError);
		  // OCL does not support 'extern'
		  if ((oclLanguage)&&(!oclCCompatibilityOption)) {
		    Error(#e, "OCL does not support the 'extern' storage class specifier");
		  }
		}
	|       st:"static"
		{
		  storageclass.setStatic(st,compilerError);
		  //if (instance_data_extraction) {
		    // 1- Leave the 'static' information in the symbol for syntax
		    //    checking
		    // 2- leave the token in the AST for simplification
		    //=> but remove the text information for making it disappear in
		    //   the C regeneration
		  //  #st.setText("");
		  //}
		  // OCL does not support 'static'
		  if ((oclLanguage)&&(!oclCCompatibilityOption)) {
		    Error(#st, "OCL does not support the 'static' storage class specifier");
		  }
		}
	|       i:"inline"
		{
		  // Like static
		  storageclass.setInline(i,compilerError);
		}

	|       t:"typedef"
		{
		  storageclass.setTypedef(t,compilerError);
 		}
        ;

functionStorageClassSpecifier[StorageClass storageclass]
	:       e:"extern"
		{
		  storageclass.setExtern(e,compilerError);
		}
	|       st:"static"
		{
		  storageclass.setStatic(st,compilerError);
		  // nothing special for variables
		}
	|       i:"inline"
		{
		  // like static, the scope is the module
		  storageclass.setInline(i,compilerError);
		}
       ;

typeQualifier[TypeSpecifierQualifier specifier_qualifier]
	:
		c:"const"
		{
		  specifier_qualifier.setConst(c,compilerError);
		}
	|       v:"volatile"
		{
		  specifier_qualifier.setVolatile(v,compilerError);
		}
	|       r:"restrict"
		{
		  specifier_qualifier.setRestrict(r,compilerError);
		}
        |       oclAddressSpaceQualifier[specifier_qualifier]
        ;

oclAddressSpaceQualifier[TypeSpecifierQualifier specifier_qualifier]
	:       g:"__global"
		{ specifier_qualifier.setAddressSpace(g,compilerError,AddressSpace.GLOBAL); }
	|       c:"__constant"
 		{ specifier_qualifier.setAddressSpace(c,compilerError,AddressSpace.CONSTANT); }
        |       l:"__local"
 		{ specifier_qualifier.setAddressSpace(l,compilerError,AddressSpace.LOCAL); }
	|       p:"__private"
 		{ specifier_qualifier.setAddressSpace(p,compilerError,AddressSpace.PRIVATE); }
        ;


typeQualifierInDeclarator[Qualifier type_node]
	:
		c:"const"
		{
		  // Type management
		  type_node.setConst(c,compilerError);
		}
	|       v:"volatile"
		{
		  // Type management
		  type_node.setVolatile(v,compilerError);
		}
	|       r:"restrict"
		{
		  // Type management
		  type_node.setRestrict(r,compilerError);
		}
        |       oclAddressSpaceQualifierInDeclarator[type_node]
        ;

oclAddressSpaceQualifierInDeclarator[Qualifier type_node]
	:       g:"__global"
		{ type_node.setAddressSpace(g,compilerError,AddressSpace.GLOBAL); }
	|       c:"__constant"
 		{ type_node.setAddressSpace(c,compilerError,AddressSpace.CONSTANT); }
        |       l:"__local"
 		{ type_node.setAddressSpace(l,compilerError,AddressSpace.LOCAL); }
	|       p:"__private"
 		{ type_node.setAddressSpace(p,compilerError,AddressSpace.PRIVATE); }
        ;



builtinTypeSpecifier[TypeSpecifierQualifier specifier_qualifier]
	:       v:"void"
		{
		  specifier_qualifier.setVoid(v,compilerError);
		}
	|       c:"char"
		{
		  specifier_qualifier.setChar(c,compilerError);
		}
	|       s:"short"
		{
		  specifier_qualifier.setShort(s,compilerError);
		}
	|       i:"int"
		{
		  specifier_qualifier.setInt(i,compilerError);
		}
	|       l:"long"
		{
		  specifier_qualifier.setLong(l,compilerError);
		}
	|       f:"float"
		{
		  specifier_qualifier.setFloat(f,compilerError);
		}
	|       d:"double"
		{
		  specifier_qualifier.setDouble(d,compilerError);
		}
	|       si:"signed"
		{
		  specifier_qualifier.setSigned(si,compilerError);
		}
	|       un:"unsigned"
		{
		  specifier_qualifier.setUnsigned(un,compilerError);
		}
        |       b:"_Bool"
        	{
		  specifier_qualifier.setBool(b,compilerError);
		} 

        |       uc:"__uchar"
		{
		  specifier_qualifier.setChar(uc,compilerError);
		  specifier_qualifier.setUnsigned(uc,compilerError);
		}
        |       us:"__ushort"
		{
		  specifier_qualifier.setShort(us,compilerError);
		  specifier_qualifier.setUnsigned(us,compilerError);
		}
        |       ui:"__uint"
		{
		  specifier_qualifier.setInt(ui,compilerError);
		  specifier_qualifier.setUnsigned(ui,compilerError);
		}
        |       ul:"__ulong"
		{
		  specifier_qualifier.setLong(ul,compilerError);
		  specifier_qualifier.setUnsigned(ul,compilerError);
		}
        |       vectorSpecifier[specifier_qualifier]
	;

// 'alone' tells if a declarator is associated to the specifier
typeSpecifier[boolean alone,
	      boolean tag_split,
	      boolean isParamList,
	      TypeSpecifierQualifier specifier_qualifier,
	      LinkedList<Symbol> new_def,
	      LinkedList<Symbol> tag_ref,
	      LinkedList<Symbol> new_def_param]
   {
     TagSymbol tag_symbol=null;
     Symbol typedefname_symbol=null;
     Type   typeof_type=null;
     EnrichedType t_expr=null;
   }
	:       builtinTypeSpecifier[specifier_qualifier]
	|       tag_symbol = ss:structSpecifier[alone,tag_split,
						isParamList,
						new_def,tag_ref,new_def_param]
		{
		  specifier_qualifier.setStruct(ss,compilerError);
		  specifier_qualifier.setSubType(tag_symbol.getType());
		}
	|       tag_symbol = us:unionSpecifier[alone,tag_split,
					       isParamList,
					       new_def,tag_ref,new_def_param]
		{
		  specifier_qualifier.setUnion(us,compilerError);
		  specifier_qualifier.setSubType(tag_symbol.getType());
		}
	|       tag_symbol = es:enumSpecifier[alone,tag_split,
					      isParamList,
					      new_def,tag_ref]
		{
		  specifier_qualifier.setEnum(es,compilerError);
		  specifier_qualifier.setSubType(tag_symbol.getType());
		}
	|       typedefname_symbol=td:typedefName[tag_ref]
		{
		  specifier_qualifier.setTypedefName(td,compilerError);
		  specifier_qualifier.setSubType(typedefname_symbol.getType());
		}
	|       co:"__complex"
		{
		  specifier_qualifier.setComplex(co,compilerError);
		}
        // GNU extension
	|       #(to:"typeof" LPAREN
		  ( (typeName)=> typeof_type=typeName[tag_split,
						      new_def,tag_ref,new_def_param]
		      {
			specifier_qualifier.setTypeof(to,compilerError);
			specifier_qualifier.setSubType(typeof_type);
		      }
                    | t_expr=expr
		      {
			specifier_qualifier.setTypeof(to,compilerError);
			specifier_qualifier.setSubType(t_expr.getType());
		      }
                    )
                    RPAREN
                )
	|	va:"__builtin_va_list"
		{
		  specifier_qualifier.setValist(va,compilerError);
		}
        ;

vectorSpecifier[TypeSpecifierQualifier specifier_qualifier]
	: 
        (
	  "char2" { specifier_qualifier.setSubType(IntegerVector.Tschar2);}
        | "char3" { specifier_qualifier.setSubType(IntegerVector.Tschar3);}
        | "char4" { specifier_qualifier.setSubType(IntegerVector.Tschar4);}
        | "char8" { specifier_qualifier.setSubType(IntegerVector.Tschar8);}
        | "char16" { specifier_qualifier.setSubType(IntegerVector.Tschar16);}
	| "uchar2" { specifier_qualifier.setSubType(IntegerVector.Tuchar2);}
        | "uchar3" { specifier_qualifier.setSubType(IntegerVector.Tuchar3);}
        | "uchar4" { specifier_qualifier.setSubType(IntegerVector.Tuchar4);}
        | "uchar8" { specifier_qualifier.setSubType(IntegerVector.Tuchar8);}
        | "uchar16" { specifier_qualifier.setSubType(IntegerVector.Tuchar16);}

	| "short2" { specifier_qualifier.setSubType(IntegerVector.Tsshort2);}
        | "short3" { specifier_qualifier.setSubType(IntegerVector.Tsshort3);}
        | "short4" { specifier_qualifier.setSubType(IntegerVector.Tsshort4);}
        | "short8" { specifier_qualifier.setSubType(IntegerVector.Tsshort8);}
        | "short16" { specifier_qualifier.setSubType(IntegerVector.Tsshort16);}
	| "ushort2" { specifier_qualifier.setSubType(IntegerVector.Tushort2);}
        | "ushort3" { specifier_qualifier.setSubType(IntegerVector.Tushort3);}
        | "ushort4" { specifier_qualifier.setSubType(IntegerVector.Tushort4);}
        | "ushort8" { specifier_qualifier.setSubType(IntegerVector.Tushort8);}
        | "ushort16" { specifier_qualifier.setSubType(IntegerVector.Tushort16);}

	| "int2" { specifier_qualifier.setSubType(IntegerVector.Tsint2);}
        | "int3" { specifier_qualifier.setSubType(IntegerVector.Tsint3);}
        | "int4" { specifier_qualifier.setSubType(IntegerVector.Tsint4);}
        | "int8" { specifier_qualifier.setSubType(IntegerVector.Tsint8);}
        | "int16" { specifier_qualifier.setSubType(IntegerVector.Tsint16);}
	| "uint2" { specifier_qualifier.setSubType(IntegerVector.Tuint2);}
        | "uint3" { specifier_qualifier.setSubType(IntegerVector.Tuint3);}
        | "uint4" { specifier_qualifier.setSubType(IntegerVector.Tuint4);}
        | "uint8" { specifier_qualifier.setSubType(IntegerVector.Tuint8);}
        | "uint16" { specifier_qualifier.setSubType(IntegerVector.Tuint16);}

	| "long2" { specifier_qualifier.setSubType(IntegerVector.Tslong2);}
        | "long3" { specifier_qualifier.setSubType(IntegerVector.Tslong3);}
        | "long4" { specifier_qualifier.setSubType(IntegerVector.Tslong4);}
        | "long8"  { specifier_qualifier.setSubType(IntegerVector.Tslong8);}
        | "long16" { specifier_qualifier.setSubType(IntegerVector.Tslong16);}
	| "ulong2" { specifier_qualifier.setSubType(IntegerVector.Tulong2);}
        | "ulong3" { specifier_qualifier.setSubType(IntegerVector.Tulong3);}
        | "ulong4" { specifier_qualifier.setSubType(IntegerVector.Tulong4);}
        | "ulong8" { specifier_qualifier.setSubType(IntegerVector.Tulong8);}
        | "ulong16" { specifier_qualifier.setSubType(IntegerVector.Tulong16);}

	| "float2" { specifier_qualifier.setSubType(FloatingPointVector.Tfloat2);}
        | "float3" { specifier_qualifier.setSubType(FloatingPointVector.Tfloat3);}
        | "float4" { specifier_qualifier.setSubType(FloatingPointVector.Tfloat4);}
        | "float8" { specifier_qualifier.setSubType(FloatingPointVector.Tfloat8);}
        | "float16" { specifier_qualifier.setSubType(FloatingPointVector.Tfloat16);}
	)
	{
	  specifier_qualifier.setVector(##,compilerError);
	}
        ;




//##################################################################
//                  Struct and union management
//##################################################################



// 'alone' tells if a declarator is associated to the specifier
structSpecifier[boolean alone,
		boolean tag_split,
		boolean isParamList,
		LinkedList<Symbol> new_def,
		LinkedList<Symbol> tag_ref,
		LinkedList<Symbol> new_def_param]
returns [TagSymbol tag_symbol]
        {
	  tag_symbol=null;
	}
	:   #( "struct"
	       (attributeSpecifierList)?
	       tag_symbol=structOrUnionBody[alone,
					    tag_split,
					    isParamList,
					    new_def,tag_ref,new_def_param,
					    true]
	      )
        ;



// 'alone' tells if a declarator is associated to the specifier
unionSpecifier[boolean alone,
	       boolean tag_split,
	       boolean isParamList,
	       LinkedList<Symbol> new_def,
	       LinkedList<Symbol> tag_ref,
	       LinkedList<Symbol> new_def_param]
returns [TagSymbol tag_symbol]
        {
	  tag_symbol=null;
	}
	:   #( "union"
	       (attributeSpecifierList)?
	       tag_symbol=structOrUnionBody[alone,
					    tag_split,
					    isParamList,
					    new_def,tag_ref,new_def_param,
					    false]
	      )
        ;


structOrUnionBody[boolean alone,
		  boolean tag_split,
		  boolean isParamList,
		  LinkedList<Symbol> new_def,
		  LinkedList<Symbol> tag_ref,
		  LinkedList<Symbol> new_def_param,
		  boolean isStruct
		  ]
returns [TagSymbol tag_symbol]
	{
	  // Incomplete
	  boolean    incompletedefinition_or_reference = false;
	  boolean    incomplete_tag = false;
	  // Complete
	  boolean    noname     = false;
	  boolean    error      = false;
	  LinkedList<Symbol> my_tag_ref = new LinkedList<Symbol>();
	  // All
	  tag_symbol = null;

	  // Create a type, by default uncomplete
	  StructOrUnion structorunion_type;
	  if (isStruct) {
	    structorunion_type=(StructOrUnion)new Struct();
	  }
	  else {
	    structorunion_type=(StructOrUnion)new Union();
	  }
	}
	:       ( (ID LCURLY) => id_node:ID
		    LCURLY
	    	    {
		      // It will be a 'complete' tag declaration
		      //----------------------------------------

		      // Lookup existing tag with such name in the symbol table
		      TagSymbol symb=lookupTagInCurrentScope(#id_node);

		      if (symb!=null) {
			// A declaration of the tag already exists in the current scope
			if (symb.getType().isComplete()) {
			  // A complete declaration already exists in the current scope
			  // There is a conflict
			  Error(  #id_node, " symbol '" 
				+ #id_node.getText()
				+ "' redefined, previous declaration line "
				+ symb.getIdNode().getLineNum()
				);
			  error=true;
			}
			else {
			  // An incomplete declaration already exists in the current scope
			  // Check that it is the same type
			  if (
			      ( ( isStruct) && (!(symb instanceof StructTag)) ) ||
			      ( (!isStruct) && (!(symb instanceof UnionTag )) )
			      ) {
			    // It is not the same type of tag, it's an error
			    Error(  #id_node, " complete definition type "
				  + #id_node.getText()
				  + " non coherent with previous incomplete definition "
				  );
			    error=true;
			  }
			  else {
			    // The incomplete declaration is compatible
			    // We will simply complete the existing incomplete type
			    structorunion_type=(StructOrUnion)symb.getType();
			  }
			}
		      }

		      // Declaration in for-loop seems to be authorized in C99 (spec unclear)
		      // We do not split yet for-loop declarations. Then, possible variable
		      // referencing a type tag declared in for-loop declaration can not be
		      // extracted
		      // => Type tag declaration forbidden in a for-loop declaration. 
		      if (tag_split && is_for_declaration) {
			if (isStruct) {
			  InternalError(  #id_node, "'struct declaration ('struct "
					+ #id_node.getText()
					+ "') in for loop initial declaration not supported"
					);
			}
			else {
			  InternalError(  #id_node, "'union declaration ('union "
					+ #id_node.getText()
					+ "') in for loop initial declaration not supported"
					);
			}
			// error=true; no need to raise a second error wirh incomplete parameter incomplete
		      }

		      // Should not declare types in function parameters
		      if (isParamList) {
			if (isStruct) {
			  Warning(  #id_node, "'struct "
				  + #id_node.getText() + "' declared inside parameter list");
 			}
			else {
			  Warning(  #id_node, "'union "
				  + #id_node.getText() + "' declared inside parameter list");
			}
			Warning(#id_node, "its scope is only this definition or declaration, which is probably not what you want");
		      }


		      // Create a new symbol (by default incomplete)
		      //--------------------------------------------
		      String declName = #id_node.getText();

		      // Sets the type
		      if (isStruct) {
			tag_symbol = new StructTag(declName);
		      }
		      else {
			tag_symbol = new UnionTag(declName);
		      }

		      // Sets pointers to AST
		      tag_symbol.setIdNode((NodeAST)#id_node);
		      
		      // Sets the type
		      structorunion_type.setSignatureName(#id_node.getText());
		      tag_symbol.setType(structorunion_type);

		      // Add the incomplete symbol to the symbol table
		      addTagSymbol(declName,tag_symbol);
		    }

		    ( 
		     structOrUnionDeclarationList[tag_split,
						  isParamList,
						  new_def,my_tag_ref,new_def_param,
						  structorunion_type,
						  isStruct]
		     )?

                    RCURLY

                  | curly:LCURLY
	            {
		      // Anonymous tag declaration
		      //--------------------------

		      // Declaration in for-loop seems to be authorized in C99 (spec unclear)
		      // We do not split yet for-loop declarations. Then, possible variable
		      // referencing a type tag declared in for-loop declaration can not be
		      // extracted
		      // => Type tag declaration forbidden in a for-loop declaration. 
		      if (tag_split && is_for_declaration) {
			if (isStruct) {
			  InternalError(#curly, "struct declaration (anonymous) in for loop initial declaration not supported");
			}
			else {
			  InternalError(#curly, "union declaration (anonymous) in for loop initial declaration not supported");
			}
			// error=true; no need to raise a second error wirh incomplete parameter incomplete
		      }

		      // Should not declare types in function parameters
		      if (isParamList) {
			if (isStruct) {
			  Warning(#curly,"anonymous struct declared inside parameter list");
 			}
			else {
			  Warning(#curly,"anonymous union declared inside parameter list");
			}
			Warning(#curly, "its scope is only this definition or declaration, which is probably not what you want");
		      }


		      // Create a new symbol with a new name
		      noname          = true;
		      String declName = getNewName();

		      // Sets the family
		      if (isStruct) {
			tag_symbol=new StructTag(declName);
		      }
		      else {
			tag_symbol=new UnionTag(declName);
		      }

		      // Set the type
		      structorunion_type.setSignatureName(declName);
		      tag_symbol.setType(structorunion_type);

		      // No ID node, so no pointer to AST

		      // Add the symbol to the symbol table (if it raise an error, it's
		      // an internal error)
		      addTagSymbol(declName,tag_symbol);
		    }

		    ( structOrUnionDeclarationList[tag_split,
						   isParamList,
						   new_def,my_tag_ref,new_def_param,
						   structorunion_type,
						   isStruct]
		    )?

                    RCURLY

		  | id_ref:ID
		  {
		    // It's a incomplete declaration
		    //------------------------------
		    incompletedefinition_or_reference=true;
		    if (alone) {
		      // The type specifier is NOT combined to an 'initDecl'
		      // (variable, proto, typedef). It is an incomplete type
		      // declaration if no complete declaration in the current scope
		      tag_symbol=lookupTagInCurrentScope(#id_ref);
		      if (tag_symbol==null) {
			// No other declaration in the current scope
			// It's an incomplete tag definition (which potentially overides a
			// complete declaration in a parent scope)
			incomplete_tag=true;
		      }
		      else {
			// Otherwise, an other complete or incomplete declaration
			// exists in the current scope. the current one is simply
			// a reference to the previous one
			if (
			    ( ( isStruct) && (!(tag_symbol instanceof StructTag)) ) ||
			    ( (!isStruct) && (!(tag_symbol instanceof UnionTag )) )
			    ) {
			  // Not the same kind of tag
			  Error(id_ref,"'"+id_ref.getText()+"'"+" defined as wrong kind of tag");
			}
			tag_ref.add(lookupTagAndSetReference(#id_ref));
		      }
		    }

		    else {
		      // The specifier is combined to an initDecl (variable, proto,
		      // typedef). It is an incomplete type declaration, if no
		      // reference is found in the scope list
		      if ((tag_symbol=lookupTag(#id_ref))==null) {
			// No other declaration in the current scope
			// It's an incomplete tag definition
			incomplete_tag=true;
		      }
		      else {
			// Otherwise, an other complete or incomplete declaration
			// exists. the current one is simply a reference to
			// the previous one
			if (
			    ( ( isStruct) && (!(tag_symbol instanceof StructTag)) ) ||
			    ( (!isStruct) && (!(tag_symbol instanceof UnionTag )) )
			    ) {
			  // Not the same kind of tag
			  Error(id_ref,"'"+id_ref.getText()+"'"+" defined as wrong kind of tag");
			}
			tag_ref.add(lookupTagAndSetReference(#id_ref));
		      }
		    }
		  }
		)
	        ( options{warnWhenFollowAmbig=false;}: (attributeSpecifierList)?  )

	{
	  //####################################
	  // Manage INCOMPLETE tag declarations
	  //####################################

	  if (incompletedefinition_or_reference) {
	    if (incomplete_tag) {
	      // Declaration in for-loop seems to be authorized in C99 (spec unclear)
	      // We do not split yet for-loop declarations. Then, possible variable
	      // referencing a type tag declared in for-loop declaration can not be
	      // extracted
	      // => Type tag declaration forbidden in a for-loop declaration. 
	      if (tag_split && is_for_declaration) {
		if (isStruct) {
		  InternalError( #id_ref, "struct declaration ('struct "
				+ #id_ref.getText()
				+ "') in for loop initial declaration not supported"
				);
		}
		else {
		  InternalError(  #id_ref, "union declaration ('union "
				+ #id_ref.getText()
				+ "') in for loop initial declaration not supported"
				);
		}
	      }

	      // Create a new symbol if needed (incomplete)
	      String declName=id_ref.getText();	    
	      
	      // Sets the family
	      if (isStruct) {
		tag_symbol=new StructTag(declName);
	      }
	      else {
		tag_symbol=new UnionTag(declName);
	      }

	      // Set the type
	      tag_symbol.setType(structorunion_type);

	      // Link to AST
	      tag_symbol.setIdNode((NodeAST)#id_ref);

	      // Link AST -> symbol
	      ((NodeAST)#id_ref).setDefinition(tag_symbol);

	      // Put the symbol in the symbol table
	      addTagSymbol(declName,tag_symbol);


	      // Sets a tag reference for higher level declaration
	      tag_ref.add(tag_symbol);


	      //-- Manage optional tag definition split --
	      if (tag_split) {

		// Create the definition tree (split)
		//-----------------------------------
		NodeAST tn;
		if (isStruct) {
		  tn = #( #[NDeclaration],
			  #( #[LITERAL_struct, "struct"] , ## )
			  );
		}
		else {
		  tn = #( #[NDeclaration],
			  #( #[LITERAL_union, "union"] , ## )
			  );		
		}

		// Manage propagated AST
		//----------------------
		// Just leave in the AST a copy of the tag 'ID' node
		## = (NodeAST)astFactory.dupTree(#id_ref);

		// Link AST -> symbol
		##.setReference(tag_symbol);

		// Manage the symbol and its definition
		//-------------------------------------

		// Link Symbol -> AST
		tag_symbol.setDeclarationNode(tn);

		// Add the symbol into 'new_def' (for correct placement of the
		// definition into the AST)
		new_def.add(tag_symbol);
	      }
	    }
	    // Else, it's a reference, nothing to do
	    
	  }


	  //####################################
	  // Manage COMPLETE tag declarations
	  //####################################
	  else {	    
	    // Continue only if no error
	    if (!error) {

	      // Manage a structure declaration without identifier
	      if (noname) {
		// create a 'named' structure
		#id_node = #[ID, tag_symbol.getName()];
		#id_node.addSibling(##);
		## = #id_node;
	      }

	      // This an uncomplete tag which has been completed
	      // Check for incomplete fields has already be done
	      // and a potential error has been raised
	      structorunion_type.setComplete();

	      if (tag_split) {
		//*** The tag definition must be split ***
		//****************************************

		// Create the definition tree (split)
		//-----------------------------------
		NodeAST tn;
		if (isStruct) {
		  tn = #( #[NDeclaration],
			  #( #[LITERAL_struct, "struct"] , ## )
			  );
		}
		else {
		  tn = #( #[NDeclaration],
			  #( #[LITERAL_union, "union"] , ## )
			  );		
		}

		// Manage propagated AST
		//----------------------
		// Just leave in the AST a copy of the tag 'ID' node
		## = (NodeAST)astFactory.dupTree(#id_node);

		// Link AST -> symbol
		##.setReference(tag_symbol);

		// Manage the symbol and its definition
		//-------------------------------------
		// Link Symbol -> AST
		tag_symbol.setDeclarationNode(tn);

		// Add the symbol into 'new_def' (for correct placement of the
		// definition into the AST)
		new_def.add(tag_symbol);

		//--  Manage type tag references  --
		//----------------------------------
		java.util.Vector<Symbol> v=new java.util.Vector<Symbol>();
		getParents(v,#tn);
		for(Symbol symbol:v) {
		  if (symbol!=tag_symbol) {
		    tag_symbol.addParent(symbol);
		  }
		}
	      }
	      else {
		//--  Manage type tag references  --
		//----------------------------------
		java.util.Vector<Symbol> v=new java.util.Vector<Symbol>();
		getParents(v,##);
		for(Symbol symbol:v) {
		  if (symbol!=tag_symbol) {
		    tag_symbol.addParent(symbol);
		  }
		}
	      }

	      // Sets a tag reference for higher level declaration
	      tag_ref.add(tag_symbol);

	      // Manage the symbol and its definition
	      //-------------------------------------
	      // set pointers to AST
	      tag_symbol.setIdNode((NodeAST)#id_node);
	      // Decorate the 'ID' node of the struct or union declaration
	      // with a pointer to the 'symbol'
	      ((NodeAST)#id_node).setDefinition(tag_symbol);
	      
	      // Note: The tag is already in the symbol table (as uncomplete, in order
	      // to manage references to this tag inside its declaration)

	      //	      while ( my_tag_ref.size() != 0 ) {
	      //		Symbol symbol;	       
	      //		symbol=(Symbol)my_tag_ref.removeFirst();
	      //		// Avoid circular dependencies (ex: struct A {int i;struct S* next;}; )
	      //		if (symbol!=tag_symbol) {
	      //		  tag_symbol.addParent(symbol);
	      //		}
	      //	      }
	    }
	  }
	}
        ;


structOrUnionDeclarationList[boolean tag_split,
			     boolean isParamList,
			     LinkedList<Symbol> new_def,
			     LinkedList<Symbol> tag_ref,
			     LinkedList<Symbol> new_def_param,
			     StructOrUnion structorunion_type,
			     boolean isStruct
			     ]
{
  boolean previousFlexibleArray=false;
  boolean flexibleArray=false;
}
	:  ( 
	    flexibleArray=s:structOrUnionDeclaration[tag_split,
						     isParamList,
						     new_def,tag_ref,new_def_param,
						     structorunion_type,
						     isStruct]
	      {
		if (previousFlexibleArray) {
		  // Flexible array must be the last
		  Error(s,"flexible array member not at end of struct");
		}
		previousFlexibleArray=flexibleArray;
	      }
	    )+
        ;

structOrUnionDeclaration[boolean tag_split,
		  boolean isParamList,
		  LinkedList<Symbol> new_def,
		  LinkedList<Symbol> tag_ref,
		  LinkedList<Symbol> new_def_param,
		  StructOrUnion structorunion_type,
		  boolean isStruct
		  ]
returns [boolean flexibleArray]
	{
	  TypeSpecifierQualifier specifier_qualifier=new TypeSpecifierQualifier();
	  flexibleArray=false;
	}
	:
	   ds:specifierQualifierList[tag_split,
				     isParamList, // propagate parameterList info
				     specifier_qualifier,
				     new_def,tag_ref,new_def_param]
	   (
	     flexibleArray=structDeclaratorList[tag_split,
					      specifier_qualifier.getType(ds,compilerError),
					      new_def_param,tag_ref,
					      structorunion_type,
					      isStruct,
					      ds]
	   )?
        ;

specifierQualifierList[boolean tag_split,
		       boolean isParamList,
		       TypeSpecifierQualifier specifier_qualifier,
		       LinkedList<Symbol> new_def,
		       LinkedList<Symbol> tag_ref,
		       LinkedList<Symbol> new_def_param]
        :  (
	    typeSpecifier[false,
			  tag_split,
			  isParamList,
			  specifier_qualifier,
			  new_def,tag_ref,new_def_param]
	    | typeQualifier[specifier_qualifier]
	    | attributeSpecifier
	   )+
        ;

structDeclaratorList[boolean tag_split,
		     Type specifier_qualifier_type,
		     LinkedList<Symbol> new_def_param,
		     LinkedList<Symbol> tag_ref,
		     StructOrUnion structorunion_type,
		     boolean isStruct,
		     NodeAST specifierNode]
returns [boolean flexibleArray]
{
  boolean previousFlexibleArray=false;
  flexibleArray=false;
}
        :  ( flexibleArray=s:structDeclarator[tag_split,
					      specifier_qualifier_type,
					      new_def_param,tag_ref,
					      structorunion_type,
					      isStruct,
					      specifierNode]
	     {
	       if (previousFlexibleArray) {
		 // Flexible array must be the last
		 Error(s,"flexible array member not at end of struct");
	       }
	       previousFlexibleArray=flexibleArray;
	     }
	   )+
        ;

structDeclarator[boolean tag_split,
		 Type specifier_qualifier_type,
		 LinkedList<Symbol> new_def_param,
		 LinkedList<Symbol> tag_ref,
		 StructOrUnion structorunion_type,
		 boolean isStruct,
		 NodeAST specifierNode]
returns [boolean flexibleArray]

	{
	  AST id_node=null;

	  // Create a new symbol
	  Symbol decl_symbol=new Symbol();
	  Type the_fieldType=null;

	  // Bitfield
	  EnrichedType t_expr;
	  boolean isBitField=false;

	  // Flexible array
	  flexibleArray=false;
	}
        :
        #( NStructDeclarator     
	    // No initializer
	   ( id_node = declarator[tag_split,
				  decl_symbol,false,
				  new_def_param,tag_ref,
				  specifier_qualifier_type] 
	   )?
 	     {
	       // Sets the symbol type if no declarator
	       if (id_node==null) {
		 // No declarator, it is necessarily a bitfield
		 the_fieldType=specifier_qualifier_type;
	       }
	       else {
		 // Get the member type
		 the_fieldType=decl_symbol.getType();
		 
		 // Sets the name
		 String declName=id_node.getText();

		 // Manage flexible arrays and check for void
		 Type fieldType=the_fieldType.unqualify();
		 if (fieldType.isArray() && (!((Array)fieldType).hasSizeDefined())) {
		   // It's a flexible array, only authorized in structure
		   if (!isStruct) {
		     FatalError((NodeAST)id_node,"flexible array member in union");
		   }
		   else {
		     // It's potentially a flexible array (if located at the end of
		     // field list)
		     flexibleArray=true;
		     
		     // Check anyway that the uncomplete array has complete element
		     if (!((Array)fieldType).hasCompleteElement()) {
		       Error((NodeAST)id_node,"field '"+declName+"' has incomplete type");
		     }
		   }
		 }
		 else if (!fieldType.isComplete()) {
		   Error((NodeAST)id_node,"field '"+declName+"' has incomplete type");
		 }

		 else if (fieldType.isVoid()) {
		   Error((NodeAST)id_node,"field '"+declName+"' declared void");
		 }
	       }
	     }

	     // Bitfield
	     ( COLON t_expr=s_node:constExpr 
	      {
		Type s_type=the_fieldType;
		Type the_type=t_expr.getType();

		isBitField=true;

		// The bitfield must have integral type
		if (s_type.isIntegralScalar()) {
		  // The bitfield size must be a constant integral
		  if (!t_expr.isConstantIntegral()) {
		    if (id_node==null) {
		      FatalError(s_node,"anonymous bit-field width not an integer constant");
		    }
		    else {
		      FatalError((NodeAST)id_node,"bit-field '" + id_node.getText() +
				 "' width not an integer constant");
		    }
		  }
		  if (!s_type.isIntScalar()) {
		    // In ANSI C, only int or unsigned int can be specifiers of a bitfield,
		    // GCC is more permissive
		    if (id_node==null) {
		      Warning(1,specifierNode,"anonymous bit-field type invalid in ISO C");
		    }
		    else {
		      Warning(1,(NodeAST)id_node,"bit-field `" + id_node.getText() +
			      "' type invalid in ISO C");
		    }
		  }
		}
		else {
		  if (id_node==null) {
		    FatalError(s_node,"anonymous bit-field has invalid type");
		  }
		  else {
		    FatalError((NodeAST)id_node,"bit-field '"+id_node.getText()+
			       		      "' has invalid type");
		  }
		}

		// Check the bitfield size
		long l=t_expr.getConstantIntegralValue().longValue();
		if (l<0) {
		  if (id_node==null) {
		    FatalError(s_node,"negative width for anonymous bit-field");
		  }
		  else {
		    FatalError((NodeAST)id_node,"negative width for bit-field '"+
			                      id_node.getText()+"'");
		  }
		}
		else if (l==0) {
		  if (id_node==null) {
		    // An unnamed bitfield with a width of 0 indicates that no further bit-field
		    // is to be packed into the unit in which the previous bit-field, if any, was
		    // placed (Cf. C89)
		    // -> Nothing to do
		  }
		  else {
		    FatalError((NodeAST)id_node,"zero width for bit-field '"+id_node.getText()+"'");
		  }
		}
		else if (l>(s_type.sizeof()<<3)) {
		  if (id_node==null) {
		    Warning(s_node,"width of anonymous bit-field exceeds its type");
		  }
		  else {
		    Warning((NodeAST)id_node,"width of bit-field '" +
			    id_node.getText()+"' exceeds its type");
		  }
		  // Saturates size
		  l=s_type.sizeof()<<3;
		}
		
		// Create the bitfield and replaces the type
		the_fieldType=new Bitfield(s_type,(int)l);
	      }
	    )?
        )
	{ 
	  Type the_type=the_fieldType;
  
	  // Put the field into the structure or union
	  if (id_node==null) {
	    // Unnamed field only authorized for bitfields
	    if (!isBitField) {
	      // ISO C doesn't support unnamed fileds, but gcc supports unnamed struct or union
	      if (the_type.isStructOrUnion()) {
		Warning(1,specifierNode,"ISO C doesn't support unnamed structs/unions");
		// The type must be complete (not checked before)
		if (!the_type.isComplete()) {
		  Error(specifierNode, "anonymous field has incomplete type");
		}
	      }
	      else {
		FatalError(specifierNode,
			   "unnamed fields of type other than struct or union are not allowed");
	      }
	    }
	    // Add an unnamed bitfield to the structure or union
	    structorunion_type.addUnnamedField(the_type);
	  }
	  else {
	    // Adds a new field to the struct or union
	    if (structorunion_type.addField(id_node.getText(),the_type)==false) {
	      // Field already declared
	      Error((NodeAST)id_node,"duplicate member '" + id_node.getText() + "'" );	    
	    }
	  }
	}
        ;


//##################################################################
//                      Enumerate management
//##################################################################


enumSpecifier[boolean alone,
	      boolean tag_split,
	      boolean isParamList,
	      LinkedList<Symbol> new_def,
	      LinkedList<Symbol> tag_ref]
returns [EnumTag enum_symbol]
	{
	  // Incomplete
	  boolean incompletedefinition_or_reference = false;
	  boolean incomplete_tag = false;
	  // Complete
	  boolean noname  = false;
	  boolean error   = false;
	  LinkedList<Symbol> my_tag_ref  = new LinkedList<Symbol>();
	  LinkedList<EnumConstant> my_children = new LinkedList<EnumConstant>();
	  enum_symbol = null;

	  // Create a type, by default uncomplete
	  Enumerate enum_type=new Enumerate();
	}
	:    #(  "enum"
	        (attributeSpecifierList)?
                ( (ID LCURLY) => id_node:ID
		    LCURLY
		    {
		      // It's a complete declaration
		      //----------------------------

		      // Lookup existing tag with such name in the symbol table
		      TagSymbol symb=lookupTagInCurrentScope(#id_node);

		      if (symb!=null) {
			// A declaration already exists in the current scope
			if (symb.getType().isComplete()) {
			  // A complete declaration already exists in the current scope
			  // There is a conflict
			  Error(  #id_node, " symbol '" 
				+ #id_node.getText()
				+ "' redefined, previous declaration line "
				+ symb.getIdNode().getLineNum()
				);
			  error=true;
			}
			else {
			  // An incomplete declaration already exists in the current scope
			  // Check that it is the same type
			  if (!(symb instanceof EnumTag)) {
			    // It's an error
			    Error(  #id_node, " complete definition type "
				  + #id_node.getText()
				  + " non coherent with previous incomplete definition "
				  );
			    error=true;
			  }
			  else {
			    // The incomplete declaration is compatible
			    // We will simply complete the existing incomplete type
			    enum_type=(Enumerate)symb.getType();
			  }
			}
		      }

		      // Declaration in for-loop seems to be authorized in C99 (spec unclear)
		      // We do not split yet for-loop declarations. Then, possible variable
		      // referencing a type tag declared in for-loop declaration can not be
		      // extracted
		      // => Type tag declaration forbidden in a for-loop declaration. 
		      if (tag_split && is_for_declaration) {
			InternalError(  #id_node, "enum declaration (enum "
				      + #id_node.getText()
				      + ") in for loop initial declaration not supported"
				      );
			// error=true; no need to raise a second error wirh incomplete parameter incomplete
		      }

		      // Should not declare types in function parameters
		      if (isParamList) {
			Warning(  #id_node, "'enum "
				+ #id_node.getText() + "' declared inside parameter list");
			Warning(  #id_node,
				"its scope is only this definition or declaration, which is probably not what you want");
		      }


		      // Create a new symbol (by default incomplete)
		      //--------------------------------------------
		      String declName = #id_node.getText();
		      enum_symbol=new EnumTag(declName);

		      // Sets the type
		      enum_type.setSignatureName(#id_node.getText());
		      enum_symbol.setType(enum_type);
		      
		      // Link to AST
		      enum_symbol.setIdNode((NodeAST)#id_node);

		      // Add the symbol to the symbol table
		      addTagSymbol(declName,enum_symbol);


		      // In enumerate, we do not enter a scope
		      // (tag and enum fields at the same level)
		    }
		    enumList[enum_symbol, my_tag_ref, my_children,enum_type]
		    RCURLY

		|   curly:LCURLY
	            {
		      // Anonymous tag declaration
		      //--------------------------

		      // Declaration in for-loop seems to be authorized in C99 (spec unclear)
		      // We do not split yet for-loop declarations. Then, possible variable
		      // referencing a type tag declared in for-loop declaration can not be
		      // extracted
		      // => Type tag declaration forbidden in a for-loop declaration. 
		      if (tag_split && is_for_declaration) {
			InternalError( #curly, "enumerate declaration (anonymous) in for loop initial declaration not supported" );
			// error=true; no need to raise a second error wirh incomplete parameter incomplete
		      }

		      // Should not declare types in function parameters
		      if (isParamList) {
			Warning(#curly, "anonymous enum declared inside parameter list");
			Warning(#curly, "its scope is only this definition or declaration, which is probably not what you want");
		      }

		      // Create a new symbol with a new name
		      noname          = true;
		      String declName = getNewName();
		      enum_symbol=new EnumTag(declName);

		      // Sets the type
		      enum_type.setSignatureName(declName);
		      enum_symbol.setType(enum_type);

		      // No ID node, so no link to AST

		      // Add the symbol to the symbol table (if it raise an error, it's
		      // an internal error)
		      addTagSymbol(declName,enum_symbol);

		      // In enumerate, we do not enter a scope
		      // (tag and enum fields at the same level)
		    }
		    enumList[enum_symbol, my_tag_ref, my_children, enum_type]
		    RCURLY

                | id_ref:ID
		  {
		    // It's an incomplete declaration
		    //-------------------------------
		    incompletedefinition_or_reference=true;
		    if (alone) {
		      // The type specifier is NOT combined to an 'initDecl'
		      // (variable, proto, typedef). It is an incomplete type
		      // declaration if no complete declaration in the current scope
		      TagSymbol tag_symbol=lookupTagInCurrentScope(#id_ref);
		      if (tag_symbol==null) {
			// No other declaration in the current scope
			// It's an incomplete tag definition
			// (which potentially overides a complete declaration in a parent scope)
			enum_symbol=null;
			incomplete_tag=true;
		      }
		      else {
			// Otherwise, an other complete or incomplete declaration
			// exists in the current scope. the current one is simply
			// a reference to the previous one
			if (!(tag_symbol instanceof EnumTag)) {
			  // Not the same kind of tag
			  FatalError(id_ref,"'"+id_ref.getText()+"'"+" defined as wrong kind of tag");
			  error=true;
			}
			enum_symbol=(EnumTag)tag_symbol;
			tag_ref.add(lookupTagAndSetReference(#id_ref));
		      }
		    }
		    else {
		      // The specifier is combined to an initDecl (variable, proto,
		      // typedef). It is an incomplete type declaration, if no
		      // reference is found in the scope list
		      TagSymbol tag_symbol=lookupTag(#id_ref);
		      if (tag_symbol==null) {
			// No other declaration in the current scope
			// It's an incomplete tag definition
			enum_symbol=null;
			incomplete_tag=true;
		      }
		      else {
			// Otherwise, an other complete or incomplete declaration
			// exists. the current one is simply a reference to
			// the previous one
			if (!(tag_symbol instanceof EnumTag)) {
			  // Not the same kind of tag
			  FatalError(id_ref,"'"+id_ref.getText()+"'"+" defined as wrong kind of tag");
			  error=true;
			}
			enum_symbol=(EnumTag)tag_symbol;			
			tag_ref.add(lookupTagAndSetReference(#id_ref));
		      }
		    }
		  }
		 
		)
      	        ( options{warnWhenFollowAmbig=false;}: (attributeSpecifierList)?  )
	      )
	{
	  //####################################
	  // Manage INCOMPLETE tag declarations
	  //####################################

	  if (incompletedefinition_or_reference) {
	    if (incomplete_tag) {
	      // This is the first incomplete tag definition of the scope

	      // Declaration in for-loop seems to be authorized in C99 (spec unclear)
	      // We do not split yet for-loop declarations. Then, possible variable
	      // referencing a type tag declared in for-loop declaration can not be
	      // extracted
	      // => Type tag declaration forbidden in a for-loop declaration. 
	      if (tag_split && is_for_declaration) {
		InternalError(  #id_ref, "'enum declaration (enum "
			      + #id_ref.getText()
			      + ") in for loop initial declaration not supported"
			      );
	      }

	      // Get the tag name
	      String declName=#id_ref.getText();	    
	      
	      // Create a new symbol (incomplete)
	      enum_symbol=new EnumTag(declName);
	      
	      // Sets the type
	      enum_symbol.setType(enum_type);

	      // Link to AST
	      enum_symbol.setIdNode((NodeAST)#id_ref);

	      //-- Put the symbol in the symbol table --
	      addTagSymbol(declName,enum_symbol);

	      // Link AST -> symbol
	      ((NodeAST)#id_ref).setDefinition(enum_symbol);

	      // Sets a tag reference for higher level declaration
	      tag_ref.add(enum_symbol);

	      if (tag_split) {
		//*** The tag definition must be split ***
		//****************************************

		// Create the definition tree (split)
		//----------------------------------
		NodeAST tn =  #( #[NDeclaration], ## );
	      
		// Manage propagated information
		//-------------------------------
		// Just propagate an [LITERAL_enum 'ID'] node to the AST building 
		// process
		NodeAST id_ref_to_propagate = (NodeAST)astFactory.dupTree(#id_ref);
		## = #( #[LITERAL_enum, "enum"] , id_ref_to_propagate); 
 
		// Link AST -> symbol
		id_ref_to_propagate.setReference(enum_symbol);

		// Link to the declaration node (relevant only for split mode)
		enum_symbol.setDeclarationNode(tn);

		// Add the symbol into 'new_def' (for correct placement of the
		// definition into the AST)
		new_def.add(enum_symbol);
	      }
	      
	    }
	  }


	  //####################################
	  // Manage COMPLETE tag declarations
	  //####################################
	  else {
	    // Continue only if no error
	    if (!error) {

	      // Manage a enum declaration without identifier
	      if (noname) {
		// create a 'named' enum
                #id_node=#[ID, enum_symbol.getName()];
		##.insertChild(#id_node);
	      }

	      // Complete declaration
	      enum_type.setComplete();

	      // set pointers to AST
	      enum_symbol.setIdNode((NodeAST)#id_node);

	      // Link AST -> symbol
	      ((NodeAST)#id_node).setDefinition(enum_symbol);

	      // Sets a tag reference for higher level declaration
	      tag_ref.add(enum_symbol);
    
	      //--  Manage type tag references  --
	      //----------------------------------
	      while ( my_tag_ref.size() != 0 ) {
		Symbol ref_symbol = my_tag_ref.removeFirst();
		enum_symbol.addParent(ref_symbol);
	      }
	      //--  Manage children  --
	      //------------------------
	      while ( my_children.size() != 0 ) {
		EnumConstant enum_field_symbol = my_children.removeFirst();
		enum_symbol.addChild(enum_field_symbol);
	      }


	      if (tag_split) {
		//*** The tag definition must be split ***
		//****************************************

		// Create a AST for the new enum definition
		// Create the definition tree (split)
		//-----------------------------------
	        NodeAST tn =  #( #[NDeclaration], ## );
	      
		// Manage propagated AST
		//----------------------
		// Just propagate an 'enum ID' node ('union ID' will then be
		// simply built)
		NodeAST id_node_to_propagate = (NodeAST)astFactory.dupTree(#id_node);
		## = #( #[LITERAL_enum, "enum"] , id_node_to_propagate);  

		// Link AST -> symbol
		id_node_to_propagate.setReference(enum_symbol);


		// Manage the symbol and its definition
		//-------------------------------------
		// set pointers to AST (only relevent for split mode)
		enum_symbol.setDeclarationNode(tn);

		// Add the symbol into 'new_def' (for correct placement of the
		// definition into the AST)
		new_def.add(enum_symbol);
	      }
	    }
	  }
	}
        ;


enumList[Symbol parent_symbol,
	 LinkedList<Symbol> tag_ref,
	 LinkedList<EnumConstant> children,
	 Enumerate enum_type]
	{
	  long counter=0L;
	}
	: ( 
	   counter=enumerator[parent_symbol,
			      tag_ref,children,
			      enum_type,
			      counter]
	  )+
        ;


enumerator[Symbol parent_symbol,
	   LinkedList<Symbol> tag_ref,
	   LinkedList<EnumConstant> children,
	   Enumerate enum_type,
	   long counter]
returns [long new_counter=counter]
	   {
	     EnrichedType t_expr;
	   }
	:  id_node:ID 
	   ( ASSIGN t_expr=e:constExpr
	     {
	       // Integer constant expression check [TBW]
	       // Sets the counter with the expression value
	       counter=t_expr.getConstantIntegralValue().longValue();
	     }
	   )?
 	{

	  // Manage potential symbol references in 'expr'
	  //---------------------------------------------
	  // (should only be other enum fields)
	  if (#e!=null) {
	    java.util.Vector<Symbol> v=new java.util.Vector<Symbol>();
	    getParents(v,#e);
	    for(Symbol s:v) {
	      // Avoid circular dependencies in case of 
	      // 'enum E {A, B=A}' for instance
	      if (s!=parent_symbol) {
		tag_ref.add(s);
	      }
	    }
	  }

	  //-- Create a new symbol --
	  //-------------------------
	  String declName=#id_node.getText();

	  // Create a new 'symbol' for the new enum field
	  EnumConstant enum_field_symbol
	    = new EnumConstant(declName);

	  // Add the parent symbol
	  enum_field_symbol.addParent(parent_symbol);

	  // An enumerate constant is has type int
	  enum_field_symbol.setType(IntegerScalar.Tsint);

	  // Manage field value
	  enum_field_symbol.setValue(counter);
	  new_counter=counter+1;

	  // Link to AST
	  enum_field_symbol.setIdNode((NodeAST)#id_node);

	  // No 'DeclarationNode' set (null)
	  // Add the new symbol and the associated 'symbol' in the symbol table
	  // Nothing to check for enumerate fields, but must be considered as a
	  // tag for the scope management
	  addEnumConstantSymbol(declName,enum_field_symbol);

	  // Adds an element to the parent ENUM
	  // No test for redefinition since it is done at symbol table setting time
	  enum_type.addElement(declName);

	  // Add the symbol to the children list
	  children.add(enum_field_symbol);

	  // Link AST -> symbol
	  ((NodeAST)#id_node).setDefinition(enum_field_symbol);

	}
        ;



//##################################################################
// Declarator
//##################################################################


// Version with type processing
pointerGroup[Type input_type]
returns [Type type_node]
        {
	  // Type management
	  type_node=null;
	  Pointer p=null;
	  Qualifier qualifier=null;
	  boolean b=false;
	}
        : 
	  #( NPointerGroup ( STAR
			     {
			       if (type_node!=null) {
				 p=new Pointer(type_node);
			       }
			       else {
				 p=new Pointer(input_type);
			       }
			       type_node=p;
			       
			       qualifier=new Qualifier();
			       b=false;
			     }
			     
			     ( typeQualifierInDeclarator[qualifier]
			       {
				 b=true;
			       }
			     )*
			     {
			       if (b==true) {
				 qualifier.setQualifiedType(p);
				 type_node=qualifier;
			       }
			     }
			   )+
	     )
        ;

//------------------------------------------------------------------
// declarator:
//
// Declaration of a 'variable', 'typedef', 'function prototype',
// or 'function definition' symbols.
// This rule returns the (main) 'ID' node of the declarator
//
// Note:
// 'function_def' to 'true' means we are in the declarator of a
// function definition
//------------------------------------------------------------------
declarator[boolean tag_split,
	   Symbol symbol,
	   boolean function_def,
	   LinkedList<Symbol> new_def_param,
	   LinkedList<Symbol> tag_ref,
	   Type input_type]
returns [AST id_node]
        {
	  boolean is_id          = false;
	  boolean e              = false;
	  boolean toto		 = false;

	  // Array management
	  boolean first_dimension = false;
	  boolean last_seen_is_array = false;
	  EnrichedType t_expr=null;

	  // Type management
	  ChildType last_encountered_child_type  = null;
	  Function func_type = null;
	  Array array_type            = null;
	  boolean useless_parenthesis = false;
	  
	  // The marker is used for sub-declarators. If the sub-declarator
	  // is not empty, the marker will be the child of a ChildType
	  // (Pointer, return value of FunctionPrototype, sub type of Array)
	  ChildType.Marker subDeclaratorMarker=new ChildType.Marker();

	  id_node = null;
	}
        :   #( NDeclarator
               ( input_type=pointerGroup[input_type] )?               
	       (attributeSpecifierList)? 


	       ( id:ID
		   {
		     is_id   = true;
		     id_node = #id;
		     // Sets the type of the symbol (in case there is no subsequent array or
		     // function parameter declaration)
		     symbol.setType(input_type);
		   }

		 | 
		   LPAREN 
	             {
		       useless_parenthesis = true ;
		       subDeclaratorMarker=new ChildType.Marker();
		     }
		   id_node=declarator[tag_split,symbol,function_def,
				      new_def_param,tag_ref,
				      subDeclaratorMarker]
		   {
		     // Check if we are really in a function definition or not
		     if ((function_def)&&(subDeclaratorMarker.hasParent())) {
		       // If a marker has a parent, it means that the current declarator has
		       // a non empty sub-declarator (pointer, parameter list or array). It
		       // means that we can not be in the function declaration parameter list
		       // -> set function_def as false
		       function_def=false;
		     }
		   }

		   RPAREN
                )

                (   #( NParameterTypeList
		        {
			  useless_parenthesis = false ;
			  // Not an array
			  last_seen_is_array=false;

			  // Manage function type building
			  //------------------------------
			  func_type=new Function();
			  func_type.setReturnType(input_type);
			  if (last_encountered_child_type==null) {
			    // This is the first type to be declared in the current declarator

			    // If a marker has a parent (meaning that a the current declarator has
			    // a sub-declaratior), the current function type must be a child of
			    // the marker's parent (type of the sub-declarator).
			    // Otherwise, we are in the declaration root
			    if (subDeclaratorMarker.hasParent()) {
			      // Set the current function type as child (function return type,
			      // sub-array type or pointed type) of the marker's parent type
			      // (type of the sub-declarator which can only be function, array
			      // or pointer)
			      ChildType parent=((ChildType)(subDeclaratorMarker.getParent()));
			      parent.setChild(func_type);
			      // Check that function can be the child of the parent type
			      typeManager.checkChildAsFunction((NodeAST)id_node,compilerError,
							       parent);
			      
			      // Manage symbol scopes
			      //---------------------
			      // We enter a function prototype parameter list
			      // Note: parameters of prototypes are not put in the symbol table
			      //       we push anyway the function proto parameter scope for
			      //       performing redefinition checking on potential type tags
			      //       declared in the parameter list
			      symbolTable.pushScope("!" + getParamlistNumber() + "!"
						    + id_node.getText() );
			    }
			    else {
			      // We are at the *declaration root* of this declarator instance.
			      // Overwrite the symbol type which has been set to 'input_type'
			      symbol.setType(func_type);
			      
			      // Manage symbol scopes
			      //---------------------
			      if (function_def) {
				// We enter a function definition parameter list
				// For supporting function mangling, the function scope
				// must itself be mangled
				symbolTable.pushScope("#" + getFunctionDefNumber() + "#"
						      + id_node.getText() );
			      }
			      else {
				// We enter a function prototype parameter list
				symbolTable.pushScope("!" + getParamlistNumber() + "!"
						      + id_node.getText() );
			      }
			    }
			  }
			  else {
			    // A type has already been declared in the current declarator
			    // (may be a function or an array)

			    // We can not be anymore in the root of a function definition
			    function_def=false;

			    // Substitute its child type (return type for a function, sub-type)
			    // for an array by the current function type
			    last_encountered_child_type.setChild(func_type);
			    // Check that function can be the child of the parent type
			    typeManager.checkChildAsFunction((NodeAST)id_node,compilerError,
							     last_encountered_child_type);

			    // Manage symbol scopes
			    //---------------------
			    // We are here necessarily in a prototype parameter list
			    symbolTable.pushScope("!" + getParamlistNumber() + "!"
						  + id_node.getText() );

			  }
			  last_encountered_child_type=func_type;
			}
                      (
		        parameterTypeList[tag_split,
					  new_def_param,tag_ref,
					  func_type,
					  function_def]
		        | (idList)?
		       )
                       RPAREN
		    	{
			  symbolTable.popScope();
			}
		       )
                    
		    | l:LBRACKET
		      {		
			last_seen_is_array=false;

			// Manage array type building
			//---------------------------
			array_type=new Array(input_type);
			if (last_encountered_child_type==null) {
			  // This is the first type to be declared in the current declarator

			  // If a marker has a parent (meaning that a the current declarator has
			  // a sub-declaratior), the current array type must be a child of
			  // the marker's parent (type of the sub-declarator).
			  // Otherwise, we are at the declaration root
			  if (subDeclaratorMarker.hasParent()) {
			    // Set the current array type as child (function return type,
			    // sub-array type or pointed type) of the marker's parent type
			    // (type of the sub-declarator which can only be function, array
			    // or pointer)
			    if (subDeclaratorMarker.getParent().isArray()) {
			      last_seen_is_array=true;
			    }
			    ChildType parent=((ChildType)(subDeclaratorMarker.getParent()));
			    parent.setChild(array_type);
			    // Check that array can be the child of the parent type
			    typeManager.checkChildAsArray((NodeAST)id_node,compilerError,
							  parent);
			  }
			  else {
			    // We are at the declaration root, check for array of voids
			    if (input_type.isVoid()) {
			      Error(l,"declaration of '"+id_node.getText()+"' as array of voids");
			    }
			    // Overwrite the symbol type which has been set to 'input_type'
			    symbol.setType(array_type);
			  }
			}
			else {
			  // A type has already been declared in the current declarator
			  // (may be a function or an array)
			  // Substitute its child type (return type for a function, sub-type)
			  // for an array by the current array type
			  if (last_encountered_child_type.isArray()) {
			    last_seen_is_array=true;
			  }
			  last_encountered_child_type.setChild(array_type);
			  // Check that array can be the child of the parent type
			  typeManager.checkChildAsArray((NodeAST)id_node,compilerError,
							last_encountered_child_type);
			}
			last_encountered_child_type=array_type;

			// Manage the array dimension counter
			if (!last_seen_is_array) {
			  // It is the first dimension of the array
			  first_dimension=true;
			}
			else {
			  first_dimension=false;
			}
		      }

		      bracket_expr[symbol, array_type, first_dimension] 

		      RBRACKET
		      {
			useless_parenthesis = false ;
			// Check that 
			if (last_seen_is_array && (!array_type.isComplete())) {
			  Error(l,"array type has incomplete element type");
			}
		      }

                )*
             )
       	     {	
	       if (useless_parenthesis){
		 ChildType parent2;
		 if (subDeclaratorMarker.hasParent()){
		   parent2 = ((ChildType)(subDeclaratorMarker.getParent()));
		   parent2.setChild(input_type);                    
		 }
		 else {symbol.setType(input_type);
		 }
	       }
	     }
        ;

// ANSI-C authorizes an incomplete array which is completed by the
// initializer
bracket_expr[Symbol symbol, Array array_type, boolean first_dimension]
	{
	  boolean e=false;
	  EnrichedType t_expr=null;
	}
	: 
	(
	   t_expr=tn:expr
	   {
	     e=true;

	     // The array size must have integral type
	     if (!t_expr.getType().isIntegralScalar()) {
	       Error(tn,"size of array has non-integer type");
	     }

	     // The array size must be a constant integral
	     if (t_expr.isConstantIntegral()) {
	       // Check that the number of element is positive
	       int dim=t_expr.getConstantIntegralValue().intValue();
	       if (dim<0) {
	         Error(tn,"size of array is negative");
	         dim=0; // to continue the compilation process
	       }
	       else if (dim==0) {
	         Warning(1,tn,"ISO C forbids zero-size arrays");
	       }
	       array_type.setNbElements(dim);
	     }
	     else {
	       // C89: forbids variable length arrays
	       // Warning(1,tn,"ISO C89 forbids variable length array '"+id_node.getText()+"'");
	       // C99: variable length array allowed in a function, not at top level
	       array_type.setVariableSize();
	       // OpenCL C: forbids variable length arrays
	       if (oclLanguage) {
	         if (isCLExtPrivateVariableLengthArray()) {
	           // The stack can not be computed anymore
	           unsized_stack_requested=true;
	           Warning(tn,"[OCL] variable length arrays disables automatic stack size computation");
	         }
	         else {
	           Error(tn,"[OCL] variable length arrays are forbidden");
	         }
	       }
	       if (vxLanguage) {
	         Error(tn,"[VX] variable length arrays are forbidden");
	       }
	     }

	     // Put symbols referenced in the expression as parent of
	     // the current symbol
	     if (symbol!=null) {
	       java.util.Vector<Symbol> v=new java.util.Vector<Symbol>();
	       getParents(v,#tn);
	       for(Symbol s:v) {	
	         symbol.addParent(s);
	       }
	     }
	   }
	 )?
	{
	  if (isExtractionMode()&&(!e)&&
	      (symbol!=null)&&(first_dimension)) {
	    // The array is uncomplete. Its size must be given by the initializer
	    // For extraction, we need to split the declaration from the initialization
	    // So that the number of elements must be set
	    // -> Add a Number node, which will be filled initialization time
	    #bracket_expr = #[IntegralNumber,""]; 
	    EnrichedType etype=new EnrichedType(IntegerScalar.Tsint); // Not an object reference
	                                                              // type is 'int'
            #bracket_expr.setDataType(etype);
	    symbol.setUncompleteArraySizeNode(#bracket_expr);
	  }
	}
	;



//------------------------------------------------------------------
// nonemptyAbstractDeclarator:
//
// Abstract declarator: declarator without ID.
// Allowed in sizeof(), casts, parameter of function pointers or
// function protoypesDeclaration of a 'variable', 'typedef',
// 'function prototype',
//------------------------------------------------------------------
nonemptyAbstractDeclarator[boolean tag_split,
			   Symbol symbol,
			   LinkedList<Symbol> new_def_param,
			   LinkedList<Symbol> tag_ref,
			   Type input_type]
	{
	  // Array management
	  EnrichedType t_expr=null;
	  boolean is_array_of_array   = false;

	  // Type management
	  ChildType.Marker subDeclaratorMarker=new ChildType.Marker();
	  ChildType last_encountered_child_type=null;
	  Function func_type=null;
	  Array array_type= null;

	  symbol.setType(input_type);
	}
        :   #( NNonemptyAbstractDeclarator
              ( 
	        input_type=pointerGroup[input_type]
	          {
		    symbol.setType(input_type);
		  }

		 ( 
		    (
		      LPAREN
		       {
			 subDeclaratorMarker=new ChildType.Marker();
		       }
		      ( nonemptyAbstractDeclarator[tag_split,
						   symbol,
						   new_def_param,tag_ref,
						   subDeclaratorMarker] 
			)?
		     RPAREN
		    )

		  | #( n1:NParameterTypeList
		         {
			   // Manage function type building
			   //------------------------------
			   func_type=new Function();
			   func_type.setReturnType(input_type);

			   if (last_encountered_child_type==null) {
			     // This is the first type to be declared in the current declarator

			     // If a marker has a parent (meaning that a the current declarator has
			     // a sub-declaratior), the current function type must be a child of
			     // the marker's parent (type of the sub-declarator).
			     // Otherwise, we are in the declaration root
			     if (subDeclaratorMarker.hasParent()) {
			       // Set the current function type as child (function return type,
			       // sub-array type or pointed type) of the marker's parent type
			       // (type of the sub-declarator which can only be function, array
			       // or pointer)
			       ChildType parent=(ChildType)(subDeclaratorMarker.getParent());
			       parent.setChild(func_type);
			       // Check that function can be the child of the parent type
			       typeManager.checkChildAsFunctionAbstract(n1,compilerError,
									parent);
			     }
			     else {
			       // We are at the declaration root, overwrite the symbol type
			       // which has been set to 'input_type' before or after the
			       // pointer group
			       symbol.setType(func_type);
			     }
			   }
			   else {
			     // A type has already been declared in the current declarator
			     // (may be a function or an array)
			     // Substitute its child type (return type for a function, sub-type)
			     // for an array by the current function type
			     last_encountered_child_type.setChild(func_type);
			     // Check that function can be the child of the parent type
			     typeManager.checkChildAsFunctionAbstract(n1,compilerError,
								      last_encountered_child_type);

			   }
			   last_encountered_child_type=func_type;
			   
			   // Manage symbol scopes
			   //---------------------
			   // We enter a prototype parameter list
			   symbolTable.pushScope("!" + getNewName());
			 }
		      
			( parameterTypeList[tag_split,
					    new_def_param,tag_ref,
					    func_type,
					    false] // not a function definition 
			 )?
			RPAREN
		         {
			   symbolTable.popScope();
			 }
		      )

		   | (
		        l1:LBRACKET 
		         {
			   is_array_of_array=false;

			   // Manage array type building
			   //---------------------------
			   array_type=new Array(input_type);
			   if (last_encountered_child_type==null) {
			     // This is the first type to be declared in the current declarator

			     // If a marker has a parent (meaning that a the current declarator has
			     // a sub-declaratior), the current array type must be a child of
			     // the marker's parent (type of the sub-declarator).
			     // Otherwise, we are at the declaration root
			     if (subDeclaratorMarker.hasParent()) {
			       // Set the current array type as child (function return type,
			       // sub-array type or pointed type) of the marker's parent type
			       // (type of the sub-declarator which can only be function, array
			       // or pointer)
			       if (subDeclaratorMarker.getParent().isArray()) {
				 is_array_of_array=true;
			       }
			       ChildType parent=((ChildType)(subDeclaratorMarker.getParent()));
			       parent.setChild(array_type);
			       // Check that array can be the child of the parent type
			       typeManager.checkChildAsArrayAbstract(l1,compilerError,
								     parent);

			       ((ChildType)(subDeclaratorMarker.getParent())).setChild(array_type);
			     }
			     else {
			       // we are at the declaration root, overwrite the symbol type
			       // which has been set to 'input_type'
			       symbol.setType(array_type);
			     }
			   }
			   else {
			     // A type has already been declared in the current declarator
			     // (may be a function or an array)
			     // Substitute its child type (return type for a function, sub-type)
			     // for an array by the current array type
			     if (last_encountered_child_type.isArray()) {
			       is_array_of_array=true;
			     }
			     last_encountered_child_type.setChild(array_type);
			     // Check that array can be the child of the parent type
			     typeManager.checkChildAsArray(l1,compilerError,
							   last_encountered_child_type);
			   }
			   last_encountered_child_type=array_type;
			 }

		       ( t_expr=tn:expr
		         {
		         // The array size must have integral type
		         if (!t_expr.getType().isIntegralScalar()) {
		           Error(tn,"size of array has non-integer type");
		         }

		         // The array size must be a constant integral
		         if (t_expr.isConstantIntegral()) {
		           // Check that the number of element is positive
		           int dim=t_expr.getConstantIntegralValue().intValue();
		           if (dim<0) {
		             Error(tn,"size of array is negative");
		             dim=0; // to continue the compilation process
		           }
		           else if (dim==0) {
		             Warning(1,tn,"ISO C forbids zero-size arrays");
		           }
		           array_type.setNbElements(dim);
		         }
		         else {
		           // C89: forbids variable length arrays
		           // Warning(1,tn,"ISO C89 forbids variable length array '"+id_node.getText()+"'");
		           // C99: variabl length array allowed in a function, not at top level
		           array_type.setVariableSize();
		           // OpenCL C: forbids variable length arrays
		           if (oclLanguage) {
		             if (isCLExtPrivateVariableLengthArray()) {
		               // The stack can not be computed anymore
		               unsized_stack_requested=true;
		               Warning(tn,"[OCL] variable length arrays disables automatic stack size computation");
		             }
		             else {
		               Error(tn,"[OCL] variable length arrays are forbidden");
		             }
		           }
		           if (vxLanguage) {
		             Error(tn,"[VX] variable length arrays are forbidden");
		           }
		         }
			 }
		       )?   

		       RBRACKET
		        {
			  if (is_array_of_array && (!array_type.isComplete())) {
			    Error(l1,"array type has incomplete element type");
			  }
			}
		     )

		   )*

            |  ( 
		   (
		     LPAREN
		     {
		       subDeclaratorMarker=new ChildType.Marker();
		     }
		     ( nonemptyAbstractDeclarator[tag_split,
						  symbol,
						  new_def_param,tag_ref,
						  subDeclaratorMarker] 
		      )?
		      RPAREN
		    )

		   | #( n2:NParameterTypeList
		         {
			   // Manage function type building
			   //------------------------------
			   func_type=new Function();
			   func_type.setReturnType(input_type);
			   if (last_encountered_child_type==null) {
			     // This is the first type to be declared in the current declarator

			     // If a marker has a parent (meaning that a the current declarator has
			     // a sub-declaratior), the current function type must be a child of
			     // the marker's parent (type of the sub-declarator).
			     // Otherwise, we are in the declaration root
			     if (subDeclaratorMarker.hasParent()) {
			       // Set the current function type as child (function return type,
			       // sub-array type or pointed type) of the marker's parent type
			       // (type of the sub-declarator which can only be function, array
			       // or pointer)
			       ChildType parent=(ChildType)(subDeclaratorMarker.getParent());
			       parent.setChild(func_type);
			       // Check that function can be the child of the parent type
			       typeManager.checkChildAsFunctionAbstract(n2,compilerError,
									parent);

			     }
			     else {
			       // We are at the declaration root, overwrite the symbol type
			       // which has been set to 'input_type' before or after the
			       // pointer group
			       symbol.setType(func_type);
			     }
			   }
			   else {
			     // A type has already been declared in the current declarator
			     // (may be a function or an array)
			     // Substitute its child type (return type for a function, sub-type)
			     // for an array by the current function type
			     last_encountered_child_type.setChild(func_type);
			     // Check that function can be the child of the parent type
			     typeManager.checkChildAsFunctionAbstract(n2,compilerError,
								      last_encountered_child_type);
			   }
			   last_encountered_child_type=func_type;
			   
			   // Manage symbol scopes
			   //---------------------
			   // We enter a prototype parameter list
			   symbolTable.pushScope("!" + getNewName());
			 }
		      
			( parameterTypeList[tag_split,
					    new_def_param,tag_ref,
					    func_type,
					    false] // not a function definition
			 )?
			RPAREN
		         {
			   symbolTable.popScope();
			 }
		      )

		   | (
		        l2:LBRACKET 
		         {
			   is_array_of_array=false;

			   // Manage array type building
			   //---------------------------
			   array_type=new Array(input_type);
			   if (last_encountered_child_type==null) {
			     // This is the first type to be declared in the current declarator

			     // If a marker has a parent (meaning that a the current declarator has
			     // a sub-declaratior), the current array type must be a child of
			     // the marker's parent (type of the sub-declarator).
			     // Otherwise, we are at the declaration root
			     if (subDeclaratorMarker.hasParent()) {
			       // Set the current array type as child (function return type,
			       // sub-array type or pointed type) of the marker's parent type
			       // (type of the sub-declarator which can only be function, array
			       // or pointer)
			       if (subDeclaratorMarker.getParent().isArray()) {
				 is_array_of_array=true;
			       }
			       ChildType parent=((ChildType)(subDeclaratorMarker.getParent()));
			       parent.setChild(array_type);
			       // Check that array can be the child of the parent type
			       typeManager.checkChildAsArray(l2,compilerError,
							     parent);
			     }
			     else {
			       // we are at the declaration root, overwrite the symbol type
			       // which has been set to 'input_type'
			       symbol.setType(array_type);
			     }
			   }
			   else {
			     // A type has already been declared in the current declarator
			     // (may be a function or an array)
			     // Substitute its child type (return type for a function, sub-type)
			     // for an array by the current array type
			     if (last_encountered_child_type.isArray()) {
			       is_array_of_array=true;
			     }
			     last_encountered_child_type.setChild(array_type);
			   }
			   last_encountered_child_type=array_type;
			   // Check that array can be the child of the parent type
			   typeManager.checkChildAsArray(l2,compilerError,
							 last_encountered_child_type);
			 }

		       // Must be const expr
		       ( t_expr=tn2:expr
		         {
		         // The array size must have integral type
		         if (!t_expr.getType().isIntegralScalar()) {
		           Error(tn2,"size of array has non-integer type");
		         }

		         // The array size must be a constant integral
		         if (t_expr.isConstantIntegral()) {
		           array_type.setNbElements(t_expr.getConstantIntegralValue().intValue());
		         }
		         else {
		           // C89: forbids variable length arrays
		           // Warning(1,tn,"ISO C89 forbids variable length array '"+id_node.getText()+"'");
		           // C99: variabl length array allowed in a function, not at top level
		           array_type.setVariableSize();
		           // OpenCL C: forbids variable length arrays
		           if (oclLanguage) {
		             if (isCLExtPrivateVariableLengthArray()) {
		               // The stack can not be computed anymore
		               unsized_stack_requested=true;
		               Warning(tn,"[OCL] variable length arrays disables automatic stack size computation");
		             }
		             else {
		               Error(tn,"[OCL] variable length arrays are forbidden");
		             }
		           }
		           if (vxLanguage) {
		             Error(tn,"[VX] variable length arrays are forbidden");
		           }
		         }
			 }
		       )? 
  
		       RBRACKET

		        {
			  if (is_array_of_array && (!array_type.isComplete())) {
			    Error(l2,"array type has incomplete element type");
			  }
			}
		     )
                )+
              )
            )
        ;




//##################################################################
//                Function definition/prototypes
//##################################################################


//------------------------------------------------------------------
// parameterTypeList :
//
// List of the parameter list of:
//   - a function definition
//   - a function prototype declaration
//   - a pointer to a function
//------------------------------------------------------------------
parameterTypeList[boolean tag_split,
		  LinkedList<Symbol> new_def_param,
		  LinkedList<Symbol> tag_ref,
		  Function func_type,
		  boolean function_def]
        {
	  Symbol symbol;
	  LinkedHashMap<String,NodeAST> parameter_map=new LinkedHashMap<String,NodeAST>();
	}
	: (
	   (
	       symbol= p:parameterDeclaration[tag_split,
					      new_def_param,tag_ref,
					      func_type,
					      function_def,
					      parameter_map] 
	       {
		 if (symbol!=null) {
		   symbol.setDeclarationNode(p);
		 }
	       }
	   )
	    ( COMMA | SEMI )?
           )+
	  ( va:VARARGS 
	    {
	      if (func_type.isVoidParameterList()) {
		Error(va,"void in parameter list must be the entire list");
	      }
	      else if (!func_type.hasParameter()) {
		Error(va,"ISO C requires a named argument before '...'");
	      }
	      else {
		func_type.addVararg();
	      }  
	    }
	    )?
       ;

//------------------------------------------------------------------
// parameterDeclaration:
//
// Declaration of a parameters of:
//   - a function definition
//   - a function prototype declaration
//   - a pointer to a function
//------------------------------------------------------------------
parameterDeclaration[boolean tag_split,
		     LinkedList<Symbol> new_def_param,
		     LinkedList<Symbol> tag_ref,
		     Function func_type,
		     boolean function_def,
		     LinkedHashMap<String,NodeAST> parameter_map
		     ]
returns [Symbol returned_symbol]
        {
	  TypeSpecifierQualifier specifier_qualifier = new TypeSpecifierQualifier();
	  StorageClass storageclass=new StorageClass();
	  // A parameter is necessarily a variable
	  Variable parameter_symbol = new Variable(storageclass);

	  returned_symbol         = null;
	  AST id_node;

	  boolean declarator=false;

	  // Dummy specifier type (temporary)
	  Type specifier_type=null;
	}
        :   #( NParameterDeclaration
	       // Possibly requests the split of specifiers
	       // (only in the parameter list of a function definition)
	       ds:declSpecifiers[false,
				 tag_split,
				 true,  // It is a parameter
				 specifier_qualifier,
				 storageclass,
				 new_def_param,tag_ref,new_def_param]
	       {
		 specifier_type=specifier_qualifier.getType(ds,compilerError);
	       }

	       // We never split tag types definition in the declarator of a
	       // function parameter, (such type tags have the scope of a 
	       // function prototype parameter list)
	       // Nevertheless, some 'external' type tags can be referenced
	       // in the declarator, so that 'tag_ref' is transmitted
	       ( 
		 id_node = declarator[tag_split,
				      parameter_symbol,
				      false,	// Not a function definition at this level
				      new_def_param,tag_ref,
				      specifier_type]
                 {
		   //-- Manage the symbol --
		   //-----------------------

		   // Sets the symbol name
		   String declName=id_node.getText();
		   parameter_symbol.setName(declName);

		   // set pointers to AST
		   parameter_symbol.setIdNode((NodeAST)#id_node);

		   //##################################################################
		   // Finalize the parameter type
		   //##################################################################

		   // Type already set, but ... :
		   // 'Array of type' must be appointed 'pointer of type' in function parameter
		   // (cf. C89, paragraph 6.7.1)
		   Type param_type=parameter_symbol.getType();
		   if (param_type.isArray()) {
		     // [TBW] An array should never be qualified, since the qualifier goes to the
		     //       sub-element. In case of qualification of a typename which is itself
		     //       a qualified array, both qualifiers should be merged.
		     if (param_type instanceof Qualifier) {
		       InternalError((NodeAST)id_node,"Qualified Array, not supported yet");
		     }
		     param_type=new Pointer(param_type.getElementType());
		     parameter_symbol.setType(param_type);
		   }

		   // [OCL] By default, a function argument is in the 'private' address space
		   if (oclLanguage) {
		     Type the_type=parameter_symbol.getType();
		     if (the_type.isQualified()) {
		       Qualifier q=the_type.getQualifier();
		       if (q.getAddressSpace()==AddressSpace.NO) {
			 q.setAddressSpace(AddressSpace.PRIVATE);
		       }
		     }
		     else {
		       parameter_symbol.setType(new Qualifier(AddressSpace.PRIVATE,the_type));
		     }
		   }


		   //##################################################################
		   // Check for redefinition and put the symbol in the symbol table
		   // if it is a parameter of function definition
		   //##################################################################

		   // Parameters of function prototype do not need to be put in the symbol table
		   // Check for redefinition with the parameter map
		   NodeAST the_node=parameter_map.get(declName);
		   if (the_node!=null) {
		     Error((NodeAST)id_node,"redefinition of parameter `"+declName+"'");
		     Message(the_node,
			   "previous definition of `"+declName+"' was here");
		   }
		   else {
		     // Put the parameter in the map
		     parameter_map.put(declName,(NodeAST)id_node);

		     // No need to put function prototypes parameters in the symbol table
		     if (function_def) {
		       addObjectLabelSymbol(declName,parameter_symbol);
		     }
		   }

		   // Link AST -> symbol
		   //-------------------
		   ((NodeAST)#id_node).setDefinition(parameter_symbol);

		   //##################################################################
		   // Add a parameter to the enclosing 'Function' type
		   //##################################################################

		   // Adds a new parameter to the enclosing function type
		   // Note: no check is performed here for name conflict. The check will be
		   // done at placement of the symbol into the symbol table
		   if (func_type.isVoidParameterList()) {
		     // Void parameter list must be empty
		     Error(ds,"void in parameter list must be the entire list");
		   }
		   else {
		     func_type.addParameter(parameter_symbol.getType());
		   }

		   //##################################################################
		   // Complete type parameter check
		   //##################################################################

		   // Last type check
		   if (parameter_symbol.getType().isIncompleteOrVoid()) {
		     if (function_def) {
		       // Raise an error
		       Error((NodeAST)id_node,"parameter '"+declName+"' has incomplete type");
		     }
		     else {
		       // Raise a warning
		       Warning((NodeAST)id_node,"parameter '"+declName+"' has incomplete type");
		     }
		   }

		   //##################################################################
		   // Check storage class
		   //##################################################################

		   // No multiple storage class allowed
		   StorageClass sc=parameter_symbol.getStorageClass();
		   if (sc.isMultipleStorageClass()) {
		     Error((NodeAST)id_node,
			   "multiple storage classes in declaration of parameter `"
			   +declName+"'");
		   }
		   // Most of storage class specifiers not allowed
		   if ( sc.isTypedef() || sc.isExtern() ||
			sc.isStatic() || sc.isAuto() ) {
		     Error((NodeAST)id_node,
			   "storage class specified for parameter `"
			   +declName+"'");
		   }

		   //##################################################################
		   // Epilog
		   //##################################################################

		   // The symbol has a declarator and a name, since without name, it would
		   // match a non empty abstract declarator
		   declarator=true;

		   // The declaration node is set by the upper rule
		   returned_symbol=parameter_symbol;		   
		 }

		 | node:nonemptyAbstractDeclarator[tag_split,
						   parameter_symbol,
						   new_def_param,tag_ref,
						   specifier_type]
		   {
		     // The parameter has a declarator but no name
		     //-------------------------------------------

		     //##################################################################
		     // Finalize the type of the parameter
		     //##################################################################

		     // Type already set, but ... :
		     // 'Array of type' must be appointed 'pointer of type' in function parameter
		     // (cf. C89, paragraph 6.7.1)
		     Type param_type=parameter_symbol.getType();
		     if (param_type.isArray()) {
		       // [TBW] An array should never be qualified, since the qualifier goes to the
		       //       sub-element. In case of qualification of a typename which is itself
		       //       a qualified array, both qualifiers should be merged.
		       if (param_type instanceof Qualifier) {
			 InternalError(node,"Qualified Array, not supported yet");
		       }
		       param_type=new Pointer(param_type.getElementType());
		       parameter_symbol.setType(param_type);
		     }

		     // [OCL] By default, a function argument is in the 'private' address space
		     if (oclLanguage) {
		       Type the_type=parameter_symbol.getType();
		       if (the_type.isQualified()) {
			 Qualifier q=the_type.getQualifier();
			 if (q.getAddressSpace()==AddressSpace.NO) {
			   q.setAddressSpace(AddressSpace.PRIVATE);
			 }
		       }
		       else {
			 parameter_symbol.setType(new Qualifier(AddressSpace.PRIVATE,the_type));
		       }
		     }


		     //##################################################################
		     // Add a parameter to the enclosing 'Function' type setting
		     //##################################################################
		     
		     // Adds a new parameter to the function type
		     if (func_type.isVoidParameterList()) {
		       // Void parameter list must be empty
		       Error(ds,"void in parameter list must be the entire list");
		     }
		     else {
		       // Add the parameter
		       func_type.addParameter(parameter_symbol.getType());

		       if (function_def) {
			 // Function definition must have a name
			 Error(ds,"parameter name omitted for function definition");
		       }
		     }

		     //##################################################################
		     // Check for complete parameter
		     //##################################################################

		     // Last type check
		     if (parameter_symbol.getType().isIncompleteOrVoid()) {
		       if (function_def) {
			 // Raise an error
			 Error(node,"parameter has incomplete type");
		       }
		       else {
			 // Raise a warning
			 Warning(node,"parameter has incomplete type");
		       }
		     }

		     //##################################################################
		     // Check storage class
		     //##################################################################

		     // No multiple storage class allowed
		     StorageClass sc=parameter_symbol.getStorageClass();
		     if (sc.isMultipleStorageClass()) {
		       Error(ds,
			     "multiple storage classes in declaration of parameter");
		     }
		     // Most of storage class specifiers not allowed
		     if ( sc.isTypedef() || sc.isExtern() ||
			  sc.isStatic() || sc.isAuto() ) {
		       Error(ds,"storage class specified for parameter");
		     }

		     //##################################################################
		     // Epilog
		     //##################################################################

		     // Informs that the declarator is present
		     declarator=true;

		     // No need to return a symbol (returned_symbol) since no parameter
		     // identifier
		   }
		 )?
		( attributeSpecifierList )?
	       )
	      {
		if (!declarator) {
		  // The parameter is only declared with a specifier (no declarator)
		  //----------------------------------------------------------------

		  // Special case for f(void) {...} which is allowed
		  if ((specifier_type.isVoid())) {
		    if (!func_type.addVoidSpecifier()) {
		      Error(ds,"void in parameter list must be the entire list");
		    }
		  }
		  else {
		    if (func_type.isVoidParameterList()) {
		      // Void parameter list must be empty
		      Error(ds,"void in parameter list must be the entire list");
		    }
		    else {
		      // Add the parameter
		      func_type.addParameter(specifier_type);
		      
		      if (function_def) {
			// No symbol defined as parameter of a function definition
			// This is an error
			Error(ds,"parameter name omitted for function definition parameter");
		      }
		      else {
			// Last check
			if (specifier_type.isIncompleteOrVoid()) {
			  Warning(ds,"parameter has incomplete type");
			}
		      }
		    }

		  }

		  //-- Check storage class
		  // No multiple storage class allowed
		  StorageClass sc=parameter_symbol.getStorageClass();
		  if (sc.isMultipleStorageClass()) {
		    Error(ds,
			  "multiple storage classes in declaration of parameter");
		  }
		  // Most of storage class specifiers not allowed
		  if ( sc.isTypedef() || sc.isExtern() ||
		       sc.isStatic() || sc.isAuto() ) {
		    Error(ds,"storage class specified for parameter");
		  }
		  
		  // No check for initializations since no initializer by 
		  // construction in the grammar
		}
	      }
        ;



//##################################################################
//                     Function definition
//##################################################################

//------------------------------------------------------------------
// functionDef:
//
// Function definition with its parameters and its body
// or 'function definition' symbol.
// It may contain sereral symboles in case of:
//    - function definition,
//    - function prototype declaration
//    - pointer to function
//------------------------------------------------------------------
functionDef
        {
	  // Spliting lists
	  LinkedList<Symbol> new_def       = new LinkedList<Symbol>();
	  LinkedList<Symbol> new_def_param = new LinkedList<Symbol>();
	  LinkedList<Symbol> tag_ref       = new LinkedList<Symbol>();
	  // Specifier and qualifiers for the function
	  TypeSpecifierQualifier function_specifier_qualifier
	    = new TypeSpecifierQualifier();
	  // Storage class for the function
	  StorageClass function_storageclass
	    =new StorageClass();
	  // Symbol of the function
	  FunctionLabel function_symbol = new FunctionLabel(function_storageclass);
	  // 'ID' node of the function
	  AST     id_node;
	  // Function name
	  String  scopeName = null;
	  // Specifier management
	  Boolean specifier   = false;
	  Type specifier_type = null;
	  // Tag split
	  boolean tagSplit = false;
	  if (isDeclarationSplitRequested()) {
	    tagSplit=true;
	  }
	}
        :   #( NFunctionDef
	       // Request to split function specifiers
	       // get new type tags into 'new_def'
	       ( ds:functionDeclSpecifiers[tagSplit,
					   function_symbol,
					   function_specifier_qualifier,
					   function_storageclass,
					   new_def,tag_ref,new_def_param]
		 {
		   specifier=true;
		   currentFunctionIsKernel=function_symbol.isKernel();
		 }
	       )?
	         {
		   if (specifier==true) {
		     // Build the type of the specifier 
		     specifier_type=function_specifier_qualifier.getType(ds,compilerError);
		   }
		   else {
		     // The function returns an int
		     specifier_type=IntegerScalar.Tsint;
		   }
	         }
	       // Request to split only parameters of the function
	       // Gets new type tags into 'new_def_param'
	       id_node = d:declarator[tagSplit,
				      function_symbol,
				      true,
				      new_def_param,tag_ref,
				      specifier_type]
	       {
		 //-- Manage the symbol --
		 //-----------------------
		 // sets the functin name
		 String declName=id_node.getText();
		 function_symbol.setName(declName);

		 // Sets the family, it is a function def
		 function_symbol.setDefinition();

		 // Type already set

		 // set pointers to AST
		 function_symbol.setIdNode((NodeAST)#id_node);
		 function_symbol.setDeclarationNode(#functionDef);


		 //-- Check function type
		 Type theType=function_symbol.getType();
		 if (!theType.isFunction()) {
		   FatalError((NodeAST)id_node,
			 "function `"+id_node.getText()+
			 "' not defined as a function type (no parameters declaration)");
		 }

		 // If the function definition has no parameter, it is equivalent to
		 // have a void parameter. Adding void allows distinguishing the
		 // function prototype without any parameter (which matchs any parameter
		 // lists) from the function definition without parameter
		 if (!((Function)theType).hasParameter()) {
		   ((Function)theType).addVoidSpecifier();
		 }

		 // For type checking of 'return' statements in the function body
		 currentFunctionDefReturnType=((Function)theType).getReturnType();

		 //-- Check storage class
		 // No multiple storage class allowed
		 StorageClass sc=function_symbol.getStorageClass();
		 if (sc.isMultipleStorageClass()) {
		   Error((NodeAST)id_node,
			 "multiple storage classes in declaration of function `"
			 +id_node.getText()+"'");
		 }
		 // 'typedef', 'register' and 'auto' storage class specifiers not allowed
		 // for a function definition
		 if (sc.isTypedef()) {
		   Error((NodeAST)id_node,"function definition `"
			 +id_node.getText()+"'declared `typedef'");
		 }
		 else if (sc.isRegister()) {
		   Error((NodeAST)id_node,"function definition `"
			 +id_node.getText()+"'declared `register'");
		 }
		 else if (sc.isAuto()) {
		   Error((NodeAST)id_node,"function definition `"
			 +id_node.getText()+"'declared `auto'");
		 }

		 // Check for builtin function
		 if (builtinManager.isBuiltinFunctionName(declName)) {
		   Error((NodeAST)id_node,"function '" + id_node.getText() +
			 "' is a compiler builtin function and can not be defined");
		 }

		 // [OCL] Check the kernel function
		 if (oclLanguage) {
		   Function function_type=(Function)function_symbol.getType();

		   // [OCL] Kernel/Function arguments checks
		   LinkedList<Type> parameter_type_list=function_type.getParameterTypeList();
		   for (Type the_type : parameter_type_list) {
		     // [OCL] Kernel arguments must be in the 'private' address space
		     if (the_type.isQualified()) {		     
		       AddressSpace as=(the_type.getQualifier()).getAddressSpace();
		       switch(as) {
		       case CONSTANT:
		       case GLOBAL:
		       case LOCAL:
			 if (currentFunctionIsKernel) {
			   Error((NodeAST)id_node,"[OCL] kernel argument '" + declName + 
				 "' is declared as " + as.getName() + 
				 ", but can only be declared as __private");
			 }
			 else {
			   Error((NodeAST)id_node,"[OCL] function argument '" + declName + 
				 "' is declared as " + as.getName() + 
				 ", but can only be declared as __private");
			 }
			 break;
		       case NO:
			 // Should never occur
			 InternalError((NodeAST)id_node,"[OCL] No-test 1");
			 break;
		       case PRIVATE:
			 // OK, nothing to do
		       }
		     }
		     else {
		       // Should never occur, since by default they have been put in the 
		       // __private address space
		       InternalError((NodeAST)id_node,"[OCL] No-test 2");
		       break;
		     }  
		   } // For


		   if (function_symbol.isKernel()) {
		     // [OCL] Sets the function type as kernel
		     function_type.setKernel();

		     // [OCL] Check that the function returns 'void'
		     Type input_type=function_type.getReturnType();
		     if (!input_type.isVoid()) {
		       Error((NodeAST)id_node,"[OCL] kernel '" + id_node.getText() +
			     "' must return void");
		     }

		     // [OCL] Kernel arguments checks
		     for (Type the_type : parameter_type_list) {
		       // Kernel pointer arguments can only point to global, constant and
		       // local address spaces
		       if (the_type.isPointer()) {
			 Type pointed_type=the_type.getPointedType();
			 if (pointed_type.isQualified()) {
			   AddressSpace as=(pointed_type.getQualifier()).getAddressSpace();
			   switch(as) {
			   case NO:
			     // Should never occur
			     InternalError((NodeAST)id_node,"[OCL] No-test 1");
			     break;
			   case PRIVATE:
			     Error((NodeAST)id_node,"[OCL] kernel pointer argument '" + declName + 
				   "' points to address space " + as.getName() + 
				   ", but can only be point to __global, __local or __constant address spaces");
			     break;
			   case CONSTANT:
			   case GLOBAL:
			   case LOCAL:
			     // OK, nothing to do
			   }
			 }
			 else {
			   Error((NodeAST)id_node,"[OCL] kernel argument '" + declName + 
				 "' can only be a pointer to __global, __local or __constant address space");
			 }
		       }

		     } // For

		   } // isKernel

		 } // oclLanguage


		 // Add the new symbol and the associated 'symbol'
		 // in the symbol table
		 addFunctionSymbol(declName,function_symbol);

		 // Link AST -> symbol
		 ((NodeAST)#id_node).setDefinition(function_symbol);

		 // Scope name
		 scopeName=id_node.getText();

		 // tag ref for the function def
		 for(Symbol symb:tag_ref) {
		   function_symbol.addParent(symb);
		 }
	       }

	       //!!-----------------------------------!!
	       //!!-----  OLD STYLE C parameters -----!!
               {
		 symbolTable.pushScope("#"+getFunctionDefNumber()+"#" + scopeName);
	       }
	       ( declaration )*
               {
		 symbolTable.popScope();
	       }
	       //!!-----------------------------------!!
	       //!!-----------------------------------!!

               (attributeSpecifierList)?

	       {
		 currentFunctionSymbol=function_symbol;
	       }
	       compoundStatement_in_funcdef[scopeName]
	       {
		 currentFunctionSymbol=null;
	       }
	      )

	    {
	      // In case of declaration split requested, all symbol declared
	      // in the statement (type tags, typedef, variables, function label)
	      // have been put in lists
	      //
	      // + new_def:
	      //   -------
	      //   list of symbols defined in specifiers 
	      //   ('struct', 'union' and 'enum' type tags)
	      //
	      // + new_def_param:
	      //   -------------
	      //   list of symbols defined in specifiers of function
	      //   parameter list of a declarator
	      //   ('struct', 'union' and 'enum' type tags)
	      //
	      // Additionnal information are also available:
	      //
	      // + tag_ref:
	      //   -------
	      //   list of type tags symbols referenced in the definition
	      //   specifier
	      //


	      // New declaration of function specifiers.
	      if ( (new_def.size()!=0) || (new_def_param.size()!=0) ) {
		// Occurs only in case of declaration split requested

		// Keep the 'NFunctionDef' tree
		NodeAST previous = (NodeAST)currentAST.root;

		// Reset 'currentAST'
		currentAST.root  = null;
		currentAST.child = null;

		// new_def first since occuring in specifiers
		while ( new_def.size() != 0 ) {
		  // if only specifiers declared, it may be the declaration of
		  // a struct, an union or an enum
		  Symbol s = new_def.removeFirst();
		  astFactory.addASTChild(currentAST,s.getDeclarationNode());
		}

		// New declaration of function parameters specifiers
		// new_def_param second since occuring in declarators
		while ( new_def_param.size() != 0 ) {
		  // if only specifiers declared, it may be the declaration of
		  // a struct, an union or an enum
		  Symbol s = new_def_param.removeFirst();
		  // the symbol moves to a upper scope, rename it
		  // current scope is not inside a tag
		  moveSymbolDeclaration(s,symbolTable.getCurrentScopeDepth());
		  // Put the declaration in the AST
		  astFactory.addASTChild(currentAST,s.getDeclarationNode());
		}


		// Add now the 'NFunctionDef' tree at the end of the list
		astFactory.addASTChild(currentAST, previous);
	
		// The result of function def is a list, there is no root
		// -> leave root at null
	      }

	      // Reset the kernel information
	      currentFunctionIsKernel=false;
	    }
        ;




//##################################################################
//                   	      Compound
//##################################################################


//------------------------------------------------------------------
// compoundStatement_in_funcdef
//
// Version of 'compoundStatement' for function definitions
// (the scope has the name of the function
//------------------------------------------------------------------
compoundStatement_in_funcdef[String scopeName]
{
  EnrichedType null_etype;
}
        :       #( NCompoundStatement
	    	  {
		    symbolTable.pushFunctionDefScope("#"+getAndIncrementFunctionDefNumber()+"#"+scopeName,compilerError);
		  }
                  ( null_etype=blockItemList )?
                  RCURLY
	    	  {
		    symbolTable.popFunctionDefScope(compilerError);
		  }
                )
        ;


//------------------------------------------------------------------
// compoundStatement
//
// Version of 'compoundStatement' when the scope has no name
// (if-then-else, loops etc ...)
//------------------------------------------------------------------
compoundStatement[boolean for_declaration]
returns [EnrichedType etype]
{
  // by default, it has the 'void' type
  etype=new EnrichedType(Void.Tvoid);
}
       :       #( NCompoundStatement
	    	  {
		    if (!for_declaration) {
		      symbolTable.pushScope();
		    }
		  }
                  ( etype=blockItemList )?
                  RCURLY
	    	  {
		    if (!for_declaration) {
		      symbolTable.popScope();
		    }
		  }
                )
        ;




//##################################################################
//---                                                           ----
//                  Management of symbol references
//---                                                           ----
//##################################################################


//------------------------------------------------------------------
// typedefName:
//
// Reference to a typedef symbol in a declaration
//------------------------------------------------------------------
typedefName[LinkedList<Symbol> tag_ref]
returns [Symbol s]
	{
	  s=null;
	}
	:       #(NTypedefName id_ref:ID)
		{
		  // Lookup in the symbol table and set 'id_ref'
		  s=lookupAndSetReference(#id_ref);
		  // Add the symbol as reference
		  tag_ref.add(s);
		}
        ;


//------------------------------------------------------------------
// primaryExpr:
//
// Main part of an expression: may reference symbols
//------------------------------------------------------------------
primaryExpr[boolean func]
returns [EnrichedType etype] 
	{
	  etype=null;
	  String string;
	}
	:       id_ref: ID
 		{
		  // Lookup in the symbol table and set the link #id_ref->symbol
		  Symbol s;
		  if (func) {
		    s=lookupAndSetReferenceToFunction(#id_ref);
		  }
		  else {
		    s=lookupAndSetReference(#id_ref);
		  }

		  if (s instanceof MangledFunctionPseudoLabel) {
		    // The type setting in 'etype' is deferred until the
		    // function can be desambiguated (when we have the whole function
		    // call parameter list)
		    etype=new EnrichedType((Type)null);
		    etype.setSymbolReference(s, #id_ref);

		    // It is necessarily a function
		    // -> It is a constant label resolved at compile time
		    etype.setConstantLabel();
		  }
		  else {
		    etype=new EnrichedType(s.getType());
		    etype.setSymbolReference(s, #id_ref);

		    if (s instanceof Variable) {
		      etype.setObjectDesignation();
		      // The variable object itself is not compile-time constant
		      // by definition, but its address may be compile time known
		      // so that a special processing must be done for the '&'
		      // unary operator
		      if (s.referencesCompileTimeAllocatedEntity()) {
			etype.setCompileTimeAllocatedObjectDesignation();
		      }
		      else {
			etype.setObjectDesignation();
		      }
		    }
		    else if (s instanceof ArrayLabel) {
		      etype.setObjectDesignation();
		      // In C, an array is (strangely) a label and
		      // a reference to an object, so we put both info
		      // for handling all cases of parent node
		      if (s.referencesCompileTimeAllocatedEntity()) {
			// It is a constant label resolved at link time
			etype.setCompileTimeAllocatedObjectDesignation();
			etype.setConstantLabel();
		      }
		      else {
			etype.setObjectDesignation();
		      }
		    }
		    else if (s instanceof FunctionLabel) {
		      // It is a constant label resolved at compile time
		      etype.setConstantLabel();
		    }
		    else if (s instanceof EnumConstant) {
		      // Get the constant value
		      etype.setConstantIntegral(BigInteger.valueOf(((EnumConstant)s).getValue()));
		    }
		    else if (s instanceof Typedef) {
		      // A typedef is not allowed as primary expression
		      Error(id_ref,"syntax error before typename '"+id_ref.getText()+"'");
		    }
		    else {
		      // Should never come here
		      InternalError(id_ref,"primaryExpr");
		    }
		  }
		  #id_ref.setDataType(etype);		  
		}
	|       n:IntegralNumber
		{
		  etype=typeManager.getIntegralNumberEnrichedType(n,compilerError,n.getText());
		  // Some checking
		  if (etype.getType().isLongLongScalar() && (!Type.getSourceABI().isLongLongAllowed())) {
		    Error(n,"long long not allowed");
		  }
		  #n.setDataType(etype);
		}
	|       fn:FloatingPointNumber
		{
		  etype=typeManager.getFloatingPointNumberEnrichedType(compilerError,fn.getText());
		  // Some checking
		  if (etype.getType().isLongDoubleScalar() && (!Type.getSourceABI().isLongDoubleAllowed())) {
		    Error(fn,"long double not allowed");
		  }
		  #fn.setDataType(etype);
		}
	|       cs:charConst
		{
		  Type the_type;
		  the_type=IntegerScalar.Tschar;
		  // It's a constant char	  
		  etype=new EnrichedType(the_type);
		  etype.setConstantIntegral(BigInteger.valueOf(cs.getText().codePointAt(1)));
		  //System.out.println( "Caractere " + cs.getText() + " = " + cs.getText().codePointAt(1));
		  #cs.setDataType(etype);
		}
	|       string=sc:stringConst
		{
		  // A string is an array of char
		  Array the_type;
		  if (oclLanguage) {
		    // In OpenCL, a string is in the __constant address space
		    the_type=new Array(new Qualifier(AddressSpace.CONSTANT,IntegerScalar.Tschar));
		  }
		  else {
		    the_type=new Array(IntegerScalar.Tschar);
		  }
		  //System.out.println( "String " + string + " = " + string.length());
		  the_type.setNbElements(string.length()-2);
		  etype=new EnrichedType(the_type);
		  etype.setConstantString();
		  #sc.setDataType(etype);
		}

	|       #( eg:NExpressionGroup etype=expr )
  		{
		  // Simply transmit the same enriched type
		  // with all its properties
		  #eg.setDataType(etype);
		}
	|       etype=compoundStatementExpr // GnuC specific
	|       etype=vectorLiteral         // OpenCL C
       ;


vectorLiteral
returns [EnrichedType etype] 
	{
	  Type t=null;
	  etype=null;
	  VectorLiteral literal=null;
	}
	:       #( nv:NVectorLiteral t=typeName_expr RPAREN
	    	   {
		     // Such construct is authorized only for vectors
		     if (!t.isVector()) {
		       FatalError(nv,"parenthesis literal can only be used for vectors");
		     }
		   }
	   
		   ( {symbolTable.isTopLevel()}? 
		     literal=l1:lparenthesisInitializer[true,(Vector)t.unqualify()]
		     {
		       // Compute TAG dependencies with regard to initialization
		       java.util.Vector<Symbol> v = new java.util.Vector<Symbol>();
		       getParentsFromInitializer(v,#l1);
		       literal.setParents(v); // tags/func/typedef from which it depends

		       // Link to global
		       literalList.add(literal); // Put in the literal table
		       #nv.setLiteral(literal);  // Link AST -> literal
		     }
		   | literal=l2:lparenthesisInitializer[false,(Vector)t.unqualify()]
		     {
		       // Compute TAG dependencies with regard to initialization
		       java.util.Vector<Symbol> v = new java.util.Vector<Symbol>();
		       getParentsFromInitializer(v,#l2);
		       literal.setParents(v); // tags/func/typedef from which it depends

		       // Link to global
		       literalList.add(literal); // Put in the literal table
		       #nv.setLiteral(literal);   // Link AST -> literal
		     }
		   )
		    {
		      // The type checking is done the 'lparenthesisInitializer' rule
		      
		      // It is a literal, it does not designate an object
		      etype=new EnrichedType(t);
		      		      // When literals for agregate will be implemented, we should propagate
		      // or not the constant information
		      // [TBW]
		      
		      // Annotate the NCast node with the type
		      ##.setDataType(etype);
		    }
		 ) 
        ;


compoundLiteral
returns [EnrichedType etype] 
	{
	  Type t=null;
	  etype=null;
	  Literal literal=null;
	}
	:       #( nc:NCompoundLiteral t=typeName_expr RPAREN 
		   ( {symbolTable.isTopLevel()}? 
		     // 'true' -> only constant initializer elements authorized
		     literal=l1:lcurlyInitializer[true,t,0]
		     {
		       // Compute TAG dependencies with regard to initialization
		       java.util.Vector<Symbol> v = new java.util.Vector<Symbol>();
		       getParentsFromInitializer(v,#l1);
		       literal.setParents(v); // tags/func/typedef from which it depends

		       // Link to global
		       literalList.add(literal); // Put in the literal table
		       #nc.setLiteral(literal);   // Link AST -> literal

		       etype=new EnrichedType(t);
		       
		       // It is a compound literal, and at the difference of a constant literal,
		       // it designates an object		      
		       // In file scope, compound literals have static storage duration
		       etype.setCompileTimeAllocatedObjectDesignation();
		     }   
		     // 'false' -> allows non constant initializer elements
		   | literal=l2:lcurlyInitializer[false,t,0]
		     {
		       // Compute TAG dependencies with regard to initialization
		       java.util.Vector<Symbol> v = new java.util.Vector<Symbol>();
		       getParentsFromInitializer(v,#l2);
		       literal.setParents(v); // tags/func/typedef from which it depends

		       // Link to global
		       literalList.add(literal); // Put in the literal table
		       #nc.setLiteral(literal);   // Link AST -> literal

		       etype=new EnrichedType(t);

		      // It is a compound literal, and at the difference of a constant literal,
		      // it designates an object		      
		       etype.setObjectDesignation();
		     }
		   )
		    {
		      // GnuC allows compound initializers in expression combined with casts
		      // ex: (union { double __d; int __i[2]; }) {__d: __x}
		      // The type checking is done the 'lcurlyInitializer' rule
		      		      
		      // Annotate the NCast node with the type
		      ##.setDataType(etype);
		    }
		 )
        ;



//------------------------------------------------------------------
// postfixExpr:
//
// Post fix expression (structure elements, array index, function
// parameters list ...)

// No relevant symbols as ID in this rule (only structure member
// identifiers)
//------------------------------------------------------------------

postfixExpr
returns [EnrichedType etype] 
	{
	  EnrichedType t_expr;
	  etype=null;
	  Function func_call_type=null;
	}
    :  
           #( n1:PTR etype=expr id1:ID )
            {
	      Type current_type=etype.getType();
	      
	      // [OCL] question: is it allowed for pointers to vectors ?
	      if (current_type.isPointer()) {
		current_type=current_type.getPointedType();
	      }
	      else if (current_type.isArray()) {
		current_type=current_type.getElementType();
	      }
	      else {
		FatalError(n1,"invalid type argument of `->'");
	      }
	      
	      if (!(current_type.isStructOrUnion())) {
		FatalError(n1,"request for member in something not a structure or union");
	      }
	      current_type=current_type.getFieldType(id1.getText());
	      if (current_type==null) {
		FatalError(n1,"struct/union has no member named '" + id1.getText() + "'");
	      }
	      // It is not a constant expression (it could be)
	      EnrichedType current_etype=etype;
	      etype=new EnrichedType(current_type);

	      // It is a reference to an object
	      if (current_etype.isConstantScalar()) {
		etype.setCompileTimeAllocatedObjectDesignation();
	      }
	      else {
		etype.setObjectDesignation();
	      }

	      #n1.setDataType(etype);
	    }

	| #( n2:DOT etype=expr id2:ID )
	    {
	      Type current_type=etype.getType();

	      if (current_type.isStructOrUnion()) {
		// Get the fild type
		current_type=current_type.getFieldType(id2.getText());
		if (current_type==null) {
		  FatalError(n2,"structure or union has no member named '" + id2.getText() + "'");
		}
		
		EnrichedType current_etype=etype;
		etype=new EnrichedType(current_type);
		
		// It is a reference to an object
		if (current_etype.designateCompileTimeAllocatedObject()) {
		  etype.setCompileTimeAllocatedObjectDesignation();
		}
		else {
		  etype.setObjectDesignation();
		}
		
		// Should propagate constant information (when Literals
		// for struct/union will be available)
		// [TBW]
		//if (etype.isConstant()) {
		//  etype.setConstantStructOrUnion(...);
		//}
		// Propagate symbol information (since fields are not
		// symbols in the symbol table and fields a sub-element
		// of the enclosing object)
		// Note : 's.c' has same compile-time information as 's'
		// [TBW] the reference symbol is not used only to
		//       propagate compile time allocation property,
		//       but may also be used in other ways.
		//       This compiled time information should then
		//       better be propagated in the Enriched type
		//       instead of the symbol itself   
		etype.setSymbolReference(current_etype.getSymbolReference(),
					 current_etype.getIdReference());
	      }
	      else if (current_type.isVector()) {
		EnrichedType current_etype=etype;

		etype=typeManager.getVectorElementEnrichedType(id2,compilerError,
							       current_etype,
							       (Vector)current_type.unqualify(),
							       id2.getText());
		
		if (current_etype.designateAnObject()) {
		  // Put it as a reference to an object so that for l-value checking is positive
		  // [TBW] BUT :
		  //    - if the encapsulating vector is an object, the reference to the
		  //      sub-vector is not.
		  //    - this allows taking the address of a sub-vectors, what should not been
		  //      allowed by OpenCL
		  etype.setObjectDesignation();
		}
		
		// Propagate symbol information
		etype.setSymbolReference(current_etype.getSymbolReference(),
					 current_etype.getIdReference());
		// Note : 'v.s1234' has same compile-time information as 'v'
		// [TBW] the reference symbol is not used only to
		//       propagate compile time allocation property,
		//       but may also be used in other ways.
		//       This compiled time information should then
		//       better be propagated in the Enriched type
		//       instead of the symbol itself   

		#n2.setType(NSwizzle);
	      }
	      else {
		if (oclLanguage) {
		  FatalError(n2,"request for member '" + id2.getText()
			     + "' in something not a structure, union or vector");
		}
		else {
		  FatalError(n2,"request for member '" + id2.getText()
			     + "' in something not a structure or union or vector");
		}
	      }

             #n2.setDataType(etype);
	    }

        | #( n3:NFunctionCall 
	      (  (ID) => etype=primaryExpr[true]
	      |          etype=expr
	      )
	      {
		func_call_type = new Function();
	      }
	      ( argExprList[func_call_type] )?
	      RPAREN
	   )
 	   {
          Type current_type=etype.getType();

          Symbol function_symbol = etype.getSymbolReference();


          // Manages builtin functions
          if ((function_symbol!=null) &&
              (function_symbol instanceof FunctionLabel) &&
              ((FunctionLabel)function_symbol).isCompilerBuiltinFunction()
              ) {
            FunctionLabel symb=(FunctionLabel)function_symbol;

            String new_name=builtinManager.checkBuiltinFunction(n3,compilerError,
                symb,
                (Function)func_call_type);
            if (new_name!=null) {
              // The symbol must be renamed
              function_symbol.setType(func_call_type);
              symb.reName(new_name);
            }

            // Set the correct symbol to the node
            etype.getIdReference().setReference(symb);

            current_type=func_call_type;
          }

          // Manages potential overloading
          else if ((function_symbol!=null) &&
              (function_symbol instanceof MangledFunctionPseudoLabel)) {
            FunctionLabel symb;
            symb=typeManager.checkMangledFunctionCall(n3,compilerError,
                function_symbol.getName(),
                (MangledFunctionPseudoLabel)function_symbol,
                (Function)func_call_type);
            if (symb==null) {
              // Should never happend
              InternalError(n3,"NFunctionCall");
            }
            // Set the correct symbol to the node
            etype.getIdReference().setReference(symb);

            current_type=symb.getType();
          }

          // Standard function call
          else {
            // OCL specific : printf is currently a recursive function
            if (oclLanguage) {
              if (  current_type.isFunction() &&
                  (function_symbol!=null) &&
                  (function_symbol.getName().equals("printf"))
                  ) {
                // The stack can not be computed anymore
                unsized_stack_requested=true;
                Warning(n3,"[OCL] calling 'printf' in an OpenCL-C program disables automatic stack size computation");
              }
            }
            // VX specific : malloc and calloc are forbidden
            if (vxLanguage) {
              if ( current_type.isFunction() &&
                  (function_symbol!=null) &&
                  (function_symbol.getName().equals("calloc") ||
                   function_symbol.getName().equals("malloc") )
                  ) {
                // No dynamic memory allocation allowed
                Error(n3,"[VX] calling '"+function_symbol.getName()+"' is forbidden");
              }
            }

            // It may be a pointer to function
            boolean isPointer=false;
            if (current_type.isPointer()) {
              current_type=current_type.getPointedType();
              isPointer=true;
            }
            if (!(current_type.isFunction())) {
              FatalError(n3,"called object is not a function");
            }
            else if (isPointer) {
               if (vxLanguage) {
                  Error(n3,"[VX] calling function pointer '"+function_symbol.getName()+"' is forbidden");
               }
            }

            // Check the argument list
            if (function_symbol==null) {
              typeManager.checkFunctionCall(n3,compilerError,null,
                  (Function)current_type,
                  func_call_type);
            }
            else {
              typeManager.checkFunctionCall(n3,compilerError,function_symbol.getName(),
                  (Function)current_type,
                  func_call_type);
            }
          }

          // Get the returned type
          current_type=current_type.getReturnType();
          // It is not a constant expression nor an object reference
          etype=new EnrichedType(current_type);
          #n3.setDataType(etype);
	   }

	| #( n4:LBRACKET etype=expr t_expr=expr RBRACKET)
	    {
	      Type current_type=etype.getType();

	      // 1[a] and a[1] are allowed. One must be a pointer or array,
	      // the other an integer
	      Type bracket_type=t_expr.getType();
	      if (!current_type.isPointerOrArray()) {
		// Exchange place
		Type temp=bracket_type;
		bracket_type=current_type;
		current_type=temp;
	      }
	      
	      // The pointer must be in current_type
	      Type t=current_type;
	      if (t.isPointer()) {
		current_type=t.getPointedType();
		
		// Check that the array element has a known size
		if (current_type.isIncomplete()) {
		  Error(n4,"dereferencing pointer to incomplete type");
		}
		else if (current_type.isVoid()) {
		  Error(n4,"dereferencing `void *' pointer");
		}
	      }
	      else if (t.isArray()) {
		current_type=t.getElementType();
		
		// Check that the array element has a known size
		// Should never happen since checks check about array element
		// completness and non-void has already been done at declaration
		// time
		if (current_type.isIncomplete()) {
		  Error(n4,"dereferencing pointer to incomplete type");
		}
		// void array already detected
	      }
	      else {
		FatalError(n4,"subscripted value is neither array nor pointer");
	      }
	      if (!bracket_type.isIntegralScalar()) {
		Error(n4,"Array subscript is not an integer");
	      }			
	      
	      
	      EnrichedType current_etype=etype;
	      etype=new EnrichedType(current_type);
	      
	      // It is a reference to an object
	      if (current_etype.designateCompileTimeAllocatedObject() || current_etype.isConstantScalar()) {
		etype.setCompileTimeAllocatedObjectDesignation();
		// Note: an array is at the same time a label (adress type)
		// and a reference to an object (strangely)
		if (current_type.isArray()) {
		  etype.setConstantLabel();
		}
	      }
	      else {
		etype.setObjectDesignation();
	      }
	     	      
	      // Should propagate constant information (when Literals for array
	      // will be available)
	      // [TBW]
	      //if (etype.isConstant()) {
	      //  etype.setConstantArray(...);
	      //}
	      
	      // Propagate symbol information (since the address of an element is has the same property
	      // as the array label when the index is compile-time known)
	      // Note: 'tab[i] has same compile-time information as 'tab' if i is constant
	      // [TBW] the reference symbol is not used only to propagate compile time allocation
	      //       property, but ay also be used in other ways. This compiled time information
	      //       should then better be propagated in the Enriched type instead of the symbol itself   
	      if (t_expr.isConstantScalar()) {
		etype.setSymbolReference(current_etype.getSymbolReference(), current_etype.getIdReference());
	      }
	      
	      #n4.setDataType(etype);
	    }

	| #( n5:NPostInc etype=expr )
	   {
	      Type current_type=etype.getType();

	      // Check for scalar
	      if (!(current_type.isScalarOrLabel()||current_type.isVector())) {
		FatalError(n5,"wrong type argument to post-increment");
	      }
	      
	      // Check for lvalue
	      typeManager.checkModifiableLvalue(n5,compilerError,
						etype,"post-increment");
	      
	      // Must point to a size known type
	      if (current_type.isPointer()) {
		// Check that the pointed type has a known size
		if (current_type.getPointedType().isIncomplete()) {
		  Error(n5,"post-increment of pointer to incomplete type");
		}
	      }
	      // No array which is not a modifiable lvalue
	      
	      // It is not a constant expression nor an object reference
	      etype=new EnrichedType(current_type);
	      // Not an object reference
	      etype.setNonObjectDesignation();

              #n5.setDataType(etype);
	   }

	 | #( n6:NPostDec etype=expr )
	   {
	      Type current_type=etype.getType();

	      // Check for scalar
	      if (!(current_type.isScalarOrLabel()||current_type.isVector())) {
		FatalError(n6,"wrong type argument to post-decrement");
	      }

	      // Check for lvalue
	      typeManager.checkModifiableLvalue(n6,compilerError,
							  etype,"post-decrement");
	      
	      // Must point to a size known type
	      if (current_type.isPointer()) {
		// Check that the pointer type has a known size
		if (current_type.getPointedType().isIncomplete()) {
		  Error(n6,"post-decrement of pointer to incomplete type");
		}
	      }
	      // No array which is not a modifiable lvalue
	      
	      // It is not a constant expression nor an object reference
	      etype=new EnrichedType(current_type);
 	      // Not an object reference
	      etype.setNonObjectDesignation();

              #n6.setDataType(etype);
	   }
	|       etype=compoundLiteral       // C99 and GNUC

        ;


//##################################################################
//      Coming from typeof GNU extension
//##################################################################


typeName[boolean tag_split,
	 LinkedList<Symbol> new_def,
	 LinkedList<Symbol> tag_ref,
	 LinkedList<Symbol> new_def_param]
returns [Type type] 
	{
	  TypeSpecifierQualifier specifier_qualifier
	    =new TypeSpecifierQualifier();
	  // Create a dummy Symbol (since the abstract declarator works
	  // currently with a Symbol as main data structure)
	  Symbol symbol=new Symbol();
	  boolean declarator=false;
	  type=null;
	}
	: #( NTypeName 
             ds:specifierQualifierList[tag_split,
				       false, // Not a function parameter
				       specifier_qualifier,
				       new_def,tag_ref,new_def_param]
	     (
	      nonemptyAbstractDeclarator[tag_split,
					 symbol,
					 new_def_param,tag_ref,
					 specifier_qualifier.getType(ds,compilerError)]
	       {
		 declarator=true;
	       }
	     )?
	   )
	{
	  if (declarator) {
	    type=symbol.getType();
	  }
	  else {
	    type=specifier_qualifier.getType(ds,compilerError);
	  }

	  ##.setDataType(new EnrichedType(type));
	}
        ;



//##################################################################
//      Coming from casts in expressions
//##################################################################

//------------------------------------------------------------------
// typeName_expr:
//
// 'typeName' of an expression. Possible contained tag declarations
// must not be split
//------------------------------------------------------------------
typeName_expr
returns [Type type] 
	{
	  // Create dummy Symbol list (since specifier/qualifier and the
	  // non empty declarator need the;))
	  LinkedList<Symbol> new_def=new LinkedList<Symbol>();
	  LinkedList<Symbol> tag_ref=new LinkedList<Symbol>();
	  LinkedList<Symbol> new_def_param=new LinkedList<Symbol>();
	  final boolean tag_split = false; // We never split the declaration here

	  // Type management
	  //----------------
	  TypeSpecifierQualifier specifier_qualifier
	    =new TypeSpecifierQualifier();
	  // Create a dummy Symbol (since the abstract declarator works
	  // currently with a Symbol as main data structure)
	  Symbol symbol=new Symbol();
	  boolean declarator=false;
	  type=null;
	}
	: #( NTypeName
	     ds:specifierQualifierList[tag_split,
				       false, // Not a function parameter
				       specifier_qualifier,
				       new_def,tag_ref,new_def_param]
	     (
	       nonemptyAbstractDeclarator[tag_split,
					  symbol,
					  new_def_param,tag_ref,
					  specifier_qualifier.getType(ds,compilerError)]
	       {
		 declarator=true;
	       }
	      )?
	   )
	{
	  if (declarator) {
	    type=symbol.getType();
	  }
	  else {
	    type=specifier_qualifier.getType(ds,compilerError);
	  }
	  ##.setDataType(new EnrichedType(type));
	}
         ;




//##################################################################
//---                                                           ----
//           Not involved in variable/type declaration
//---                                                           ----
//##################################################################



// GNU attributes
attributeSpecifierList
        :
        ( attributeSpecifier )+
	;

attributeSpecifier
  {
    EnrichedType null_type;
  }
:       #( "__attribute"
	   (
	     id:ID (.)*
	       {
		 if ( id.getText().equals("vec_type_hint") ||
		      id.getText().equals("work_group_size_int") ||
		      id.getText().equals("reqd_work_group_size") ||
		      id.getText().equals("stack_size") ||
		      id.getText().equals("stack_in_extmem")
		      ) {
		   Warning(id,"Attribute '"+id.getText()+
			   "' looks like a non well placed OpenCL kernel attribute. It should be put behind the 'kernel' keyword, otherwise it is ignored");
		 }
	       }
	    | (.)*
	   )
	  )
        | #( NAsmAttribute LPAREN null_type=expr RPAREN )
        ;

idList
        :       ID ( COMMA ID )*
        ;


the_declaration
        :       
                (   //ANTLR doesn't know that declarationList properly eats all the declarations
                    //so it warns about the ambiguity
                    options {
                        warnWhenFollowAmbig = false;
                    } :
                localLabelDecl
                | declaration
                )
        ;


localLabelDecl
        :   #("__label__" (ID)+ )
        ;
   

blockItemList
returns [EnrichedType etype]
{
  etype=null;
}
        :       ( 
		        the_declaration
	    		{
			  // It has the 'void' type
			  etype=new EnrichedType(Void.Tvoid);
			}
		 |      functionDef
	    		{
			  // It has the 'void' type
			  etype=new EnrichedType(Void.Tvoid);
			}
		   // Get the type only for a statement
		 | etype=statement[false]
		 | pragma
		)+
        ;


statement[boolean for_declaration]
returns [EnrichedType etype]
{
  etype=null;
}
        :       etype=statementBody[for_declaration]
        ;




expr
returns [EnrichedType etype] 
	{
	  etype=null;
	}
        :       etype=assignExpr
        |       etype=constExpr
        ;

constExpr
returns [EnrichedType etype] 
	{
	  etype=null;
	}
        :	etype=conditionalExpr
        |       etype=logicalOrExpr
        |       etype=logicalAndExpr
        |       etype=inclusiveOrExpr
        |       etype=exclusiveOrExpr
        |       etype=bitAndExpr
        |       etype=equalityExpr
        |       etype=relationalExpr
        |       etype=shiftExpr
        |       etype=additiveExpr
        |       etype=multExpr
        |       etype=castExpr
        |       etype=unaryExpr
        |       etype=postfixExpr
        |       etype=primaryExpr[false]
        |       etype=commaExpr
        |       etype=emptyExpr
		  //        |       etype=initializer
        |       etype=gnuAsmExpr // Gnu specific
        ;


assignExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left=null,right=null;
	  Type t_expr=null;
	  etype=null;
	}
        :
	(
	 	 #( ASSIGN 	  left=expr right=expr)
 	    	   {
		     Type l=left.getType();

		     // Check operands types
		     typeManager.checkAssignOperands(##,compilerError,
						     l,right,"assignment");

		     // Check for correct Lvalue
		     typeManager.checkModifiableLvalue(##,compilerError,
						       left,"assignment");

		     // The resulting type is the unqualified version of the left operand
		     // An assign is not a constant expression statically evaluable
		     // An assign is not an object reference
		     etype=new EnrichedType(l.unqualify());

		     // Propagate constants
		     if (l.isFloatingPointScalar()) {
		       if (right.isConstantArithmetic()) {
			 etype.setConstantFloatingpoint(right.getConstantFloatingpointValue());
		       }
		     }
		     else if (l.isIntegralScalar()) {
		       if (right.isConstantArithmetic()) {
			 etype.setConstantIntegral(right.getConstantIntegralValue());
		       }
		     }
		     else if (l.isPointerOrLabel()) {
		       if (right.isConstantScalar()) {
			 etype.setConstantLabel();
		       }
		     }

		     // Annotate the node with the type
		     ##.setDataType(etype);
		   }

	 | (     #( DIV_ASSIGN    left=expr right=expr)
	  	   { 
		     // Check operands types
		     t_expr=typeManager.getTypeArithmeticBinaryOperator(##,compilerError,
									left.getType(),
									right.getType());
		   }
	    |    #( PLUS_ASSIGN   left=expr right=expr)
		   { 
		     // Check operands types
		     t_expr=typeManager.getTypeAdditiveBinaryOperator(##,compilerError,
								      this,
								      left.getType(),
								      right.getType(), false);
		   }
	    |    #( MINUS_ASSIGN  left=expr right=expr)
		   {
		     // Check operands types
		     t_expr=typeManager.getTypeAdditiveBinaryOperator(##,compilerError,
								      this,
								      left.getType(),
								      right.getType(), true);
		   }
	    |    #( STAR_ASSIGN   left=expr right=expr)
	  	   {
		     // Check operands types
		     t_expr=typeManager.getTypeArithmeticBinaryOperator(##,compilerError,
									left.getType(),
									right.getType());
		   }
	    |    #( MOD_ASSIGN    left=expr right=expr)
	  	   {
		     // Check operands types
		     t_expr=typeManager.getTypeIntegralBinaryOperator(##,compilerError,
								      left.getType(),
								      right.getType());
		   }
	    |    #( RSHIFT_ASSIGN left=expr right=expr)
	  	   {
		     // Check operands types
		     t_expr=typeManager.getTypeShiftOperator(##,compilerError,
							     left.getType(),
							     right.getType());
		   }
	    |    #( LSHIFT_ASSIGN left=expr right=expr)
	  	   {
		     // Check operands types
		     t_expr=typeManager.getTypeShiftOperator(##,compilerError,
							     left.getType(),
							     right.getType());
		   }
	    |    #( BAND_ASSIGN   left=expr right=expr)
	  	   {
		     // Check operands types
		     t_expr=typeManager.getTypeIntegralBinaryOperator(##,compilerError,
								      left.getType(),
								      right.getType());
		   }
	    |    #( BOR_ASSIGN    left=expr right=expr)
	  	   {
		     // Check operands types
		     t_expr=typeManager.getTypeIntegralBinaryOperator(##,compilerError,
								      left.getType(),
								      right.getType());
		   }
	    |    #( BXOR_ASSIGN   left=expr right=expr)
	  	   {
		     // Check operands types
		     t_expr=typeManager.getTypeIntegralBinaryOperator(##,compilerError,
								      left.getType(),
								      right.getType());
		   }
	   )
 	    {	  
	      // Check operands
	      typeManager.checkAssignOperands(##,compilerError,
					      left.getType(),t_expr,"assignment");

	      // Check for correct Lvalue
	      typeManager.checkModifiableLvalue(##,compilerError,
						left,"assignment");
	      
	      // The resulting type is the unqualified version of the left operand
	      // An assign is not a constant expression statically evaluable
	      // Assign is not an object reference
	      etype=new EnrichedType(left.getType().unqualify());
	      
	      // Annotate the node with the type
	      ##.setDataType(etype);
	    }
	)
        ;


// Note: here, 'left' is optional in the grammar.
//       it is not allowed in ANSI C 89 / 99. It seems to be GNU C.
conditionalExpr
returns [EnrichedType etype] 
	{
	  EnrichedType t_test=null;
	  EnrichedType right=null;
	  EnrichedType left=null;
	  Type the_type=null;
	  etype=null;
	}
	:       #( QUESTION t_test=expr (left=expr)? COLON right=expr )
 	{
	  // Check operands
	  the_type=typeManager.getTypeConditionalOperator(##,compilerError,
							  t_test.getType(),
							  left.getType(),right.getType());

	  // Create the enriched type
	  // Not an object reference
	  etype=new EnrichedType(the_type);

	  // Propagate constants
	  if (t_test.isConstantArithmetic() &&
	      left.isConstantArithmetic() &&
	      right.isConstantArithmetic()) {
	    //  Constant expression which can be evaluated here
	    if (the_type.isFloatingPointScalar()) {
	      // Expression result floating point
	      if (t_test.isConstantZero()) {
		etype.setConstantFloatingpoint(right.getConstantFloatingpointValue());
	      }
	      else {
		etype.setConstantFloatingpoint(left.getConstantFloatingpointValue());
	      }
	    }
	    else {
	      // Expression result integral
	      if (t_test.isConstantZero()) {
		etype.setConstantIntegral(right.getConstantIntegralValue());
	      }
	      else {
		etype.setConstantIntegral(left.getConstantIntegralValue());
	      }
	    }
	  }
	  else if (t_test.isConstantScalar() &&
		   left.isConstantScalar() && right.isConstantScalar()) {
	    // Constant expression but which can not be evaluated here
	    etype.setConstantLabel();
	  }

	  // Annotate the node with the enriched type
	  ##.setDataType(etype);
	}
        ;


logicalOrExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left,right;
	  etype=null;
	}
	:       #( LOR left=e_left:expr right=e_right:expr) 
	         {
		   // Check operands and get the type
		   Type the_type=typeManager.
		     getTypeLogicalBinaryOperator(##,compilerError,
						  left.getType(),right.getType(),
						  #e_left,#e_right);

		   // Not an object reference
		   etype=new EnrichedType(the_type);

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     //  Constant expression which can be evaluated here
		     if ((!left.isConstantZero()) || (!right.isConstantZero())) {
		       etype.setConstantIntegral(BigInteger.ONE);
		     }
		     else {
		       etype.setConstantIntegral(BigInteger.ZERO);
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }

		   // Annotate the node with the enriched type
		   ##.setDataType(etype);
		 }
        ;



logicalAndExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left,right;
	  etype=null;
	}
	:       #( LAND left=e_left:expr right=e_right:expr )
	         {
		   // Check operands and get the type
		   Type the_type=typeManager.
		     getTypeLogicalBinaryOperator(##,compilerError,
						  left.getType(),right.getType(),
						  e_left,e_right);

		   // Not an object reference
		   etype=new EnrichedType(the_type);

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     //  Constant expression which can be evaluated here
		     if ((!left.isConstantZero()) && (!right.isConstantZero())) {
		       etype.setConstantIntegral(BigInteger.ONE);
		     }
		     else {
		       etype.setConstantIntegral(BigInteger.ZERO);
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }

		   // Annotate the node with the enriched type
		   ##.setDataType(etype);

		 }
        ;


inclusiveOrExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left=null,right=null;
	  Type the_type=null;
	  etype=null;
	}
	:       #( BOR left=expr right=expr )
	 	   {
		     the_type=typeManager.getTypeIntegralBinaryOperator(##,compilerError,
									left.getType(),right.getType());

		     // Not an object reference
		     etype=new EnrichedType(the_type);

		     // Propagate constants
		     if (left.isConstantIntegral() && right.isConstantIntegral()) {
		       //  Constant expression which can be evaluated here
		       etype.setConstantIntegral(left.getConstantIntegralValue().or( 
						 right.getConstantIntegralValue()) );
		     }
		     
		     // Annotate the node with the enriched type
		     ##.setDataType(etype);
		   }
         ;


exclusiveOrExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left=null,right=null;
	  Type the_type=null;
	  etype=null;
	}
	:       #( BXOR left=expr right=expr )
	 	   {
		     the_type=typeManager.getTypeIntegralBinaryOperator(##,compilerError,
									left.getType(),right.getType());

		     // Not an object reference
		     etype=new EnrichedType(the_type);

		     // Propagate constants
		     if (left.isConstantIntegral() && right.isConstantIntegral()) {
		       //  Constant expression which can be evaluated here
		       etype.setConstantIntegral(left.getConstantIntegralValue().xor( 
						 right.getConstantIntegralValue() ) );
		     }

		     // Annotate the node with the enriched type
		     ##.setDataType(etype);
		   }
        ;


bitAndExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left=null,right=null;
	  Type the_type=null;
	  etype=null;
	}
	:       #( BAND left=expr right=expr )
	 	   {
		     the_type=typeManager.getTypeIntegralBinaryOperator(##,compilerError,
									left.getType(),right.getType());

		     // Not an object reference
		     etype=new EnrichedType(the_type);

		     // Propagate constants
		     if (left.isConstantIntegral() && right.isConstantIntegral()) {
		       //  Constant expression which can be evaluated here
		       etype.setConstantIntegral(left.getConstantIntegralValue().and( 
						 right.getConstantIntegralValue()) );
		     }

		     // Annotate the node with the enriched type
		     ##.setDataType(etype);
		   }
        ;



equalityExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left,right;
	  etype=null;
	}
	:
	(
	        #( EQUAL     left=e_left1:expr right=e_right1:expr)
	 	 {
		   // Check operands
		   Type t=typeManager.
		     getTypeRelationalOperator(##,compilerError,
					       left,right,
					       #e_left1,#e_right1);

		   // Resulting type is int
		   // Not an object reference
		   etype=new EnrichedType(t);	    

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     // Constant expression which can be evaluated here
		     Type the_type=typeManager.
		       getArithmeticCommonTypeNoQualifier(left.getType().unqualify(),
							  right.getType().unqualify());
		     if (the_type.isFloatingPointScalar()) {
		       // Comparison of at least one floating point
		       if (left.getConstantFloatingpointValue() ==
			   right.getConstantFloatingpointValue() ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		     else {
		       // Comparison of integrals
		       if (left.getConstantIntegralValue().compareTo(
			   right.getConstantIntegralValue())==0 ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }
		   
		   // Annotate the  node with the type
		   ##.setDataType(etype);
		 }

        |       #( NOT_EQUAL left=e_left2:expr right=e_right2:expr)
	 	 {
		   // Check operands
		   Type t=typeManager.
		     getTypeRelationalOperator(##,compilerError,
					       left,right,
					       #e_left2,#e_right2);

		   // Resulting type is int
		   // Not an object reference
		   etype=new EnrichedType(t);	    

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     // Constant expression which can be evaluated here
		     Type the_type=typeManager.
		       getArithmeticCommonTypeNoQualifier(left.getType().unqualify(),
							  right.getType().unqualify());
		     if (the_type.isFloatingPointScalar()) {
		       // Comparison of at least one floating point
		       if (left.getConstantFloatingpointValue() !=
			   right.getConstantFloatingpointValue() ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		     else {
		       // Comparison of integrals
		       if (left.getConstantIntegralValue().compareTo(
			   right.getConstantIntegralValue()) != 0 ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }
		   
		   // Annotate the  node with the type
		   ##.setDataType(etype);
		 }
	)
        ;


relationalExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left,right;
	  etype=null;
	}
	:
	(
	        #( LT  left=e_left1:expr right=e_right1:expr)
	 	 {
		   // Check operands
		   Type t=typeManager.
		     getTypeRelationalOperator(##,compilerError,
					       left,right,
					       #e_left1,#e_right1);

		   // Resulting type is int
		   // Not an object reference
		   etype=new EnrichedType(t);	    

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     // Constant expression which can be evaluated here
		     Type the_type=typeManager.
		       getArithmeticCommonTypeNoQualifier(left.getType().unqualify(),
							  right.getType().unqualify());
		     if (the_type.isFloatingPointScalar()) {
		       // Comparison of at least one floating point
		       if (left.getConstantFloatingpointValue() <
			   right.getConstantFloatingpointValue() ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		     else {
		       // Comparison of integrals
		       if (left.getConstantIntegralValue().compareTo(
			   right.getConstantIntegralValue()) < 0 ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }
		   
		   // Annotate the node with the enriched type
		   ##.setDataType(etype);
		 }

        |       #( LTE left=e_left2:expr right=e_right2:expr)
	 	 {
		   // Check operands
		   Type t=typeManager.
		     getTypeRelationalOperator(##,compilerError,
					       left,right,
					       #e_left2,#e_right2);

		   // Resulting type is int
		   // Not an object reference
		   etype=new EnrichedType(t);	    

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     // Constant expression which can be evaluated here
		     Type the_type=typeManager.
		       getArithmeticCommonTypeNoQualifier(left.getType().unqualify(),
							  right.getType().unqualify());
		     if (the_type.isFloatingPointScalar()) {
		       // Comparison of at least one floating point
		       if (left.getConstantFloatingpointValue() <=
			   right.getConstantFloatingpointValue() ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		     else {
		       // Comparison of integrals
		       if (left.getConstantIntegralValue().compareTo(
			   right.getConstantIntegralValue()) <= 0 ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }
		   
		   // Annotate the node with the enriched type
		   ##.setDataType(etype);
		 }

	 |      #( GT  left=e_left3:expr right=e_right3:expr)
	 	 {
		   // Check operands
		   Type t=typeManager.
		     getTypeRelationalOperator(##,compilerError,
					       left,right,
					       #e_left3,#e_right3);

		   // Resulting type is int
		   // Not an object reference
		   etype=new EnrichedType(t);	    

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     // Constant expression which can be evaluated here
		     Type the_type=typeManager.
		       getArithmeticCommonTypeNoQualifier(left.getType().unqualify(),
							  right.getType().unqualify());
		     if (the_type.isFloatingPointScalar()) {
		       // Comparison of at least one floating point
		       if (left.getConstantFloatingpointValue() >
			   right.getConstantFloatingpointValue() ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		     else {
		       // Comparison of integrals
		       if (left.getConstantIntegralValue().compareTo(
			   right.getConstantIntegralValue()) > 0 ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }
		   
		   // Annotate the node with the enriched type
		   ##.setDataType(etype);
		 }

	|       #( GTE left=e_left4:expr right=e_right4:expr)
	 	 {
		   // Check operands
		   Type t=typeManager.
		     getTypeRelationalOperator(##,compilerError,
					       left,right,
					       #e_left4,#e_right4);

		   // Resulting type is int
		   // Not an object reference
		   etype=new EnrichedType(t);	    

		   // Propagate constants
		   if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		     // Constant expression which can be evaluated here
		     Type the_type=typeManager.
		       getArithmeticCommonTypeNoQualifier(left.getType().unqualify(),
							  right.getType().unqualify());
		     if (the_type.isFloatingPointScalar()) {
		       // Comparison of at least one floating point
		       if (left.getConstantFloatingpointValue() >=
			   right.getConstantFloatingpointValue() ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		     else {
		       // Comparison of integrals
		       if (left.getConstantIntegralValue().compareTo(
			   right.getConstantIntegralValue()) >= 0 ) {
			 etype.setConstantIntegral(BigInteger.ONE);
		       }
		       else {
			 etype.setConstantIntegral(BigInteger.ZERO);
		       }
		     }
		   }
		   else if (left.isConstantScalar() && right.isConstantScalar()) {
		     // Constant expression but which can not be evaluated here
		     etype.setConstantLabel();
		   }
		   
		   // Annotate the node with the enriched type
		   ##.setDataType(etype);
		 }
	)
        ;


shiftExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left=null,right=null;
	  Type the_type=null;
	  etype=null;
	}
	:
	(
	 	#( LSHIFT left=expr right=expr)
	  	 {
		   the_type=typeManager
		     .getTypeShiftOperator(##,compilerError,
					   left.getType(),right.getType());

		   // Not an object reference
		   etype=new EnrichedType(the_type);
		   
		   // Propagate constants
		   if (left.isConstantIntegral() && right.isConstantIntegral()) {
		     //  Constant expression which can be evaluated here
		     etype.setConstantIntegral(left.getConstantIntegralValue().shiftLeft( 
					       right.getConstantIntegralValue().intValue()) );
		   }
	    
		   // Annotate the node with the enriched type
		   ##.setDataType(etype);
		 }

        |	#( RSHIFT left=expr right=expr)
	  	 {
		   the_type=typeManager
		     .getTypeShiftOperator(##,compilerError,
					   left.getType(),right.getType());

		   // Not an object reference
		   etype=new EnrichedType(the_type);
		   
		   // Propagate constants
		   if (left.isConstantIntegral() && right.isConstantIntegral()) {
		     //  Constant expression which can be evaluated here
		     etype.setConstantIntegral(left.getConstantIntegralValue().shiftRight(
					       right.getConstantIntegralValue().intValue()) );
		   }
	    
		   // Annotate the node with the enriched type
		   ##.setDataType(etype);
		 }
	)
        ;



additiveExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left=null,right=null;
	  Type the_type=null;
	  etype=null;
	}
	:
	(
	   #( PLUS  left=expr right=expr)
	    {
	      the_type=typeManager.
		getTypeAdditiveBinaryOperator(##,compilerError,
					      this,
					      left.getType(),right.getType(),
					      false);

	      // Not an object reference
	      etype=new EnrichedType(the_type);

	      // Propagate constants
	      if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		//  Constant expression which can be evaluated here
		if (the_type.isFloatingPointScalar()) {
		  // Floating point
		  etype.setConstantFloatingpoint(left.getConstantFloatingpointValue() +
						 right.getConstantFloatingpointValue() );
		}
		else {
		  // Integral
		  etype.setConstantIntegral(left.getConstantIntegralValue().add(
					    right.getConstantIntegralValue()) );
		}
	      }
	      else if (left.isConstantScalar() && right.isConstantScalar()) {
		// Constant expression but which can not be evaluated here
		etype.setConstantLabel();
	      }

	      // Annotate the node with the enriched type
	      ##.setDataType(etype);
	    }

         | #( MINUS left=expr right=expr)
	    {
	      the_type=typeManager.
		getTypeAdditiveBinaryOperator(##,compilerError,
					      this,
					      left.getType(),right.getType(),
					      true);

	      // Not an object reference
	      etype=new EnrichedType(the_type);

	      // Propagate constants
	      if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		//  Constant expression which can be evaluated here
		if (the_type.isFloatingPointScalar()) {
		  // Floating point
		  etype.setConstantFloatingpoint(left.getConstantFloatingpointValue() -
						 right.getConstantFloatingpointValue() );
		}
		else {
		  // Integral
		  etype.setConstantIntegral(left.getConstantIntegralValue().subtract(
					    right.getConstantIntegralValue()) );
		}
	      }
	      else if (left.isConstantScalar() && right.isConstantScalar()) {
		// Constant expression but which can not be evaluated here
		etype.setConstantLabel();
	      }

	      // Annotate the node with the enriched type
	      ##.setDataType(etype);
	    }
	)

        ;


multExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left=null,right=null;
	  Type the_type=null;
	  etype=null;
	}
	:
	(
		#( STAR left=expr right=expr)
	 	   {
		     the_type=typeManager.
		       getTypeArithmeticBinaryOperator(##,compilerError,
						       left.getType(),right.getType());

		     // Not an object reference
		     etype=new EnrichedType(the_type);

		     // Propagate constants
		     if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		       //  Constant expression which can be evaluated here
		       if (the_type.isFloatingPointScalar()) {
			 // Floating point
			 etype.setConstantFloatingpoint(left.getConstantFloatingpointValue() *
							right.getConstantFloatingpointValue() );
		       }
		       else {
			 // Integral
			 etype.setConstantIntegral(left.getConstantIntegralValue().multiply(
						   right.getConstantIntegralValue()) );
		       }
		     }
		     else if (left.isConstantScalar() && right.isConstantScalar()) {
		       // Constant expression but which can not be evaluated here
		       etype.setConstantLabel();
		     }

		     ##.setDataType(etype);
		   }

        |       #( DIV  left=expr right=expr)
	 	   {
		     the_type=typeManager.
		       getTypeArithmeticBinaryOperator(##,compilerError,
						       left.getType(),right.getType());

		     // Not an object reference
		     etype=new EnrichedType(the_type);

		     // Propagate constants
		     if (left.isConstantArithmetic() && right.isConstantArithmetic()) {
		       //  Constant expression which can be evaluated here
		       if (the_type.isFloatingPointScalar()) {
			 // Floating point
			 etype.setConstantFloatingpoint(left.getConstantFloatingpointValue() /
							right.getConstantFloatingpointValue() );
		       }
		       else {
			 // Integral
			 etype.setConstantIntegral(left.getConstantIntegralValue().divide(
						   right.getConstantIntegralValue()) );
		       }
		     }
		     else if (left.isConstantScalar() && right.isConstantScalar()) {
		       // Constant expression but which can not be evaluated here
		       etype.setConstantLabel();
		     }

		     // Annotate the node with the enriched type
		     ##.setDataType(etype);
		   }
        |       #( MOD  left=expr right=expr)
	 	   {
		     the_type=typeManager.
		       getTypeIntegralBinaryOperator(##,compilerError,
						     left.getType(),right.getType());

		     // Not an object reference
		     etype=new EnrichedType(the_type);

		     // Propagate constants
		     if (left.isConstantIntegral() && right.isConstantIntegral()) {
		       //  Constant expression which can be evaluated here
		       etype.setConstantIntegral(left.getConstantIntegralValue().mod( 
						 right.getConstantIntegralValue()) );
		     }
	    
		     // Annotate the node with the enriched type
		     ##.setDataType(etype);
		   }
	)
        ;



castExpr
returns [EnrichedType etype] 
	{
	  Type t=null;
	  EnrichedType t_expr=null;
	  etype=null;
	}
	: #( NCast t=typeName_expr RPAREN 
	     t_expr=expr
	     {
	       // Check correct cast operands
	       typeManager.checkCastOperands(##,compilerError,
					     t,t_expr.getType());
	       
	       etype=new EnrichedType(t);
	       
	       // propagate object reference information
	       if (!t_expr.getType().isVoid()) {
		 // Transfer object properties
		 if (t_expr.designateAnObject()) {
		   etype.setObjectDesignation();
		 }
	       }
	       
	       // Propagate constants
	       if (t.isFloatingPointScalar()) {
		 if (t_expr.isConstantArithmetic()) {
		   etype.setConstantFloatingpoint(
			 t_expr.getConstantFloatingpointValue());
		 }
	       }
	       else if (t.isIntegralScalar()) {
		 if (t_expr.isConstantArithmetic()) {
		   etype.setConstantIntegral(
			 t_expr.getConstantIntegralValue());
		 }
	       }
	       else if (t.isPointerOrLabel()) {
		 if (t_expr.isConstantScalar()) {
		   etype.setConstantLabel();
		 }
	       }
	       
	       // Annotate the NCast node with the type
	       ##.setDataType(etype);
	     }
	   )
        ;


unaryExpr
returns [EnrichedType etype] 
	{
	  Type t=null;
	  EnrichedType t_expr=null;
	  etype=null;
	}
        :       #( NPreInc t_expr=expr )
		{
		  t=t_expr.getType();

		  // Check for scalar
		  if (!(t.isScalarOrLabel()||t.isVector())) {
		    FatalError(##,"wrong type argument to pre-increment");
		  }

		  // Check for correct Lvalue
		  typeManager.checkModifiableLvalue(##,compilerError,
						    t_expr,"pre-increment");

		  // Must point to a size known type
		  if (t.isPointer()) {
		    // Check that the pointed type has a known size
		    if (t.getPointedType().isIncomplete()) {
		      Error(##,"pre-increment of pointer to incomplete type");
		    }
		  }
		  // No array which is not a modifiable lvalue

		  // Propagates the type and constant value
		  etype=new EnrichedType(t_expr);

		  // Not an object reference
		  etype.setNonObjectDesignation();

		  // Annotate the INC node with the type
		  ##.setDataType(etype);
		}

        |       #( NPreDec t_expr=expr )
 		{
		  t=t_expr.getType();

		  // Check for scalar
		  if (!(t.isScalarOrLabel()||t.isVector())) {
		    FatalError(##,"wrong type argument to pre-decrement");
		  }

		  // Check for correct Lvalue
		  typeManager.checkModifiableLvalue(##,compilerError,
						    t_expr,"pre-decrement");

		  // Must point to a size known type
		  if (t.isPointer()) {
		    // Check that the pointerd type has a known size
		    if (t.getPointedType().isIncomplete()) {
		      Error(##,"pre-decrement of pointer to incomplete type");
		    }
		  }
		  // No array which is not a modifiable lvalue
		  
		  // Propagates the type and constant value
		  etype=new EnrichedType(t_expr);

		  // Not an object reference
		  etype.setNonObjectDesignation();

		  // Annotate the DEC node with the type
		  ##.setDataType(etype);
		}

	|       #( NAddress t_expr=expr) // '&'
	    	{
		  // Should be a lvalue (expression which designates and object)
		  // or a function designator
		  Type type_expr=t_expr.getType();

		  if (type_expr.isFunction()) {
		    // Nothing to do:
		    // the address of a function is also a function
		  }
		  else {
		    typeManager.checkLvalue(##,compilerError,
					    t_expr,"'&'");
		    // It must not be a bitfield
		    if (type_expr.isBitfield()) {
		      Error(##,"cannot take address of a bit-field");
		    }
		    // It must not be a vector reference
		    // [To be clarified with the OpenCL working-group]
		    if (t_expr.isVectorElementReference()) {
		      Error(##,"cannot take address of a sub-vector reference");
		    }
		  }
		  
		  // Determine the new type
		  Type the_type;
		  if (type_expr.isFunction()) {
		    // the address of a function is also a function
		    the_type=type_expr;
		  }
		  else if (type_expr.isArray()) {
		    the_type=new Pointer(((Array)type_expr.unqualify()).getElementType());
		  }
		  else {
		    the_type=new Pointer(type_expr);
		  }
		  
		  // Propagates the type
		  // Not an object reference
		  etype=new EnrichedType(the_type);
		  
		  // The address can be compile time known
		  // Concerns only objects (variable/array label) and functions labels
		  if (t_expr.designateCompileTimeAllocatedObject()) {
		      etype.setConstantLabel();
		  }
		  
		  // Annotate the NUnaryExpr node with the type
		  ##.setDataType(etype);
		}

        |       #( NDereference t_expr=expr) // '*'
	    	{
		  Type the_type=t_expr.getType();
		  if (the_type.isPointer()) {
		    the_type=the_type.getPointedType();
		  }
		  else if (the_type.isArray()) {
		    the_type=the_type.getElementType();
		  }
		  else {
		    FatalError(##,"invalid type argument of `unary *'");
		  }
		  
		  // Check that the array element has a known size
		  if (the_type.isIncomplete()) {
		    FatalError(##,"dereferencing pointer to incomplete type");
		  }
		  else if (the_type.isVoid()) {
		    Error(##,"dereferencing `void *' pointer");
		  }
		  
		  // Propagates the type
		  etype=new EnrichedType(the_type);

		  // Manage the object reference property
		  if (the_type.isFunction()){
		    // Nothing to do, a function is not an ojject
		  }
		  else if (the_type.isArray()) {
		    // Array is (strangely) at the same time an address and
		    // a reference to an object
		    if (t_expr.isConstantScalar()) {
		      etype.setCompileTimeAllocatedObjectDesignation();
		      etype.setConstantLabel();
		    }
		    else {
		      etype.setObjectDesignation();
		    }
		  }
		  else {
		    // All other types designate an object
		    if (t_expr.isConstantScalar()) {
		      etype.setCompileTimeAllocatedObjectDesignation();
		    }
		    else {
		      etype.setObjectDesignation();
		    }
		  }
		  
		  // Annotate the NUnaryExpr node with the type
		  ##.setDataType(etype);
		}

	|       #( NUnaryPlus t_expr=expr) // '+'
	    	{
		  // Check for arithmetic
		  if (t_expr.getType().isArithmeticScalar()) {
		    // Promotion is performed
		    Type the_type=t_expr.getType().promote();
		    
		    // Propagates the type
		    // Not an object reference
		    etype=new EnrichedType(the_type);
		    
		    // Propagate constants
		    if (t_expr.isConstantFloatingpoint()) {
		      etype.setConstantFloatingpoint(t_expr.getConstantFloatingpointValue());
		    }
		    else if (t_expr.isConstantIntegral()) {
		      etype.setConstantIntegral(t_expr.getConstantIntegralValue());
		    }
		  }
		  else if (t_expr.getType().isVector()) {
			    // Propagates the type
		    // Not an object reference
		    etype=new EnrichedType(t_expr.getType());
		    
		    // [TBW] Propagate constants
		  }
		  else {
		    FatalError(##,"wrong type argument to unary plus");
		  }

		  // Annotate the NUnaryExpr node with the type
		  ##.setDataType(etype);
		}

	|      #( NUnaryMinus t_expr=expr) // '-'
	       {
		 // Check for arithmetic
		 if (t_expr.getType().isArithmeticScalar()) {
		   // Promotion is performed
		   Type the_type=t_expr.getType().promote();
		   
		   // Propagates the type
		   // Not an object reference
		   etype=new EnrichedType(the_type);
		   
		   // Propagate constants
		   if (t_expr.isConstantFloatingpoint()) {
		     etype.setConstantFloatingpoint(-t_expr.getConstantFloatingpointValue());
		   }
		   else if (t_expr.isConstantIntegral()) {
		     etype.setConstantIntegral(t_expr.getConstantIntegralValue().negate());
		   }
		 }
		 else if (t_expr.getType().isVector()) {
		   // Propagates the type
		   // Not an object reference
		   etype=new EnrichedType(t_expr.getType());
		   
		   // [TBW] Propagate constants
		 }
		 else {
		   FatalError(##,"wrong type argument to unary minus");
		 }

		 // Annotate the NUnaryExpr node with the type
		 ##.setDataType(etype);
	       }

	|     #( BNOT t_expr=expr) // '~'
	      {
		// Check for integral
		if (t_expr.getType().isIntegralScalar()) {
		  // Promotion is performed
		  Type the_type=t_expr.getType().promote();
		  
		  // Propagates the type
		  // Not an object reference
		  etype=new EnrichedType(the_type);
		  
		  // Propagate constants
		  if (t_expr.isConstantIntegral()) {
		    etype.setConstantIntegral(t_expr.getConstantIntegralValue().not());
		  }
		}
		else if (t_expr.getType().isIntegralVector()) {
		  // Propagates the type
		  // Not an object reference
		  etype=new EnrichedType(t_expr.getType());
		  
		  // [TBW] Propagate constants
		}
		else {
		  FatalError(##,"wrong type argument to bit-complement");
		}
		
		// Annotate the NUnaryExpr node with the type
		##.setDataType(etype);
	      }

	|     #( LNOT t_expr=expr) // '!'
	      {
		// Get the type of the logical operator
		Type the_type=typeManager.getTypeLogicalUnaryOperator(##,compilerError,
								      t_expr.getType(),"exclamation mark");
		
		// Not an object reference
		etype=new EnrichedType(the_type);
		
		// Propagate constants
		if (t_expr.isConstantArithmetic()) {
		  if (t_expr.isConstantZero()) {
		    etype.setConstantIntegral(BigInteger.ONE);
		  }
		  else {
		    etype.setConstantIntegral(BigInteger.ZERO);
		  }
		}
		
		// Annotate the NUnaryExpr node with the type
		##.setDataType(etype);
	      }

	|     #( NLabelAsValue id_ref:ID) // "&&"
		{
		  // Lookup in the symbol table and set 'id_ref'
		  Symbol s=lookupCodeLabelAndSetReference(#id_ref);
	       
		  if (s==null) {
		    // No symbol yet defined. Since he forward reference is allowed for
		    // labels, create a symbol
		 
		    // Create a new symbol
		    String declName = #id_ref.getText();
		    CodeLabel symbol = new CodeLabel(declName);
		 
		    // It shall be a forward reference to a label that is
		    // defined later
		    symbol.setReference();
		 
		    // Sets the 'void *' type
		    symbol.setType(new Pointer(Void.Tvoid));
		 
		    // Link to AST
		    symbol.setIdNode((NodeAST)#id_ref);
		 
		    // Add the symbol to the symbol table
		    addCodeLabel(declName,symbol);
		 
		    // Link AST -> symbol
		    ((NodeAST)#id_ref).setReference(symbol);
		  }

		  // Label as value: as the type 'void *' and must be a label
		  etype=new EnrichedType(new Pointer(Void.Tvoid));
		  etype.setConstantLabel();

		  // Annotate the node with the type
		  ##.setDataType(etype);
	}

	// Not C89 [TBW later]
	|     #( "__real" t_expr=expr) 
		{
		  // [TBW]
		  compilerError.raiseFatalError(##, "__real not supported");
		}
	|     #( "__imag" t_expr=expr) 
		{
		  // [TBW]
		  compilerError.raiseFatalError(##, "__imag not supported");
		}
	// Target specific
        |     #( so:"sizeof"
		   ( ( LPAREN typeName_expr )=> lp:LPAREN t=typeName_expr RPAREN
		     				  { #lp.setDataType(new EnrichedType(t)); }
		     | t_expr=expr {t=t_expr.getType();}
                    )
	       )
		{
		  // Type is size_t, which is platform dependent
		  Type the_type=getType_size_t();

		  // Propagates the type
		  // Not an object reference
		  etype=new EnrichedType(the_type);

		  // Compute 'sizeof' value
		  if (t.isIncomplete()) {
		    Error(so,"sizeof applied to an incomplete type");
		    etype.setConstantIntegral(BigInteger.ZERO);
		  }
		  else if (t.isBitfield()) {
		    Error(so,"sizeof applied to a bit-field");
		    etype.setConstantIntegral(BigInteger.valueOf(t.sizeof()));
		  }
		  else {
		    etype.setConstantIntegral(BigInteger.valueOf(t.sizeof()));
		  }
		     
		  // Annotate the 'sizeof' node with the type
		  ##.setDataType(etype);
		}

	        // Target specific
	|      #( ao:"__alignof"
                    ( ( LPAREN t=typeName_expr )=> lp2:LPAREN t=typeName_expr RPAREN
		     				  { #lp2.setDataType(new EnrichedType(t)); }
		      | t_expr=expr  {t=t_expr.getType();}
                    )
                 )
		 {
		   // Type is size_t, which is platform dependent
		   Type the_type=getType_size_t();
		   
		   // Propagates the type
		   // Not an object reference
		   etype=new EnrichedType(the_type);
		   
		   // Compute 'alignof' value
		   if (t.isIncomplete()) {
		     Error(ao,"__alignof applied to an incomplete type");
		     etype.setConstantIntegral(BigInteger.ONE);
		   }
		   else if (t.isBitfield()) {
		     Error(ao,"__alignof applied to a bit-field");
		     etype.setConstantIntegral(BigInteger.valueOf(t.sizeof()));
		   }
		   else {
		     etype.setConstantIntegral(BigInteger.valueOf(t.alignof()));
		   }
		   
		   // Annotate the 'alignof' node with the type
		   ##.setDataType(etype);
		 }
	|      !#( vo:"__vec_step"  // OpenCL builtin
                    ( ( LPAREN t=typeName_expr )=> LPAREN t=typeName_expr RPAREN
		      | t_expr=expr  {t=t_expr.getType();}
                    )
                 )
		 {
		   // Not an object reference, type is 'int'
		   etype=new EnrichedType(IntegerScalar.Tsint);
		   
		   // Compute 'vec_step' value
		   int value=0;
		   if (t.isIntegerScalar() || t.isFloatingPointScalar()) {
		     ##= #[IntegralNumber,"1"];
		     etype.setConstantIntegral(BigInteger.ONE);
		   }
		   else if (t.isVector()) {
		     int size=0;
		     switch (t.getNbVectorElements()) {
		     case 2:
		       ##= #[IntegralNumber,"2"];
		       size=2;
		       break;
		     case 3:
		       ##= #[IntegralNumber,"4"];
		       size=4;
		       break;
		     case 4:
		       ##= #[IntegralNumber,"4"];
		       size=4;
		       break;
		     case 8:
		       ##= #[IntegralNumber,"8"];
		       size=8;
		       break;
		     case 16:
		       ##= #[IntegralNumber,"16"];
		       size=16;
		       break;
		     }
		     etype.setConstantIntegral(BigInteger.valueOf(size));
		   }
		   else {
		     Error(vo,"'vec_step' builtin to a non arithmetic scalar or vector type");
		     ##= #[IntegralNumber,"0"];
		   }
		   
		   // Annotate the 'vec_step' node with the type
		   ##.setDataType(etype);
		 }
        ;



commaExpr
returns [EnrichedType etype] 
	{
	  EnrichedType left, right=null;
	  etype=null;
	}
	:   #(NCommaExpr left=expr right=expr)
	      {
		// Propagates the type and constant value
		etype=new EnrichedType(right);
		
		// Not an object reference
		etype.setNonObjectDesignation();

		// Annotate the node with the type
		##.setDataType(etype);
	      }
        ;


emptyExpr
returns [EnrichedType etype] 
	{
	  // It has the 'void' type
	  etype=new EnrichedType(Void.Tvoid);
	}
	:   NEmptyExpression
	      {
		// Annotate the emptyExpr node with the type
		##.setDataType(etype);
	      }
         ;




// -- GNU extension 
// A compound statement enclosed in parentheses may appear as an expression in GNU C.
// This allows you to use loops, switches, and local variables within an expression.
// The last thing in the compound statement should be an expression followed by a semicolon;
// the value of this subexpression serves as the value of the entire construct.
// See http://gcc.gnu.org/onlinedocs/gcc/Statement-Exprs.html
compoundStatementExpr
returns [EnrichedType etype] 
	{
	  EnrichedType compound_etype;
	  etype=null;
	}
        :   #(LPAREN compound_etype=compoundStatement[false] RPAREN)
	      {
		// Propagates the type and constant value
		etype=new EnrichedType(compound_etype);
		
		// Not an object reference
		etype.setNonObjectDesignation();

		// Annotate the emptyExpr node with the type
		##.setDataType(etype);
	      }
        ;


// -- GNU/C99 extension for 'case' statements and array initializer
rangeExpr[String s]
returns [Range range] 
	{
	  EnrichedType t_inf,t_sup;
	  range=null;
	}
        :   #(NRangeExpr t_inf=constExpr VARARGS t_sup=constExpr)
	{
	  //  Operands should be integral
	  int inf,sup;
	  if ((!t_inf.getType().isIntegralScalar())||(!t_sup.getType().isIntegralScalar())) {
	    Error(##,"non integral value for range in "+s);
	    // Dummy range
	    inf=0;sup=0;
	  }
	  else if ((!t_inf.isConstantIntegral())||(!t_sup.isConstantIntegral())) {
	    Error(##,"non constant value for range in "+s);
	    // Dummy range
	    inf=0;sup=0;
	  }
	  else {
	    inf=t_inf.getConstantIntegralValue().intValue();
	    sup=t_sup.getConstantIntegralValue().intValue(); 
	  }
	  if (inf>sup) {
	    Warning(##,"empty range specified in "+s);
	  }
	  range=new Range(inf,sup);

	  // Set the type has integer
	  // Not an object reference
	  EnrichedType etype=new EnrichedType(IntegerScalar.Tsint);
	  // Annotate the node with the type
	  ##.setDataType(etype);
	}
        ;


// -- GNU extension
// [TBW]
gnuAsmExpr
returns [EnrichedType etype] 
	{
	  // It has the 'void' type
	  etype=new EnrichedType(Void.Tvoid);
	  String s;
	}
        :   #(NGnuAsmExpr
                ("volatile")? 
                LPAREN s=stringConst
                ( options { warnWhenFollowAmbig = false; }:
                  COLON (strOptExprPair ( COMMA strOptExprPair)* )?
                  ( options { warnWhenFollowAmbig = false; }:
                    COLON (strOptExprPair ( COMMA strOptExprPair)* )?
                  )?
                )?
                ( COLON s=stringConst ( COMMA s=stringConst)* )?
                RPAREN
            )
        ;

strOptExprPair
	{
	  EnrichedType null_expr;
	  String s;
	}
        :  s=stringConst ( LPAREN null_expr=expr RPAREN )?
        ;




exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }


argExprList[Function fp]
	{
	  EnrichedType etype;
	}
        :       (
		  etype=expr
	          {
		    Type the_type=etype.getType();
		    
		    // Specific processing for OpenCL
		    if (oclLanguage) {
		      if (the_type.isQualified()) {
			Qualifier q=the_type.getQualifier();
			if (q.getAddressSpace()==AddressSpace.NO) {
			  // By default, it is private
			  q.setAddressSpace(AddressSpace.PRIVATE);
			}
		      }
		      else {
			// By default, the type is considered in the private address space
			the_type=new Qualifier(AddressSpace.PRIVATE,the_type);
		      }
		    }

		    // Put the parameter type to the function type
		    fp.addParameter(the_type);
		  }
		)+
        ;



protected
charConst
        :       CharLiteral
        ;


protected
stringConst
returns [String string]
{
  string="\"\"";
}
        :       #(NStringSeq 
		    (
		     sl:StringLiteral
		      {
			string=string.substring(0,string.length()-1) +
			       sl.getText().substring(1);
		      }
		     )+
		  )
        ;

