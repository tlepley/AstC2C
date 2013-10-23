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

/* OpenCL miscellaneous functions */

#ifndef MISCELLANEOUS_H
#define MISCELLANEOUS_H


/* shuffle */

#define SHUFFLE_MN(type, m, utype, n)				\
static inline type##n __ocl_shuffle_##type##m##_##utype##n (type##m x, utype##n mask) { \
  type##n s; \
  int i; \
  for (i=0;i<n;i++) { \
    int elem = _VEC_ELEM(mask,i) & (m-1); \
    _VEC_ELEM(s,i) = _VEC_ELEM(x,elem); \
  } \
  return s; \
}

#define SHUFFLE_3N(type, utype, n)				\
static inline type##n __ocl_shuffle_##type##3_##utype##n (type##3 x, utype##n mask) { \
  type##n s; \
  int i; \
  for (i=0;i<n;i++) { \
    int elem=_VEC_ELEM(mask,i) & 0x3; \
    if (elem==3) elem=0; \
    _VEC_ELEM(s,i) = _VEC_ELEM(x,elem); \
  } \
  return s; \
}

#define SHUFFLE_N(type,utype,n) \
  SHUFFLE_MN(type,2,utype,n)		  \
  SHUFFLE_3N(type,utype,n)		  \
  SHUFFLE_MN(type,4,utype,n)		  \
  SHUFFLE_MN(type,8,utype,n)		  \
  SHUFFLE_MN(type,16,utype,n)

#define SHUFFLE(type,utype) \
  SHUFFLE_N(type,utype,2)		  \
  SHUFFLE_N(type,utype,3)		  \
  SHUFFLE_N(type,utype,4)		  \
  SHUFFLE_N(type,utype,8)		  \
  SHUFFLE_N(type,utype,16)

SHUFFLE(char,uchar)
SHUFFLE(uchar,uchar)
SHUFFLE(short,ushort)
SHUFFLE(ushort,ushort)
SHUFFLE(int,uint)
SHUFFLE(uint,uint)
SHUFFLE(long,ulong)
SHUFFLE(ulong,ulong)
SHUFFLE(float,uint)

  
/* shuffle2 */

#define SHUFFLE2_MN(type, m, utype, n)				\
static inline type##n __ocl_shuffle2_##type##m##_##utype##n (type##m x, type##m y, utype##n mask) { \
  type##n s; \
  int i; \
  for (i=0;i<n;i++) { \
    int elem = _VEC_ELEM(mask,i) & ( (m<<1) - 1 );	\
    _VEC_ELEM(s,i) = (elem<m)? _VEC_ELEM(x,elem):_VEC_ELEM(y,elem-m);	\
  } \
  return s; \
}

#define SHUFFLE2_3N(type, utype, n)				\
static inline type##n __ocl_shuffle2_##type##3_##utype##n (type##3 x, type##3 y, utype##n mask) { \
  type##n s; \
  int i; \
  for (i=0;i<n;i++) { \
    int elem=_VEC_ELEM(mask,i) & 0x7; \
    if (elem>5) elem=0; \
    _VEC_ELEM(s,i) = (elem<3)? _VEC_ELEM(x,elem):_VEC_ELEM(y,elem-3);	\
  } \
  return s; \
}

#define SHUFFLE2_N(type,utype,n) \
  SHUFFLE2_MN(type,2,utype,n)		  \
  SHUFFLE2_3N(type,utype,n)		  \
  SHUFFLE2_MN(type,4,utype,n)		  \
  SHUFFLE2_MN(type,8,utype,n)		  \
  SHUFFLE2_MN(type,16,utype,n)

#define SHUFFLE2(type,utype) \
  SHUFFLE2_N(type,utype,2)		  \
  SHUFFLE2_N(type,utype,3)		  \
  SHUFFLE2_N(type,utype,4)		  \
  SHUFFLE2_N(type,utype,8)		  \
  SHUFFLE2_N(type,utype,16)

SHUFFLE2(char,uchar)
SHUFFLE2(uchar,uchar)
SHUFFLE2(short,ushort)
SHUFFLE2(ushort,ushort)
SHUFFLE2(int,uint)
SHUFFLE2(uint,uint)
SHUFFLE2(long,ulong)
SHUFFLE2(ulong,ulong)
SHUFFLE2(float,uint)

  
#endif
