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

/* Management of OpenCL sub-vectors */

#ifndef VECTOR_ELEMENTS_H
#define VECTOR_ELEMENTS_H

#include "vector_types.h"


//===============================================================
// Specification of vector elements
//===============================================================

#define VECTOR_SUBELEM_SPEC(n) \
typedef struct  { \
  int elem[n]; \
} VECTOR_SUBELEM_##n;

VECTOR_SUBELEM_SPEC(1)
VECTOR_SUBELEM_SPEC(2)
VECTOR_SUBELEM_SPEC(3)
VECTOR_SUBELEM_SPEC(4)
VECTOR_SUBELEM_SPEC(8)
VECTOR_SUBELEM_SPEC(16)


  
//===============================================================
// Vector element getters
//===============================================================

#define GET_VECTOR_SCALAR(type,nb_elem)			\
static inline type __ocl_get_##type##nb_elem##_1 (type##nb_elem src, VECTOR_SUBELEM_1 elem_spec) { \
  return src.element[elem_spec.elem[0]]; \
}

#define GET_VECTOR_ELEMENT_NN(type,nb_elem,nb_elem_got)		\
static inline type##nb_elem_got __ocl_get_##type##nb_elem##_##nb_elem_got (type##nb_elem src, VECTOR_SUBELEM_##nb_elem_got elem_spec) {	\
  type##nb_elem_got s;  \
  int i; \
  for (i=0;i<nb_elem_got;i++) {  \
    s.element[i]=src.element[elem_spec.elem[i]]; \
  } \
  return s; \
}

#define GET_VECTOR_ELEMENT_N(type,nb_elem) \
  GET_VECTOR_SCALAR(type,nb_elem)	 \
  GET_VECTOR_ELEMENT_NN(type,nb_elem,2)  \
  GET_VECTOR_ELEMENT_NN(type,nb_elem,3)  \
  GET_VECTOR_ELEMENT_NN(type,nb_elem,4)  \
  GET_VECTOR_ELEMENT_NN(type,nb_elem,8)  \
  GET_VECTOR_ELEMENT_NN(type,nb_elem,16)
  
#define GET_VECTOR_ELEMENT(type) \
  GET_VECTOR_ELEMENT_N(type,2) \
  GET_VECTOR_ELEMENT_N(type,3) \
  GET_VECTOR_ELEMENT_N(type,4) \
  GET_VECTOR_ELEMENT_N(type,8) \
  GET_VECTOR_ELEMENT_N(type,16)

  
GET_VECTOR_ELEMENT(char)
GET_VECTOR_ELEMENT(uchar)
GET_VECTOR_ELEMENT(short)
GET_VECTOR_ELEMENT(ushort)
GET_VECTOR_ELEMENT(int)
GET_VECTOR_ELEMENT(uint)
GET_VECTOR_ELEMENT(long)
GET_VECTOR_ELEMENT(ulong)
GET_VECTOR_ELEMENT(float)


  
//===============================================================
// Vector element assignment
//===============================================================
  
#define ASSIGN_VECTOR_ELEMENT_SCALAR(type,nb_elem,op_name,op)			\
static inline type __ocl_set##op_name##_##type##nb_elem##_1 (type##nb_elem *left, VECTOR_SUBELEM_1 elem_spec, type right) { \
  left->element[elem_spec.elem[0]] op right;					\
  return right; \
}

#define ASSIGN_VECTOR_ELEMENT_NN(type,nb_elem,nb_elem_set,op_name,op)		\
static inline type##nb_elem_set __ocl_set##op_name##_##type##nb_elem##_##nb_elem_set (type##nb_elem *left, VECTOR_SUBELEM_##nb_elem_set elem_spec, type##nb_elem_set right) { \
  int i; \
  for (i=0;i<nb_elem_set;i++) {  \
    left->element[elem_spec.elem[i]] op right.element[i]; \
  } \
  return right; \
}

#define ASSIGN_VECTOR_ELEMENT_N(type,nb_elem,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT_SCALAR(type,nb_elem,op_name,op)		\
  ASSIGN_VECTOR_ELEMENT_NN(type,nb_elem,2,op_name,op)  	\
  ASSIGN_VECTOR_ELEMENT_NN(type,nb_elem,3,op_name,op)  	\
  ASSIGN_VECTOR_ELEMENT_NN(type,nb_elem,4,op_name,op)  	\
  ASSIGN_VECTOR_ELEMENT_NN(type,nb_elem,8,op_name,op)  	\
  ASSIGN_VECTOR_ELEMENT_NN(type,nb_elem,16,op_name,op)
  
#define ASSIGN_VECTOR_ELEMENT(type,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT_N(type,2,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT_N(type,3,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT_N(type,4,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT_N(type,8,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT_N(type,16,op_name,op)

#define ASSIGN_VECTOR_ELEMENT_INTEGRAL(op_name,op) \
  ASSIGN_VECTOR_ELEMENT(char,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(uchar,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(short,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(ushort,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(int,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(uint,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(long,op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(ulong,op_name,op)

#define ASSIGN_VECTOR_ELEMENT_ALL(op_name,op)	\
  ASSIGN_VECTOR_ELEMENT_INTEGRAL(op_name,op)	\
  ASSIGN_VECTOR_ELEMENT(float,op_name,op)


// All operators which can set elements of a vector
ASSIGN_VECTOR_ELEMENT_ALL(ASSIGN,=)
ASSIGN_VECTOR_ELEMENT_ALL(PLUS_ASSIGN,+=)
ASSIGN_VECTOR_ELEMENT_ALL(MINUS_ASSIGN,-=)
ASSIGN_VECTOR_ELEMENT_ALL(STAR_ASSIGN,*=)
ASSIGN_VECTOR_ELEMENT_ALL(DIV_ASSIGN,/=)
ASSIGN_VECTOR_ELEMENT_INTEGRAL(MOD_ASSIGN,%=)
ASSIGN_VECTOR_ELEMENT_INTEGRAL(RSHIFT_ASSIGN,>>=)
ASSIGN_VECTOR_ELEMENT_INTEGRAL(LSHIFT_ASSIGN,<<=)
ASSIGN_VECTOR_ELEMENT_INTEGRAL(BAND_ASSIGN,&=)
ASSIGN_VECTOR_ELEMENT_INTEGRAL(BOR_ASSIGN,|=)
ASSIGN_VECTOR_ELEMENT_INTEGRAL(BXOR_ASSIGN,^=)



//===============================================================
// Vector element post-modif
//===============================================================
  
#define POSTMODIF_VECTOR_ELEMENT_SCALAR(type,nb_elem,op_name,op)			\
static inline type __ocl_set##op_name##_##type##nb_elem##_1 (type##nb_elem *src_dest, VECTOR_SUBELEM_1 elem_spec) { \
  type res=(src_dest->element[elem_spec.elem[0]]) op;	  \
  return res; \
}

#define POSTMODIF_VECTOR_ELEMENT_NN(type,nb_elem,nb_elem_set,op_name,op)		\
static inline type##nb_elem_set __ocl_set##op_name##_##type##nb_elem##_##nb_elem_set (type##nb_elem *src_dest, VECTOR_SUBELEM_##nb_elem_set elem_spec) { \
  int i; \
  type##nb_elem_set res; \
  for (i=0;i<nb_elem_set;i++) {  \
    res.element[i] = (src_dest->element[elem_spec.elem[i]]) op; \
  } \
  return res; \
}

#define POSTMODIF_VECTOR_ELEMENT_N(type,nb_elem,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT_SCALAR(type,nb_elem,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT_NN(type,nb_elem,2,op_name,op)  	\
  POSTMODIF_VECTOR_ELEMENT_NN(type,nb_elem,3,op_name,op)  	\
  POSTMODIF_VECTOR_ELEMENT_NN(type,nb_elem,4,op_name,op)  	\
  POSTMODIF_VECTOR_ELEMENT_NN(type,nb_elem,8,op_name,op)  	\
  POSTMODIF_VECTOR_ELEMENT_NN(type,nb_elem,16,op_name,op)
  
#define POSTMODIF_VECTOR_ELEMENT(type,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT_N(type,2,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT_N(type,3,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT_N(type,4,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT_N(type,8,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT_N(type,16,op_name,op)

#define POSTMODIF_VECTOR_ELEMENT_ALL(op_name,op) \
  POSTMODIF_VECTOR_ELEMENT(char,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT(uchar,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT(short,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT(ushort,op_name,op) \
  POSTMODIF_VECTOR_ELEMENT(int,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT(uint,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT(long,op_name,op)	\
  POSTMODIF_VECTOR_ELEMENT(ulong,op_name,op) \
  POSTMODIF_VECTOR_ELEMENT(float,op_name,op)

// All operators which can set elements of a vector
POSTMODIF_VECTOR_ELEMENT_ALL(PostInc,++)
POSTMODIF_VECTOR_ELEMENT_ALL(PostDec,--)

  
//===============================================================
// Vector element pre-modif
//===============================================================
  
#define PREMODIF_VECTOR_ELEMENT_SCALAR(type,nb_elem,op_name,op)			\
static inline type __ocl_set##op_name##_##type##nb_elem##_1 (type##nb_elem *src_dest, VECTOR_SUBELEM_1 elem_spec) { \
  type res=op (src_dest->element[elem_spec.elem[0]]); \
  return res; \
}

#define PREMODIF_VECTOR_ELEMENT_NN(type,nb_elem,nb_elem_set,op_name,op)		\
static inline type##nb_elem_set __ocl_set##op_name##_##type##nb_elem##_##nb_elem_set (type##nb_elem *src_dest, VECTOR_SUBELEM_##nb_elem_set elem_spec) { \
  int i; \
  type##nb_elem_set res; \
  for (i=0;i<nb_elem_set;i++) {  \
    res.element[i] = op (src_dest->element[elem_spec.elem[i]]);	\
  } \
  return res; \
}

#define PREMODIF_VECTOR_ELEMENT_N(type,nb_elem,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT_SCALAR(type,nb_elem,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT_NN(type,nb_elem,2,op_name,op)  	\
  PREMODIF_VECTOR_ELEMENT_NN(type,nb_elem,3,op_name,op)  	\
  PREMODIF_VECTOR_ELEMENT_NN(type,nb_elem,4,op_name,op)  	\
  PREMODIF_VECTOR_ELEMENT_NN(type,nb_elem,8,op_name,op)  	\
  PREMODIF_VECTOR_ELEMENT_NN(type,nb_elem,16,op_name,op)
  
#define PREMODIF_VECTOR_ELEMENT(type,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT_N(type,2,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT_N(type,3,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT_N(type,4,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT_N(type,8,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT_N(type,16,op_name,op)

#define PREMODIF_VECTOR_ELEMENT_ALL(op_name,op) \
  PREMODIF_VECTOR_ELEMENT(char,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT(uchar,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT(short,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT(ushort,op_name,op) \
  PREMODIF_VECTOR_ELEMENT(int,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT(uint,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT(long,op_name,op)	\
  PREMODIF_VECTOR_ELEMENT(ulong,op_name,op) \
  PREMODIF_VECTOR_ELEMENT(float,op_name,op)

// All operators which can set elements of a vector
PREMODIF_VECTOR_ELEMENT_ALL(PreInc,++)
PREMODIF_VECTOR_ELEMENT_ALL(PreDec,--)

  
  
#endif
