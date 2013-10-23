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

import ir.base.NodeAST;
import ir.symboltable.symbols.FunctionLabel;
import ir.types.c.Function;
import common.CompilerError;

public abstract class BuiltinManager {

  abstract public boolean isBuiltinFunctionName(String function_name);
  abstract public String checkBuiltinFunction(NodeAST node, CompilerError ce,
      FunctionLabel function_symbol,
      Function function_type);

  
  public static BuiltinManager getFromName(String s) {
    Class<?> t=null;
    try {
      t=Class.forName("builtins."+s+"BuiltinManager");
    }
    catch(ClassNotFoundException e) {
      CompilerError.GLOBAL.raiseInternalError("Unknown language '"+s+"' for builtins");
    }
    
    Class<? extends BuiltinManager>  c=null;
    try {
       c = t.asSubclass(BuiltinManager.class);
    }
    catch(ClassCastException e) {
      CompilerError.GLOBAL.raiseInternalError("getFromName(\""+s+"\"), ClassCastException");
    }
       
    BuiltinManager bm=null;
    try {
      bm=c.newInstance();
    }
    catch(InstantiationException e) {
      CompilerError.GLOBAL.raiseInternalError("getFromName(\""+s+"\"), InstantiationException");
    }
    catch(IllegalAccessException e) {
      CompilerError.GLOBAL.raiseInternalError("getFromName(\""+s+"\"), IllegalAccessException");
    }
    return bm;
  }
 
  
  // ******************************************************************
  // checkNbParam :
  //
  // Returns 'true' if the number if parameters of the function type
  // corresponds to the one specified. In the negative, it raises an
  // error and returns 'false'
  // ******************************************************************
  protected boolean checkNbParam(NodeAST node, CompilerError ce,
      String func_name, Function f, int n) {
    if (f.getNbParameters()!=n) {
      ce.raiseError(node,"expecting "+n+" parameters for builtin function '"
          +func_name+"', got "+f.getNbParameters());
      return false;
    }
    return true;
  }

}
