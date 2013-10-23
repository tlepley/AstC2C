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

/* Complex sub-literal of a vector literal (which can only be a vector) */

package ir.literals.ocl;


import ir.base.EnrichedType;
import ir.literals.Literal;
import ir.types.ocl.Vector;


public class VectorSubLiteral extends Literal {
  //------------------------------
  // Private data
  //------------------------------
  EnrichedType etype=null;

  //==================================================================
  // Constructor
  //==================================================================
  public VectorSubLiteral(Vector vec) {
    setType(vec);
  }


  //------------------------------------------------------------------
  // Setters
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // setEnrichedType  
  //
  // Sets the enriched type of the literal expression. This enriched
  // enriched type holds the native type of the literal expression
  // and its possible constant value
  //
  //------------------------------------------------------------------
  public void setEnrichedType(EnrichedType et) {
    etype=et;
  }


  //------------------------------------------------------------------
  // Getter
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // isConstant 
  //
  // Returns 'true' if the literal is a 'compile time' constant
  //
  //------------------------------------------------------------------
  public boolean isConstant() {
    if (etype==null) {
      // Should never happend
      return true;
    }

    // [TBW] See see with EnrichedTypes
    //return etype.isConstantScalar();
    return false;
  }

  //------------------------------------------------------------------
  // getEnrichedType  
  //
  // Gets the enriched type of the literal expression. This enriched
  // enriched type holds the native type of the literal expression
  // and its possible constant value
  //
  //------------------------------------------------------------------
  public EnrichedType getEnrichedType() {
    return etype;
  }


  //==================================================================
  // Verbose functions
  //==================================================================

  // public String toString()
  // Use the standard Literal 'toString' function which dumps the AST, because
  // it can be an expression



}
