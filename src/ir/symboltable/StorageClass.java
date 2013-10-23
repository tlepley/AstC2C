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

/* Storage class of a symbol */


package ir.symboltable;

import ir.base.NodeAST;

import common.CompilerError;


public class StorageClass {
  // Storage class
  private boolean typedef_storage_class  = false;
  private boolean static_storage_class   = false;
  private boolean extern_storage_class   = false;
  private boolean auto_storage_class     = false;
  private boolean register_storage_class = false;
  //-> C99, only for functions
  private boolean inline_storage_class   = false;


  //==================================================================
  // Setting
  //==================================================================

  // 'typedef' storage class
  public void setTypedef(NodeAST tn,CompilerError cp) {
    if (typedef_storage_class) {
      cp.raiseWarning(tn,"duplicate 'typedef'");
    }
    typedef_storage_class=true;
  }
  // 'extern' storage class
  public void setExtern(NodeAST tn,CompilerError cp) {
    if (extern_storage_class) {
      cp.raiseWarning(tn,"duplicate 'extern'");
    }
    extern_storage_class=true;
  }
  // 'static' storage class
  public void setStatic(NodeAST tn,CompilerError cp) {
    if (static_storage_class) {
      cp.raiseWarning(tn,"duplicate 'static'");
    }
    static_storage_class=true;
  }
  // 'register' storage class
  public void setRegister(NodeAST tn,CompilerError cp) {
    if (register_storage_class) {
      cp.raiseWarning(tn,"duplicate 'register'");
    }
    register_storage_class=true;
  }
  // 'auto' storage class
  public void setAuto(NodeAST tn,CompilerError cp) {
    if (auto_storage_class) {
      cp.raiseWarning(tn,"duplicate 'auto'");
    }
    auto_storage_class=true;
  }
  // 'inline' storage class
  public void setInline(NodeAST tn,CompilerError cp) {
    if (inline_storage_class) {
      cp.raiseWarning(tn,"duplicate 'inline'");
    }
    inline_storage_class=true;
  }


  //==================================================================
  // Query
  //==================================================================

  // 'typedef' type specifier
  public boolean isTypedef() {
    return(typedef_storage_class);
  }
  // Returns 'true' if the symbol is declared as 'extern'
  public boolean isExtern() {
    return(extern_storage_class);
  }
  // Returns 'true' if the symbol is declared as 'static'
  public boolean isStatic() {
    return(static_storage_class);
  }
  // Returns 'true' if the symbol is declared as 'auto'
  public boolean isAuto() {
    return(auto_storage_class);
  }
  // Returns 'true' if the symbol is declared as 'register'
  public boolean isRegister() {
    return(register_storage_class);
  }

  // Returns 'true' if the symbol is declared as 'inline'
  public boolean isInline() {
    return(inline_storage_class);
  }


  //==================================================================
  // Check
  //==================================================================

  // Returns 'true' multiple storage classes have been specified
  // Note: 'inline' is not considered as a storage class for this check
  public boolean isMultipleStorageClass() {
    Boolean error=false;

    if (typedef_storage_class) {
      if (extern_storage_class || static_storage_class ||
	  auto_storage_class || register_storage_class ) {
	return true;
      }
    }
    else if (extern_storage_class) {
      if ( static_storage_class ||
	   auto_storage_class || register_storage_class ) {
	return true;
      }
    }
    else if (static_storage_class) {
      if ( auto_storage_class || register_storage_class ) {
	return true;
      }
    }
    else if ( auto_storage_class && register_storage_class) {
      return true;
    }

    return false;
  }


}
