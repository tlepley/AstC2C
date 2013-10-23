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

/* Symbol of the symbol table */

package ir.symboltable.symbols;

import ir.symboltable.*;
import ir.types.*;

import java.math.BigInteger;
import java.util.Vector;

import ir.base.NodeAST;


public class Symbol {

  //--------------------------------------
  // ID unique to each compiler thread
  //--------------------------------------
  private static final InheritableThreadLocal<Integer> id_counter = new InheritableThreadLocal<Integer>() {
    @Override
    protected Integer initialValue() {
      return 0;
    }
  };
  
  private int incrementIdCounter() {
    int i=id_counter.get();
    id_counter.set(i+1);
    return i;
  }

  //------------------------------
  // Private data
  //------------------------------
  // unique symbol ID
  private int id;

  // Symbol name
  private String name   = null; // Original name
  private String rename = null; // after potential renaming

  // Scope level
  private int scope_depth = 0;

  // Symbol type
  private Type type = null;

  // Storage class
  private StorageClass storageClass=null;

  // Link with the AST
  private NodeAST id_tnode             = null; // ID node
  // In split mode
  private NodeAST declaration_tnode    = null; // Declaration node


  // Symbol dependencies
  //--------------------

  // 'Brothers' are declaration referring to the same symbol in
  // a particular scope.
  // It occurs in particular in the case of :
  //   - successive incomplete and complete type tag declaration
  //   - successive function prototype / definition
  //   - successive external / non external variable or array definition
  Vector<Symbol> brothers = null;

  // 'Parents' are type tags necessary to the definition of the
  // symbol
  Vector<Symbol> parents  = null;


  //------------------------------
  // Information set by the linker
  //------------------------------

  // Symbol defined in the program
  // Note: a program corresponds to the link unit, namely the set of
  //       source files provided to the linker
  boolean programInternal = false;
  // [Note] We could have a 'symbol alias' notion for external symbols
  //        which have been linked

  //---------------------------------------------
  // Specific for extracted (and mangled) symbols
  //---------------------------------------------
  Symbol functionScope=null;

  //==================================================================
  // Constructors
  //==================================================================

  public Symbol() {
    id     = incrementIdCounter();
    storageClass=new StorageClass();

    }
  public Symbol(String s) {
    id     = incrementIdCounter();
    name   = s;
    storageClass=new StorageClass();
  }
  public Symbol(TypeSpecifierQualifier s) {
    id     = incrementIdCounter();
    storageClass  = new StorageClass();
  }
  public Symbol(StorageClass sc) {
    id     = incrementIdCounter();
    storageClass  = sc;
  }

  
  // Copy constructor
  public Symbol(Symbol symb) {
    id	                 = incrementIdCounter(); // New (unique id)

    // Name
    name                 = symb.name;
    rename               = symb.rename;
    
    // Scope location
    scope_depth          = symb.scope_depth;

    // Symbol type (future use)
    type                 = symb.type;

    // Storage class
    storageClass	 = symb.storageClass;

    // Link to AST
    id_tnode             = symb.id_tnode;
    declaration_tnode    = symb.declaration_tnode;

    brothers             = symb.brothers;
    if (brothers!=null) {
      brothers           = new Vector<Symbol>(symb.brothers);
    }
    parents  	         = symb.parents;
    if (parents!=null) {
      parents            = new Vector<Symbol>(symb.parents);
    }

    // Linker information
    programInternal     = symb.programInternal;


    uncomplete_array    = symb.uncomplete_array;
  }
 

  //==================================================================
  // Array specific
  //==================================================================

  // Incomplete arrays
  private NodeAST uncomplete_array     = null;

  // Sets the symbol as an incomplete array
  // An incomplete array, is an array for which the first dimension
  // is not specified (should be specified in the initializer)
  public void setUncompleteArraySizeNode(NodeAST t) {
    uncomplete_array=t;
  }
  // Sets the size of the incomplete array (from the initializer)
  public void setInitArraySize(int i) {
    if (uncomplete_array!=null) {
      // Adds the text of the number to the node
      uncomplete_array.setText(""+i);
      // Sets the node as constant
      uncomplete_array.getDataType().setConstantIntegral(BigInteger.valueOf(i));
    }
  }





  //==================================================================
  // Setters
  //==================================================================

  //------------------------------------------------------------------
  // setName
  //
  // Sets the original name of the symbol
  //------------------------------------------------------------------
  public void setName(String s) {
    name=s;
  }

  //------------------------------------------------------------------
  // reName
  //
  // Rename the symbol and all it's brothers (flushed associated
  // symbols)
  //------------------------------------------------------------------
  public void reName(String str) {
    if (brothers!=null) {
      for(Symbol s:brothers) {
	s.rename=str;
      }
    }
    else {
      rename=str;
    }
  }

  //------------------------------------------------------------------
  // setScopeDepth
  //
  // Sets the scope depth of the symbol (0 is the global scope)
  //------------------------------------------------------------------
  public void setScopeDepth(int d) {
    scope_depth=d;
  }

  //------------------------------------------------------------------
  // setScopeDepth
  //
  // Sets the scope depth of the symbol (0 is the global scope)
  //------------------------------------------------------------------
  public void setFunctionScope(Symbol f) {
    functionScope=f;
  }
  public Symbol getFunctionScope() {
    return functionScope;
  }

  //------------------------------------------------------------------
  // setType
  //
  // Sets the type of the symbol
  //------------------------------------------------------------------
  public void setType(Type t) {
    type=t;
  }

  //==================================================================
  // Links with the AST
  //==================================================================

  // Sets the ID node for the symbol declaration
  public void setIdNode(NodeAST t) {
    id_tnode=t;
  }
  // Sets the head of the declaration AST tree
  public void setDeclarationNode(NodeAST t) {
    declaration_tnode=t;
  }

  // Returns the ID node of the symbol declaration
  public NodeAST getIdNode() {
    return(id_tnode);
  }
  // Returns the head of the declaration tree
  public NodeAST getDeclarationNode() {
    return(declaration_tnode);
  }



  //==================================================================
  // Linker specific
  //==================================================================

  // Setting
  public void setProgramInternal() {
    if (brothers!=null) {
      for(Symbol s:brothers) {
	s.programInternal=true;
      }
    }
    programInternal=true;
  }

  // Query
  public boolean isProgramInternal() {
    return programInternal;
  }


  //==================================================================
  // 'Brothers' management
  //
  // -> for incomplete and complete type tag management
  //==================================================================

  // Setting
  public void setBrothers(Vector<Symbol> v) {
    brothers=v;
  }
  public void addBrother(Symbol symb) {
    if (brothers==null) {
      brothers=new Vector<Symbol>(10);
    }
    brothers.addElement(symb);
  }
  // Query
  public int getNbBrothers() {
    if (brothers==null) {
      return(0);
    }
    return(brothers.size());
  }
  public Vector<Symbol> getBrothers() {
    return brothers;
  }



  //==================================================================
  // 'parents' management
  //
  // 'Parents' are globalisable and compile time resolvable symbols
  // (tag, typedef, function prototype) necessary for the declaration
  // of the symbol.
  // Note: Parents are useful for moving a symbol declaration and for
  //       make a symbol global
  //==================================================================

  // Setting
  public void setParents(Vector<Symbol> v) {
    parents=v;
  }
  public void addParent(Symbol symb) {
    if (parents==null) {
      parents=new Vector<Symbol>(10);
    }
    parents.addElement(symb);
  }

  // Query
  public int getNbParents() {
    if (parents==null) {
      return(0);
    }
    return(parents.size());
  }
  public Vector<Symbol> getParents() {
    return parents;
  }



  //==================================================================
  // Getters
  //==================================================================

  //------------------------------------------------------------------
  // getId
  //
  // Returns the (unique) symbol id
  //------------------------------------------------------------------
  public int getId() {
    return(id);
  }

  //------------------------------------------------------------------
  // getOutputName
  //
  // Returns the symbol name for the output source file. It performs
  // mangling if necessary
  //------------------------------------------------------------------
  public String getOutputName() {
    return getName();
  }

  //------------------------------------------------------------------
  // getName
  //
  // Returns the symbol name
  //------------------------------------------------------------------
  public String getName() {
    if (rename==null) {
      return(name);
    }
    return(rename);
  }

  //------------------------------------------------------------------
  // getOriginalName
  //
  // Get the original name (if renaming occurred)
  //------------------------------------------------------------------
  public String getOriginalName() {
    return(name);
  }

  //------------------------------------------------------------------
  // getType
  //
  // Returns type corresponding to the symbol
  //------------------------------------------------------------------
  public Type getType() {
    return type;
  }

  //------------------------------------------------------------------
  // getStorageClass
  //
  // Returns the storage class corresponding to the symbol
  //------------------------------------------------------------------
  public StorageClass getStorageClass() {
    return storageClass;
  }

  //------------------------------------------------------------------
  // getScopeDepth
  //
  // Returns the scope depth of the symbol (0 is the global scope)
  //------------------------------------------------------------------
  public int getScopeDepth() {
    return scope_depth;
  }

  //------------------------------------------------------------------
  // isInTopLevelScope
  //
  // Returns 'true' if the symbol is in the top scope
  //------------------------------------------------------------------
  public boolean isInTopLevelScope() {
    return scope_depth==0;
  }


  //------------------------------------------------------------------
  // referencesCompileTimeAllocatedEntity
  //
  // Returns 'true' if the symbol references an object or a function
  // whose address is compile time known
  // Note: concerns only data objects and functions
  //------------------------------------------------------------------
  public boolean referencesCompileTimeAllocatedEntity() {
    return false;
  }


  //------------------------------------------------------------------
  // isInProgramScope
  //
  // Returns 'true' if the symbol scope is the program (exported from
  // the C module)
  // Note: concerns only data objects and functions
  //------------------------------------------------------------------
  public boolean isInProgramScope() {
    if ((!isInTopLevelScope())||isStatic()||isInline()) {
      return false;
    }
    return true;
  }


  //------------------------------------------------------------------
  // Storage class tests
  //------------------------------------------------------------------
    // Returns 'true' if the symbol is declared as 'extern'
  public boolean isExtern() {
    return storageClass.isExtern();
  }
    // Returns 'true' if the symbol is declared as 'static'
  public boolean isStatic() {
    return storageClass.isStatic();
  }
    // Returns 'true' if the symbol is declared as 'register'
  public boolean isRegister() {
    return storageClass.isRegister();
  }
    // Returns 'true' if the symbol is declared as 'auto'
  public boolean isAuto() {
    return storageClass.isAuto();
  }
    // Returns 'true' if the symbol is declared as 'inline'
  public boolean isInline() {
    return storageClass.isInline();
  }
  // Returns 'true' if the symbol is visible only in the C module
  // ('static' or 'inline')
  public boolean isModuleVisibility() {
    return(isStatic()||isInline());
  }





  //==================================================================
  // Verbose functions
  //==================================================================


  //------------------------------------------------------------------
  // getMessageName:
  //
  // Return the symbol reference name as i should appear in a message
  // or error
  //------------------------------------------------------------------
  public String getMessageName() {
    return "symbol '"+ name +"'";
  }


  //------------------------------------------------------------------
  // toString:
  //
  // Dump the symbol to a string
  //------------------------------------------------------------------
  public String toString() {
    StringBuffer buff = new StringBuffer();

    // Name
    buff.append("name=");
    if (rename!=null) {
      buff.append(rename).append(" (original=").append(name).append(")");
    }
    else {
      buff.append(name);
    }
            
    // Scope depth
    buff.append(", depth=").append(scope_depth);
      
    // 'Type'
    if (type==null) {
      buff.append(", [no type]");
    }
    else {
      buff.append(", type=").append(type.toString());
    }
          
    // Storage class
    boolean flag=false;
    buff.append(", storage=[");
    if (isExtern()) {
      if (flag) { buff.append(" "); }
      buff.append("extern");
      flag=true;
    }
    if (isInline()) {
      if (flag) { buff.append(" "); }
      buff.append("inline");
      flag=true;
    }
    if (isStatic()) {
      if (flag) { buff.append(" "); }
      buff.append("static");
      flag=true;
    }
    if (isRegister()) {
      if (flag) { buff.append(" "); }
      buff.append("register");
      flag=true;
    }
    if (isAuto()) {
      if (flag) { buff.append(" "); }
      buff.append("auto");
      flag=true;
    }
    if (!flag) {
      buff.append("NO");
    }
    buff.append("]");
      
    // 'Brothers'
    if (brothers==null) {
      buff.append(", [no brothers]");
    }
    else {
      int i=0;
      buff.append(", brothers=[");
      for(Object obj:brothers) {
	if (i==0) {
	  i=1;
	}
	else {
	  buff.append(" ");
	}
	buff.append(((Symbol)obj).id);
      }
      buff.append("]");
    }
    
    // 'Parents'
    if (parents==null) {
      buff.append(", [no parent]");
    }
    else {
      int i=0;
      buff.append(", parents=[");
      for(Object obj:parents) {
	if (i==0) {
	  i=1;
	}
	else {
	  buff.append(" ");
	}
	buff.append(((Symbol)obj).id);
      }
      buff.append("]");
    }
      
      
    // Program internal
    if (isProgramInternal()) {
      buff.append(", [program internal]");
    }

    // Return the final string
    return buff.toString();
  }

  //------------------------------------------------------------------
  // toStringShort:
  //
  // Dump the symbol to a string in a short way
  //------------------------------------------------------------------
  public String toStringShort() {
    StringBuffer buff = new StringBuffer();
    buff.append(id).append("/");
    if (rename!=null) {
      buff.append(rename).append("(").append(name).append(")");
    }
    else {
      buff.append(name);
    }
    buff.append("/").append(scope_depth).append("/");;

    // Return the final string
    return(buff.toString());
  }

}
