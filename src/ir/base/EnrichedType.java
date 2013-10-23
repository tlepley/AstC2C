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


package ir.base;


import ir.types.*;
import ir.symboltable.symbols.*;
import java.math.BigInteger;
import java.util.LinkedList;

// [TBW] Manage constant properties and values for aggregates
//       (struct, array, vectors) to be seen with literals

public class EnrichedType {

  // Symbol families
  public enum ConstantType {
    INTEGRAL,FLOATINGPOINT,LABEL,STRING
  };


  //==================================================================
  // Private data
  //==================================================================
  // data type
  private Type type=null;

  // Constant expressions (value computable at compile time)
  private boolean isConstant=false;
  private ConstantType constantType  = ConstantType.INTEGRAL;  
  private BigInteger   integralValue = null;
  private double       floatingpointValue = 0;


  // For lvalue
  private boolean designateAnObject=false;
  private boolean designateACompileTimeAllocatedObject=false;


  private Symbol  symbolReference=null;
  private NodeAST   idReference=null;

  // For vector element reference
  LinkedList<Integer> vector_element_reference=null;
  boolean vector_element_reference_with_duplicated_element=false;


  //==================================================================
  // Constructor
  //==================================================================
  
  public EnrichedType(Type t) {
    type=t;
  }

  public EnrichedType(EnrichedType et) {
    type		= et.type;

    isConstant		= et.isConstant;
    constantType	= et.constantType;
    integralValue	= et.integralValue;
    floatingpointValue  = et.floatingpointValue;

    // Value of the type
    designateAnObject	= et.designateAnObject;
    designateACompileTimeAllocatedObject = et.designateACompileTimeAllocatedObject;

    symbolReference	= et.symbolReference;
    idReference		= et.idReference;

    if (et.vector_element_reference==null) {
      vector_element_reference=null;
    }
    else {
      vector_element_reference = new LinkedList<Integer>(et.vector_element_reference);
    }
    vector_element_reference_with_duplicated_element = et.vector_element_reference_with_duplicated_element;
  }


  //==================================================================
  // Constant expressions management Setters
  //==================================================================

  //------------------------------------------------------------------
  // setConstantIntegral:
  //
  // sets the enriched type as a constant integral whose value is given
  // in parameter
  //------------------------------------------------------------------
  public void setConstantIntegral(BigInteger l) {
    isConstant=true;
    constantType=ConstantType.INTEGRAL;
    integralValue=l;
  }

  //------------------------------------------------------------------
  // setConstantFloatingpoint :
  //
  // sets the enriched type as a constant floating point whose value
  // is given in parameter
  //------------------------------------------------------------------
  public void setConstantFloatingpoint(double d) {
    isConstant=true;
    constantType=ConstantType.FLOATINGPOINT;
    floatingpointValue=d;
  }

  //------------------------------------------------------------------
  // setConstantLabel :
  //
  // sets the enriched type as a constant value which is not known
  // before link
  //------------------------------------------------------------------
  public void setConstantLabel() {
    isConstant=true;
    constantType=ConstantType.LABEL;
  }

  //------------------------------------------------------------------
  // setConstantString:
  //
  // sets the enriched type as a constant string
  //------------------------------------------------------------------
  public void setConstantString() {
    isConstant=true;
    constantType=ConstantType.STRING;
  }


  //------------------------------------------------------------------
  // setVectorElementReference :
  //
  // sets the enriched type as a reference to vector elements
  // (like v.xy)
  //------------------------------------------------------------------
  public void setVectorElementReference(LinkedList<Integer> l) {
    vector_element_reference=l;
  }

  //------------------------------------------------------------------
  //  setVectorElementReferenceWithDuplication:
  //
  // sets the enriched type as a reference to vector elements which
  // referenced several times the same element (like v.xyzz)
  //------------------------------------------------------------------
  public void setVectorElementReferenceWithDuplication(LinkedList<Integer> l) {
    vector_element_reference=l;
    vector_element_reference_with_duplicated_element=true;
  }



  //==================================================================
  // Constant expressions management Getters
  //==================================================================

  //------------------------------------------------------------------
  // getType:
  //
  // Returns the type
  //------------------------------------------------------------------
  public Type getType() {
    return type;
  }

  //------------------------------------------------------------------
  // isConstantScalar:
  //
  // Returns 'true' if the enriched type is a constant scalar (
  // arithmetic or pointer)
  //------------------------------------------------------------------
  public boolean isConstantScalar() {
    return isConstant;
  }

  //------------------------------------------------------------------
  // isConstantArithmetic:
  //
  // Returns 'true' if the enriched type is a constant arithmetic value
  // (integral or floating point)
  //------------------------------------------------------------------
  public boolean isConstantArithmetic() {
    return isConstant && (
			  (constantType==ConstantType.INTEGRAL) ||
			  (constantType==ConstantType.FLOATINGPOINT)
			  );
  }

  //------------------------------------------------------------------
  // isConstantIntegral :
  //
  // Returns 'true' if the enriched type is a constant integral value
  //------------------------------------------------------------------
  public boolean isConstantIntegral() {
    return isConstant && (constantType==ConstantType.INTEGRAL);
  }

  //------------------------------------------------------------------
  // getConstantIntegralValue :
  //
  // Returns the arithmetic constant value converted to integral BigInteger.
  // Note: this function assumes that the enriched type is a constant
  //       arithmetic value
  //------------------------------------------------------------------
  public BigInteger getConstantIntegralValue() {
    if (constantType==ConstantType.INTEGRAL) {
      return integralValue;
    }
    else {
      return (BigInteger.valueOf((long)floatingpointValue));
    }
  }

  //------------------------------------------------------------------
  // isConstantFloatingpoint :
  //
  // Returns 'true' if the enriched type is a floating point value
  //------------------------------------------------------------------
  public boolean isConstantFloatingpoint() {
    return isConstant && (constantType==ConstantType.FLOATINGPOINT);
  }

  //------------------------------------------------------------------
  // getConstantFloatingpointValue :
  //
  // Returns the arithmetic constant value converted to floating point
  // double.
  // Note: this function assumes that the enriched type is a constant
  //       arithmetic value
  //------------------------------------------------------------------
  public double getConstantFloatingpointValue() {
    if (constantType==ConstantType.FLOATINGPOINT) {
      return floatingpointValue;
    }
    else {
      return integralValue.doubleValue();
    }
  }

  //------------------------------------------------------------------
  // isConstantZero :
  //
  // Returns 'true' if the enriched type is a constant value and if
  // its value is zero
  //------------------------------------------------------------------
  public boolean isConstantZero() {
    if (isConstant) {
      if (constantType==ConstantType.INTEGRAL) {
	return integralValue.compareTo(BigInteger.ZERO)==0;
      }
      else if (constantType==ConstantType.FLOATINGPOINT) {
	return floatingpointValue==0;
      }
      else {
	return false;
      }
    }
    else {
      return false;
    }
  }

  //------------------------------------------------------------------
  // isNonNullConstant :
  //
  // Returns 'true' if the enriched type is a constant value and if
  // its value is zero
  //------------------------------------------------------------------
  public boolean isNonNullConstant() {
    if (isConstant) {
      if (constantType==ConstantType.INTEGRAL) {
	return integralValue.compareTo(BigInteger.ZERO)!=0;
      }
      else if (constantType==ConstantType.FLOATINGPOINT) {
	return floatingpointValue!=0;
      }
      else {
	// We do not know if it is null
	return false;
      }
    }
    else {
      // It is not a constant
      return false;
    }
  }

  //------------------------------------------------------------------
  // isConstantLabel :
  //
  // Returns 'true' if the enriched type is constant value which is
  // not known before link
  //------------------------------------------------------------------
  public boolean isConstantLabel() {
    return isConstant && (constantType==ConstantType.LABEL);
  }

  //------------------------------------------------------------------
  // isConstantString :
  //
  // Returns 'true' if the enriched type is constant value which is
  // string
  //------------------------------------------------------------------
  public boolean isConstantString() {
    return isConstant && (constantType==ConstantType.STRING);
  }




  //==================================================================
  // Lvalue Management
  //==================================================================

  public void setObjectDesignation() {
    designateAnObject=true;
  }

  public void setCompileTimeAllocatedObjectDesignation() {
    designateAnObject=true;
    designateACompileTimeAllocatedObject=true;
  }

  public void setNonObjectDesignation() {
    designateAnObject=false;
    designateACompileTimeAllocatedObject=false;
  }

  //------------------------------------------------------------------
  // designateCompileTimeAllocatedObject
  //
  // Returns 'true' if the symbol references an object whose address
  // is compile time known. In other words, it must be allocated
  // statically in the heap (not in the stack)
  //------------------------------------------------------------------
  public boolean designateCompileTimeAllocatedObject() {
    return designateACompileTimeAllocatedObject;
  }

  public boolean designateAnObject() {
    return designateAnObject==true;
  }

  public void setSymbolReference(Symbol s, NodeAST id) {
   symbolReference=s;
   idReference=id;
  }

  public void setNonSymbolReference() {
    symbolReference=null;
    idReference=null;
  }

  public boolean isSymbolReference() {
    return symbolReference!=null;
  }

  public Symbol getSymbolReference() {
    return symbolReference;
  }

  public NodeAST getIdReference() {
    return idReference;
  }


  //------------------------------------------------------------------
  // isVectorElementReference :
  //
  // Tells if the enriched type as a reference to vector elements
  // (like v.xy)
  //------------------------------------------------------------------
  public boolean isVectorElementReference() {
    return vector_element_reference!=null;
  }
  public LinkedList<Integer> getVectorElementReference() {
    return vector_element_reference;
  }

  //------------------------------------------------------------------
  // isVectorElementReferenceWithDuplication:
  //
  // Teels if the enriched type as a reference to vector elements which
  // references several times the same element (like v.xyzz)
  //------------------------------------------------------------------
  public boolean isVectorElementReferenceWithDuplication() {
    return (vector_element_reference!=null) &&
            vector_element_reference_with_duplicated_element;
  }




  //==================================================================
  // Verbose functions
  //==================================================================

  //------------------------------------------------------------------
  // toString:
  //
  // Returns string a textual representation of the enriched type.
  //------------------------------------------------------------------
  public String toString() {
    // Currently simply dumps the Type
    // [TBW]
    if (type!=null) {
      return type.toString();
    }
    else {
      return "";
    }
  }

}
