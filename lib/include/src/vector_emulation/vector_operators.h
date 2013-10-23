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

/* Operations on OpenCL vector */


#ifndef VECTOR_OPERATORS_H
#define VECTOR_OPERATORS_H

#include "vector_types.h"


/*=============================================
               Binary operators
  =============================================*/

#define BINARY_N(type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem left, type##nb_elem right) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=left.element[i] op right.element[i]; \
  } \
  return result; \
}

#define BINARY(type, op_name,op) \
  BINARY_N(type,2,  op_name,op) \
  BINARY_N(type,3,  op_name,op) \
  BINARY_N(type,4,  op_name,op) \
  BINARY_N(type,8,  op_name,op) \
  BINARY_N(type,16, op_name,op)

#define INTEGRAL_BINARY_OPERATOR(op_name,op) \
  BINARY(char,op_name,op)  \
  BINARY(uchar,op_name,op) \
  BINARY(short,op_name,op) \
  BINARY(ushort,op_name,op)\
  BINARY(int,op_name,op)   \
  BINARY(uint,op_name,op)  \
  BINARY(long,op_name,op)  \
  BINARY(ulong,op_name,op)

#define BINARY_OPERATOR(op_name,op)    \
  INTEGRAL_BINARY_OPERATOR(op_name,op) \
  BINARY(float,op_name,op)

  
BINARY_OPERATOR(PLUS,+)
BINARY_OPERATOR(MINUS,-)
BINARY_OPERATOR(STAR,*)
INTEGRAL_BINARY_OPERATOR(BOR,|)
INTEGRAL_BINARY_OPERATOR(BAND,&)
INTEGRAL_BINARY_OPERATOR(BXOR,^)


/*=============================================
              Shift operators
  =============================================*/

#define SHIFT_N(type,nb_elem,op_name,op,mask) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem left, type##nb_elem right) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=left.element[i] op (right.element[i] & mask);	\
  } \
  return result; \
}

#define SHIFT(type, op_name,op,mask) \
  SHIFT_N(type,2,  op_name,op,mask) \
  SHIFT_N(type,3,  op_name,op,mask) \
  SHIFT_N(type,4,  op_name,op,mask) \
  SHIFT_N(type,8,  op_name,op,mask) \
  SHIFT_N(type,16, op_name,op,mask)

#define SHIFT_OPERATOR(op_name,op)		\
  SHIFT(char,op_name,op,0x7)  \
  SHIFT(uchar,op_name,op,0x7) \
  SHIFT(short,op_name,op,0xf) \
  SHIFT(ushort,op_name,op,0xf)\
  SHIFT(int,op_name,op,0x1f)   \
  SHIFT(uint,op_name,op,0x1f)  \
  SHIFT(long,op_name,op,0x3f)  \
  SHIFT(ulong,op_name,op,0x3f)

SHIFT_OPERATOR(LSHIFT,<<)
SHIFT_OPERATOR(RSHIFT,>>)


/*=============================================
             Div and Remainder operators
  =============================================*/

// Special case since they should not generate any exception
  
#define DIV_N(type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem left, type##nb_elem right) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=  (right.element[i]==-1) ? -left.element[i] : left.element[i] op ( (right.element[i]==0)? 1 : right.element[i] ); \
  } \
  return result; \
}

#define REMAINDER_N(type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem left, type##nb_elem right) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=  (right.element[i]==-1) ? 0 : left.element[i] op ( (right.element[i]==0)? 1 : right.element[i] ); \
  } \
  return result; \
}

#define DIV_REMAINDER_U_N(type,nb_elem,op_name,op)					\
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem left, type##nb_elem right) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=left.element[i] op ( (right.element[i]==0)? 1 : right.element[i] ); \
  } \
  return result; \
}

#define DIV(type, op_name,op) \
  DIV_N(type,2,  op_name,op) \
  DIV_N(type,3,  op_name,op) \
  DIV_N(type,4,  op_name,op) \
  DIV_N(type,8,  op_name,op) \
  DIV_N(type,16, op_name,op)

#define DIV_REMAINDER_U(type, op_name,op) \
  DIV_REMAINDER_U_N(type,2,  op_name,op) \
  DIV_REMAINDER_U_N(type,3,  op_name,op) \
  DIV_REMAINDER_U_N(type,4,  op_name,op) \
  DIV_REMAINDER_U_N(type,8,  op_name,op) \
  DIV_REMAINDER_U_N(type,16, op_name,op)

#define DIV_OPERATOR(op_name,op) \
  DIV(char,op_name,op)  \
  DIV_REMAINDER_U(uchar,op_name,op) \
  DIV(short,op_name,op) \
  DIV_REMAINDER_U(ushort,op_name,op)\
  DIV(int,op_name,op)   \
  DIV_REMAINDER_U(uint,op_name,op)  \
  DIV(long,op_name,op)  \
  DIV_REMAINDER_U(ulong,op_name,op) \
  BINARY(float,op_name,op)

#define REMAINDER(type, op_name,op)  \
  REMAINDER_N(type,2,  op_name,op) \
  REMAINDER_N(type,3,  op_name,op) \
  REMAINDER_N(type,4,  op_name,op) \
  REMAINDER_N(type,8,  op_name,op) \
  REMAINDER_N(type,16, op_name,op)

#define REMAINDER_OPERATOR(op_name,op) \
  REMAINDER(char,op_name,op)  \
  DIV_REMAINDER_U(uchar,op_name,op) \
  REMAINDER(short,op_name,op) \
  DIV_REMAINDER_U(ushort,op_name,op)\
  REMAINDER(int,op_name,op)   \
  DIV_REMAINDER_U(uint,op_name,op)  \
  REMAINDER(long,op_name,op)  \
  DIV_REMAINDER_U(ulong,op_name,op)
  
DIV_OPERATOR(DIV,/)
REMAINDER_OPERATOR(MOD,%)



  
/*=============================================
      Assign operators (on the full vector)
  =============================================*/

#define ASSIGN_N(type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem *dest, type##nb_elem src) { \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    dest->element[i] op src.element[i]; \
  } \
  return src; \
}

#define ASSIGN(type, op_name,op) \
  ASSIGN_N(type,2,  op_name,op) \
  ASSIGN_N(type,3,  op_name,op) \
  ASSIGN_N(type,4,  op_name,op) \
  ASSIGN_N(type,8,  op_name,op) \
  ASSIGN_N(type,16, op_name,op)

#define INTEGRAL_ASSIGN_OPERATOR(op_name,op) \
  ASSIGN(char,op_name,op)  \
  ASSIGN(uchar,op_name,op) \
  ASSIGN(short,op_name,op) \
  ASSIGN(ushort,op_name,op)\
  ASSIGN(int,op_name,op)   \
  ASSIGN(uint,op_name,op)  \
  ASSIGN(long,op_name,op)  \
  ASSIGN(ulong,op_name,op)

#define ASSIGN_OPERATOR(op_name,op)    \
  INTEGRAL_ASSIGN_OPERATOR(op_name,op) \
  ASSIGN(float,op_name,op)


ASSIGN_OPERATOR(ASSIGN,=)
ASSIGN_OPERATOR(PLUS_ASSIGN,+=)
ASSIGN_OPERATOR(MINUS_ASSIGN,-=)
ASSIGN_OPERATOR(STAR_ASSIGN,*=)
INTEGRAL_ASSIGN_OPERATOR(BOR_ASSIGN,|=)
INTEGRAL_ASSIGN_OPERATOR(BAND_ASSIGN,&=)
INTEGRAL_ASSIGN_OPERATOR(BXOR_ASSIGN,^=)


 /*=============================================
      Shift operators (on the full vector)
  =============================================*/

#define SHIFT_ASSIGN_N(type,nb_elem,op_name,op,mask)				\
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem *dest, type##nb_elem src) { \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    dest->element[i] op (src.element[i] & mask);	\
  } \
  return src; \
}

#define SHIFT_ASSIGN(type, op_name,op,mask) \
  SHIFT_ASSIGN_N(type,2,  op_name,op,mask) \
  SHIFT_ASSIGN_N(type,3,  op_name,op,mask) \
  SHIFT_ASSIGN_N(type,4,  op_name,op,mask) \
  SHIFT_ASSIGN_N(type,8,  op_name,op,mask) \
  SHIFT_ASSIGN_N(type,16, op_name,op,mask)

#define SHIFT_ASSIGN_OPERATOR(op_name,op)	\
  SHIFT_ASSIGN(char,op_name,op,0x7) \
  SHIFT_ASSIGN(uchar,op_name,op,0x7) \
  SHIFT_ASSIGN(short,op_name,op,0xf) \
  SHIFT_ASSIGN(ushort,op_name,op,0xf)\
  SHIFT_ASSIGN(int,op_name,op,0x1f)   \
  SHIFT_ASSIGN(uint,op_name,op,0x1f)  \
  SHIFT_ASSIGN(long,op_name,op,0x3f)  \
  SHIFT_ASSIGN(ulong,op_name,op,0x3f)

 
SHIFT_ASSIGN_OPERATOR(LSHIFT_ASSIGN,<<=)
SHIFT_ASSIGN_OPERATOR(RSHIFT_ASSIGN,>>=)


  
/*=============================================
    Div-assign operators (on the full vector)
  =============================================*/

// Special case since they should not generate any exception

#define DIV_ASSIGN_N(type,nb_elem,op_name,op)				\
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem *dest, type##nb_elem src) { \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    (src.element[i]==-1)?  (dest->element[i] *= -1) : (dest->element[i] op ( (src.element[i]==0)? 1 : src.element[i] )); \
  } \
  return src; \
}

#define REMAINDER_ASSIGN_N(type,nb_elem,op_name,op)				\
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem *dest, type##nb_elem src) { \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    (src.element[i]==-1)?  (dest->element[i] = 0) : (dest->element[i] op ( (src.element[i]==0)? 1 : src.element[i] )); \
  } \
  return src; \
}

#define DIV_REMAINDER_U_ASSIGN_N(type,nb_elem,op_name,op)				\
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem *dest, type##nb_elem src) { \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    dest->element[i] op ( (src.element[i]==0)? 1 : src.element[i] ); \
  } \
  return src; \
}

  
#define DIV_REMAINDER_U_ASSIGN(type, op_name,op)	\
  DIV_REMAINDER_U_ASSIGN_N(type,2,  op_name,op) \
  DIV_REMAINDER_U_ASSIGN_N(type,3,  op_name,op) \
  DIV_REMAINDER_U_ASSIGN_N(type,4,  op_name,op) \
  DIV_REMAINDER_U_ASSIGN_N(type,8,  op_name,op) \
  DIV_REMAINDER_U_ASSIGN_N(type,16, op_name,op)

#define DIV_ASSIGN(type, op_name,op)		\
  DIV_ASSIGN_N(type,2,  op_name,op) \
  DIV_ASSIGN_N(type,3,  op_name,op) \
  DIV_ASSIGN_N(type,4,  op_name,op) \
  DIV_ASSIGN_N(type,8,  op_name,op) \
  DIV_ASSIGN_N(type,16, op_name,op)

#define DIV_ASSIGN_OPERATOR(op_name,op) \
  DIV_ASSIGN(char,op_name,op)  \
  DIV_REMAINDER_U_ASSIGN(uchar,op_name,op) \
  DIV_ASSIGN(short,op_name,op) \
  DIV_REMAINDER_U_ASSIGN(ushort,op_name,op)\
  DIV_ASSIGN(int,op_name,op)   \
  DIV_REMAINDER_U_ASSIGN(uint,op_name,op)  \
  DIV_ASSIGN(long,op_name,op)  \
  DIV_REMAINDER_U_ASSIGN(ulong,op_name,op) \
  ASSIGN(float,op_name,op)

  
#define REMAINDER_ASSIGN(type, op_name,op) \
  REMAINDER_ASSIGN_N(type,2,  op_name,op) \
  REMAINDER_ASSIGN_N(type,3,  op_name,op) \
  REMAINDER_ASSIGN_N(type,4,  op_name,op) \
  REMAINDER_ASSIGN_N(type,8,  op_name,op) \
  REMAINDER_ASSIGN_N(type,16, op_name,op)

#define REMAINDER_ASSIGN_OPERATOR(op_name,op)	\
  REMAINDER_ASSIGN(char,op_name,op)  \
  DIV_REMAINDER_U_ASSIGN(uchar,op_name,op) \
  REMAINDER_ASSIGN(short,op_name,op) \
  DIV_REMAINDER_U_ASSIGN(ushort,op_name,op)\
  REMAINDER_ASSIGN(int,op_name,op)   \
  DIV_REMAINDER_U_ASSIGN(uint,op_name,op)  \
  REMAINDER_ASSIGN(long,op_name,op)  \
  DIV_REMAINDER_U_ASSIGN(ulong,op_name,op) \

  
DIV_ASSIGN_OPERATOR(DIV_ASSIGN,/=)
REMAINDER_ASSIGN_OPERATOR(MOD_ASSIGN,%=)

  
/*=============================================
              Relational operators
  =============================================*/

#define RELATIONAL_SN(sign,type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##sign##type##nb_elem( sign##type##nb_elem left, sign##type##nb_elem right) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=(left.element[i] op right.element[i])?(type)-1:0; \
  } \
  return result; \
}
  
#define RELATIONAL_FLOAT_N(type,nb_elem,op_name,op) \
static inline int##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem left, type##nb_elem right) { \
  int##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=(left.element[i] op right.element[i])?(type)-1:0; \
  } \
  return result; \
}

#define RELATIONAL_N(type,nb_elem,op_name,op) \
  RELATIONAL_SN(,type,nb_elem,op_name,op)     \
  RELATIONAL_SN(u,type,nb_elem,op_name,op)

#define RELATIONAL_FLOAT(type, op_name,op) \
  RELATIONAL_FLOAT_N(type,2,  op_name,op) \
  RELATIONAL_FLOAT_N(type,3,  op_name,op) \
  RELATIONAL_FLOAT_N(type,4,  op_name,op) \
  RELATIONAL_FLOAT_N(type,8,  op_name,op) \
  RELATIONAL_FLOAT_N(type,16, op_name,op)

#define RELATIONAL(type, op_name,op) \
  RELATIONAL_N(type,2,  op_name,op)  \
  RELATIONAL_N(type,3,  op_name,op)  \
  RELATIONAL_N(type,4,  op_name,op)  \
  RELATIONAL_N(type,8,  op_name,op)  \
  RELATIONAL_N(type,16, op_name,op)

#define RELATIONAL_OPERATOR(op_name,op) \
  RELATIONAL(char,op_name,op)  \
  RELATIONAL(short,op_name,op) \
  RELATIONAL(int,op_name,op)   \
  RELATIONAL(long,op_name,op)  \
  RELATIONAL_FLOAT(float,op_name,op)

RELATIONAL_OPERATOR(EQUAL,==)
RELATIONAL_OPERATOR(NOT_EQUAL,!=)
RELATIONAL_OPERATOR(LT,<)
RELATIONAL_OPERATOR(LTE,<=)
RELATIONAL_OPERATOR(GT,>)
RELATIONAL_OPERATOR(GTE,>=)
// Not relational, but same fuctions
RELATIONAL_OPERATOR(LOR,||)
RELATIONAL_OPERATOR(LAND,&&)



/*=============================================
               unary operators
  =============================================*/

#define UNARY_N(type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem src) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]= op (src.element[i]); \
  } \
  return result; \
}

#define UNARY(type, op_name,op) \
  UNARY_N(type,2,  op_name,op) \
  UNARY_N(type,3,  op_name,op) \
  UNARY_N(type,4,  op_name,op) \
  UNARY_N(type,8,  op_name,op) \
  UNARY_N(type,16, op_name,op)

#define INTEGRAL_UNARY_OPERATOR(op_name,op) \
  UNARY(char,op_name,op)  \
  UNARY(uchar,op_name,op) \
  UNARY(short,op_name,op) \
  UNARY(ushort,op_name,op)\
  UNARY(int,op_name,op)   \
  UNARY(uint,op_name,op)  \
  UNARY(long,op_name,op)  \
  UNARY(ulong,op_name,op)

#define UNARY_OPERATOR(op_name,op)    \
  INTEGRAL_UNARY_OPERATOR(op_name,op) \
  UNARY(float,op_name,op)

UNARY_OPERATOR(UnaryPlus,+)
UNARY_OPERATOR(UnaryMinus,-)
INTEGRAL_UNARY_OPERATOR(BNOT,~)


/*=============================================
               unary logical operators
  =============================================*/

#define UNARY_LOGICAL_SN(sign,type,nb_elem,op_name,op)			\
static inline type##nb_elem __ocl_##op_name##_##sign##type##nb_elem( sign##type##nb_elem src) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=(op (src.element[i]))?(type)-1:0;	\
  } \
  return result; \
}

#define UNARY_LOGICAL_FLOAT_N(type,nb_elem,op_name,op) \
static inline int##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem src) { \
  int##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    result.element[i]=(op (src.element[i]))?(type)-1:0;	\
  } \
  return result; \
}
  
#define UNARY_LOGICAL_N(type,nb_elem,op_name,op) \
  UNARY_LOGICAL_SN(,type,nb_elem,op_name,op) \
  UNARY_LOGICAL_SN(u,type,nb_elem,op_name,op)
    
#define UNARY_LOGICAL(type, op_name,op) \
  UNARY_LOGICAL_N(type,2,  op_name,op) \
  UNARY_LOGICAL_N(type,3,  op_name,op) \
  UNARY_LOGICAL_N(type,4,  op_name,op) \
  UNARY_LOGICAL_N(type,8,  op_name,op) \
  UNARY_LOGICAL_N(type,16, op_name,op)

#define UNARY_LOGICAL_FLOAT(type, op_name,op) \
  UNARY_LOGICAL_FLOAT_N(type,2,  op_name,op) \
  UNARY_LOGICAL_FLOAT_N(type,3,  op_name,op) \
  UNARY_LOGICAL_FLOAT_N(type,4,  op_name,op) \
  UNARY_LOGICAL_FLOAT_N(type,8,  op_name,op) \
  UNARY_LOGICAL_FLOAT_N(type,16, op_name,op)

#define INTEGRAL_UNARY_LOGICAL_OPERATOR(op_name,op) \
  UNARY_LOGICAL(char,op_name,op)  \
  UNARY_LOGICAL(short,op_name,op) \
  UNARY_LOGICAL(int,op_name,op)   \
  UNARY_LOGICAL(long,op_name,op)  \
  UNARY_LOGICAL_FLOAT(float,op_name,op)

INTEGRAL_UNARY_LOGICAL_OPERATOR(LNOT,!)




/*=============================================
              Conditional operators
  =============================================*/

#define CONDITIONAL_N(type,cond_type,nb_elem,op_name,nbbits)		\
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( cond_type##nb_elem cond, type##nb_elem left, type##nb_elem right) { \
  type##nb_elem result; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    if ((cond.element[i]>>(nbbits - 1)) & 0x1) {	\
       result.element[i]=left.element[i]; \
    } \
    else { \
       result.element[i]=right.element[i]; \
    } \
  } \
  return result; \
}

#define CONDITIONAL(type,cond_type,op_name,nbbits) \
  CONDITIONAL_N(type,cond_type,2,op_name,nbbits) \
  CONDITIONAL_N(type,cond_type,3,op_name,nbbits) \
  CONDITIONAL_N(type,cond_type,4,op_name,nbbits) \
  CONDITIONAL_N(type,cond_type,8,op_name,nbbits) \
  CONDITIONAL_N(type,cond_type,16,op_name,nbbits)

  
#define CONDITIONAL_OPERATOR(op_name) \
  CONDITIONAL(char,char,op_name,8) \
  CONDITIONAL(uchar,uchar,op_name,8) \
  CONDITIONAL(short,short,op_name,16) \
  CONDITIONAL(ushort,ushort,op_name,16)	\
  CONDITIONAL(int,int,op_name,32) \
  CONDITIONAL(uint,uint,op_name,32) \
  CONDITIONAL(long,long,op_name,64) \
  CONDITIONAL(ulong,ulong,op_name,64) \
  CONDITIONAL(float,int,op_name,32)


CONDITIONAL_OPERATOR(QUESTION)


  

/*=============================================
    Post-modif operators (on the full vector)
  =============================================*/

#define POSTMODIF_N(type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem *src_dest) { \
  type##nb_elem result=*src_dest; \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    (src_dest->element[i]) op; \
  } \
  return result; \
}

#define POSTMODIF(type, op_name,op) \
  POSTMODIF_N(type,2,  op_name,op) \
  POSTMODIF_N(type,3,  op_name,op) \
  POSTMODIF_N(type,4,  op_name,op) \
  POSTMODIF_N(type,8,  op_name,op) \
  POSTMODIF_N(type,16, op_name,op)

#define POSTMODIF_OPERATOR(op_name,op) \
  POSTMODIF(char,op_name,op)  \
  POSTMODIF(uchar,op_name,op) \
  POSTMODIF(short,op_name,op) \
  POSTMODIF(ushort,op_name,op)\
  POSTMODIF(int,op_name,op)   \
  POSTMODIF(uint,op_name,op)  \
  POSTMODIF(long,op_name,op)  \
  POSTMODIF(ulong,op_name,op) \
  POSTMODIF(float,op_name,op)

POSTMODIF_OPERATOR(PostInc,++)
POSTMODIF_OPERATOR(PostDec,--)

  
/*=============================================
    Pre-modif operators (on the full vector)
  =============================================*/

#define PREMODIF_N(type,nb_elem,op_name,op) \
static inline type##nb_elem __ocl_##op_name##_##type##nb_elem( type##nb_elem *src_dest) { \
  int i=0; \
  for(i=0;i<nb_elem;i++) { \
    op (src_dest->element[i]); \
  } \
  return *src_dest; \
}

#define PREMODIF(type, op_name,op) \
  PREMODIF_N(type,2,  op_name,op) \
  PREMODIF_N(type,3,  op_name,op) \
  PREMODIF_N(type,4,  op_name,op) \
  PREMODIF_N(type,8,  op_name,op) \
  PREMODIF_N(type,16, op_name,op)

#define PREMODIF_OPERATOR(op_name,op) \
  PREMODIF(char,op_name,op)  \
  PREMODIF(uchar,op_name,op) \
  PREMODIF(short,op_name,op) \
  PREMODIF(ushort,op_name,op)\
  PREMODIF(int,op_name,op)   \
  PREMODIF(uint,op_name,op)  \
  PREMODIF(long,op_name,op)  \
  PREMODIF(ulong,op_name,op) \
  PREMODIF(float,op_name,op)

PREMODIF_OPERATOR(PreInc,++)
PREMODIF_OPERATOR(PreDec,--)


#endif
