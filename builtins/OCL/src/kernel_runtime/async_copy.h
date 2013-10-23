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


#ifndef ASYNC_COPY_H
#define ASYNC_COPY_H


/*==================================================================*/
/* Work-group async copies                                          */
/*==================================================================*/


/*--------
     1D
  --------*/
static inline event_t async_work_group_copy_generic__local(void *dst, void *src,
							   int size_elements,
							   size_t num_elements,
							   event_t event) {
  int i;
  char *dest=(char *)dst;
  char *source=(char *)src;
  for (i=0;i<(num_elements*size_elements);i++) {
    *dest++=*source++;
  }
  return 0;
}

static inline event_t async_work_group_copy_generic__global(void *dst, void *src,
							    int size_elements,
							    size_t num_elements,
							    event_t event) {
  int i;
  char *dest=(char *)dst;
  char *source=(char *)src;
  for (i=0;i<(num_elements*size_elements);i++) {
    *dest++=*source++;
  }
  return 0;
}


/*--------
     2D
  --------*/
static inline event_t async_work_group_2d_copy_generic__local(void *dst, void *src,
							       int size_elements,
							       size_t src_origin[2], size_t region[2],
							       size_t src_stride,
							       event_t event) {
  char *dest  =(char *)dst;
  char *src_pointer=(char *)src+size_elements*(src_origin[1]*src_stride+src_origin[0]);
  char *source;
							       
  int i,j;
  for (j=0;j<region[1];j++) {
    for (i=0,source=src_pointer;i<(region[0]*size_elements);i++) {
      *dest++=*source++;
    }
    src_pointer+=size_elements*src_stride;
  }
  return 0;
}


static inline event_t async_work_group_2d_copy_generic__global(void *dst, void *src,
							      int size_elements,
							      size_t dst_origin[2], size_t region[2],
							      size_t dst_stride,
							      event_t event) {
  char *dest_pointer =(char *)dst+size_elements*(dst_origin[1]*dst_stride+dst_origin[0]);
  char *source  =(char *)src;
  char *dest;
							       
  int i,j;
  for (j=0;j<region[1];j++) {
    for (i=0,dest=dest_pointer;i<(region[0]*size_elements);i++) {
      *dest++=*source++;
    }
    dest_pointer+=size_elements*dst_stride;
  }
  return 0;
}


/*--------
  Strided
  --------*/
static inline event_t async_work_group_strided_copy_generic__local(void *dst, void *src,
								   int size_elements,
								   size_t num_elements,
								   size_t src_stride,
								   event_t event) {
  int i,j;
  char *source;
  char *dest=(char *)dst;
  char *line_source=(char *)src;
  for (i=0;i<num_elements;i++) {
    for (source=line_source,j=0;j<size_elements;j++) {
      *dest++=*source++;
    }
    line_source+=src_stride*size_elements;
  }
  return 0;
}

static inline event_t async_work_group_strided_copy_generic__global(void *dst, void *src,
								    int size_elements,
								    size_t num_elements,
								    size_t dst_stride,
								    event_t event) {
  int i,j;
  char *dest;
  char *line_dest=(char *)dst;
  char *source=(char *)src;
  for (i=0;i<num_elements;i++) {
    for (dest=line_dest,j=0;j<size_elements;j++) {
      *dest++=*source++;
    }
    line_dest+=dst_stride*size_elements;
  }
  return 0;
}


/* Wait function - nothing done */
static inline void wait_group_events(int num_events, event_t *event_list) {}




/*==================================================================*/
/* Prefetch                                                         */
/*==================================================================*/
static inline void prefetch_generic(const void *src,
				    int size_elements,
				    size_t num_elements
				    ) {
}
  



/*==================================================================*/
/* Work-item Asynch copies                                          */
/*==================================================================*/

/*--------
     1D
  --------*/
static inline event_t async_work_item_copy_generic__private(void *dst, void *src,
							   int size_elements,
							   size_t num_elements,
							   event_t event) {
  int i;
  char *dest=(char *)dst;
  char *source=(char *)src;
  for (i=0;i<(num_elements*size_elements);i++) {
    *dest++=*source++;
  }
  return 0;
}

static inline event_t async_work_item_copy_generic__local(void *dst, void *src,
							   int size_elements,
							   size_t num_elements,
							   event_t event) {
  int i;
  char *dest=(char *)dst;
  char *source=(char *)src;
  for (i=0;i<(num_elements*size_elements);i++) {
    *dest++=*source++;
  }
  return 0;
}

static inline event_t async_work_item_copy_generic__global(void *dst, void *src,
							    int size_elements,
							    size_t num_elements,
							    event_t event) {
  int i;
  char *dest=(char *)dst;
  char *source=(char *)src;
  for (i=0;i<(num_elements*size_elements);i++) {
    *dest++=*source++;
  }
  return 0;
}


/*--------
     2D
  --------*/
static inline event_t async_work_item_2d_copy_generic__private(void *dst, void *src,
							       int size_elements,
							       size_t src_origin[2], size_t region[2],
							       size_t src_stride,
							       event_t event) {
  char *dest  =(char *)dst;
  char *src_pointer=(char *)src+size_elements*(src_origin[1]*src_stride+src_origin[0]);
  char *source;
							       
  int i,j;
  for (j=0;j<region[1];j++) {
    for (i=0,source=src_pointer;i<(region[0]*size_elements);i++) {
      *dest++=*source++;
    }
    src_pointer+=size_elements*src_stride;
  }
  return 0;
}

static inline event_t async_work_item_2d_copy_generic__local(void *dst, void *src,
							     int size_elements,
							     size_t src_origin[2], size_t region[2],
							     size_t src_stride,
							     event_t event) {
  char *dest  =(char *)dst;
  char *src_pointer=(char *)src+size_elements*(src_origin[1]*src_stride+src_origin[0]);
  char *source;
							       
  int i,j;
  for (j=0;j<region[1];j++) {
    for (i=0,source=src_pointer;i<(region[0]*size_elements);i++) {
      *dest++=*source++;
    }
    src_pointer+=size_elements*src_stride;
  }
  return 0;
}


static inline event_t async_work_item_2d_copy_generic__global(void *dst, void *src,
							      int size_elements,
							      size_t dst_origin[2], size_t region[2],
							      size_t dst_stride,
							      event_t event) {
  char *dest_pointer =(char *)dst+size_elements*(dst_origin[1]*dst_stride+dst_origin[0]);
  char *source  =(char *)src;
  char *dest;
							       
  int i,j;
  for (j=0;j<region[1];j++) {
    for (i=0,dest=dest_pointer;i<(region[0]*size_elements);i++) {
      *dest++=*source++;
    }
    dest_pointer+=size_elements*dst_stride;
  }
  return 0;
}


/* Wait function - nothing done */
static inline void wait_events(int num_events, event_t *event_list) {}

static inline void dma_wait_group_events(int num_events, dma_req_t *event_list) {}
static inline void dma_wait_events(int num_events, dma_req_t *event_list) {}







/*==================================================================*/
/* Work-group Asynch copies                                          */
/*==================================================================*/

/*--------
     1D
  --------*/
#define ASYNC_WORK_GROUP_COPY_N(type, n, dest_as) \
static inline event_t __ocl_async_work_group_copy_##type##n##_##dest_as (type##n *dst, const type##n *src, size_t num_elements, event_t event) { \
  return async_work_group_copy_generic__##dest_as ((void *)dst,(void *)src, sizeof(type##n), num_elements, event); \
}

#define ASYNC_WORK_GROUP_COPY_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_COPY_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_COPY_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_COPY_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_COPY_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_COPY_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_COPY_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_COPY(type) \
  ASYNC_WORK_GROUP_COPY_ALL(type,global) \
  ASYNC_WORK_GROUP_COPY_ALL(type,local)

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
#define ASYNC_WORK_GROUP_2D_COPY_N(type, n, dest_as)				\
static inline event_t __ocl_async_work_group_2d_copy_##type##n##_##dest_as (type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, event_t event) { \
  return async_work_group_2d_copy_generic__##dest_as ((void *)dst,(void *)src, sizeof(type##n),global_origin, region, global_stride, event); \
}

#define ASYNC_WORK_GROUP_2D_COPY_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_2D_COPY_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_2D_COPY_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_2D_COPY(type) \
  ASYNC_WORK_GROUP_2D_COPY_ALL(type,global) \
  ASYNC_WORK_GROUP_2D_COPY_ALL(type,local)

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
#define ASYNC_WORK_GROUP_STRIDED_COPY_N(type, n, dest_as) \
static inline event_t __ocl_async_work_group_strided_copy_##type##n##_##dest_as (type##n *dst, const type##n *src, size_t num_elements, size_t global_stride, event_t event) { \
  return async_work_group_strided_copy_generic__##dest_as ((void *)dst,(void *)src, sizeof(type##n), num_elements, global_stride, event); \
}

#define ASYNC_WORK_GROUP_STRIDED_COPY_ALL(type,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_N(type,,dest_as)  \
  ASYNC_WORK_GROUP_STRIDED_COPY_N(type,2,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_N(type,3,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_N(type,4,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_N(type,8,dest_as) \
  ASYNC_WORK_GROUP_STRIDED_COPY_N(type,16,dest_as)

#define ASYNC_WORK_GROUP_STRIDED_COPY(type) \
  ASYNC_WORK_GROUP_STRIDED_COPY_ALL(type,global) \
  ASYNC_WORK_GROUP_STRIDED_COPY_ALL(type,local)

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
  return prefetch_generic((void *)src, sizeof(type##n), num_elements); \
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
#define ASYNC_WORK_ITEM_COPY_N(type, n, dest_as) \
static inline event_t __ocl_async_work_item_copy_##type##n##_##dest_as (type##n *dst, const type##n *src, size_t num_elements, event_t event) { \
  return async_work_item_copy_generic__##dest_as((void *)dst,(void *)src, sizeof(type##n), num_elements, event); \
}

#define ASYNC_WORK_ITEM_COPY_ALL(type, dest_as) \
  ASYNC_WORK_ITEM_COPY_N(type,,dest_as)  \
  ASYNC_WORK_ITEM_COPY_N(type,2,dest_as) \
  ASYNC_WORK_ITEM_COPY_N(type,3,dest_as) \
  ASYNC_WORK_ITEM_COPY_N(type,4,dest_as) \
  ASYNC_WORK_ITEM_COPY_N(type,8,dest_as) \
  ASYNC_WORK_ITEM_COPY_N(type,16,dest_as)

#define ASYNC_WORK_ITEM_COPY(type) \
  ASYNC_WORK_ITEM_COPY_ALL(type,global) \
  ASYNC_WORK_ITEM_COPY_ALL(type,local) \
  ASYNC_WORK_ITEM_COPY_ALL(type,private)

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
#define ASYNC_WORK_ITEM_2D_COPY_N(type, n, dest_as) \
static inline event_t __ocl_async_work_item_2d_copy_##type##n##_##dest_as (type##n *dst, const type##n *src, size_t global_origin[2], size_t region[2], size_t global_stride, event_t event) { \
  return async_work_item_2d_copy_generic__##dest_as ((void *)dst,(void *)src, sizeof(type##n),global_origin, region, global_stride, event); \
}

#define ASYNC_WORK_ITEM_2D_COPY_ALL(type,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_N(type,,dest_as)  \
  ASYNC_WORK_ITEM_2D_COPY_N(type,2,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_N(type,3,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_N(type,4,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_N(type,8,dest_as) \
  ASYNC_WORK_ITEM_2D_COPY_N(type,16,dest_as)

#define ASYNC_WORK_ITEM_2D_COPY(type) \
  ASYNC_WORK_ITEM_2D_COPY_ALL(type,global) \
  ASYNC_WORK_ITEM_2D_COPY_ALL(type,local) \
  ASYNC_WORK_ITEM_2D_COPY_ALL(type,private)

ASYNC_WORK_ITEM_2D_COPY(char)
ASYNC_WORK_ITEM_2D_COPY(uchar)
ASYNC_WORK_ITEM_2D_COPY(short)
ASYNC_WORK_ITEM_2D_COPY(ushort)
ASYNC_WORK_ITEM_2D_COPY(int)
ASYNC_WORK_ITEM_2D_COPY(uint)
ASYNC_WORK_ITEM_2D_COPY(long)
ASYNC_WORK_ITEM_2D_COPY(ulong)
ASYNC_WORK_ITEM_2D_COPY(float)

#endif
