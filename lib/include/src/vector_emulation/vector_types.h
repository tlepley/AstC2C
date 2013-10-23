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

/* OpenCL vector emulated types */

#ifndef VECTOR_TYPES_H
#define VECTOR_TYPES_H


/*=============================================
                  Vector types
  =============================================*/


#define _VEC(type,nb_elem) \
typedef struct { \
  type element[nb_elem]; \
} __attribute__ ((aligned ( sizeof(type) * nb_elem))) type##nb_elem;

/* vec3 has a size 4 in memory */
#define _VEC3(type) \
typedef struct { \
  type element[4]; \
} __attribute__ ((aligned ( sizeof(type) * 4))) type##3;

#define _VECTOR(type) \
  _VEC(type,2) \
  _VEC3(type) \
  _VEC(type,4) \
  _VEC(type,8) \
  _VEC(type,16)

_VECTOR(char)
_VECTOR(uchar)
_VECTOR(short)
_VECTOR(ushort)
_VECTOR(int)
_VECTOR(uint)
_VECTOR(long)
_VECTOR(ulong)
_VECTOR(float)

#endif
