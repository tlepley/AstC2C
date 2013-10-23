/*
  This file is part of AstC2C.
  
  Authors: Monty Zukoski, Thierry Lepley
*/

/*
DESCRIPTION:
	    This tree grammar is for a Gnu C AST + OpenCL C. No
	    actions in it, it is subclass which will do something
	    useful.
*/

header {
package parser;
import ir.base.NodeAST;
}

                     
class GnuCTreeParser extends TreeParser;

options
        {
        importVocab = GNUC;
        buildAST = false;
        ASTLabelType = "NodeAST";

        // Copied following options from java grammar.
        codeGenMakeSwitchThreshold = 2;
        codeGenBitsetTestThreshold = 3;
        }


{
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


}

translationUnit  options {
  defaultErrorHandler=false;
}
        :       ( externalList )? 
        ;

/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/


externalList
        :       ( externalDef )+
        ;


externalDef
        :       declaration
        |       functionDef
        |       asm_expr
        |       SEMI
        |       pragma
        |       typelessDeclaration
        ;

pragma : 
       	 PRAGMA
	;

typelessDeclaration
        :       #(NTypeMissing initDeclList SEMI)
        ;



asm_expr
        :       #( "asm" ( "volatile" )? LCURLY expr RCURLY )
        ;

declaration
        : declarationStd
        | declarationNoInitDecl
  	| null_decl
 	;

for_declaration
        : declarationStd_body
        | declarationNoInitDecl_body
 	;

null_decl: NNoDeclaration
        ;

declarationStd
        :       declarationStd_body
        ;

declarationStd_body
        :       #( NDeclaration
                    declSpecifiers
                    (                   
                        initDeclList
                    )?
                )
        ;

declarationNoInitDecl
        :       declarationNoInitDecl_body
        ;

declarationNoInitDecl_body
        :       #( NDeclarationNoInitDecl
                    declSpecifiers
                )
        ;

declSpecifiers 
        :       ( storageClassSpecifier
                | typeQualifier
                | typeSpecifier
		| attributeSpecifier
                )+
        ;

//------------------------------------------------------------------------------
// Specific K&R declaration for avoiding grammar ambiguity with attribute
// specifier located after declarators of a function definition (before any old
// style parameter declaration)
knr_declarationStd
        :       declarationStd_body
        ;
knr_declarationStd_body
        :       #( NDeclaration
                    knr_declSpecifiers
                    ( initDeclList )?
                )
        ;
knr_declSpecifiers 
        :       ( storageClassSpecifier
                | typeQualifier
                | typeSpecifier
                )
		( storageClassSpecifier
                | typeQualifier
                | typeSpecifier
		| attributeSpecifier
                )*
        ;
//------------------------------------------------------------------------------

storageClassSpecifier
        :       "auto"
        |       "register"
        |       "typedef"
        |       functionStorageClassSpecifier
        ;


functionStorageClassSpecifier
        :       "extern"
        |       "static"
        |       "inline"
        ;


typeQualifier
        :       "const"
        |       "volatile"
		// C99 feature
        |       "restrict"
        |       oclAddressSpaceQualifier
       ;

// OpenCL Specific
oclAddressSpaceQualifier
        :       "__global"
        |       "__constant"
        |       "__local"
        |       "__private"
        ;

oclFunctionQualifier
        :       "__kernel" ( attributeSpecifierList )?
        ;

typeSpecifier
        :       "void"
        |       "char"
        |       "short"
        |       "int"
        |       "long"
        |       "float"
        |       "double"
        |       "signed"
        |       "unsigned"
        |       "__uchar"
        |       "__ushort"
        |       "__uint"
        |       "__ulong"
        |       vectorSpecifier
        |       structSpecifier
        |       unionSpecifier
        |       enumSpecifier
        |       typedefName
        |       #("typeof" LPAREN
                    ( (typeName )=> typeName 
                    | expr
                    )
                    RPAREN
                )
        |       "__complex"
	|	"__builtin_va_list"
    	|	"_Bool"
        ;

vectorSpecifier
	:       "char2"
        |       "char3"	    
        |       "char4"	    
        |       "char8"	    
        |       "char16"	    
	|       "uchar2"
        |       "uchar3"	    
        |       "uchar4"	    
        |       "uchar8"	    
        |       "uchar16"	    

	|       "short2"
        |       "short3"	    
        |       "short4"	    
        |       "short8"	    
        |       "short16"	    
	|       "ushort2"
        |       "ushort3"	    
        |       "ushort4"	    
        |       "ushort8"	    
        |       "ushort16"	    

	|       "int2"
        |       "int3"	    
        |       "int4"	    
        |       "int8"	    
        |       "int16"	    
	|       "uint2"
        |       "uint3"	    
        |       "uint4"	    
        |       "uint8"	    
        |       "uint16"	    

	|       "long2"
        |       "long3"	    
        |       "long4"	    
        |       "long8"	    
        |       "long16"	    
	|       "ulong2"
        |       "ulong3"	    
        |       "ulong4"	    
        |       "ulong8"	    
        |       "ulong16"	    

	|       "float2"
        |       "float3"	    
        |       "float4"	    
        |       "float8"	    
        |       "float16"	    
        ;

typedefName
        :       #(NTypedefName ID)
        ;


structSpecifier
        :   #("struct"
	      (attributeSpecifierList)?
	      structOrUnionBody
	      )
        ;

unionSpecifier
        :   #( "union"
	      (attributeSpecifierList)?
	       structOrUnionBody
	     )
        ;
   
structOrUnionBody
        :       ( (ID LCURLY) => ID LCURLY
                    ( structDeclarationList )?
                    RCURLY  
                |   LCURLY
                    ( structDeclarationList )?
                    RCURLY
                | ID
                )
	        ( options{warnWhenFollowAmbig=false;}: (attributeSpecifierList)?  )	        
        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/


structDeclarationList
        :       ( structDeclaration )+
        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/



structDeclaration
	:       specifierQualifierList (structDeclaratorList)?
        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/



specifierQualifierList
        :       (
                  typeSpecifier
                | typeQualifier
	        | attributeSpecifier
                )+
        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/



structDeclaratorList
        :       ( structDeclarator )+
        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/



structDeclarator
        :
        #( NStructDeclarator      
            ( declarator )?
            ( COLON constExpr )?
        )
        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/




enumSpecifier
        :   #(  "enum"
	        (options{warnWhenFollowAmbig=false;}: attributeSpecifierList )?		
                ( ID )? 
                ( LCURLY enumList RCURLY )?
      	        ( options{warnWhenFollowAmbig=false;}: (attributeSpecifierList)?  )
            )
        ;


enumList
        :       ( enumerator )+
        ;


enumerator
        :       ID ( ASSIGN constExpr )?
        ;


// GNU attributes
attributeSpecifierList
        :
        ( options{warnWhenFollowAmbig=false;}: attributeSpecifier )+
	;

attributeSpecifier:
          #( "__attribute" (.)* )
        | #( NAsmAttribute LPAREN expr RPAREN )
        ;

initDeclList
        :   initDecl
	    (
	      (attributeSpecifierList)?
	      initDecl
	     )*
        ;


initDecl
                                        { String declName = ""; }
        :       #( NInitDecl
                declarator
		(attributeSpecifierList)?
                ( ASSIGN initializer )?
                )
        ;


pointerGroup
        :       #( NPointerGroup ( STAR ( typeQualifier )* )+ )
        ;



idList
        :       ID ( COMMA ID )*
        ;




initializer
        :  #( NInitializer 
	      ( expr| lcurlyInitializer )
	     )
        ;

// Not an initializer, because only used in expressions, by syntactically close
// to 'lcurlyInitializer'
lparenthesisInitializer 
        :  #( NLparenthesisInitializer 
                ( expr )+
                RPAREN
            )
        ;

lcurlyInitializer
        :  #( NLcurlyInitializer
                initializerList
                RCURLY
            )
        ;



initializerElementLabel
        :   #( NInitializerElementLabel
                (
                    ( LBRACKET expr RBRACKET (ASSIGN)? )
                    | ID COLON
                    | DOT ID ASSIGN
                )
            )
        ;

initializerList
	:  ( 
	       (initializerElementLabel)? 
	       initializer
	    )*
        ;


declarator
        :   #( NDeclarator
                ( pointerGroup )?               
	        (attributeSpecifierList)?   

                ( ID
                | LPAREN
		  declarator RPAREN
                )

                (   #( NParameterTypeList
                      (
                        parameterTypeList
                        | (idList)?
                      )
                      RPAREN
                    )
                 | LBRACKET ( expr )? RBRACKET
                )*
             )
        ;


 
parameterTypeList
        :       ( parameterDeclaration ( COMMA | SEMI )? )+ ( VARARGS )?
        ;
    


parameterDeclaration
        :       #( NParameterDeclaration
                declSpecifiers
                (declarator | nonemptyAbstractDeclarator)?
		( attributeSpecifierList )?
                )
        ;


functionDef
        :   #( NFunctionDef
                ( functionDeclSpecifiers)? 
                declarator
                (knr_declarationStd)*
                (attributeSpecifierList)?
                compoundStatement
            )
        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/

functionDeclSpecifiers
        :       
                ( functionStorageClassSpecifier
                | typeQualifier
                | typeSpecifier
                | oclFunctionQualifier
		| attributeSpecifier
                )+
        ;

the_declaration
        :       
                (   //ANTLR doesn't know that declarationList properly eats all the declarations
                    //so it warns about the ambiguity
                    options {
                        warnWhenFollowAmbig = false;
                    } :
                localLabelDecl
                | declaration
                )
        ;

localLabelDecl
        :   #("__label__" (ID)+ )
        ;
   


compoundStatement
        :       #( NCompoundStatement
                ( blockItemList )?
                RCURLY
                )
        ;

blockItemList
        :       ( 
		  the_declaration
                | functionDef
		| statement
		| pragma
		)+
        ;

statement
        :       statementBody
        ;
        
statementBody
        :       SEMI                    // Empty statements

        |       compoundStatement       // Group of statements

        |       #(NStatementExpr expr)                    // Expressions

// Iteration statements:

        |       #( "while" expr statement )
        |       #( "do" statement expr )
        |       #( "for"
		   ( expr | for_declaration ) expr expr
                statement
                )


// Jump statements:

        |       #( "goto" expr )
        |       "continue" 
        |       "break"
        |       #( "return" ( expr )? )


// Labeled statements:
        |       #( NLabel ID (statement)? )
        |       #( "case" ( rangeExpr | constExpr ) (statement)? )
        |       #( "default" (statement)? )



// Selection statements:

        |       #( "if"
                    expr statement  
                    ( "else" statement )?
                 )
        |       #( "switch" expr statement )



        ;
/*
exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }
*/




expr
        :       assignExpr
        |       constExpr
        ;


constExpr
        :
                conditionalExpr
        |       logicalOrExpr
        |       logicalAndExpr
        |       inclusiveOrExpr
        |       exclusiveOrExpr
        |       bitAndExpr
        |       equalityExpr
        |       relationalExpr
        |       shiftExpr
        |       additiveExpr
        |       multExpr
        |       castExpr
        |       unaryExpr
        |       postfixExpr
        |       primaryExpr
        |       commaExpr
        |       emptyExpr
//        |       initializer
        |       gnuAsmExpr
        |       conversion
        |       compoundLiteral
        ;


conversion
        :   #(NConvert expr)
        ;

commaExpr
        :   #(NCommaExpr expr expr)
        ;

emptyExpr
        :   NEmptyExpression
        ;

compoundStatementExpr
        :   #(LPAREN compoundStatement RPAREN)
        ;

rangeExpr
        :   #(NRangeExpr constExpr VARARGS constExpr)
        ;

gnuAsmExpr
        :   #(NGnuAsmExpr
                ("volatile")? 
                LPAREN stringConst
                ( options { warnWhenFollowAmbig = false; }:
                  COLON (strOptExprPair ( COMMA strOptExprPair)* )?
                  ( options { warnWhenFollowAmbig = false; }:
                    COLON (strOptExprPair ( COMMA strOptExprPair)* )?
                  )?
                )?
                ( COLON stringConst ( COMMA stringConst)* )?
                RPAREN
            )
        ;

strOptExprPair
        :  stringConst ( LPAREN expr RPAREN )?
        ;
        
assignExpr
        :       #( ASSIGN expr expr)
        |       #( DIV_ASSIGN expr expr)
        |       #( PLUS_ASSIGN expr expr)
        |       #( MINUS_ASSIGN expr expr)
        |       #( STAR_ASSIGN expr expr)
        |       #( MOD_ASSIGN expr expr)
        |       #( RSHIFT_ASSIGN expr expr)
        |       #( LSHIFT_ASSIGN expr expr)
        |       #( BAND_ASSIGN expr expr)
        |       #( BOR_ASSIGN expr expr)
        |       #( BXOR_ASSIGN expr expr)
        ;


conditionalExpr
        :       #( QUESTION expr (expr)? COLON expr )
        ;


logicalOrExpr
        :       #( LOR expr expr) 
        ;


logicalAndExpr
        :       #( LAND expr expr )
        ;


inclusiveOrExpr
        :       #( BOR expr expr )
        ;


exclusiveOrExpr
        :       #( BXOR expr expr )
        ;


bitAndExpr
        :       #( BAND expr expr )
        ;



equalityExpr
        :       #( EQUAL expr expr)
        |       #( NOT_EQUAL expr expr)
        ;


relationalExpr
        :       #( LT expr expr)
        |       #( LTE expr expr)
        |       #( GT expr expr)
        |       #( GTE expr expr)
        ;



shiftExpr
        :       #( LSHIFT expr expr)
        |	#( RSHIFT expr expr)
        ;


additiveExpr
        :       #( PLUS expr expr)
        |       #( MINUS expr expr)
        ;


multExpr
        :       #( STAR expr expr)
        |       #( DIV expr expr)
        |       #( MOD expr expr)
        ;

castExpr
        :       #( NCast typeName RPAREN expr )
        ;


typeName
        :       #( NTypeName specifierQualifierList (nonemptyAbstractDeclarator)? )
        ;


nonemptyAbstractDeclarator
        :   #( NNonemptyAbstractDeclarator
              ( pointerGroup
		 ( 
		   (
		     LPAREN
		     ( nonemptyAbstractDeclarator)?
		     RPAREN
		    )
		   | #( NParameterTypeList
			(parameterTypeList)?
			RPAREN
		      )
		   | ( LBRACKET (expr)? RBRACKET )
		 )*
            |  ( 
		   (
		     LPAREN
		     ( nonemptyAbstractDeclarator )?
		     RPAREN
		    )
		   | #( NParameterTypeList		      
			(parameterTypeList)? 
			RPAREN
		      )
		   | ( LBRACKET (expr)? RBRACKET )
                )+
              )
            )
        ;

unaryExpr
        :       #( NPreInc expr )
        |       #( NPreDec expr )
        |       #( NAddress expr )
        |       #( NDereference expr )
        |       #( NUnaryPlus expr )
        |       #( NUnaryMinus expr )
        |       #( BNOT expr )
        |       #( LNOT expr )
        |       #( NLabelAsValue expr )
        |       #( "__real" expr )
        |       #( "__imag" expr )
        |       #( "sizeof"
                    ( ( LPAREN typeName )=> LPAREN typeName RPAREN
                    | expr
                    )
                )
        |       #( "__alignof"
                    ( ( LPAREN typeName )=> LPAREN typeName RPAREN
                    | expr
                    )
                )
        |       #( "__vec_step"   // OpenCL builtin
                    ( ( LPAREN typeName )=> LPAREN typeName RPAREN
                    | expr
                    )
                )
        ;



postfixExpr
	:       #( PTR expr ID )
	      | #( DOT expr ID )
 	      | #( NSwizzle expr ID )
              | #( NFunctionCall expr (argExprList)? RPAREN )
	      | #( LBRACKET expr expr RBRACKET )
	      | #( NPostInc expr )
	      | #( NPostDec expr )
        ;


primaryExpr
        :       ID
        |       IntegralNumber
        |       FloatingPointNumber
        |       charConst
        |       stringConst
        |       #( NExpressionGroup expr )
        |       compoundStatementExpr
	|       vectorLiteral
        ;

vectorLiteral
	:       #( NVectorLiteral typeName RPAREN lparenthesisInitializer )
        ;

compoundLiteral
        :       #( NCompoundLiteral typeName RPAREN lcurlyInitializer )
        ;


argExprList
        :       ( expr )+
        ;



protected
charConst
        :       CharLiteral
        ;


protected
stringConst
        :       #(NStringSeq (StringLiteral)+)
        ;


protected
intConst
        :       IntOctalConst
        |       LongOctalConst
        |       UnsignedOctalConst
        |       IntIntConst
        |       LongIntConst
        |       UnsignedIntConst
        |       IntHexConst
        |       LongHexConst
        |       UnsignedHexConst
        ;


protected
floatConst
        :       FloatDoubleConst
        |       DoubleDoubleConst
        |       LongDoubleConst
        ;


exception
catch [RecognitionException ex]
                        {
                        reportError(ex);
                        System.out.println("PROBLEM TREE:\n" 
                                                + _t.toStringList());
                        if (_t!=null) {_t = _t.getNextSibling();}
                        }


    






