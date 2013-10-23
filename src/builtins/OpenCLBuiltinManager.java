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

package builtins;

import ir.symboltable.symbols.FunctionLabel;
import ir.types.Type;
import ir.types.c.ArithmeticScalar;
import ir.types.c.FloatingPointScalar;
import ir.types.c.Function;
import ir.types.c.IntegerScalar;
import ir.types.c.Qualifier;
import ir.types.c.Void;
import ir.types.ocl.AddressSpace;
import ir.types.ocl.FloatingPointVector;
import ir.types.ocl.IntegerVector;
import ir.types.ocl.Vector;
import ir.base.NodeAST;
import common.CompilerError;
import java.util.HashMap;
import java.util.Map;


public class OpenCLBuiltinManager extends BuiltinManager {

  // ##################################################################
  // Relation String -> type
  // ##################################################################

  public static HashMap<String,Type> typeMap = new HashMap<String,Type>();
  static{
    typeMap.put("char",IntegerScalar.Tschar);
    typeMap.put("uchar",IntegerScalar.Tuchar);
    typeMap.put("short",IntegerScalar.Tsshort);
    typeMap.put("ushort",IntegerScalar.Tushort);
    typeMap.put("int",IntegerScalar.Tsint);
    typeMap.put("uint",IntegerScalar.Tuint);
    typeMap.put("long",IntegerScalar.Tslong);
    typeMap.put("ulong",IntegerScalar.Tulong);
    typeMap.put("float",FloatingPointScalar.Tfloat);

    typeMap.put("char2",IntegerVector.Tschar2);
    typeMap.put("char3",IntegerVector.Tschar3);
    typeMap.put("char4",IntegerVector.Tschar4);
    typeMap.put("char8",IntegerVector.Tschar8);
    typeMap.put("char16",IntegerVector.Tschar16);
    typeMap.put("uchar2",IntegerVector.Tuchar2);
    typeMap.put("uchar3",IntegerVector.Tuchar3);
    typeMap.put("uchar4",IntegerVector.Tuchar4);
    typeMap.put("uchar8",IntegerVector.Tuchar8);
    typeMap.put("uchar16",IntegerVector.Tuchar16);

    typeMap.put("short2",IntegerVector.Tsshort2);
    typeMap.put("short3",IntegerVector.Tsshort3);
    typeMap.put("short4",IntegerVector.Tsshort4);
    typeMap.put("short8",IntegerVector.Tsshort8);
    typeMap.put("short16",IntegerVector.Tsshort16);
    typeMap.put("ushort2",IntegerVector.Tushort2);
    typeMap.put("ushort3",IntegerVector.Tushort3);
    typeMap.put("ushort4",IntegerVector.Tushort4);
    typeMap.put("ushort8",IntegerVector.Tushort8);
    typeMap.put("ushort16",IntegerVector.Tushort16);

    typeMap.put("int2",IntegerVector.Tsint2);
    typeMap.put("int3",IntegerVector.Tsint3);
    typeMap.put("int4",IntegerVector.Tsint4);
    typeMap.put("int8",IntegerVector.Tsint8);
    typeMap.put("int16",IntegerVector.Tsint16);
    typeMap.put("uint2",IntegerVector.Tuint2);
    typeMap.put("uint3",IntegerVector.Tuint3);
    typeMap.put("uint4",IntegerVector.Tuint4);
    typeMap.put("uint8",IntegerVector.Tuint8);
    typeMap.put("uint16",IntegerVector.Tuint16);

    typeMap.put("long2",IntegerVector.Tslong2);
    typeMap.put("long3",IntegerVector.Tslong3);
    typeMap.put("long4",IntegerVector.Tslong4);
    typeMap.put("long8",IntegerVector.Tslong8);
    typeMap.put("long16",IntegerVector.Tslong16);
    typeMap.put("ulong2",IntegerVector.Tulong2);
    typeMap.put("ulong3",IntegerVector.Tulong3);
    typeMap.put("ulong4",IntegerVector.Tulong4);
    typeMap.put("ulong8",IntegerVector.Tulong8);
    typeMap.put("ulong16",IntegerVector.Tulong16);

    typeMap.put("float2",FloatingPointVector.Tfloat2);
    typeMap.put("float3",FloatingPointVector.Tfloat3);
    typeMap.put("float4",FloatingPointVector.Tfloat4);
    typeMap.put("float8",FloatingPointVector.Tfloat8);
    typeMap.put("float16",FloatingPointVector.Tfloat16);
  }

  
  // ##################################################################
  // OCL builtins
  // ##################################################################

  enum OCL_BUILTINS {
    AS_TYPE,
    ASYNC_WORK_GROUP_COPY,
    ASYNC_WORK_GROUP_STRIDED_COPY,
    PREFETCH,
    VLOAD,
    VSTORE,

    // Clam internal
    CLAM_MATH_INTERNAL,

    // Relational
    FLOAT_PARAM_VFVF_TO_VI,
    FLOAT_PARAM_VF_TO_VI,
    ALL_ANY,
    FLOAT_PARAM_VFIVFIVFI_TO_VFI,
    SELECT,

    // Math
    FLOAT_PARAM_VFVF_TO_VF,
    FLOAT_PARAM_VFVFVF_TO_VF,
    FMINMAX,
    FLOAT_PARAM_VF_TO_VF,

    // Integer
    ABS,
    ABS_DIFF,
    INTEGER_I,
    INTEGER_II,
    UPSAMPLE,

    // Integer and common
    CLAMP,
    MINMAX,

    // Geometric
    CROSS,
    FLOAT_PARAM_4VF_TO_F,
    FLOAT_PARAM_4VFVF_TO_F,
    FLOAT_PARAM_4VF_TO_VF,

    // Common
    MIX,
    STEP,
    SMOOTHSTEP,

    // Miscellaneous
    SHUFFLE,
    SHUFFLE2,

    // Atomic
    ATOM1,
    ATOMIC1,
    ATOM2,
    ATOMIC2,
    ATOM_XCHG,
    ATOMIC_XCHG,
    ATOM_CMPXCHG,
    ATOMIC_CMPXCHG,

    // Non implemented
    NON_IMPLEMENTED,

    // extensions
    ASYNC_WORK_GROUP_2D_COPY,
    ASYNC_WORK_ITEM_COPY,
    ASYNC_WORK_ITEM_2D_COPY,

    DMA_WORK_GROUP_COPY,
    DMA_WORK_GROUP_2D_COPY,
    DMA_WORK_ITEM_COPY,
    DMA_WORK_ITEM_2D_COPY,
  };

  public static Map<String,OCL_BUILTINS> oclBuiltinMap = new HashMap<String,OCL_BUILTINS>();
  static{
    // as_type built-ins
    oclBuiltinMap.put("as_char",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uchar",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_short",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ushort",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_int",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uint",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_long",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ulong",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_float",OCL_BUILTINS.AS_TYPE);

    oclBuiltinMap.put("as_char2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_char3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_char4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_char8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_char16",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uchar2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uchar3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uchar4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uchar8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uchar16",OCL_BUILTINS.AS_TYPE);

    oclBuiltinMap.put("as_short2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_short3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_short4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_short8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_short16",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ushort2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ushort3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ushort4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ushort8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ushort16",OCL_BUILTINS.AS_TYPE);

    oclBuiltinMap.put("as_int2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_int3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_int4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_int8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_int16",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uint2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uint3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uint4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uint8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_uint16",OCL_BUILTINS.AS_TYPE);

    oclBuiltinMap.put("as_long2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_long3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_long4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_long8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_long16",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ulong2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ulong3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ulong4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ulong8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_ulong16",OCL_BUILTINS.AS_TYPE);

    oclBuiltinMap.put("as_float2",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_float3",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_float4",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_float8",OCL_BUILTINS.AS_TYPE);
    oclBuiltinMap.put("as_float16",OCL_BUILTINS.AS_TYPE);

    // Async work-group copy
    oclBuiltinMap.put("async_work_group_copy",OCL_BUILTINS.ASYNC_WORK_GROUP_COPY);

    // Async work-group strided copy
    oclBuiltinMap.put("async_work_group_strided_copy",OCL_BUILTINS.ASYNC_WORK_GROUP_STRIDED_COPY);

    // Perfetch
    oclBuiltinMap.put("prefetch",OCL_BUILTINS.PREFETCH);

    // vload
    oclBuiltinMap.put("vload2",OCL_BUILTINS.VLOAD);
    oclBuiltinMap.put("vload3",OCL_BUILTINS.VLOAD);
    oclBuiltinMap.put("vload4",OCL_BUILTINS.VLOAD);
    oclBuiltinMap.put("vload8",OCL_BUILTINS.VLOAD);
    oclBuiltinMap.put("vload16",OCL_BUILTINS.VLOAD);

    // vstore
    oclBuiltinMap.put("vstore2",OCL_BUILTINS.VSTORE);
    oclBuiltinMap.put("vstore3",OCL_BUILTINS.VSTORE);
    oclBuiltinMap.put("vstore4",OCL_BUILTINS.VSTORE);
    oclBuiltinMap.put("vstore8",OCL_BUILTINS.VSTORE);
    oclBuiltinMap.put("vstore16",OCL_BUILTINS.VSTORE);

    //--------  internal clam builtins  ----------
    oclBuiltinMap.put("__MAXFLOAT",OCL_BUILTINS.CLAM_MATH_INTERNAL);
    oclBuiltinMap.put("__NAN",OCL_BUILTINS.CLAM_MATH_INTERNAL);
    oclBuiltinMap.put("__HUGE_VALF",OCL_BUILTINS.CLAM_MATH_INTERNAL);
    oclBuiltinMap.put("__INFINITY",OCL_BUILTINS.CLAM_MATH_INTERNAL);

    //------------------ MATH ---------------------
    oclBuiltinMap.put("acos",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("acosh",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("acospi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("asin",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("asinh",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("asinpi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("atan",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("atan2",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("atanh",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("atanpi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("atan2pi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("cbrt",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("ceil",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("copysign",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("cos",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("cosh",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("cospi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("erfc",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("erf",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("exp",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("exp2",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("exp10",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("expm1",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("fabs",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("fdim",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("floor",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("fma",OCL_BUILTINS.FLOAT_PARAM_VFVFVF_TO_VF);
    oclBuiltinMap.put("fmax",OCL_BUILTINS.FMINMAX);
    oclBuiltinMap.put("fmin",OCL_BUILTINS.FMINMAX);
    oclBuiltinMap.put("fmod",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("fract",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("frexp",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("hypot",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("ilogb",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("ldexp",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("lgamma",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("lgamma_r",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("log",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("log2",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("log10",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("log1p",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("logb",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("mad",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("maxmag",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("minmag",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("modf",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("nan",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("nextafter",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("pow",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("pown",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("powr",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("remainder",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("remquo",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("rint",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("rootn",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("round",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("rsqrt",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("sin",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("sincos",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("sinh",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("sinpi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("sqrt",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("tan",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("tanh",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("tanpi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("tgamma",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("trunc",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);

    //------------------ Integer ---------------------
    oclBuiltinMap.put("abs",OCL_BUILTINS.ABS);
    oclBuiltinMap.put("abs_diff",OCL_BUILTINS.ABS_DIFF);
    oclBuiltinMap.put("add_sat",OCL_BUILTINS.INTEGER_II);
    oclBuiltinMap.put("hadd",OCL_BUILTINS.INTEGER_II);
    oclBuiltinMap.put("rhadd",OCL_BUILTINS.INTEGER_II);
    oclBuiltinMap.put("clz",OCL_BUILTINS.INTEGER_I);
    oclBuiltinMap.put("mad_hi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("mad_sat",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("mul_hi",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("rotate",OCL_BUILTINS.INTEGER_II);
    oclBuiltinMap.put("sub_sat",OCL_BUILTINS.INTEGER_II);
    oclBuiltinMap.put("upsample",OCL_BUILTINS.UPSAMPLE);
    oclBuiltinMap.put("mad24",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("mul24",OCL_BUILTINS.NON_IMPLEMENTED);

    //---------------- Integer & common -------------------
    oclBuiltinMap.put("clamp",OCL_BUILTINS.CLAMP);
    oclBuiltinMap.put("max",OCL_BUILTINS.MINMAX);
    oclBuiltinMap.put("min",OCL_BUILTINS.MINMAX);

    //------------------ Common ---------------------
    oclBuiltinMap.put("degrees",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("mix",OCL_BUILTINS.MIX);
    oclBuiltinMap.put("radians",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("step",OCL_BUILTINS.STEP);
    oclBuiltinMap.put("smoothstep",OCL_BUILTINS.SMOOTHSTEP);
    oclBuiltinMap.put("sign",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);

    //------------------ Geometric ---------------------
    oclBuiltinMap.put("cross",OCL_BUILTINS.CROSS);
    oclBuiltinMap.put("dot",OCL_BUILTINS.FLOAT_PARAM_4VFVF_TO_F);
    oclBuiltinMap.put("distance",OCL_BUILTINS.FLOAT_PARAM_4VFVF_TO_F);
    oclBuiltinMap.put("length",OCL_BUILTINS.FLOAT_PARAM_4VF_TO_F);
    oclBuiltinMap.put("normalize",OCL_BUILTINS.FLOAT_PARAM_4VF_TO_VF);
    oclBuiltinMap.put("fast_distance",OCL_BUILTINS.FLOAT_PARAM_4VFVF_TO_F);
    oclBuiltinMap.put("fast_length",OCL_BUILTINS.FLOAT_PARAM_4VF_TO_F);
    oclBuiltinMap.put("fast_normalize",OCL_BUILTINS.FLOAT_PARAM_4VF_TO_VF);

    //------------------ Relational ---------------------
    oclBuiltinMap.put("isequal",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("isnotequal",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("isgreater",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("isgreaterequal",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("isless",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("islessequal",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("islessgreater",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("isfinite",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VI);
    oclBuiltinMap.put("isinf",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VI);
    oclBuiltinMap.put("isnan",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VI);
    oclBuiltinMap.put("isnormal",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VI);
    oclBuiltinMap.put("isordered",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("isunordered",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VI);
    oclBuiltinMap.put("signbit",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VI);
    oclBuiltinMap.put("all",OCL_BUILTINS.ALL_ANY);
    oclBuiltinMap.put("any",OCL_BUILTINS.ALL_ANY);
    oclBuiltinMap.put("bitselect",OCL_BUILTINS.FLOAT_PARAM_VFIVFIVFI_TO_VFI);
    oclBuiltinMap.put("select",OCL_BUILTINS.SELECT);

    //------------------ Half ---------------------
    oclBuiltinMap.put("half_cos",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_divide",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("half_exp",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_exp2",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_exp10",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("half_log",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_log2",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_log10",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_powr",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("half_recip",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("half_rsqrt",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("half_sin",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_sqrt",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("half_tan",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);

    //------------------ Native ---------------------
    oclBuiltinMap.put("native_cos",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_divide",OCL_BUILTINS.FLOAT_PARAM_VFVF_TO_VF);
    oclBuiltinMap.put("native_exp",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_exp2",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_exp10",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("native_log",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_log2",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_log10",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_powr",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("native_recip",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("native_rsqrt",OCL_BUILTINS.NON_IMPLEMENTED);
    oclBuiltinMap.put("native_sin",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_sqrt",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);
    oclBuiltinMap.put("native_tan",OCL_BUILTINS.FLOAT_PARAM_VF_TO_VF);

    //------------------ Miscellaneous -------------------
    oclBuiltinMap.put("shuffle",OCL_BUILTINS.SHUFFLE);
    oclBuiltinMap.put("shuffle2",OCL_BUILTINS.SHUFFLE2);

    //---------------- 32 bits atomic ------------------
    oclBuiltinMap.put("atom_add",OCL_BUILTINS.ATOM2);
    oclBuiltinMap.put("atom_sub",OCL_BUILTINS.ATOM2);
    oclBuiltinMap.put("atom_xchg",OCL_BUILTINS.ATOM_XCHG);
    oclBuiltinMap.put("atom_inc",OCL_BUILTINS.ATOM1);
    oclBuiltinMap.put("atom_dec",OCL_BUILTINS.ATOM1);
    oclBuiltinMap.put("atom_cmpxchg",OCL_BUILTINS.ATOM_CMPXCHG);
    oclBuiltinMap.put("atom_min",OCL_BUILTINS.ATOM2);
    oclBuiltinMap.put("atom_max",OCL_BUILTINS.ATOM2);
    oclBuiltinMap.put("atom_and",OCL_BUILTINS.ATOM2);
    oclBuiltinMap.put("atom_or",OCL_BUILTINS.ATOM2);
    oclBuiltinMap.put("atom_xor",OCL_BUILTINS.ATOM2);

    oclBuiltinMap.put("atomic_add",OCL_BUILTINS.ATOMIC2);
    oclBuiltinMap.put("atomic_sub",OCL_BUILTINS.ATOMIC2);
    oclBuiltinMap.put("atomic_xchg",OCL_BUILTINS.ATOMIC_XCHG);
    oclBuiltinMap.put("atomic_inc",OCL_BUILTINS.ATOMIC1);
    oclBuiltinMap.put("atomic_dec",OCL_BUILTINS.ATOMIC1);
    oclBuiltinMap.put("atomic_cmpxchg",OCL_BUILTINS.ATOMIC_CMPXCHG);
    oclBuiltinMap.put("atomic_min",OCL_BUILTINS.ATOMIC2);
    oclBuiltinMap.put("atomic_max",OCL_BUILTINS.ATOMIC2);
    oclBuiltinMap.put("atomic_and",OCL_BUILTINS.ATOMIC2);
    oclBuiltinMap.put("atomic_or",OCL_BUILTINS.ATOMIC2);
    oclBuiltinMap.put("atomic_xor",OCL_BUILTINS.ATOMIC2);

    //--------------- EXTENSIONS ------------------
    oclBuiltinMap.put("async_work_group_2d_copy",OCL_BUILTINS.ASYNC_WORK_GROUP_2D_COPY);
    oclBuiltinMap.put("async_work_item_copy",OCL_BUILTINS.ASYNC_WORK_ITEM_COPY);
    oclBuiltinMap.put("async_work_item_2d_copy",OCL_BUILTINS.ASYNC_WORK_ITEM_2D_COPY);

    oclBuiltinMap.put("dma_work_group_copy",OCL_BUILTINS.DMA_WORK_GROUP_COPY);
    oclBuiltinMap.put("dma_work_group_2d_copy",OCL_BUILTINS.DMA_WORK_GROUP_2D_COPY);
    oclBuiltinMap.put("dma_work_item_copy",OCL_BUILTINS.DMA_WORK_ITEM_COPY);
    oclBuiltinMap.put("dma_work_item_2d_copy",OCL_BUILTINS.DMA_WORK_ITEM_2D_COPY);
  }


// ##########################################################################
// 
// ##########################################################################


  // ******************************************************************
  // isFunctionName  :
  //
  // Check if the function is a builtin function and in this case,
  // check if operands are corrects
  // It returns null if it is not a builtin function and a string which
  // corresponds to the name the function must finally have
  // ******************************************************************
  public  boolean isBuiltinFunctionName(String function_name) {
    if (oclBuiltinMap.containsKey(function_name)) {
      return true;
    }
    else if (function_name.startsWith("convert_")) {
      return isConvert(function_name);
    }
    return false;
  }


  // ******************************************************************
  // checkBuiltinFunction  :
  //
  // Check if the function is a builtin function and in this case,
  // check if operands are corrects
  // It returns null if it is not a builtin function and a string which
  // corresponds to the name the function must finaly have
  // In case of positive builtin detection, the function sets the
  // return type of function_type
  // ******************************************************************
  public String checkBuiltinFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {

    String function_name=function_symbol.getName();
    OCL_BUILTINS b=oclBuiltinMap.get(function_name);
    if (b==null) {
      if (function_name.startsWith("convert_")) {
        return checkConvertFunction(node,ce,function_symbol,function_type);
      }
    }
    else {
      switch(b) {
      case AS_TYPE:
        return checkAsTypeFunction(node,ce,function_symbol,function_type);
      case ASYNC_WORK_GROUP_COPY:
        return checkAsyncWorkGroupCopyFunction(node,ce,function_symbol,function_type);
      case ASYNC_WORK_GROUP_STRIDED_COPY:
        return checkAsyncWorkGroupStridedCopyFunction(node,ce,function_symbol,function_type);
      case PREFETCH:
        return checkPrefetchFunction(node,ce,function_symbol,function_type);
      case VLOAD:
        return checkVloadFunction(node,ce,function_symbol,function_type);
      case VSTORE:
        return checkVstoreFunction(node,ce,function_symbol,function_type);

        //------ Relational ------
      case CLAM_MATH_INTERNAL:
        return checkClamMathInternalFunction(node,ce,function_symbol,function_type);

        //------ Relational ------
      case FLOAT_PARAM_VFVF_TO_VI:
        return checkParam_VFVF_to_VI(node,ce,function_symbol,function_type);
      case FLOAT_PARAM_VF_TO_VI:
        return checkParam_VF_to_VI(node,ce,function_symbol,function_type);
      case ALL_ANY:
        return checkAllAnyFunction(node,ce,function_symbol,function_type);
      case FLOAT_PARAM_VFIVFIVFI_TO_VFI:
        return checkParam_VFIVFIVFI_to_VFI(node,ce,function_symbol,function_type);
      case SELECT:
        return checkSelectFunction(node,ce,function_symbol,function_type);

        //------ Math ------
      case FLOAT_PARAM_VFVF_TO_VF:
        return checkParam_VFVF_to_VF(node,ce,function_symbol,function_type);
      case FLOAT_PARAM_VFVFVF_TO_VF:
        return checkParam_VFVFVF_to_VF(node,ce,function_symbol,function_type);
      case FMINMAX:
        return checkFMinMaxFunction(node,ce,function_symbol,function_type);
      case FLOAT_PARAM_VF_TO_VF:
        return checkParam_VF_to_VF(node,ce,function_symbol,function_type);

        //------ Integer ------
      case ABS:
        return checkAbsFunction(node,ce,function_symbol,function_type);
      case ABS_DIFF:
        return checkAbsDiffFunction(node,ce,function_symbol,function_type);
      case INTEGER_I:
        return checkParamIFunction(node,ce,function_symbol,function_type);
      case INTEGER_II:
        return checkParamIIFunction(node,ce,function_symbol,function_type);
      case UPSAMPLE:
        return checkUpsampleFunction(node,ce,function_symbol,function_type);

        //------ Integer & Common------
      case CLAMP:
        return checkClampFunction(node,ce,function_symbol,function_type);
      case MINMAX:
        return checkMinMaxFunction(node,ce,function_symbol,function_type);

        //------ Geometric ------
      case CROSS:
        return checkCrossFunction(node,ce,function_symbol,function_type);
      case FLOAT_PARAM_4VF_TO_VF:
        return checkParam_4VF_to_VF(node,ce,function_symbol,function_type);
      case FLOAT_PARAM_4VF_TO_F:
        return checkParam_4VF_to_F(node,ce,function_symbol,function_type);
      case FLOAT_PARAM_4VFVF_TO_F:
        return checkParam_4VFVF_to_F(node,ce,function_symbol,function_type);

        //------ Common ------
      case MIX:
        return checkMixFunction(node,ce,function_symbol,function_type);
      case STEP:
        return checkStepFunction(node,ce,function_symbol,function_type);
      case SMOOTHSTEP:
        return checkSmoothstepFunction(node,ce,function_symbol,function_type);

        //------ Miscellaneous ------
      case SHUFFLE:
        return checkShuffleFunction(node,ce,function_symbol,function_type);
      case SHUFFLE2:
        return checkShuffle2Function(node,ce,function_symbol,function_type);

        //------ Extension ------
      case ASYNC_WORK_ITEM_COPY:
        return checkAsyncWorkItemCopyFunction(node,ce,function_symbol,function_type);
      case ASYNC_WORK_GROUP_2D_COPY:
        return checkAsyncWorkGroup2DCopyFunction(node,ce,function_symbol,function_type);
      case ASYNC_WORK_ITEM_2D_COPY:
        return checkAsyncWorkItem2DCopyFunction(node,ce,function_symbol,function_type);
      case DMA_WORK_GROUP_COPY:
        return checkDmaWorkGroupCopyFunction(node,ce,function_symbol,function_type);
      case DMA_WORK_ITEM_COPY:
        return checkDmaWorkItemCopyFunction(node,ce,function_symbol,function_type);
      case DMA_WORK_GROUP_2D_COPY:
        return checkDmaWorkGroup2DCopyFunction(node,ce,function_symbol,function_type);
      case DMA_WORK_ITEM_2D_COPY:
        return checkDmaWorkItem2DCopyFunction(node,ce,function_symbol,function_type);
       
              
        //------ Atomic ------
      case ATOM1:
        return checkAtomic1(node,ce,function_symbol,function_type,true);
      case ATOMIC1:
        return checkAtomic1(node,ce,function_symbol,function_type,false);
      case ATOM2:
        return checkAtomic2(node,ce,function_symbol,function_type,true);
      case ATOMIC2:
        return checkAtomic2(node,ce,function_symbol,function_type,false);
      case ATOM_XCHG:
        return checkAtomicXchg(node,ce,function_symbol,function_type,true);
      case ATOMIC_XCHG:
        return checkAtomicXchg(node,ce,function_symbol,function_type,false);
      case ATOM_CMPXCHG:
        return checkAtomicCmpXchg(node,ce,function_symbol,function_type,true);
      case ATOMIC_CMPXCHG:
        return checkAtomicCmpXchg(node,ce,function_symbol,function_type,false);

        //------ Non yet implemented ------
      case NON_IMPLEMENTED:
        ce.raiseError(node,"OpenCL builtin function '"+
            function_name+"' not yet implemented");
        // Arbitrary return 'int'
        function_type.setReturnType(IntegerScalar.Tsint);
        return function_name;	  
      }
    }


    // It is not a builtin function
    return null;
  }

  // ##################################################################
  // Generic builtin functions
  // ##################################################################

  private String checkClamMathInternalFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    function_type.setReturnType(FloatingPointScalar.Tfloat);
    if (checkNbParam(node,ce,function_name,function_type,0)) {	
      return "__clam" + function_name;
    }

    return function_name;
  }


  private String checkParam_VF_to_VI(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0).unqualify();

      if (param_type.isFloatScalar()) {
        function_type.setReturnType(IntegerScalar.Tsint);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else if (param_type.isFloatVector()) {
        function_type.setReturnType(IntegerVector.getSintVector(((Vector)param_type).getNbElements()));
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }

  private String checkParam_VFVF_to_VI(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type1.isFloatScalar()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(IntegerScalar.Tsint);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else if (param_type1.isFloatVector()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(IntegerVector.getSintVector(((Vector)param_type1).getNbElements()));
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }

  private String checkParam_VF_to_VF(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0).unqualify();

      if (param_type.isFloatScalar() || param_type.isFloatVector()) {
        function_type.setReturnType(param_type);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }

  private String checkParam_VFVF_to_VF(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type1.isFloatScalar() || param_type1.isFloatVector()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }

  private String checkParam_VFVFVF_to_VF(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();
      Type param_type3=function_type.getParameterType(2).unqualify();

      if (param_type1.isFloatScalar() || param_type1.isFloatVector()) {
        if ((param_type1!=param_type2) || (param_type1!=param_type3)) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }

  private String checkParam_VFIVFIVFI_to_VFI(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();
      Type param_type3=function_type.getParameterType(2).unqualify();

      if (
          param_type1.isFloatScalar() ||
          param_type1.isIntegerScalar() ||
          param_type1.isVector()
          ) {
        if ((param_type1!=param_type2) || (param_type1!=param_type3)) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }

  private String checkParam_4VF_to_VF(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0).unqualify();

      if (param_type.isFloatScalar()) {
        function_type.setReturnType(param_type);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else if (param_type.isFloatVector()) {
        if (param_type.getNbVectorElements() > 4 ) {
          ce.raiseError(node,"builtin function '"+
              function_name+"' does not support vectors with more than 4 elements");
        }
        function_type.setReturnType(param_type);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }


  private String checkParam_4VF_to_F(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0).unqualify();

      if (param_type.isFloatScalar()) {
        function_type.setReturnType(FloatingPointScalar.Tfloat);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else if (param_type.isFloatVector()) {
        if (param_type.getNbVectorElements() > 4 ) {
          ce.raiseError(node,"builtin function '"+
              function_name+"' does not support vectors with more than 4 elements");
        }
        function_type.setReturnType(FloatingPointScalar.Tfloat);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }

  private String checkParam_4VFVF_to_F(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type1.isFloatScalar()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(FloatingPointScalar.Tfloat);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else if (param_type1.isFloatVector()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          if (param_type1.getNbVectorElements() > 4 ) {
            ce.raiseError(node,"builtin function '"+
                function_name+"' does not support vectors with more than 4 elements");
          }
          function_type.setReturnType(FloatingPointScalar.Tfloat);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }

  private String checkParamIFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0).unqualify();

      if (param_type.isIntegerScalar() || param_type.isIntegralVector()) {
        function_type.setReturnType(param_type);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }

  private String checkParamIIFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type1.isIntegerScalar() || param_type1.isIntegralVector()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }



  // ##################################################################
  // Math builtin functions
  // ##################################################################
  private String checkFMinMaxFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type1.isIntegralVector()) {
        IntegerVector vector_param_type=(IntegerVector)param_type1;
        if (param_type2==vector_param_type) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type);
        }
        else if ( param_type2==vector_param_type.getBaseType() ) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type.getBaseType());
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else if (param_type1.isFloatScalar()) {
        if (param_type2!=param_type1) {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1)+
              "_"+DumpAsType(param_type1);
        }
      }
      else if (param_type1.isFloatVector()) {
        FloatingPointVector vector_param_type=(FloatingPointVector)param_type1;
        if (param_type2==vector_param_type) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type);
        }
        else if ( param_type2==vector_param_type.getBaseType() ) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type.getBaseType());
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else {
        ce.raiseError(node,"wrong first parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }


  // ##################################################################
  // Geometric builtin functions
  // ##################################################################

  private String checkCrossFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if ( 
          !(
              param_type1.isFloatVector() &&
              (
                  ( ((Vector)param_type1).getNbElements()==3 ) ||
                  ( ((Vector)param_type1).getNbElements()==4 )
                  )
              )
          ) {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
      else {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }


  // ##################################################################
  // Integer builtin functions
  // ##################################################################

  private String checkAbsFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0).unqualify();
      if (param_type.isIntegerScalar()) {
        Type dest_type=((IntegerScalar)param_type).getUnsignedVersion();
        function_type.setReturnType(dest_type);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else if (param_type.isIntegralVector()) {
        Type dest_type=((IntegerVector)param_type).getUnsignedVersion();
        function_type.setReturnType(dest_type);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else {
        ce.raiseError(node,"parameters must be integer for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tuint);
    return function_name;
  }

  private String checkAbsDiffFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type2 != param_type1) {
        ce.raiseError(node,"non coherent parameters for builtin function '"+
            function_name+"'");
      }
      else {
        if (param_type1.isIntegerScalar()) {
          Type dest_type=((IntegerScalar)param_type1).getUnsignedVersion();
          function_type.setReturnType(dest_type);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
        else if (param_type1.isIntegralVector()) {
          Type dest_type=((IntegerVector)param_type1).getUnsignedVersion();
          function_type.setReturnType(dest_type);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
        }
        else {
          ce.raiseError(node,"parameters must be integer for builtin function '"+
              function_name+"'");
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tuint);
    return function_name;
  }


  private String checkClampFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_type=function_type.getParameterType(0).unqualify();
      Type min_type=function_type.getParameterType(1).unqualify();
      Type max_type=function_type.getParameterType(2).unqualify();

      if (param_type.isIntegerScalar()) {
        if ((min_type!=param_type) || (max_type!=param_type)) {
          ce.raiseError(node,"wrong min/max parameters for builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type)+
              "_"+DumpAsType(param_type);
        }
      }
      else if (param_type.isIntegralVector()) {
        IntegerVector vector_param_type=(IntegerVector)param_type;
        if ((min_type==vector_param_type) && (max_type==vector_param_type)) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type);
        }
        else if ( (min_type==vector_param_type.getBaseType()) &&
            (max_type==vector_param_type.getBaseType()) ) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type.getBaseType());
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else if (param_type.isFloatScalar()) {
        if ((min_type!=param_type) || (max_type!=param_type)) {
          ce.raiseError(node,"wrong min/max parameters for builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type)+
              "_"+DumpAsType(param_type);
        }
      }
      else if (param_type.isFloatVector()) {
        FloatingPointVector vector_param_type=(FloatingPointVector)param_type;
        if ((min_type==vector_param_type) && (max_type==vector_param_type)) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type);
        }
        else if  ((min_type==vector_param_type.getBaseType()) && 
            (max_type==vector_param_type.getBaseType()) ) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type.getBaseType());
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else {
        ce.raiseError(node,"wrong first parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  private String checkMinMaxFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type1.isIntegerScalar()) {
        if (param_type2!=param_type1) {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1)+
              "_"+DumpAsType(param_type1);
        }
      }
      else if (param_type1.isIntegralVector()) {
        IntegerVector vector_param_type=(IntegerVector)param_type1;
        if (param_type2==vector_param_type) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type);
        }
        else if ( param_type2==vector_param_type.getBaseType() ) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type.getBaseType());
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else if (param_type1.isFloatScalar()) {
        if (param_type2!=param_type1) {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1)+
              "_"+DumpAsType(param_type1);
        }
      }
      else if (param_type1.isFloatVector()) {
        FloatingPointVector vector_param_type=(FloatingPointVector)param_type1;
        if (param_type2==vector_param_type) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type);
        }
        else if ( param_type2==vector_param_type.getBaseType() ) {
          function_type.setReturnType(vector_param_type);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type)+
              "_"+DumpAsType(vector_param_type.getBaseType());
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else {
        ce.raiseError(node,"wrong first parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  private String checkUpsampleFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type1.isIntegerScalar()) {
        IntegerScalar param_type1_scalar=(IntegerScalar)param_type1;
        switch( param_type1_scalar.getBaseType()) {
        case CHAR:
        case SHORT_INT:
        case INT:
          if (param_type2==param_type1_scalar.getUnsignedVersion()) {
            // parameters OK
            IntegerScalar return_type=null;
            switch( param_type1_scalar.getBaseType()) {
            case CHAR:
              return_type = param_type1_scalar.isSigned() ?
                  IntegerScalar.Tsshort : IntegerScalar.Tushort;
              break;
            case SHORT_INT:
              return_type = param_type1_scalar.isSigned() ?
                  IntegerScalar.Tsint : IntegerScalar.Tuint;
              break;
            case INT:
              return_type = param_type1_scalar.isSigned() ?
                  IntegerScalar.Tslong : IntegerScalar.Tulong;
              break;
            default:
              ce.raiseError(node,"wrong parameters for builtin function '"+
                  function_name+"'");           
              break;
            }
            function_type.setReturnType(return_type);
            return "__ocl_"+function_name+"_"+DumpAsType(return_type);
          }
          break;
        default:
          ce.raiseError(node,"wrong parameters for builtin function '"+
              function_name+"'");
          break;
        }
      }
      else if (param_type1.isIntegralVector()) {
        IntegerVector param_type1_vec=(IntegerVector)param_type1;
        switch( (param_type1_vec).getBaseType().getBaseType()) {
        case CHAR:
        case SHORT_INT:
        case INT:
          if (param_type2==param_type1_vec.getUnsignedVersion()) {
            // parameters OK
            IntegerVector return_type=null;
            switch( param_type1_vec.getBaseType().getBaseType()) {
            case CHAR:
              return_type = param_type1_vec.isSigned() ?
                  IntegerVector.getSshortVector(param_type1_vec.getNbElements()) :
                    IntegerVector.getUshortVector(param_type1_vec.getNbElements());
                  break;
            case SHORT_INT:
              return_type = param_type1_vec.isSigned() ?
                  IntegerVector.getSintVector(param_type1_vec.getNbElements()) :
                    IntegerVector.getUintVector(param_type1_vec.getNbElements());
                  break;
            case INT:
              return_type = param_type1_vec.isSigned() ?
                  IntegerVector.getSlongVector(param_type1_vec.getNbElements()) :
                    IntegerVector.getUlongVector(param_type1_vec.getNbElements());
                  break;
            default:
              ce.raiseError(node,"wrong parameters for builtin function '"+
                  function_name+"'");           
              break;
            }
            function_type.setReturnType(return_type);
            return "__ocl_"+function_name+"_"+DumpAsType(return_type);
          }
          break;
        default:
          ce.raiseError(node,"wrong parameters for builtin function '"+
              function_name+"'");           
          break;
        }
      }

      ce.raiseError(node,"wrong parameters for builtin function '"+
          function_name+"'");
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  // ##################################################################
  // Common built-in functions
  // ##################################################################

  private String checkMixFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();
      Type param_type3=function_type.getParameterType(2).unqualify();

      if (param_type1.isFloatScalar() || param_type1.isFloatVector()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          if (param_type3!=FloatingPointScalar.Tfloat) {
            ce.raiseError(node,"the third parameter should be 'float' for builtin function '"+
                function_name+"'");
          }
          else {
            function_type.setReturnType(param_type1);
            return "__ocl_"+function_name+"_"+DumpAsType(param_type1);
          }
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }


  private String checkStepFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (param_type2.isFloatScalar()) {
        if (param_type1!=param_type2) {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type2);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type2)+
              "_"+DumpAsType(param_type2);
        }
      }
      else if (param_type2.isFloatVector()) {
        FloatingPointVector vector_param_type2=(FloatingPointVector)param_type2;
        if (param_type1==vector_param_type2) {
          function_type.setReturnType(vector_param_type2);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type2)+
              "_"+DumpAsType(vector_param_type2);
        }
        else if ( param_type1==vector_param_type2.getBaseType() ) {
          function_type.setReturnType(vector_param_type2);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type2.getBaseType())+
              "_"+DumpAsType(vector_param_type2);
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else {
        ce.raiseError(node,"wrong parameters for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }

  private String checkSmoothstepFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();
      Type param_type3=function_type.getParameterType(2).unqualify();

      if (param_type3.isFloatScalar()) {
        if ((param_type1!=param_type3) || (param_type2!=param_type3)) {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type2);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type3)+
              "_"+DumpAsType(param_type3);
        }
      }
      else if (param_type3.isFloatVector()) {
        FloatingPointVector vector_param_type3=(FloatingPointVector)param_type3;
        if ((param_type1==vector_param_type3)&&(param_type2==vector_param_type3)) {
          function_type.setReturnType(vector_param_type3);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type3)+
              "_"+DumpAsType(vector_param_type3);
        }
        else if ( (param_type1==vector_param_type3.getBaseType()) && (param_type2==vector_param_type3.getBaseType()) ) {
          function_type.setReturnType(vector_param_type3);
          return "__ocl_"+function_name+"_"+DumpAsType(vector_param_type3.getBaseType())+
              "_"+DumpAsType(vector_param_type3);
        }
        else {
          ce.raiseError(node,"non coherent parameters for builtin function '"+
              function_name+"'");
        }
      }
      else {
        ce.raiseError(node,"wrong parameters for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(FloatingPointScalar.Tfloat);
    return function_name;
  }


  // ##################################################################
  // 'convert' builtin functions
  // ##################################################################

  public boolean isConvert(String function_name) {
    String s=function_name.substring("convert_".length());
    String dest_type;
    if (s.contains("_")) {
      int index=s.indexOf('_');
      dest_type=s.substring(0,index);
      s=s.substring(index+1);
    }
    else {
      dest_type=s;
      s="";
    }
    if (!typeMap.containsKey(dest_type)) {
      // Non recognized type, it is not a builtin function
      return false;
    }

    if (s.equals("")) {
      return true;
    }


    boolean isSat=false;
    if (s.contains("_")) {
      // An other modifier, it is necessarily 'sat'
      int index=s.indexOf('_');
      String modifier=s.substring(0,index);
      if (!modifier.equals("sat")) {
        return false;
      }
      isSat=true;
      s=s.substring(index+1);
    }

    // Last modifier
    if (s.equals("sat")) {
      if (isSat) {
        // Already refined
        return false;
      }
      return true;
    }
    else if ( (s.equals("rte")) || (s.equals("rtz")) ||
        (s.equals("rtp")) ||(s.equals("rtn")) ) {
      return true;
    }
    return false;
  }

  // Here, we know that the builtin function is correct
  private String checkConvertFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    String s=function_name.substring("convert_".length());

    String dest_type_name;
    String modifiers;
    if (s.contains("_")) {
      int index=s.indexOf('_');
      dest_type_name=s.substring(0,index);
      modifiers=s.substring(index+1);
    }
    else {
      dest_type_name=s;
      modifiers="";
    }

    Type dest_type=typeMap.get(dest_type_name);
    // The function will always return thsi type
    function_type.setReturnType(dest_type);

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type source_type =function_type.getParameterType(0).unqualify();

      if (dest_type.isIntegerScalar() || dest_type.isFloatScalar()) {
        if ( (!source_type.isIntegerScalar()) && (!source_type.isFloatScalar())) {
          // No correspondence source/dest
          ce.raiseError(node,"source and destination don't have the same number of elements in builtin function '"+
              function_name+"'");
          return function_name;
        }
      }
      else {
        // Necessarily a vector
        if ((!source_type.isVector()) || (dest_type.getNbVectorElements()!=source_type.getNbVectorElements())) {
          ce.raiseError(node,"source and destination don't have the same number of elements in builtin function '"+
              function_name+"'");
          return function_name;	  
        }
      }

      // Get information regarding modifiers
      boolean isSat=false;
      boolean isRound=false;
      if (!modifiers.equals("")) {
        if (modifiers.contains("_")) {
          // There are 2 modifiers, necessarily saturation and rounding
          isSat=true;
          isRound=true;
        }
        else if (modifiers.equals("sat")) {
          isSat=true;
        }
        else {
          // Necessarily rounding
          isRound=true;
        }
      }

      // Check modifiers and type coherency
      if (isSat && (!(dest_type.isIntegerScalar() || dest_type.isIntegralVector()))) {
        ce.raiseError(node,"saturating convertion must be to integer type (builtin '"+
            function_name+"')");
        return function_name;	  
      }

      if (isRound) {
        if ( ( ( dest_type.isIntegerScalar() || dest_type.isIntegralVector() ) && 
            ( source_type.isIntegerScalar() || source_type.isIntegralVector() ) ) ||
            ( ( source_type.isIntegerScalar() || source_type.isIntegralVector() ) && 
                ( dest_type.isIntegerScalar() || dest_type.isIntegralVector() ) )
            ) {
          ce.raiseError(node,"rounding must be between integer and floating point type (builtin '"+
              function_name+"')");
          return function_name;	    
        }

        // Not supported yet 
        ce.raiseError(node,"rounded conversions not supported yet (builtin '"+
            function_name+"')");
      }

      // Return 'convert_<dest>_<source>_<nmodifiers>' even in case of error
      if (isSat || isRound) {
        return "__ocl_convert_"+DumpAsType(dest_type)+"_"+DumpAsType(source_type)+"_"+modifiers;
      }
      else {
        return "__ocl_convert_"+DumpAsType(dest_type)+"_"+DumpAsType(source_type);
      }
    }

    return function_name;	    
  }



  // ##################################################################
  // 'as_typen' builtin functions
  // ##################################################################

  private String checkAtomic1(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type,
      boolean atom) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type1=function_type.getParameterType(0).unqualify();

      if (
          (!param_type1.isPointer()) || 
          (!param_type1.getPointedType().isIntScalar())
          ) {
        ce.raiseError(node,"parameter must be a pointer to integer in builtin function '"+
            function_name+"'");
      }
      else {
        Type pointed_type=param_type1.getPointedType();
        if (!pointed_type.isGlobalAddressSpaceQualified() && !pointed_type.isLocalAddressSpaceQualified()) {
          ce.raiseError(node,"parameter must point to global or local address spaces in builtin function '"+
              function_name+"'");
        }
        else {
          function_symbol.setExternalBuiltinFunction();
          function_type.setReturnType(pointed_type.unqualify());
          if (atom) {
            return "__ocl_atomic"+function_name.substring(4)+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
          else {
            return "__ocl_"+function_name+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  private String checkAtomic2(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type,
      boolean atom) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (
          (!param_type1.isPointer()) || 
          (!param_type1.getPointedType().isIntScalar())
          ) {
        ce.raiseError(node,"first parameter must be a pointer to int in builtin function '"+
            function_name+"'");
      }
      else if (!param_type2.isIntegerScalar()) {
        ce.raiseError(node,"second parameter must be an integer in builtin function '"+
            function_name+"'");
      }
      else {
        Type pointed_type=param_type1.getPointedType();
        if (!pointed_type.isGlobalAddressSpaceQualified() && !pointed_type.isLocalAddressSpaceQualified()) {
          ce.raiseError(node,"first parameter must point to global or local address spaces in builtin function '"+
              function_name+"'");
        }
        else {
          function_symbol.setExternalBuiltinFunction();
          function_type.setReturnType(pointed_type.unqualify());
          if (atom) {
            return "__ocl_atomic"+function_name.substring(4)+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
          else {
            return "__ocl_"+function_name+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }

  private String checkAtomicXchg(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type,
      boolean atom) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();

      if (
          (!param_type1.isPointer()) || 
          ( !param_type1.getPointedType().isIntScalar() &&
              !param_type1.getPointedType().isFloatScalar() )
          ) {
        ce.raiseError(node,"first parameter must be a pointer to integer or float in builtin function '"+
            function_name+"'");
      }
      else {
        Type pointed_type=param_type1.getPointedType();
        if (!pointed_type.isGlobalAddressSpaceQualified() && !pointed_type.isLocalAddressSpaceQualified()) {
          ce.raiseError(node,"first parameter must point to global or local address spaces in builtin function '"+
              function_name+"'");
        }

        else if (pointed_type.isIntScalar() && !param_type2.isIntegerScalar()) {
          ce.raiseError(node,"Non coherent parameters in builtin function '"+
              function_name+"'");
        }
        else if (pointed_type.isFloatScalar() && !param_type2.isArithmeticScalar()) {
          ce.raiseError(node,"Non coherent parameters in builtin function '"+
              function_name+"'");
        }
        else {
          function_symbol.setExternalBuiltinFunction();
          function_type.setReturnType(pointed_type.unqualify());
          if (atom) {
            return "__ocl_atomic"+function_name.substring(4)+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
          else {
            return "__ocl_"+function_name+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }

  private String checkAtomicCmpXchg(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type,
      boolean atom) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();
      Type param_type3=function_type.getParameterType(2).unqualify();

      if (
          (!param_type1.isPointer()) || 
          (!param_type1.getPointedType().isIntScalar())
          ) {
        ce.raiseError(node,"first parameter must be a pointer to integer in builtin function '"+
            function_name+"'");
      }
      else if (!param_type2.isIntegerScalar()) {
        ce.raiseError(node,"second parameter must be an integer in builtin function '"+
            function_name+"'");
      }
      else if (!param_type3.isIntegerScalar()) {
        ce.raiseError(node,"third parameter must be an integer in builtin function '"+
            function_name+"'");
      }
      else {
        Type pointed_type=param_type1.getPointedType();
        if (!pointed_type.isGlobalAddressSpaceQualified() && !pointed_type.isLocalAddressSpaceQualified()) {
          ce.raiseError(node,"first parameter must point to global or local address spaces in builtin function '"+
              function_name+"'");
        }
        else {
          function_symbol.setExternalBuiltinFunction();
          function_type.setReturnType(pointed_type.unqualify());
          if (atom) {
            return "__ocl_atomic"+function_name.substring(4)+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
          else {
            return "__ocl_"+function_name+"_"+DumpAsType(pointed_type.unqualify())+
                "_"+getASName(((Qualifier)pointed_type).getAddressSpace());
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }



  // ##################################################################
  // 'as_typen' builtin functions
  // ##################################################################

  private String checkAsTypeFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    String dest_type_name=function_name.substring("as_".length());
    Type dest_type=typeMap.get(dest_type_name);
    if (dest_type==null) {
      // Not recognized as a correct builtin
      return null;
    }
    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0).unqualify();

      if (param_type.isArithmeticScalar() || param_type.isVector()) {
        // Accepted source type
        if (dest_type.sizeof()==param_type.sizeof()) {
          // Return '__as_type_<dest>_<source>' even in case of error
          function_type.setReturnType(dest_type);
          return "__ocl_as_"+DumpAsType(dest_type)+"_"+DumpAsType(param_type);
        }
        else {
          // No correspondence operand/dest sizes
          ce.raiseError(node,"size of source and targets differ for operator '"+
              function_name+"'");
        }
      }
      else {
        ce.raiseError(node,"non allowed parameter type for operator '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(dest_type);
    return "__ocl_as_"+DumpAsType(dest_type);
  }


  // ##################################################################
  // work-group async copy builtin functions
  // ##################################################################

  private String checkAsyncWorkGroupCopyFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    if (checkNbParam(node,ce,function_name,function_type,4)) {
      // Right number of parameters
      Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
      Type param_pointer_source =function_type.getParameterType(1).unqualify();
      Type param_nb_elem        =function_type.getParameterType(2).unqualify();
      Type param_event          =function_type.getParameterType(3).unqualify();

      // Check the first parameter
      Type qualifier=getPointedType(param_pointer_dest);
      if (qualifier==null) {
        ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
            function_name+"'");
      }
      else {
        if (!(qualifier instanceof Qualifier)) {
          ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
              function_name+"'");
        }
        else {
          AddressSpace as=((Qualifier)qualifier).getAddressSpace();
          Type gentype=((Qualifier)qualifier).getQualifiedType();
          if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.LOCAL)) {
            ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
                function_name+"'");
          }
          else {
            if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
                (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
                ) {
              ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
                  function_name+"'");
            }
            else {
              // Check the second parameter
              Type qualifier1=getPointedType(param_pointer_source);
              if (qualifier1==null) {
                ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
                    function_name+"'");
              }
              else {
                if (!(qualifier1 instanceof Qualifier)) {
                  ce.raiseError(node,"wrong address space for second parameter of builtin function '"+
                      function_name+"'");
                }
                else {
                  AddressSpace as1=((Qualifier)qualifier1).getAddressSpace();
                  Type gentype1=((Qualifier)qualifier1).getQualifiedType();
                  if (
                      ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.LOCAL)) ||
                      ( (as == AddressSpace.LOCAL) && ((as1 != AddressSpace.GLOBAL)&&(as1 != AddressSpace.CONSTANT)) )
                      ){
                    ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
                        function_name+"'");
                  }
                  else {
                    if (gentype1 != gentype) {
                      ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
                          function_name+"'");
                    }
                    else {
                      // Check the third parameter
                      if (!param_nb_elem.isIntegralScalar()) {
                        ce.raiseError(node,"wrong third parameter (num_gentypes) for builtin function '"+
                            function_name+"'");
                      }
                      else {
                        if (!param_event.isIntegralScalar()) {
                          ce.raiseError(node,"wrong fourth parameter (event) for builtin function '"+
                              function_name+"'");
                        }
                        else {
                          // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
                          function_symbol.setExternalBuiltinFunction();
                          function_type.setReturnType(IntegerScalar.Tsint);
                          return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  // ##################################################################
  // work-group 2D async copy builtin functions
  // ##################################################################

  private String checkAsyncWorkGroup2DCopyFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    if (checkNbParam(node,ce,function_name,function_type,6)) {
      // Right number of parameters
      Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
      Type param_pointer_source =function_type.getParameterType(1).unqualify();
      Type param_origin         =function_type.getParameterType(2).unqualify();
      Type param_region         =function_type.getParameterType(3).unqualify();
      Type param_nb_elem        =function_type.getParameterType(4).unqualify();
      Type param_event          =function_type.getParameterType(5).unqualify();

      // Check the first parameter
      Type qualifier=getPointedType(param_pointer_dest);
      if (qualifier==null) {
        ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
            function_name+"'");
      }
      else {
        if (!(qualifier instanceof Qualifier)) {
          ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
              function_name+"'");
        }
        else {
          AddressSpace as=((Qualifier)qualifier).getAddressSpace();
          Type gentype=((Qualifier)qualifier).getQualifiedType();
          if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.LOCAL)) {
            ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
                function_name+"'");
          }
          else {
            if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
                (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
                ) {
              ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
                  function_name+"'");
            }
            else {
              // Check the second parameter
              Type qualifier1=getPointedType(param_pointer_source);
              if (qualifier1==null) {
                ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
                    function_name+"'");
              }
              else {
                if (!(qualifier1 instanceof Qualifier)) {
                  ce.raiseError(node,"wrong address space for second parameter of builtin function '"+
                      function_name+"'");
                }
                else {
                  AddressSpace as1=((Qualifier)qualifier1).getAddressSpace();
                  Type gentype1=((Qualifier)qualifier1).getQualifiedType();
                  if (
                      ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.LOCAL)) ||
                      ( (as == AddressSpace.LOCAL) && ((as1 != AddressSpace.GLOBAL)&&(as1 != AddressSpace.CONSTANT)) )
                      ){
                    ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
                        function_name+"'");
                  }
                  else {
                    if (gentype1 != gentype) {
                      ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
                          function_name+"'");
                    }
                    else {
                      // Check the third parameter
                      if (!param_nb_elem.isIntegralScalar()) {
                        ce.raiseError(node,"wrong fifth parameter (num_gentypes) for builtin function '"+
                            function_name+"'");
                      }
                      else {
                        if (!param_event.isIntegralScalar()) {
                          ce.raiseError(node,"wrong sixth parameter (event) for builtin function '"+
                              function_name+"'");
                        }
                        else {
                          Type or=getPointedType(param_origin);
                          if ((or==null) || (!or.isIntegralScalar())) {
                            ce.raiseError(node,"wrong fourth parameter (origin) for builtin function '"+
                                function_name+"'");
                          }
                          else {
                            Type re=getPointedType(param_origin);
                            if ((re==null) || (!re.isIntegralScalar())) {
                              ce.raiseError(node,"wrong fifth parameter (region) for builtin function '"+
                                  function_name+"'");
                            }
                            else {
                              // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
                              function_symbol.setExternalBuiltinFunction();
                              function_type.setReturnType(IntegerScalar.Tsint);
                              return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  // ##################################################################
  // Extensions for explicit DMA node management
  // ##################################################################

  private String checkDmaWorkGroupCopyFunction(NodeAST node, CompilerError ce,
		  FunctionLabel function_symbol,
		  Function function_type) {
	  String function_name=function_symbol.getName();
	  if (checkNbParam(node,ce,function_name,function_type,4)) {
		  // Right number of parameters
		  Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
		  Type param_pointer_source =function_type.getParameterType(1).unqualify();
		  Type param_nb_elem        =function_type.getParameterType(2).unqualify();
		  Type param_req          =function_type.getParameterType(3).unqualify();

		  // Check the first parameter
		  Type qualifier=getPointedType(param_pointer_dest);
		  if (qualifier==null) {
			  ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
					  function_name+"'");
		  }
		  else {
			  if (!(qualifier instanceof Qualifier)) {
				  ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
						  function_name+"'");
			  }
			  else {
				  AddressSpace as=((Qualifier)qualifier).getAddressSpace();
				  Type gentype=((Qualifier)qualifier).getQualifiedType();
				  if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.LOCAL)) {
					  ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
							  function_name+"'");
				  }
				  else {
					  if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
							  (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
					  ) {
						  ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
								  function_name+"'");
					  }
					  else {
						  // Check the second parameter
						  Type qualifier1=getPointedType(param_pointer_source);
						  if (qualifier1==null) {
							  ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
									  function_name+"'");
						  }
						  else {
							  if (!(qualifier1 instanceof Qualifier)) {
								  ce.raiseError(node,"wrong address space for second parameter of builtin function '"+
										  function_name+"'");
							  }
							  else {
								  AddressSpace as1=((Qualifier)qualifier1).getAddressSpace();
								  Type gentype1=((Qualifier)qualifier1).getQualifiedType();
								  if (
										  ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.LOCAL)) ||
										  ( (as == AddressSpace.LOCAL) && ((as1 != AddressSpace.GLOBAL)&&(as1 != AddressSpace.CONSTANT)) )
								  ){
									  ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
											  function_name+"'");
								  }
								  else {
									  if (gentype1 != gentype) {
										  ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
												  function_name+"'");
									  }
									  else {
										  // Check the third parameter
										  if (!param_nb_elem.isIntegralScalar()) {
											  ce.raiseError(node,"wrong third parameter (num_gentypes) for builtin function '"+
													  function_name+"'");
										  }
										  else {
											  
											  Type qualifierReq=getPointedType(param_req);
											  if (qualifierReq==null) {
												  ce.raiseError(node,"fourth parameter must be a pointer for builtin function '"+
														  function_name+"'");
											  }
											  else {
												  AddressSpace asReq;
												  if (!(qualifierReq instanceof Qualifier)) {
													  asReq=AddressSpace.PRIVATE;
												  }
												  else {
													  asReq=((Qualifier)qualifierReq).getAddressSpace();
													  if (asReq==AddressSpace.NO) { asReq=AddressSpace.PRIVATE;}
												  }
											
												  if ((asReq != AddressSpace.LOCAL)) {
													  ce.raiseError(node,"wrong address space for fourth parameter of builtin function '"+
															  function_name+"'");
												  }
												  
												  // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
												  function_symbol.setExternalBuiltinFunction();
												  function_type.setReturnType(Void.Tvoid);
												  return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
											  }
										  }
									  }
								  }
							  }
						  }
					  }
				  }
			  }
		  }
	  }

	  // Error, but we set anyway the return type
	  function_type.setReturnType(Void.Tvoid);
	  return function_name;
  }

  private String checkDmaWorkGroup2DCopyFunction(NodeAST node, CompilerError ce,
		  FunctionLabel function_symbol,
		  Function function_type) {
	  String function_name=function_symbol.getName();
	  if (checkNbParam(node,ce,function_name,function_type,6)) {
		  // Right number of parameters
		  Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
		  Type param_pointer_source =function_type.getParameterType(1).unqualify();
		  Type param_origin         =function_type.getParameterType(2).unqualify();
		  Type param_region         =function_type.getParameterType(3).unqualify();
		  Type param_nb_elem        =function_type.getParameterType(4).unqualify();
		  Type param_req          =function_type.getParameterType(5).unqualify();

		  // Check the first parameter
		  Type qualifier=getPointedType(param_pointer_dest);
		  if (qualifier==null) {
			  ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
					  function_name+"'");
		  }
		  else {
			  if (!(qualifier instanceof Qualifier)) {
				  ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
						  function_name+"'");
			  }
			  else {
				  AddressSpace as=((Qualifier)qualifier).getAddressSpace();
				  Type gentype=((Qualifier)qualifier).getQualifiedType();
				  if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.LOCAL)) {
					  ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
							  function_name+"'");
				  }
				  else {
					  if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
							  (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
					  ) {
						  ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
								  function_name+"'");
					  }
					  else {
						  // Check the second parameter
						  Type qualifier1=getPointedType(param_pointer_source);
						  if (qualifier1==null) {
							  ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
									  function_name+"'");
						  }
						  else {
							  if (!(qualifier1 instanceof Qualifier)) {
								  ce.raiseError(node,"wrong address space for second parameter of builtin function '"+
										  function_name+"'");
							  }
							  else {
								  AddressSpace as1=((Qualifier)qualifier1).getAddressSpace();
								  Type gentype1=((Qualifier)qualifier1).getQualifiedType();
								  if (
										  ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.LOCAL)) ||
										  ( (as == AddressSpace.LOCAL) && ((as1 != AddressSpace.GLOBAL)&&(as1 != AddressSpace.CONSTANT)) )
								  ){
									  ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
											  function_name+"'");
								  }
								  else {
									  if (gentype1 != gentype) {
										  ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
												  function_name+"'");
									  }
									  else {
										  // Check the third parameter
										  if (!param_nb_elem.isIntegralScalar()) {
											  ce.raiseError(node,"wrong fifth parameter (num_gentypes) for builtin function '"+
													  function_name+"'");
										  }
										  else {
											  Type qualifierReq=getPointedType(param_req);
											  if (qualifierReq==null) {
												  ce.raiseError(node,"sixth parameter must be a pointer for builtin function '"+
														  function_name+"'");
											  }
											  else {
												  AddressSpace asReq;
												  if (!(qualifierReq instanceof Qualifier)) {
													  asReq=AddressSpace.PRIVATE;
												  }
												  else {
													  asReq=((Qualifier)qualifierReq).getAddressSpace();
													  if (asReq==AddressSpace.NO) { asReq=AddressSpace.PRIVATE;}
												  }

												  if ((asReq != AddressSpace.LOCAL)) {
													  ce.raiseError(node,"wrong address space for sixth parameter of builtin function '"+
															  function_name+"'");
												  }

												  else {
													  Type or=getPointedType(param_origin);
													  if ((or==null) || (!or.isIntegralScalar())) {
														  ce.raiseError(node,"wrong fourth parameter (origin) for builtin function '"+
																  function_name+"'");
													  }
													  else {
														  Type re=getPointedType(param_origin);
														  if ((re==null) || (!re.isIntegralScalar())) {
															  ce.raiseError(node,"wrong fifth parameter (region) for builtin function '"+
																	  function_name+"'");
														  }
														  else {
															  // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
															  function_symbol.setExternalBuiltinFunction();
															  function_type.setReturnType(Void.Tvoid);
															  return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
														  }
													  }
												  }
											  }
										  }
									  }
								  }
							  }
						  }
					  }
				  }
			  }
		  }
	  }

	  // Error, but we set anyway the return type
	  function_type.setReturnType(Void.Tvoid);
	  return function_name;
  }

  private String checkDmaWorkItemCopyFunction(NodeAST node, CompilerError ce,
		  FunctionLabel function_symbol,
		  Function function_type) {
	  String function_name=function_symbol.getName();
	  if (checkNbParam(node,ce,function_name,function_type,4)) {
		  // Right number of parameters
		  Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
		  Type param_pointer_source =function_type.getParameterType(1).unqualify();
		  Type param_nb_elem        =function_type.getParameterType(2).unqualify();
		  Type param_req          =function_type.getParameterType(3).unqualify();

		  // Check the first parameter
		  Type qualifier=getPointedType(param_pointer_dest);
		  if (qualifier==null) {
			  ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
					  function_name+"'");
		  }
		  else {
			  AddressSpace as;
			  Type gentype;
			  if (!(qualifier instanceof Qualifier)) {
				  as=AddressSpace.PRIVATE;
				  gentype=qualifier;
			  }
			  else {
				  as=((Qualifier)qualifier).getAddressSpace();
				  if (as==AddressSpace.NO) { as=AddressSpace.PRIVATE;}
				  gentype=((Qualifier)qualifier).getQualifiedType();
			  }

			  if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.PRIVATE) && (as != AddressSpace.LOCAL)) {
				  ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
						  function_name+"'");
			  }
			  else {
				  if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
						  (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
				  ) {
					  ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
							  function_name+"'");
				  }
				  else {
					  // Check the second parameter
					  Type qualifier1=getPointedType(param_pointer_source);
					  if (qualifier1==null) {
						  ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
								  function_name+"'");
					  }
					  else {
						  AddressSpace as1;
						  Type gentype1;
						  if (!(qualifier1 instanceof Qualifier)) {
							  as1=AddressSpace.PRIVATE;
							  gentype1=qualifier1;
						  }
						  else {
							  as1=((Qualifier)qualifier1).getAddressSpace();
							  if (as1==AddressSpace.NO) { as1=AddressSpace.PRIVATE;}
							  gentype1=((Qualifier)qualifier1).getQualifiedType();
						  }
						  if (  ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.PRIVATE) && (as1 != AddressSpace.LOCAL)) ||
								  ( ((as == AddressSpace.PRIVATE) || (as == AddressSpace.LOCAL)) && ((as1 != AddressSpace.GLOBAL) && (as1 != AddressSpace.CONSTANT)) )
						  ){
							  ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
									  function_name+"'");
						  }
						  else {
							  if (gentype1 != gentype) {
								  ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
										  function_name+"'");
							  }
							  else {
								  // Check the third parameter
								  if (!param_nb_elem.isIntegralScalar()) {
									  ce.raiseError(node,"wrong third parameter (num_gentypes) for builtin function '"+
											  function_name+"'");
								  }
								  else {
									  
									  Type qualifierReq=getPointedType(param_req);
									  if (qualifierReq==null) {
										  ce.raiseError(node,"fourth parameter must be a pointer for builtin function '"+
												  function_name+"'");
									  }
									  else {
										  AddressSpace asReq;
										  if (!(qualifierReq instanceof Qualifier)) {
											  asReq=AddressSpace.PRIVATE;
										  }
										  else {
											  asReq=((Qualifier)qualifierReq).getAddressSpace();
											  if (asReq==AddressSpace.NO) { asReq=AddressSpace.PRIVATE;}
										  }
										  // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
										  function_symbol.setExternalBuiltinFunction();
										  function_type.setReturnType(Void.Tvoid);
										  return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
									  }
								  }
							  }
						  }
					  }
				  }
			  }
		  }
	  }

	  // Error, but we set anyway the return type
	  function_type.setReturnType(Void.Tvoid);
	  return function_name;
  }

  private String checkDmaWorkItem2DCopyFunction(NodeAST node, CompilerError ce,
		  FunctionLabel function_symbol,
		  Function function_type) {
	  String function_name=function_symbol.getName();
	  if (checkNbParam(node,ce,function_name,function_type,6)) {
		  // Right number of parameters
		  Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
		  Type param_pointer_source =function_type.getParameterType(1).unqualify();
		  Type param_origin         =function_type.getParameterType(2).unqualify();
		  Type param_region         =function_type.getParameterType(3).unqualify();
		  Type param_nb_elem        =function_type.getParameterType(4).unqualify();
		  Type param_req          =function_type.getParameterType(5).unqualify();

		  // Check the first parameter
		  Type qualifier=getPointedType(param_pointer_dest);
		  if (qualifier==null) {
			  ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
					  function_name+"'");
		  }
		  else {
			  AddressSpace as;
			  Type gentype;
			  if (!(qualifier instanceof Qualifier)) {
				  as=AddressSpace.PRIVATE;
				  gentype=qualifier;
			  }
			  else {
				  as=((Qualifier)qualifier).getAddressSpace();
				  if (as==AddressSpace.NO) { as=AddressSpace.PRIVATE;}
				  gentype=((Qualifier)qualifier).getQualifiedType();
			  }

			  if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.PRIVATE) && (as != AddressSpace.LOCAL)) {
				  ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
						  function_name+"'");
			  }
			  else {
				  if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
						  (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
				  ) {
					  ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
							  function_name+"'");
				  }
				  else {
					  // Check the second parameter
					  Type qualifier1=getPointedType(param_pointer_source);
					  if (qualifier1==null) {
						  ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
								  function_name+"'");
					  }
					  else {
						  AddressSpace as1;
						  Type gentype1;
						  if (!(qualifier1 instanceof Qualifier)) {
							  as1=AddressSpace.PRIVATE;
							  gentype1=qualifier1;
						  }
						  else {
							  as1=((Qualifier)qualifier1).getAddressSpace();
							  if (as1==AddressSpace.NO) { as1=AddressSpace.PRIVATE;}
							  gentype1=((Qualifier)qualifier1).getQualifiedType();
						  }
						  if (  ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.PRIVATE) && (as1 != AddressSpace.LOCAL)) ||
								  ( ((as == AddressSpace.PRIVATE) || (as == AddressSpace.LOCAL)) && ((as1 != AddressSpace.GLOBAL) && (as1 != AddressSpace.CONSTANT)) )
						  ){
							  ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
									  function_name+"'");
						  }
						  else {
							  if (gentype1 != gentype) {
								  ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
										  function_name+"'");
							  }
							  else {
								  // Check the third parameter
								  if (!param_nb_elem.isIntegralScalar()) {
									  ce.raiseError(node,"wrong fifth parameter (num_gentypes) for builtin function '"+
											  function_name+"'");
								  }
								  else {
									  Type qualifierReq=getPointedType(param_req);
									  if (qualifierReq==null) {
										  ce.raiseError(node,"sixth parameter must be a pointer for builtin function '"+
												  function_name+"'");
									  }
									  else {
										  AddressSpace asReq;
										  if (!(qualifierReq instanceof Qualifier)) {
											  asReq=AddressSpace.PRIVATE;
										  }
										  else {
											  asReq=((Qualifier)qualifierReq).getAddressSpace();
											  if (asReq==AddressSpace.NO) { asReq=AddressSpace.PRIVATE;}
										  }

										  Type or=getPointedType(param_origin);
										  if ((or==null) || (!or.isIntegralScalar())) {
											  ce.raiseError(node,"wrong fourth parameter (origin) for builtin function '"+
													  function_name+"'");		    
										  }
										  else {
											  Type re=getPointedType(param_origin);
											  if ((re==null) || (!re.isIntegralScalar())) {
												  ce.raiseError(node,"wrong fifth parameter (region) for builtin function '"+
														  function_name+"'");		    
											  }
											  else {
												  // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
												  function_symbol.setExternalBuiltinFunction();
												  function_type.setReturnType(Void.Tvoid);
												  return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
											  }
										  }
									  }
								  }
							  }
						  }
					  }
				  }
			  }
		  }
	  }

	  // Error, but we set anyway the return type
	  function_type.setReturnType(Void.Tvoid);
	  return function_name;
  }


  // ##################################################################
  // work-group async strided copy builtin functions
  // ##################################################################

  private String checkAsyncWorkGroupStridedCopyFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    if (checkNbParam(node,ce,function_name,function_type,5)) {
      // Right number of parameters
      Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
      Type param_pointer_source =function_type.getParameterType(1).unqualify();
      Type param_nb_elem        =function_type.getParameterType(2).unqualify();
      Type param_stride         =function_type.getParameterType(3).unqualify();
      Type param_event          =function_type.getParameterType(4).unqualify();

      // Check the first parameter
      Type qualifier=getPointedType(param_pointer_dest);
      if (qualifier==null) {
        ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
            function_name+"'");
      }
      else {
        if (!(qualifier instanceof Qualifier)) {
          ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
              function_name+"'");
        }
        else {
          AddressSpace as=((Qualifier)qualifier).getAddressSpace();
          Type gentype=((Qualifier)qualifier).getQualifiedType();
          if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.LOCAL)) {
            ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
                function_name+"'");
          }
          else {
            if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
                (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
                ) {
              ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
                  function_name+"'");
            }
            else {
              // Check the second parameter
              Type qualifier1=getPointedType(param_pointer_source);
              if (qualifier1==null) {
                ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
                    function_name+"'");
              }
              else {
                if (!(qualifier1 instanceof Qualifier)) {
                  ce.raiseError(node,"wrong address space for second parameter of builtin function '"+
                      function_name+"'");
                }
                else {
                  AddressSpace as1=((Qualifier)qualifier1).getAddressSpace();
                  Type gentype1=((Qualifier)qualifier1).getQualifiedType();
                  if (
                      ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.LOCAL)) ||
                      ( (as == AddressSpace.LOCAL) && ((as1 != AddressSpace.GLOBAL)&&(as1 != AddressSpace.CONSTANT)) )
                      ){
                    ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
                        function_name+"'");
                  }
                  else {
                    if (gentype1 != gentype) {
                      ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
                          function_name+"'");
                    }
                    else {
                      // Check the third parameter
                      if (!param_nb_elem.isIntegralScalar()) {
                        ce.raiseError(node,"wrong third parameter (num_gentypes) for builtin function '"+
                            function_name+"'");
                      }
                      else {
                        if (!param_stride.isIntegralScalar()) {
                          ce.raiseError(node,"wrong fourth parameter (stride) for builtin function '"+
                              function_name+"'");
                        }
                        else {
                          if (!param_event.isIntegralScalar()) {
                            ce.raiseError(node,"wrong fifth parameter (event) for builtin function '"+
                                function_name+"'");
                          }
                          else {
                            // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
                            function_symbol.setExternalBuiltinFunction();
                            function_type.setReturnType(IntegerScalar.Tsint);
                            return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  // ##################################################################
  // prefetch builtin functions
  // ##################################################################

  private String checkPrefetchFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    if (checkNbParam(node,ce,function_name,function_type,2)) {
      // Right number of parameters
      Type param_pointer_dest   = function_type.getParameterType(0).unqualify();
      Type param_nb_elem        = function_type.getParameterType(1).unqualify();

      // Check the first parameter
      Type qualifier=getPointedType(param_pointer_dest);
      if (qualifier==null) {
        ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
            function_name+"'");
      }
      else {
        if (!(qualifier instanceof Qualifier)) {
          ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
              function_name+"'");
        }
        else {
          AddressSpace as=((Qualifier)qualifier).getAddressSpace();
          Type gentype=((Qualifier)qualifier).getQualifiedType();
          if ((as != AddressSpace.GLOBAL)) {
            ce.raiseError(node,"address space must be __global for the first parameter of builtin function '"+
                function_name+"'");
          }
          else {
            if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
                (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
                ) {
              ce.raiseError(node,"wrong gentype for the first parameter of builtin function '"+
                  function_name+"'");
            }
            else {
              // Check the third parameter
              if (!param_nb_elem.isIntegralScalar()) {
                ce.raiseError(node,"wrong second parameter (num_gentypes) for builtin function '"+
                    function_name+"'");
              }
              else {
                // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
                function_type.setReturnType(Void.Tvoid);
                return "__ocl_"+function_name+"_"+DumpAsType(gentype);
              }
            }
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(Void.Tvoid);
    return function_name;
  }



  // ##################################################################
  // work-item async copy builtin functions
  // ##################################################################

  private String checkAsyncWorkItemCopyFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    if (checkNbParam(node,ce,function_name,function_type,4)) {
      // Right number of parameters
      Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
      Type param_pointer_source =function_type.getParameterType(1).unqualify();
      Type param_nb_elem        =function_type.getParameterType(2).unqualify();
      Type param_event          =function_type.getParameterType(3).unqualify();

      // Check the first parameter
      Type qualifier=getPointedType(param_pointer_dest);
      if (qualifier==null) {
        ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
            function_name+"'");
      }
      else {
        AddressSpace as;
        Type gentype;
        if (!(qualifier instanceof Qualifier)) {
          as=AddressSpace.PRIVATE;
          gentype=qualifier;
        }
        else {
          as=((Qualifier)qualifier).getAddressSpace();
          if (as==AddressSpace.NO) { as=AddressSpace.PRIVATE;}
          gentype=((Qualifier)qualifier).getQualifiedType();
        }

        if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.PRIVATE) && (as != AddressSpace.LOCAL)) {
          ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
              function_name+"'");
        }
        else {
          if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
              (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
              ) {
            ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
                function_name+"'");
          }
          else {
            // Check the second parameter
            Type qualifier1=getPointedType(param_pointer_source);
            if (qualifier1==null) {
              ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
                  function_name+"'");
            }
            else {
              AddressSpace as1;
              Type gentype1;
              if (!(qualifier1 instanceof Qualifier)) {
                as1=AddressSpace.PRIVATE;
                gentype1=qualifier1;
              }
              else {
                as1=((Qualifier)qualifier1).getAddressSpace();
                if (as1==AddressSpace.NO) { as1=AddressSpace.PRIVATE;}
                gentype1=((Qualifier)qualifier1).getQualifiedType();
              }
              if (  ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.PRIVATE) && (as1 != AddressSpace.LOCAL)) ||
                  ( ((as == AddressSpace.PRIVATE) || (as == AddressSpace.LOCAL)) && ((as1 != AddressSpace.GLOBAL) && (as1 != AddressSpace.CONSTANT)) )
                  ){
                ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
                    function_name+"'");
              }
              else {
                if (gentype1 != gentype) {
                  ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
                      function_name+"'");
                }
                else {
                  // Check the third parameter
                  if (!param_nb_elem.isIntegralScalar()) {
                    ce.raiseError(node,"wrong third parameter (num_gentypes) for builtin function '"+
                        function_name+"'");
                  }
                  else {
                    if (!param_event.isIntegralScalar()) {
                      ce.raiseError(node,"wrong fourth parameter (event) for builtin function '"+
                          function_name+"'");
                    }
                    else {
                      // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
                      function_symbol.setExternalBuiltinFunction();
                      function_type.setReturnType(IntegerScalar.Tsint);
                      return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  // ##################################################################
  // work-item 2D async copy builtin functions
  // ##################################################################

  private String checkAsyncWorkItem2DCopyFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    if (checkNbParam(node,ce,function_name,function_type,6)) {
      // Right number of parameters
      Type param_pointer_dest   =function_type.getParameterType(0).unqualify();
      Type param_pointer_source =function_type.getParameterType(1).unqualify();
      Type param_origin         =function_type.getParameterType(2).unqualify();
      Type param_region         =function_type.getParameterType(3).unqualify();
      Type param_nb_elem        =function_type.getParameterType(4).unqualify();
      Type param_event          =function_type.getParameterType(5).unqualify();

      // Check the first parameter
      Type qualifier=getPointedType(param_pointer_dest);
      if (qualifier==null) {
        ce.raiseError(node,"first parameter must be a pointer for builtin function '"+
            function_name+"'");
      }
      else {
        AddressSpace as;
        Type gentype;
        if (!(qualifier instanceof Qualifier)) {
          as=AddressSpace.PRIVATE;
          gentype=qualifier;
        }
        else {
          as=((Qualifier)qualifier).getAddressSpace();
          if (as==AddressSpace.NO) { as=AddressSpace.PRIVATE;}
          gentype=((Qualifier)qualifier).getQualifiedType();
        }

        if ((as != AddressSpace.GLOBAL) && (as != AddressSpace.PRIVATE) && (as != AddressSpace.LOCAL)) {
          ce.raiseError(node,"wrong address space for first parameter of builtin function '"+
              function_name+"'");
        }
        else {
          if ( (!gentype.isIntegerScalar()) && (!gentype.isFloatScalar()) &&
              (!gentype.isIntegralVector()) && (!gentype.isFloatVector())
              ) {
            ce.raiseError(node,"wrong gentype for first parameter of builtin function '"+
                function_name+"'");
          }
          else {
            // Check the second parameter
            Type qualifier1=getPointedType(param_pointer_source);
            if (qualifier1==null) {
              ce.raiseError(node,"second parameter must be a pointer for builtin function '"+
                  function_name+"'");
            }
            else {
              AddressSpace as1;
              Type gentype1;
              if (!(qualifier1 instanceof Qualifier)) {
                as1=AddressSpace.PRIVATE;
                gentype1=qualifier1;
              }
              else {
                as1=((Qualifier)qualifier1).getAddressSpace();
                if (as1==AddressSpace.NO) { as1=AddressSpace.PRIVATE;}
                gentype1=((Qualifier)qualifier1).getQualifiedType();
              }
              if (  ((as == AddressSpace.GLOBAL) && (as1 != AddressSpace.PRIVATE) && (as1 != AddressSpace.LOCAL)) ||
                  ( ((as == AddressSpace.PRIVATE) || (as == AddressSpace.LOCAL)) && ((as1 != AddressSpace.GLOBAL) && (as1 != AddressSpace.CONSTANT)) )
                  ){
                ce.raiseError(node,"non coherent address spaces for first and second parameters of builtin function '"+
                    function_name+"'");
              }
              else {
                if (gentype1 != gentype) {
                  ce.raiseError(node,"non coherent gentype for first and second parameters of builtin function '"+
                      function_name+"'");
                }
                else {
                  // Check the third parameter
                  if (!param_nb_elem.isIntegralScalar()) {
                    ce.raiseError(node,"wrong fifth parameter (num_gentypes) for builtin function '"+
                        function_name+"'");
                  }
                  else {
                    if (!param_event.isIntegralScalar()) {
                      ce.raiseError(node,"wrong sixth parameter (event) for builtin function '"+
                          function_name+"'");
                    }
                    else {
                      Type or=getPointedType(param_origin);
                      if ((or==null) || (!or.isIntegralScalar())) {
                        ce.raiseError(node,"wrong fourth parameter (origin) for builtin function '"+
                            function_name+"'");		    
                      }
                      else {
                        Type re=getPointedType(param_origin);
                        if ((re==null) || (!re.isIntegralScalar())) {
                          ce.raiseError(node,"wrong fifth parameter (region) for builtin function '"+
                              function_name+"'");		    
                        }
                        else {
                          // Return 'async_work_group_copy_generic__<dest AS>' even in case of error
                          function_symbol.setExternalBuiltinFunction();
                          function_type.setReturnType(IntegerScalar.Tsint);
                          return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }



  // ##################################################################
  // 'vload' builtin functions
  // ##################################################################

  private String checkVloadFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    int nb_elem=Integer.parseInt(function_name.substring("vload".length()));
    if (checkNbParam(node,ce,function_name,function_type,2)) {
      // Right number of parameters
      Type param_offset=function_type.getParameterType(0).unqualify();
      Type param_pointer=function_type.getParameterType(1).unqualify();

      // Check first parameter
      if (!param_offset.isIntegralScalar()) {
        ce.raiseError(node,"first parameter must be integral for builtin function '"+
            function_name+"'");
      }
      else {
        // Check second parameter
        Type qualifier=getPointedType(param_pointer);

        Type gentype;
        AddressSpace as=AddressSpace.NO;
        if (qualifier instanceof Qualifier) {
          as=((Qualifier)qualifier).getAddressSpace();
          gentype=((Qualifier)qualifier).getQualifiedType();
        }
        else {
          as=AddressSpace.PRIVATE;
          gentype=qualifier;
        }      
        if (gentype.isIntegerScalar()) {
          // Return 'vload<n>_<type>_<as>'
          function_type.setReturnType(IntegerVector.getVectorType((IntegerScalar)gentype,nb_elem));
          return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
        }
        else if (gentype.isFloatScalar()) {
          // Return 'vload<n>_<type>_<as>'
          function_type.setReturnType(FloatingPointVector.getFloatType(nb_elem));
          return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
        }
        else {
          ce.raiseError(node,"wrong gentype for builtin function '"+
              function_name+"'");
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  // ##################################################################
  // 'vstore' builtin functions
  // ##################################################################

  private String checkVstoreFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    int nb_elem=Integer.parseInt(function_name.substring("vstore".length()));
    if (checkNbParam(node,ce,function_name,function_type,3)) {
      // Right number of parameters
      Type param_vector =function_type.getParameterType(0).unqualify();
      Type param_offset =function_type.getParameterType(1).unqualify();
      Type param_pointer=function_type.getParameterType(2).unqualify();


      // Check first parameter
      if (!param_vector.isVector() ){
        ce.raiseError(node,"first parameter of builtin function '"+
            function_name+"' must be a vector");
      }
      else {
        if (((Vector)param_vector).getNbElements()!=nb_elem) {
          ce.raiseError(node,"wrong vector size for first parameter of builtin function '"+
              function_name+"'");
        }
        else {
          // Check second parameter
          if (!param_offset.isIntegralScalar()) {
            ce.raiseError(node,"second parameter must be integral for builtin function '"+
                function_name+"'");
          }
          else {
            Type gentype_vector=((Vector)param_vector).getBaseType();

            // Check third parameter
            Type qualifier=getPointedType(param_pointer);

            Type gentype;
            AddressSpace as=AddressSpace.NO;
            if (qualifier instanceof Qualifier) {
              as=((Qualifier)qualifier).getAddressSpace();
              gentype=((Qualifier)qualifier).getQualifiedType();
            }
            else {
              as=AddressSpace.PRIVATE;
              gentype=qualifier;
            }      
            if (gentype == gentype_vector) {
              // Return 'vstore<n>_<type>_<as>' even in case of error
              function_type.setReturnType(Void.Tvoid);
              return "__ocl_"+function_name+"_"+DumpAsType(gentype)+"_"+getASName(as);
            }
            else {
              ce.raiseError(node,"wrong gentype for third parameter of builtin function '"+
                  function_name+"'");
            }
          }
        }
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(Void.Tvoid);
    return function_name;
  }


  // ##################################################################
  // Relational builtin functions
  // ##################################################################

  private String checkAllAnyFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();
    if (checkNbParam(node,ce,function_name,function_type,1)) {
      Type param_type=function_type.getParameterType(0);

      if (param_type.isSignedIntegerScalar() ||
          param_type.isSignedIntegerVector()) {
        // Accepted source type
        function_type.setReturnType(IntegerScalar.Tsint);
        return "__ocl_"+function_name+"_"+DumpAsType(param_type);
      }
      else {
        ce.raiseError(node,"wrong parameter type for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }


  private String checkSelectFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_type1=function_type.getParameterType(0).unqualify();
      Type param_type2=function_type.getParameterType(1).unqualify();
      Type param_type3=function_type.getParameterType(2).unqualify();

      if (param_type1.isFloatScalar()) {
        if ((param_type1!=param_type2) || (!param_type3.isIntScalar())) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1)+"_"+DumpAsType(param_type3);
        }
      }
      else if ( param_type1.isIntegerScalar() ) {
        if (  (param_type1!=param_type2)       || 
            (!param_type3.isIntegerScalar()) ||
            (((IntegerScalar)param_type1).getBaseType() != ((IntegerScalar)param_type3).getBaseType())
            ) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1)+"_"+DumpAsType(param_type3);
        }
      }
      else if (param_type1.isFloatVector()) {
        if ( (param_type1!=param_type2)		||
            (!param_type3.isIntVector())	||
            (param_type1.getNbVectorElements() != param_type3.getNbVectorElements())
            ) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1)+"_"+DumpAsType(param_type3);
        }
      }
      else if ( param_type1.isIntegralVector() ) {
        if (  (param_type1!=param_type2)        || 
            (!param_type3.isIntegralVector()) ||
            (param_type1.getNbVectorElements() != param_type3.getNbVectorElements()) ||
            (((IntegerVector)param_type1).getBaseType().getBaseType() != ((IntegerVector)param_type3).getBaseType().getBaseType())
            ) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(param_type1);
          return "__ocl_"+function_name+"_"+DumpAsType(param_type1)+"_"+DumpAsType(param_type3);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }



  // ##################################################################
  // Miscellaneous builtin functions
  // ##################################################################

  private String checkShuffleFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,2)) {
      Type param_x    = function_type.getParameterType(0).unqualify();
      Type param_mask = function_type.getParameterType(1).unqualify();

      if (param_x.isFloatVector()) {
        if ( !( param_mask.isIntegralVector() && (param_mask.getVectorBaseType()==IntegerScalar.Tuint ) )) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(((Vector)param_x).getEquivalentType(((Vector)param_mask).getNbElements()));
          return "__ocl_"+function_name+"_"+DumpAsType(param_x)+"_"+DumpAsType(param_mask);
        }
      }
      else if ( param_x.isIntegralVector() ) {
        if (  (!param_mask.isIntegralVector()) ||
            (param_mask.getVectorBaseType() != ((IntegerVector)param_x).getUnsignedVersion().getBaseType())
            ) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(((Vector)param_x).getEquivalentType(((Vector)param_mask).getNbElements()));
          return "__ocl_"+function_name+"_"+DumpAsType(param_x)+"_"+DumpAsType(param_mask);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }

  private String checkShuffle2Function(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type) {
    String function_name=function_symbol.getName();

    if (checkNbParam(node,ce,function_name,function_type,3)) {
      Type param_x   = function_type.getParameterType(0).unqualify();
      Type param_y   = function_type.getParameterType(1).unqualify();
      Type param_mask= function_type.getParameterType(2).unqualify();

      if (param_x.isFloatVector()) {
        if ( 
            (param_x!=param_y) ||
            ( !( param_mask.isIntegralVector() &&
                (param_mask.getVectorBaseType()==IntegerScalar.Tuint) 
                ))
            ) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(((Vector)param_x).getEquivalentType(((Vector)param_mask).getNbElements()));
          return "__ocl_"+function_name+"_"+DumpAsType(param_x)+"_"+DumpAsType(param_mask);
        }
      }
      else if ( param_x.isIntegralVector() ) {
        if (  
            (param_x!=param_y) ||
            (!param_mask.isIntegralVector()) ||
            (param_mask.getVectorBaseType() != ((IntegerVector)param_x).getUnsignedVersion().getBaseType())
            ) {
          ce.raiseError(node,"non coherent gentype between parameters of builtin function '"+
              function_name+"'");
        }
        else {
          function_type.setReturnType(((Vector)param_x).getEquivalentType(((Vector)param_mask).getNbElements()));
          return "__ocl_"+function_name+"_"+DumpAsType(param_x)+"_"+DumpAsType(param_mask);
        }
      }
      else {
        ce.raiseError(node,"wrong parameter for builtin function '"+
            function_name+"'");
      }
    }

    // Error, but we set anyway the return type
    function_type.setReturnType(IntegerScalar.Tsint);
    return function_name;
  }



  // ##################################################################
  // Generic builtins checks
  // ##################################################################

  Type getPointedType(Type p) {
    if (p.isPointer()) {
      return p.getPointedType();
    }
    else if (p.isArray()) {
      return p.getElementType();
    }
    return null;
  } 

  String getASName(AddressSpace as) {
    switch(as) {
    case PRIVATE:
      return "private";
    case LOCAL:
      return "local";
    case CONSTANT:
      return "constant";
    case GLOBAL:
      return "global";
    default:
      return "private";
    }
  }

  // It is either an arithmetic scalar or a vector
  String DumpAsType(Type t) {
    if (t.isArithmeticScalar()) {
      return ((ArithmeticScalar)t.unqualify()).dump();
    }
    else {
      return ((Vector)t.unqualify()).dump();
    }
  }
  

 

}
