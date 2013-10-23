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

/* OpenCL integer functions */

#ifndef VECTOR_BUILTINS_INTEGER_H
#define VECTOR_BUILTINS_INTEGER_H


/* Absolute value */

#define ABS_SCALAR_N_SRC(utype,type) \
static inline utype __ocl_abs_##type (type src) { \
  return (utype)(src>0?src:-src);	 \
}

#define ABS_VECTOR_N_SRC(utype,type,nb_elem) \
static inline utype##nb_elem __ocl_abs_##type##nb_elem (type##nb_elem src) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(utype)((src.element[i]>0)?src.element[i]:(-src.element[i])); \
  } \
  return s; \
}

#define ABS_SCALAR_N_SRC_U(utype) \
static inline utype __ocl_abs_##utype (utype src) { \
  return src; \
}

#define ABS_VECTOR_N_SRC_U(utype,nb_elem) \
static inline utype##nb_elem __ocl_abs_##utype##nb_elem (utype##nb_elem src) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=src.element[i]; \
  } \
  return s; \
}

#define ABS_SRC(utype,type) \
   ABS_SCALAR_N_SRC(utype,type)	\
   ABS_SCALAR_N_SRC_U(utype)	\
   ABS_VECTOR_N_SRC(utype,type,2) \
   ABS_VECTOR_N_SRC_U(utype,2)	\
   ABS_VECTOR_N_SRC(utype,type,3) \
   ABS_VECTOR_N_SRC_U(utype,3)	\
   ABS_VECTOR_N_SRC(utype,type,4) \
   ABS_VECTOR_N_SRC_U(utype,4)	\
   ABS_VECTOR_N_SRC(utype,type,8) \
   ABS_VECTOR_N_SRC_U(utype,8)	\
   ABS_VECTOR_N_SRC(utype,type,16)\
   ABS_VECTOR_N_SRC_U(utype,16)

ABS_SRC(uchar,char)
ABS_SRC(ushort,short)
ABS_SRC(uint,int)
ABS_SRC(ulong,long)


  
/* Absolute value of difference */

#define ABS_DIFF_SCALAR_N_SRC(utype,type) \
static inline utype __ocl_abs_diff_##type (type src1,type src2) { \
  if (src1<0) { \
    if (src2<0) { \
      return src1 < src2 ? src2-src1 : src1-src2; \
    } \
    else { \
      return (utype)(-(src1+1))+(utype)src2+1; \
    } \
  } \
  else { \
    if (src2<0) { \
      return (utype)src1+1+(utype)(-(src2+1)); \
    } \
    else { \
      return src1 < src2 ? src2-src1 : src1-src2; \
    } \
  } \
}

#define ABS_DIFF_VECTOR_N_SRC(utype,type,nb_elem) \
static inline utype##nb_elem __ocl_abs_diff_##type##nb_elem (type##nb_elem src1, type##nb_elem src2) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=__ocl_abs_diff_##type (src1.element[i],src2.element[i]); \
  } \
  return s; \
}

#define ABS_DIFF_SCALAR_N_SRC_U(utype) \
static inline utype __ocl_abs_diff_##utype (utype src1,utype src2) {	\
  return (src1>=src2) ? src1-src2 : src2-src1;				\
}

#define ABS_DIFF_VECTOR_N_SRC_U(utype,nb_elem) \
static inline utype##nb_elem __ocl_abs_diff_##utype##nb_elem (utype##nb_elem src1, utype##nb_elem src2) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i] = (src1.element[i]>=src2.element[i]) ? src1.element[i]-src2.element[i] : src2.element[i]-src1.element[i]; \
  } \
  return s; \
}

#define ABS_DIFF_SRC(utype,type) \
   ABS_DIFF_SCALAR_N_SRC(utype,type)	\
   ABS_DIFF_SCALAR_N_SRC_U(utype)	\
   ABS_DIFF_VECTOR_N_SRC(utype,type,2)	\
   ABS_DIFF_VECTOR_N_SRC_U(utype,2)	\
   ABS_DIFF_VECTOR_N_SRC(utype,type,3)	\
   ABS_DIFF_VECTOR_N_SRC_U(utype,3)	\
   ABS_DIFF_VECTOR_N_SRC(utype,type,4)	\
   ABS_DIFF_VECTOR_N_SRC_U(utype,4)	\
   ABS_DIFF_VECTOR_N_SRC(utype,type,8)	\
   ABS_DIFF_VECTOR_N_SRC_U(utype,8)	\
   ABS_DIFF_VECTOR_N_SRC(utype,type,16) \
   ABS_DIFF_VECTOR_N_SRC_U(utype,16)

ABS_DIFF_SRC(uchar,char)
ABS_DIFF_SRC(ushort,short)
ABS_DIFF_SRC(uint,int)
ABS_DIFF_SRC(ulong,long)


  
/* Saturating addition */

#define ADD_SAT_SCALAR_N_SRC(type,TYPENAME) \
  static inline type __ocl_add_sat_##type (type src1,type src2) {	\
  if (src1<0) { \
    if (src2<0) { \
      return (src2<=(OCL_##TYPENAME##_MIN-src1))?OCL_##TYPENAME##_MIN:(src1+src2); \
    } \
    else { \
      return src1+src2; \
    } \
  } \
  else { \
    if (src2<0) { \
      return src1+src2; \
    } \
    else { \
      return (src2>=(OCL_##TYPENAME##_MAX-src1))?OCL_##TYPENAME##_MAX:(src1+src2); \
    } \
  } \
}

#define ADD_SAT_VECTOR_N_SRC(type,nb_elem,TYPENAME) \
static inline type##nb_elem __ocl_add_sat_##type##nb_elem (type##nb_elem src1, type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=__ocl_add_sat_##type (src1.element[i],src2.element[i]); \
  } \
  return s; \
}

#define ADD_SAT_SCALAR_N_SRC_U(utype,TYPENAME) \
static inline utype __ocl_add_sat_##utype (utype src1,utype src2) {	\
  return (src2>=(OCL_##TYPENAME##_MAX-src1))?OCL_##TYPENAME##_MAX:(src1+src2); \
}

#define ADD_SAT_VECTOR_N_SRC_U(utype,nb_elem,TYPENAME) \
static inline utype##nb_elem __ocl_add_sat_##utype##nb_elem (utype##nb_elem src1, utype##nb_elem src2) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]= (src2.element[i]>=(OCL_##TYPENAME##_MAX-src1.element[i]))? \
      OCL_##TYPENAME##_MAX:(src1.element[i]+src2.element[i]); \
  } \
  return s; \
}

#define ADD_SAT_SRC(utype,type,UTYPENAME,TYPENAME)	\
   ADD_SAT_SCALAR_N_SRC(type,TYPENAME)	\
   ADD_SAT_SCALAR_N_SRC_U(utype,UTYPENAME)	\
   ADD_SAT_VECTOR_N_SRC(type,2,TYPENAME)	\
   ADD_SAT_VECTOR_N_SRC_U(utype,2,UTYPENAME)	\
   ADD_SAT_VECTOR_N_SRC(type,3,TYPENAME)	\
   ADD_SAT_VECTOR_N_SRC_U(utype,3,UTYPENAME)	\
   ADD_SAT_VECTOR_N_SRC(type,4,TYPENAME)	\
   ADD_SAT_VECTOR_N_SRC_U(utype,4,UTYPENAME)	\
   ADD_SAT_VECTOR_N_SRC(type,8,TYPENAME)	\
   ADD_SAT_VECTOR_N_SRC_U(utype,8,UTYPENAME)	\
   ADD_SAT_VECTOR_N_SRC(type,16,TYPENAME) \
   ADD_SAT_VECTOR_N_SRC_U(utype,16,UTYPENAME)

ADD_SAT_SRC(uchar,char,UCHAR,SCHAR)
ADD_SAT_SRC(ushort,short,USHRT,SHRT)
ADD_SAT_SRC(uint,int,UINT,INT)
ADD_SAT_SRC(ulong,long,ULONG,LONG)


/* Saturating subtraction */

#define SUB_SAT_SCALAR_N_SRC(type,TYPENAME) \
static inline type __ocl_sub_sat_##type (type src1,type src2) {	\
  if (src1<0) { \
    if (src2<0) { \
      return src1-src2; \
    } \
    else { \
      return (src2>=(src1-OCL_##TYPENAME##_MIN))?OCL_##TYPENAME##_MIN:(src1-src2); \
    } \
  } \
  else { \
    if (src2<0) { \
      return (src2<=(src1-OCL_##TYPENAME##_MAX))?OCL_##TYPENAME##_MAX:(src1-src2); \
    } \
    else { \
      return src1-src2; \
    } \
  } \
}

#define SUB_SAT_VECTOR_N_SRC(type,nb_elem,TYPENAME) \
static inline type##nb_elem __ocl_sub_sat_##type##nb_elem (type##nb_elem src1, type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=__ocl_sub_sat_##type (src1.element[i],src2.element[i]); \
  } \
  return s; \
}

#define SUB_SAT_SCALAR_N_SRC_U(utype,TYPENAME) \
static inline utype __ocl_sub_sat_##utype (utype src1,utype src2) {	\
  return (src2>=src1)?0:(src1-src2); \
}

#define SUB_SAT_VECTOR_N_SRC_U(utype,nb_elem,TYPENAME) \
static inline utype##nb_elem __ocl_sub_sat_##utype##nb_elem (utype##nb_elem src1, utype##nb_elem src2) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(src2.element[i]>=src1.element[i])? \
                 0:(src1.element[i]-src2.element[i]); \
  } \
  return s; \
}

#define SUB_SAT_SRC(utype,type,UTYPENAME,TYPENAME) \
   SUB_SAT_SCALAR_N_SRC(type,TYPENAME)		\
   SUB_SAT_SCALAR_N_SRC_U(utype,UTYPENAME)	\
   SUB_SAT_VECTOR_N_SRC(type,2,TYPENAME)	\
   SUB_SAT_VECTOR_N_SRC_U(utype,2,UTYPENAME)	\
   SUB_SAT_VECTOR_N_SRC(type,3,TYPENAME)	\
   SUB_SAT_VECTOR_N_SRC_U(utype,3,UTYPENAME)	\
   SUB_SAT_VECTOR_N_SRC(type,4,TYPENAME)	\
   SUB_SAT_VECTOR_N_SRC_U(utype,4,UTYPENAME)	\
   SUB_SAT_VECTOR_N_SRC(type,8,TYPENAME)	\
   SUB_SAT_VECTOR_N_SRC_U(utype,8,UTYPENAME)	\
   SUB_SAT_VECTOR_N_SRC(type,16,TYPENAME)	\
   SUB_SAT_VECTOR_N_SRC_U(utype,16,UTYPENAME)

SUB_SAT_SRC(uchar,char,UCHAR,SCHAR)
SUB_SAT_SRC(ushort,short,USHRT,SHRT)
SUB_SAT_SRC(uint,int,UINT,INT)
SUB_SAT_SRC(ulong,long,ULONG,LONG)


  
/* HADD : (x+y)>>1 */

#define HADD_SCALAR_N_SRC(type) \
static inline type __ocl_hadd_##type (type src1, type src2) { \
  type carry=(src1&1)&(src2&1); \
  return (src1>>1)+(src2>>1)+carry; \
}

#define HADD_VECTOR_N_SRC(type,nb_elem) \
static inline type##nb_elem __ocl_hadd_##type##nb_elem (type##nb_elem src1,type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    type carry=(src1.element[i]&1)&(src2.element[i]&1); \
    s.element[i]=(src1.element[i]>>1)+(src2.element[i]>>1)+carry; \
  } \
  return s; \
}

#define HADD_SRC(type) 	     \
   HADD_SCALAR_N_SRC(type)   \
   HADD_VECTOR_N_SRC(type,2) \
   HADD_VECTOR_N_SRC(type,3) \
   HADD_VECTOR_N_SRC(type,4) \
   HADD_VECTOR_N_SRC(type,8) \
   HADD_VECTOR_N_SRC(type,16)

HADD_SRC(char)
HADD_SRC(uchar)
HADD_SRC(short)
HADD_SRC(ushort)
HADD_SRC(int)
HADD_SRC(uint)
HADD_SRC(long)
HADD_SRC(ulong)

  
/* RHADD : (x+y+1)>>1 */

#define RHADD_SCALAR_N_SRC(type) \
static inline type __ocl_rhadd_##type (type src1, type src2) { \
  type carry=(src1&1)|(src2&1); \
  return (src1>>1)+(src2>>1)+carry; \
}

#define RHADD_VECTOR_N_SRC(type,nb_elem) \
static inline type##nb_elem __ocl_rhadd_##type##nb_elem (type##nb_elem src1,type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    type carry=(src1.element[i]&1)|(src2.element[i]&1); \
    s.element[i]=(src1.element[i]>>1)+(src2.element[i]>>1)+carry; \
  } \
  return s; \
}

#define RHADD_SRC(type)       \
   RHADD_SCALAR_N_SRC(type)   \
   RHADD_VECTOR_N_SRC(type,2) \
   RHADD_VECTOR_N_SRC(type,3) \
   RHADD_VECTOR_N_SRC(type,4) \
   RHADD_VECTOR_N_SRC(type,8) \
   RHADD_VECTOR_N_SRC(type,16)

RHADD_SRC(char)
RHADD_SRC(uchar)
RHADD_SRC(short)
RHADD_SRC(ushort)
RHADD_SRC(int)
RHADD_SRC(uint)
RHADD_SRC(long)
RHADD_SRC(ulong)


/* CLZ */

#define CLZ_SCALAR_N_SRC(type,type_size) \
static inline type __ocl_clz_##type (type src) { \
  type bit=((type)1)<<(type_size-1);	   \
  int n; \
  for(n=0;n<type_size;n++,bit>>=1) { \
    if (src&bit) break; \
  } \
  return n; \
}

#define CLZ_VECTOR_N_SRC(type,type_size,nb_elem) \
static inline type##nb_elem __ocl_clz_##type##nb_elem (type##nb_elem src) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    type bit=((type)1)<<(type_size-1); \
    int n=0; \
    for(n=0;n<type_size;n++,bit>>=1) { \
      if (src.element[i]&bit) break; \
    } \
    s.element[i]=n; \
  } \
  return s; \
}

#define CLZ_SRC(type,type_size)	    \
   CLZ_SCALAR_N_SRC(type,type_size)   \
   CLZ_VECTOR_N_SRC(type,type_size,2) \
   CLZ_VECTOR_N_SRC(type,type_size,3) \
   CLZ_VECTOR_N_SRC(type,type_size,4) \
   CLZ_VECTOR_N_SRC(type,type_size,8) \
   CLZ_VECTOR_N_SRC(type,type_size,16)

CLZ_SRC(char,8)
CLZ_SRC(uchar,8)
CLZ_SRC(short,16)
CLZ_SRC(ushort,16)
CLZ_SRC(int,32)
CLZ_SRC(uint,32)
CLZ_SRC(long,64)
CLZ_SRC(ulong,64)
  

/* MAX */

#define MAX_SCALAR_N_SRC(type) \
static inline type __ocl_max_##type##_##type (type src1, type src2) { \
  return (src1>src2)?src1:src2; \
}

#define MAX_VECTOR_N_SRC(type,nb_elem) \
static inline type##nb_elem __ocl_max_##type##nb_elem##_##type##nb_elem (type##nb_elem src1,type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(src1.element[i]>src2.element[i])?src1.element[i]:src2.element[i]; \
  } \
  return s; \
} \
static inline type##nb_elem __ocl_max_##type##nb_elem##_##type (type##nb_elem src1,type src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(src1.element[i]>src2)?src1.element[i]:src2; \
  } \
  return s; \
}

#define MAX_SRC(type) 	    \
   MAX_SCALAR_N_SRC(type)   \
   MAX_VECTOR_N_SRC(type,2) \
   MAX_VECTOR_N_SRC(type,3) \
   MAX_VECTOR_N_SRC(type,4) \
   MAX_VECTOR_N_SRC(type,8) \
   MAX_VECTOR_N_SRC(type,16)

MAX_SRC(char)
MAX_SRC(uchar)
MAX_SRC(short)
MAX_SRC(ushort)
MAX_SRC(int)
MAX_SRC(uint)
MAX_SRC(long)
MAX_SRC(ulong)
MAX_SRC(float)

  
/* MIN */

#define MIN_SCALAR_N_SRC(type) \
static inline type __ocl_min_##type##_##type (type src1, type src2) { \
  return (src1<src2)?src1:src2; \
}

#define MIN_VECTOR_N_SRC(type,nb_elem) \
static inline type##nb_elem __ocl_min_##type##nb_elem##_##type##nb_elem (type##nb_elem src1,type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(src1.element[i]<src2.element[i])?src1.element[i]:src2.element[i]; \
  } \
  return s; \
} \
static inline type##nb_elem __ocl_min_##type##nb_elem##_##type (type##nb_elem src1,type src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(src1.element[i]<src2)?src1.element[i]:src2; \
  } \
  return s; \
}

#define MIN_SRC(type) 	    \
   MIN_SCALAR_N_SRC(type)   \
   MIN_VECTOR_N_SRC(type,2) \
   MIN_VECTOR_N_SRC(type,3) \
   MIN_VECTOR_N_SRC(type,4) \
   MIN_VECTOR_N_SRC(type,8) \
   MIN_VECTOR_N_SRC(type,16)

MIN_SRC(char)
MIN_SRC(uchar)
MIN_SRC(short)
MIN_SRC(ushort)
MIN_SRC(int)
MIN_SRC(uint)
MIN_SRC(long)
MIN_SRC(ulong)
MIN_SRC(float)

  
/* CLAMP */

#define CLAMP_SCALAR_N_SRC(type) \
static inline type __ocl_clamp_##type##_##type (type src, type minval, type maxval) { \
  if (src<minval) { \
    return minval; \
  } \
  if (src>maxval)  { \
    return maxval; \
  } \
  return src; \
}

#define CLAMP_VECTOR_N_SRC(type,nb_elem) \
static inline type##nb_elem __ocl_clamp_##type##nb_elem##_##type##nb_elem (type##nb_elem src,type##nb_elem minval ,type##nb_elem maxval) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=__ocl_clamp_##type##_##type(src.element[i],minval.element[i],maxval.element[i]); \
  } \
  return s; \
} \
static inline type##nb_elem __ocl_clamp_##type##nb_elem##_##type (type##nb_elem src, type minval, type maxval) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=__ocl_clamp_##type##_##type(src.element[i],minval,maxval); \
  } \
  return s; \
}

#define CLAMP_SRC(type)       \
   CLAMP_SCALAR_N_SRC(type)   \
   CLAMP_VECTOR_N_SRC(type,2) \
   CLAMP_VECTOR_N_SRC(type,3) \
   CLAMP_VECTOR_N_SRC(type,4) \
   CLAMP_VECTOR_N_SRC(type,8) \
   CLAMP_VECTOR_N_SRC(type,16)

CLAMP_SRC(char)
CLAMP_SRC(uchar)
CLAMP_SRC(short)
CLAMP_SRC(ushort)
CLAMP_SRC(int)
CLAMP_SRC(uint)
CLAMP_SRC(long)
CLAMP_SRC(ulong)
CLAMP_SRC(float)


/* rotate left */

#define ROTATE_SCALAR_N_SRC(type,type_size,type_size_bit) \
static inline type __ocl_rotate_##type (type src1, type src2) { \
  type lshift  = src2 & ((1<<type_size_bit)-1); \
  type overflow= (src1 >> (type_size - lshift) ) & ( ( ((type)1) << lshift ) - 1 ) ; \
  return (src1 << lshift) | overflow; \
}

#define ROTATE_VECTOR_N_SRC(type,type_size,type_size_bit,nb_elem) \
static inline type##nb_elem __ocl_rotate_##type##nb_elem (type##nb_elem src1, type##nb_elem src2) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    type lshift  = src2.element[i] & ((1<<type_size_bit)-1); \
    type overflow= (src1.element[i] >> (type_size - lshift) ) & ( ( ((type)1) << lshift ) - 1 ) ; \
    s.element[i] = (src1.element[i] << lshift) | overflow; \
  } \
  return s; \
}

#define ROTATE_SRC(type,type_size,type_size_bit)       \
   ROTATE_SCALAR_N_SRC(type,type_size,type_size_bit)   \
   ROTATE_VECTOR_N_SRC(type,type_size,type_size_bit,2) \
   ROTATE_VECTOR_N_SRC(type,type_size,type_size_bit,3) \
   ROTATE_VECTOR_N_SRC(type,type_size,type_size_bit,4) \
   ROTATE_VECTOR_N_SRC(type,type_size,type_size_bit,8) \
   ROTATE_VECTOR_N_SRC(type,type_size,type_size_bit,16)

ROTATE_SRC(char,8,3)
ROTATE_SRC(uchar,8,3)
ROTATE_SRC(short,16,4)
ROTATE_SRC(ushort,16,4)
ROTATE_SRC(int,32,5)
ROTATE_SRC(uint,32,5)
ROTATE_SRC(long,64,6)
ROTATE_SRC(ulong,64,6)


/* upsample */

#define UPSAMPLE_SCALAR_N_SRC(type_dest,type_src1,type_src2,src_type_size) \
static inline type_dest __ocl_upsample_##type_dest (type_src1 src1, type_src2 src2) { \
  return ( (type_dest)src1 << src_type_size ) | src2 ; \
}

#define UPSAMPLE_VECTOR_N_SRC(type_dest,type_src1,type_src2,src_type_size,nb_elem) \
static inline type_dest##nb_elem __ocl_upsample_##type_dest##nb_elem (type_src1##nb_elem src1,type_src2##nb_elem src2) { \
  type_dest##nb_elem s; \
  int i; \
  for(i=0;i<nb_elem;i++) { \
    s.element[i]= ( (type_dest)src1.element[i] << src_type_size ) | src2.element[i] ; \
  } \
  return s; \
}

#define UPSAMPLE_SRC(type_dest,type_src1,type_src2,src_type_size)       \
   UPSAMPLE_SCALAR_N_SRC(type_dest,type_src1,type_src2,src_type_size)   \
   UPSAMPLE_VECTOR_N_SRC(type_dest,type_src1,type_src2,src_type_size,2) \
   UPSAMPLE_VECTOR_N_SRC(type_dest,type_src1,type_src2,src_type_size,3) \
   UPSAMPLE_VECTOR_N_SRC(type_dest,type_src1,type_src2,src_type_size,4) \
   UPSAMPLE_VECTOR_N_SRC(type_dest,type_src1,type_src2,src_type_size,8) \
   UPSAMPLE_VECTOR_N_SRC(type_dest,type_src1,type_src2,src_type_size,16)

UPSAMPLE_SRC(short,char,uchar,8)
UPSAMPLE_SRC(ushort,uchar,uchar,8)
UPSAMPLE_SRC(int,short,ushort,16)
UPSAMPLE_SRC(uint,ushort,ushort,16)
UPSAMPLE_SRC(long,int,uint,32)
UPSAMPLE_SRC(ulong,uint,uint,32)

#endif
