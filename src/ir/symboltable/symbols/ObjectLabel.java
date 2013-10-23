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

/* Symbol designating an object (which can be initialized) */

package ir.symboltable.symbols;

import ir.base.NodeAST;
import ir.symboltable.*;


public abstract class ObjectLabel extends Symbol {

  //==================================================================
  // Constructors
  //==================================================================

  public ObjectLabel(String name) {
    super(name);
  }

  public ObjectLabel(StorageClass sc) {
    super(sc);
  }

  public ObjectLabel(Symbol symb) {
    super(symb);
  }


  //==================================================================
  // Management of initialization
  //==================================================================

  private NodeAST initialization_tnode = null; // Initialization node


  // Sets the head of the initialization tree of the variable
  public void setInitializationNode(NodeAST t) {
    initialization_tnode=t;
  }

  // Returns the head of the initialization tree
  // (returns 'null' if not initialized)
  //-> concerns onyl variables
  public NodeAST getInitializationNode() {
    return(initialization_tnode);
  }


  //==================================================================
  // Query
  //==================================================================

  //------------------------------------------------------------------
  // referencesCompileTimeAllocatedEntity
  //
  // Returns 'true' if the symbol references an object whose address
  // is compile time known. In other words, it must be allocated
  // statically in the heap (not in the stack)
  //------------------------------------------------------------------
  public boolean referencesCompileTimeAllocatedEntity() {
    return
      // The symbol scope is top level, so it references a variable global
      isInTopLevelScope() ||
      // The symbol scope is local to a function for example, but it
      // references a symbol whose address is known at compile time
      isStatic() || isExtern();
  }

}
