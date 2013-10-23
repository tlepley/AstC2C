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

/* OpenCL ABI information */

package abi;

import ir.types.c.IntegerScalar;

import java.math.BigInteger;

public class OCL_ABI implements ABI {

  public static final OCL_ABI abi = new OCL_ABI();

  static final BigInteger INT_MAX    = BigInteger.ONE.shiftLeft(31).subtract(BigInteger.ONE);
  static final BigInteger UINT_MAX   = BigInteger.ONE.shiftLeft(32).subtract(BigInteger.ONE);
  static final BigInteger LONG_MAX   = BigInteger.ONE.shiftLeft(63).subtract(BigInteger.ONE);
  static final BigInteger ULONG_MAX  = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
  static final BigInteger LLONG_MAX  = BigInteger.ONE.negate();
  static final BigInteger ULLONG_MAX = BigInteger.ONE.negate();

  // Long types support
  public boolean isLongLongAllowed()   {return false;}
  public boolean isDoubleAllowed()     {return false;}
  public boolean isLongDoubleAllowed() {return false;}

  // Sizes in bytes
  public int getCharSize()       { return  1; }
  public int getShortSize()      { return  2; }
  public int getIntSize()        { return  4; }
  public int getLongSize()       { return  8; }
  public int getLonglongSize()   { return -1; }
  public int getFloatSize()      { return  4; }
  public int getDoubleSize()     { return -1; }
  public int getLongdoubleSize() { return -1; }
  public int getPointerSize()    { return  4; }

  // Limits
  public BigInteger getINT_MAX()    { return INT_MAX; }
  public BigInteger getUINT_MAX()   { return UINT_MAX; }
  public BigInteger getLONG_MAX()   { return LONG_MAX; }
  public BigInteger getULONG_MAX()  { return ULONG_MAX; }
  public BigInteger getLLONG_MAX()  { return LLONG_MAX; }
  public BigInteger getULLONG_MAX() { return ULLONG_MAX; }

  // Alignment in bytes
  public int getCharAlignment()       { return 1; }
  public int getShortAlignment()      { return 2; }
  public int getIntAlignment()        { return 4; }
  public int getLongAlignment()       { return 4; }
  public int getLonglongAlignment()   { return 8; }
  public int getFloatAlignment()      { return 4; }
  public int getDoubleAlignment()     { return 8; }
  public int getLongdoubleAlignment() { return 4; }
  public int getPointerAlignment()    { return 4; }

  // Equivalence. Not relevant here since arithmetic types are platform 
  // independents => return int
  public IntegerScalar getEquivalent_size_t()    { return IntegerScalar.Tuint; }
  public IntegerScalar getEquivalent_ptrdiff_t() { return IntegerScalar.Tsint; }
  public IntegerScalar getEquivalent_intptr_t()  { return IntegerScalar.Tsint; }
  public IntegerScalar getEquivalent_uintptr_t() { return IntegerScalar.Tuint; }
}
