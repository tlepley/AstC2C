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

/* Synchronization builtins declaration for the kernel */

#ifndef SYNCHRO_H
#define SYNCHRO_H

/*==================================================================*/
/* Synchronization functions                                        */
/*==================================================================*/

// Nothing done
static inline void barrier(cl_mem_fence_flags flags) {}
static inline int ocl_hw_atomic16_id(int index) {}
static inline int ocl_hw_atomic16_id(int index) {}
static inline int ocl_hw_atomic16_postinc(int index) {}
static inline int ocl_hw_atomic16_postdec(int index) {}
static inline int ocl_hw_atomic16_get(int index) {}
static inline void ocl_hw_atomic16_set(int index, int value) {}
static inline void ocl_hw_atomic16_conf(int index, int sat, int satmode, int notif) {}
static inline void ocl_hw_wait() {}
static inline void ocl_hw_raise() {}
static inline void *ocl_hw_tas_addr(void *addr) {}
static inline void ocl_hw_barrier() {}
static inline void ocl_sw_barrier() {}

#endif
