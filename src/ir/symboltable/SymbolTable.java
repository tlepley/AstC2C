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

/* Symbol table */


package ir.symboltable;

import ir.symboltable.symbols.*;

import common.CompilerError;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;


public class SymbolTable {

  //------------------------------
  // Private data
  //------------------------------

  // Scope management
  private Vector<String> scopeStack;
  private int unnamedScopeCounter = 0;
  private int scope_depth         = 0;

  // Ordinary name space
  //--------------------
  private Map<String,Symbol> symTable;

  // Tag name space
  //---------------
  private Map<String,TagSymbol> symTableTag;

  // Code label name space
  //----------------------
  private Map<String,CodeLabel> symTableCodeLabel;
  private Vector<String> scopeStackCodeLabel;
  private int scope_depth_label = 0; // Specific scope for labels
  private LinkedList<CodeLabel> referenceCodeLabelList;


  // Global table where overriden symbols are put
  private LinkedList<Symbol> flushed_list;



  //==================================================================
  // Constructor
  //==================================================================
  public SymbolTable()  {
    scopeStackCodeLabel    = new Vector<String>(10);
    scopeStack             = new Vector<String>(10);
    symTableTag            = new LinkedHashMap<String,TagSymbol>(200);
    symTableCodeLabel      = new LinkedHashMap<String,CodeLabel>(50);
    symTable               = new LinkedHashMap<String,Symbol>(200);
    referenceCodeLabelList = new LinkedList<CodeLabel>();
    flushed_list           = new LinkedList<Symbol>(); 
  }


  //==================================================================
  // Mangling management
  //==================================================================

  // ******************************************************************
  // Mangling counter management 
  // ******************************************************************
  private int mangling_counter  = 0;

  public int getManglingCounter() {
    return(mangling_counter);
  }
  public void setManglingCounter(int m) {
    mangling_counter=m;
  }

  // ******************************************************************
  // getMangling :
  //
  // Returns the mangled version of 's', from a counter which is
  // incremented. The resulting name is unique
  // ******************************************************************
  public String getMangling(String s) {
    mangling_counter++;
    StringBuffer str = new StringBuffer("_M" + mangling_counter + "_" + s);
    return(str.toString());
  }

  // ******************************************************************
  // getSameMangling :
  //
  // Returns the mangled version of 's', from a counter which is not
  // incremented
  // ******************************************************************
  public String getSameMangling(String s) {
    StringBuffer str = new StringBuffer("_M" + mangling_counter + "_" + s);
    return(str.toString());
  }


  // ******************************************************************
  // getMangling :
  //
  // Returns a new symbol name from a counter which is incremented.
  // The resulting name is unique
  // ******************************************************************
  public String getNewName() {
    mangling_counter++;
    StringBuffer str = new StringBuffer("_N" + mangling_counter);
    return(str.toString());
  }



  //==================================================================
  // Scope management
  //==================================================================


  //------------------------------------------------------------------
  //  isTopLevel 
  //
  //  Returns true is the current global scope is the top level one
  //
  //------------------------------------------------------------------
  public boolean isTopLevel() {
    return scope_depth==0;
  }

  // Returns the current scope depth
  public int getCurrentScopeDepth() {
    return(scope_depth);
  }


  //------------------------------------------------------------------
  // Push scope
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  //  PushScope 
  //
  //  Adds one level of the global scope (named scope)
  //
  //------------------------------------------------------------------
  public void pushScope(String s) {
    //System.out.println("push scope:" + s);
    scope_depth++;
    scopeStack.addElement(s);
  }

  //------------------------------------------------------------------
  //  pushScope 
  //
  //  Adds one level of the global scope (unnamed scope)
  //
  //------------------------------------------------------------------
  public void pushScope() {
    //System.out.println("push scope:" + s);
    scope_depth++;
    scopeStack.addElement("" + unnamedScopeCounter++);
  }

  //------------------------------------------------------------------
  //  pushCodeLabelScope 
  //
  //  Adds one level of label scope
  //
  //------------------------------------------------------------------
  private void pushCodeLabelScope(String s, CompilerError ce) {
    scope_depth_label++;
    scopeStackCodeLabel.addElement(s);
    if (referenceCodeLabelList.size()!=0) {
      ce.raiseInternalError("SymbolTable::pushCodeLabelScope");
    }
  }


  //------------------------------------------------------------------
  //  pushFunctionDefScope 
  //
  //  Adds one level of scope at the beginning of a function definition.
  //  It adds one level of the global scope and of the label scope
  //
  //------------------------------------------------------------------
  public void pushFunctionDefScope(String s, CompilerError ce) {
    //System.out.println("push function def scope:" + s);
    pushScope(s);
    pushCodeLabelScope(s,ce);
  }



  //------------------------------------------------------------------
  // Pop scope
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  //  popScope 
  //
  //  Removes one level of the global scope
  //
  //------------------------------------------------------------------
  public void popScope() {
    int size = scopeStack.size();
    if(size>0) {
      scopeStack.removeElementAt(size-1);
    }
    scope_depth--;
  }


  //------------------------------------------------------------------
  //   popCodeLabelScope
  //
  //  Removes one level of label scope
  //
  //------------------------------------------------------------------
  private void popCodeLabelScope(CompilerError ce) {
    int size = scopeStackCodeLabel.size();
    if(size>0) {
      scopeStackCodeLabel.removeElementAt(size-1);
    }
    scope_depth_label--;

    // Check that all referenced labels have been defined
    for(CodeLabel symb:referenceCodeLabelList) {
      if (!symb.isDefinition()) {
	ce.raiseError(symb.getIdNode(), "Label '"+ symb.getName() + "' used but not defined");
      }
    }
    referenceCodeLabelList.clear();
  }

  //------------------------------------------------------------------
  //  popFunctionDefScope 
  //
  //  Removes one level of scope at the end of a function definition.
  //  It removes one level of the global scope and of the label scope
  //
  //------------------------------------------------------------------
  public void popFunctionDefScope(CompilerError ce) {
    popScope();
    popCodeLabelScope(ce);
  }


  //------------------------------------------------------------------
  //  currentScopeAsString 
  //
  //  Returns the current global scope as a string (identifiers
  //  separated by ':')
  //
  //------------------------------------------------------------------
  public String currentScopeAsString() {
    StringBuffer buf = new StringBuffer(100);
    boolean first    = true;
    Enumeration<String> e = scopeStack.elements();
    
    while(e.hasMoreElements()) {
      if(first) {
	first = false;
      }
      else {
	buf.append(":");
      }
      buf.append(e.nextElement());
    }
    return(buf.toString());
  }


  //------------------------------------------------------------------
  //  currentCodeLabelScopeAsString 
  //
  //  Returns the current label scope as a string (identifiers
  //  separated by ':')
  //
  //------------------------------------------------------------------
  public String currentCodeLabelScopeAsString() {
    StringBuffer buf = new StringBuffer(100);
    boolean first    = true;
    Enumeration<String> e = scopeStackCodeLabel.elements();
    
    while(e.hasMoreElements()) {
      if(first) {
	first = false;
      }
      else {
	buf.append(":");
      }
      buf.append(e.nextElement());
    }
    return(buf.toString());
  }



  //==================================================================
  // Symbol table building
  //==================================================================

  //------------------------------------------------------------------
  //  flush 
  //
  //  Flush the symbol
  //
  //------------------------------------------------------------------
  private void flush(Symbol symbol) {
    flushed_list.add(symbol);
  }


  //------------------------------------------------------------------
  // Ordinary symbol
  //------------------------------------------------------------------
 
  private void raiseBuiltinRedefinitionError(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // Redefinition
    ce.raiseFatalError(new_symbol.getIdNode(),"can not redefine '"+name+"', which is a compiler builtin function");
  }

  private void raiseRedefinitionError(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // Redefinition
    ce.raiseError(new_symbol.getIdNode(),"redefinition of '"+name+"'");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration of '"+name+"' was here");
  }

  private void raiseDifferentSymbolError(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseError(new_symbol.getIdNode(),"'" + name +
		  "' redeclared as different kind of symbol");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration of '"+name+"' was here");
  }

  private void raiseConflictingTypeError(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseError(new_symbol.getIdNode(),"conflicting type for '"+name+"'");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration of '"+name+"' was here");
  }

  private void raiseConflictingDeclarationError(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseError(new_symbol.getIdNode(),"conflicting declaration for '"+name+"'");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration of '"+name+"' was here");
  }

  private void raiseFunctionRedefinitionWarning(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseWarning(new_symbol.getIdNode(),"redefinition of function '"+name+"' (no error forced by option)");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration of '"+name+"' was here");
  }

  private void raiseFunctionRedefinitionError(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseError(new_symbol.getIdNode(),"redefinition of function '"+name+"'");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration of '"+name+"' was here");
  }

  private void raiseRedeclarationError(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseError(new_symbol.getIdNode(),"redeclaration of '"+name+"'");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration of '"+name+"' was here");
  }

  private void raiseManglingError1(String name, Symbol new_symbol, Symbol symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseError(new_symbol.getIdNode(),"Mixing mangled and unmangled function '"+name+"'");
    ce.raiseMessage(symbol_in_table.getIdNode(),"previous declaration (not mangled) of '"+name+"' was here");
  }
  private void raiseManglingError2(String name, Symbol new_symbol, MangledFunctionPseudoLabel symbol_in_table, CompilerError ce) {
    // different kind of symbol
    ce.raiseError(new_symbol.getIdNode(),"Mixing mangled and unmangled function '"+name+"'");
    ce.raiseMessage(symbol_in_table.getManglingList().get(0).getIdNode(),"previous declaration (mangled) of '"+name+"' was here");
  }


  //------------------------------------------------------------------
  //  add 
  //
  //  Add a new ordinary symbol definition related to the current scope
  //  The current function performs compatibility checks and raises
  //  potential errors
  //
  //------------------------------------------------------------------
  public void addObjectLabel(String name, ObjectLabel new_symbol, CompilerError ce) {
    new_symbol.setScopeDepth(scope_depth);
    String scopedName=addCurrentScopeToName(name);
    Symbol symbol_in_table=symTable.get(scopedName);

    if (symbol_in_table!=null) {
      // A symbol with this name already exists

      if (!(symbol_in_table instanceof ObjectLabel)) {
	// Never compatible since different symbol
	raiseDifferentSymbolError(name,new_symbol,symbol_in_table,ce);
      }
      else {
	if (isTopLevel()) {
	  // Check for storage class
	  if (!new_symbol.isExtern()) {
	    // Storage class must be the same
	    if (symbol_in_table.isStatic() && (!new_symbol.isStatic()) ) {
	      raiseConflictingDeclarationError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	    if (symbol_in_table.isRegister() && (!new_symbol.isRegister()) ) {
	      raiseConflictingDeclarationError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	    if (symbol_in_table.isAuto() && (!new_symbol.isAuto()) ) {
	      raiseConflictingDeclarationError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	  }
	  if (!symbol_in_table.isExtern()) {
	    // Storage class must be the same
	    if (new_symbol.isStatic() && (!symbol_in_table.isStatic()) ) {
	      raiseConflictingDeclarationError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	    if (new_symbol.isRegister() && (!symbol_in_table.isRegister()) ) {
	      raiseConflictingDeclarationError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	    if (new_symbol.isAuto() && (!symbol_in_table.isAuto()) ) {
	      raiseConflictingDeclarationError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	  }
	  
	  // Should check for same type
	  if (!symbol_in_table.getType().isEquivalentForVariableAndArrayDeclaration(new_symbol.getType())) {
	    raiseConflictingTypeError(name,new_symbol,symbol_in_table,ce);
	    return;
	  }
	}
	else {
	  // Declaration in a scope
	  if ((!symbol_in_table.isExtern())&&(!new_symbol.isExtern())) {
	    if (symbol_in_table.getType().isEquivalentForVariableAndArrayDeclaration(new_symbol.getType())) {
	      raiseRedeclarationError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	    else {
	      raiseConflictingTypeError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	  }
	  else {
	    // Should check for same type
	    if (!symbol_in_table.getType().isEquivalentForVariableAndArrayDeclaration(new_symbol.getType())) {
	      raiseConflictingTypeError(name,new_symbol,symbol_in_table,ce);
	      return;
	    }
	  }
	}

	// We must flush the possible 'extern' declaration if a declaration
	// exists in the same scope
	// -> In the current version, the extern is considered as referencing a
	//    global variable (which is potentially false), but in C90, no
	//    reference will point to the flushed extern declaration, because
	//    all declaration are at the beginning of a scope, so that there is
	//    not problem
	// -> Must be reworked for supporting C99 
	//    (ex: int a; a++; extern int a; a=0;)
	if (symbol_in_table.isExtern()) {
	  if (new_symbol.isExtern()) {
	    flush(new_symbol);
	  }
	  else {
	    flush(symbol_in_table);
	    symTable.put(scopedName,new_symbol);
	  }
	}
	// For array tentative definitions 
	else if(!symbol_in_table.getType().isComplete() && new_symbol.getType().isComplete()){
	    flush(symbol_in_table);
	    symTable.put(scopedName,new_symbol);
	}
	else {
	  flush(new_symbol);
	}

	// Manage brother relationship between kept symbol and flushed one
	Vector<Symbol> v=symbol_in_table.getBrothers();
	if (v==null) {
	  v=new Vector<Symbol>();
	  v.add(symbol_in_table);
	  v.add(new_symbol);
	  symbol_in_table.setBrothers(v);
	  new_symbol.setBrothers(v);
	}
	else {
	  v.add(new_symbol);
	  new_symbol.setBrothers(v);	
	}
      }
    }

    else {
      // Simply put the symbol in the symbol table
      symTable.put(scopedName,new_symbol);
    }
  }



  public void addEnumConstant(String name, EnumConstant new_symbol, CompilerError ce) {
    // Enum constants are in the ordinary namespace
    new_symbol.setScopeDepth(scope_depth);
    String scopedName=addCurrentScopeToName(name);
    Symbol symbol_in_table=symTable.get(scopedName);
    
    if (symbol_in_table!=null) {
      // A symbol with this name already exists
      // => it is necessarily an error with a typedef which can not be redefined

      if (symbol_in_table instanceof EnumConstant) {
	// Redefinition of the enum constant
	raiseRedefinitionError(name,new_symbol,symbol_in_table,ce);
      }
      else {
	// Different kind of symbol
	raiseDifferentSymbolError(name,new_symbol,symbol_in_table,ce);
      }
    }
    else {
      // Simply put the symbole in the symbol table
      symTable.put(scopedName,new_symbol);
    }
  }


  public void addTypedef(String name, Typedef new_symbol, CompilerError ce) {
    // Typedefs are in the ordinary namespace
    new_symbol.setScopeDepth(scope_depth);
    String scopedName=addCurrentScopeToName(name);
    Symbol symbol_in_table=symTable.get(scopedName);
    
    if (symbol_in_table!=null) {
      // A symbol with this name already exists
      // => it is necessarily an error with a typedef which can not be redefined

      if (symbol_in_table instanceof Typedef) {
	// Redefinition of the typedef
	raiseRedefinitionError(name,new_symbol,symbol_in_table,ce);
      }
      else {
	// Different kind of symbol
	raiseDifferentSymbolError(name,new_symbol,symbol_in_table,ce);
      }
    }
    else {
      // Simply put the symbole in the symbol table
      symTable.put(scopedName,new_symbol);
    }
  }


  public void addFunction(String name, FunctionLabel new_symbol, CompilerError ce) {
    addFunction(name, new_symbol, false, ce);
  }

  public void addFunction(String name, FunctionLabel new_symbol, 
			  boolean canRedefine, CompilerError ce) {
    // Functions are in the ordinary namespace
    new_symbol.setScopeDepth(scope_depth);
    String scopedName=addCurrentScopeToName(name);
    Symbol symbol_in_table=symTable.get(scopedName);

    if (symbol_in_table!=null) {
      // A symbol with this name already exists


      // Check that it is a function type
      if (!(symbol_in_table instanceof CommonFunctionLabelInterface)) {
	raiseDifferentSymbolError(name,new_symbol,symbol_in_table,ce);
	return;
      }

      if (symbol_in_table instanceof MangledFunctionPseudoLabel) {
	// The symbol in the table is mangled
	MangledFunctionPseudoLabel pseudo_function_in_table
	  =(MangledFunctionPseudoLabel)symbol_in_table;

	// The previous and new symbol must have the same mangling property
	if (!new_symbol.isMangledFunction()) {
	  raiseManglingError2(name,new_symbol,pseudo_function_in_table,ce);
	}

	// Is there a compatible function ?
	FunctionLabel compatibleSymbolInTable=pseudo_function_in_table
	  .getEquivalentMangledFunction(new_symbol.getType());

	if (compatibleSymbolInTable==null) {
	  // No compatible Symbol, just add it
	  pseudo_function_in_table.addMangledFunction(new_symbol);
	}
	else {
	  // A builtin function prototype can only be defined
	  // or redefined by a builtin function
	  if ( compatibleSymbolInTable.isExternalBuiltinFunction() &&
	      (!new_symbol.isExternalBuiltinFunction())
	       ) {
	    raiseBuiltinRedefinitionError(name,new_symbol,
					  pseudo_function_in_table,ce);
	  }

	  if (compatibleSymbolInTable.isPrototype()) {

	    if (new_symbol.isPrototype()) {
	      // * Function prototype  vs  function prototype *
	      
	      // We flush the new prototype
	      flush(new_symbol);
	    }
	    else  {
	      // * Function prototype  vs  function definition *
	      
	      // Note: everybody is supposed here to be at scope level 0
	      // We must flush the prototype of the symbol table
	      flush(compatibleSymbolInTable);
	      pseudo_function_in_table
		.removeMangledFunction(compatibleSymbolInTable);
	      
	      // Add the new symbol
	      pseudo_function_in_table.addMangledFunction(new_symbol);
	    }
	  }
	  else {
	    if (new_symbol.isPrototype()) {
	      // * Function definition  vs  function prototype *
	      
	      // We flush the new prototype
	      flush(new_symbol);
	    }
	    else {
	      // * Function definition  vs  function definition *

	      // Redefinition of the function
	      if (canRedefine) {
		raiseFunctionRedefinitionWarning(name,new_symbol,
						 compatibleSymbolInTable,ce);
	      }
	      else {
		raiseFunctionRedefinitionError(name,new_symbol,
					       compatibleSymbolInTable,ce);
	      }
	    }	    
	  }

	  // Manage brother relationship between kept symbol and flushed one
	  Vector<Symbol> v=compatibleSymbolInTable.getBrothers();
	  if (v==null) {
	    v=new Vector<Symbol>();
	    v.add(compatibleSymbolInTable);
	    v.add(new_symbol);
	    compatibleSymbolInTable.setBrothers(v);
	    new_symbol.setBrothers(v);
	  }
	  else {
	    v.add(new_symbol);
	    new_symbol.setBrothers(v);	
	  }

	}

	return;
      }


      // The symbol in the table is mangled
      FunctionLabel function_in_table=(FunctionLabel)symbol_in_table;

      if (function_in_table.isPrototype()) {

	if (new_symbol.isPrototype()) {
	  // * Function prototype  vs  function prototype *


	  // Check for equivalence between both declarations
	  if (!function_in_table.getType()
	      .isEquivalentForFunctionDeclaration(new_symbol.getType())) {
	    raiseConflictingTypeError(name,new_symbol,function_in_table,ce);
	  }

	  // A builtin function prototype can only be defined or redefined by a
	  // builtin function
	  if ( function_in_table.isExternalBuiltinFunction() &&
	      (!new_symbol.isExternalBuiltinFunction())
	     ) {
	    raiseBuiltinRedefinitionError(name,new_symbol,function_in_table,ce);
	  }

	  // We flush the new prototype
	  flush(new_symbol);
	}
	else  {
	  // * Function prototype  vs  function definition *

	  // Check for equivalence between both declarations
	  if (!function_in_table.getType()
	      .isEquivalentForFunctionDeclaration(new_symbol.getType())) {
	    raiseConflictingTypeError(name,new_symbol,function_in_table,ce);
	  }

	  // A builtin function prototype can only be defined or redefined by a
	  // builtin function
	  if (function_in_table.isExternalBuiltinFunction() &&
	      (!new_symbol.isExternalBuiltinFunction())
	     ) {
	    raiseBuiltinRedefinitionError(name,new_symbol,function_in_table,ce);
	  }

	  // Note: everybody is supposed here to be at scope level 0
	  // We must flush the prototype of the symbol table
	  flush(function_in_table);
	  // Add the new symbol
	  symTable.put(scopedName,new_symbol);
	}
      }
      else {
	  
	if (new_symbol.isPrototype()) {
	  // * Function definition  vs  function prototype *

	  // Check for equivalence between both declarations
	  if (!function_in_table.getType()
	      .isEquivalentForFunctionDeclaration(new_symbol.getType())) {
	    raiseConflictingTypeError(name,new_symbol,function_in_table,ce);
	  }

	  // A builtin function prototype can only be defined or redefined by a
	  // builtin function
	  if ( function_in_table.isExternalBuiltinFunction() &&
	       (!new_symbol.isExternalBuiltinFunction())
	     ) {
	    raiseBuiltinRedefinitionError(name,new_symbol,function_in_table,ce);
	  }

	  // We flush the new prototype
	  flush(new_symbol);
	}
	else {
	  // * Function definition  vs  function definition *
	  if (canRedefine) {
	    raiseFunctionRedefinitionWarning(name,new_symbol,function_in_table,ce);
	  }
	  else {
	    raiseFunctionRedefinitionError(name,new_symbol,function_in_table,ce);
	  }
	}
      }


      // Manage brother relationship between kept symbol and flushed one
      Vector<Symbol> v=function_in_table.getBrothers();
      if (v==null) {
	v=new Vector<Symbol>();
	v.add(function_in_table);
	v.add(new_symbol);
	function_in_table.setBrothers(v);
	new_symbol.setBrothers(v);
      }
      else {
	v.add(new_symbol);
	new_symbol.setBrothers(v);	
      }
    }

    else {
      // No such symbol in the table, simply put the symbol in the table
      if (new_symbol.isMangledFunction()) {
	MangledFunctionPseudoLabel top_symbol
	  =new MangledFunctionPseudoLabel(new_symbol.getName());
	top_symbol.setScopeDepth(new_symbol.getScopeDepth());
	top_symbol.addMangledFunction(new_symbol);
	symTable.put(scopedName,top_symbol);
      }
      else {
	symTable.put(scopedName,new_symbol);
      }
    }
  }



  //------------------------------------------------------------------
  // Tag symbol
  //------------------------------------------------------------------


  //------------------------------------------------------------------
  //  addTag 
  //
  //  Add a new tag symbol definition related to the current scope
  //  The current function performs compatibility checks and raises
  //  potential errors
  //  Note: This function assumes that the symbol name has already
  //        been mangled
  //
  //------------------------------------------------------------------
  public void addTag(String name, TagSymbol new_symbol, CompilerError ce) {
    new_symbol.setScopeDepth(scope_depth);
    String scopedName=addCurrentScopeToName(name);

    TagSymbol symbol_in_table=symTableTag.get(scopedName);
    if (symbol_in_table!=null) {
      if (new_symbol.getClass()==symbol_in_table.getClass()) {
	// Same family, uncomplete tags can be overwritten
	if (symbol_in_table.getType().isComplete()) {
	  if (new_symbol.getType().isComplete()) {
	    // Should never occur
	    ce.raiseMessage(new_symbol.getIdNode(), "redefinition of "
			    + new_symbol.getMessageName());
	    ce.raiseError(symbol_in_table.getIdNode(), 
			  symbol_in_table.getMessageName()
			  + " previously declared here");
	  }
	  else {
	    flush(new_symbol);
	  }
	}
	else {
	  flush(symbol_in_table);
	  symTableTag.put(scopedName,new_symbol);
	}
      }
      else {
	// Different family
	ce.raiseError(new_symbol.getIdNode(),new_symbol.getMessageName()
		      + " redeclared as different kind of symbol");
	if (symbol_in_table.getIdNode()!=null) {
	  ce.raiseMessage(symbol_in_table.getIdNode(),
			  symbol_in_table.getMessageName()
			  + " previously declared here");
	}
      }

      // Manage brother relationship between kept symbol and flushed one
      Vector<Symbol> v=symbol_in_table.getBrothers();
      if (v==null) {
	v=new Vector<Symbol>();
	v.add(symbol_in_table);
	v.add(new_symbol);
	symbol_in_table.setBrothers(v);
	new_symbol.setBrothers(v);
      }
      else {
	v.add(new_symbol);
	new_symbol.setBrothers(v);	
      }
    }
    else {
      // Simply put the symbol in the table
      symTableTag.put(scopedName,new_symbol);
    }
  }


  //------------------------------------------------------------------
  // Code Label symbol
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  //  addCodeLabel 
  //
  //  Add a new Label symbol related to the current function
  //  The current function performs compatibility checks and raises
  //  potential errors
  //  Note: This function assumes that the symbol name has already
  //        been mangled
  //
  //------------------------------------------------------------------
  public void addCodeLabel(String name, CodeLabel symbol, CompilerError ce) {
    symbol.setScopeDepth(scope_depth_label);
    String scopedName=addCurrentCodeLabelScopeToName(name);

    CodeLabel s=symTableCodeLabel.get(scopedName);
    if (s!=null) {
	ce.raiseMessage(symbol.getIdNode(),"redefinition of '" + name + "'");
	ce.raiseError(s.getIdNode(),"'" + name + "' previously declared here");
    }
    else {
      // Simply put the symbol in the table
      symTableCodeLabel.put(scopedName,symbol);

      if (!symbol.isDefinition()) {
	referenceCodeLabelList.add(symbol);
      }
    }
  }



  //==================================================================
  // Symbols lookup
  //==================================================================


  //------------------------------------------------------------------
  // getListOfKernels
  //
  // Returns the list of symbols (accessible, not flushed) defining a
  // data or a function 
  //------------------------------------------------------------------
  public ArrayList<FunctionLabel> getListOfKernels() {
    ArrayList<FunctionLabel> symbol_list = new ArrayList<FunctionLabel>();

    // Symbols defining a data or a function are only in the ordinary
    // name space
    for(Symbol symb:symTable.values()) {
      // Note: kernels shall not be mangled
      if (symb instanceof FunctionLabel) {
	FunctionLabel func_symb=(FunctionLabel)symb;
	if (func_symb.isDefinition() && func_symb.isKernel()) {
	  symbol_list.add(func_symb);
	}
      }
    }

    return(symbol_list);
  }


  //------------------------------------------------------------------
  // getListOfFunctionDefinitionLabels
  //
  // Returns an array List containing function definition symbols
  // of the symbol table
  //------------------------------------------------------------------
  public ArrayList<FunctionLabel> getListOfFunctionDefinitionLabels() {
    ArrayList<FunctionLabel> symbol_list = new ArrayList<FunctionLabel>();

    // Functions are in the ordinary namespace
    for(Symbol symb:symTable.values()) {

      // Function
      if (symb instanceof FunctionLabel) {
	if (((FunctionLabel)symb).isDefinition()) {
	  symbol_list.add((FunctionLabel)symb);
	}
      }

      // Mangled function
      else if (symb instanceof MangledFunctionPseudoLabel) {
        LinkedList<FunctionLabel> mangled_list=
	  ((MangledFunctionPseudoLabel)symb).getManglingList();
	for(FunctionLabel func_symb:mangled_list) {
	  if (func_symb.isDefinition()) {
	    symbol_list.add(func_symb);
	  }
	}
      }
    }

    return(symbol_list);
  }


  //------------------------------------------------------------------
  // getListOfFunctionPrototypeLabels
  //
  // Returns an array List containing function prototype symbols
  // of the symbol table
  //------------------------------------------------------------------
  public ArrayList<FunctionLabel> getListOfFunctionPrototypeLabels() {
    ArrayList<FunctionLabel> symbol_list = new ArrayList<FunctionLabel>();

    // Functions are in the ordinary namespace
    for(Symbol symb:symTable.values()) {

      // Function
      if (symb instanceof FunctionLabel) {
	if (((FunctionLabel)symb).isPrototype()) {
	  symbol_list.add((FunctionLabel)symb);
	}
      }

      // Mangled function
      else if (symb instanceof MangledFunctionPseudoLabel) {
        LinkedList<FunctionLabel> mangled_list=
	  ((MangledFunctionPseudoLabel)symb).getManglingList();
	for(FunctionLabel func_symb:mangled_list) {
	  if (func_symb.isPrototype()) {
	    symbol_list.add(func_symb);
	  }
	}
      }
    }

    return(symbol_list);
  }


  //------------------------------------------------------------------
  // getListOfFunctionDataDefinitionSymbol
  //
  // Returns the list of symbols (accessible, not flushed) defining a
  // data or a function 
  //------------------------------------------------------------------
  public ArrayList<Symbol> getListOfFunctionDataDefinitionSymbol() {
    ArrayList<Symbol> symbol_list = new ArrayList<Symbol>();

    // Symbols defining a data or a function are only in the ordinary
    // name space
    for(Symbol symb:symTable.values()) {

      // Data objects (variables and arrays)
      if (symb instanceof ObjectLabel) {
	// Only definitions are considered here
	if (!symb.isExtern()) {
	  symbol_list.add(symb);
	}
      }
      
      // Function
      else if (symb instanceof FunctionLabel) {
	if (((FunctionLabel)symb).isDefinition()) {
	  symbol_list.add(symb);
	}
      }

      // Mangled function
      else if (symb instanceof MangledFunctionPseudoLabel) {
        LinkedList<FunctionLabel> mangled_list=
	  ((MangledFunctionPseudoLabel)symb).getManglingList();
	for(FunctionLabel func_symb:mangled_list) {
	  if (func_symb.isDefinition()) {
	    symbol_list.add(func_symb);
	  }
	}
      }
    }

    return(symbol_list);
  }


  //------------------------------------------------------------------
  // getListOfFunctionDataReferenceSymbol
  //
  // Returns the list of symbols (accessible, not flushed) referencing
  // data or a function (defined in an other module)
  //------------------------------------------------------------------
  public ArrayList<Symbol> getListOfFunctionDataReferenceSymbol() {
    ArrayList<Symbol> symbol_list = new ArrayList<Symbol>();

    // Symbols defining a data object or a function are only in the ordinary
    // name space
    for(Symbol symb:symTable.values()) {

      // Data objects (variables and arrays)
      if (symb instanceof ObjectLabel) {
	// Only references are considered here
	if (symb.isExtern()) {
	  symbol_list.add(symb);
	}
      }
      
      // Function
      else if (symb instanceof FunctionLabel) {
	if (((FunctionLabel)symb).isPrototype()) {
	  symbol_list.add(symb);
	}
      }

      // Mangled function
      else if (symb instanceof MangledFunctionPseudoLabel) {
        LinkedList<FunctionLabel> mangled_list=
	  ((MangledFunctionPseudoLabel)symb).getManglingList();
	for(FunctionLabel func_symb:mangled_list) {
	  if (func_symb.isPrototype()) {
	    symbol_list.add(func_symb);
	  }
	}
      }
    }

    return(symbol_list);
  }




  //------------------------------------------------------------------
  // Ordinary symbol lookup
  //------------------------------------------------------------------


  //------------------------------------------------------------------
  // lookupName
  //
  // lookup an unscoped name in the table by prepending
  // the current scope.
  // - if not found, pop scopes and look again
  // - returns null if no symbol found
  //------------------------------------------------------------------
  public Symbol lookupName(String name) {
    String scope  = currentScopeAsString();
    String scopedName;
    Symbol symbol = null;

    while ( (symbol==null) && (scope != null) ) {
      scopedName = addScopeToName(scope, name);
      symbol = symTable.get(scopedName);
      scope = removeOneLevelScope(scope);
    }
    return(symbol);
  }

  //------------------------------------------------------------------
  // lookupNameInTopLevelScope
  //
  // lookup an unscoped name in the table at top level
  // - if not found, pop scopes and look again
  // - returns null if no symbol found
  //------------------------------------------------------------------
  public Symbol lookupNameInTopLevelScope(String name) {
    return symTable.get(name);
  }


  //------------------------------------------------------------------
  // Tag lookup
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // lookupTagName
  //
  // lookup an unscoped name in the table by prepending
  // the current scope.
  // - if not found, pop scopes and look again
  // - returns null if no symbol found
  //------------------------------------------------------------------
  public TagSymbol lookupTagName(String name) {
    String scope  = currentScopeAsString();
    String scopedName;
    TagSymbol symbol = null;

    while ( (symbol==null) && (scope != null) ) {
      scopedName = addScopeToName(scope, name);
      symbol = symTableTag.get(scopedName);
      scope = removeOneLevelScope(scope);
    }
    return(symbol);
  }

  //------------------------------------------------------------------
  // lookupTagNameInCurrentScope
  //
  // lookup an unscoped name in the table by prepending
  // the current scope.
  // - if not found, pop scopes and look again
  // - returns null if no symbol found
  //------------------------------------------------------------------
  public TagSymbol lookupTagNameInCurrentScope(String name) {
    String scope  = currentScopeAsString();
    String scopedName;
    TagSymbol symbol = null;

    scopedName = addScopeToName(scope, name);
    symbol = symTableTag.get(scopedName);

    return(symbol);
  }


  //------------------------------------------------------------------
  // Label lookup
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // lookupCodeLabel
  //
  // lookup an unscoped name in the table by prepending
  // the current scope.
  // - if not found, pop scopes and look again
  // - returns null if no symbol found
  //------------------------------------------------------------------
  public CodeLabel lookupCodeLabel(String name) {
    String scope  = currentCodeLabelScopeAsString();
    String scopedName;
    CodeLabel symbol = null;

    while ( (symbol==null) && (scope != null) ) {
      scopedName = addScopeToName(scope, name);
      symbol = symTableCodeLabel.get(scopedName);
      scope = removeOneLevelScope(scope);
    }
    return(symbol);
  }




  //==================================================================
  // Debug functions
  //==================================================================

  //------------------------------------------------------------------
  // toString
  //
  // Converts the symbol table to a string
  //------------------------------------------------------------------
  public String toString() {
    StringBuffer buff = new StringBuffer(300);

    buff.append("SymbolTable { \nCurrentScope: ");
    
    String current_scope=currentScopeAsString();
    if (current_scope.compareTo("")==0) {
      buff.append("  <top level>");
    }
    else {
      buff.append(currentScopeAsString());
    }
    // Ordinary name space
    buff.append("\n");
    buff.append("\n  ORDINARY name space:\n");
    Iterator<Map.Entry<String,Symbol>> iter=symTable.entrySet().iterator();
    while (iter.hasNext()) {
      // Get Map informations
      Map.Entry<String,Symbol> entry = iter.next();
      String s    = entry.getKey();
      Symbol symb = entry.getValue();

      // Generate symbol information
      int id = symb.getId();
      buff.append("  ");
      if (id<10) {
	buff.append("  ");
      }
      else if (id<100) {
	buff.append(" ");
      }
      buff.append("").append(id).append("  ").append(s.toString())
	  .append("\n       -> ").append(symb.toString()).append("\n");
    }

    // Tag namespace
    buff.append("\n  TAG name space:\n");
    Iterator<Map.Entry<String,TagSymbol>> iter_tag=symTableTag.entrySet().iterator();
    while (iter_tag.hasNext()) {
      // Get Map informations
      Map.Entry<String,TagSymbol> entry = iter_tag.next();
      String s    = entry.getKey();
      Symbol symb = entry.getValue();

      // Generate symbol information
      int id = symb.getId();
      buff.append("  ");
      if (id<10) {
	buff.append("  ");
      }
      else if (id<100) {
	buff.append(" ");
      }
      buff.append("").append(id).append("  ").append(s.toString())
	  .append("\n       -> ").append(symb.toString()).append("\n");
    }

    // Code label namespace
    buff.append("\n  CODE LABEL name space:\n");
    Iterator<Map.Entry<String,CodeLabel>> iter_code_label
      =symTableCodeLabel.entrySet().iterator();
    while (iter_code_label.hasNext()) {
      // Get Map informations
      Map.Entry<String,CodeLabel> entry = iter_code_label.next();
      String s    = entry.getKey();
      CodeLabel symb = entry.getValue();

      // Generate symbol information
      int id = symb.getId();
      buff.append("  ");
      if (id<10) {
	buff.append("  ");
      }
      else if (id<100) {
	buff.append(" ");
      }
      buff.append("").append(id).append("  ").append(s.toString())
	  .append("\n       -> ").append(symb.toString()).append("\n");
    }

    buff.append("\n  FLUSHED SYMBOLS:\n");
    for(Symbol symb:flushed_list) {
      // Generate symbol information
      int id = symb.getId();
      buff.append("  ");
      if (id<10) {
	buff.append("  ");
      }
      else if (id<100) {
	buff.append(" ");
      }
      buff.append("").append(id).append("  ").append(symb.getOriginalName())
	  .append("\n       -> ").append(symb.toString()).append("\n");
    }
    buff.append("}\n");

    // Return the string
    return(buff.toString());
  }




  //==================================================================
  // Private functions for managing scoped names
  //==================================================================

  // lookup a fully scoped name in the symbol table
  private Symbol lookupScopedName(String scopedName) {
    return(symTable.get(scopedName));
  }

  // given a name for a type, append it with the 
  // current scope.
  private String addCurrentScopeToName(String name) {
    String currScope = currentScopeAsString();
    return(addScopeToName(currScope, name));
  }
  private String addCurrentCodeLabelScopeToName(String name) {
    String currScope = currentCodeLabelScopeAsString();
    return(addScopeToName(currScope, name));
  }


  // given a name for a type, append it with the 
  // given scope.
  private String addScopeToName(String scope, String name) {
    if( (scope==null) || (scope.length()>0) ) {
      return(scope + ":" + name);
    }
    else {
      return(name);
    }
  }

  // remove one level of scope from name
  private String removeOneLevelScope(String scopeName) {
    int index = scopeName.lastIndexOf(":");
    if (index>0) {
      return(scopeName.substring(0,index));
    }
    if (scopeName.length() > 0) {
      return("");
    }
    return(null);
  }

};
