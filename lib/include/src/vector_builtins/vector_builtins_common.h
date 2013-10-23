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

#ifndef VECTOR_BUILTINS_COMMON_H
#define VECTOR_BUILTINS_COMMON_H

/* 'min', 'max' and 'clamp' implemented in the 'integer' library */



/* Degrees */

#define DEGREES_SCALAR_N \
static inline float __ocl_degrees_float (float radians) {	\
  return (180.0f / 0x1.921fb6p+1f ) * radians; \
}

#define DEGREES_VECTOR_N(nb_elem) \
static inline float##nb_elem __ocl_degrees_float##nb_elem (float##nb_elem radians) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= ( 180.0f / 0x1.921fb6p+1f ) * radians.element[i] ;	\
  } \
  return s; \
}

DEGREES_SCALAR_N
DEGREES_VECTOR_N(2)
DEGREES_VECTOR_N(3)
DEGREES_VECTOR_N(4)
DEGREES_VECTOR_N(8)
DEGREES_VECTOR_N(16)
  
  
/* MIX : x+(y-x)*a */

#define MIX_SCALAR_N \
static inline float __ocl_mix_float (float x, float y, float a) {	\
  return x+(y-x)*a; \
}

#define MIX_VECTOR_N(nb_elem) \
static inline float##nb_elem __ocl_mix_float##nb_elem (float##nb_elem x, float##nb_elem y, float a) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=x.element[i]+(y.element[i]-x.element[i])*a; \
  } \
  return s; \
}

MIX_SCALAR_N
MIX_VECTOR_N(2)
MIX_VECTOR_N(3)
MIX_VECTOR_N(4)
MIX_VECTOR_N(8)
MIX_VECTOR_N(16)


/* Radians */

#define RADIANS_SCALAR_N \
static inline float __ocl_radians_float (float degrees) {	\
  return ( 0x1.921fb6p+1f / 180.0f ) * degrees; \
}

#define RADIANS_VECTOR_N(nb_elem) \
static inline float##nb_elem __ocl_radians_float##nb_elem (float##nb_elem degrees) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= ( 0x1.921fb6p+1f / 180.0f ) * degrees.element[i] ; \
  } \
  return s; \
}

RADIANS_SCALAR_N
RADIANS_VECTOR_N(2)
RADIANS_VECTOR_N(3)
RADIANS_VECTOR_N(4)
RADIANS_VECTOR_N(8)
RADIANS_VECTOR_N(16)

  
/* STEP */

#define STEP_SCALAR_N \
static inline float __ocl_step_float_float (float edge, float x) { \
  return (x < edge) ? 0.0f : 1.0; \
}

#define STEP_VECTOR_N(nb_elem) \
static inline float##nb_elem __ocl_step_float##nb_elem##_float##nb_elem (float##nb_elem edge, float##nb_elem x) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(x.element[i] < edge.element[i]) ? 0.0f : 1.0; \
  } \
  return s; \
} \
static inline float##nb_elem __ocl_step_float_float##nb_elem (float edge, float##nb_elem x) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(x.element[i] < edge) ? 0.0f : 1.0; \
  } \
  return s; \
}

STEP_SCALAR_N
STEP_VECTOR_N(2)
STEP_VECTOR_N(3)
STEP_VECTOR_N(4)
STEP_VECTOR_N(8)
STEP_VECTOR_N(16)

  
/* SMOOTHSTEP */

#define SMOOTHSTEP_SCALAR_N \
static inline float __ocl_smoothstep_float_float (float edge0, float edge1, float x) { \
  float t= (x <= edge0) ? 0.0f : ((x >= edge1) ? 1.0f : (x-edge0)/(edge1-edge0)) ; \
  return t*t*(3.0f - 2.0f * t);	 \
}

#define SMOOTHSTEP_VECTOR_N(nb_elem) \
static inline float##nb_elem __ocl_smoothstep_float##nb_elem##_float##nb_elem (float##nb_elem edge0, float##nb_elem edge1, float##nb_elem x) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    float t= (x.element[i] <= edge0.element[i]) ? 0.0f : \
             ((x.element[i] >= edge1.element[i]) ? 1.0f : (x.element[i]-edge0.element[i])/(edge1.element[i]-edge0.element[i])) ; \
    s.element[i]= t*t*(3.0f - 2.0f * t); \
  } \
  return s; \
} \
static inline float##nb_elem __ocl_smoothstep_float_float##nb_elem (float edge0, float edge1, float##nb_elem x) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    float t= (x.element[i] <= edge0) ? 0.0f : ((x.element[i] >= edge1) ? 1.0f : (x.element[i]-edge0)/(edge1-edge0)) ; \
    s.element[i]= t*t*(3.0f - 2.0f * t); \
  } \
  return s; \
}

SMOOTHSTEP_SCALAR_N
SMOOTHSTEP_VECTOR_N(2)
SMOOTHSTEP_VECTOR_N(3)
SMOOTHSTEP_VECTOR_N(4)
SMOOTHSTEP_VECTOR_N(8)
SMOOTHSTEP_VECTOR_N(16)


/* Sign */
/* [TBW] Note: NaN should be managed */
  
#define SIGN_SCALAR_N \
static inline float __ocl_sign_float (float x) {	\
  return (x>0.0f)? 1.0f : (x<0.0f) ? -1.0f : x ;  \
}

#define SIGN_VECTOR_N(nb_elem) \
static inline float##nb_elem __ocl_sign_float##nb_elem (float##nb_elem x) { \
  float##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= (x.element[i]>0.0f)? 1.0f : (x.element[i]<0.0f) ? -1.0f : x.element[i] ; \
  } \
  return s; \
}

SIGN_SCALAR_N
SIGN_VECTOR_N(2)
SIGN_VECTOR_N(3)
SIGN_VECTOR_N(4)
SIGN_VECTOR_N(8)
SIGN_VECTOR_N(16)

#endif
