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

/* C bitfield */

package ir.types.c;

import ir.types.Type;

import java.util.HashSet;
import java.util.HashMap;


public class Bitfield extends ArithmeticScalar {

  //==================================================================
  // Private data
  //==================================================================
  Type baseType=null; // _Bool, int or unsigned int

  // Size of the bitfield
  int size_in_bits=1;


  //==================================================================
  // Constructor
  //==================================================================
  public Bitfield(Type t, int s) {
    baseType=t;
    size_in_bits=s;
  }


  //==================================================================
  // Type management
  //==================================================================

  public boolean isIntegralScalar() {return true;}

  public boolean isBitfield() {return true;}

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
    return baseType.promote();
  }


  //==================================================================
  // Getters
  //==================================================================

  //------------------------------------------------------------------
  // getBaseType
  //
  // Returns the base type of the bitfield
  //------------------------------------------------------------------
  public Type getBaseType() {
    return baseType;
  }
 
  //------------------------------------------------------------------
  // getSize
  //
  // Returns the size of the bitfield
  //------------------------------------------------------------------
  public int getSizeInBits() {
    return size_in_bits;
  }
 
  public int getSizeInBytes() {
    return ((size_in_bits-1)>>3)+1;
  }
 

  // Signature: should never be called
  public String getSignature() {
    return null;
  }

  //==================================================================
  // Target Specific information
  //==================================================================
  
  public int sizeof() {
    return baseType.sizeof();
  }

  public int alignof() {
    return baseType.alignof();
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
  public String toStringInternal(HashSet<Type> ts,
				    HashMap<Type,Integer> cs) {
    return dump();
  }

  //------------------------------------------------------------------
  // dump :
  //
  // Returns the original C type syntax
  //------------------------------------------------------------------
  public String dump() {
    StringBuffer buff = new StringBuffer();
    buff.append(((ArithmeticScalar)baseType).dump()+":"+size_in_bits);
    return(buff.toString());
  }

}
