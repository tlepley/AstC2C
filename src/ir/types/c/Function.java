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

/* C Function prototype type */

package ir.types.c;

import ir.types.Type;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.HashMap;


public class Function extends ChildType {
  //==================================================================
  // Private data
  //==================================================================
  private LinkedList<Type> parameterTypeList=null;
  private boolean voidParameterList = false;
  private boolean vararg = false;

  // OCL
  private boolean is_kernel=false;


  //==================================================================
  // Constructor
  //==================================================================
  public Function() {
    parameterTypeList=new LinkedList<Type>();
  }


  //==================================================================
  // Type management
  //==================================================================

  public boolean isFunction() {return true;}


  //==================================================================
  // Setters
  //==================================================================

  //------------------------------------------------------------------
  // setReturnType
  //
  // Sets the type returned by the function prototype
  //------------------------------------------------------------------
  public void setReturnType(Type t) {
    setChild(t);
  }

  //------------------------------------------------------------------
  // addParameter
  //
  // Adds a parameter to the function prototype
  //------------------------------------------------------------------
  public void addParameter(Type t) {
    parameterTypeList.add(t);
  }


  //------------------------------------------------------------------
  // addVoidSpecifier
  //
  // Adds a special 'void' parameter
  //------------------------------------------------------------------
  public boolean addVoidSpecifier() {
    if ( voidParameterList || (parameterTypeList.size()!=0) ) {
      // It's an error, the void specifier must be alone in the parameter list
      return false;
    }
    voidParameterList=true;
    return true;
  }

  //------------------------------------------------------------------
  // addVararg
  //
  // Adds a special 'vararg' parameter
  //------------------------------------------------------------------
  public boolean addVararg() {
    if ( voidParameterList || (hasVararg()) ) {
      // It's an error, the void specifier must be alone in the parameter list
      return false;
    }
    vararg=true;
    return true;
  }


  //==================================================================
  // OCL
  //==================================================================
  public void setKernel() {
     is_kernel=true;
  }


  //==================================================================
  // Getters
  //==================================================================

  //------------------------------------------------------------------
  // getReturnType
  //
  // Returns type of the returned value
  //------------------------------------------------------------------
  public Type getReturnType() {
    return getChild();
  }

  //------------------------------------------------------------------
  // getParameterList
  //
  // Returns list of parameter types
  //------------------------------------------------------------------
  public LinkedList<Type> getParameterTypeList() {
    return parameterTypeList;
  }

  //------------------------------------------------------------------
  // hasParameter
  //
  // Returns 'true' is the function has at least one parameter
  //------------------------------------------------------------------
  public boolean hasParameter() {
    if (parameterTypeList.size()!=0) {
      return true;
    }
    return false;
  }

  public int getNbParameters() {
    return parameterTypeList.size();
  }

  public Type getParameterType(int i) {
    return parameterTypeList.get(i);
  }

  //------------------------------------------------------------------
  // isVoidParameterList
  //
  // Returns 'true' is the function has an explicit void parameter
  // list
  //------------------------------------------------------------------
  public boolean isVoidParameterList() {
    return voidParameterList;
  }

  public boolean hasVararg() {
    return vararg;
  }


  //==================================================================
  // Signature management (for arguments of function prototypes)
  //==================================================================

  //------------------------------------------------------------------
  // getParameterSignature
  //
  // Returns the signature for the ith parameter of the function
  //------------------------------------------------------------------
  public String getParameterSignature(int i) {
    return getParameterType(i).unqualify().getSignature();
  }


  //------------------------------------------------------------------
  // getSignatureForMangling
  //
  // Returns the top level signature for the function
  // This signature is used for function mangling
  //------------------------------------------------------------------
  public String getSignatureForMangling() {
    String s="";

    // Manage parameters
    for(Type t:parameterTypeList) {
      // Parameters are passed by value, so that
      // the address space qualifier is not relevant
      // in the signature of parameters
      s=s+t.unqualify().getSignature();
    }

    // Epilogue
    return s;
  }

  //------------------------------------------------------------------
  // getSignature
  //
  // Returns the standard signature for the function
  //------------------------------------------------------------------
  public String getSignature() {
    // Prologue
    String s= "F" + getReturnType().getSignature();

    // Note: a function definition without parameter has necessarily a
    // 'void parameter list' in its type. If the type has no parameter,
    // it means that it is a a function protoype which is compatible with
    // all other prototypes
    if (!hasParameter()) {
      if (isVoidParameterList()) {
	s=s+"v";
      }
      else {
	s=s+"*";
      }
    }
    else {
      // Get the signature for all parameters
      for(Type t:parameterTypeList) {
	// Parameters are passed by value, so that
	// the address space qualifier is not relevant
	// in the signature of parameters
	s=s+t.unqualify().getSignature();
      }
    }

    // Epilogue
    return s+"E";
  }



  //==================================================================
  // OCL specific
  //==================================================================

  public boolean isKernel() {
    return(is_kernel);
  }


  //==================================================================
  // Target Specific information
  //==================================================================

  public int sizeof() {
    return Type.getSourceABI().getPointerSize();
  }

  public int alignof() {
    return Type.getSourceABI().getPointerAlignment();
  }



  //==================================================================
  // Compatibility checks
  //==================================================================
  
  //------------------------------------------------------------------
  // isEquivalentForVariableAndArrayDeclaration :
  //
  // This function checks the compatibility of types in the context
  // of two variable/array declarations
  //------------------------------------------------------------------
  public boolean isEquivalentForVariableAndArrayDeclaration(Type t) {
    return isEquivalentForFunctionDeclaration(t);
  }


  //------------------------------------------------------------------
  // isEquivalentForFunctionDeclaration :
  //
  // This function checks the compatibility of types in the context
  // of two function prototype declarations
  //------------------------------------------------------------------
  public boolean isEquivalentForFunctionDeclaration(Type t) {
    // Check for function type
    if (!t.isFunction()) {
      return false;
    }

    return isEquivalentForFunctionMangling(t);
  }


  //------------------------------------------------------------------
  // isEquivalentForFunctionMangling :
  //
  // This function checks the compatibility of types in the context
  // of two function mangling (the return value does not discriminate)
  //------------------------------------------------------------------
  public boolean isEquivalentForFunctionMangling(Type t) {
    // Check for function type
    if (!t.isFunction()) {
      return false;
    }

    Function fp = (Function)t;

    // Check parameter list
    if ( (!hasParameter()) && (!isVoidParameterList())) {
      // It is necessarilty a function prototype which is compatible
      // with any parameter list
      return true;
    }
    if ( (!fp.hasParameter()) && (!fp.isVoidParameterList())) {
      // It is necessarilty a function prototype which is compatible
      // with any parameter list
      return true;
    }

    // Here, both have a well defined parameter list
    if (isVoidParameterList()) {
      if (fp.isVoidParameterList()) {
	return true;
      }
      return false;
    }

    // Check that both prototypes have the same number of parameters
    if (getNbParameters()!=fp.getNbParameters()) {
      return false;
    }

    // Check parameters one by one
    int i;
    for (i=0;i<getNbParameters();i++) {
      if (!getParameterType(i).isEquivalentForFunctionDeclaration(fp.getParameterType(i))) {
	return false;
      }
    }

    // Vararg property must be the same
    if ((hasVararg() && (!fp.hasVararg())) || ((!hasVararg()) && fp.hasVararg())) {
      return false;
    }

    return true;
  }



  //==================================================================
  // Verbose functions
  //==================================================================

  //------------------------------------------------------------------
  // getTreeSet:
  //
  // Function allows detecting multiple type reference in the type
  // tree. It is in particular useful to manage the possible
  // struct/union cycles. 'ts' holds type node encountered and 'cs'
  // holds multiply encountered type nodes.
  //------------------------------------------------------------------
  public void getTreeSet(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    // Manage the current type and the return value
    super.getTreeSet(ts,cs); // ChildType

    // Manage parameters
    for(Type t:parameterTypeList) {
      t.getTreeSet(ts, cs);
    }
  }


  //------------------------------------------------------------------
  // toStringInternal:
  //
  // Returns string a textual representation of the type. Use 'ts' and
  // 'cs' to avoid displaying multiple times the same type
  // (and avoid cycles)
  //------------------------------------------------------------------
  public String toStringInternal(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    boolean start=true;
    StringBuffer buff = new StringBuffer();

    if (isKernel()) {
      buff.append("kernel(");
    }
    else {
      buff.append("function(");
    }
    if (voidParameterList) {
      buff.append("void");
    }
    else {
      for(Type t:parameterTypeList) {
	if (start==false) {
	  buff.append(",");
	}
	start=false;
	buff.append(t.toStringInternal(ts,cs));
      }
    }
    buff.append(") returning {").append(getReturnType().toStringInternal(ts,cs)).append("}");

    return(buff.toString());
  }

}
