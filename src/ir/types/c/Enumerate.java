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

/* C Enumerate type */

package ir.types.c;

import ir.types.Type;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.HashMap;


public class Enumerate extends ArithmeticScalar {

  //==================================================================
  // Private data
  //==================================================================
  private LinkedHashSet<String> elementSet=null;
  private boolean complete=false;
  private String signatureName=null;


  //==================================================================
  // Constructor
  //==================================================================
  public Enumerate() {
    elementSet=new LinkedHashSet<String>();
  }


  //==================================================================
  // Type management
  //==================================================================

  public boolean isEnumerate() {return true;}
  public boolean isIntegralScalar() {return true;}


  //==================================================================
  // Setters
  //==================================================================

  //------------------------------------------------------------------
  // addElement
  //
  // Add an element to the enum.
  // Returns false if this element was already declared
  //------------------------------------------------------------------
  public boolean addElement(String e) {
    return elementSet.add(e);
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
  // getElementSet
  //
  // Returns set of enum elements
  //------------------------------------------------------------------
  public Set<String> getElementSet() {
    return elementSet;
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
  // Conversion Management
  //==================================================================

  //------------------------------------------------------------------
  // promote:
  //
  // Returns the type to which it must be converted in case of
  // promotion.
  //------------------------------------------------------------------
  public Type promote() {
    return IntegerScalar.Tsint;
  }


  //==================================================================
  // Target Specific information
  //==================================================================

  // Consider that it's an int, which is not necessarily true [TBW]
  public int sizeof() {
    return Type.getSourceABI().getIntSize();
  }

  public int alignof() {
    return Type.getSourceABI().getIntAlignment();
  }


  //==================================================================
  // Verbose functions
  //==================================================================

  //------------------------------------------------------------------
  // toStringInternal:
  //
  // Returns string a textual representation of the type. Use 'ts' and
  // 'cs' to avoid displaying multiple times the same type
  // (and avoid cycles)
  //------------------------------------------------------------------
  public String toStringInternal(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    if (!isComplete()) {
      return("enum incomplete");
    }
    else {
      boolean first=true;
      StringBuffer buff = new StringBuffer();
      buff.append("enum {");
      for(String s:elementSet) {
	if (first==false) {
	  buff.append(", ");
	}
	first=false;
	buff.append(s);
      }
      buff.append("}");
      return(buff.toString());
    }
  }

  //------------------------------------------------------------------
  // dump :
  //
  // Returns the original C type syntax
  //------------------------------------------------------------------
  public String dump() {
    if (!isComplete()) {
      return("enum");
    }
    else {
      boolean first=true;
      StringBuffer buff = new StringBuffer();
      buff.append("enum {");
      for(String s:elementSet) {
	if (first==false) {
	  buff.append(", ");
	}
	first=false;
	buff.append(s);
      }
      buff.append("}");
      return(buff.toString());
    }
  }

}
