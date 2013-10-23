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

/* C integer floating point types */

package ir.types.c;

import ir.types.Type;

import java.util.HashMap;
import java.util.HashSet;

public class FloatingPointScalar extends ArithmeticScalar {

  // Integral floating point types
  public static final FloatingPointScalar Tfloat      = new FloatingPointScalar(FloatingType.FLOAT);
  public static final FloatingPointScalar Tdouble     = new FloatingPointScalar(FloatingType.DOUBLE);
  public static final FloatingPointScalar Tlongdouble = new FloatingPointScalar(FloatingType.LONG_DOUBLE);

  // Specifiers
  public enum FloatingType {
    FLOAT, DOUBLE, LONG_DOUBLE
  };


  //==================================================================
  // Private data
  //==================================================================
  private FloatingType baseType=null;


  //==================================================================
  // Private constructor
  //==================================================================
  private FloatingPointScalar(FloatingType t) {
    baseType=t;
  }


  //==================================================================
  // Type class generic methods
  //==================================================================

  public boolean isFloatingPointScalar() {return true;}
  public boolean isFloatScalar() {return isFloat();}
  public boolean isDoubleScalar() {return isDouble();}
  public boolean isLongDoubleScalar() {return isLongDouble();}


  //==================================================================
  // Getters
  //==================================================================

  //------------------------------------------------------------------
  // getBaseType
  //
  // Returns base type of the floating point scalar type
  //------------------------------------------------------------------
  public FloatingType getBaseType() {
    return baseType;
  }
  public boolean isFloat() {return baseType==FloatingType.FLOAT;}
  public boolean isDouble() {return baseType==FloatingType.DOUBLE;}
  public boolean isLongDouble() {return baseType==FloatingType.LONG_DOUBLE;}


  //==================================================================
  // Signature management (for arguments of function prototypes)
  //==================================================================

  public String getSignature() {
    switch(baseType) {
    case FLOAT:
      return "f";
    case DOUBLE:
      return "d";
    case LONG_DOUBLE:
      return "e";
    }
    // Should never occur
    return null;
  }


  //==================================================================
  // Target Specific information
  //==================================================================

  public int sizeof() {
    switch (baseType) {
    case FLOAT:
      return Type.getSourceABI().getFloatSize();
    case DOUBLE:
      return Type.getSourceABI().getDoubleSize();
    case LONG_DOUBLE:
      return Type.getSourceABI().getLongdoubleSize();
    default:
      // Error
      return 0;
    }
  }

  public int alignof() {
    switch (baseType) {
    case FLOAT:
      return Type.getSourceABI().getFloatAlignment();
    case DOUBLE:
      return Type.getSourceABI().getDoubleAlignment();
    case LONG_DOUBLE:
      return Type.getSourceABI().getLongdoubleAlignment();
    default:
      // Error
      return 0;
    }
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

    switch (baseType) {
    case FLOAT: buff.append("float"); break;
    case DOUBLE: buff.append("double"); break;
    case LONG_DOUBLE: buff.append("long double"); break;
    default:
      // Internal error
    }

    return(buff.toString());
  }

  //------------------------------------------------------------------
  // dump :
  //
  // Returns the original type syntax
  //------------------------------------------------------------------
  public String dump() {
    switch (baseType) {
    case FLOAT: return "float";
    case DOUBLE: return "double";
    case LONG_DOUBLE: return "long double";
    default:
      // Internal error
      return null;
    }
  }

}
