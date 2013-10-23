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

/* Function label symbol */


package ir.symboltable.symbols;

import ir.symboltable.*;
import ir.types.*;
import ir.types.c.Function;
import ir.types.c.IntegerScalar;
import ir.symboltable.ocl.KernelPrototype;



public class FunctionLabel extends Symbol implements CommonFunctionLabelInterface {

  //------------------------------
  // Private data
  //------------------------------

  // Function prototype or definition
  boolean is_function_definition = false;

  // Builtin information
  private boolean is_compiler_builtin_function = false;
  private boolean is_external_builtin_function = false;

  // Symbol to be mangled
  private boolean is_mangled_function = false;

  //------------------------------
  // OpenCL data
  //------------------------------
  private boolean is_kernel=false;
  private KernelPrototype kernel_prototype=null;
  // Attributes
  Type vec_type_hint=IntegerScalar.Tsint;
  int[] work_group_size_hint = new int[]{-1,-1,-1};
  int[] reqd_work_group_size = new int[]{-1,-1,-1};
  // Stack size management
  long stack_size=-1;



  //==================================================================
  // Constructors
  //==================================================================

  public FunctionLabel() {
    super();
  }

  public FunctionLabel(StorageClass sc) {
    super(sc);
  }

  public FunctionLabel(String name) {
    super(name);
  }

  public FunctionLabel(Symbol symb) {
    super(symb);
  }

  public FunctionLabel(FunctionLabel symb) {

    is_kernel		 = symb.is_kernel;

    // Builtin information
    is_compiler_builtin_function = symb.is_compiler_builtin_function;
    is_external_builtin_function = symb.is_external_builtin_function;

    // Mangling information
    is_mangled_function	 = symb.is_mangled_function;
  }


  //==================================================================
  // Def/proto management
  //==================================================================

  // Setting
  public void setDefinition() {
     is_function_definition=true;
  }
  public void setPrototype() {
     is_function_definition=false;
  }

  // Query
  public boolean isDefinition() {
    return is_function_definition;
  }
  public boolean isPrototype() {
    return !is_function_definition;
  }

  //------------------------------------------------------------------
  // isCompileTimeKnown
  //
  // The address of a function definition/prototype is always known
  // 
  // whose address is compile time known
  // Note: concerns only data objects and functions
  //------------------------------------------------------------------
  public boolean referencesCompileTimeAllocatedEntity() {
    return true;
  }


  //==================================================================
  // Symbol regeneration
  //==================================================================

  //------------------------------------------------------------------
  // getOutputName
  //
  // Returns the symbol name for the output source file. It performs
  // mangling if necessary
  //------------------------------------------------------------------
  public String getOutputName() {
    String output=getName();
    if (isMangledFunction()) {
      // Only functions are mangled
      return "_Z"+output.length()+output+((Function)getType()).getSignatureForMangling();
    }
    else {
      return output;
    }
  }

  //==================================================================
  // Builtin management
  //==================================================================

  // Setting
  public void setCompilerBuiltinFunction() {
     is_compiler_builtin_function=true;
  }
  public void setExternalBuiltinFunction() {
     is_external_builtin_function=true;
  }

  // Query
  public boolean isCompilerBuiltinFunction() {
    return is_compiler_builtin_function;
  }
  public boolean isExternalBuiltinFunction() {
    return is_external_builtin_function;
  }


  //==================================================================
  // Mangling management
  //==================================================================

  // Set a symbol as mangled
  public void setMangledFunction() {
     is_mangled_function=true;
  }

  // Query if the symbol is mangled
  public boolean isMangledFunction() {
    return is_mangled_function;
  }

  //==================================================================
  // Kernel management: OCL specific
  //==================================================================

  // Setting
  public void setKernel() {
     is_kernel=true;
  }
  public void setKernelPrototype(KernelPrototype kp) {
    kernel_prototype=kp;
  }

  // Query
  public boolean isKernel() {
    return is_kernel;
  }
  public KernelPrototype getKernelPrototype() {
    return kernel_prototype;
  }


  // Attribute setters
  public void setAttribute_vec_type_hint(Type t) {
    vec_type_hint=t;
  }
  public void setAttribute_work_group_size_hint(int d0, int d1, int d2) {
    work_group_size_hint[0]=d0;
    work_group_size_hint[1]=d1;
    work_group_size_hint[2]=d2;
  }
  public void setAttribute_reqd_work_group_size(int d0, int d1, int d2) {
    reqd_work_group_size[0]=d0;
    reqd_work_group_size[1]=d1;
    reqd_work_group_size[2]=d2;
  }
//   public void setAttribute_reqd_max_work_group_elements(int n) {
//       reqd_max_work_group_elements=n;
//   }
  public void setAttribute_stack_size(long s) {
      stack_size=s;
  }

  // Attribute getters
  public Type getAttribute_vec_type_hint() {
    return vec_type_hint;
  }
  public int getAttribute_work_group_size_hint(int dim) {
    return work_group_size_hint[dim];
  }
  public int getAttribute_reqd_work_group_size(int dim) {
    return reqd_work_group_size[dim];
  }
//   public long getAttribute_reqd_max_work_group_elements() {
//       return reqd_max_work_group_elements;
//   }
  public long getAttribute_stack_size() {
      return stack_size;
  }


  //==================================================================
  // Verbosing
  //==================================================================


  //------------------------------------------------------------------
  // getMessageName:
  //
  // Return the symbol reference name as i should appear in a message
  // or error
  //------------------------------------------------------------------
  public String getMessageName() {
    return "function '"+ getName() +"'";
  }

  //------------------------------------------------------------------
  // toString:
  //
  // Dump the symbol to a string
  //------------------------------------------------------------------
  public String toString() {
    StringBuffer buff = new StringBuffer();

    buff.append(super.toString());

    // Standard symbol
    if (isCompilerBuiltinFunction()) {
      buff.append("[builtin]");
    }
    if (isExternalBuiltinFunction()) {
      buff.append("[ext builtin]");
    }
    if (isMangledFunction()) {
      buff.append("[mangled]");
    }

    // Return the final string
    return buff.toString();
  }
}
