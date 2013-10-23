/*
  This file is part of AstC2C.
  
  Authors: Monty Zukoski, Thierry Lepley
*/

/*
DESCRIPTION:
   This grammar supports the GNU C language and OpenCL C.
   Clearly that this grammar does *NOT* deal with preprocessor
   functionality (including things like trigraphs), nor does this
   grammar deal with multi-byte characters nor strings containing
   multi-byte characters

CHANGELOG from the original version (Monty Zukoski):
    + More support for GnuC attributes
	+ Better support for function declaration (  int (f)(int) {} )
	+ Implemented C99 features (declaration in block, declaration
	  in 'for' loop)
	+ Add Line/source information to newly created nodes
	+ Add compiler error management
	+ Added OpenCL support
    + Added OpenVX support
*/

header {
  package parser;
  import ir.base.*;
  import ir.symboltable.SimpleSymbolTable;

  import common.CompilerError;

  import java.io.InputStream;
  import java.io.Reader;
}



//##################################################################
//
//                         Syntaxic analyzer
//
//##################################################################

class GnuCParser extends Parser;

options {
  k = 2;
  exportVocab  = GNUC;
  buildAST     = true;
  ASTLabelType = "NodeAST";

  // Copied following options from java grammar.
  codeGenMakeSwitchThreshold = 2;
  codeGenBitsetTestThreshold = 3;
}
{
  // Suppport C++-style single-line comments?
  public static boolean CPPComments = true;

  // Language options
  boolean oclLanguage = false;
  public void setOclLanguage() {
    oclLanguage=true;
  }
  boolean vxLanguage = false;
  public void setVxLanguage() {
    vxLanguage=true;
  }

  // Error object
  private CompilerError compilerError = new CompilerError();


  // Associate an external error module to the parser
  public void setCompilerError(CompilerError cp) {
    compilerError = cp;
  }

  // ##################################################################
  // Mini symbol table management for typename detection
  // ##################################################################

  // access to symbol table
  public SimpleSymbolTable symbolTable = new SimpleSymbolTable();

  // source for names to unnamed scopes
  protected int unnamedScopeCounter = 0;

  public boolean isTypedefName(String name) {
    boolean returnValue = false;
    NodeAST node = symbolTable.lookupNameInCurrentScope(name);
    for (; node != null; node = (NodeAST) node.getNextSibling() ) {
      if(node.getType() == LITERAL_typedef) {
        returnValue = true;
        break;
      }
    }
    return returnValue;
  }

  public String getAScopeName() {
    return "" + (unnamedScopeCounter++);
  }

  public void pushScope(String scopeName) {
    symbolTable.pushScope(scopeName);
  }

  public void popScope() {
    symbolTable.popScope();
  }

  public void printSymbolTable() {
    // print the symbol table
    System.out.println(symbolTable.toString());
  }


  class Marker {
    private boolean parent = false;
    public void setParent() { parent=true; }
    public boolean hasParent() { return parent;}
  };

  Marker globalMarker=new Marker();

  // ##################################################################
  // Parsing error management
  //
  // Very simple error reporting to improve [TBW]
  // ##################################################################

  public void reportError(RecognitionException ex) {
    NodeAST node=null;
    try {
      node=(NodeAST)astFactory.create(LT(1));
    } catch (Exception e) {
      compilerError.raiseFatalError("syntax error");
    }
    // ex.printStackTrace(System.err);              
    compilerError.raiseFatalError(node,"syntax error");

    // try {
    //   System.err.println("ANTLR Parsing Error: "+ ex + " token name:" + tokenNames[LA(1)]);
    //   ex.printStackTrace(System.err);
    // }
    // catch (TokenStreamException e) {
    //   System.err.println("ANTLR Parsing Error: "+ ex);
    //   ex.printStackTrace(System.err);              
    //}
  }

  public void reportError(String s) {
    compilerError.raiseError("ANTLR Parsing Error from String: " + s);
  }

  public void reportWarning(String s) {
    compilerError.raiseWarning("ANTLR Parsing Warning from String: " + s);
  }

}


translationUnit
        :       ( externalList )?       /* Empty source files are allowed.  */
        ;

externalList
        :       ( externalDef )+
        ;

// [TL note] : typeless statement goes through 'typelessDeclaration' and 'functionDef'
//             which is not clean. It would be better to manage it with DeclSpecifier
//             and 'addIntLiteral'

externalDef
	:       ( declaration )=> a:declaration
	|       ( functionPrefix )=>  b:functionDef
	|       {vxLanguage}? vxKernelDef
    |       noSpecifierDeclaration
    |       asm_expr
    |       SEMI!
    |       pragma
    ;


pragma : PRAGMA ; 

asm_expr
        :       "asm"^ 
                ("volatile")? LCURLY expr RCURLY ( SEMImore ! )+
        ;

declaration : 
        declaration_body
        SEMI!
        ;

declaration_body
	{ 
	  AST ds1   = null;
	  boolean b = false;
	}
        :       ds:declSpecifiers  { ds1 = astFactory.dupList(#ds); }
                (                       
                  initDeclList[ds1]
		  {
		    b=true;
		  }
                )?
        {
	  if (b) {
    	    int lineNum = ##.getLineNum();
    	    String source  = ##.getSource();
	    ## = #( #[NDeclaration], ##);
	    ##.setLineNum(lineNum);
	    ##.setSource(source);

	  }
	  else {
    	    int lineNum = ##.getLineNum();
    	    String source  = ##.getSource();
	    ## = #( #[NDeclarationNoInitDecl], ##);
	    ##.setLineNum(lineNum);
	    ##.setSource(source);
	  }
	}             
        ;

knr_declaration : 
        knr_declaration_body
        ( SEMI! )+
        ;

knr_declaration_body
	{ 
	  AST ds1   = null;
	  boolean b = false;
	}
        :       ds:knr_declSpecifiers  { ds1 = astFactory.dupList(#ds); }
                (                       
                  initDeclList[ds1]
		  {
		    b=true;
		  }
                )?
        {
	  if (b) {
    	    int lineNum = ##.getLineNum();
    	    String source  = ##.getSource();
	    ## = #( #[NDeclaration], ##);
	    ##.setLineNum(lineNum);
	    ##.setSource(source);

	  }
	  else {
    	    int lineNum = ##.getLineNum();
    	    String source  = ##.getSource();
	    ## = #( #[NDeclarationNoInitDecl], ##);
	    ##.setLineNum(lineNum);
	    ##.setSource(source);
	  }
	}             
        ;

/* these two are here because GCC allows "cat = 13;" as a valid program! */
functionPrefix
        { String declName; }
        :       ( (functionDeclSpecifiers)=> ds:functionDeclSpecifiers
                |  //epsilon
                )
	        declName = d:declarator[true, new Marker()]
		 ( knr_declaration )* (VARARGS)? ( SEMI )*
		( attributeSpecifierList )?
                LCURLY
        ;

noSpecifierDeclaration
        {
	  // By default, it's an int	
	  NodeAST inNodeAST = #[LITERAL_int,"int"];
	}
	:       a:initDeclList[inNodeAST] SEMI
	{
	  compilerError.raiseWarning(#a,"data definition has no type and storage class specifier, set by default as integer");

	  // Get InitDecl source information
	  int lineNum = ##.getLineNum();
	  String source = ##.getSource();

	  // Set source information to the type
	  inNodeAST.setLineNum(lineNum);
	  inNodeAST.setSource(source);

	  // Declaration statement
	  ## = #( #[NDeclaration], inNodeAST, ##);
	  ##.setLineNum(lineNum);
	  ##.setSource(source);
	}
        ;


addIntLiteralFunction
	:
	{
	  ## = #[LITERAL_int,"int"];

	  // Just for the java compiler
	  if (false) {throw new RecognitionException();}
	}
        ;

addIntLiteral[boolean is_type_specifier, NodeAST last]
	:
	{
	  if (is_type_specifier==true) {
	    compilerError.raiseWarning(1,last,"data definition has no type specifier, set by default as integer");

	    ## = #[LITERAL_int,"int"];
	    ##.setLineNum(last.getLineNum());
	    ##.setSource(last.getSource());
	  }
	  // Just for the java compiler
	  if (false) {throw new RecognitionException();}
	}
        ;

knr_declSpecifiers
        {
	  int specCount=0;
	  boolean is_type_specifier=true;
	  NodeAST last=null;
	}
        :
 		(
		  options { // this loop properly aborts when
                            //  it finds a non-typedefName ID MBZ
            	            warnWhenFollowAmbig = false;
                          } :
                  s1:storageClassSpecifier {last=#s1;}
		| t1:typeQualifier {last=#t1;}
                | ( typeSpecifier[specCount] )=>
                    specCount = u1:typeSpecifier[specCount]
 	            { is_type_specifier=false; last=#u1;}
               )
	       (
		  options { // this loop properly aborts when
                            //  it finds a non-typedefName ID MBZ
            	            warnWhenFollowAmbig = false;
                          } :
                  s:storageClassSpecifier {last=#s;}
		| t:typeQualifier {last=#t;}
                | ( typeSpecifier[specCount] )=>
                    specCount = u:typeSpecifier[specCount]
 	            { is_type_specifier=false; last=#u;}
		| a:attributeSpecifier {last=#a;}
               )*
	       addIntLiteral[is_type_specifier,last]	
        ;

declSpecifiers
        {
	  int specCount=0;
	  boolean is_type_specifier=true;
	  NodeAST last=null;
	}
        :     (
		  options { // this loop properly aborts when
                            //  it finds a non-typedefName ID MBZ
            	            warnWhenFollowAmbig = false;
                          } :
                  s:storageClassSpecifier {last=#s;}
		| t:typeQualifier {last=#t;}
                | ( typeSpecifier[specCount] )=>
                    specCount = u:typeSpecifier[specCount]
 	            { is_type_specifier=false; last=#u;}
		| a:attributeSpecifier {last=#a;}
               )+
	       addIntLiteral[is_type_specifier,last]	
        ;


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

// OCL language
oclAddressSpaceQualifier
        :       "__global"
        |       "__constant"
        |       "__local"
        |       "__private"
        ;
oclFunctionQualifier
        :       "__kernel" ( attributeSpecifierList )?
        ;


typeSpecifier [int specCount] returns [int retSpecCount]
        { retSpecCount = specCount + 1; }
        :       
        ( 	"void"
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

	|       structOrUnionSpecifier
        |       enumSpecifier
        |       { specCount==0 }? typedefName
        |       "typeof"^ LPAREN
                ( ( typeName )=> typeName
                | expr
                )
                RPAREN
        |       "__complex"
	|	"__builtin_va_list"
        |   	"_Bool"
        )
        ;

vectorSpecifier
	:
        (
	  	"char2"
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
	)
	//	{
	//	  compilerError.raiseFatalError(##, "OCL vectors not yet supported");
	//	}
        ;

typedefName
        :       { isTypedefName ( LT(1).getText() ) }?
                i:ID 
		  {
		    int lineNum = #i.getLineNum();
		    String source  = #i.getSource();
		    ## = #(#[NTypedefName], #i);
		    ##.setLineNum(lineNum);
		    ##.setSource(source);
		  }
        ;

structOrUnionSpecifier
        { String scopeName; }
        :       sou:structOrUnion!
		( attributeSpecifierList )?
                ( 
		  ( ID LCURLY )=> i:ID l:LCURLY
                        {
			  scopeName = #sou.getText() + " " + #i.getText();
                          #l.setText(scopeName);
			  pushScope(scopeName);
			}
                       ( structDeclarationList )?
                       RCURLY
		       { popScope();}
                |   l1:LCURLY
                        {
			  scopeName = getAScopeName();
                          #l1.setText(#sou.getText());
			  pushScope(scopeName);
			}
                       ( structDeclarationList )?
                       RCURLY
		       { popScope(); }
                | ID
                )
		// In case of ambuguity, attributes are attached to the struct/union,
		// not to the container declaration
		( options { warnWhenFollowAmbig = false; }: attributeSpecifierList )?
                {
		  ## = #( #sou, ## );
		}
        ;

structOrUnion
        :       "struct"
        |       "union"
        ;

structDeclarationList
        :       ( structDeclaration )+
        ;

structDeclaration
	:       specifierQualifierList (structDeclaratorList)? ( SEMI! )+
        ;

specifierQualifierList
        {
	  int specCount=0;
	  boolean is_type_specifier=true;
	  NodeAST last=null;
	}
        :       (               options {   // this loop properly aborts when
                                            // it finds a non-typedefName ID MBZ
                                            warnWhenFollowAmbig = false;
                                        } :
                ( typeSpecifier[specCount] )=>
		  specCount = u:typeSpecifier[specCount] 
   	          { is_type_specifier=false; last=#u;}
		| t:typeQualifier {last=#t;}
		| a:attributeSpecifier {last=#a;}
                )+
	       addIntLiteral[is_type_specifier,last]	
        ;

structDeclaratorList
        :       structDeclarator ( COMMA! structDeclarator )*
        ;

structDeclarator
        :       declarator[false,globalMarker]
                ( COLON constExpr )?
                  {
		    int lineNum = ##.getLineNum();
		    String source  = ##.getSource();
		    ## = #( #[NStructDeclarator], ##); 
		    ##.setLineNum(lineNum);
		    ##.setSource(source);
		  }
        ;

enumSpecifier
        :       "enum"^
		( attributeSpecifierList )?
                ( ( ID LCURLY ) => i:ID LCURLY enumList[i.getText()] RCURLY
                | LCURLY enumList["anonymous"] RCURLY
                | ID
                )
		// In case of ambuguity, attributes are attached to the struct/union,
		// not to the container declaration
		( options { warnWhenFollowAmbig = false; }: attributeSpecifierList )?
        ;
        
enumList[String enumName]
        :       enumerator[enumName] ( options{warnWhenFollowAmbig=false;}: COMMA! enumerator[enumName] )* ( COMMA! )?
        ;


enumerator[String enumName]
        :       i:ID { symbolTable.add(  i.getText(),
					 #(   null,
					      #[LITERAL_enum, "enum"],
					      #[ ID, enumName]
					      )
					 );
		     }
                (ASSIGN constExpr)?
        ;


initDeclList[AST declarationSpecifiers]
        :       initDecl[declarationSpecifiers] 
                ( options{warnWhenFollowAmbig=false;}: COMMA! 
		  ( options{warnWhenFollowAmbig=false;}: attributeSpecifierList )?
		  initDecl[declarationSpecifiers]
		)*
                ( COMMA! )?
        ;

initDecl[AST declarationSpecifiers]
                                        { String declName = ""; }
        :       declName = d:declarator[false,globalMarker]
                                        {   AST ds1, d1;
                                            ds1 = astFactory.dupList(declarationSpecifiers);
                                            d1 = astFactory.dupList(#d);
                                            symbolTable.add(declName, #(null, ds1, d1) );
                                        }
		( attributeSpecifierList )?
                ( ASSIGN initializer )?
                {
		  int lineNum = ##.getLineNum();
		  String source  = ##.getSource();
		  ## = #( #[NInitDecl], ## );
		  ##.setLineNum(lineNum);
		  ##.setSource(source);
		}
        ;



pointerGroup
        :       ( STAR ( typeQualifier )* )+ 
		{
		  int lineNum = ##.getLineNum();
		  String source  = ##.getSource();
		  ## = #( #[NPointerGroup], ##);
		  ##.setLineNum(lineNum);
		  ##.setSource(source);
		}
        ;

idList
        :       ID ( options{warnWhenFollowAmbig=false;}: COMMA ID )*
        ;



initializer
        : ( assignExpr| lcurlyInitializer )
	  { 
	    // origin
    	    int lineNum = ##.getLineNum();
    	    String source  = ##.getSource();
	    ## = #( #[NInitializer], ## );
	    ##.setLineNum(lineNum);
	    ##.setSource(source);
	  }
        ;


// GCC allows more specific initializers
initializerElementLabel
        :   (   
	        // For arrays 
	        ( LBRACKET ((constExpr VARARGS)=> rangeExpr | constExpr) RBRACKET (ASSIGN)? )
		// For struct and union
                | ID COLON
                | DOT ID ASSIGN
            )
	    // Break ambiguity with assignExpr
            {
	      int lineNum = ##.getLineNum();
	      String source  = ##.getSource();
	      ## = #( #[NInitializerElementLabel], ##) ;
	      ##.setLineNum(lineNum);
	      ##.setSource(source);
	    }
        ;

// Not an initializer, because only used in expressions, by syntactically close
// to 'lcurlyInitializer'
lparenthesisInitializer 
        :    
                LPAREN^ assignExpr (COMMA! assignExpr)* RPAREN
                { ##.setType( NLparenthesisInitializer ); }
        ;

// GCC allows empty initializer lists
lcurlyInitializer 
        :    
                LCURLY^ (initializerList ( COMMA! )? )? RCURLY
                { ##.setType( NLcurlyInitializer ); }
        ;

initializerList
        : ( // Ambiguity with assignExpr of initializer
	    (initializerElementLabel)=> initializerElementLabel |
	  )
	  initializer
	  (
	     options{warnWhenFollowAmbig=false;}:COMMA!  
	     ( // Ambiguity with assignExpr of initializer
	       (initializerElementLabel)=> initializerElementLabel |
	     )
	     initializer
	  )*
        ;
    

declarator[boolean isFunctionDefinition, Marker marker] returns [String declName]
  { declName = ""; }
  :
    ( pointerGroup {marker.setParent();} )?             
        ( attributeSpecifierList )?
        (
		  id:ID  { declName = id.getText(); }
		| LPAREN
		  declName = declarator[isFunctionDefinition, marker] RPAREN
		 {
		   if ((isFunctionDefinition)&&(marker.hasParent())) {
		     // If a marker has a parent, it means that the current declarator has
		     // a non empty sub-declarator (pointer, parameter list ot array). It
		     // means that we can not be in the function declaration parameter list
		     // -> set function_def as false
		     isFunctionDefinition=false;
		   }
		 }
      )
	   ( declaratorParameterList[isFunctionDefinition, declName] {marker.setParent();}
       | LBRACKET ( expr )? RBRACKET {marker.setParent();}
       )*
       {
		  int lineNum = ##.getLineNum();
		  String source  = ##.getSource();
		  ## = #( #[NDeclarator], ## );
		  ##.setLineNum(lineNum);
		  ##.setSource(source);
	   }
       ;

declaratorParameterList[boolean isFunctionDefinition, String declName]
        :
                LPAREN^
		  { 
		    if (isFunctionDefinition) {
		      pushScope(declName);
		    }
		    else {
		      pushScope("!"+declName); 
		    }
		  }
                (                           
                        (declSpecifiers)=> parameterTypeList
                        | (idList)?
                )
		 {
		   popScope();
		 }    
                ( COMMA! )?
                RPAREN       
		{ ##.setType(NParameterTypeList); }      
        ;
          
 
parameterTypeList
        :       parameterDeclaration
                (   options {
                            warnWhenFollowAmbig = false;
                        } : 
                  ( COMMA | SEMI )  
                  parameterDeclaration
                )*
                ( ( COMMA | SEMI ) 
                  VARARGS
                )?
        ;

parameterDeclaration
        {
	  String declName;
	}
        :       ds:declSpecifiers
	( ( declarator[false,globalMarker] )=> declName = d:declarator[false,globalMarker]
                            {
                            AST d2, ds2;
                            d2 = astFactory.dupList(#d);
                            ds2 = astFactory.dupList(#ds);
                            symbolTable.add(declName, #(null, ds2, d2));
                            }
                | nonemptyAbstractDeclarator
                )?
		( attributeSpecifierList )?
	
                  {
		    int lineNum = ##.getLineNum();
		    String source  = ##.getSource();
		    ## = #( #[NParameterDeclaration], ## );
		    ##.setLineNum(lineNum);
		    ##.setSource(source);
		  }
        ;



functionDef
        { String declName; }
        :  ( (functionDeclSpecifiers)=> ds:functionDeclSpecifiers
		   | addIntLiteralFunction //epsilon
           )
	     declName = d:declarator[true, new Marker()]
                            {
                            AST d2, ds2;
                            d2 = astFactory.dupList(#d);
                            ds2 = astFactory.dupList(#ds);
                            symbolTable.add(declName, #(null, ds2, d2));
                            pushScope(declName);
                            }
		  ( attributeSpecifierList )?
		  ( knr_declaration )*
                            { popScope(); }
                compoundStatement[declName]
                            {
			      int lineNum = ##.getLineNum();
			      String source  = ##.getSource();
			      ## = #( #[NFunctionDef], ## );
			      ##.setLineNum(lineNum);
			      ##.setSource(source);
			    }
        ;

        
vxKernelDef
        { String declName=null; }
        : 
         "__vxUserKernel"!
          LPAREN!
          id:ID!  { declName = id.getText(); }
          pl:vxParameterList[declName]!
          cs:compoundStatement[declName]
        {
             int lineNum = ##.getLineNum();
             String source  = ##.getSource();
             ## = #( #[NFunctionDef], #[LITERAL___kernel,"kernel"], #[LITERAL_void,"void"], #( #[NDeclarator], #id, #pl ) , #cs );
             ##.setLineNum(lineNum);
             ##.setSource(source);
        }
        ;

vxParameterList[String declName]
     { pushScope(declName); }
     :
       ( ( COMMA | SEMI)=> vxParameterList2
       |
       )
       RPAREN
        {
          popScope();
          ## = #( #[NParameterTypeList,"("], ## );    
        }
    ;
                      
 
vxParameterList2
:
   ( COMMA! | SEMI! ) parameterDeclaration
   ( ( COMMA | SEMI ) parameterDeclaration )*
   ( ( COMMA | SEMI )  VARARGS )?
   ( COMMA! )?
;
              

functionDeclSpecifiers
        {
	  int specCount=0;
	  boolean is_type_specifier=true;
	  NodeAST last=null;
	}
        :       ( 
		  options {
                    // this loop properly aborts when
		    // it finds a non-typedefName ID MBZ
		    warnWhenFollowAmbig = false;
		  } :
                  s:functionStorageClassSpecifier {last=#s;}
		| t:typeQualifier {last=#t;}
                | ( typeSpecifier[specCount] )=>
                  specCount = u:typeSpecifier[specCount]
 	           { is_type_specifier=false; last=#u;}
		  // OpenCL specific
		| v:oclFunctionQualifier {last=#v;}
		| a:attributeSpecifier {last=#a;}
                )+
	       addIntLiteral[is_type_specifier,last] 
        ;


the_declaration
         :       ( options {  // this loop properly aborts when
		              // it finds a non-typedefName ID MBZ
		              warnWhenFollowAmbig = false;
		           } :
                   localLabelDeclaration
                |  ( declarationPredictor )=> declaration
                )
        ;

declarationPredictor
        :       (options {      //only want to look at declaration if I don't see typedef
                    warnWhenFollowAmbig = false;
                }:
                "typedef"
                | declaration
                )
        ;



localLabelDeclaration    
        :       ( //GNU note:  any __label__ declarations must come before regular declarations.
                "__label__"^ ID (options{warnWhenFollowAmbig=false;}: COMMA! ID)* ( COMMA! )? ( SEMI! )+
                )
        ;

dummyForVocabulary
	// Rule representing a declaration without any
	// initialization
        :  NDeclarationNoInitDecl

	// Rule representing a declaration that has been extracted
	// (instance data)
        |  NNoDeclaration
       ;



// GNU attributes
attributeSpecifierList
        :
        ( options { warnWhenFollowAmbig = false; }: attributeSpecifier )+
	;

attributeSpecifier
        :       "__attribute"^ LPAREN! LPAREN! attributeList RPAREN! RPAREN!
                | "asm"^ LPAREN stringConst RPAREN { ##.setType( NAsmAttribute ); }
        ;

//attributeDecl_complex
//        :       "__attribute"^ LPAREN LPAREN attributeList RPAREN RPAREN
//                | "asm"^ LPAREN stringConst RPAREN { ##.setType( NAsmAttribute ); }
//        ;

attributeList
        :       attribute ( options{warnWhenFollowAmbig=false;}: COMMA attribute)*  ( COMMA )?
        ;

attribute
        :       ( ~(LPAREN | RPAREN | COMMA)
                |  LPAREN attributeList RPAREN
                )*
        ;
        



compoundStatement[String scopeName]
        :       LCURLY^
                  {
		    pushScope(scopeName);
                  }
                ( blockItemList )?
                  { popScope(); }
                RCURLY
                  { ##.setType( NCompoundStatement ); ##.setAttribute( "scopeName", scopeName ); }
        ;

nestedFunctionDef
        { String declName; }
        :       ( "auto" )? //only for nested functions
                ( (functionDeclSpecifiers)=> ds:functionDeclSpecifiers
                )?
		declName = d:declarator[false,globalMarker]
                            {
                            AST d2, ds2;
                            d2 = astFactory.dupList(#d);
                            ds2 = astFactory.dupList(#ds);
                            symbolTable.add(declName, #(null, ds2, d2));
                            pushScope(declName);
                            }
                ( declaration )*
                  { popScope(); }
                compoundStatement[declName]
                  {
		    int lineNum = ##.getLineNum();
		    String source  = ##.getSource();
		    ## = #( #[NFunctionDef], ## );
		    ##.setLineNum(lineNum);
		    ##.setSource(source);
		  }
        ;


empty_expr:
    {
      if (false) {
        throw new RecognitionException();
      }
      #empty_expr = #[ NEmptyExpression ];
    }
    ; 

blockItemList
        :       ( 
		 //this ambiguity is ok, declarationList and nestedFunctionDef end properly
		 options {
		   warnWhenFollowAmbig = false;
		 } :
		 ( 
		  "typedef" | "__label__" | declaration )=> the_declaration
		 | (nestedFunctionDef)=> nestedFunctionDef
                 | statement
		 | pragma
		 )+
        ;

statement
        :       SEMI                    // Empty statements
        
        |       compoundStatement[getAScopeName()]       // Group of statements

        |       expr SEMI!
		{
		  int lineNum = ##.getLineNum();
		  String source  = ##.getSource();
		  ## = #( #[NStatementExpr], ## );
		  ##.setLineNum(lineNum);
		  ##.setSource(source);
		} // Expressions

// Iteration statements:

        |       "while"^ LPAREN! expr RPAREN! statement
        |       "do"^ statement "while"! LPAREN! expr RPAREN! SEMI!
        |       "for"^
		LPAREN!
		  ( (SEMI)   => empty_expr | (expr) => expr | declaration_body ) SEMI!
		  ( (SEMI)   => empty_expr | expr ) SEMI!
		  ( (RPAREN) => empty_expr | expr )
		RPAREN!
                s:statement

// Jump statements:

        |       "goto"^ ID SEMI!
        |       "continue" SEMI!
        |       "break" SEMI!
        |       "return"^ ( expr )? SEMI!


	|       label_id:ID COLON!
		// There is not real ambiguity since a subsequent statement is mandatory
		( options { warnWhenFollowAmbig = false; }: attributeSpecifierList )?
		// Expecting a statement just after a label
		(  statement
		   | {compilerError.raiseError(#label_id,"a statement is expected after label '"+#label_id.getText()+"'");}
		)
		{
		  int lineNum = ##.getLineNum();
		  String source  = ##.getSource();
		  ## = #( #[NLabel], ## );
		  ##.setLineNum(lineNum);
		  ##.setSource(source);
		}
// GNU allows range expressions in case statements
        |       "case"^ ((constExpr VARARGS)=> rangeExpr | constExpr) COLON! ( options{warnWhenFollowAmbig=false;}:statement )?
        |       "default"^ COLON! ( options{warnWhenFollowAmbig=false;}: statement )?

// Selection statements:

        |       "if"^
                 LPAREN! expr RPAREN! statement  
                ( //standard if-else ambiguity
                        options {
                            warnWhenFollowAmbig = false;
                        } :
                "else" statement )?
        |       "switch"^ LPAREN! expr RPAREN! statement
        ;

expr
        :       assignExpr (options {
                                /* MBZ:
                                    COMMA is ambiguous between comma expressions and
                                    argument lists.  argExprList should get priority,
                                    and it does by being deeper in the expr rule tree
                                    and using (COMMA assignExpr)*
                                */
                                warnWhenFollowAmbig = false;
                            } :
                            c:COMMA^ { #c.setType(NCommaExpr); } assignExpr         
                            )*
        ;

assignExpr
        :       conditionalExpr ( a:assignOperator! assignExpr { ## = #( #a, ## );} )?
        ;

assignOperator
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
        ;


conditionalExpr
        :       logicalOrExpr
                ( QUESTION^ (expr)? COLON conditionalExpr )?
        ;

rangeExpr   //used in initializers only
        :  constExpr VARARGS constExpr
           {
	     int lineNum = ##.getLineNum();
	     String source  = ##.getSource();
	     ## = #(#[NRangeExpr], ##);
	     ##.setLineNum(lineNum);
	     ##.setSource(source);
	   }
        ;

constExpr
        :       conditionalExpr
        ;

logicalOrExpr
        :       logicalAndExpr ( LOR^ logicalAndExpr )*
        ;


logicalAndExpr
        :       inclusiveOrExpr ( LAND^ inclusiveOrExpr )*
        ;

inclusiveOrExpr
        :       exclusiveOrExpr ( BOR^ exclusiveOrExpr )*
        ;


exclusiveOrExpr
        :       bitAndExpr ( BXOR^ bitAndExpr )*
        ;


bitAndExpr
        :       equalityExpr ( BAND^ equalityExpr )*
        ;



equalityExpr
        :       relationalExpr
                ( ( EQUAL^ | NOT_EQUAL^ ) relationalExpr )*
        ;


relationalExpr
        :       shiftExpr
                ( ( LT^ | LTE^ | GT^ | GTE^ ) shiftExpr )*
        ;



shiftExpr
        :       additiveExpr
                ( ( LSHIFT^ | RSHIFT^ ) additiveExpr )*
        ;


additiveExpr
        :       multExpr
                ( ( PLUS^ | MINUS^ ) multExpr )*
        ;


multExpr
        :       castExpr
                ( ( STAR^ | DIV^ | MOD^ ) castExpr )*
        ;


castExpr
        :       ( LPAREN vectorSpecifier RPAREN )=>
                  LPAREN^ typeName RPAREN
  		   ( 
		      // Note: the OCL grammar is very ambiguous
		      // (uchar2)(uchar)0 is a cast, not a parenthesis
		      // initializer
		      ( LPAREN typeName )=>
		      // It is an other cast, not a lparenthesisInit
		      castExpr 
	              { ##.setType(NCast); }
		    | ( LPAREN assignExpr )=>
		      lparenthesisInitializer
	              { ##.setType(NVectorLiteral); }
		    | castExpr 
	              { ##.setType(NCast); }
		   )
	 // ANTLR can not desambiguate between counpout literals and casts,
	 // so we do it ourselves
         |       ( LPAREN typeName RPAREN LCURLY)=> unaryExpr
         |       ( LPAREN typeName RPAREN )=> LPAREN^ typeName RPAREN castExpr 
	          { ##.setType(NCast); }
         |       unaryExpr
        ;


typeName
	:  ( s:specifierQualifierList (nonemptyAbstractDeclarator)? )
           {
	     int lineNum = ##.getLineNum();
	     String source  = ##.getSource();
	     ## = #(#[NTypeName], ##);
	     ##.setLineNum(lineNum);
	     ##.setSource(source);
	   }
        ;


// Note: Complex and ambiguous rule in C. Need to desambiguate it manually ...
nonemptyAbstractDeclarator
        :   (
                (pointerGroup (LPAREN | LBRACKET)) =>
		 // pointer direct-abstract-declarator
                (   pointerGroup
		    (
		    	  (LPAREN (pointerGroup | LPAREN | LBRACKET)) 
			   => LPAREN nonemptyAbstractDeclarator ( COMMA! )? RPAREN
                   	| declaratorParameterList[false, "abstract"]
                   	| (LBRACKET (expr)? RBRACKET)
               	    )+
	        )
		 // pointer
               | (pointerGroup) => pointerGroup
	       | // direct-abstract-declarator
		  (   
		     (LPAREN (pointerGroup | LPAREN | LBRACKET)) 
		       => LPAREN nonemptyAbstractDeclarator ( COMMA! )? RPAREN
                   | declaratorParameterList[false, "abstract"]
                   | (LBRACKET (expr)? RBRACKET)
                )+
            )
            {
	      int lineNum = ##.getLineNum();
	      String source  = ##.getSource();
	      ## = #( #[NNonemptyAbstractDeclarator], ## );
	      ##.setLineNum(lineNum);
	      ##.setSource(source);
	    }
                                
        ;

unaryExpr
        :       postfixExpr
        |       i:INC^ unaryExpr
            	{ #i.setType( NPreInc ); }
	|       d:DEC^ unaryExpr
            	{ #d.setType( NPreDec ); }
        |       (
		 b:BAND^
            	   { #b.setType( NAddress ); }
		| s:STAR^
            	   { #s.setType( NDereference ); }
		| p:PLUS^
            	   { #p.setType( NUnaryPlus ); }
		| m:MINUS^
            	   { #m.setType( NUnaryMinus ); }
        	| BNOT^    //also stands for complex conjugation
        	| LNOT^
        	| l:LAND^  //for label dereference (&&label)
            	   { #l.setType( NLabelAsValue ); }
        	| "__real"^
        	| "__imag"^
		)
	        castExpr

		// Notes for sizeof & alignof :
	        // The C99 grammar puts unaryExpr before '('typeName')', the pb
		// is that ANTLR is notr able to disambguate unaryExpr and
		// '('typeName')' even with lookahead. ANTLR will match unaryExpr
		// in case of '('typeName')' and will end up into syntax error.
	        // We must them match manually '('typeName')' before unaryExpr
		// and then match manually before compoundLiteral (which is an
		// unaryExpr and then has priority) since it is ambiguous with
		// '('typeName')'
        |       "sizeof"^
                ( 
                  ( LPAREN typeName RPAREN LCURLY)=> unaryExpr
                | ( LPAREN typeName )=> LPAREN typeName RPAREN
                | unaryExpr
                )
        |       "__alignof"^
                (
                  ( LPAREN typeName RPAREN LCURLY)=> unaryExpr
		| ( LPAREN typeName )=> LPAREN typeName RPAREN
                | unaryExpr
                )
        |       "__vec_step"^ // OpenCL specific
                (
                  ( LPAREN typeName RPAREN LCURLY)=> unaryExpr
		| ( LPAREN typeName )=> LPAREN typeName RPAREN
                | unaryExpr
                )       
        |       gnuAsmExpr
        ;

postfixExpr
        :  (
	         primaryExpr
               | compoundLiteral
           )

                ( PTR^ ID
                | DOT^ ID
                | l:LPAREN^ (a:argExprList)? RPAREN
            	  { #l.setType( NFunctionCall ); }
                | LBRACKET^ expr RBRACKET
		| i:INC^
            	  { #i.setType( NPostInc ); }
		| d:DEC^
            	  { #d.setType( NPostDec ); }
                )*

        ;

compoundLiteral:
                LPAREN^ typeName RPAREN lcurlyInitializer
                {##.setType(NCompoundLiteral);}
        ;

gnuAsmExpr
        :       "asm"^ ("volatile")? 
                LPAREN stringConst
                ( options { warnWhenFollowAmbig = false; }:
                  COLON (strOptExprPair ( COMMA strOptExprPair)* )?
                  ( options { warnWhenFollowAmbig = false; }:
                    COLON (strOptExprPair ( COMMA strOptExprPair)* )?
                  )?
                )?
                ( COLON stringConst ( COMMA stringConst)* )?
                RPAREN
                { ##.setType(NGnuAsmExpr); }
        ;

//GCC requires the PARENs
strOptExprPair
        :  stringConst ( LPAREN expr RPAREN )?
        ;


primaryExpr
        :       ID
        |       IntegralNumber
        |       FloatingPointNumber
        |       charConst
        |       stringConst
	// A compound statement enclosed in parentheses is a valid expression an in GNU C.
	// This allows use loops, switches, and local variables within an expression.
	// The last thing in the compound statement should be an expression followed by a semicolon;
	// the value/type of this subexpression serves as value/type of the entire construct.
	// See http://gcc.gnu.org/onlinedocs/gcc/Statement-Exprs.html
        |       (LPAREN LCURLY) => LPAREN^ compoundStatement[getAScopeName()] RPAREN
        |       (LPAREN^ expr RPAREN)        { ##.setType(NExpressionGroup); }
        ;
    

argExprList
        :       assignExpr ( COMMA! assignExpr )*
        ;



protected
charConst
        :       CharLiteral
        ;


protected
stringConst
        :       (StringLiteral)+ 
		{
		  int lineNum = ##.getLineNum();
		  String source  = ##.getSource();
		  ## = #(#[NStringSeq], ##);
		  ##.setLineNum(lineNum);
		  ##.setSource(source);
		}
        ;


protected
intConst        :
		IntOctalConst
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


dummy
        :       NTypedefName
        |       NInitDecl
        |       NDeclarator
        |       NStructDeclarator
        |       NDeclaration
        |       NCast
        |       NPointerGroup
        |       NExpressionGroup
        |       NFunctionCall
	|       NPreInc
	|       NPreDec
	|       NAddress
	|       NDereference
	|       NUnaryPlus
	|       NUnaryMinus
	|       NPostInc
	|       NPostDec
        |       NNonemptyAbstractDeclarator
        |       NInitializer
        |       NStatementExpr
        |       NEmptyExpression
        |       NParameterTypeList
        |       NFunctionDef
        |       NCompoundStatement
        |       NParameterDeclaration
        |       NCommaExpr
        |       NUnaryExpr
        |       NLabel
        |       NPostfixExpr
        |       NRangeExpr
        |       NStringSeq
        |       NInitializerElementLabel
        |       NLcurlyInitializer
        |       NLparenthesisInitializer
        |       NAsmAttribute
        |       NGnuAsmExpr
        |       NTypeMissing
	|	NVectorLiteral
	|	NConvert
	|	NTypeName
	|	NLabelAsValue
	|	NSwizzle
	|	NCompoundLiteral
        ;




//##################################################################
//
//                         Lexical analyzer
//
//##################################################################

{
        import java.io.*;
        import antlr.*;
}


class GnuCLexer extends Lexer;
options 
        {
        k = 3;
        exportVocab = GNUC;
        testLiterals = false;
        }    
tokens {
        LITERAL___extension__ = "__extension__";
}

{
  // Language options
  boolean oclLanguage = false;
  public void setOclLanguage() {
    oclLanguage=true;
  }
  boolean vxLanguage = false;
  public void setVxLanguage() {
    vxLanguage=true;
  }

  // Error object
  private CompilerError compilerError = new CompilerError();

  public void initialize(String src)
  {
    setOriginalSource(src);
    initialize();
  }

  public void initialize() 
  {
    literals.put(new ANTLRHashString("__alignof__", this), new Integer(LITERAL___alignof));
    literals.put(new ANTLRHashString("__asm", this), new Integer(LITERAL_asm));
    literals.put(new ANTLRHashString("__asm__", this), new Integer(LITERAL_asm));
    literals.put(new ANTLRHashString("__attribute__", this), new Integer(LITERAL___attribute));
    literals.put(new ANTLRHashString("__complex__", this), new Integer(LITERAL___complex));
    literals.put(new ANTLRHashString("__const", this), new Integer(LITERAL_const));
    literals.put(new ANTLRHashString("__const__", this), new Integer(LITERAL_const));
    literals.put(new ANTLRHashString("__restrict", this), new Integer(LITERAL_restrict));
    literals.put(new ANTLRHashString("__restrict__", this), new Integer(LITERAL_restrict));
    literals.put(new ANTLRHashString("__imag__", this), new Integer(LITERAL___imag));
    literals.put(new ANTLRHashString("__inline", this), new Integer(LITERAL_inline));
    literals.put(new ANTLRHashString("__inline__", this), new Integer(LITERAL_inline));
    literals.put(new ANTLRHashString("__real__", this), new Integer(LITERAL___real));
    literals.put(new ANTLRHashString("__signed", this), new Integer(LITERAL_signed));
    literals.put(new ANTLRHashString("__signed__", this), new Integer(LITERAL_signed));
    literals.put(new ANTLRHashString("__typeof", this), new Integer(LITERAL_typeof));
    literals.put(new ANTLRHashString("__typeof__", this), new Integer(LITERAL_typeof));
    literals.put(new ANTLRHashString("__volatile", this), new Integer(LITERAL_volatile));
    literals.put(new ANTLRHashString("__volatile__", this), new Integer(LITERAL_volatile));

    if (oclLanguage) {
      // OpenCL specific
      literals.put(new ANTLRHashString("global", this), new Integer(LITERAL___global));
      literals.put(new ANTLRHashString("constant", this), new Integer(LITERAL___constant));
      literals.put(new ANTLRHashString("local", this), new Integer(LITERAL___local));
      literals.put(new ANTLRHashString("private", this), new Integer(LITERAL___private));
      literals.put(new ANTLRHashString("kernel", this), new Integer(LITERAL___kernel));
      literals.put(new ANTLRHashString("uchar", this), new Integer(LITERAL___uchar));
      literals.put(new ANTLRHashString("ushort", this), new Integer(LITERAL___ushort));
      literals.put(new ANTLRHashString("uint", this), new Integer(LITERAL___uint));
      literals.put(new ANTLRHashString("ulong", this), new Integer(LITERAL___ulong));
      literals.put(new ANTLRHashString("vec_step", this), new Integer(LITERAL___vec_step));
    }
    
    if (vxLanguage) {
      // OpenCL specific
      literals.put(new ANTLRHashString("vxUserKernel", this), new Integer(LITERAL___vxUserKernel));
    }
  }


  LineObject lineObject = new LineObject();
  String originalSource = "";
  PreprocessorInfoChannel preprocessorInfoChannel = new PreprocessorInfoChannel();
  int tokenNumber = 0;
  boolean countingTokens = true;
  int deferredLineCount = 0;

  public void setCountingTokens(boolean ct) {
    countingTokens = ct;
    if ( countingTokens ) {
      tokenNumber = 0;
    }
    else {
      tokenNumber = 1;
    }
  }

  public void setOriginalSource(String src) {
    originalSource = src;
    lineObject.setSource(src);
  }

  public void setSource(String src) {
    lineObject.setSource(src);
  }

  public String getOriginalSource(String src) {
    return originalSource;
  }

  
  public PreprocessorInfoChannel getPreprocessorInfoChannel() {
    return preprocessorInfoChannel;
  }

  public void setPreprocessingDirective(String pre) {
    preprocessorInfoChannel.addLineForTokenNumber( pre, new Integer(tokenNumber) );
  }
  
  protected Token makeToken(int t) {
    if ( t != Token.SKIP && countingTokens) {
      tokenNumber++;
    }
    CToken tok = (CToken) super.makeToken(t);
    tok.setLine(lineObject.line);
    tok.setSource(lineObject.source);
    tok.setTokenNumber(tokenNumber);
    
    lineObject.line += deferredLineCount;
    deferredLineCount = 0;
    return tok;
  }

  public void deferredNewline() { 
    deferredLineCount++;
  }

  public void newline() { 
    lineObject.newline();
  }

}



protected
Vocabulary
        :       '\3'..'\377'
        ;

/* Operators: */

ASSIGN          : '=' ;
COLON           : ':' ;
COMMA           : ',' ;
QUESTION        : '?' ;
SEMI            : ';' ;
PTR             : "->" ;


LPAREN          : '(' ;
RPAREN          : ')' ;
LBRACKET        : '[' ;
RBRACKET        : ']' ;
LCURLY          : '{' ;
RCURLY          : '}' ;

EQUAL           : "==" ;
NOT_EQUAL       : "!=" ;
LTE             : "<=" ;
LT              : "<" ;
GTE             : ">=" ;
GT              : ">" ;

DIV             : '/' ;
DIV_ASSIGN      : "/=" ;
PLUS            : '+' ;
PLUS_ASSIGN     : "+=" ;
INC             : "++" ;
MINUS           : '-' ;
MINUS_ASSIGN    : "-=" ;
DEC             : "--" ;
STAR            : '*' ;
STAR_ASSIGN     : "*=" ;
MOD             : '%' ;
MOD_ASSIGN      : "%=" ;
RSHIFT          : ">>" ;
RSHIFT_ASSIGN   : ">>=" ;
LSHIFT          : "<<" ;
LSHIFT_ASSIGN   : "<<=" ;

LAND            : "&&" ;
LNOT            : '!' ;
LOR             : "||" ;

BAND            : '&' ;
BAND_ASSIGN     : "&=" ;
BNOT            : '~' ;
BOR             : '|' ;
BOR_ASSIGN      : "|=" ;
BXOR            : '^' ;
BXOR_ASSIGN     : "^=" ;

Whitespace
        :       ( ( ' ' | '\t' | '\014')
                | "\r\n"                { newline(); }
                | ( '\n' | '\r' )       { newline(); }
                )                       { _ttype = Token.SKIP; }
        ;

Comment
        :       "/*"
                ( { LA(2) != '/' }? '*'
                | "\r\n"                { deferredNewline(); }
                | ( '\r' | '\n' )       { deferredNewline(); }
                | ~( '*'| '\r' | '\n' )
                )*
                "*/"                    { _ttype = Token.SKIP; }
        ;


CPPComment
        :
                "//" ( ~('\n') )* 
                        {
                        _ttype = Token.SKIP;
                        }
        ;

PREPROC_DIRECTIVE
options {
  paraphrase = "a line directive";
}

        :
        '#'
        ( ( "line" || (( ' ' | '\t' | '\014')+ '0'..'9')) => LineDirective      
            | (~'\n')*                                  { setPreprocessingDirective(getText()); }
        )
                {  
                    _ttype = Token.SKIP;
                }
        ;

PRAGMA
	:
	'#' (Space)* "pragma" ((Space)+ ID)?
	(~('\r' | '\n'))*
	("\r\n" | "\r" | "\n")
	{ newline(); }
;
exception // for rule
catch [RecognitionException ex] {
  NodeAST t=new NodeAST();
  t.setLineNum(lineObject.getLine());
  t.setSource(lineObject.getSource());
  compilerError.raiseFatalError(t,"syntax error in #pragma directive");
}



protected Space:
        ( ' ' | '\t' | '\014')
        ;

protected LineDirective {
  boolean oldCountingTokens = countingTokens;
  countingTokens = false;
}
:
	{
	  lineObject = new LineObject();
	  deferredLineCount = 0;
	}
	("line")?  // this would be for if the directive started "#line",
		   // but not there for GNU directives
	(Space)+
	n:IntegralNumber { lineObject.setLine(Integer.parseInt(n.getText())); } 
	(Space)+
	( fn:StringLiteral
	  {
	    try { 
	      lineObject.setSource(fn.getText().substring(1,fn.getText().length()-1)); 
	    } 
	    catch (StringIndexOutOfBoundsException e) { /*not possible*/ } 
	  }
	| fi:ID { lineObject.setSource(fi.getText()); }
        )?
	(Space)*
	("1"      { lineObject.setEnteringFile(true); }    )?
	(Space)*
	("2"      { lineObject.setReturningToFile(true); } )?
	(Space)*
	("3"      { lineObject.setSystemHeader(true); }    )?
	(Space)*
	("4"      { lineObject.setTreatAsC(true); }        )?
	(~('\r' | '\n'))*
	("\r\n" | "\r" | "\n")
	{
	  preprocessorInfoChannel.
	    addLineForTokenNumber(new LineObject(lineObject), new Integer(tokenNumber));
	  countingTokens = oldCountingTokens;
	}
;


/* Literals: */

/*
 * Note that we do NOT handle tri-graphs nor multi-byte sequences.
 */


/*
 * Note that we can't have empty character constants (even though we
 * can have empty strings :-).
 */
CharLiteral
        :       '\'' ( Escape | ~( '\'' ) ) '\''
        ;



WideCharLiteral
        :
                'L' CharLiteral
                                { $setType(CharLiteral); }
        ;


StringLiteral
        :
                '"'
                ( ('\\' ~('\n'))=> Escape
                | ( '\r'        { newline(); }
                  | '\n'        {
                                newline();
                                }
                  | '\\' '\n'   {
                                newline();
                                }
                  )
                | ~( '"' | '\r' | '\n' | '\\' )
                )*
                '"'
        ;


WideStringLiteral
        :
                'L' StringLiteral
                                { $setType(StringLiteral); }
        ;

protected BadStringLiteral
        :       // Imaginary token.
        ;


/*
 * Handle the various escape sequences.
 *
 * Note carefully that these numeric escape *sequences* are *not* of the
 * same form as the C language numeric *constants*.
 *
 * There is no such thing as a binary numeric escape sequence.
 *
 * Octal escape sequences are either 1, 2, or 3 octal digits exactly.
 *
 * There is no such thing as a decimal escape sequence.
 *
 * Hexadecimal escape sequences are begun with a leading \x and continue
 * until a non-hexadecimal character is found.
 *
 * No real handling of tri-graph sequences, yet.
 */

protected
Escape
        :       '\\'
                ( options{warnWhenFollowAmbig=false;}: 
                  ~('0'..'7' | 'x')
                | ('0'..'3') ( options{warnWhenFollowAmbig=false;}: Digit )*
                | ('4'..'7') ( options{warnWhenFollowAmbig=false;}: Digit )*
                | 'x' ( options{warnWhenFollowAmbig=false;}: Digit | 'a'..'f' | 'A'..'F' )+
                )
        ;



/* Numeric Constants: */

protected
Digit
        :       '0'..'9'
        ;

protected IntSuffix
	: (   'L'
            | 'l'
            | 'U'
            | 'u'

	    // Complex numbers ?
            | 'I'
            | 'i'
            | 'J'
            | 'j'
	     
	   )
        ;
protected FloatSuffix
        :
              'F'
            | 'f'
	    | 'L'
            | 'l'
        ;

protected
Exponent
        :       ( 'e' | 'E' ) ( '+' | '-' )? ( Digit )+
        ;

protected
BinaryExponent
        :       ( 'p' | 'P' ) ( '+' | '-' )? ( Digit )+
        ;


// DOT & VARARGS are commented out since they are generated as part of
// the Number rule below due to some bizarre lexical ambiguity shme.

protected
DOT:;
protected
VARARGS:;


protected HexadecimalPrefix :
	'0' ( 'x' | 'X' )
	;
protected HexadecimalDigit :
        'a'..'f' | 'A'..'F' | Digit
	;

// Floating point Literals
protected DecimalFloatingConstant1 : 
      ( Digit )+ '.' ( Digit )* ( Exponent )? ( FloatSuffix )?
      ;
protected DecimalFloatingConstant2 : 
      '.' ( Digit )+ ( Exponent )? ( FloatSuffix )?
      ;
protected DecimalFloatingConstant3 : 
      ( Digit )+ Exponent ( FloatSuffix )?
      ;
protected HexadecimalFloatingConstant1 : 
      HexadecimalPrefix
      ( HexadecimalDigit )+
      '.' ( HexadecimalDigit )*
      ( BinaryExponent )?
      ( FloatSuffix )?
      ;
protected HexadecimalFloatingConstant2 : 
      HexadecimalPrefix
      '.' ( HexadecimalDigit )+
      ( BinaryExponent )?
      ( FloatSuffix )?
      ;
protected HexadecimalFloatingConstant3 : 
      HexadecimalPrefix
      ( HexadecimalDigit )+
      BinaryExponent
      ( FloatSuffix )?
      ;

protected
FloatingPointNumber:;

IntegralNumber
        :
	// Floating point Literals
	        ( ( Digit )+ '.' )
		  => DecimalFloatingConstant1
                  { _ttype = FloatingPointNumber; }
        |       ( '.' ( Digit )+ )
		  => DecimalFloatingConstant2
                  { _ttype = FloatingPointNumber; }
        |       ( ( Digit )+ Exponent )
		  => DecimalFloatingConstant3
                  { _ttype = FloatingPointNumber; }
	|       ( HexadecimalPrefix ( HexadecimalDigit )+ '.' )
		  => HexadecimalFloatingConstant1
                  { _ttype = FloatingPointNumber; }
	|       ( HexadecimalPrefix '.' ( HexadecimalDigit )+ )
		  => HexadecimalFloatingConstant2
                  { _ttype = FloatingPointNumber; }
	|       ( HexadecimalPrefix ( HexadecimalDigit )+ BinaryExponent )
		  => HexadecimalFloatingConstant3
                  { _ttype = FloatingPointNumber; }

	// Other
        |       ( "..." )=> "..."       { _ttype = VARARGS;     }

        |       '.'                     { _ttype = DOT; }

	// Integral Literals
        |       '0' ( '0'..'7' )*       
                ( IntSuffix | FloatSuffix )*

        |       '1'..'9' ( Digit )*     
                ( IntSuffix | FloatSuffix )*

        |       HexadecimalPrefix ( HexadecimalDigit )+
                ( IntSuffix )*
        ;

     

protected ID
        options 
                {
                testLiterals = true; 
                }
        :       ( 'a'..'z' | 'A'..'Z' | '_' | '$')
                ( 'a'..'z' | 'A'..'Z' | '_' | '$' | '0'..'9' )*
        ;


IDMEAT
        :
         i:ID
	   {
	     
	     if ( i.getType() == LITERAL___extension__ ) {
	       $setType(Token.SKIP);
	     }
	     else {
	       $setType(i.getType());
	     }
             
	   }
        ;

