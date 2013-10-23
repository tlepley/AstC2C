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

/* Tree grammar which performs computes the kernel signature */

header {
package parser;
import ir.base.*;
import ir.symboltable.*;
import ir.symboltable.symbols.*;
import ir.types.*;
import ir.symboltable.ocl.KernelPrototype;

import common.CompilerError;
}

class KernelSignature extends GnuCTreeParser;
options {
  importVocab  = GNUC;
  buildAST     = false;
  ASTLabelType = "NodeAST";
}
{
  // Error object
  private CompilerError compilerError = new CompilerError();


  // Associate an external error module to the tree parser
  public KernelSignature(CompilerError cp) {
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
        System.err.println("Fatal error in ClamEngine :\n"+e);
        e.printStackTrace();
        compilerError.exitWithError(1);
    }
    compilerError.exitIfError();
  }

}


//##################################################################
//                      Grammar entry point
//##################################################################


functionDef
{
  NodeAST id_node;
}
	:   #( n:NFunctionDef
                ( functionDeclSpecifiers)? 
                id_node=declarator_funcdef
                (attributeSpecifierList)?
                (declaration)*
                compoundStatement
            )
        ;




declarator_funcdef returns [NodeAST id_node]
        {
	  boolean is_id  = false;
	  boolean first_encountered_type = true;
	  Symbol id_symbol=null;
	  KernelPrototype kernel_prototype = null;
	  id_node=null;
	}
        :   #( NDeclarator
               ( pointerGroup )?               

	       (attributeSpecifierList)?   
	       ( id:ID
		   {
		     id_node=id;
		     is_id   = true;
		     id_symbol = ((NodeAST)id_node).getDefinition();
		   }

		 | LPAREN
		   id_node=declarator_funcdef RPAREN
                )

                (   #( NParameterTypeList
		        {
			  FunctionLabel func_symb=(FunctionLabel)id_symbol;
			  if ((is_id)&&(first_encountered_type)&&(func_symb.isKernel())) {
			    kernel_prototype = new KernelPrototype();
			    func_symb.setKernelPrototype(kernel_prototype);
			  }
			}
                       (
		        parameterTypeList_kernel[kernel_prototype]
		        | (idList)?
		       )
                       RPAREN
		       )
                       {
			 kernel_prototype=null;
			 first_encountered_type=false;
		       }
		     | l:LBRACKET (expr)? RBRACKET
                )*
	       )
        ;


parameterTypeList_kernel[KernelPrototype kernel_prototype]
        {
	  NodeAST id_node;
	}
	: (
	   (
	       id_node=p:parameterDeclaration_kernel 
	       {
		 if (kernel_prototype!=null) {
		   kernel_prototype.addParameter(id_node,p);
		 }
	       }
	   )
	    ( COMMA | SEMI )?
           )+
          ( VARARGS )?
       ;


//------------------------------------------------------------------
// parameterDeclaration:
//
// Declaration of a parameters of:
//   - a function definition
//   - a function prototype declaration
//   - a pointer to a function
//------------------------------------------------------------------
parameterDeclaration_kernel
returns [NodeAST id_node]
        {
	  NodeAST id;
	  id_node=null;
	}
        :   #( NParameterDeclaration
	       declSpecifiers
	       ( 
		 id = declarator_funcdef
                 {
		   id_node=id;
		 }
		 | nonemptyAbstractDeclarator
	 	 )?
		( attributeSpecifierList )?
	       )
        ;




