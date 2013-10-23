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

import ir.types.*;
import ir.types.c.Function;

import java.util.LinkedList;




public class MangledFunctionPseudoLabel extends Symbol implements CommonFunctionLabelInterface {

  //------------------------------
  // Private data
  //------------------------------
  private LinkedList<FunctionLabel> mangled_symbol_list = new LinkedList<FunctionLabel>();


  //==================================================================
  // Constructors
  //==================================================================

  public MangledFunctionPseudoLabel(String name) {
    super(name);
  }

  public MangledFunctionPseudoLabel (MangledFunctionPseudoLabel symb) {
    super(symb);

    mangled_symbol_list  = new LinkedList<FunctionLabel>(symb.mangled_symbol_list);
  }


  //==================================================================
  // Mangled symbols manipulation
  //==================================================================

  // Add a mangled symbol
  public void addMangledFunction(FunctionLabel s) {
    if (mangled_symbol_list==null) {
      mangled_symbol_list = new LinkedList<FunctionLabel>();
    }
    mangled_symbol_list.add(s);
  }
  
  // Remove a mangled symbol
  public boolean removeMangledFunction(FunctionLabel s) {
    if (mangled_symbol_list==null) {
      return false;
    }
    mangled_symbol_list.remove(s);
    return true;
  }



  //==================================================================
  // Getters
  //==================================================================

  // Get the list of mangled symbols
  public LinkedList<FunctionLabel> getManglingList() {
    return mangled_symbol_list;
  }

  // Get a mangled symbol equivalents the prototype given in parameter
  public FunctionLabel getEquivalentMangledFunction(Type t_to_check) {
    if (mangled_symbol_list==null) {
      return null;
    }
    for (FunctionLabel s:mangled_symbol_list) {
      if (((Function)s.getType()).isEquivalentForFunctionMangling(t_to_check)) {
	return s;
      }
    }
    return null;
  }



  //==================================================================
  // Verbose functions
  //==================================================================

  //------------------------------------------------------------------
  // toString:
  //
  // Dump the symbol to a string
  //------------------------------------------------------------------
  public String toString() {
    StringBuffer buff = new StringBuffer();

    // Name, id and depth
    buff.append(super.toStringShort());

    buff.append(" ** MANGLED FUNCTION PSEUDO SYMBOL **\n");
    // Not a real symbol, but a list of mangled symbols
    for (Symbol s: mangled_symbol_list) {
      buff.append("          ").append(s.toString()).append("\n");
    }

    // Return the final string
    return(buff.toString());
  }
}

 
