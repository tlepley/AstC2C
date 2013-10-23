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

/* Walk over the AST Tree corresponding to the OpenCL C language and
   build the symbol table with its associated type table.
   + It propagates types over expression
   + It performs checks related to symbols and type
   + It propagate constant values over expressions
   + It decorates ID nodes with a reference to the corresponding
     symbol and type
*/

package parser;

import ir.symboltable.*;
import common.CompilerError;
import parser.SymbolTableBuilder;
import antlr.collections.AST;


public class OclSymbolTableBuilder extends SymbolTableBuilder {

  public OclSymbolTableBuilder(CompilerError cp) {
    super(cp);
  }

  // ******************************************************************
  // run :
  //
  // Runs the tree parser builds a symbol table, and annotates the AST
  // with references to the symbol table
  // ******************************************************************
  public void run(AST tree, SymbolTable st) {
    setOclLanguage();
    symbolTable=st;
    run(tree);
  }

  // ******************************************************************
  // runSplit :
  //
  // Runs the tree parser builds a symbol table, and annotates the AST
  // with references to the symbol table
  // ******************************************************************
  public void runSplit(AST tree, SymbolTable st) {
    setOclLanguage();
    symbolTable=st;
    runSplit(tree);
  }

}

