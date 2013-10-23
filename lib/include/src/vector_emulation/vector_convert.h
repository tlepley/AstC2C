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

/* OpenCL vector widdening/conversion */

#ifndef VECTOR_CONVERT_H
#define VECTOR_CONVERT_H

#include "vector_types.h"

#define WIDEN_N(type,nb_elem)			    \
static inline type##nb_elem __ocl_convert_##type##nb_elem##_##type (type src) { \
  type##nb_elem s;  \
  int i; \
  for (i=0;i<nb_elem;i++) {  \
    s.element[i]=src; \
  } \
  return s; \
}

#define WIDEN(type) \
  WIDEN_N(type,2)   \
  WIDEN_N(type,3)   \
  WIDEN_N(type,4)   \
  WIDEN_N(type,8)   \
  WIDEN_N(type,16)

WIDEN(uchar)
WIDEN(ushort)
WIDEN(uint)
WIDEN(ulong)
WIDEN(char)
WIDEN(short)
WIDEN(int)
WIDEN(long)
WIDEN(float)


/*============================================================
                   Standard conversions
  ============================================================*/

#define CONVERT_SCALAR_SRC(type,type_src) \
static inline type __ocl_convert_##type##_##type_src (type_src src) { \
  return (type)src; \
}

#define CONVERT_N_SRC(type,nb_elem,type_src)				\
static inline type##nb_elem __ocl_convert_##type##nb_elem##_##type_src##nb_elem (type_src##nb_elem src) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=(type)src.element[i]; \
  } \
  return s; \
}

#define CONVERT_SRC(type,type_src)  \
  CONVERT_SCALAR_SRC(type,type_src) \
  CONVERT_N_SRC(type,2,type_src)    \
  CONVERT_N_SRC(type,3,type_src)    \
  CONVERT_N_SRC(type,4,type_src)    \
  CONVERT_N_SRC(type,8,type_src)    \
  CONVERT_N_SRC(type,16,type_src)

#define CONVERT(type)      \
  CONVERT_SRC(type,uchar)  \
  CONVERT_SRC(type,ushort) \
  CONVERT_SRC(type,uint)   \
  CONVERT_SRC(type,ulong)  \
  CONVERT_SRC(type,char)   \
  CONVERT_SRC(type,short)  \
  CONVERT_SRC(type,int)	   \
  CONVERT_SRC(type,long)   \
  CONVERT_SRC(type,float)

CONVERT(uchar)
CONVERT(ushort)
CONVERT(uint)
CONVERT(ulong)
CONVERT(char)
CONVERT(short)
CONVERT(int)
CONVERT(long)
CONVERT(float)



/*============================================================
                   Saturated conversions
  ============================================================*/

/* Note: comparisons are automatically performed in the larger type */


/* Saturated Unsigned <- Unsigned */
#define CONVERT_SATNOSAT_SCALAR(type,type_src) \
static inline type __ocl_convert_##type##_##type_src##_sat (type_src src) { \
  return src; \
}
#define CONVERT_SATNOSAT_N(type,nb_elem,type_src) \
static inline type##nb_elem __ocl_convert_##type##nb_elem##_##type_src##nb_elem##_sat (type_src##nb_elem src) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    s.element[i]=src.element[i]; \
  } \
  return s; \
}

/* Saturated Unsigned <- Signed */
#define CONVERT_SATZERO_SCALAR(utype,stype_src) \
static inline utype __ocl_convert_##utype##_##stype_src##_sat (stype_src src) { \
    if (src<0) { \
      return 0; \
    } \
    else { \
      return src; \
    } \
}
#define CONVERT_SATZERO_N(utype,nb_elem,stype_src) \
static inline utype##nb_elem __ocl_convert_##utype##nb_elem##_##stype_src##nb_elem##_sat (stype_src##nb_elem src) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) {  \
    if ((src.element[i])<0) { \
      s.element[i]=0; \
    } \
    else { \
      s.element[i]=src.element[i]; \
    } \
  } \
  return s; \
}

/* Saturated Unsigned <- Unsigned */
#define CONVERT_SATMAX_SCALAR(type,type_src,TYPENAME) \
static inline type __ocl_convert_##type##_##type_src##_sat (type_src src) { \
    if (src>OCL_##TYPENAME##_MAX) { \
      return OCL_##TYPENAME##_MAX; \
    } \
    else { \
      return src; \
    } \
}
#define CONVERT_SATMAX_N(type,nb_elem,type_src,TYPENAME) \
static inline type##nb_elem __ocl_convert_##type##nb_elem##_##type_src##nb_elem##_sat (type_src##nb_elem src) { \
  type##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    if ((src.element[i])>OCL_##TYPENAME##_MAX) { \
      s.element[i]=OCL_##TYPENAME##_MAX; \
    } \
    else { \
      s.element[i]=src.element[i]; \
    } \
  } \
  return s; \
}


/* Saturated Unsigned <- Signed */
#define CONVERT_SATZEROMAX_SCALAR(utype,stype_src,TYPENAME) \
static inline utype __ocl_convert_##utype##_##stype_src##_sat (stype_src src) { \
    if (src>OCL_##TYPENAME##_MAX) { \
      return OCL_##TYPENAME##_MAX; \
    } \
    else if (src<0) { \
      return 0; \
    } \
    else { \
      return src; \
    } \
}
#define CONVERT_SATZEROMAX_N(utype,nb_elem,stype_src,TYPENAME) \
static inline utype##nb_elem __ocl_convert_##utype##nb_elem##_##stype_src##nb_elem##_sat (stype_src##nb_elem src) { \
  utype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) {  \
    if ((src.element[i])>OCL_##TYPENAME##_MAX) { \
      s.element[i]=OCL_##TYPENAME##_MAX; \
    } \
    else if ((src.element[i])<0) { \
      s.element[i]=0; \
    } \
    else { \
      s.element[i]=src.element[i]; \
    } \
  } \
  return s; \
}

/* Saturated Signed <- Signed */
#define CONVERT_SATMINMAX_SCALAR(stype,stype_src,TYPENAME) \
static inline stype __ocl_convert_##stype##_##stype_src##_sat (stype_src src) { \
    if ((src)>OCL_##TYPENAME##_MAX) { \
      return OCL_##TYPENAME##_MAX; \
    } \
    else if ((src)<OCL_##TYPENAME##_MIN) {	\
      return OCL_##TYPENAME##_MIN; \
    } \
    else { \
      return src; \
    } \
}
#define CONVERT_SATMINMAX_N(stype,nb_elem,stype_src,TYPENAME) \
static inline stype##nb_elem __ocl_convert_##stype##nb_elem##_##stype_src##nb_elem##_sat (stype_src##nb_elem src) { \
  stype##nb_elem s; \
  int i; \
  for (i=0;i<nb_elem;i++) { \
    if ((src.element[i])>OCL_##TYPENAME##_MAX) { \
      s.element[i]=OCL_##TYPENAME##_MAX; \
    } \
    else if ((src.element[i])<OCL_##TYPENAME##_MIN) {	\
      s.element[i]=OCL_##TYPENAME##_MIN; \
    } \
    else { \
      s.element[i]=src.element[i]; \
    } \
  } \
  return s; \
}


#define CONVERT_SATNOSAT(type,type_src) \
  CONVERT_SATNOSAT_SCALAR(type,type_src) \
  CONVERT_SATNOSAT_N(type,2,type_src) \
  CONVERT_SATNOSAT_N(type,3,type_src) \
  CONVERT_SATNOSAT_N(type,4,type_src) \
  CONVERT_SATNOSAT_N(type,8,type_src) \
  CONVERT_SATNOSAT_N(type,16,type_src)
  
#define CONVERT_SATZERO(utype,stype_src) \
  CONVERT_SATZERO_SCALAR(utype,stype_src) \
  CONVERT_SATZERO_N(utype,2,stype_src) \
  CONVERT_SATZERO_N(utype,3,stype_src) \
  CONVERT_SATZERO_N(utype,4,stype_src) \
  CONVERT_SATZERO_N(utype,8,stype_src) \
  CONVERT_SATZERO_N(utype,16,stype_src)

#define CONVERT_SATMAX(type,type_src,TYPENAME) \
  CONVERT_SATMAX_SCALAR(type,type_src,TYPENAME) \
  CONVERT_SATMAX_N(type,2,type_src,TYPENAME) \
  CONVERT_SATMAX_N(type,3,type_src,TYPENAME) \
  CONVERT_SATMAX_N(type,4,type_src,TYPENAME) \
  CONVERT_SATMAX_N(type,8,type_src,TYPENAME) \
  CONVERT_SATMAX_N(type,16,type_src,TYPENAME)

#define CONVERT_SATZEROMAX(utype,stype_src,TYPENAME) \
  CONVERT_SATZEROMAX_SCALAR(utype,stype_src,TYPENAME) \
  CONVERT_SATZEROMAX_N(utype,2,stype_src,TYPENAME) \
  CONVERT_SATZEROMAX_N(utype,3,stype_src,TYPENAME) \
  CONVERT_SATZEROMAX_N(utype,4,stype_src,TYPENAME) \
  CONVERT_SATZEROMAX_N(utype,8,stype_src,TYPENAME) \
  CONVERT_SATZEROMAX_N(utype,16,stype_src,TYPENAME)

#define CONVERT_SATMINMAX(stype,stype_src,TYPENAME) \
  CONVERT_SATMINMAX_SCALAR(stype,stype_src,TYPENAME) \
  CONVERT_SATMINMAX_N(stype,2,stype_src,TYPENAME) \
  CONVERT_SATMINMAX_N(stype,3,stype_src,TYPENAME) \
  CONVERT_SATMINMAX_N(stype,4,stype_src,TYPENAME) \
  CONVERT_SATMINMAX_N(stype,8,stype_src,TYPENAME) \
  CONVERT_SATMINMAX_N(stype,16,stype_src,TYPENAME)


CONVERT_SATMAX(uchar,ushort,UCHAR)		
CONVERT_SATMAX(uchar,uint,UCHAR) 
CONVERT_SATMAX(uchar,ulong,UCHAR) 
CONVERT_SATZERO(uchar,char)
CONVERT_SATZEROMAX(uchar,short,UCHAR) 
CONVERT_SATZEROMAX(uchar,int,UCHAR) 
CONVERT_SATZEROMAX(uchar,long,UCHAR)

CONVERT_SATNOSAT(ushort,uchar) 
CONVERT_SATMAX(ushort,uint,USHRT) 
CONVERT_SATMAX(ushort,ulong,USHRT) 
CONVERT_SATZERO(ushort,char)
CONVERT_SATZERO(ushort,short) 
CONVERT_SATZEROMAX(ushort,int,USHRT) 
CONVERT_SATZEROMAX(ushort,long,USHRT)
  
CONVERT_SATNOSAT(uint,uchar) 
CONVERT_SATNOSAT(uint,ushort)		
CONVERT_SATMAX(uint,ulong,UINT) 
CONVERT_SATZERO(uint,char)
CONVERT_SATZERO(uint,short) 
CONVERT_SATZERO(uint,int) 
CONVERT_SATZEROMAX(uint,long,UINT)

CONVERT_SATNOSAT(ulong,uchar) 
CONVERT_SATNOSAT(ulong,ushort)		
CONVERT_SATNOSAT(ulong,uint) 
CONVERT_SATZERO(ulong,char)
CONVERT_SATZERO(ulong,short) 
CONVERT_SATZERO(ulong,int) 
CONVERT_SATZERO(ulong,long)


CONVERT_SATMAX(char,uchar,SCHAR)
CONVERT_SATMAX(char,ushort,SCHAR)
CONVERT_SATMAX(char,uint,SCHAR)
CONVERT_SATMAX(char,ulong,SCHAR)
CONVERT_SATMINMAX(char,short,SCHAR)
CONVERT_SATMINMAX(char,int,SCHAR)
CONVERT_SATMINMAX(char,long,SCHAR)

CONVERT_SATNOSAT(short,uchar)
CONVERT_SATMAX(short,ushort,SHRT)
CONVERT_SATMAX(short,uint,SHRT)
CONVERT_SATMAX(short,ulong,SHRT)
CONVERT_SATNOSAT(short,char)
CONVERT_SATMINMAX(short,int,SHRT)
CONVERT_SATMINMAX(short,long,SHRT)

CONVERT_SATNOSAT(int,uchar)
CONVERT_SATNOSAT(int,ushort)
CONVERT_SATMAX(int,uint,INT)
CONVERT_SATMAX(int,ulong,INT)
CONVERT_SATNOSAT(int,char)
CONVERT_SATNOSAT(int,short)
CONVERT_SATMINMAX(int,long,INT)

CONVERT_SATNOSAT(long,uchar)
CONVERT_SATNOSAT(long,ushort)
CONVERT_SATNOSAT(long,uint)
CONVERT_SATMAX(long,ulong,LONG)
CONVERT_SATNOSAT(long,char)
CONVERT_SATNOSAT(long,short)
CONVERT_SATNOSAT(long,int)


#endif
