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


#ifndef VECTOR_BUILTINS_GEOMETRIC_H
#define VECTOR_BUILTINS_GEOMETRIC_H


/* cross product:
   Cx = AyBz - AzBy
   Cy = AzBx - AxBz
   Cz = AxBy - AyBx 
*/

static inline float3 __ocl_cross_float3 (float3 src1, float3 src2) {
  float3 s; \
  s.element[0]=src1.element[1]*src2.element[2]-src1.element[2]*src2.element[1];
  s.element[1]=src1.element[2]*src2.element[0]-src1.element[0]*src2.element[2];
  s.element[2]=src1.element[0]*src2.element[1]-src1.element[1]*src2.element[0];
  return s;
}
static inline float4 __ocl_cross_float4 (float4 src1, float4 src2) {
  float4 s; \
  s.element[0]=src1.element[1]*src2.element[2]-src1.element[2]*src2.element[1];
  s.element[1]=src1.element[2]*src2.element[0]-src1.element[0]*src2.element[2];
  s.element[2]=src1.element[0]*src2.element[1]-src1.element[1]*src2.element[0];
  s.element[3]=0;
  return s;
}


/* dot builtin function */

#define DOT_VECTOR_N(nb_elem) \
static inline float __ocl_dot##_##float##nb_elem (float##nb_elem src1, float##nb_elem src2) {	\
  int i; \
  float acc=0.0f; \
  for (i=0;i<nb_elem;i++) { \
    acc += src1.element[i]*src2.element[i]; \
  } \
  return acc; \
}

#define DOT_SCALAR \
static inline float __ocl_dot##_##float (float src1, float src2) {	\
  return src1*src2; \
}

DOT_SCALAR
DOT_VECTOR_N(2)
DOT_VECTOR_N(3)
DOT_VECTOR_N(4)

  
/* 'distance' builtin function */

#define DISTANCE_VECTOR_N(name,nb_elem) \
static inline float __ocl_##name##_##float##nb_elem (float##nb_elem src1, float##nb_elem src2) {	\
  int i; \
  double acc=0.0; \
  for (i=0;i<nb_elem;i++) { \
    acc += pow(src1.element[i]-src2.element[i], 2.0);	\
  } \
  return sqrt(acc); \
}

#define DISTANCE_SCALAR(name) \
static inline float __ocl_##name##_##float (float src1, float src2) {	\
  float t=src1-src2; \
  return t<0 ? -t : t; \
}

DISTANCE_SCALAR(distance)
DISTANCE_VECTOR_N(distance,2)
DISTANCE_VECTOR_N(distance,3)
DISTANCE_VECTOR_N(distance,4)

DISTANCE_SCALAR(fast_distance)
DISTANCE_VECTOR_N(fast_distance,2)
DISTANCE_VECTOR_N(fast_distance,3)
DISTANCE_VECTOR_N(fast_distance,4)

/* 'length' builtin function */

#define LENGTH_VECTOR_N(name,nb_elem) \
static inline float __ocl_##name##_##float##nb_elem (float##nb_elem src) {	\
  int i; \
  double acc=0.0; \
  for (i=0;i<nb_elem;i++) { \
    acc += powf(src.element[i], 2.0);		\
  } \
  return sqrt(acc); \
}

#define LENGTH_SCALAR(name) \
static inline float __ocl_##name##_##float (float src) { \
  return src < 0 ? -src : src;	\
}

LENGTH_SCALAR(length)
LENGTH_VECTOR_N(length,2)
LENGTH_VECTOR_N(length,3)
LENGTH_VECTOR_N(length,4)

LENGTH_SCALAR(fast_length)
LENGTH_VECTOR_N(fast_length,2)
LENGTH_VECTOR_N(fast_length,3)
LENGTH_VECTOR_N(fast_length,4)

  
/* 'normalize' builtin function */

#define NORMALIZE_VECTOR_N(name,nb_elem) \
static inline float##nb_elem __ocl_##name##_##float##nb_elem (float##nb_elem src) { \
  float##nb_elem s; \
  float length= __ocl_length_float##nb_elem (src); \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i] = src.element[i]/length; \
  } \
  return s; \
}

#define NORMALIZE_SCALAR(name) \
static inline float __ocl_##name##_##float (float src) { \
  return 1.0f; \
}

NORMALIZE_SCALAR(normalize)
NORMALIZE_VECTOR_N(normalize,2)
NORMALIZE_VECTOR_N(normalize,3)
NORMALIZE_VECTOR_N(normalize,4)

NORMALIZE_SCALAR(fast_normalize)
NORMALIZE_VECTOR_N(fast_normalize,2)
NORMALIZE_VECTOR_N(fast_normalize,3)
NORMALIZE_VECTOR_N(fast_normalize,4)


#endif
