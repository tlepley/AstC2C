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

/* Kernel prototype typeb */


package ir.symboltable.ocl;

import java.util.LinkedList;

import ir.base.NodeAST;


public class KernelPrototype {
  //==================================================================
  // Private data
  //==================================================================
  private LinkedList<NodeAST> parameterAstList=null;
  private LinkedList<NodeAST> parameterIdList=null;

  //==================================================================
  // Constructor
  //==================================================================
  public KernelPrototype() {
    parameterAstList=new LinkedList<NodeAST>();
    parameterIdList=new LinkedList<NodeAST>();
  }


  //------------------------------------------------------------------
  // addParameter
  //
  // Adds a parameter to the function prototype
  //------------------------------------------------------------------
  public void addParameter(NodeAST id, NodeAST t) {
    parameterAstList.add(t);
    parameterIdList.add(id);
  }

  //------------------------------------------------------------------
  // getParameterList
  //
  // Returns list of parameter types
  //------------------------------------------------------------------
  public LinkedList<NodeAST> getParameterAstList() {
    return parameterAstList;
  }
  public LinkedList<NodeAST> getParameterIdList() {
    return parameterIdList;
  }

  //------------------------------------------------------------------
  // hasParameter
  //
  // Returns 'true' is the function has at least one parameter
  //------------------------------------------------------------------
  public boolean hasParameter() {
    if (parameterAstList.size()!=0) {
      return true;
    }
    return false;
  }


  public int getNbParameters() {
    return parameterAstList.size();
  }

  public NodeAST getParameterId(int i) {
    return parameterIdList.get(i);
  }

  public NodeAST getParameterAst(int i) {
    return parameterAstList.get(i);
  }

}
