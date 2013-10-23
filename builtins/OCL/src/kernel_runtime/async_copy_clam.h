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

/* Async copy builtins declaration for the kernel */

/*==================================================================*/
/* Work-group Asynch copies                                          */
/*==================================================================*/

#define ASYNC_WORK_GROUP_COPY_TOEXT_N(type, n, dest_as) \
static inline event_t __ocl_async_work_group_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, event_t event) { \
  return ocl_wg_async_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1); \
}

#define ASYNC_WORK_GROUP_COPY_TOLOC_N(type, n, dest_as) \
static inline event_t __ocl_async_work_group_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, event_t event) { \
  return ocl_wg_async_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1); \
}

#define DMA_WORK_GROUP_COPY_TOEXT_N(type, n, dest_as) \
static inline void __ocl_dma_work_group_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, dma_req_t *req) { \
  ocl_wg_dma_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1, req); \
}

#define DMA_WORK_GROUP_COPY_TOLOC_N(type, n, dest_as) \
static inline void __ocl_dma_work_group_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, dma_req_t *req) { \
  ocl_wg_dma_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1, req); \
}

#define ASYNC_WORK_GROUP_COPY_TOEXT_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOEXT_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_COPY_TOEXT_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOEXT_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOEXT_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOEXT_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOEXT_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_COPY_TOLOC_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOLOC_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_COPY_TOLOC_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOLOC_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOLOC_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOLOC_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_COPY_TOLOC_N(type,16,dest_as)

#define DMA_WORK_GROUP_COPY_TOEXT_ALL(type,dest_as) \
  DMA_WORK_GROUP_COPY_TOEXT_N(type,,dest_as)  \
  DMA_WORK_GROUP_COPY_TOEXT_N(type,2,dest_as) \
  DMA_WORK_GROUP_COPY_TOEXT_N(type,3,dest_as) \
  DMA_WORK_GROUP_COPY_TOEXT_N(type,4,dest_as) \
  DMA_WORK_GROUP_COPY_TOEXT_N(type,8,dest_as) \
  DMA_WORK_GROUP_COPY_TOEXT_N(type,16,dest_as)

#define DMA_WORK_GROUP_COPY_TOLOC_ALL(type,dest_as) \
  DMA_WORK_GROUP_COPY_TOLOC_N(type,,dest_as)  \
  DMA_WORK_GROUP_COPY_TOLOC_N(type,2,dest_as) \
  DMA_WORK_GROUP_COPY_TOLOC_N(type,3,dest_as) \
  DMA_WORK_GROUP_COPY_TOLOC_N(type,4,dest_as) \
  DMA_WORK_GROUP_COPY_TOLOC_N(type,8,dest_as) \
  DMA_WORK_GROUP_COPY_TOLOC_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_COPY(type) \
  ASYNC_WORK_GROUP_COPY_TOEXT_ALL(type,global) \
  ASYNC_WORK_GROUP_COPY_TOLOC_ALL(type,local) \
  DMA_WORK_GROUP_COPY_TOEXT_ALL(type,global) \
  DMA_WORK_GROUP_COPY_TOLOC_ALL(type,local)

ASYNC_WORK_GROUP_COPY(char)
ASYNC_WORK_GROUP_COPY(uchar)
ASYNC_WORK_GROUP_COPY(short)
ASYNC_WORK_GROUP_COPY(ushort)
ASYNC_WORK_GROUP_COPY(int)
ASYNC_WORK_GROUP_COPY(uint)
ASYNC_WORK_GROUP_COPY(long)
ASYNC_WORK_GROUP_COPY(ulong)
ASYNC_WORK_GROUP_COPY(float)


/*--------
     2D
  --------*/
#define ASYNC_WORK_GROUP_2D_COPY_TOEXT_N(type, n, dest_as)				\
static inline event_t __ocl_async_work_group_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, event_t event) { \
  char *dst_pointer=(char *)dst+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  return ocl_wg_async_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride); \
}

#define ASYNC_WORK_GROUP_2D_COPY_TOLOC_N(type, n, dest_as)				\
static inline event_t __ocl_async_work_group_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, event_t event) { \
  char *src_pointer=(char *)src+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  return ocl_wg_async_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride); \
}

#define DMA_WORK_GROUP_2D_COPY_TOEXT_N(type, n, dest_as)				\
static inline void __ocl_dma_work_group_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, dma_req_t *req) { \
  char *dst_pointer=(char *)dst+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  ocl_wg_dma_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride, req); \
}

#define DMA_WORK_GROUP_2D_COPY_TOLOC_N(type, n, dest_as)				\
static inline void __ocl_dma_work_group_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, dma_req_t *req) { \
  char *src_pointer=(char *)src+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  ocl_wg_dma_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride, req); \
}

#define ASYNC_WORK_GROUP_2D_COPY_TOEXT_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOEXT_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_2D_COPY_TOEXT_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOEXT_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOEXT_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOEXT_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOEXT_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_2D_COPY_TOLOC_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOLOC_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_2D_COPY_TOLOC_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOLOC_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOLOC_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOLOC_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_TOLOC_N(type,16,dest_as)

#define DMA_WORK_GROUP_2D_COPY_TOEXT_ALL(type,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOEXT_N(type,,dest_as)  \
  DMA_WORK_GROUP_2D_COPY_TOEXT_N(type,2,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOEXT_N(type,3,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOEXT_N(type,4,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOEXT_N(type,8,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOEXT_N(type,16,dest_as)

#define DMA_WORK_GROUP_2D_COPY_TOLOC_ALL(type,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOLOC_N(type,,dest_as)  \
  DMA_WORK_GROUP_2D_COPY_TOLOC_N(type,2,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOLOC_N(type,3,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOLOC_N(type,4,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOLOC_N(type,8,dest_as) \
  DMA_WORK_GROUP_2D_COPY_TOLOC_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_2D_COPY(type) \
  ASYNC_WORK_GROUP_2D_COPY_TOEXT_ALL(type,global) \
  ASYNC_WORK_GROUP_2D_COPY_TOLOC_ALL(type,local) \
  DMA_WORK_GROUP_2D_COPY_TOEXT_ALL(type,global) \
  DMA_WORK_GROUP_2D_COPY_TOLOC_ALL(type,local)

ASYNC_WORK_GROUP_2D_COPY(char)
ASYNC_WORK_GROUP_2D_COPY(uchar)
ASYNC_WORK_GROUP_2D_COPY(short)
ASYNC_WORK_GROUP_2D_COPY(ushort)
ASYNC_WORK_GROUP_2D_COPY(int)
ASYNC_WORK_GROUP_2D_COPY(uint)
ASYNC_WORK_GROUP_2D_COPY(long)
ASYNC_WORK_GROUP_2D_COPY(ulong)
ASYNC_WORK_GROUP_2D_COPY(float)


/*--------
  Strided
  --------*/
#define ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_N(type, n, dest_as) \
static inline event_t __ocl_async_work_group_strided_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, size_t global_stride, event_t event) { \
  return ocl_wg_async_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst, sizeof(type##n) * num_elements, sizeof(type##n), sizeof(type##n) * global_stride); \
}

#define ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_N(type, n, dest_as) \
static inline event_t __ocl_async_work_group_strided_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, size_t global_stride, event_t event) { \
  return ocl_wg_async_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src, sizeof(type##n) * num_elements, sizeof(type##n), sizeof(type##n) * global_stride); \
}

#define ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_STRIDED_COPY(type) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOEXT_ALL(type,global) \
  ASYNC_WORK_GROUP_STRIDED_COPY_TOLOC_ALL(type,local)

ASYNC_WORK_GROUP_STRIDED_COPY(char)
ASYNC_WORK_GROUP_STRIDED_COPY(uchar)
ASYNC_WORK_GROUP_STRIDED_COPY(short)
ASYNC_WORK_GROUP_STRIDED_COPY(ushort)
ASYNC_WORK_GROUP_STRIDED_COPY(int)
ASYNC_WORK_GROUP_STRIDED_COPY(uint)
ASYNC_WORK_GROUP_STRIDED_COPY(long)
ASYNC_WORK_GROUP_STRIDED_COPY(ulong)
ASYNC_WORK_GROUP_STRIDED_COPY(float)

  
/*--------
  Prefetch
  --------*/
#define PREFETCH_N(type, n) \
static inline void __ocl_prefetch_##type##n (const type##n *src, size_t num_elements) { \
  prefetch_generic((void *)src, sizeof(type##n), num_elements); \
}

#define PREFETCH(type) \
  PREFETCH_N(type,)  \
  PREFETCH_N(type,2) \
  PREFETCH_N(type,3) \
  PREFETCH_N(type,4) \
  PREFETCH_N(type,8) \
  PREFETCH_N(type,16)

PREFETCH(char)
PREFETCH(uchar)
PREFETCH(short)
PREFETCH(ushort)
PREFETCH(int)
PREFETCH(uint)
PREFETCH(long)
PREFETCH(ulong)
PREFETCH(float)


  
/*==================================================================*/
/* Work-item Asynch copies                                          */
/*==================================================================*/

/*--------
     1D
  --------*/
#define ASYNC_WORK_ITEM_COPY_TOEXT_N(type, n, dest_as) \
static inline event_t __ocl_async_work_item_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, event_t event) { \
  return ocl_wi_async_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1); \
}

#define ASYNC_WORK_ITEM_COPY_TOLOC_N(type, n, dest_as) \
static inline event_t __ocl_async_work_item_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, event_t event) { \
  return ocl_wi_async_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1); \
}

#define DMA_WORK_ITEM_COPY_TOEXT_N(type, n, dest_as) \
static inline void __ocl_dma_work_item_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, dma_req_t *req) { \
  ocl_wi_dma_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1, req); \
}

#define DMA_WORK_ITEM_COPY_TOLOC_N(type, n, dest_as) \
static inline void __ocl_dma_work_item_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t num_elements, dma_req_t *req) { \
  ocl_wi_dma_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src, sizeof(type##n) * num_elements, (1<<15)-1, (1<<15)-1, req); \
}

#define ASYNC_WORK_ITEM_COPY_TOLOC_ALL(type, dest_as) \
  ASYNC_WORK_ITEM_COPY_TOLOC_N(type,,dest_as)  \
  ASYNC_WORK_ITEM_COPY_TOLOC_N(type,2,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOLOC_N(type,3,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOLOC_N(type,4,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOLOC_N(type,8,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOLOC_N(type,16,dest_as)

#define ASYNC_WORK_ITEM_COPY_TOEXT_ALL(type, dest_as) \
  ASYNC_WORK_ITEM_COPY_TOEXT_N(type,,dest_as)  \
  ASYNC_WORK_ITEM_COPY_TOEXT_N(type,2,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOEXT_N(type,3,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOEXT_N(type,4,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOEXT_N(type,8,dest_as) \
  ASYNC_WORK_ITEM_COPY_TOEXT_N(type,16,dest_as)

#define DMA_WORK_ITEM_COPY_TOLOC_ALL(type, dest_as) \
  DMA_WORK_ITEM_COPY_TOLOC_N(type,,dest_as)  \
  DMA_WORK_ITEM_COPY_TOLOC_N(type,2,dest_as) \
  DMA_WORK_ITEM_COPY_TOLOC_N(type,3,dest_as) \
  DMA_WORK_ITEM_COPY_TOLOC_N(type,4,dest_as) \
  DMA_WORK_ITEM_COPY_TOLOC_N(type,8,dest_as) \
  DMA_WORK_ITEM_COPY_TOLOC_N(type,16,dest_as)

#define DMA_WORK_ITEM_COPY_TOEXT_ALL(type, dest_as) \
  DMA_WORK_ITEM_COPY_TOEXT_N(type,,dest_as)  \
  DMA_WORK_ITEM_COPY_TOEXT_N(type,2,dest_as) \
  DMA_WORK_ITEM_COPY_TOEXT_N(type,3,dest_as) \
  DMA_WORK_ITEM_COPY_TOEXT_N(type,4,dest_as) \
  DMA_WORK_ITEM_COPY_TOEXT_N(type,8,dest_as) \
  DMA_WORK_ITEM_COPY_TOEXT_N(type,16,dest_as)

#define ASYNC_WORK_ITEM_COPY(type) \
  ASYNC_WORK_ITEM_COPY_TOEXT_ALL(type,global) \
  ASYNC_WORK_ITEM_COPY_TOLOC_ALL(type,local) \
  ASYNC_WORK_ITEM_COPY_TOLOC_ALL(type,private) \
  DMA_WORK_ITEM_COPY_TOEXT_ALL(type,global) \
  DMA_WORK_ITEM_COPY_TOLOC_ALL(type,local) \
  DMA_WORK_ITEM_COPY_TOLOC_ALL(type,private)

ASYNC_WORK_ITEM_COPY(char)
ASYNC_WORK_ITEM_COPY(uchar)
ASYNC_WORK_ITEM_COPY(short)
ASYNC_WORK_ITEM_COPY(ushort)
ASYNC_WORK_ITEM_COPY(int)
ASYNC_WORK_ITEM_COPY(uint)
ASYNC_WORK_ITEM_COPY(long)
ASYNC_WORK_ITEM_COPY(ulong)
ASYNC_WORK_ITEM_COPY(float)

/*--------
     2D
  --------*/
#define ASYNC_WORK_ITEM_2D_COPY_TOEXT_N(type, n, dest_as) \
static inline event_t __ocl_async_work_item_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, event_t event) { \
  char *dst_pointer =(char *)dst+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  return ocl_wi_async_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride); \
}

#define ASYNC_WORK_ITEM_2D_COPY_TOLOC_N(type, n, dest_as) \
static inline event_t __ocl_async_work_item_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, event_t event) { \
  char *src_pointer =(char *)src+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  return ocl_wi_async_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride); \
}

#define DMA_WORK_ITEM_2D_COPY_TOEXT_N(type, n, dest_as) \
static inline void __ocl_dma_work_item_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, dma_req_t *req) { \
  char *dst_pointer =(char *)dst+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  ocl_wi_dma_memcpy(this, OCL_LOC2EXT_CMD, (void *)src, (void *)dst_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride, req); \
}

#define DMA_WORK_ITEM_2D_COPY_TOLOC_N(type, n, dest_as) \
static inline void __ocl_dma_work_item_2d_copy_##type##n##_##dest_as (workItem_t * this, type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, dma_req_t *req) { \
  char *src_pointer =(char *)src+sizeof(type##n)*(global_origin[1]*global_stride+global_origin[0]); \
  ocl_wi_dma_memcpy(this, OCL_EXT2LOC_CMD, (void *)dst, (void *)src_pointer, region[0] * sizeof(type##n) * region[1], region[0] * sizeof(type##n), sizeof(type##n) * global_stride, req); \
}

#define ASYNC_WORK_ITEM_2D_COPY_TOLOC_ALL(type,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_N(type,,dest_as)  \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_N(type,2,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_N(type,3,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_N(type,4,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_N(type,8,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_N(type,16,dest_as)

#define ASYNC_WORK_ITEM_2D_COPY_TOEXT_ALL(type,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOEXT_N(type,,dest_as)  \
  ASYNC_WORK_ITEM_2D_COPY_TOEXT_N(type,2,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOEXT_N(type,3,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOEXT_N(type,4,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOEXT_N(type,8,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_TOEXT_N(type,16,dest_as)

#define DMA_WORK_ITEM_2D_COPY_TOLOC_ALL(type,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOLOC_N(type,,dest_as)  \
  DMA_WORK_ITEM_2D_COPY_TOLOC_N(type,2,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOLOC_N(type,3,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOLOC_N(type,4,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOLOC_N(type,8,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOLOC_N(type,16,dest_as)

#define DMA_WORK_ITEM_2D_COPY_TOEXT_ALL(type,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOEXT_N(type,,dest_as)  \
  DMA_WORK_ITEM_2D_COPY_TOEXT_N(type,2,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOEXT_N(type,3,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOEXT_N(type,4,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOEXT_N(type,8,dest_as) \
  DMA_WORK_ITEM_2D_COPY_TOEXT_N(type,16,dest_as)

#define ASYNC_WORK_ITEM_2D_COPY(type) \
  ASYNC_WORK_ITEM_2D_COPY_TOEXT_ALL(type,global) \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_ALL(type,local) \
  ASYNC_WORK_ITEM_2D_COPY_TOLOC_ALL(type,private) \
  DMA_WORK_ITEM_2D_COPY_TOEXT_ALL(type,global) \
  DMA_WORK_ITEM_2D_COPY_TOLOC_ALL(type,local) \
  DMA_WORK_ITEM_2D_COPY_TOLOC_ALL(type,private)

ASYNC_WORK_ITEM_2D_COPY(char)
ASYNC_WORK_ITEM_2D_COPY(uchar)
ASYNC_WORK_ITEM_2D_COPY(short)
ASYNC_WORK_ITEM_2D_COPY(ushort)
ASYNC_WORK_ITEM_2D_COPY(int)
ASYNC_WORK_ITEM_2D_COPY(uint)
ASYNC_WORK_ITEM_2D_COPY(long)
ASYNC_WORK_ITEM_2D_COPY(ulong)
ASYNC_WORK_ITEM_2D_COPY(float)
