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

/* ******************************************************************
DESCRIPTION:
         This treeparser adds the 'this' pointer as first parameter
         of function pointers, of function prototypes internal to the
	 program (link unit) and to function definitions.
	 Then, All pointers to function are considered to point on a
	 function internal to the program.
****************************************************************** */

header {
package parser;
import ir.base.NodeAST;
import ir.symboltable.symbols.FunctionLabel;
import ir.symboltable.symbols.Symbol;

import common.CompilerError;
}


class ThisRewriter extends GnuCTreeParser;

options {
  importVocab  = GNUC;
  buildAST     = false;
  ASTLabelType = "NodeAST";
}
{
  // Error object
  CompilerError compilerError = new CompilerError();

  // builtin management
  boolean this_for_external_builtins=false;

  // Associate an external error module to the tree parser
  public ThisRewriter(CompilerError cp) {
    compilerError = cp;
  }

  private boolean shouldAddThis(Symbol symbol) {
    if (symbol.isProgramInternal()) {
      return true;
    }

    if ( this_for_external_builtins && 
	(symbol instanceof FunctionLabel) &&
	((FunctionLabel)symbol).isExternalBuiltinFunction()
       ) {
      return true;
    }

    return false;
  }

  // ##################################################################
  // Public execution interface
  // ##################################################################

  // ******************************************************************
  // run :
  //
  // Runs the tree parser, split declarations, builds a symbol table,
  // and annotate the AST with references to the symbol table
  // ******************************************************************
  public void run(AST tree) {
    try { translationUnit(tree); }
    catch (Exception e) { 
        System.err.println("Fatal error in ThisRewriter :\n"+e);
        e.printStackTrace();
        compilerError.exitWithError(1);
    }

    compilerError.exitIfError();
  }

  public void run(AST tree, boolean external_builtin) {
    this_for_external_builtins=external_builtin;
    try { translationUnit(tree); }
    catch (Exception e) {
        System.err.println("Fatal error in ThisRewriter :\n"+e);
        e.printStackTrace();
        compilerError.exitWithError(1);
    }

    compilerError.exitIfError();
  }


  // ##################################################################
  // AST building function for the '_this' argument 
  // ##################################################################

  private String thisName = "_this";
  private String thisType = "void";

  // ******************************************************************
  // setThis :
  //
  // Set the name and the type og 'this'
  // ******************************************************************
  public void setThis(String type, String name) {
    thisName=name;
    thisType=type;
  }

  // Standard parameter list style
  private NodeAST createThisParameterDefinition() {
    return (
	#( #[NParameterDeclaration],
	     #[LITERAL_void, thisType],
	     #( #[NDeclarator],
		  #( #[NPointerGroup],
		       #[STAR, "*"]
		   ),
		  #[ID, thisName]
	      )
	  )
	);
  }
  // K&R Style
  private NodeAST createKnRThisParameter() {
    return( #[ID, thisName] );
  }
  private NodeAST createKnRThisParameterDefinition() {
    return( 
	     #( #[NDeclaration],
		  #[LITERAL_void, thisType],
		  #( #[NInitDecl],
		     #( #[NDeclarator],
			  #( #[NPointerGroup],
	      		     #[STAR, "*"]
			   ),
	      		 #[ID, thisName]
		       )
		   )
		)
	     );
  }

  // Argument in function calls
  private NodeAST createThisArgumentCall() {
    return(#[ID, thisName]);
  }

}


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//
// Management of parameter lists in symbol definition:
//   - pointer to function
//   - function prototype
//   - function definition
//
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%



structDeclarator
        :
        #( NStructDeclarator      
            ( declarator_this[false] )?
            ( COLON expr )?
        )
        ;

initDecl
	{ String declName = ""; }
        :       #( NInitDecl
                declarator_this[false]
		(attributeSpecifierList)?
                ( ASSIGN initializer )?
                )
        ;

parameterDeclaration
        :       #( NParameterDeclaration
                declSpecifiers
                (declarator_this[false] | nonemptyAbstractDeclarator)?
		( attributeSpecifierList )?
                )
        ;


//------------------------------------------------------------------
// declarator_this:
//
// the declarator corresponds to the part where the symbol is
// defined and where parameter lists are defined for function
// pointers/proto/definitions
//------------------------------------------------------------------
declarator_this[boolean function_def]
	{
	  Symbol symbol = null;
	  boolean function_def_parameter_list=false;
	  NodeAST id_node = null;
	  int putThis=0;
	}
        :   #( NDeclarator
                ( pointerGroup )?               
		(attributeSpecifierList)?		   

                ( 
		   id:ID 
		      {
			if (function_def) {
			  function_def_parameter_list=true;
			}
			// It is a definition in a declarator
			symbol=id.getDefinition();
		      }
                 | LPAREN
		   declarator_this[function_def] RPAREN
                )

	        (   #( p:NParameterTypeList
		       {putThis=0;}
                       (
			 (RPAREN) => putThis=noParameter_this[symbol,function_def_parameter_list]
			| (voidParameterTypeList_this[symbol] RPAREN)=>
		          putThis=voidParameterTypeList_this[symbol]
			| putThis=parameterTypeList_this[symbol]
			| putThis=idList_this[symbol]
		       )
		       r:RPAREN
                     )
		    {
		      if (putThis==1) {
			NodeAST the_this = createThisParameterDefinition();
			the_this.setNextSibling(p.getFirstChild());
			p.setFirstChild(the_this);
		      }
		      else if (putThis==2) {
			NodeAST the_this = createThisParameterDefinition();
			the_this.setNextSibling(r);
		        p.setFirstChild(the_this);
		      }
		      else if (putThis==3) {
			NodeAST the_this = createThisParameterDefinition();
			NodeAST comma = #[COMMA, ","];
			the_this.setNextSibling(comma);
			comma.setNextSibling(p.getFirstChild());
                        p.setFirstChild(the_this);			
		      }
		      else if (putThis==4) {
			// Manage the 'this' pointer
			NodeAST this_param  = createKnRThisParameter();
			NodeAST comma       = #[COMMA, ","];
			this_param.setNextSibling(comma);
			comma.setNextSibling(p.getFirstChild());
                        p.setFirstChild(this_param);			
		      }
		    }
                 | LBRACKET ( expr )? RBRACKET
                )*
             )
        ;


//=====================================================================
// Note regarding parameters
//==========================
//
// 'symbol==false' with an ID should only occur for fields of
// struct/union (which are not managed by the symbol table). Since
// fields of structures can not be functions, it should never happen
// false never happen in function parameter rules (noParameter_this,
// parameterTypeList_this and idList_this). In These rules, we then
// consider that 'symbol==null' corresponds to pointer to functions.
//
// We then consider that the following case can occur in the parameter
// list rules:
// + Parameter list of a pointer to function
//   int (f())(*here*)
//   int (*f)(*here*)
//   -> symbol==null, function_def_parameter_list==false
// + Parameter list of function prototype
//   int f(*here*)
//   -> symbol!=null, function_def_parameter_list==false
// + Parameter list of function definition
//   int f(*here*)
//   -> symbol!=null, function_def_parameter_list==true
//=====================================================================


//------------------------------------------------------------------
// noParameter_this:
//
// Manage empty parameter lists. 'void *_this' is added at the
// beginning of the parameter list for pointer to functions and
// program internal function protos/definitions.
//
// Note: In C, an empty parameter list is compatible with all
//       all function definitions. Then, to avoid non-compatibility
//	 'this' is only added to the parameter list of function
//	 definitions
//------------------------------------------------------------------
noParameter_this[Symbol symbol, boolean function_def]
returns [int putThis]
{
  putThis=0;
}
	:
	{
	  if (false) {
	    // Just to please ANTLR
	    throw new RecognitionException();
	  }
	  if (function_def) {
	    // Add '_this' when empty parameter only for function definitions 
	    if ((symbol==null)||(shouldAddThis(symbol))) {
	      // manage the '_this' pointer
	      putThis=1;
	    }
	  }
	  // Note:
	  // Prototypes without parameters are compatible with all functions
	  // Adding the 'this' parameter could end up to syntax error since
	  // the prototype would then need the whole list of parameters
	}
      ;

//------------------------------------------------------------------
// parameterTypeList_this:
//
// Manage non empty parameter lists. 'void *_this' is added at the
// beginning of the parameter list for pointer to functions and
// program internal function protos/definitions.
// 
// Note: In C, puting 'void' as unique parameter means that the
//       function must have strictly 0 parameters. Then, when adding
//	 'this' as parameter to a function, 'void' must be removed
//------------------------------------------------------------------

voidParameterTypeList_this[Symbol symbol]
returns [int putThis]
{
  putThis=0;
}
        :
	#( NParameterDeclaration "void" )
 	{
	  if ((symbol==null)||(shouldAddThis(symbol))) {
	    // There is 'void' as parameter list. Replace it by 
	    // 'void * _this'
	    putThis=2;
	  }
	}
    ;

parameterTypeList_this[Symbol symbol]
returns [int putThis]
{
  putThis=0;
}
        :
	( parameterDeclaration ( COMMA | SEMI )? )+ ( VARARGS )?
 	{
	  if ((symbol==null)||(shouldAddThis(symbol))) {
	    // Add 'void *_this' at the beginning of the parameter list
	    putThis=3;
	  }
	}
       ;


//------------------------------------------------------------------
// idList_this:
//
// Manage non empty id lists (K&R ols C style). '_this' is added at
// the beginning of the parameter list for pointer to functions and
// program internal function protos/definitions.
//------------------------------------------------------------------
idList_this[Symbol symbol]
returns [int putThis]
{
  putThis=0;
}
// Note: Put dummy a: and b: to avoid ANTLR substituing ID in the java part
	:  a:ID ( COMMA b:ID )*
	{
	  if ((symbol==null)||(shouldAddThis(symbol))) {
	    putThis=4;
	  }
	}
        ;


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Remove __nonnull__ GNU extensoin
// Note: Check why it was usefull
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


attributeSpecifier
{
  boolean keep=true;
}
        :
        #(  a:"__attribute" keep=attributeList)
	  {
	    if (!keep) {
	      a.setFirstChild(null);
	    }
	  }
          | #( NAsmAttribute LPAREN expr RPAREN )
        ;

//attributeDecl:
//        #( "__attribute" attributeList )
//        | #( NAsmAttribute LPAREN expr RPAREN )
//        ;

attributeList returns [boolean keep]
{
  keep=true;
  boolean flag=true;
}
        : flag=attribute
	  { keep &= flag; }
	  ( options{warnWhenFollowAmbig=false;}: COMMA 
	    flag=attribute
	    { keep &= flag; }
	  )*  ( COMMA )?
        ;

attribute returns [boolean flag]
        { flag = true; }
        :  (
	       (ID) => id:ID
	       //==================================================================
	       // Pb with function declarators: since gcc attribute can be put in
	       // specifiers, it is not possible at that time to know if 'this'
	       // must be added or not to the function. Then, we can not know
	       // whether or not attribute parameters must be modified
	       // => we need to delete attribute (can not update them)
	       //
	       // ( { id.getText().equals("__nonnull__")&&add_param }? LPAREN
	       //   { compilerError.raiseWarning(#id,"modifying __nonnull__ attribute"); }
	       //   n:IntegralNumber
	       //   { #n.setText(""+(Integer.parseInt(n.getText())+1));}
	       //   (
	       //     COMMA nn:IntegralNumber
	       //     { #nn.setText(""+(Integer.parseInt(nn.getText())+1));}
	       //   )*
	       //   RPAREN
	       // | { id.getText().equals("__format__")&&add_param }? LPAREN ID COMMA n1:IntegralNumber COMMA n2:IntegralNumber RPAREN
	       //   {
	       //     compilerError.raiseWarning(#id,"modifying __format__ attribute");
	       //     #n1.setText(""+(Integer.parseInt(n1.getText())+1));
	       //
	       //     // For functions where arguments are not available to
	       //     // be checked, n2 is specified as 0
	       //     int n2_value=Integer.parseInt(n2.getText());
	       //     if (n2_value!=0) {
	       //	 #n2.setText(""+(n2_value+1));
	       //     }
	       //   }
	       //  |
	       // )
	       //==================================================================
               {
		 // Remove function attributes which reference function parameters
		 if (   id.getText().equals("__nonnull__")
		    || 	id.getText().equals("nonnull")
		    || 	id.getText().equals("__format__")
		    || 	id.getText().equals("format")
		    || 	id.getText().equals("__format_arg__")
		    || 	id.getText().equals("format_arg")
		    ) {
		   compilerError.raiseWarning(1,id,id.getText()+" GNU extension removed (not supported by the C2C)");
		   flag=false;
		 }
	       }
	     | ~(LPAREN | RPAREN | COMMA)
             |  LPAREN attributeList RPAREN
           )*
	;



//##################################################################
// Function definition
//##################################################################


functionDef
{
  boolean putThis=false;
}
        :   #( NFunctionDef
                ( functionDeclSpecifiers)?
	        d:declarator_this[true]
	        putThis=knr_param
                (attributeSpecifierList)?
                compoundStatement
            )
	    {
	      if (putThis) {
		// Manage the 'this' pointer
		NodeAST this_param = createKnRThisParameterDefinition();
		this_param.setNextSibling(d.getNextSibling());
	        d.setNextSibling(this_param);
	      }
	    }
        ;

//------------------------------------------------------------------
// knr_param:
//
// Manage K&R old C style for parameter declarations (if there is
// at least one  K&R parameter of course)
//
// Note: A function definition is always internal to the program,
// so that the 'void *_this' parameter must always been added
//------------------------------------------------------------------
knr_param
returns [boolean putThis]
{
  putThis=false;
  boolean is_knr_style=false;
}
	 :
	 (declaration {is_knr_style=true;} | VARARGS {is_knr_style=true;})*
	  {
	    if (is_knr_style) {
	      putThis=true;
	    }
	  }
	  ;



//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//---                                                                      ----
//            Management of parameter lists in symbol references:
//---                                                                      ----
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


//------------------------------------------------------------------
// postfixExpr :
//
// Functions calls are managed in this rule.
//   - If the argument list is attached to an ID, it is the
//     argument list of either a function pointer, function
//     internal or external to the program
//   - If the argument list is not attached to an ID, it is
//     necessarily the argument list of a pointer to function
//     (argExprList_withoutId)
//------------------------------------------------------------------


postfixExpr
	{
	  NodeAST id_node=null;
	  boolean putThis=false;
	}
	:       #( PTR expr ID )
	      | #( DOT expr ID )
	      | #( NSwizzle expr ID )
              | #( NFunctionCall 
		   { putThis=false; }
	           (  (ID) => id:ID
		              {
				id_node=id;
				if (id_node!=null) {
				  if (id_node.getReference()==null) {
				    // Should never occur
				    compilerError.raiseInternalError(
			              id_node,"(postfixExpr) Non referenced symbol '" +
			              id_node.getText() + "'");
				  }			  
				}
			      }
		   |          e:expr
	           )
		   ( 
		    {id_node!=null}? putThis=argExprList_withId[id_node]
		   |                 putThis=argExprList_withoutId
		   )
		   RPAREN
		 )
		 {
		   if (putThis) {
		     NodeAST pred=e;
		     if (id_node!=null) { pred=id; }
		     NodeAST this_param  = createThisArgumentCall();
		     this_param.setNextSibling(pred.getNextSibling());
		     pred.setNextSibling(this_param);
		   }
		 }
	      | #( LBRACKET expr expr RBRACKET )
	      | #( NPostInc expr )
	      | #( NPostDec expr )
        ;


//------------------------------------------------------------------
// argExprList_withId :
//
// Manage the argument list attached to an ID
// It is the argument list of either a
//	- function pointer,
//	- an function internal to the program
//	- an function external to the program
//	- an external builtin function
// The '_this' argument must be added in all cases, except to
// external function and external builtins
//------------------------------------------------------------------
argExprList_withId[NodeAST id_node]
returns [boolean putThis]
{
  putThis=false;
}
	: (argExprList)? 
	{
	  Symbol symbol=id_node.getReference();
	  if (symbol==null) {
	    // Should never occur
	    compilerError.raiseInternalError(id_node,"(This) Non referenced function '" + id_node.getText() + "'");
	  }
	  else {
	    // Add '_this' as argument only for call to an function external
	    // to the program
	    if ((!(symbol instanceof FunctionLabel))||(shouldAddThis(symbol))) {
	      putThis=true;
	    }
	  }
	}
	;


//------------------------------------------------------------------
// argExprList_withoutId :
//
// Manage the argument list non attached to an ID (or in function
// pointer inside a struct/union construct)
// The '_this' argument must be added in all cases
//------------------------------------------------------------------
argExprList_withoutId
returns [boolean putThis]
{
  putThis=false;
}
	: (argExprList)? 
	{
	  putThis=true;
	}
	;

exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }

