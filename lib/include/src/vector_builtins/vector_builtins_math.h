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
/* OpenCL math functions */

#ifndef VECTOR_BUILTINS_MATH_H
#define VECTOR_BUILTINS_MATH_H


// 1 float parameter builtin
#define MATH_BUILTIN_PARAM_F_VECTOR_N_SRC(name,name_to_call,type,nb_elem,postfix) \
static inline type##nb_elem __ocl_##name##_##type##nb_elem (type##nb_elem src) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=name_to_call##postfix (src.element[i]);	\
  } \
  return s; \
}

#define MATH_BUILTIN_PARAM_F_SCALAR_N_SRC(name,name_to_call,type,postfix) \
static inline type __ocl_##name##_##type (type src) { \
  return name_to_call##postfix (src);	      \
}

#define MATH_BUILTIN_PARAM_F_VECTOR_SRC(name,name_to_call,type,postfix)	\
  MATH_BUILTIN_PARAM_F_SCALAR_N_SRC(name,name_to_call,type,postfix)    \
  MATH_BUILTIN_PARAM_F_VECTOR_N_SRC(name,name_to_call,type,2,postfix)    \
  MATH_BUILTIN_PARAM_F_VECTOR_N_SRC(name,name_to_call,type,3,postfix)    \
  MATH_BUILTIN_PARAM_F_VECTOR_N_SRC(name,name_to_call,type,4,postfix)    \
  MATH_BUILTIN_PARAM_F_VECTOR_N_SRC(name,name_to_call,type,8,postfix)    \
  MATH_BUILTIN_PARAM_F_VECTOR_N_SRC(name,name_to_call,type,16,postfix)

#define MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(name)	\
  MATH_BUILTIN_PARAM_F_VECTOR_SRC(name,name,float,f)

#define MATH_BUILTIN_PARAM_F_VECTOR(name,name_to_call)		\
  MATH_BUILTIN_PARAM_F_VECTOR_SRC(name,name_to_call,float,f)



// 2 float parameter builtin
#define MATH_BUILTIN_PARAM_FF_VECTOR_N_SRC(name,name_to_call,type,nb_elem,postfix) \
static inline type##nb_elem __ocl_##name##_##type##nb_elem (type##nb_elem src1, type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=name_to_call##postfix (src1.element[i], src2.element[i]);	\
  } \
  return s; \
}
#define MATH_BUILTIN_PARAM_FF_SCALAR_N_SRC(name,name_to_call,type,postfix) \
  static inline type __ocl_##name##_##type (type src1, type src2) {			\
    return name_to_call##postfix (src1,src2);				\
}

#define MATH_BUILTIN_PARAM_FF_VECTOR_SRC(name,name_to_call,type,postfix)	\
  MATH_BUILTIN_PARAM_FF_SCALAR_N_SRC(name,name_to_call,type,postfix)    \
  MATH_BUILTIN_PARAM_FF_VECTOR_N_SRC(name,name_to_call,type,2,postfix)    \
  MATH_BUILTIN_PARAM_FF_VECTOR_N_SRC(name,name_to_call,type,3,postfix)    \
  MATH_BUILTIN_PARAM_FF_VECTOR_N_SRC(name,name_to_call,type,4,postfix)    \
  MATH_BUILTIN_PARAM_FF_VECTOR_N_SRC(name,name_to_call,type,8,postfix)    \
  MATH_BUILTIN_PARAM_FF_VECTOR_N_SRC(name,name_to_call,type,16,postfix)

#define MATH_BUILTIN_PARAM_FF_VECTOR_SIMPLE(name)	\
  MATH_BUILTIN_PARAM_FF_VECTOR_SRC(name,name,float,f)

#define MATH_BUILTIN_PARAM_FF_VECTOR(name,name_to_call)		\
  MATH_BUILTIN_PARAM_FF_VECTOR_SRC(name,name_to_call,float,f)

  
// 2 float parameter builtin (variant with the second which can always be float scalar)
#define MATH_BUILTIN_PARAM_FFvar_VECTOR_N_SRC(name,name_to_call,type,nb_elem,postfix) \
static inline type##nb_elem __ocl_##name##_##type##nb_elem##_##type##nb_elem (type##nb_elem src1, type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=name_to_call##postfix (src1.element[i], src2.element[i]); \
  } \
  return s; \
} \
static inline type##nb_elem __ocl_##name##_##type##nb_elem##_##type (type##nb_elem src1, type src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=name_to_call##postfix (src1.element[i], src2);	\
  } \
  return s; \
}

#define MATH_BUILTIN_PARAM_FFvar_SCALAR_N_SRC(name,name_to_call,type,postfix) \
  static inline type __ocl_##name##_##type##_##type (type src1, type src2) { \
    return name_to_call##postfix (src1,src2);				\
}

#define MATH_BUILTIN_PARAM_FFvar_VECTOR_SRC(name,name_to_call,type,postfix)	\
  MATH_BUILTIN_PARAM_FFvar_SCALAR_N_SRC(name,name_to_call,type,postfix)    \
  MATH_BUILTIN_PARAM_FFvar_VECTOR_N_SRC(name,name_to_call,type,2,postfix)    \
  MATH_BUILTIN_PARAM_FFvar_VECTOR_N_SRC(name,name_to_call,type,3,postfix)    \
  MATH_BUILTIN_PARAM_FFvar_VECTOR_N_SRC(name,name_to_call,type,4,postfix)    \
  MATH_BUILTIN_PARAM_FFvar_VECTOR_N_SRC(name,name_to_call,type,8,postfix)    \
  MATH_BUILTIN_PARAM_FFvar_VECTOR_N_SRC(name,name_to_call,type,16,postfix)

#define MATH_BUILTIN_PARAM_FFvar_VECTOR_SIMPLE(name)	\
  MATH_BUILTIN_PARAM_FFvar_VECTOR_SRC(name,name,float,f)

#define MATH_BUILTIN_PARAM_FFvar_VECTOR(name,name_to_call) \
  MATH_BUILTIN_PARAM_FFvar_VECTOR_SRC(name,name_to_call,float,f)


// 3 float parameter builtin
#define MATH_BUILTIN_PARAM_FFF_VECTOR_N_SRC(name,name_to_call,type,nb_elem,postfix) \
static inline type##nb_elem __ocl_##name##_##type##nb_elem (type##nb_elem src1, type##nb_elem src2, type##nb_elem src3) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=name_to_call##postfix (src1.element[i], src2.element[i], src3.element[i]);	\
  } \
  return s; \
}
#define MATH_BUILTIN_PARAM_FFF_SCALAR_N_SRC(name,name_to_call,type,postfix) \
  static inline type __ocl_##name##_##type (type src1, type src2, type src3) {		\
    return name_to_call##postfix (src1,src2,src3);				\
}

#define MATH_BUILTIN_PARAM_FFF_VECTOR_SRC(name,name_to_call,type,postfix)	\
  MATH_BUILTIN_PARAM_FFF_SCALAR_N_SRC(name,name_to_call,type,postfix)    \
  MATH_BUILTIN_PARAM_FFF_VECTOR_N_SRC(name,name_to_call,type,2,postfix)    \
  MATH_BUILTIN_PARAM_FFF_VECTOR_N_SRC(name,name_to_call,type,3,postfix)    \
  MATH_BUILTIN_PARAM_FFF_VECTOR_N_SRC(name,name_to_call,type,4,postfix)    \
  MATH_BUILTIN_PARAM_FFF_VECTOR_N_SRC(name,name_to_call,type,8,postfix)    \
  MATH_BUILTIN_PARAM_FFF_VECTOR_N_SRC(name,name_to_call,type,16,postfix)

#define MATH_BUILTIN_PARAM_FFF_VECTOR_SIMPLE(name)	\
  MATH_BUILTIN_PARAM_FFF_VECTOR_SRC(name,name,float,f)

#define MATH_BUILTIN_PARAM_FFF_VECTOR(name,name_to_call)		\
  MATH_BUILTIN_PARAM_FFF_VECTOR_SRC(name,name_to_call,float,f)




//------------------------------------------------------------------
//                     Math builtin functions
//------------------------------------------------------------------

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(acos);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(acosh);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(asin);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(asinh);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(atan);
MATH_BUILTIN_PARAM_FF_VECTOR_SIMPLE(atan2);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(atanh);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(cbrt);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(ceil);
MATH_BUILTIN_PARAM_FF_VECTOR_SIMPLE(copysign);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(cos);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(cosh);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(erfc);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(erf);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(exp);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(exp2);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(expm1);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(fabs);
MATH_BUILTIN_PARAM_FF_VECTOR_SIMPLE(fdim);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(floor);
MATH_BUILTIN_PARAM_FFF_VECTOR_SIMPLE(fma);
MATH_BUILTIN_PARAM_FFvar_VECTOR_SIMPLE(fmax);
MATH_BUILTIN_PARAM_FFvar_VECTOR_SIMPLE(fmin);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(log);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(log2);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(log10);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(log1p);

// Note: fix for xp70 which is not logb defined
MATH_BUILTIN_PARAM_F_VECTOR(logb,ilogb)
//MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(logb);

MATH_BUILTIN_PARAM_FF_VECTOR_SIMPLE(nextafter);
MATH_BUILTIN_PARAM_FF_VECTOR_SIMPLE(pow);

MATH_BUILTIN_PARAM_FF_VECTOR(powr,pow);
MATH_BUILTIN_PARAM_FF_VECTOR_SIMPLE(remainder);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(rint);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(round);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(sin);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(sinh);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(sqrt);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(tan);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(tanh);

MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(tgamma);
MATH_BUILTIN_PARAM_F_VECTOR_SIMPLE(trunc);


//------------------------------------------------------------------
//                          Division
//------------------------------------------------------------------

// 2 float parameter builtin
#define MATH_BUILTIN_DIVIDE_VECTOR_N_SRC(name,type,nb_elem) \
static inline type##nb_elem __ocl_##name##_##type##nb_elem (type##nb_elem src1, type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=src1.element[i] / src2.element[i];	\
  } \
  return s; \
}
#define MATH_BUILTIN_DIVIDE_SCALAR_N_SRC(name,type) \
  static inline type __ocl_##name##_##type (type src1, type src2) {   \
    return src1 / src2;	 \
}

#define MATH_BUILTIN_DIVIDE_VECTOR_SRC(name,type)	\
  MATH_BUILTIN_DIVIDE_SCALAR_N_SRC(name,type)      \
  MATH_BUILTIN_DIVIDE_VECTOR_N_SRC(name,type,2)    \
  MATH_BUILTIN_DIVIDE_VECTOR_N_SRC(name,type,3)    \
  MATH_BUILTIN_DIVIDE_VECTOR_N_SRC(name,type,4)    \
  MATH_BUILTIN_DIVIDE_VECTOR_N_SRC(name,type,8)    \
  MATH_BUILTIN_DIVIDE_VECTOR_N_SRC(name,type,16)

#define MATH_BUILTIN_DIVIDE_VECTOR(name)		\
  MATH_BUILTIN_DIVIDE_VECTOR_SRC(name,float)



//------------------------------------------------------------------
//                  Half Math builtin functions
//------------------------------------------------------------------

MATH_BUILTIN_PARAM_F_VECTOR(half_cos,cos);
MATH_BUILTIN_DIVIDE_VECTOR(half_divide)
MATH_BUILTIN_PARAM_F_VECTOR(half_exp,exp);
MATH_BUILTIN_PARAM_F_VECTOR(half_exp2,exp2);

MATH_BUILTIN_PARAM_F_VECTOR(half_log,log);
MATH_BUILTIN_PARAM_F_VECTOR(half_log2,log2);
MATH_BUILTIN_PARAM_F_VECTOR(half_log10,log10);

MATH_BUILTIN_PARAM_F_VECTOR(half_sin,sin);
MATH_BUILTIN_PARAM_F_VECTOR(half_sqrt,sqrt);
MATH_BUILTIN_PARAM_F_VECTOR(half_tan,tan);


//------------------------------------------------------------------
//                  Native Math builtin functions
//------------------------------------------------------------------

MATH_BUILTIN_PARAM_F_VECTOR(native_cos,cos);
MATH_BUILTIN_DIVIDE_VECTOR(native_divide)
MATH_BUILTIN_PARAM_F_VECTOR(native_exp,exp);
MATH_BUILTIN_PARAM_F_VECTOR(native_exp2,exp2);

MATH_BUILTIN_PARAM_F_VECTOR(native_log,log);
MATH_BUILTIN_PARAM_F_VECTOR(native_log2,log2);
MATH_BUILTIN_PARAM_F_VECTOR(native_log10,log10);

MATH_BUILTIN_PARAM_F_VECTOR(native_sin,sin);
MATH_BUILTIN_PARAM_F_VECTOR(native_sqrt,sqrt);
MATH_BUILTIN_PARAM_F_VECTOR(native_tan,tan);

#endif

