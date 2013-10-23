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

/* Aggregate Literals (vector, array, struct, union) */


package ir.literals.c;


import ir.literals.Literal;
import ir.types.*;
import ir.types.c.Array;
import ir.types.c.StructOrUnion;
import ir.types.ocl.Vector;


public class AggregateLiteral extends Literal {

  //------------------------------
  // Private data
  //------------------------------
  private boolean isConstant=true;

  // Elements management
  private java.util.Vector<Literal> elementVector=null;
  private int index=0;
  private int size=0;


  //==================================================================
  // Constructor
  //==================================================================

  public AggregateLiteral(Type t) {
    int alloc_size;

    // Sets the declared type
    setType(t);
    
    // The type here is necessarily unqualified (StrutOrUnion or Array)
    if ((t.isStructOrUnion()) && (t.isComplete())) {
      size=((StructOrUnion)t).getNbFields();
      alloc_size=size;
    }
    else if ( (t instanceof Array) && (((Array)(t)).hasSizeDefined()) ) {
      size=((Array)t).getNbElements();
      alloc_size=size;
    }
    else if (t instanceof Vector) {
      size=((Vector)t).getNbElements();
      alloc_size=size;
    }
    else {
      // Uncomplete aggregate, no size defined (yet)
      size=0;
      alloc_size=20;
    }

    // Allocate and initialize the vector
    elementVector=new java.util.Vector<Literal>(alloc_size);
    elementVector.setSize(size);
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
    if (index>=size) {
      // The vector must be resized
      size=index+1;
      elementVector.setSize(size);
    }
    elementVector.setElementAt(l,index);

    // Sets the new index
    index++;

    // Propagate the constant property
    if (!l.isConstant()) {
      isConstant=false;
    }
  }


  //------------------------------------------------------------------
  // addAtIndex :
  //
  // Add an elements at a specified index
  //
  //------------------------------------------------------------------
  public void addAtIndex(int new_index, Literal l) {
    index=new_index;
    add(l);
  }


  //------------------------------------------------------------------
  // addAtIndexRange :
  //
  // Add a new element to the specified index range
  //
  //------------------------------------------------------------------
  public void addAtIndexRange(int inf, int sup, Literal l) {
    if (sup>=size) {
      // The vector must be resized
      size=sup+1;
      elementVector.setSize(size);
    }

    // Put the value for the whole range
    int i;
    for(i=inf;i<=sup;i++) {
     elementVector.setElementAt(l,i);
    }

    // Sets the new index
    index=sup+1;

    // Propagate the constant property
    if (!l.isConstant()) {
      isConstant=false;
    }
  }



  //------------------------------------------------------------------
  // Getters
  //------------------------------------------------------------------

  //------------------------------------------------------------------
  // isConstant :
  //
  // Returns 'true' if the literal is a 'compile time' constant
  //
  //------------------------------------------------------------------
  public boolean isConstant() {
    return isConstant;
  }

  //------------------------------------------------------------------
  // getSize : 
  //
  // Returns the (current for uncomplete arrays) size of the aggregate
  //
  //------------------------------------------------------------------
  public int getSize() {
    return size;
  }


  //------------------------------------------------------------------
  // getAtIndex :
  //
  // Add an elements at a specified index
  //
  //------------------------------------------------------------------
  public Literal getAtIndex(int i) {
    if (i>=size) {
      // Should never occur
      return null;
    }
    return elementVector.elementAt(0);
  }




  //==================================================================
  // Verbose functions
  //==================================================================

  public String toString() {
    int i;
    Literal l;
    StringBuffer buff = new StringBuffer();
    
    buff.append("{");
    for(i=0;i<size;i++) {
      if (i!=0) {
	buff.append(", ");
      }
      l=elementVector.get(i);
      if (l==null) {
	buff.append("<null>");
      }
      else {
	buff.append(l.toString());
      }
    }
    buff.append("}");

    return buff.toString();
  }

}
