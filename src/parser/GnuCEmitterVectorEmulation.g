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

/* Regenerate code with emulation of Vector */

header {
package parser;

import ir.base.*;
import ir.literals.*;
import ir.literals.c.*;
import ir.literals.ocl.*;
import ir.types.*;
import ir.types.c.*;
import ir.types.ocl.*;

import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

import common.CompilerError;
}

class GnuCEmitterVectorEmulation extends GnuCEmitter;

options {
  importVocab  = GNUC;
  buildAST     = false;
  ASTLabelType = "NodeAST";
}

{
  // Error manager
  private CompilerError compilerError = null;

  private boolean programScopeLevel=true;

  protected int tabs = 0;
  protected PrintStream currentOutput = System.out;
  protected int lineNum = 1;
  protected String currentSource = "";
  protected LineObject trueSourceFile;
  protected final int lineDirectiveThreshold = Integer.MAX_VALUE;
  protected PreprocessorInfoChannel preprocessorInfoChannel = null;
  protected Stack sourceFiles = new Stack();
  

  public GnuCEmitterVectorEmulation(PrintStream ps, CompilerError cp) {
    currentOutput=ps;
    compilerError=cp;	
  }

  public GnuCEmitterVectorEmulation(PrintStream ps, PreprocessorInfoChannel preprocChannel, CompilerError cp) {
    currentOutput=ps;
    preprocessorInfoChannel = preprocChannel;
    compilerError=cp;	
  }


  // Management of the silent mode
  private boolean is_silent=false;
  protected void setSilentMode() {
    is_silent=true;
  }
  protected void unsetSilentMode() {
    is_silent=false;
  }
  protected boolean isSilentMode() {
    return is_silent;
  }


  //
  protected void initializePrinting() {
    if (preprocessorInfoChannel!=null) {
      List<Object> preprocs = preprocessorInfoChannel.extractLinesPrecedingTokenNumber( 1 );
      printPreprocs(preprocs);
      /*    if ( currentSource.equals("") ) {
	    trueSourceFile = new LineObject(currentSource);
	    currentOutput.println("# 1 \"" + currentSource + "\"");
	    sourceFiles.push(trueSourceFile);
	    } 
      */
    }
  }

  protected void finalizePrinting() {
    // flush any leftover preprocessing instructions to the stream
    if (preprocessorInfoChannel!=null) {
      printPreprocs( 
		    preprocessorInfoChannel.extractLinesPrecedingTokenNumber( 
		    new Integer( preprocessorInfoChannel.getMaxTokenNumber() + 1 ) ));
      //print a newline so file ends at a new line
      currentOutput.println();
    }
  }

  protected void printPreprocs( List<Object> preprocs )  {
    // if there was a preprocessingDirective previous to this token then
    // print a newline and the directive, line numbers handled later
    if ( preprocs.size() > 0 ) {  
      currentOutput.println();  //make sure we're starting a new line unless this is the first line directive
      lineNum++;
      for (Object o:preprocs) {
	if ( o instanceof LineObject ) {
	  LineObject l = (LineObject) o;
	  
	  // we always return to the trueSourceFile, we never enter it from another file
	  // force it to be returning if in fact we aren't currently in trueSourceFile
	  //if (( trueSourceFile != null ) //trueSource exists
	  //	&& ( !currentSource.equals(trueSourceFile.getSource()) ) //currently not in trueSource
	  //	&& ( trueSourceFile.getSource().equals(l.getSource())  ) ) { //returning to trueSource
	  //    l.setEnteringFile( false );
	  //   l.setReturningToFile( true );
	  // }
	  
	  
	  // print the line directive
	  currentOutput.println(l);
	  lineNum = l.getLine();
	  currentSource = l.getSource();
	  
	  
	  // the very first line directive always represents the true sourcefile
	  if ( trueSourceFile == null ) {
	    trueSourceFile = new LineObject(currentSource);
	    sourceFiles.push(trueSourceFile);
	  }
	  
	  // keep our own stack of files entered
	  if ( l.getEnteringFile() ) {
	    sourceFiles.push(l);
	  }
	  
	  // if returning to a file, pop the exited files off the stack
	  if ( l.getReturningToFile() ) {
	    LineObject top = (LineObject) sourceFiles.peek();
	    while (( top != trueSourceFile ) && (! l.getSource().equals(top.getSource()) )) {
	      sourceFiles.pop();
	      top = (LineObject) sourceFiles.peek();
	    }
	  }
	}
	else {
	  // it was a #pragma or such
	  currentOutput.println(o);
	  lineNum++;
	}
      }
    }
    
  }

  // Manage preprocessing directives
  protected void moveToNode( NodeAST t ) {
    int tLineNum = t.getLocalLineNum();
    if ( tLineNum == 0 ) tLineNum = lineNum;

    if (preprocessorInfoChannel!=null) {
      List<Object> preprocs = preprocessorInfoChannel.extractLinesPrecedingTokenNumber(t.getTokenNumber());
      printPreprocs(preprocs);
    }

    if ( (lineNum != tLineNum) ) {
        // we know we'll be newlines or a line directive or it probably
        // is just the case that this token is on the next line
        // either way start a new line and indent it
        currentOutput.println();
        lineNum++;      
        printTabs();
    }

    if ( lineNum == tLineNum ){
        // do nothing special, we're at the right place
    }
    else {  
        int diff = tLineNum - lineNum;
        if ( lineNum < tLineNum ) {
            // print out the blank lines to bring us up to right line number
            for ( ; lineNum < tLineNum ; lineNum++ ) {
                currentOutput.println();
            }
            printTabs();
        }
        else { // reset lineNum and insert a #line directive
            lineNum = tLineNum; 
	    currentOutput.println("# "+lineNum+" \"" + currentSource + "\"");
        }
    }
  }
  

  protected void print( NodeAST t ) {
    // Manage preprocessing directives and line positioning
    moveToNode(t);

    if (!isSilentMode()) {
      currentOutput.print( t.getText() + " " );
    }
  }
  


  /** It is not ok to print newlines from the String passed in as 
      it will screw up the line number handling **/
  protected void print( String s ) {
    if (!isSilentMode()) {
      currentOutput.print( s + " " );
    }
  }
  
  protected void printTabs() {
    for ( int i = 0; i< tabs; i++ ) {
      currentOutput.print( "\t" );
    }
  }
  
  protected void commaSep( NodeAST t ) {
    print( t );
    if ( t.getNextSibling() != null ) {
      print( "," );
    }
  }
  
  protected void comma( ) {
    print( "," );
  }
  
  int traceDepth = 0;
  public void reportError(RecognitionException ex) {
    if ( ex != null)   {
      System.err.println("ANTLR Tree Parsing RecognitionException Error: " + ex.getClass().getName() + " " + ex );
      ex.printStackTrace(System.err);
    }
  }
  public void reportError(NoViableAltException ex) {
    System.err.println("ANTLR Tree Parsing NoViableAltException Error: " + ex.toString());
    NodeAST.printTree( ex.node );
    ex.printStackTrace(System.err);
  }
  public void reportError(MismatchedTokenException ex) {
    if ( ex != null)   {
      NodeAST.printTree( ex.node );
      System.err.println("ANTLR Tree Parsing MismatchedTokenException Error: " + ex );
      ex.printStackTrace(System.err);
    }
  }
  public void reportError(String s) {
    System.err.println("ANTLR Error from String: " + s);
  }
  public void reportWarning(String s) {
    System.err.println("ANTLR Warning from String: " + s);
  }
  protected void match(AST t, int ttype) throws MismatchedTokenException {
    //System.out.println("match("+ttype+"); cursor is "+t);
    super.match(t, ttype);
  }
  public void match(AST t, BitSet b) throws MismatchedTokenException {
    //System.out.println("match("+b+"); cursor is "+t);
    super.match(t, b);
  }
  protected void matchNot(AST t, int ttype) throws MismatchedTokenException {
    //System.out.println("matchNot("+ttype+"); cursor is "+t);
    super.matchNot(t, ttype);
  }
  public void traceIn(String rname, AST t) {
    traceDepth += 1;
    for (int x=0; x<traceDepth; x++) System.out.print(" ");
    super.traceIn(rname, t);   
  }
  public void traceOut(String rname, AST t) {
    for (int x=0; x<traceDepth; x++) System.out.print(" ");
    super.traceOut(rname, t);
    traceDepth -= 1;
  }


  // We consider that the check has already been done and that 
  // the conversion is correct
  public boolean convert(Type dt, Type st) {
    Type dest_type=dt.unqualify();
    Type src_type=st.unqualify();

    if (dest_type==src_type) {
      return false;
    }
    if (dest_type.isVector()) {
      Vector dest_vect=(Vector)dest_type;
      if (src_type.isVector()) {
	Vector src_vect=(Vector)src_type;
	if (dest_vect.getNbElements()!=src_vect.getNbElements()) {
	  // Internal error
	}
	print("__ocl_convert_"+dest_vect.dump()+"_"+src_vect.dump()+"(");
      }
      else if (src_type.isScalar()) {
	// Note: there will be an automatic conversion of the scalar
	// to the base scalar type of the vector
	print("__ocl_convert_"+dest_vect.dump()+"_"+dest_vect.dumpBaseType()+"(");
      }
      else {
	// Internal error
      }
    }
    return true;
  }


  
  // ******************************************************************
  // manageSwizzleLeftValue :
  //
  // Manage the swizzling operator appearing as left-value of operators
  // (either unary, or binary). It returns true if the swizzling
  // operator has bee found and processed and false otherwise.
  // ******************************************************************
  boolean manageSwizzleLeftValue(NodeAST swizzle, String s, boolean isAssign)
    throws antlr.RecognitionException {
    if (swizzle.getType()==NSwizzle) {
      Type   swizzle_type   = swizzle.getDataType().getType().unqualify();

      NodeAST  vec_dest_expr  = (NodeAST)swizzle.getFirstChild();
      while (vec_dest_expr.getType()==NSwizzle) {
	vec_dest_expr=(NodeAST)vec_dest_expr.getFirstChild();
      }
      Vector vec_dest_type  = (Vector)vec_dest_expr.getDataType().getType().unqualify();
      
      // Since we potentially exchange the order of printing, we move to the
      // line of the left expression
      moveToNode(vec_dest_expr);
      
      if (swizzle_type.isVector()) {
	Vector swizzle_vector_type=(Vector)swizzle_type;
	print("__ocl_set"+s+"_"+vec_dest_type.dump()+"_"+swizzle_vector_type.getNbElements()+"(");
	print("&(");
	expr(vec_dest_expr);
	print(")");
	print(",(VECTOR_SUBELEM_"+swizzle_vector_type.getNbElements()+"){{");
      }
      else {
	// Should be a scalar
	print("__ocl_set"+s+"_"+vec_dest_type.dump()+"_1(");
	print("&(");
	expr(vec_dest_expr);
	print(")");
	print(",(VECTOR_SUBELEM_1){{");
      }
      

      boolean flag=false;
      for(int index:swizzle.getDataType().getVectorElementReference()) {
	if (flag) {
	  print(",");
	}
	flag=true;
	print(""+index);
      }
      
      print("}}");

      // Assign operator has an additional operand which may be converted
      if (isAssign) {
	comma();

	NodeAST e1 = (NodeAST) swizzle.getNextSibling();
	if (swizzle_type.isVector()) {
	  boolean is_convert = convert(swizzle_type,
				       e1.getDataType().getType());
	  expr( e1 );
	  if (is_convert) {
	    print(")");
	  }
	}
	else {
	  expr( e1 );
	}
      }

      print(")");

      return true;
    }
    return false;
  }

}


initializer
	:  ni:NInitializer
           {
	     NodeAST e = (NodeAST) ni.getFirstChild();

	     switch (e.getType()) {
	     case NLcurlyInitializer:
	       lcurlyInitializer( e );
	       break; 
	     case NVectorLiteral:
	       vectorLiteral( e , true);
	       break; 
	     default:
	       // If the target is a vector and e is scalar, need to create the vector
	       if (ni.getDataType().getType().isVector() && e.getDataType().getType().isScalar()) {
		 Vector vector_type = (Vector)ni.getDataType().getType().unqualify();
		 if (programScopeLevel) {
		   print("{{");
		   for(int i=0;i<vector_type.getNbElements();i++) {
		     if (i>0) { print(", "); }
		     expr( e );
		   }
		   print("}}");
		 }
		 else {
		   print("__ocl_convert_"+vector_type.dump()+"_"+vector_type.dumpBaseType());
		   print("(");
		   expr( e );
		   print(")");
		 }
	       }
	       else {
		 expr( e );
	       }
	     }
	   }
        ;


lparent_expr[boolean flag]
{
  if (flag) { comma(); }
}
	: expr
	;


lparenthesisInitializer
	   {
	     boolean flag=false;
	   }
	:  #( NLparenthesisInitializer
	      {
		print("{{");
	      }
	      (
	        lparent_expr[flag]
		{ flag=true; }
	      )+
              RPAREN
	      {
		print("}}");
	      }
            )
        ;

//[TBW] To be managed
lparenthesisInitializer_complex[Vector vector_type]
	   {
	     int nb = 0;
	   }
	:  #( NLparenthesisInitializer
	      (
	       expr
		{
		  if (nb==0) {
		    comma();
		  }
		  nb++;
		}
	      )+
              RPAREN
            )
        ;

lparenthesisInitializer_scalar[Vector vector_type]
	:  #( lp: NLparenthesisInitializer
	      { 
		print("__ocl_convert_"+vector_type.dump()+"_"+vector_type.dumpBaseType());
		print(lp);
	      }
	      (
	       expr
	      )
              rp: RPAREN
	      { print(rp); }
            )
        ;

lparenthesisInitializer_init_program_scope_scalar[Vector vector_type]
        : lp:NLparenthesisInitializer
  	    // No non-terminal allowed as tree root, so here we manually
	    // get the first and second children of the binary operator
	    { 
	      // At program scope level in an initializer, the scalar
	      // expression is necessarily a constant expression
	      // We can then dupplicate it
	      NodeAST e;
	      e = (NodeAST) lp.getFirstChild();
	      print("{{");
	      for(int i=0;i<vector_type.getNbElements();i++) {
		if (i>0) { print(", "); }
		expr( e );
	      }
	      print("}}");
	    }
        ;

vectorLiteral[boolean isInit]
{
  EnrichedType etype=null;
  boolean is_scalar=false;
  boolean is_complex=false;
  VectorLiteral literal=null;
}
        : #( 
	    c:NVectorLiteral
	    {
	      // Check literal
	      literal=(VectorLiteral)c.getLiteral();
	      if (literal.isScalarDefined()) {
		is_scalar=true;
	      }
	      else if (literal.isComplexDefined()) {
		is_complex=true;
	      }

	      if (is_scalar || is_complex) {
		etype=c.getDataType();
		setSilentMode();
	      }
	      else if (isInit) {
		// Note: In case of initializer, no need to have a compound
		// literal, a standard struct initializer is enough
		// Also, since a compound literal is considered as gcc as
		// non constant, it avoids error with vector initializer at
		// program scope level
		setSilentMode();
	      }
	      else {
		print(c);
	      }
	    }
	    t:typeName                
	    rp:RPAREN
	    {
	      if (is_scalar || is_complex){
		unsetSilentMode();
	      }
	      else if (isInit) {
		unsetSilentMode();
	      }
	      else {
		print(rp);
	      }
	    }
	    (
	        {is_scalar && isInit && programScopeLevel}?
		lparenthesisInitializer_init_program_scope_scalar[(Vector)etype.getType().unqualify()]
	      | {is_scalar}?
		lparenthesisInitializer_scalar[(Vector)etype.getType().unqualify()]
	      | {is_complex}?
		lparenthesisInitializer_complex[(Vector)etype.getType().unqualify()]
	        { compilerError.raiseError(c,"Complex vector initializer not supported yet");}
	      | lparenthesisInitializer
	      )
	    )
        ;

primaryExpr
        :       i:ID                            { print( i ); }
        |       n:IntegralNumber                { print( n ); }
        |       fn:FloatingPointNumber          { print( fn ); }
        |       charConst
        |       stringConst
        |       #( eg:NExpressionGroup          { print( eg ); }
                 expr                           { print( ")" ); }
                )
        |       compoundStatementExpr
	|       compoundLiteral
	|       vectorLiteral[false]
        ;

functionDef
        :   #( NFunctionDef
                ( functionDeclSpecifiers)? 
                declarator
                (attributeSpecifierList)?
                ( knr_declarationStd )*
	        {programScopeLevel=false;}
                compoundStatement
	        {programScopeLevel=true;}
            )
        ;


//------------------------------------------------------------------
// Expressions
//------------------------------------------------------------------
expr
        :       
                binaryExpr
        |       conditionalExpr
        |       castExpr
        |       unaryExpr
        |       postfixExpr
        |       primaryExpr
        |       emptyExpr
		//        |       initializer
        |       rangeExpr
        |       gnuAsmExpr
        |       conversion
        ;

conversion
	:   c:NConvert //#(NConvert expr)
	    {
	      NodeAST e = (NodeAST) c.getFirstChild();
	      boolean is_convert=convert(c.getDataType().getType(),
					 e.getDataType().getType());
	      expr( e );
	      if (is_convert) {
		print(")");
	      }
	    }
        ;

//------------------------------------------------------------------
// Binary operators
//------------------------------------------------------------------

binaryExpr
        :       nothingCommaExpr
	|	noConvertBinaryExpr
	|       allConvertBinaryExpr
	|       assignExpr
        ;


//------------------------------------------------------------------
// Binary operators which can not widden scalar on any of their
// operands
//------------------------------------------------------------------


nothingCommaExpr
        :  b:NCommaExpr
  	    // no non-terminal allowed as tree root, so here we manually
	    // get the first and second children of the binary operator
	    { 
	      NodeAST e1, e2;
	      e1 = (NodeAST) b.getFirstChild();
	      e2 = (NodeAST) e1.getNextSibling();
	      expr( e1 );
	      print( b );
	      expr( e2 );
	    }                              
        ;


noConvertBinaryOperator
returns [String s]
{ s=null; }
	:       LOR	{s="LOR";}
        |       LAND	{s="LAND";}
        |       EQUAL	{s="EQUAL";}
        |       NOT_EQUAL {s="NOT_EQUAL";}
        |       LT	{s="LT";}
        |       LTE	{s="LTE";}
        |       GT	{s="GT";}
        |       GTE	{s="GTE";}
        ;

noConvertBinaryExpr
{String s=null;}
        : s=b:noConvertBinaryOperator
	    // no non-terminal allowed as tree root, so here we manually
	    // get the first and second children of the binary operator
	   {      
	     NodeAST e1, e2;
	     e1 = (NodeAST) b.getFirstChild();
	     e2 = (NodeAST) e1.getNextSibling();
	     
	     if (b.getDataType().getType().isVector()) {
	       moveToNode(e1);
    
	       // Potential expr conversion are done through the NConvert node
	       // Note: tag the function with the type of the sub-expr to and
	       //       not the operator type
	       //       ucharn op ucharn -> charn  = charn OP_ucharn(ucharn, ucharn)
	       print( "__ocl_"+s+"_"+((Vector)e1.getDataType().getType().unqualify()).dump()+"(");
	       expr( e1 );
	       comma();
	       expr( e2 );
	       print(")");
	     }
	     else {
	       expr( e1 );
	       print( b );
	       expr( e2 );
	     }
	   }                              
        ;




//------------------------------------------------------------------
// Binary operators which can widden scalar on both operands in
// a symetrical way
// Note: operators which can only widden scalar on the right operand
//       are also considered here since no conversion will occur
//       naturally on the left operand
//------------------------------------------------------------------

allConvertBinaryOperator
returns [String s]
{ s=null; }
	:       PLUS	{s="PLUS";}
        |       MINUS	{s="MINUS";}
        |       STAR	{s="STAR";}
        |       DIV	{s="DIV";}
        |       MOD	{s="MOD";}
        |       LSHIFT	{s="LSHIFT";}
        |       RSHIFT	{s="RSHIFT";}
        |       BOR	{s="BOR";}
        |       BAND	{s="BAND";}
        |       BXOR	{s="BXOR";}
        ;

allConvertBinaryExpr
{String s=null;}
        : s=b:allConvertBinaryOperator
	    // no non-terminal allowed as tree root, so here we manually
	    // get the first and second children of the binary operator
	   {      
	     NodeAST e1, e2;
	     boolean is_convert;
	     e1 = (NodeAST) b.getFirstChild();
	     e2 = (NodeAST) e1.getNextSibling();
	     
	     if (b.getDataType().getType().isVector()) {
	       moveToNode(e1);
    
	       print( "__ocl_"+s+"_"+((Vector)b.getDataType().getType().unqualify()).dump()+"(");
	       
	       is_convert=convert(b.getDataType().getType(),
				  e1.getDataType().getType());
	       expr( e1 );
	       if (is_convert) {
		 print(")");
	       }
	       comma();
	       is_convert=convert(b.getDataType().getType(),
				  e2.getDataType().getType());
	       expr( e2 );
	       if (is_convert) {
		 print(")");
	       }
	       print(")");
	     }

	     // Special case for shift which has a particular behavior in OpenCL
	     else if ( (b.getType()==LSHIFT) || (b.getType()==RSHIFT) ) {
	       Type promoted_type=e1.getDataType().getType().promote().unqualify();
	       int mask;
	       switch(promoted_type.sizeof()) {
	       case 1:
		 // char
		 mask=0x7;
		 break;
	       case 2:
		 // short
		 mask=0xf;
		 break;
	       case 4:
		 // int
		 mask=0x1f;
		 break;
	       case 8:
		 // long
		 mask=0x3f;
		 break;
	       default:
		 compilerError.raiseError("Unknown size "+promoted_type.sizeof()+" for the left shift operand");
	       	 mask=0x0;
	       }

	       // Standard
	       expr( e1 );
	       print( b );
	       print("( (");
	       expr( e2 );
	       print(") & " + mask + " )");
	     }

	     // Special case for division/modulo which should not raise any exception
	     else if (
		      ((b.getType()==DIV) || (b.getType()==MOD))  &&
		      b.getDataType().getType().isIntegerScalar()
		    ) {
	       // Resulting type
	       IntegerScalar t=(IntegerScalar)b.getDataType().getType().unqualify();

	       if ( e2.getDataType().isConstantZero() ) {
		 //( (<type>)e1 )
		 moveToNode(e1);
		 print("(");
		 print("("+t.dump()+")");
		 expr( e1 );
		 print(")");
	       }
	       else if (
			(e1.getDataType().isConstantScalar()) &&
			(e2.getDataType().isConstantScalar())) {
		 // If both are constant, generate the standard operation
		 // In effect, such constant operations can occur inside a global variable
		 // initialization, where '({ })' expressions are not allowed
		 expr( e1 );
		 print( b );
		 expr( e2 );
	       }
	       // All operands are converted in the type of the operator
	       else if (t.isUnsigned()) {
		 expr( e1 );
		 print( b );
		 //({<type> __db2=<e2>; __db2==0 ? 1 : __db2})
		 print("({ "+t.dump()+" __db2=");
		 expr(e2);
		 print("; __db2==0 ? 1 : __db2 ; })");
	       }
	       else {
		 moveToNode(e1);
		 if (b.getType()==DIV) {
		   //({<type> __db1=<e1>; <type> __db2=<e2>; __db2==-1 ? - __db1 : __db1 / (__db2==0 ? 1 : __db2); })
		   print("({ "+t.dump()+" __db1=");
		   expr(e1);
		   print("; " +t.dump()+" __db2=");
		   expr(e2);
		   print("; __db2==-1 ? - __db1 : __db1 / (__db2==0 ? 1 : __db2); })");
		 }
		 else {
		   //({<type> __db2=<e2>; __db2==-1 ? 0        : ( e1 ) % (__db2==0 ? 1 : __db2) })
		   print("({ "+t.dump()+" __db2=");
		   expr(e2);
		   print("; __db2==-1 ? 0 : ( ");
		   expr(e1);
		   print(" ) % (__db2==0 ? 1 : __db2); })");
		 }
	       }
	     }
	     else {
	       // Standard
	       expr( e1 );
	       print( b );
	       expr( e2 );
	     }
	   }                           
        ;




//------------------------------------------------------------------
// Unary operators
//------------------------------------------------------------------

unaryExpr
        :       stdUnaryExpr
        |       logicalUnaryExpr
	|       preUnaryExpr
	|       otherUnaryExpr
        ;

//------------------------------------------------------------------
// Standard unary operators oeprating on vectors
// (without pre-post modification)
//------------------------------------------------------------------

stdUnaryOperator
returns [String s]
{ s=null; }
        : NUnaryPlus  {s="UnaryPlus";}
        | NUnaryMinus {s="UnaryMinus";}
        | BNOT        {s="BNOT";}
        ;

stdUnaryExpr
{ String s=null; }
        : s=a:stdUnaryOperator
	    // no non-terminal allowed as tree root, so here we manually
	    // get the first child of the unary operator
	   {      
	     NodeAST e;
	     e = (NodeAST) a.getFirstChild();
	     
	     if (a.getDataType().getType().isVector()) {	       
	       moveToNode(a);
	       print("__ocl_"+s+"_"+((Vector)a.getDataType().getType().unqualify()).dump()+"(");   
	       expr( e );
	       print(")");
	     }
	     else {
	       print( a );
	       expr( e );
	     }
	   }                              
  	;

logicalUnaryExpr
        : a:LNOT
	    // no non-terminal allowed as tree root, so here we manually
	    // get the first child of the unary operator
	   {      
	     NodeAST e;
	     e = (NodeAST) a.getFirstChild();
	     
	     if (e.getDataType().getType().isVector()) {
	       moveToNode(a);	       
	       print("__ocl_LNOT_"+((Vector)e.getDataType().getType().unqualify()).dump()+"(");   
	       expr( e );
	       print(")");
	     }
	     else {
	       print( a );
	       expr( e );
	     }
	   }                              
  	;


preUnaryOperator
returns [String s]
{ s=null; }
        : NPreInc  {s="PreInc";}
        | NPreDec  {s="PreDec";}
        ;


preUnaryExpr
{ String s=null; }
	:  s=po:preUnaryOperator // #( NPreInc expr ) / #( NPreDec expr )
	    {      	      
	      if (!manageSwizzleLeftValue((NodeAST)po.getFirstChild(),s,false)) {      
		NodeAST e = (NodeAST) po.getFirstChild();
		
		if (po.getDataType().getType().isVector()) {
		  Vector post_vector_type=(Vector)po.getDataType().getType().unqualify();
		  
		  moveToNode(e);
		  print("__ocl_"+s+"_"+post_vector_type.dump()+"(");
		  // Take the address of the expression
		  print("&(");
		  expr(e);
		  print(")");
		  print(")");
		}
		else {
		  print( po );
		  expr( e );
		}
	      }
	    }
        ;

otherUnaryExpr
        : #( ad:NAddress { print( ad ); } expr )
        | #( de:NDereference { print( de ); } expr )
	  // gcc ?
        | #( la:NLabelAsValue { print( la ); } expr )
        | #( re:"__real" { print( re ); } expr )
        | #( im:"__imag"{ print( im ); }  expr )
        | #( s:"sizeof"                { print( s ); }
              ( ( LPAREN typeName )=> 
		  lps:LPAREN           { print( lps ); }
		  typeName 
		  rps:RPAREN           { print( rps ); }
              | expr
              )
           )
        | #( a:"__alignof"             { print( a ); }
              ( ( LPAREN typeName )=> 
                  lpa:LPAREN           { print( lpa ); }
                  typeName 
                  rpa:RPAREN           { print( rpa ); }
              | expr
              )
           )
        | #( v:"__vec_step"            { print( v ); }
              ( ( LPAREN typeName )=> 
                  lpv:LPAREN           { print( lpv ); }
                  typeName 
                  rpv:RPAREN           { print( rpv ); }
              | expr
              )
           )
        ;






//------------------------------------------------------------------
// Assignement operators and specific case with sub-vector elements
//------------------------------------------------------------------

assignOperator
returns [String s]
{ s=null; }
        :	ASSIGN 		{s="ASSIGN";}
        |       PLUS_ASSIGN 	{s="PLUS_ASSIGN";}
        |       MINUS_ASSIGN 	{s="MINUS_ASSIGN";}
        |       STAR_ASSIGN 	{s="STAR_ASSIGN";}
	|	DIV_ASSIGN 	{s="DIV_ASSIGN";}
        |       MOD_ASSIGN 	{s="MOD_ASSIGN";}
        |       RSHIFT_ASSIGN 	{s="RSHIFT_ASSIGN";}
        |       LSHIFT_ASSIGN 	{s="LSHIFT_ASSIGN";}
        |       BAND_ASSIGN 	{s="BAND_ASSIGN";}
        |       BOR_ASSIGN 	{s="BOR_ASSIGN";}
        |       BXOR_ASSIGN 	{s="BXOR_ASSIGN";}
        ;

assignExpr
{String s=null;}
	: s=a:assignOperator
          // no rules allowed as roots, so here I manually get 
          // the first and second children of the binary operator
          // and then print them out in the right order
	    {
	      if (!manageSwizzleLeftValue((NodeAST)a.getFirstChild(),s,true)) {      
		NodeAST e_left,e_right;
		e_left  = (NodeAST) a.getFirstChild();
		e_right = (NodeAST) e_left.getNextSibling();
		
		if (a.getDataType().getType().isVector()) {
		  Vector assign_vector_type=(Vector)a.getDataType().getType().unqualify();

		  moveToNode(e_left);
		  print("__ocl_"+s+"_"+assign_vector_type.dump()+"(");
		  // Take the address of the left-value
		  print("&(");
		  expr(e_left);
		  print(")");
		  print(",");
		  // Get the right value
		  boolean is_convert;
		  is_convert=convert(a.getDataType().getType(),
				     e_right.getDataType().getType());
		  expr( e_right );
		  if (is_convert) {
		    print(")");
		  }
		  print(")");
		}

		// Special case for shift which has a particular behavior in OpenCL
		else if ( (a.getType()==LSHIFT_ASSIGN) || (a.getType()==RSHIFT_ASSIGN) ) {
		  Type promoted_type=e_left.getDataType().getType().promote().unqualify();
		  int mask;
		  switch(promoted_type.sizeof()) {
		  case 1:
		    // char
		    mask=0x7;
		    break;
		  case 2:
		    // short
		    mask=0xf;
		    break;
		  case 4:
		    // int
		    mask=0x1f;
		    break;
		  case 8:
		    // long
		    mask=0x3f;
		    break;
		  default:
		    compilerError.raiseError("Unknown size "+promoted_type.sizeof()+" for the left shift operand");
		    mask=0x0;
		  }
		  
		  // Standard
		  expr( e_left );
		  print( a );
		  print("( (");
		  expr( e_right );
		  print(") & " + mask + " )");
		}

		// Special case for division which should not raise any exception
		else if (
			 ((a.getType()==DIV_ASSIGN) || (a.getType()==MOD_ASSIGN))
			 //			 && ( !e_right.getDataType().isNonNullConstant() )
			 ) {
		  // The type of the operator is not necessarily the type of the
		  // arithmetic operation.
		  // => We must ensure that the arithmetic operation is integer
		  // Note: type checking already done, so no need to distinguish
		  //       DIV_ASSIGN and MOD_ASSIGN
		  Type common_type = TypeManager.
		  getArithmeticCommonTypeNoQualifier(
				      e_left.getDataType().getType().unqualify(),
				      e_right.getDataType().getType().unqualify()
				      );
		  if (common_type.isIntegerScalar()) {
		    IntegerScalar t=(IntegerScalar)common_type;

		    if (t.isUnsigned()) {
		      expr( e_left );
		      print( a );
		      //({<type> __db0=<e_right>; __db0==0 ? 1 : __db0})
		      print("({ "+t.dump()+" __db0=");
		      expr(e_right);
		      print("; __db0==0 ? 1 : __db0 ; })");
		    }
		    else {
		      moveToNode(e_left);
		      if (a.getType()==DIV_ASSIGN) {
			//({<type> __db2=<e_right>; __db2==-1 ? (e_left *= -1) : (e_left /= (__db2==0 ? 1 : __db2)); })
			print("({ "+t.dump()+" __db2=");
			expr(e_right);
			print("; __db2==-1 ? (");
			expr(e_left);
			print(" *= -1) : (");
			expr(e_left);
			print(" /= (__db2==0 ? 1 : __db2)); })");
		      }
		      else {
			//({<type> __db2=<e_right>; __db2==-1 ? (e_left = 0) : (e_left %= (__db2==0 ? 1 : __db2)) })
			print("({ "+t.dump()+" __db2=");
			expr(e_right);
			print("; __db2==-1 ? (");
			expr(e_left);
			print(" = 0) : (");
			expr(e_left);
			print(" %= (__db2==0 ? 1 : __db2)); })");
		      }
		    }
		  }
		  else {
		    // Standard operation
		    expr( e_left );
		    print( a );
		    expr( e_right );
		  }
		}
		else {
		  expr( e_left );
		  print( a );
		  expr( e_right );
		}
	      }
	    }                            
        ;


//------------------------------------------------------------------
// Sub-vector elements getters
//------------------------------------------------------------------

postOperator
returns [String s]
{ s=null; }
        :	NPostInc       	{s="PostInc";}
        |       NPostDec	{s="PostDec";}
        ;

postfixExpr
{
  Vector vector=null;
  String s;
}
	:  #( p:PTR expr { print( p ); }
	      i1:ID { print( i1 ); } )
	|  d:DOT // #( DOT expr ID )
	     {
	       // Standard struct/union access
	       NodeAST e, id;
	       e  = (NodeAST) d.getFirstChild();
	       id = (NodeAST) e.getNextSibling();
	       
	       expr(e);
	       print(d);
	       print(id);
	     }

	|  sw:NSwizzle // #( NSwizzle expr ID )
	     {
	       // Maybe struct/union access or vector access 
	       NodeAST e, id;
	       e  = (NodeAST) sw.getFirstChild();
	       id = (NodeAST) e.getNextSibling();
	       
	       Type dest_src_type  = e.getDataType().getType();

	       Vector dest_src_vector_type=(Vector)dest_src_type.unqualify();
	       Type dest_type = sw.getDataType().getType().unqualify();
		 
	       if (dest_type.isVector()) {
		 Vector dest_vector=(Vector)dest_type;
		 print("__ocl_get_"+dest_src_vector_type.dump()+"_"+dest_vector.getNbElements()+"(");
		 expr(e);
		 print(",(VECTOR_SUBELEM_"+dest_vector.getNbElements()+"){{");
	       }
	       else {
		 // Should be a scalar
		 print("__ocl_get_"+dest_src_vector_type.dump()+"_1(");
		 expr(e);
		 print(",(VECTOR_SUBELEM_1){{");
	       }
		 		
	       boolean flag=false;
	       for(int index:dest_src_vector_type.getElementList(id.getText())) {
		 if (flag) {
		   print(",");
		 }
		 flag=true;
		 print(""+index);
	       }
	       print("}})");
	     }

	| #( f:NFunctionCall expr { print( f ); }
	       (argExprList)?
	       rp:RPAREN { print( rp ); }
	   )
	| #( b:LBRACKET expr{ print( b ); }
	       expr
	       rb:RBRACKET { print( rb ); }
	   )
	|  s=po:postOperator // #( pi:NPostInc expr ) / #( pd:NPostDec expr )
	    {      	      
	      if (!manageSwizzleLeftValue((NodeAST)po.getFirstChild(),s,false)) {      
		NodeAST e = (NodeAST) po.getFirstChild();
		
		if (po.getDataType().getType().isVector()) {
		  Vector post_vector_type=(Vector)po.getDataType().getType().unqualify();

		  moveToNode(e);
		  print("__ocl_"+s+"_"+post_vector_type.dump()+"(");
		  // Take the address of the expression
		  print("&(");
		  expr(e);
		  print(")");
		  print(")");
		}
		else {
		  expr( e );
		  print( po );
		}
	      }
	    }
        ;


conditionalExpr
	: q:QUESTION     // #( QUESTION expr ( expr )? COLON expr )
	{
	  // Get all expressions
	  NodeAST condition,e1,colon,e2;
	  condition=(NodeAST) q.getFirstChild();
	  e1 = (NodeAST) condition.getNextSibling();
	  if (e1.getType()==COLON) {
	    // The gnu-c style conditional operator without expression
	    // is not supported
	    compilerError.raiseFatalError("GNUC style conditional operator not supported in vector emulation mode");
	  }
	  colon= (NodeAST) e1.getNextSibling();
	  e2   = (NodeAST) colon.getNextSibling();

	  // Generate the code with correct conversions in case of vectors
	  boolean is_convert;
	     
	  if (q.getDataType().getType().isVector()) {
	    Vector question_vector_type=(Vector)q.getDataType().getType().unqualify();

	    if (condition.getDataType().getType().isScalar()) {
	      // The condition is a scalar:
	      // -> Operand expressions are evaluated only if needed
	      expr( condition );
	      print( q );
	      is_convert=convert(question_vector_type,
				 e1.getDataType().getType());
	      expr( e1 );
	      if (is_convert) {
		print(")");
	      }
	      print( colon );
	      is_convert=convert(question_vector_type,
				 e2.getDataType().getType());
	      expr( e2 );
	      if (is_convert) {
		print(")");
	      }
	    }
	    else {
	      // The condition is a vector
	      // -> Behavior then equivalent to 'select'

	      // Since we exchange the order of printing, we move to the
	      // line of the condition expression before any print
	      moveToNode(condition);
	      
	      print( "__ocl_QUESTION_"+question_vector_type.dump()+"(");
	      
	      expr( condition );

	      comma();
	      is_convert=convert(question_vector_type,
				 e1.getDataType().getType());
	      expr( e1 );
	      if (is_convert) {
		print(")");
	      }
	      comma();
	      is_convert=convert(question_vector_type,
				 e2.getDataType().getType());
	      expr( e2 );
	      if (is_convert) {
		print(")");
	      }
	      print(")");
	    }
	  }
	  else {
	    // Standard scalar expression
	    expr( condition );
	    print( q );
	    expr( e1 );
	    print( colon );
	    expr( e2 );
	  }           
	}
        ;

castExpr
	: c:NCast  // #( NCast typeName RPAREN expr )
	    {
	      NodeAST typename = (NodeAST) c.getFirstChild();
	      NodeAST rparen   = (NodeAST) typename.getNextSibling();
	      NodeAST e        = (NodeAST) rparen.getNextSibling();

	      if (c.getDataType().getType().isVector()) {
		// Should be vector<-scalar
		boolean is_convert=convert(c.getDataType().getType(),
					   e.getDataType().getType());
		expr( e );
		if (is_convert) {
		  print(")");
		}
	      }
	      else {
		print(c);
		typeName(typename);
		print(rparen);
		expr(e);
	      }
	    }
        ;

