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

/* OpenCL relational functions */

#ifndef RELATIONAL_H
#define RELATIONAL_H

// Internal builtins for math.h
static inline float __clam__MAXFLOAT() {
  return FLT_MAX;
}
static inline float __clam__NAN() {
  return NAN;
}
static inline float __clam__HUGE_VALF() {
  return HUGE_VALF;
}
static inline float __clam__INFINITY() {
  return INFINITY;
}


/*-- isfinite, isinf, isnan, isnormal, signbit */
#define RELATIONAL_BUILTIN_CLASSIFY_SCALAR_N_SRC(name,name_to_call,type,postfix) \
static inline int __ocl_##name##_##type (type src) { \
  return name_to_call##postfix (src) ? 1 : 0;  \
}

#define RELATIONAL_BUILTIN_CLASSIFY_VECTOR_N_SRC(name,name_to_call,type,nb_elem,postfix) \
static inline int##nb_elem __ocl_##name##_##type##nb_elem (type##nb_elem src) { \
  int##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=name_to_call##postfix (src.element[i]) ? -1 : 0;	\
  } \
  return s; \
}

#define RELATIONAL_BUILTIN_CLASSIFY_VECTOR_SRC(name,name_to_call,type,postfix)	\
  RELATIONAL_BUILTIN_CLASSIFY_SCALAR_N_SRC(name,name_to_call,type,postfix)    \
  RELATIONAL_BUILTIN_CLASSIFY_VECTOR_N_SRC(name,name_to_call,type,2,postfix)    \
  RELATIONAL_BUILTIN_CLASSIFY_VECTOR_N_SRC(name,name_to_call,type,3,postfix)    \
  RELATIONAL_BUILTIN_CLASSIFY_VECTOR_N_SRC(name,name_to_call,type,4,postfix)    \
  RELATIONAL_BUILTIN_CLASSIFY_VECTOR_N_SRC(name,name_to_call,type,8,postfix)    \
  RELATIONAL_BUILTIN_CLASSIFY_VECTOR_N_SRC(name,name_to_call,type,16,postfix)

#define RELATIONAL_BUILTIN_CLASSIFY_VECTOR_VERY_SIMPLE(name)	\
  RELATIONAL_BUILTIN_CLASSIFY_VECTOR_SRC(name,name,float,)


RELATIONAL_BUILTIN_CLASSIFY_VECTOR_VERY_SIMPLE(isfinite);
RELATIONAL_BUILTIN_CLASSIFY_VECTOR_VERY_SIMPLE(isinf);
RELATIONAL_BUILTIN_CLASSIFY_VECTOR_VERY_SIMPLE(isnan);
RELATIONAL_BUILTIN_CLASSIFY_VECTOR_VERY_SIMPLE(isnormal);
RELATIONAL_BUILTIN_CLASSIFY_VECTOR_VERY_SIMPLE(signbit);


/* isequal, isgreater, isgreaterequal, isless, islessequal */
#define RELATIONAL_BUILTIN_GREATERLESSEQUAL_SCALAR_N_SRC(name,relation) \
static inline int __ocl_##name##_float (float src1, float src2) {\
  return (isnan(src1) || isnan(src2)) ?				 \
          0 : src1 relation src2 ; \
}

#define RELATIONAL_BUILTIN_GREATERLESSEQUAL_VECTOR_N_SRC(name,nb_elem,relation)	\
static inline int##nb_elem __ocl_##name##_float##nb_elem (float##nb_elem src1, float##nb_elem src2) { \
  int##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= isnan(src1.element[i]) || isnan(src2.element[i]) ?       \
	           0 : (src1.element[i] relation src2.element[i]) ? -1 : 0 ;  \
  } \
  return s; \
}

#define RELATIONAL_BUILTIN_GREATERLESSEQUAL_SRC(name,relation) 	 \
  RELATIONAL_BUILTIN_GREATERLESSEQUAL_SCALAR_N_SRC(name,relation)      \
  RELATIONAL_BUILTIN_GREATERLESSEQUAL_VECTOR_N_SRC(name,2,relation)    \
  RELATIONAL_BUILTIN_GREATERLESSEQUAL_VECTOR_N_SRC(name,3,relation)    \
  RELATIONAL_BUILTIN_GREATERLESSEQUAL_VECTOR_N_SRC(name,4,relation)    \
  RELATIONAL_BUILTIN_GREATERLESSEQUAL_VECTOR_N_SRC(name,8,relation)    \
  RELATIONAL_BUILTIN_GREATERLESSEQUAL_VECTOR_N_SRC(name,16,relation)

RELATIONAL_BUILTIN_GREATERLESSEQUAL_SRC(isequal,==);
RELATIONAL_BUILTIN_GREATERLESSEQUAL_SRC(isgreater,>);
RELATIONAL_BUILTIN_GREATERLESSEQUAL_SRC(isgreaterequal,>=);
RELATIONAL_BUILTIN_GREATERLESSEQUAL_SRC(isless,<);
RELATIONAL_BUILTIN_GREATERLESSEQUAL_SRC(islessequal,<=);


/* isnotequal */
#define RELATIONAL_BUILTIN_NOTEQUAL_SCALAR_N_SRC(name,relation) \
static inline int __ocl_##name##_float (float src1, float src2) {\
  return isnan(src1) || isnan(src2) ? \
           1 : src1 relation src2 ; \
}

#define RELATIONAL_BUILTIN_NOTEQUAL_VECTOR_N_SRC(name,nb_elem,relation)	\
static inline int##nb_elem __ocl_##name##_float##nb_elem (float##nb_elem src1, float##nb_elem src2) { \
  int##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= isnan(src1.element[i]) || isnan(src2.element[i]) ? \
	           -1 : (src1.element[i] relation src2.element[i]) ? -1 : 0 ;		\
  } \
  return s; \
}

#define RELATIONAL_BUILTIN_NOTEQUAL_SRC(name,relation) 	 \
  RELATIONAL_BUILTIN_NOTEQUAL_SCALAR_N_SRC(name,relation)      \
  RELATIONAL_BUILTIN_NOTEQUAL_VECTOR_N_SRC(name,2,relation)    \
  RELATIONAL_BUILTIN_NOTEQUAL_VECTOR_N_SRC(name,3,relation)    \
  RELATIONAL_BUILTIN_NOTEQUAL_VECTOR_N_SRC(name,4,relation)    \
  RELATIONAL_BUILTIN_NOTEQUAL_VECTOR_N_SRC(name,8,relation)    \
  RELATIONAL_BUILTIN_NOTEQUAL_VECTOR_N_SRC(name,16,relation)

RELATIONAL_BUILTIN_NOTEQUAL_SRC(isnotequal,!=);


/* islessgreater */
#define RELATIONAL_BUILTIN_LESSGREATER_SCALAR_N_SRC(name) \
static inline int __ocl_##name##_float (float src1, float src2) {\
  return isnan(src1) || isnan(src2) ? \
    	  0 : (src1 < src2) || (src1 > src2) ; \
}

#define RELATIONAL_BUILTIN_LESSGREATER_VECTOR_N_SRC(name,nb_elem)	\
static inline int##nb_elem __ocl_##name##_float##nb_elem (float##nb_elem src1, float##nb_elem src2) { \
  int##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= isnan(src1.element[i]) || isnan(src2.element[i]) ?	\
	           0 : ((src1.element[i] < src2.element[i]) || (src1.element[i] > src2.element[i])) ? -1 : 0 ; \
  } \
  return s; \
}

#define RELATIONAL_BUILTIN_LESSGREATER_SRC(name) 	 \
  RELATIONAL_BUILTIN_LESSGREATER_SCALAR_N_SRC(name)      \
  RELATIONAL_BUILTIN_LESSGREATER_VECTOR_N_SRC(name,2)    \
  RELATIONAL_BUILTIN_LESSGREATER_VECTOR_N_SRC(name,3)    \
  RELATIONAL_BUILTIN_LESSGREATER_VECTOR_N_SRC(name,4)    \
  RELATIONAL_BUILTIN_LESSGREATER_VECTOR_N_SRC(name,8)    \
  RELATIONAL_BUILTIN_LESSGREATER_VECTOR_N_SRC(name,16)

RELATIONAL_BUILTIN_LESSGREATER_SRC(islessgreater);

/* isordered */
#define RELATIONAL_BUILTIN_ORDERED_SCALAR_N_SRC(name) \
static inline int __ocl_##name##_float (float src1, float src2) {\
  return isnan(src1) || isnan(src2) ? \
    	  0 : 1; \
}

#define RELATIONAL_BUILTIN_ORDERED_VECTOR_N_SRC(name,nb_elem)	\
static inline int##nb_elem __ocl_##name##_float##nb_elem (float##nb_elem src1, float##nb_elem src2) { \
  int##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= isnan(src1.element[i]) || isnan(src2.element[i]) ? \
	           0 : -1 ; \
  } \
  return s; \
}

#define RELATIONAL_BUILTIN_ORDERED_SRC(name) 	 \
  RELATIONAL_BUILTIN_ORDERED_SCALAR_N_SRC(name)      \
  RELATIONAL_BUILTIN_ORDERED_VECTOR_N_SRC(name,2)    \
  RELATIONAL_BUILTIN_ORDERED_VECTOR_N_SRC(name,3)    \
  RELATIONAL_BUILTIN_ORDERED_VECTOR_N_SRC(name,4)    \
  RELATIONAL_BUILTIN_ORDERED_VECTOR_N_SRC(name,8)    \
  RELATIONAL_BUILTIN_ORDERED_VECTOR_N_SRC(name,16)

RELATIONAL_BUILTIN_ORDERED_SRC(isordered);

/* isunordered */
#define RELATIONAL_BUILTIN_UNORDERED_SCALAR_N_SRC(name) \
static inline int __ocl_##name##_float (float src1, float src2) {\
  return isnan(src1) || isnan(src2) ? \
    	  1 : 0; \
}

#define RELATIONAL_BUILTIN_UNORDERED_VECTOR_N_SRC(name,nb_elem)	\
static inline int##nb_elem __ocl_##name##_float##nb_elem (float##nb_elem src1, float##nb_elem src2) { \
  int##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= isnan(src1.element[i]) || isnan(src2.element[i]) ? \
	           -1 : 0 ; \
  } \
  return s; \
}

#define RELATIONAL_BUILTIN_UNORDERED_SRC(name) 	 \
  RELATIONAL_BUILTIN_UNORDERED_SCALAR_N_SRC(name)      \
  RELATIONAL_BUILTIN_UNORDERED_VECTOR_N_SRC(name,2)    \
  RELATIONAL_BUILTIN_UNORDERED_VECTOR_N_SRC(name,3)    \
  RELATIONAL_BUILTIN_UNORDERED_VECTOR_N_SRC(name,4)    \
  RELATIONAL_BUILTIN_UNORDERED_VECTOR_N_SRC(name,8)    \
  RELATIONAL_BUILTIN_UNORDERED_VECTOR_N_SRC(name,16)

RELATIONAL_BUILTIN_UNORDERED_SRC(isunordered);


/* any & all */
#define ANYALL_NB_SCALAR(type, nbbits) \
static inline int __ocl_any_##type ( type src) { \
  return (src>>(nbbits - 1)) & 0x1; \
} \
static inline int __ocl_all_##type ( type src) { \
  return (src>>(nbbits - 1)) & 0x1; \
}

#define ANYALL_NB_N(type, n, nbbits) \
static inline int __ocl_any_##type##n ( type##n src) { \
  int i, res=0;	      \
  for (i=0;i<n;i++) { \
    res |= (_VEC_ELEM(src,i)>>(nbbits - 1)) & 0x1; \
  } \
  return res; \
} \
static inline int __ocl_all_##type##n ( type##n src) { \
  int i, res=1;	      \
  for (i=0;i<n;i++) { \
    res &= (_VEC_ELEM(src,i)>>(nbbits - 1)) & 0x1; \
  } \
  return res; \
}

#define ANYALL_NB(type,nbbits) \
  ANYALL_NB_SCALAR(type,nbbits) \
  ANYALL_NB_N(type,2,nbbits) \
  ANYALL_NB_N(type,3,nbbits) \
  ANYALL_NB_N(type,4,nbbits) \
  ANYALL_NB_N(type,8,nbbits) \
  ANYALL_NB_N(type,16,nbbits)

ANYALL_NB(char,8)
ANYALL_NB(short,16)
ANYALL_NB(int,32)
ANYALL_NB(long,64)

  
/* bitselect */
#define BITSELECT_SCALAR(type) \
static inline type __ocl_bitselect_##type (type a, type b, type c) {	\
  return (b & c) | (a & (~c)); \
}
#define BITSELECT_FLOAT_SCALAR \
static inline float __ocl_bitselect_float (float a, float b, float c) {	\
  union {int i; float f;} ua,ub,uc,ures; \
  ua.f=a;ub.f=b;uc.f=c; \
  ures.i=(ub.i & uc.i) | (ua.i & (~(uc.i)));	\
  return ures.f; \
}

#define BITSELECT_N(type, n) \
static inline type##n __ocl_bitselect_##type##n (type##n a, type##n b, type##n c) { \
  type##n s; \
  int i;	      \
  for (i=0;i<n;i++) { \
    _VEC_ELEM(s,i) = (_VEC_ELEM(b,i) & _VEC_ELEM(c,i)) | (_VEC_ELEM(a,i) & (~_VEC_ELEM(c,i))) ; \
  } \
  return s; \
}
  
#define BITSELECT_FLOAT_N(n) \
static inline float##n __ocl_bitselect_float##n (float##n a, float##n b, float##n c) { \
  float##n s; \
  int i;	      \
  for (i=0;i<n;i++) { \
    union {int i; float f;} ua,ub,uc,ures; \
    ua.f=_VEC_ELEM(a,i);ub.f=_VEC_ELEM(b,i);uc.f=_VEC_ELEM(c,i);	\
    ures.i=(ub.i & uc.i) | (ua.i & (~(uc.i)));				\
    _VEC_ELEM(s,i) = ures.f; \
  } \
  return s; \
}

#define BITSELECT(type) \
  BITSELECT_SCALAR(type) \
  BITSELECT_N(type,2) \
  BITSELECT_N(type,3) \
  BITSELECT_N(type,4) \
  BITSELECT_N(type,8) \
  BITSELECT_N(type,16)

#define BITSELECT_FLOAT \
  BITSELECT_FLOAT_SCALAR \
  BITSELECT_FLOAT_N(2) \
  BITSELECT_FLOAT_N(3) \
  BITSELECT_FLOAT_N(4) \
  BITSELECT_FLOAT_N(8) \
  BITSELECT_FLOAT_N(16)

BITSELECT(char)
BITSELECT(uchar)
BITSELECT(short)
BITSELECT(ushort)
BITSELECT(int)
BITSELECT(uint)
BITSELECT(long)
BITSELECT(ulong)
BITSELECT_FLOAT


/* select */
#define SELECT_SCALAR(type, stype, nbbits) \
static inline type __ocl_select_##type##_##stype (type a, type b, stype c) {	\
  return c ? b : a;	\
} \
static inline type __ocl_select_##type##_u##stype (type a, type b, u##stype c) {	\
  return c ? b : a;	\
}

#define SELECT_N(type, stype, n, nbbits) \
static inline type##n __ocl_select_##type##n##_##stype##n (type##n a, type##n b, stype##n c) { \
  type##n s; \
  int i; \
  for (i=0;i<n;i++) { \
    _VEC_ELEM(s,i) =((_VEC_ELEM(c,i)>>(nbbits - 1)) & 0x1)? _VEC_ELEM(b,i) : _VEC_ELEM(a,i); \
  } \
  return s; \
} \
static inline type##n __ocl_select_##type##n##_u##stype##n (type##n a, type##n b, u##stype##n c) { \
  type##n s; \
  int i; \
  for (i=0;i<n;i++) { \
    _VEC_ELEM(s,i) =((_VEC_ELEM(c,i)>>(nbbits - 1)) & 0x1)? _VEC_ELEM(b,i) : _VEC_ELEM(a,i); \
  } \
  return s; \
}

#define SELECT(type,stype,nbbits)  \
  SELECT_SCALAR(type,stype,nbbits) \
  SELECT_N(type,stype,2,nbbits) \
  SELECT_N(type,stype,3,nbbits) \
  SELECT_N(type,stype,4,nbbits) \
  SELECT_N(type,stype,8,nbbits) \
  SELECT_N(type,stype,16,nbbits)

SELECT(char,char,8)
SELECT(uchar,char,8)
SELECT(short,short,16)
SELECT(ushort,short,16)
SELECT(int,int,32)
SELECT(uint,int,32)
SELECT(long,long,64)
SELECT(ulong,long,64)
SELECT(float,int,32)

  
#endif
