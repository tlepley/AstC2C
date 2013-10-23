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

/* Source level linker used in conjunction with data extraction at
   symbol table creation time */

package ir.symboltable;


import ir.symboltable.symbols.*;
import ir.base.NodeAST;
import common.CompilerError;

import java.util.ArrayList;

import parser.SymbolTableBuilder;


public class ExtractionLinker extends Linker {

  // ##################################################################
  // List of instance data
  // ##################################################################
  ArrayList<ObjectLabel> globalInstanceDataList=new ArrayList();

  //------------------------------------------------------------------
  //  getInstanceData  
  //------------------------------------------------------------------
  public ArrayList<ObjectLabel> getInstanceData() {
    return globalInstanceDataList;
  }

  //------------------------------------------------------------------
  //  getInitializedInstanceData  
  //------------------------------------------------------------------
  public ArrayList<ObjectLabel> getInitializedInstanceData() {
    ArrayList<ObjectLabel> l=new ArrayList<ObjectLabel>();

    for(ObjectLabel symb : globalInstanceDataList) {
      ObjectLabel ol=(ObjectLabel)symb;
      if (ol.getInitializationNode() != null) {
	l.add(ol);
      }
    }

    return l;
  }



  // ##################################################################
  // Building
  // ##################################################################

  // Constructor
  public ExtractionLinker(boolean nc, CompilerError ce) {
    super(nc,ce);
  }

  
  //------------------------------------------------------------------
  //  processInstanceData
  //
  //  Performs the link for function and data symbols with a scope
  //  global to the program (the program is the link unit)
  //  => Sets the 'ProgramInternal' information to program scope which
  //     correspond to objects internal the program
  //
  //------------------------------------------------------------------
  public void processInstanceData(Iterable<ObjectLabel> instance_data_list,
				  Iterable<ObjectLabel> extern_data_list) {

    // Process instance data declarations
    //-----------------------------------
    for (ObjectLabel symb : instance_data_list) {

      // It is an 'object' internal to the program
      symb.setProgramInternal();
      
      // Update the global symbol table with objects global to the program
      // exported and static

      String symbName = symb.getName();

      // Look if such a symbol already exists in the global symbol table
      // Note: the global symbol table works at the top scope level only
      Symbol symb_old = globalSymbolTable.lookupName(symbName);

      if (symb_old != null) {
	//==========================================================
	// The symbol already exists
	//==========================================================

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
	globalSymbolTable.addObjectLabel(symbName, symb, globalCompilerError);
	globalInstanceDataList.add(symb);
      }
    }

    // Process extern data declarations
    //---------------------------------
     for (ObjectLabel symb : extern_data_list) {
      if (globalSymbolTable.lookupName(symb.getName()) != null) {
	// It's the external declaration of an instance data
	symb.setProgramInternal();
	// No need for the extern declaration, nullify it in the AST
	NodeAST decl = symb.getDeclarationNode();
	decl.setType(SymbolTableBuilder.NNoDeclaration);
	decl.setFirstChild(null);
      }
    }

  }


  //------------------------------------------------------------------
  //  processFunction  
  //
  //  Performs the link for functions definitions and prototypes
  //  => Sets the 'programInternal' information to symbol which
  //     correspond to objects internal to the program
  //
  //------------------------------------------------------------------
  public void processFunction() {
    int i;

    // Process function definitions of all files
    //------------------------------------------
    for(i=0;i<nbSymbolTable;i++) {
      SymbolTable st= symbolTableArrayList.get(i);

      ArrayList<FunctionLabel> funcdef_list =
	st.getListOfFunctionDefinitionLabels();

      for (FunctionLabel symb_def : funcdef_list) {
	// It is an 'object' internal to the component
	symb_def.setProgramInternal();

	String symbName = symb_def.getName();	
	if (!symb_def.isModuleVisibility()) {
	  // [TBW] actually, with type tags, it is not possible to check the
	  // the equivalence of functions through types.
	  // Here, we should use the mangled function name ONLY !
 
	  globalSymbolTable.addFunction(symbName, symb_def, true, globalCompilerError);
	}
      }
    }


    // Process function prototypes of all files
    //-----------------------------------------
    for(i=0;i<nbSymbolTable;i++) {
      SymbolTable st= symbolTableArrayList.get(i);

      ArrayList<FunctionLabel> proto_list =
	st.getListOfFunctionPrototypeLabels();
      for (FunctionLabel symb : proto_list) {
	// [TBW] We may want to check the type coherency
	//       actually, with type tags, it is not possible to check the coherency
	//       so that we should use the mangled names ONLY !

	// This prototype refers to an definition outside the module
	// -> look in others modules
	if (globalSymbolTable.lookupName(symb.getName())!= null) {
	  // It is the prototype of a function external to the module, but
	  // internal to the component
	  symb.setProgramInternal();
	}
      }
    }

  }


}
