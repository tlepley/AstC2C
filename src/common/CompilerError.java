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

/* Manager for errors of the compiler */

package common;

import java.io.File;

import ir.base.NodeAST;

public class CompilerError {
  // Global error manager
  static public final CompilerError GLOBAL = new CompilerError();

  // Verbose level
  private int verboseLevel=0;

  // Current processed filename
  private String fileName = null;

  // ##################################################################
  // Constructors
  // ################################################################## 

  public CompilerError() {
    fileName = null;
  }

  public CompilerError(int v) {
    verboseLevel = v;
    fileName     = null;
  }

  public CompilerError(String s) {
    fileName     = s;
  }

  public CompilerError(File f) {
    fileName     = f.getPath();
  }

  public CompilerError(int v, String s) {
    verboseLevel = v;
    fileName     = s;
  }

  public CompilerError(int v, File f) {
    verboseLevel = v;
    fileName     = f.getPath();
  }

  public void setVerboseLevel(int v) {
    verboseLevel=v;
  }

  // ##################################################################
  // Global
  // ##################################################################

  // maximum number of errors allowed
  private int nbMaxErrors = 5;
  // error counter
  private int nb_errors   = 0;


  // ******************************************************************
  // setNbMaxErrors :
  //
  // Sets the maximum number of errors allowed
  // ******************************************************************
  void setNbMaxErrors(int i) {
    nbMaxErrors=i;
  }

  // ******************************************************************
  // isAnError :
  //
  // Returns 'true' if at least one error has been raised.
  // ******************************************************************
  private boolean isAnError() {
    return(nb_errors>0);
  }

  // ******************************************************************
  // addAnError:
  //
  // Increment the counter error and exit if too much errors
  // ******************************************************************
  private void addAnError() {
    if (++nb_errors >= nbMaxErrors) {
      System.err.print("Too many errors, ");
      exitWithError();
    }
  }

  // ******************************************************************
  // exit :
  //
  // Display a message and stop the compilation process
  // ******************************************************************
  public void exitWithError() {
      System.err.println("stopping the compilation process");
      // Exit the program execution
      throw new CompilerExit(1);
  }
  public void exitWithError(int i) {
      System.err.println("stopping the compilation process");
      // Exit the program execution
      throw new CompilerExit(i);
  }
  public void exitNormally() {
    // Exit the program execution
    throw new CompilerExit(0);
}

  // ******************************************************************
  // exitIfError :
  //
  // Exits if some errors are pending
  // ******************************************************************
  public void exitIfError() {
    if (isAnError()) {
      exitWithError();
    }
  }



  // ##################################################################
  // General purpose errors
  // ##################################################################


  // ******************************************************************
  // printPrefix :
  //
  // Prints "<filename>" if filename is set, nothing otherwise
  // ******************************************************************
   private void printPrefix() {
    if (fileName!=null) {
      System.err.print(fileName + ":");
    }
  }
   private void printPrefix(String s) {
     System.err.print(s + ":");
  }


  // ******************************************************************
  // raiseMessage :
  //
  // Prints a warning message for AST node 'tn' (from which the line
  // number is taken).
  // ******************************************************************
   public void raiseMessage(String message) {
    if (verboseLevel>=0) {
      printPrefix();
      System.err.println(message);
    }
  }
   public void raiseMessage(int level, String message) {
    if (verboseLevel>=level) {
      printPrefix();
      System.err.println(message);
    }
  }

  // ******************************************************************
  // raiseWarning :
  //
  // Prints a warning message for AST node 'tn' (from which the line
  // number is taken).
  // ******************************************************************
   public void raiseWarning(String message) {
    if (verboseLevel>=0) {
      printPrefix();
      System.err.println("warning: " + message);
    }
  }  public void raiseWarning(int level, String message) {
    if (verboseLevel>=level) {
      printPrefix();
      System.err.println("warning: " + message);
    }
  }

  // ******************************************************************
  // raiseError :
  //
  // Prints an error message for AST node 'tn' (from which the line
  // number is taken). Exit after 5 errors.
  // ******************************************************************
   public void raiseError(String message) {
    printPrefix();
    System.err.println("error: " + message);
    addAnError();
  }

  // ******************************************************************
  // raiseFatalError :
  //
  // Prints an error message and exit directly
  // ******************************************************************
   public void raiseFatalError(String message) {
    printPrefix();
    System.err.println("fatal error: " + message);
    exitWithError();
  }

  // ******************************************************************
  // raiseInternalError :
  //
  // Prints an error message and exit directly
  // ******************************************************************
   public void raiseInternalError(String message) {
    printPrefix();
    System.err.println("internal error: " + message);
    exitWithError();
  }




  // ##################################################################
  // Parsing errors
  // ##################################################################



  // ******************************************************************
  // printPrefix :
  //
  // Prints "<filename>:<line num>: " where the line number has been
  // extracted from the TNode if the filename is set or "<line num>: "
  // otherwise
  // ******************************************************************
  private void printPrefix(NodeAST tn) {
    String s;
    s=(String)tn.getSource();
    if (s==null) {
      printPrefix();
    }
    else {
      printPrefix(s);
    }
    System.err.print(tn.getLineNum() + ": ");
  }


  // ******************************************************************
  // raiseMessage :
  //
  // Prints a warning message for AST node 'tn' (from which the line
  // number is taken).
  // ******************************************************************
  public void raiseMessage(NodeAST tn, String message) {
    if (verboseLevel>=0) {
      printPrefix(tn);
      System.err.println(message);
    }
  }
  public void raiseMessage(int level, NodeAST tn, String message) {
    if (verboseLevel>=level) {
      printPrefix(tn);
      System.err.println(message);
    }
  }

  // ******************************************************************
  // raiseWarning :
  //
  // Prints a warning message for AST node 'tn' (from which the line
  // number is taken).
  // ******************************************************************
  public void raiseWarning(NodeAST tn, String message) {
    if (verboseLevel>=0) {
      printPrefix(tn);
      System.err.println("warning: " + message);
    }
  }
  public void raiseWarning(int level, NodeAST tn, String message) {
    if (verboseLevel>=level) {
      printPrefix(tn);
      System.err.println("warning: " + message);
    }
  }


  // ******************************************************************
  // raiseError :
  //
  // Prints an error message for AST node 'tn' (from which the line
  // number is taken). Exit after 5 errors.
  // ******************************************************************
  public void raiseError(NodeAST tn, String message) {
    printPrefix(tn);
    System.err.println("error: " + message);
    addAnError();
  }


  // ******************************************************************
  // raiseFatalError :
  //
  // Prints an error message and exit directly
  // ******************************************************************
  public void raiseFatalError(NodeAST tn, String message) {
    printPrefix(tn);
    System.err.println("fatal error: " + message);
    exitWithError();
  }

  // ******************************************************************
  // raiseInternalError :
  //
  // Prints an error message and exit directly
  // ******************************************************************
  public void raiseInternalError(NodeAST tn, String message) {
    printPrefix(tn);
    System.err.println("internal error: " + message);
    exitWithError();
  }

}
