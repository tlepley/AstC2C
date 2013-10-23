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

/* OpenCL vector literals */

#ifndef VECTOR_LITERALS_H
#define VECTOR_LITERALS_H

#include "vector_types.h"

#define SET_SCALAR(type,nb_elem)			       \
static inline type##nb_elem __ocl_set_##type##nb_elem##_scalar(int index, type elem, type##nb_elem src) { \
  src.element[index]=elem; \
  return src; \
}
#define SET_VECTOR(type,nb_elem, nb_elem_src) \
static inline type##nb_elem __ocl_set_##type##nb_elem##_##type##nb_elem_src(int index, type##nb_elem_src elem, type##nb_elem src) { \
  int i;  \
  for (i=0;i<nb_elem_src;i++) {  \
    src.element[index+i]=elem.element[i]; \
  } \
  return src; \
}

#define SET_VECTOR2(type) \
  SET_SCALAR(type,2)

#define SET_VECTOR3(type) \
  SET_SCALAR(type,3)   \
  SET_VECTOR(type,3,2)

#define SET_VECTOR4(type)			\
  SET_SCALAR(type,4)   \
  SET_VECTOR(type,4,3) \
  SET_VECTOR(type,4,2)

#define SET_VECTOR8(type) \
  SET_SCALAR(type,8)   \
  SET_VECTOR(type,8,4) \
  SET_VECTOR(type,8,3) \
  SET_VECTOR(type,8,2)

#define SET_VECTOR16(type) \
  SET_SCALAR(type,16)   \
  SET_VECTOR(type,16,8) \
  SET_VECTOR(type,16,4) \
  SET_VECTOR(type,16,3) \
  SET_VECTOR(type,16,2)

#define SET_ALL_VECTOR(type) \
  SET_VECTOR2(type) \
  SET_VECTOR4(type) \
  SET_VECTOR8(type) \
  SET_VECTOR16(type)
  

SET_ALL_VECTOR(char)
SET_ALL_VECTOR(uchar)
SET_ALL_VECTOR(short)
SET_ALL_VECTOR(ushort)
SET_ALL_VECTOR(int)
SET_ALL_VECTOR(uint)
SET_ALL_VECTOR(long)
SET_ALL_VECTOR(ulong)
  
SET_ALL_VECTOR(float)
  
#endif
