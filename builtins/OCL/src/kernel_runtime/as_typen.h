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

/* OpenCL as_typen builtin functions */

#ifndef AS_TYPEN_H
#define AS_TYPEN_H

#define ASTYPEN_DECL(type) \
  type##2 tp_##type##2;	\
  type##3 tp_##type##3;	\
  type##4 tp_##type##4;	\
  type##8 tp_##type##8;	\
  type##16 tp_##type##16;

typedef union {
  char   tp_char;
  uchar  tp_uchar;
  short  tp_short;
  ushort tp_ushort;
  int    tp_int;
  uint   tp_uint;
  long   tp_long;
  ulong  tp_ulong;
  float  tp_float;
  ASTYPEN_DECL(char)
  ASTYPEN_DECL(uchar)
  ASTYPEN_DECL(short)
  ASTYPEN_DECL(ushort)
  ASTYPEN_DECL(int)
  ASTYPEN_DECL(uint)
  ASTYPEN_DECL(long)
  ASTYPEN_DECL(ulong)
  ASTYPEN_DECL(float)
} __ocl_as_typen;


#define ASTYPEN_NN(dest_type,dest_n,src_type,src_n) \
static inline dest_type##dest_n __ocl_as_##dest_type##dest_n##_##src_type##src_n \
                        ( src_type##src_n src) { \
  __ocl_as_typen var; \
  var.tp_##src_type##src_n = src; \
  return var.tp_##dest_type##dest_n ; \
}

//==================================================================
// 8 bit types (char, uchar)
//==================================================================

#define ASTYPEN_8(type) \
ASTYPEN_NN(type,,char,) \
ASTYPEN_NN(type,,uchar,) \
   \
ASTYPEN_NN(type,2,char,2) \
ASTYPEN_NN(type,2,uchar,2) \
ASTYPEN_NN(type,2,short,) \
ASTYPEN_NN(type,2,ushort,) \
 \
ASTYPEN_NN(type,3,char,3) \
ASTYPEN_NN(type,3,uchar,3) \
ASTYPEN_NN(type,3,char,4) \
ASTYPEN_NN(type,3,uchar,4) \
ASTYPEN_NN(type,3,short,2) \
ASTYPEN_NN(type,3,ushort,2) \
ASTYPEN_NN(type,3,int,) \
ASTYPEN_NN(type,3,uint,) \
ASTYPEN_NN(type,3,float,) \
 \
ASTYPEN_NN(type,4,char,3) \
ASTYPEN_NN(type,4,uchar,3) \
ASTYPEN_NN(type,4,char,4) \
ASTYPEN_NN(type,4,uchar,4) \
ASTYPEN_NN(type,4,short,2) \
ASTYPEN_NN(type,4,ushort,2) \
ASTYPEN_NN(type,4,int,) \
ASTYPEN_NN(type,4,uint,) \
ASTYPEN_NN(type,4,float,) \
 \
ASTYPEN_NN(type,8,char,8) \
ASTYPEN_NN(type,8,uchar,8) \
ASTYPEN_NN(type,8,short,3) \
ASTYPEN_NN(type,8,ushort,3) \
ASTYPEN_NN(type,8,short,4) \
ASTYPEN_NN(type,8,ushort,4) \
ASTYPEN_NN(type,8,int,2) \
ASTYPEN_NN(type,8,uint,2) \
ASTYPEN_NN(type,8,float,2) \
ASTYPEN_NN(type,8,long,) \
ASTYPEN_NN(type,8,ulong,) \
 \
ASTYPEN_NN(type,16,char,16) \
ASTYPEN_NN(type,16,uchar,16) \
ASTYPEN_NN(type,16,short,8) \
ASTYPEN_NN(type,16,ushort,8) \
ASTYPEN_NN(type,16,int,3) \
ASTYPEN_NN(type,16,uint,3) \
ASTYPEN_NN(type,16,float,3) \
ASTYPEN_NN(type,16,int,4) \
ASTYPEN_NN(type,16,uint,4) \
ASTYPEN_NN(type,16,float,4) \
ASTYPEN_NN(type,16,long,2) \
ASTYPEN_NN(type,16,ulong,2)

ASTYPEN_8(char)
ASTYPEN_8(uchar)

//==================================================================
// 16 bit types (short, ushort)
//==================================================================
  
#define ASTYPEN_16(type) \
ASTYPEN_NN(type,,char,2) \
ASTYPEN_NN(type,,uchar,2) \
ASTYPEN_NN(type,,short,) \
ASTYPEN_NN(type,,ushort,) \
 \
ASTYPEN_NN(type,2,char,3) \
ASTYPEN_NN(type,2,uchar,3) \
ASTYPEN_NN(type,2,char,4) \
ASTYPEN_NN(type,2,uchar,4) \
ASTYPEN_NN(type,2,short,2) \
ASTYPEN_NN(type,2,ushort,2) \
ASTYPEN_NN(type,2,int,) \
ASTYPEN_NN(type,2,uint,) \
ASTYPEN_NN(type,2,float,) \
 \
ASTYPEN_NN(type,3,char,8) \
ASTYPEN_NN(type,3,uchar,8) \
ASTYPEN_NN(type,3,short,3) \
ASTYPEN_NN(type,3,ushort,3) \
ASTYPEN_NN(type,3,short,4) \
ASTYPEN_NN(type,3,ushort,4) \
ASTYPEN_NN(type,3,int,2) \
ASTYPEN_NN(type,3,uint,2) \
ASTYPEN_NN(type,3,float,2) \
ASTYPEN_NN(type,3,long,) \
ASTYPEN_NN(type,3,ulong,) \
 \
ASTYPEN_NN(type,4,char,8) \
ASTYPEN_NN(type,4,uchar,8) \
ASTYPEN_NN(type,4,short,3) \
ASTYPEN_NN(type,4,ushort,3) \
ASTYPEN_NN(type,4,short,4) \
ASTYPEN_NN(type,4,ushort,4) \
ASTYPEN_NN(type,4,int,2) \
ASTYPEN_NN(type,4,uint,2) \
ASTYPEN_NN(type,4,float,2) \
ASTYPEN_NN(type,4,long,) \
ASTYPEN_NN(type,4,ulong,) \
 \
ASTYPEN_NN(type,8,char,16) \
ASTYPEN_NN(type,8,uchar,16) \
ASTYPEN_NN(type,8,short,8) \
ASTYPEN_NN(type,8,ushort,8) \
ASTYPEN_NN(type,8,int,3) \
ASTYPEN_NN(type,8,uint,3) \
ASTYPEN_NN(type,8,float,3) \
ASTYPEN_NN(type,8,int,4) \
ASTYPEN_NN(type,8,uint,4) \
ASTYPEN_NN(type,8,float,4) \
ASTYPEN_NN(type,8,long,2) \
ASTYPEN_NN(type,8,ulong,2) \
 \
ASTYPEN_NN(type,16,short,16) \
ASTYPEN_NN(type,16,ushort,16) \
ASTYPEN_NN(type,16,int,8) \
ASTYPEN_NN(type,16,uint,8) \
ASTYPEN_NN(type,16,float,8) \
ASTYPEN_NN(type,16,long,3) \
ASTYPEN_NN(type,16,ulong,3) \
ASTYPEN_NN(type,16,long,4) \
ASTYPEN_NN(type,16,ulong,4)

  
ASTYPEN_16(short)
ASTYPEN_16(ushort)


//==================================================================
// 32 bit types (int,uint,float)
//==================================================================

#define ASTYPEN_32(type) \
ASTYPEN_NN(type,,char,3) \
ASTYPEN_NN(type,,uchar,3) \
ASTYPEN_NN(type,,char,4) \
ASTYPEN_NN(type,,uchar,4) \
ASTYPEN_NN(type,,short,2) \
ASTYPEN_NN(type,,ushort,2) \
ASTYPEN_NN(type,,int,) \
ASTYPEN_NN(type,,uint,) \
ASTYPEN_NN(type,,float,) \
 \
ASTYPEN_NN(type,2,char,8) \
ASTYPEN_NN(type,2,uchar,8) \
ASTYPEN_NN(type,2,short,3) \
ASTYPEN_NN(type,2,ushort,3) \
ASTYPEN_NN(type,2,short,4) \
ASTYPEN_NN(type,2,ushort,4) \
ASTYPEN_NN(type,2,int,2) \
ASTYPEN_NN(type,2,uint,2) \
ASTYPEN_NN(type,2,float,2) \
ASTYPEN_NN(type,2,long,) \
ASTYPEN_NN(type,2,ulong,) \
 \
ASTYPEN_NN(type,3,char,16) \
ASTYPEN_NN(type,3,uchar,16) \
ASTYPEN_NN(type,3,short,8) \
ASTYPEN_NN(type,3,ushort,8) \
ASTYPEN_NN(type,3,int,3) \
ASTYPEN_NN(type,3,uint,3) \
ASTYPEN_NN(type,3,float,3) \
ASTYPEN_NN(type,3,int,4) \
ASTYPEN_NN(type,3,uint,4) \
ASTYPEN_NN(type,3,float,4) \
ASTYPEN_NN(type,3,long,2) \
ASTYPEN_NN(type,3,ulong,2) \
 \
ASTYPEN_NN(type,4,char,16) \
ASTYPEN_NN(type,4,uchar,16) \
ASTYPEN_NN(type,4,short,8) \
ASTYPEN_NN(type,4,ushort,8) \
ASTYPEN_NN(type,4,int,3) \
ASTYPEN_NN(type,4,uint,3) \
ASTYPEN_NN(type,4,float,3) \
ASTYPEN_NN(type,4,int,4) \
ASTYPEN_NN(type,4,uint,4) \
ASTYPEN_NN(type,4,float,4) \
ASTYPEN_NN(type,4,long,2) \
ASTYPEN_NN(type,4,ulong,2) \
 \
ASTYPEN_NN(type,8,short,16) \
ASTYPEN_NN(type,8,ushort,16) \
ASTYPEN_NN(type,8,int,8) \
ASTYPEN_NN(type,8,uint,8) \
ASTYPEN_NN(type,8,float,8) \
ASTYPEN_NN(type,8,long,3) \
ASTYPEN_NN(type,8,ulong,3) \
ASTYPEN_NN(type,8,long,4) \
ASTYPEN_NN(type,8,ulong,4) \
 \
ASTYPEN_NN(type,16,int,16) \
ASTYPEN_NN(type,16,uint,16) \
ASTYPEN_NN(type,16,float,16) \
ASTYPEN_NN(type,16,long,8) \
ASTYPEN_NN(type,16,ulong,8)

ASTYPEN_32(int)
ASTYPEN_32(uint)
ASTYPEN_32(float)


//==================================================================
// 64 bit types (long,ulong)
//==================================================================

#define ASTYPEN_64(type) \
ASTYPEN_NN(type,,char,8) \
ASTYPEN_NN(type,,uchar,8) \
ASTYPEN_NN(type,,short,3) \
ASTYPEN_NN(type,,ushort,3) \
ASTYPEN_NN(type,,short,4) \
ASTYPEN_NN(type,,ushort,4) \
ASTYPEN_NN(type,,int,2) \
ASTYPEN_NN(type,,uint,2) \
ASTYPEN_NN(type,,float,2) \
ASTYPEN_NN(type,,long,) \
ASTYPEN_NN(type,,ulong,) \
 \
ASTYPEN_NN(type,2,char,16) \
ASTYPEN_NN(type,2,uchar,16) \
ASTYPEN_NN(type,2,short,8) \
ASTYPEN_NN(type,2,ushort,8) \
ASTYPEN_NN(type,2,int,3) \
ASTYPEN_NN(type,2,uint,3) \
ASTYPEN_NN(type,2,float,3) \
ASTYPEN_NN(type,2,int,4) \
ASTYPEN_NN(type,2,uint,4) \
ASTYPEN_NN(type,2,float,4) \
ASTYPEN_NN(type,2,long,2) \
ASTYPEN_NN(type,2,ulong,2) \
 \
ASTYPEN_NN(type,3,short,16) \
ASTYPEN_NN(type,3,ushort,16) \
ASTYPEN_NN(type,3,int,8) \
ASTYPEN_NN(type,3,uint,8) \
ASTYPEN_NN(type,3,float,8) \
ASTYPEN_NN(type,3,long,3) \
ASTYPEN_NN(type,3,ulong,3) \
ASTYPEN_NN(type,3,long,4) \
ASTYPEN_NN(type,3,ulong,4) \
 \
ASTYPEN_NN(type,4,short,16) \
ASTYPEN_NN(type,4,ushort,16) \
ASTYPEN_NN(type,4,int,8) \
ASTYPEN_NN(type,4,uint,8) \
ASTYPEN_NN(type,4,float,8) \
ASTYPEN_NN(type,4,long,3) \
ASTYPEN_NN(type,4,ulong,3) \
ASTYPEN_NN(type,4,long,4) \
ASTYPEN_NN(type,4,ulong,4) \
 \
ASTYPEN_NN(type,8,int,16) \
ASTYPEN_NN(type,8,uint,16) \
ASTYPEN_NN(type,8,float,16) \
ASTYPEN_NN(type,8,long,8) \
ASTYPEN_NN(type,8,ulong,8) \
 \
ASTYPEN_NN(type,16,long,16) \
ASTYPEN_NN(type,16,ulong,16)

ASTYPEN_64(long)
ASTYPEN_64(ulong)
   
  
#endif
