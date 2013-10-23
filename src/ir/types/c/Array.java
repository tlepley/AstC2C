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

/* C Array type */

package ir.types.c;

import ir.types.Type;

import java.util.HashSet;
import java.util.HashMap;

public class Array extends ChildType {
  //==================================================================
  // Private data
  //==================================================================

  boolean hasSizeDefined=false;
  boolean hasConstantSize=false;
  int nbElements = 0;

  //==================================================================
  // Constructor
  //==================================================================
  public Array(Type of) {
    super(of);
  }


  //==================================================================
  // Type management
  //==================================================================
  public boolean isArray() {return true;}
  public boolean isAggregate() {return true;}

  // Specific to Qualifier:
  // An array is never qualified by himself, only its elements can be
  // qualified
  public boolean isQualified() {return getChild().isQualified();}
  public Qualifier getQualifier() {return getChild().getQualifier();}
  public boolean isConstQualified() {return getChild().isConstQualified();}
  public boolean isVolatileQualified() {return getChild().isVolatileQualified();}
  public boolean isRestrictQualified() {return getChild().isRestrictQualified();}
  public boolean isAddressSpaceQualified() {return getChild().isAddressSpaceQualified();}
  public boolean isConstantAddressSpaceQualified() {return getChild().isConstantAddressSpaceQualified();}
  public boolean isGlobalAddressSpaceQualified() {return getChild().isGlobalAddressSpaceQualified();}
  public boolean isLocalAddressSpaceQualified() {return getChild().isLocalAddressSpaceQualified();}
  public boolean isPrivateAddressSpaceQualified() {return getChild().isPrivateAddressSpaceQualified();}


  //==================================================================
  // Setters
  //==================================================================
 
  public void setVariableSize() {
    hasSizeDefined=true;
    hasConstantSize=false;
  }

  public void setNbElements(int i) {
    hasSizeDefined=true;
    hasConstantSize=true;
    nbElements=i;
  }


  //==================================================================
  // Getters
  //==================================================================

  public boolean hasSizeDefined() {
    return hasSizeDefined;
  }

  public boolean isDynamic() {
    return hasSizeDefined && (!hasConstantSize);
  }

  public boolean hasConstantSize() {
    return hasSizeDefined && hasConstantSize;
  }

  public int getNbElements() {
    return nbElements;
  }

  public boolean isComplete() {
    // We should check also that the size is constant [TBW]
    // leave it like this until sizeof is not managed as a constant
    if ((!hasSizeDefined) || (!getElementType().isComplete())) {
      return false;
    }
    return true;
  }

  public boolean hasCompleteElement() {
    return getElementType().isComplete();
  }


  //------------------------------------------------------------------
  // getElementType
  //
  // Returns type of array elements
  //------------------------------------------------------------------
  public Type getElementType() {
    return getChild();
  }


  //==================================================================
  // Signature management (for arguments of function prototypes)
  // Should never occur since arrays as function arguments are
  // transformed into pointer
  //==================================================================

  public String getSignature() {
    // Note: as function parameter, an array is equivalent to a pointer
    return "P" + getElementType().getSignature();

    // return "A" + getNbElements()+ "_" + getElementType().getSignature();
  }


  //==================================================================
  // Target Specific information
  //==================================================================

  // Relevant only if complete
  public int sizeof() {
    return nbElements*getElementType().sizeof();
  }

  // Relevant only if complete
  public int alignof() {
    return getElementType().alignof();
  }


  //==================================================================
  // Compatibility checks
  //==================================================================
  
  //------------------------------------------------------------------
  // isEquivalentForVariableAndArrayDeclaration :
  //
  // This function checks the compatibility of types in the context
  // of two variable/array declarations
  //------------------------------------------------------------------
  public boolean isEquivalentForVariableAndArrayDeclaration(Type t) {
    if (t.isArray()) {
      return getElementType().isEquivalentForVariableAndArrayDeclaration(t.getElementType());
    } 
    else {
      // Qualified or other type
      return false;
    }
  }

  //------------------------------------------------------------------
  // isEquivalentForFunctionDeclaration :
  //
  // This function checks the compatibility of types in the context
  // of two function prototypes declaration
  //------------------------------------------------------------------
  public boolean isEquivalentForFunctionDeclaration(Type t) {
    if (t.isAddressSpaceQualified()) {
      return false;
    }
    Type tu=t.unqualify();

    // Should be an array
    if (!tu.isArray()) {
      return false;
    }

    // Check array size
    if (getNbElements()!=((Array)tu).getNbElements()) {
      System.err.println("size "+getNbElements() + "!=" + ((Array)tu).getNbElements());
      return false;
    }

    // Check element type compatibility
    return getElementType().isEquivalentForFunctionDeclaration(tu.getElementType());
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
    StringBuffer buff = new StringBuffer();
    buff.append("array[");
    if (hasSizeDefined()) {
      if (hasConstantSize()) {
	buff.append(nbElements);
      }
      else {
	buff.append("<n>");
      }
    }

    buff.append("] of {").append(getElementType().toStringInternal(ts,cs)).append("}");
    return(buff.toString());
  }
}
