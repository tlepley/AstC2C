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

/* Source level linker */


package ir.symboltable;

import ir.symboltable.symbols.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.io.PrintStream;

import common.CompilerError;


public class Linker {

  // Linker option
  boolean optionLink = false;

  // Error manager
  CompilerError globalCompilerError;

  // Global symbol table
  SymbolTable globalSymbolTable = new SymbolTable();

  // Lists of symbol tables
  ArrayList<SymbolTable> symbolTableArrayList = new ArrayList<SymbolTable>();
  ArrayList<String>      fileNameArrayList    = new ArrayList<String>();
  int nbSymbolTable=0;


  // ##################################################################
  // Building
  // ##################################################################

  // Constructor
  public Linker(boolean nc, CompilerError ce) {
    optionLink=nc;
    globalCompilerError=ce;
  }

  //------------------------------------------------------------------
  //  addSymbolTable  
  //
  // Add a symbol table to the linker
  //
  //------------------------------------------------------------------
  public void addSymbolTable(SymbolTable st, String s) {
    symbolTableArrayList.add(st);
    fileNameArrayList.add(s);
    nbSymbolTable++;
  }

  //------------------------------------------------------------------
  // getSymbolTable   
  //
  // Returns the ith symbol table
  //
  //------------------------------------------------------------------
  public SymbolTable getSymbolTable(int i) {
    return symbolTableArrayList.get(i);
  }

  //------------------------------------------------------------------
  // getGlobalSymbolTable   
  //
  // Returns the ith symbol table
  //
  //------------------------------------------------------------------
  public SymbolTable getGlobalSymbolTable() {
    return globalSymbolTable;
  }


  // ##################################################################
  // Verbose
  // ##################################################################

  //------------------------------------------------------------------
  // printSymbolTables   
  //
  // Print the symbol table of each linked module
  //
  //------------------------------------------------------------------
  public void printSymbolTables(PrintStream ps) {
    int i;

    for(i=0;i<nbSymbolTable;i++) {
      SymbolTable st  = symbolTableArrayList.get(i);
      String fileName = fileNameArrayList.get(i);
      ps.println();
      ps.println("File '" + fileName + "'");
      ps.print  ("-------");
      for (int j = 0; j < (fileName.toString().length()); j++) { ps.print("-"); }
      ps.println();
      ps.println(st.toString());
    }
    ps.println();
  }


  //------------------------------------------------------------------
  //  run
  //
  //  Performs the link for function and data symbols with a scope
  //  global to the program (the program is the link unit)
  //  => Sets the 'ProgramInternal' information to program scope which
  //     correspond to objects of the component
  //
  //------------------------------------------------------------------

  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // [TBW] Should check that symbols with the same name are coherents
  //       in term of types
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  public void run() {
    int i;

    // Process function and data definitions of all modules
    //-----------------------------------------------------
    HashSet<Symbol> errorSet=new HashSet<Symbol>();
    for(i=0;i<nbSymbolTable;i++) {
      SymbolTable st=symbolTableArrayList.get(i);
      
      // Get the list of data and function definition
      ArrayList<Symbol> def_list =
	st.getListOfFunctionDataDefinitionSymbol();
      
      for (Symbol symb : def_list) {
	// It is an 'object' internal to the program
	//System.err.println(symb.getName()+" set as program internal");
	symb.setProgramInternal();

	// Update the global symbol table with objects global to the program
	// (exported out of a module)
	if (symb.isInProgramScope()) {
	  //-> The scope of the symbol is the program (exported from the
	  //   module)

	  String symbName = symb.getName();	

	  if (symb instanceof FunctionLabel) {
	    // [TBW] actually, with type tags, it is not possible to check the
	    // the equivalence of functions through types.
	    // Here, we should use the mangled function name ONLY !
 
	    globalSymbolTable.addFunction(symbName, (FunctionLabel)symb, true, globalCompilerError);
	  }
	  else {
	    // [TBW] We may want to check the type coherency
	    //       actually, with type tags, it is not possible to check the coherency
	    //       so that we should use the mangled names ONLY !

	    // Look if such a symbol already exists in the global symbol table
	    // Note: the global symbol table works at the top scope level only
	    Symbol symb_old = globalSymbolTable.lookupName(symbName);

	    // [TBW] all this should be handled in SymbolTable, not here ...
	    if (symb_old != null) {
	      //==========================================================
	      // The symbol already exists
	      //==========================================================
	      
	      // Such a symbol is already defined
	      if (!errorSet.contains(symb_old)) {
		errorSet.add(symb_old);
		if (optionLink) {
		  globalCompilerError.raiseWarning(symb.getIdNode(),
						   "multiple definition of '" + symb_old.getName()
						   + "' (no error forced by option)");
		}
		else {
		  globalCompilerError.raiseError(symb.getIdNode(),
						 "multiple definition of '" + symb_old.getName() + "'");
		}
	      }
	      
	      
	      else {
		//==========================================================
		// The symbol does not exist
		//==========================================================
		
		// Add the symbol in the ordinary name space
		// Note: the global symbol table works at the top scope level only
		if (symb instanceof ObjectLabel) {
		  globalSymbolTable.addObjectLabel(symbName, (ObjectLabel)symb, globalCompilerError);
		}
		else {
		  globalCompilerError.raiseInternalError(symb.getIdNode(),
							 "Linker::run");
		}
	      }
	    }
	  }
	}
      }
    }

    // Process function prototypes and external data references
    //---------------------------------------------------------
    for(i=0;i<nbSymbolTable;i++) {
      SymbolTable st= symbolTableArrayList.get(i);

      ArrayList<Symbol> ref_list =
	st.getListOfFunctionDataReferenceSymbol();

      for (Symbol symb : ref_list) {
	// [TBW] We may want to check the type coherency
	//       actually, with type tags, it is not possible to check the coherency
	//       so that we should use the mangled names ONLY !
 
	// This prototype refers to an definition outside the module
	// -> look in others modules
	// Note: the global symbol table works at the top scope level only
	if (globalSymbolTable.lookupName(symb.getName())!= null) {
	  // It references a data or a function external to the module, but
	  // internal to the program (defined in an other module)
	  symb.setProgramInternal();
	}
      }
    }

  }

}
