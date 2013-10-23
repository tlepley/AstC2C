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

/* Generic Literals (mother class of literals) */

package ir.literals;


import ir.symboltable.symbols.*;
import ir.types.*;

import java.util.Vector;

import ir.base.NodeAST;


public abstract class Literal {
  // Type of the current literal
  Type type=null;

  // 'Parents' are symbols necessary to the definition of
  // the literal
  Vector<Symbol> parents  = null;
  NodeAST astNode =null;


  //------------------------------------------------------------------
  // Setters
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // setType
  //
  // Sets the declared type of the literal (can be different from
  // its native type in case of scalar)
  //
  //------------------------------------------------------------------
  protected void setType(Type t) {
    // Sets the type
    type=t;
  }

  //------------------------------------------------------------------
  // setAstNode
  //
  // Sets the AST node corresponding the the literal declaration
  //
  //------------------------------------------------------------------
  public void setAstNode(NodeAST node) {
    astNode=node;
  }

  //------------------------------------------------------------------
  // setParents
  //
  // Sets the parent symbols of the literal
  //
  //------------------------------------------------------------------
  public void setParents(Vector<Symbol> v) {
    parents=v;
  }



  //------------------------------------------------------------------
  // Getters
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // isConstant 
  //
  // Returns 'true' if the literal is a 'compile time' constant
  //
  //------------------------------------------------------------------
  abstract public boolean isConstant();

  //------------------------------------------------------------------
  // getType
  //
  // Gets the declared type of the literal (can be different from
  // its native type in case of scalar)
  //
  //------------------------------------------------------------------
  public Type getType() {
    // Sets the type
    return type;
  }

  //------------------------------------------------------------------
  // setAstNode
  //
  // Returns the AST node corresponding the the literal declaration
  //
  //------------------------------------------------------------------
  public NodeAST getAstNode() {
    return astNode;
  }

  //------------------------------------------------------------------
  // setParents
  //
  // Returns the list of parent symbols of the literal
  //
  //------------------------------------------------------------------
  public Vector<Symbol> getParents() {
    return parents;
  }


  //==================================================================
  // Verbose functions
  //==================================================================

  public String toString() {
    return NodeAST.emitTreeToString(getAstNode());
  }

  public String toStringLong() {
    StringBuffer buff = new StringBuffer();

    // Standard print
    buff.append(toString());

    // Constant
    if (isConstant()) {
      // Compile time constant
      buff.append(", compile-time constant");
    }
    else {
      // Non compile time constant
      buff.append(", Non compile-time constant");
    }

    // 'Parents'
    if (parents==null) {
      buff.append(", [no parent]");
    }
    else {
      int i=0;
      buff.append(", parents=[");
      for(Symbol obj:parents) {
        if (i==0) {
          i=1;
        }
        else {
          buff.append(" ");
        }
        buff.append(obj.getId());
      }
      buff.append("]");
    }

    // Return the final string
    return buff.toString();
  }
}
