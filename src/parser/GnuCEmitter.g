/*
  This file is part of AstC2C.
  
  Authors: Monty Zukoski, Thierry Lepley
*/

/* This tree grammar is for a Gnu C AST + OpenCL C.It
    turns the tree back into source code.
*/


header {
package parser;

import ir.base.LineObject;
import ir.base.PreprocessorInfoChannel;
import ir.base.NodeAST;

import java.io.PrintStream;
import java.util.Stack;
import java.util.List;

}

                     
class GnuCEmitter extends TreeParser;

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
  protected int tabs = 0;
  protected PrintStream currentOutput = System.out;
  protected int lineNum = 1;
  protected String currentSource = "";
  protected LineObject trueSourceFile;
  protected final int lineDirectiveThreshold = Integer.MAX_VALUE;
  protected PreprocessorInfoChannel preprocessorInfoChannel = null;
  protected Stack sourceFiles = new Stack();
  

  public GnuCEmitter(PrintStream ps) {
    currentOutput=ps;
  }

  public GnuCEmitter(PrintStream ps, PreprocessorInfoChannel preprocChannel) {
    currentOutput=ps;
    preprocessorInfoChannel = preprocChannel;
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
      List<Object> preprocs = preprocessorInfoChannel.extractLinesPrecedingTokenNumber( new Integer(1) );
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
        else { // just reset lineNum
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
  

}
    

translationUnit  options {
  defaultErrorHandler=false;
}
        :
                                { initializePrinting(); }
               ( externalList )? 
                                { finalizePrinting(); }
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
       p:PRAGMA  { print( p ); } 
	;

typelessDeclaration
        :       #(NTypeMissing initDeclList s: SEMI)    { print( s ); }
        ;



asm_expr
        :       #( a:"asm"                              { print( a ); } 
                 ( v:"volatile"                         { print( v ); } 
                 )? 
                    lc:LCURLY                           { print( lc ); tabs++; }
                    expr
                    rc:RCURLY                           { tabs--; print( rc ); }
                )
		{ print( ";" ); }
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

// Rule representing a declaration that has been extracted
// (instance data)
null_decl: NNoDeclaration
        ;

declarationStd
        :       declarationStd_body
		{ print( ";" ); }
        ;

declarationStd_body
        :       #( NDeclaration
                    declSpecifiers
                    ( initDeclList )?
                )
        ;

declarationNoInitDecl
        :       declarationNoInitDecl_body
		{ print( ";" ); }
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
		{ print( ";" ); }
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
        :       a:"auto"                        { print( a ); }
        |       b:"register"                    { print( b ); }
        |       c:"typedef"                     { print( c ); }
        |       functionStorageClassSpecifier
        ;


functionStorageClassSpecifier
        :       a:"extern"                      { print( a ); }
        |       b:"static"                      { print( b ); }
        |       c:"inline"                      { print( c ); }
        ;


typeQualifier
        :       a:"const"                       { print( a ); }
        |       b:"volatile"                    { print( b ); }
        |       c:"restrict"                    { print( c ); }
        |       oclAddressSpaceQualifier
        ;

oclAddressSpaceQualifier
	:       a:"__global"                      { print( a ); }
	|       b:"__constant"                    { print( b ); }
	|       c:"__local"                       { print( c ); }
	|       d:"__private"                     { print( d ); }
        ;

oclFunctionQualifier
	:       a:"__kernel"                      { print( a ); }
		( attributeSpecifierList )?
        ;

typeSpecifier
        :       a:"void"                        { print( a ); }
        |       b:"char"                        { print( b ); }
        |       c:"short"                       { print( c ); }
        |       d:"int"                         { print( d ); }
        |       e:"long"                        { print( e ); }
        |       f:"float"                       { print( f ); }
        |       g:"double"                      { print( g ); }
        |       h:"signed"                      { print( h ); }
        |       i:"unsigned"                    { print( i ); }
        |       uc:"__uchar"                    { print( uc ); }
        |       us:"__ushort"                   { print( us ); }
        |       ui:"__uint"                     { print( ui ); }
        |       ul:"__ulong"                    { print( ul ); }
        |       vectorSpecifier
        |       structSpecifier
        |       unionSpecifier
        |       enumSpecifier
        |       typedefName
        |       #(n:"typeof" lp:LPAREN             { print( n ); print( lp ); }
                    ( (typeName )=> typeName 
                    | expr
                    )
                    rp:RPAREN                      { print( rp ); }
                )
        |       p:"__complex"                   { print( p ); }
        |       q:"__builtin_va_list"           { print( q ); }
        |       r:"_Bool"                       { print( r ); }
        ;

vectorSpecifier
	:
	(
	 	 a:"char2" { print( a ); }
	 |       e:"char3" { print( e ); }
	 |       b:"char4" { print( b ); }
	 |       c:"char8" { print( c ); }
	 |       d:"char16" { print( d ); }	    
	 |       f:"uchar2" { print( f ); }
	 |       j:"uchar3" { print( j ); }
	 |       g:"uchar4" { print( g ); }
	 |       h:"uchar8" { print( h ); }
	 |       i:"uchar16" { print( i ); }	    

	 |       k:"short2" { print( k ); }
	 |       o:"short3" { print( o ); }
	 |       l:"short4" { print( l ); }
	 |       m:"short8" { print( m ); }
	 |       n:"short16" { print( n ); }
	 |       p:"ushort2" { print( p ); }
	 |       t:"ushort3" { print( t ); }
	 |       q:"ushort4" { print( q ); }
	 |       r:"ushort8" { print( r ); }
	 |       s:"ushort16" { print( s ); }

	 |       aa:"int2" { print( aa ); }
	 |       ee:"int3" { print( ee ); }
	 |       bb:"int4" { print( bb ); }
	 |       cc:"int8" { print( cc ); }
	 |       dd:"int16" { print( dd ); }
	 |       ff:"uint2" { print( ff ); }
	 |       jj:"uint3" { print( jj ); }
	 |       gg:"uint4" { print( gg ); }
	 |       hh:"uint8" { print( hh ); }
	 |       ii:"uint16"  { print( ii ); }

	 |       kk:"long2" { print( kk ); }
	 |       oo:"long3" { print( oo ); }
	 |       ll:"long4" { print( ll ); }
	 |       mm:"long8" { print( mm ); }
	 |       nn:"long16" { print( nn ); }
	 |       pp:"ulong2" { print( pp ); }
	 |       tt:"ulong3" { print( tt ); }
	 |       qq:"ulong4" { print( qq ); }
	 |       rr:"ulong8" { print( rr ); }
	 |       ss:"ulong16" { print( ss ); }

	 |       aaa:"float2" { print( aaa ); }
	 |       eee:"float3" { print( eee ); }
	 |       bbb:"float4" { print( bbb ); }
	 |       ccc:"float8" { print( ccc ); }
	 |       ddd:"float16" { print( ddd ); }
	)
        ;


typedefName
        :       #(NTypedefName i:ID         { print( i ); } )
        ;


structSpecifier
        :   #( a:"struct"                       { print( a ); }
	        (attributeSpecifierList)?
                structOrUnionBody
            )
        ;

unionSpecifier
        :   #( a:"union"                        { print( a ); }
	        (attributeSpecifierList)?
                structOrUnionBody
            )
        ;
   
structOrUnionBody
        :       ( (ID LCURLY) => i1:ID lc1:LCURLY { print( i1 ); print ( "{" ); tabs++; }
                    ( structDeclarationList )?
                    rc1:RCURLY                    { tabs--; print( rc1 ); }
                |   lc2:LCURLY                    { print( "{"); tabs++; }
                    ( structDeclarationList )?
                    rc2:RCURLY                    { tabs--; print( rc2 ); }
                | i2:ID                           { print( i2 ); }
                )
	        ( options{warnWhenFollowAmbig=false;}: (attributeSpecifierList)?  )	        
        ;

structDeclarationList
        :       (
		  structDeclaration { print( ";" ); }
                )+
        ;


structDeclaration
	:       specifierQualifierList (structDeclaratorList)?
        ;


specifierQualifierList
        :       (
                  typeSpecifier
                | typeQualifier
	        | attributeSpecifier
                )+
        ;


structDeclaratorList
        :       structDeclarator
                ( { print(","); } structDeclarator )*
        ;


structDeclarator
        :
        #( NStructDeclarator       
            ( declarator )?
            ( c:COLON { print( c ); } expr )?
        )
        ;


enumSpecifier
        :   #(  a:"enum"                   { print( a ); }
	        ( options{warnWhenFollowAmbig=false;}: attributeSpecifierList )?		
                ( i:ID { print( i ); } )? 
                ( lc:LCURLY                { print( lc ); tabs++; }
                    enumList 
                  rc:RCURLY                { tabs--; print( rc ); }
                )?
     	        ( options{warnWhenFollowAmbig=false;}: attributeSpecifierList )? 
            )
        ;


enumList
        :       
		enumerator ( {print(",");} enumerator)*
        ;


enumerator
        :       i:ID            { print( i ); }
                ( b:ASSIGN      { print( b ); }
                  expr
                )?
        ;


// GNU attributes
attributeSpecifierList
        :
        ( options{warnWhenFollowAmbig=false;}: attributeSpecifier )+
	;

attributeSpecifier:
        #( a:"__attribute"            { print( a ); print("((");}
           (b:. { print( b ); } )*
        )
	{print("))"); }
        | #( n:NAsmAttribute            { print( n ); }
             lp:LPAREN                  { print( lp ); }
             expr
             rp:RPAREN                  { print( rp ); }
           )    
        ;

initDeclList
        :       initDecl     
		( 
		 { print( "," ); }
		 (attributeSpecifierList)?
		  initDecl
		 )*
        ;


initDecl
                                        { String declName = ""; }
        :       #(NInitDecl
                declarator
		(attributeSpecifierList)?
                ( a:ASSIGN              { print( a ); }
                  initializer
                 )?
                )
        ;


pointerGroup
        :       #( NPointerGroup 
                   ( a:STAR             { print( a ); }
                    ( typeQualifier )* 
                   )+ 
                )
        ;



idList
        :       i:ID                            { print( i ); }
                (  c:COMMA                      { print( c ); }
                   id:ID                        { print( id ); }
                )*
        ;



initializer
        :  #( NInitializer 
	      (  
	         lcurlyInitializer
	       | vectorLiteral
	       | expr
	      )
	     )
        ;


initializerElementLabel
        :   #( NInitializerElementLabel
                (
                    ( l:LBRACKET              { print( l ); }
                        expr
                        r:RBRACKET            { print( r ); }
                        (a1:ASSIGN             { print( a1 ); } )?
                    )
                    | i1:ID c:COLON           { print( i1 ); print( c ); } 
                    | d:DOT i2:ID a2:ASSIGN      { print( d ); print( i2 ); print( a2 ); }
                )
            )
        ;

lparenthesisInitializer 
	   {int nb = 0;}
        :  #( n:NLparenthesisInitializer    { print( n ); }
	      (
	       {if (nb!=0) {comma();} nb++;} 
                expr
	      )+
              rp:RPAREN                     { print( rp ); } 
            )
        ;

lcurlyInitializer
        :     #(n:NLcurlyInitializer    { print( n ); tabs++; }
                initializerList       
                rc:RCURLY               { tabs--; print( rc ); } 
                )
        ;

initializerList
	    {int nb = 0;}
	:   ( 
	       {if (nb!=0) {comma();} nb++;} 
	       (initializerElementLabel)? 
	       initializer
	    )*
        ;


declarator
        :   #( NDeclarator
                ( pointerGroup )?               
		(attributeSpecifierList)?   

                ( id:ID { print( id ); }
                | lp:LPAREN { print( lp ); }
		  declarator rp:RPAREN { print( rp ); }
		)

                (   #( n:NParameterTypeList       { print( n ); }
                    (
                        parameterTypeList
                        | (idList)?
                    )
                    r:RPAREN                      { print( r ); }
                    )
                 | lb:LBRACKET { print( lb );} ( expr )? rb:RBRACKET { print( rb ); }
                )*
	      )
        ;


 
parameterTypeList
        :       ( parameterDeclaration
                    ( c:COMMA { print( c ); }
                      | s:SEMI { print( s ); }
                    )?
                )+
                ( v:VARARGS { print( v ); } )?
        ;
    


parameterDeclaration
        :       #( NParameterDeclaration
                declSpecifiers
                (declarator | nonemptyAbstractDeclarator)?
                (attributeSpecifierList)?
                )
        ;


functionDef
        :   #( NFunctionDef
                ( functionDeclSpecifiers)? 
                declarator
                ( knr_declarationStd )*
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
                |  declaration
                )
        ;

localLabelDecl
        :   #(a:"__label__"             { print( a ); }
              ( i:ID                    { commaSep( i ); }
              )+
                                        { print( ";" ); }
            )
        ;
   


compoundStatement
        :       #( cs:NCompoundStatement                { print( cs ); tabs++; }
                ( blockItemList )?
                rc:RCURLY                               { tabs--; print( rc ); }
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
        :       s:SEMI                          { print( s ); }

        |       compoundStatement       // Group of statements

        |       #(NStatementExpr
                expr                    { print( ";" ); }
                )                    // Expressions

// Iteration statements:

        |       #( w:"while" { print( w ); print( "(" ); } 
                expr { print( ")" ); } 
                statement )

        |       #( d:"do" { print( d ); } 
                statement 
                        { print( " while ( " ); }
                expr 
                        { print( " );" ); }
                )

        |       #( f:"for" { print( f ); print( "(" ); }
		( expr | for_declaration ) { print( ";" ); }
                expr    { print( ";" ); }
                expr    { print( ")" ); }
                statement
                )


// Jump statements:

        |       #( g:"goto"             { print( g );}  
                   expr                 { print( ";" ); } 
                )
        |       c:"continue"            { print( c ); print( ";" );}
        |       b:"break"               { print( b ); print( ";" );}
        |       #( r:"return"           { print( r ); }
                ( expr )? 
                                        { print( ";" ); }
                )


// Labeled statements:
        |       #( NLabel 
                ni:ID                   { print( ni ); print( ":" ); }
                ( statement )?
                )

        |       #( 
                ca:"case"               { print( ca ); }
                expr                    { print( ":" ); }
                (statement)? 
                )

        |       #( 
                de:"default"            { print( de ); print( ":" ); }
                (statement)? 
                )



// Selection statements:

        |       #( i:"if"               { print( i ); print( "(" ); }
                 expr                   { print( ")" ); }
                statement  
                (   e:"else"            { print( e ); }
                    statement 
                )?
                )
        |       #( sw:"switch"          { print( sw ); print( "(" ); }
                expr                    { print( ")" ); }
                statement 
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
	|       compoundLiteral
        ;

conversion
        :   #(NConvert expr)
        ;

emptyExpr
        :   NEmptyExpression
        ;

compoundStatementExpr
        :   #(l:LPAREN                  { print( l ); }
                compoundStatement 
                r:RPAREN                { print( r ); }
            )
        ;

rangeExpr
        :   #(NRangeExpr expr v:VARARGS{ print( v ); } expr)
        ;

gnuAsmExpr
        :   #(n:NGnuAsmExpr                        { print( n ); }
                (v:"volatile" { print( v ); } )? 
                lp:LPAREN               { print( lp ); }
                stringConst
                (  options { warnWhenFollowAmbig = false; }:
                    c1:COLON { print( c1 );} 
                    (strOptExprPair 
                        ( c2:COMMA { print( c2 ); } strOptExprPair)* 
                    )?
                  (  options { warnWhenFollowAmbig = false; }:
                    c3:COLON            { print( c3 ); }
                      (strOptExprPair 
                        ( c4:COMMA { print( c4 ); } strOptExprPair)* 
                      )?
                  )?
                )?
                ( c5:COLON              { print( c5 ); }
                  stringConst 
                  ( c6:COMMA            { print( c6 ); }
                    stringConst
                  )* 
                )?
                rp:RPAREN               { print( rp ); }
            )
        ;

strOptExprPair
        :   stringConst 
            ( 
            l:LPAREN                    { print( l ); }
            expr 
            r:RPAREN                    { print( r ); }
            )?
        ;

binaryOperator
        :       ASSIGN
        |       DIV_ASSIGN
        |       PLUS_ASSIGN
        |       MINUS_ASSIGN
        |       STAR_ASSIGN
        |       MOD_ASSIGN
        |       RSHIFT_ASSIGN
        |       LSHIFT_ASSIGN
        |       BAND_ASSIGN
        |       BOR_ASSIGN
        |       BXOR_ASSIGN
        |       LOR
        |       LAND
        |       BOR
        |       BXOR
        |       BAND
        |       EQUAL
        |       NOT_EQUAL
        |       LT
        |       LTE
        |       GT
        |       GTE
        |       LSHIFT
        |       RSHIFT
        |       PLUS
        |       MINUS
        |       STAR
        |       DIV
        |       MOD
        |       NCommaExpr
        ;

binaryExpr
        :       b:binaryOperator
                    // no rules allowed as roots, so here I manually get 
                    // the first and second children of the binary operator
                    // and then print them out in the right order
                    {       NodeAST e1, e2;
		      e1 = (NodeAST) b.getFirstChild();
		      e2 = (NodeAST) e1.getNextSibling();
		      expr( e1 );
		      print( b );
		      expr( e2 );
		    }                                     
        ;

        
conditionalExpr
        :       #( q:QUESTION 
                expr                    { print( q ); }
                ( expr )? 
                c:COLON                 { print( c ); }
                expr 
                )
        ;


castExpr
        :       #( 
                c:NCast                 { print( c ); }
                typeName                
                rp:RPAREN               { print( rp ); }
                expr
		)
        ;


typeName
	: #( NTypeName
	     specifierQualifierList (nonemptyAbstractDeclarator)?
	  )
        ;

nonemptyAbstractDeclarator
        :   #( NNonemptyAbstractDeclarator
              ( pointerGroup
		 ( 
		   (
		     lp1:LPAREN                         { print( lp1 ); }
		     ( nonemptyAbstractDeclarator)?
		     rp1:RPAREN                         { print( rp1 ); }
		    )
		   | #( n1:NParameterTypeList            { print( n1 ); }
			(parameterTypeList)?
			rp11:RPAREN                      { print( rp11 ); }
		      )
		   | (
		       lb1:LBRACKET                      { print( lb1 ); }
		       (expr)? 
		       rb1:RBRACKET                      { print( rb1 ); }
		     )
		 )*
            |  ( 
		   (
		     lp2:LPAREN                         { print( lp2 ); }
		     ( nonemptyAbstractDeclarator )?
		     rp2:RPAREN                         { print( rp2 ); }
		    )
		   | #( n2:NParameterTypeList            { print( n2 ); }
			(parameterTypeList)?
			rp21:RPAREN                      { print( rp21 ); }
		      )
		   | (
		       lb2:LBRACKET                      { print( lb2 ); }
		       (expr)? 
		       rb2:RBRACKET                      { print( rb2 ); }
		     )
                )+
              )
            )
        ;





unaryExpr
        :       #( i:NPreInc { print( i ); } expr )
        |       #( d:NPreDec { print( d ); } expr )
        |       #( ad:NAddress { print( ad ); } expr )
        |       #( de:NDereference { print( de ); }expr )
        |       #( up:NUnaryPlus { print( up ); } expr )
        |       #( um:NUnaryMinus{ print( um ); } expr )
        |       #( bn:BNOT { print( bn ); } expr )
        |       #( ln:LNOT { print( ln ); } expr )
		// gcc ?
        |       #( la:NLabelAsValue { print( la ); } expr )
        |       #( re:"__real" { print( re ); } expr )
        |       #( im:"__imag"{ print( im ); }  expr )
        |       #( s:"sizeof"                           { print( s ); }
                    ( ( LPAREN typeName )=> 
                        lps:LPAREN                      { print( lps ); }
                        typeName 
                        rps:RPAREN                      { print( rps ); }
                    | expr
                    )
                )
        |       #( a:"__alignof"                        { print( a ); }
                    ( ( LPAREN typeName )=> 
                        lpa:LPAREN                      { print( lpa ); }
                        typeName 
                        rpa:RPAREN                      { print( rpa ); }
                    | expr
                    )
                )
        |       #( v:"__vec_step"                       { print( v ); }
                    ( ( LPAREN typeName )=> 
                        lpv:LPAREN                      { print( lpv ); }
                        typeName 
                        rpv:RPAREN                      { print( rpv ); }
                    | expr
                    )
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


postfixExpr
	:       #( p:PTR expr { print( p ); }
		   i1:ID { print( i1 ); } )
	      | #( d:DOT expr { print( d ); }
		   i2:ID { print( i2 ); } )
	      | #( s:NSwizzle expr { print( s ); }
		   i3:ID { print( i3 ); } )
	      | #( f:NFunctionCall expr { print( f ); }
		   (argExprList)?
		   rp:RPAREN { print( rp ); }
		 )
	      | #( b:LBRACKET expr{ print( b ); }
		   expr
		   rb:RBRACKET { print( rb ); }
		 )
	      | #( pi:NPostInc expr { print( pi ); } )
	      | #( pd:NPostDec expr { print( pd ); } )
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
	|       vectorLiteral
        ;


vectorLiteral
        :       #( 
                c:NVectorLiteral      { print( c ); }
                typeName                
                rp:RPAREN               { print( rp ); }
                lparenthesisInitializer
		)
        ;

compoundLiteral
        :       #( 
                c:NCompoundLiteral      { print( c ); }
                typeName                
                rp:RPAREN               { print( rp ); }
                lcurlyInitializer
		)
        ;


argExprList
        :       expr ( {print( "," );} expr )*
        ;



protected
charConst
        :       c:CharLiteral                   { print( c ); }
        ;


protected
stringConst
        :       #( NStringSeq
                    (
                    s:StringLiteral                 { print( s ); }
                    )+
                )
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


    





    


