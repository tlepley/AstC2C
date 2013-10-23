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

/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
DESCRIPTION:
   Tree grammar which performs code transformation for the CLAM
   OpenCL C compiler 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

header {
package parser;
import ir.base.*;
import ir.symboltable.*;
import ir.symboltable.symbols.*;
import ir.types.*;
import ir.symboltable.ocl.KernelPrototype;

import common.CompilerError;
}

class OclRewriter extends GnuCTreeParser;
options {
  importVocab  = GNUC;
  buildAST     = false;
  ASTLabelType = "NodeAST";
}
{
  // Error object
  private CompilerError compilerError = new CompilerError();


  // Associate an external error module to the tree parser
  public OclRewriter(CompilerError cp) {
    compilerError = cp;
  }


  // ##################################################################
  // Public execution interface
  // ##################################################################

  // ******************************************************************
  // run
  // ******************************************************************
  public void run(AST tree) {
    try { translationUnit(tree); }
    catch (Exception e) { 
        System.err.println("Fatal error in OclEngine :\n"+e);
        e.printStackTrace();
        compilerError.exitWithError(1);
    }
    compilerError.exitIfError();
  }

}


//##################################################################
//                      Grammar entry point
//##################################################################


// Remove OpenCL qualifiers
oclAddressSpaceQualifier
        :
	(        g:"__global"
 		{ g.setText(""); }
	  |      c:"__constant"
 		{ c.setText(""); }
	  |      l:"__local"
 		{ l.setText(""); }
	  |      p:"__private"
		{ p.setText(""); }
 	)
        ;

oclFunctionQualifier
	:       k:"__kernel" ( attributeSpecifierList )?
		{ k.setText(""); }
        ;

primaryExpr
        :       ID
        |       n:IntegralNumber
		{
		  EnrichedType t=n.getDataType();
		  if (t.getType().isLongScalar()) {

		    if (Type.getTargetABI().getLongSize()==8) {
		      // Target device is a 64 bit machine (nothing to do)
		    }
		    else if ((Type.getTargetABI().getLongSize()==4)&&(Type.getTargetABI().getLonglongSize()==8)) {
		      // Target device is a 32 bit machine
		      // (long constants becomes long long constants)
		      String s=n.getText().toLowerCase();
		      if (s.endsWith("lu")) {
			n.setText(s.substring(0,s.length()-2)+"llu");
		      }
		      else {
			n.setText(s+"l");
		      }
		    }
		    else {
		      compilerError.raiseFatalError("Can not generate OpenCL 'long' type with the given target device");
		    }
		  }
		}
        |       FloatingPointNumber
        |       charConst
        |       stringConst
        |       #( NExpressionGroup expr )
        |       compoundStatementExpr
	|       vectorLiteral
        ;
