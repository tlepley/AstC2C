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

/* Vector */

package ir.literals.ocl;


import ir.literals.Literal;
import ir.literals.c.AggregateLiteral;
import ir.literals.c.ExprLiteral;
import ir.types.ocl.Vector;


public class VectorLiteral extends AggregateLiteral {

  private int nb_def_elements=0;

  //==================================================================
  // Constructor
  //==================================================================

  public VectorLiteral(Vector t) {
    // Initialize the aggregate 
    super(t);
  }

  //------------------------------------------------------------------
  // Setters
  //------------------------------------------------------------------


  //------------------------------------------------------------------
  // add :
  //
  // Add a new element at the current index and increment this index
  //
  //------------------------------------------------------------------
  public void add(Literal l) {
    nb_def_elements++;
    super.add(l);
  }

  //------------------------------------------------------------------
  // addAtIndex :
  //
  // Add an elements at a specified index
  //
  //------------------------------------------------------------------
  public void addAtIndex(int new_index, Literal l) {
    nb_def_elements++;
    
    super.addAtIndex(new_index,l);
  }

  //------------------------------------------------------------------
  // addAtIndexRange :
  //
  // Add a new element to the specified index range
  //
  //------------------------------------------------------------------
  public void addAtIndexRange(int inf, int sup, Literal l) {
    if (sup>=inf) {
      nb_def_elements+=sup-inf+1;
    }

    super.addAtIndexRange(inf,sup,l);
  }


  //------------------------------------------------------------------
  // Getters
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // getNbDefinitionElements  : 
  //
  // Returns the number of elements given in the definition of the
  // literal. This number may differ from the 'size' in the case of
  // vectors.
  // Ex: (int4)(1,2,(int2)(3,4)) has a size of 4 but 3*elements in
  //     the definition
  //
  //------------------------------------------------------------------
  public int getNbDefinitionElements() {
    return nb_def_elements;
  }

  //------------------------------------------------------------------
  // isScalarDefined : 
  //
  // Returns true is the vector literal is defined with one unique
  // scalar
  // Ex: (int4)(1)
  //
  //------------------------------------------------------------------
  public boolean isScalarDefined() {
    if (nb_def_elements==1) {
      if (getAtIndex(0) instanceof ExprLiteral) {
	return true;
      }
    }
    return false;
  }

  //------------------------------------------------------------------
  // isComplexDefined : 
  //
  // Returns true is the vector literal is defined with at lease
  // a sub-vector
  // Ex: (int4)(1, 2, (int2)(3,4))
  //
  //------------------------------------------------------------------
  public boolean isComplexDefined() {
    if ((nb_def_elements<getSize()) && (!isScalarDefined())) {
      return true;
    }
    return false;
  }


  //==================================================================
  // Verbose functions
  //==================================================================
  public String toString() {
    return "Vector = "+ super.toString();

  }
}
