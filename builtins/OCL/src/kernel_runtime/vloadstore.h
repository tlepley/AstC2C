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

/* OpenCL vector load/store builtin functions */

#ifndef VLOADSTORE_H
#define VLOADSTORE_H


#define VLOAD_N_AS(type, n, src_as) \
static inline type##n __ocl_vload##n##_##type##_##src_as (size_t offset, const type *src) { \
  type##n dest; \
  int i; const type *p; \
  for (i=0,p=src+(n*offset);i<n;i++,p++) { \
    _VEC_ELEM(dest,i)=*p; \
  } \
  return dest; \
}

#define VSTORE_N_AS(type, n, dest_as) \
static inline void __ocl_vstore##n##_##type##_##dest_as (type##n data, size_t offset, type *dest) { \
  int i; type *p; \
  for (i=0,p=dest+(n*offset);i<n;i++,p++) { \
    *p=_VEC_ELEM(data,i); \
  } \
}

#define VLOAD_N(type, n) \
  VLOAD_N_AS(type,n,global) \
  VLOAD_N_AS(type,n,constant) \
  VLOAD_N_AS(type,n,local) \
  VLOAD_N_AS(type,n,private) 
#define VSTORE_N(type, n) \
  VSTORE_N_AS(type,n,global) \
  VSTORE_N_AS(type,n,local) \
  VSTORE_N_AS(type,n,private)

#define VLOADSTORE_N(type, n) \
  VLOAD_N(type,n) \
  VSTORE_N(type,n)

#define VLOADSTORE(type) \
  VLOADSTORE_N(type,2) \
  VLOADSTORE_N(type,3) \
  VLOADSTORE_N(type,4) \
  VLOADSTORE_N(type,8) \
  VLOADSTORE_N(type,16)

VLOADSTORE(char)
VLOADSTORE(uchar)
VLOADSTORE(short)
VLOADSTORE(ushort)
VLOADSTORE(int)
VLOADSTORE(uint)
VLOADSTORE(float)
VLOADSTORE(long)
VLOADSTORE(ulong)

#endif
