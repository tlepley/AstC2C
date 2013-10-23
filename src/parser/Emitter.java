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

/* Regenerate the C code corresponding to an AST.
   This emitter manages preprocessing directives and symbol renaming */

package parser;

import ir.symboltable.symbols.*;

import java.io.PrintStream;

import ir.base.PreprocessorInfoChannel;
import ir.base.NodeAST;
import parser.GnuCEmitter;


public class Emitter extends GnuCEmitter {

  public Emitter(PrintStream ps) {
    super(ps);
  }
  
  public Emitter(PrintStream ps, PreprocessorInfoChannel preprocChannel) {
    super(ps,preprocChannel);
  }


  protected void print( NodeAST t ) {
    // Manage preprocessing directives
    moveToNode(t);

    if (!isSilentMode()) {
      // In case of symbol renaming
      Symbol symbol;
      boolean b=false;
      symbol=t.getDefinition();
      if (symbol!=null) {
	currentOutput.print( symbol.getOutputName() + " " );
	b=true;
      }
      symbol=t.getReference();
      if (symbol!=null) {
	if (b==false) {
	  currentOutput.print( symbol.getOutputName() + " " );
	  b=true;
	}
      }
      
      if(b==false) {
	String to_print=t.getText();
	if (to_print.length()!=0) {
	  currentOutput.print(to_print + " ");
	}
      }
    }
  }

}
