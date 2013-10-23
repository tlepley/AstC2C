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

/* Structure or union type (mother class of struct and union) */

package ir.types.c;

import ir.types.Type;

import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;


abstract public class StructOrUnion extends Type {

  //==================================================================
  // Private data
  //==================================================================
  private boolean complete=false;
  private String signatureName=null;
  private int counter=0;
  private int unnamedCounter=0;
  private LinkedHashMap<String, Integer> fieldMap=new LinkedHashMap<String, Integer>();
  private Vector<Type> typeVector=new Vector<Type>();



  //==================================================================
  // Type management
  //==================================================================

  public boolean isStructOrUnion() {return true;}
  public boolean isAggregate() {return true;}




  //==================================================================
  // Setters
  //==================================================================

  //------------------------------------------------------------------
  // addField
  //
  // Add a field to the tag. Returns 'true' if the field is new in
  // the struct/union or 'false' if this field was already declared
  //------------------------------------------------------------------
  public boolean addField(String e, Type t) {
    if (fieldMap.put(e,counter)==null) {
      typeVector.add(t);
      counter++;
      return true;
    }
    else {
      return false;
    }
  }

  //------------------------------------------------------------------
  // addUnnamedField
  //
  // Add an unnamed (bit)field to the tag
  //------------------------------------------------------------------
  public void addUnnamedField(Type t) {
    fieldMap.put("!"+unnamedCounter++,counter);
    typeVector.add(t);
    counter++;
  }

  //------------------------------------------------------------------
  // setComplete
  //
  // Sets the tag as complete
  //------------------------------------------------------------------
  public void setComplete() {
    complete=true;
  }

  //------------------------------------------------------------------
  // setSignature
  //
  // Sets the signature of the tag (used to create function prototype
  // signatures). In case of type tags, the signature may depend
  // on the declaration name
  //------------------------------------------------------------------
  public void setSignatureName(String s) {
    signatureName=s;
  }


  //==================================================================
  // Getters
  //==================================================================

  //------------------------------------------------------------------
  // getFieldType
  //
  // Returns the type of the field named 's' in the struct/union
  // This function returns 'null' if such a field does not exist.
  //------------------------------------------------------------------
  public Type getFieldType(String s) {
    Integer i;

    i=fieldMap.get(s);
    if (i==null) {
      // No such a field
      return null;
    }
    else {
      // Return the corresponding type
      return typeVector.get(i);
    }
  }

  //------------------------------------------------------------------
  // getFieldType
  //
  // Returns the type of the ith field of the struct/union
  // This function returns 'null' if such a field does not exist.
  //------------------------------------------------------------------
  public Type getFieldType(int i) {
    if ((i<0)||(i>=counter)) {
      return null;
    }
    // Return the corresponding type
    return typeVector.get(i);
  }


  //------------------------------------------------------------------
  // getFieldNumber
  //
  // Returns the index number of the field named 's' in the
  // struct/union
  // This function returns -1 if such a field does not exist.
  //------------------------------------------------------------------
  public int getFieldNumber(String s) {
    Integer i;

    i=fieldMap.get(s);
    if (i==null) {
      // No such a field
      return -1;
    }
    else {
      return i;
    }
  }

  //------------------------------------------------------------------
  // getNbFields
  //
  // Returns the number of fields declared in its body
  //------------------------------------------------------------------
  public int getNbFields() {
    return counter;
  }

  //------------------------------------------------------------------
  // hasEmptyBody
  //
  // Returns true is the structure or union has no field declared
  // in its body
  //------------------------------------------------------------------
  public boolean hasEmptyBody() {
    return counter==0;
  }

  //------------------------------------------------------------------
  // isComplete
  //
  // Returns true if the structure or union is complete
  //------------------------------------------------------------------
  public boolean isComplete() {
    return(complete==true);
  }

  //------------------------------------------------------------------
  // getSignature
  //
  // Returns the signature of the tag (used to create function 
  // prototype signatures)
  //------------------------------------------------------------------
  public String getSignature() {
    return ""+signatureName.length()+getElementType();
  }

  //==================================================================
  // Target Specific information
  //==================================================================

  //------------------------------------------------------------------
  // sizeofAllFields
  //
  // Returns the global size of the aggregate if we consider the
  // fields are located one behind this other one. This function takes
  // into consideration the aligment constraints
  // Note: this function is used to compute the size of a 'struct'
  //------------------------------------------------------------------
  public int sizeofAllFields() {
    int size=0;

    // For bitfield
    boolean previousBitfield=false;
    int base_size_in_bits=0;
    int subsize_in_bits=0;

    for(Type fieldType:typeVector) {
      if (fieldType.isBitfield()) {
	// More target specific information requested here to manage
	// bitfields [TBW]. In the C2C, we arbitrary decide not to
	// put a bitfield over two different containers

	Bitfield the_type=(Bitfield)fieldType;
	Type base_type=the_type.getBaseType();

	if ( (!previousBitfield) ||
	     ((subsize_in_bits+the_type.getSizeInBits())>base_size_in_bits) ) {

	  // -> We create a new container
	  // Align the container
	  if (size!=0) {
	    size=(((size-1)/base_type.alignof())+1)*base_type.alignof();
	  }
	  // Add the container size to the global size
	  size+=base_type.sizeof();
	  
	  // Set the occupied size in the current container
	  base_size_in_bits=base_type.sizeof()<<3;
	}

	if (the_type.getSizeInBits()==0) {
	  // Move to the next container
	  previousBitfield=false;
	}
	else {
	  // Set the occupied size in the current container
	  subsize_in_bits+=the_type.getSizeInBits();
	  
	  // This is a bitfield
	  previousBitfield=true;
	}
      }
      else {
	// Align the field
	if (size!=0) {
	  size=(((size-1)/fieldType.alignof())+1)*fieldType.alignof();
	}
	// Add the field size
	size+=fieldType.sizeof();

	// Not a bitfield
	previousBitfield=false;
      }
    }


    // Adds padding for good array alignment
    if (size!=0) {
      size=(((size-1)/alignof())+1)*alignof();
    }

    return size;
  }

  //------------------------------------------------------------------
  // maxSizeofFields
  //
  // Returns the size of the bigger fields
  // Note: this function is used to compute the size of an 'union'
  //------------------------------------------------------------------
  public int maxSizeofFields() {
    int size=0;

    for(Type fieldType:typeVector) {
      if (fieldType.sizeof()>size) {
	size=fieldType.sizeof();
      }
    }
    return size;
  }

  //------------------------------------------------------------------
  // alignof
  //
  // Returns the larget aligment constraint of fields 
  // Note: this function is used to compute the alignment constraint
  // of an 'union'
  //------------------------------------------------------------------
  public int alignof() {
    int align=1;

    for(Type fieldType:typeVector) {
      if (fieldType.alignof()>align) {
	align=fieldType.alignof();
      }
    }
    return align;
  }



  //==================================================================
  // Verbose functions
  //==================================================================

  //------------------------------------------------------------------
  // getTreeSet:
  //
  // Function allows detecting multiple type reference in the type
  // tree. It is in particular useful to manage the possible
  // struct/union cycles. 'ts' holds type node encountered and 'cs'
  // holds multiply encountered type nodes.
  //------------------------------------------------------------------
  public void getTreeSet(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    if (ts.contains(this)) {
      cs.put(this,incrementIdCounter());
    }
    else {
      ts.add(this);
      for(Type fieldType:typeVector) {
	fieldType.getTreeSet(ts,cs);
      }
    }
  }

  //------------------------------------------------------------------
  // toStringInternal:
  //
  // Returns string a textual representation of the type. Use 'ts' and
  // 'cs' to 'ts' to avoid displaying multiple times the same type
  // (and avoid cycles)
  //------------------------------------------------------------------
  public String toStringInternal(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    if (!isComplete()) {
      return("incomplete");
    }
    else {
      StringBuffer buff = new StringBuffer();

      if (cs.containsKey(this)) {
	// Write this complex type only once
	// (also potentially involved in a type cycle)
	if (ts.contains(this)) {
	  // Occurence already occured, just reference it
	  return("S" + cs.get(this));
	}
	// Name it
	ts.add(this);
	buff.append("S" + cs.get(this)+" ");
      }

      boolean first=true;
      buff.append("{");
      for(String s:fieldMap.keySet()) {
	if (first==false) {
	  buff.append(" ");
	}
	first=false;
	buff.append(s).append(":");
	buff.append(typeVector.get(fieldMap.get(s)).toStringInternal(ts,cs)).append(";");
      }
      buff.append("}");
      return(buff.toString());
    }
  }

}
