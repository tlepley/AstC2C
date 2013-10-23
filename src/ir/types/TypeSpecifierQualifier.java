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

/* Type specifier and qualifier list used of the parser */

package ir.types;

import ir.base.NodeAST;
import ir.types.c.Array;
import ir.types.c.Bool;
import ir.types.c.FloatingPointScalar;
import ir.types.c.IntegerScalar;
import ir.types.c.Qualifier;
import ir.types.c.Void;
import ir.types.ocl.AddressSpace;

import common.CompilerError;


public class TypeSpecifierQualifier {

  // Type specifiers
  public enum BaseTypeSpecifier {
    NO,
    VOID,BOOL,CHAR,INT,FLOAT,DOUBLE,
    VECTOR,
    COMPLEX,IMAGINARY,
    STRUCT,UNION,ENUM,
    TYPEDEFNAME,TYPEOF,
    VALIST
  };
  public enum SignSpecifier {
    NO, SIGNED, UNSIGNED
  };
  public enum SizeSpecifier {
    NO, SHORT, LONG, LONG_LONG
  };

  // Type qualifier
  private boolean const_type_qualifier       = false;
  private boolean volatile_type_qualifier    = false;
  private boolean restrict_type_qualifier    = false;
  private AddressSpace address_space = AddressSpace.NO;

  //Type specifiers
  private BaseTypeSpecifier base_type_specifier = BaseTypeSpecifier.NO;
  private SizeSpecifier size_type_specifier 	= SizeSpecifier.NO;
  private SignSpecifier sign_type_specifier 	= SignSpecifier.NO;


  // Sub-type for tags, typedefnames and typeof 
  private Type sub_type = null; // type



  //==================================================================
  // Type specifier management
  //==================================================================

  // Sets the sub-type of the specifier: type of a type tag, a typedefnames
  // or a typeof 
  public void setSubType(Type t) {
    sub_type=t;
  }


  // Check if no sign and size specifier
  private void CheckNoSizeSignTypeSpecifier(NodeAST tn,CompilerError cp) {
    if (
	(size_type_specifier!=SizeSpecifier.NO) ||
	(sign_type_specifier!=SignSpecifier.NO)
	) {
      cp.raiseError(tn,"long, short, signed or unsigned invalid");
    }
  }


  // Check specifiers and return the type
  public Type getType(NodeAST tn,CompilerError cp) {
    Type type=null;

    switch(base_type_specifier) {
    case VOID:
      CheckNoSizeSignTypeSpecifier(tn,cp);
      type=Void.Tvoid;
      break;
      
    case BOOL:
      CheckNoSizeSignTypeSpecifier(tn,cp);
      type=Bool.Tbool;
      break;
      
    case CHAR:
      if (size_type_specifier!=SizeSpecifier.NO) {
	cp.raiseError(tn,"long or short specified with char");
      } 
      if(sign_type_specifier==SignSpecifier.UNSIGNED) {
	type=IntegerScalar.Tuchar;
      }
      else {
	type=IntegerScalar.Tschar;
      }
      break;
    
    case NO:
    case INT:
      // Manage the base type specifier
      switch(size_type_specifier) {
      case SHORT:
	if(sign_type_specifier==SignSpecifier.UNSIGNED) {
	  type=IntegerScalar.Tushort;
	}
	else {
	  type=IntegerScalar.Tsshort;
	}
	break;
      case LONG:
	if(sign_type_specifier==SignSpecifier.UNSIGNED) {
	  type=IntegerScalar.Tulong;
	}
	else {
	  type=IntegerScalar.Tslong;
	}
	break;
      case LONG_LONG:
	if (!Type.getSourceABI().isLongLongAllowed()) {
	  cp.raiseError(tn,"long long not allowed");
	}
	if(sign_type_specifier==SignSpecifier.UNSIGNED) {
	  type=IntegerScalar.Tulonglong;
	}
	else {
	  type=IntegerScalar.Tslonglong;
	}
	break;
      default:
	if(sign_type_specifier==SignSpecifier.UNSIGNED) {
	  type=IntegerScalar.Tuint;
	}
	else {
	  type=IntegerScalar.Tsint;
	}
      }
      break;
      
    case FLOAT:
      if (size_type_specifier!=SizeSpecifier.NO) {
	cp.raiseError(tn,"long or short specified with floating type, the only valid combination is `long double'");
      }
      if (sign_type_specifier!=SignSpecifier.NO) {
	cp.raiseError(tn,"signed or unsigned invalid with floating type");
      }
      type=FloatingPointScalar.Tfloat;
      break;
      
    case DOUBLE:
      if ((size_type_specifier!=SizeSpecifier.NO)&&(size_type_specifier!=SizeSpecifier.LONG)) {
	cp.raiseError(tn,"long or short specified with floating type, the only valid combination is `long double'");
      }
      if (sign_type_specifier!=SignSpecifier.NO) {
	cp.raiseError(tn,"signed or unsigned invalid with floating type");
      }
      if (size_type_specifier==SizeSpecifier.LONG) {
	if (!Type.getSourceABI().isLongDoubleAllowed()) {
	  cp.raiseError(tn,"long double not allowed");
	}
	type=FloatingPointScalar.Tlongdouble;
      }
      else {
//	if (!Type.getSourceABI().isDoubleAllowed()) {
//	  cp.raiseError(tn,"double not allowed");
//	}
	type=FloatingPointScalar.Tdouble;
      }
      // Sets the specifier type
      break;
    
    case VECTOR:
    case STRUCT:
    case UNION:
    case ENUM:
     CheckNoSizeSignTypeSpecifier(tn,cp);
     type=sub_type;
     break;
      
    case TYPEOF:
    case TYPEDEFNAME:
      // Sign and size specifiers (which are type specifiers) are not
      // allowed with a typedefname (itself a type specifier)
      if (
	  (size_type_specifier!=SizeSpecifier.NO) || 
	  (sign_type_specifier!=SignSpecifier.NO)
	  ) {
	if (base_type_specifier==BaseTypeSpecifier.TYPEOF) {
	  cp.raiseError(tn,"type specifier not allowed with a typeof");
	}
	else {
	  cp.raiseError(tn,"type specifier not allowed with a typedef name");
	}
      }
      // Other type specifiers (base data type) have been already filtered

      // First take the sub_type as base type
      type=sub_type;

      // Specific case for array
      if (type instanceof Array) {
	Array array=(Array)type;
	if (!array.hasSizeDefined()) {
	  // Clone the Array object. This is to manage the case when an
	  // incomplete array is declared as typedef. This typedef can
	  // be used to declare arrays which have different size setting
	  // through their initializer:
	  //
	  //   typedef int T[];
	  //   T tab1= {1, 2};
	  //   T tab2= {3, 4, 5};
	  type=array.clone();
	}
      }

      // Merge type qualifiers
      if (
	  (const_type_qualifier   !=false) || 
	  (volatile_type_qualifier!=false) || 
	  (restrict_type_qualifier!=false) || 
	  (address_space!=AddressSpace.NO)
	  ) {
	Qualifier new_qualifier;
	
	if (type instanceof Qualifier) {
	  // Clone the qualifier to merge before merging
	  new_qualifier=(Qualifier)(((Qualifier)type).clone());
	}
	else {
	  // Create a new qualifier
	  new_qualifier=new Qualifier(type);
	}
	
	// Merge qualifiers
	if (const_type_qualifier) {
	  new_qualifier.setConst(tn,cp);
	}
	if (volatile_type_qualifier) {
	  new_qualifier.setVolatile(tn,cp);
	}
	if (restrict_type_qualifier) {
	  new_qualifier.setRestrict(tn,cp);
	}
	if (address_space!=AddressSpace.NO) {
	  new_qualifier.setAddressSpace(tn,cp,address_space);
	}

	// Sets the type
	type=(Type)new_qualifier;
      }
      return type;
      
    default:
      // No processing yet (complex, imaginary, valist)
      // -> int by default
      // [TBW]
      type=IntegerScalar.Tsint;
     }

    // Manage type qualifiers (except for typename)
    if (
	(const_type_qualifier   !=false) || 
	(volatile_type_qualifier!=false) || 
	(restrict_type_qualifier!=false) || 
	(address_space!=AddressSpace.NO)
	) {
      Qualifier qualifier=new Qualifier(type);
      // Merge qualifiers
      if (const_type_qualifier) {
	qualifier.setConst(tn,cp);
      }
      if (volatile_type_qualifier) {
	qualifier.setVolatile(tn,cp);
      }
      if (restrict_type_qualifier) {
	qualifier.setRestrict(tn,cp);
      }
      if (address_space!=AddressSpace.NO) {
	qualifier.setAddressSpace(tn,cp,address_space);
      }
      
      type=(Type)qualifier;
    }
    return type;
  }


  // On the fly checks
  public void checkSign(NodeAST tn,CompilerError cp) {
    if (base_type_specifier!=BaseTypeSpecifier.NO) {
       cp.raiseError(tn,"multiple data type in specifier");
    }
  }
  public void checkMultipleDataType(NodeAST tn,CompilerError cp) {
    if (base_type_specifier!=BaseTypeSpecifier.NO) {
       cp.raiseError(tn,"multiple data type in specifier");
    }
  }

  // 'typeof' type specifier
  public void setTypeof(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.TYPEOF;
  }
  // 'typedef name' type specifier
  public void setTypedefName(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.TYPEDEFNAME;
  }
  // 'void' type specifier
  public void setVoid(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.VOID;
  }
  // '_Bool' type specifier
  public void setBool(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.BOOL;
  }
  // 'char' type specifier
  public void setChar(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.CHAR;
  }
  // 'int' type specifier
  public void setInt(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.INT;
  }
  // 'float' type specifier
  public void setFloat(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.FLOAT;
  }
  // 'double' type specifier
  public void setDouble(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.DOUBLE;
  }
  // vector type specifier
  public void setVector(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.VECTOR;
  }
  // 'complex' type specifier
  public void setComplex(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.COMPLEX;
  }
  // 'imaginary' type specifier
  public void setImaginary(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.IMAGINARY;
  }
  // 'struct' type specifier
  public void setStruct(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.STRUCT;
  }
  // 'union' type specifier
  public void setUnion(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.UNION;
  }
  // 'enum' type specifier
  public void setEnum(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.ENUM;
  }
  // 'valist' type specifier
  public void setValist(NodeAST tn,CompilerError cp) {
    checkMultipleDataType(tn,cp);
    base_type_specifier=BaseTypeSpecifier.VALIST;
  }

  // 'short' type specifier
  public void setShort(NodeAST tn,CompilerError cp) {
    if (size_type_specifier==SizeSpecifier.SHORT) {
      cp.raiseWarning(tn,"duplicate 'short'");
    }
    else if (
	     (size_type_specifier==SizeSpecifier.LONG) ||
	     (size_type_specifier==SizeSpecifier.LONG_LONG)
	     ) {
      cp.raiseError(tn,"both long and short specified");
    }
    size_type_specifier=SizeSpecifier.SHORT;
  }
  // 'long' type specifier
  public void setLong(NodeAST tn,CompilerError cp) {
    if (size_type_specifier==SizeSpecifier.SHORT) {
      cp.raiseError(tn,"both long and short specified");
    }
    else if (size_type_specifier==SizeSpecifier.LONG_LONG) {
      cp.raiseError(tn,"'long long long' is too long");
    }
    else if (size_type_specifier==SizeSpecifier.LONG) {
      size_type_specifier=SizeSpecifier.LONG_LONG;
    }
    else {
      size_type_specifier=SizeSpecifier.LONG;
    }
  }

  // 'signed' type specifier
  public void setSigned(NodeAST tn,CompilerError cp) {
    if (sign_type_specifier==SignSpecifier.SIGNED) {
      cp.raiseWarning(tn,"duplicate 'signed'");
    }
    else if (sign_type_specifier==SignSpecifier.UNSIGNED) {
      cp.raiseError(tn,"both signed and unsigned specified");
    }
    sign_type_specifier=SignSpecifier.SIGNED;
  }
  // 'unsigned' type specifier
  public void setUnsigned(NodeAST tn,CompilerError cp) {
    if (sign_type_specifier==SignSpecifier.UNSIGNED) {
      cp.raiseWarning(tn,"duplicate 'unsigned'");
    }
    else if (sign_type_specifier==SignSpecifier.SIGNED) {
      cp.raiseError(tn,"both signed and unsigned specified");
    }
    sign_type_specifier=SignSpecifier.UNSIGNED;
  }


  // 'const' type qualifier
  public void setConst(NodeAST tn,CompilerError cp) {
    if (const_type_qualifier) {
      cp.raiseWarning(tn,"duplicate 'const'");
    }
    const_type_qualifier=true;
  }
  // 'volatile' type qualifier
  public void setVolatile(NodeAST tn,CompilerError cp) {
    if (volatile_type_qualifier) {
      cp.raiseWarning(tn,"duplicate 'volatile'");
    }
    volatile_type_qualifier=true;
  }
  // 'restrict' type qualifier
  public void setRestrict(NodeAST tn,CompilerError cp) {
    if (restrict_type_qualifier) {
      cp.raiseWarning(tn,"duplicate 'restrict'");
    }
    restrict_type_qualifier=true;
  }
  // OCL: address space qualifier
  public void setAddressSpace(NodeAST tn,CompilerError cp, AddressSpace as) {
    if (as==address_space) {
      StringBuffer sb=new StringBuffer();
      sb.append("duplicate `").append(as.getName()).append("'");
      cp.raiseWarning(tn,sb.toString());
    }
    else {
      if (address_space!=AddressSpace.NO) {
	cp.raiseError(tn,"More than one address space qualifier specified");
      }
      else {
	address_space=as;
      }
    }
  }



  //==================================================================
  // Query of Type specifiers
  //==================================================================


  // 'void' type specifier
  public boolean isTypedefName() {
    return(base_type_specifier==BaseTypeSpecifier.TYPEDEFNAME);
  }
  public boolean isVoid() {
    return(base_type_specifier==BaseTypeSpecifier.VOID);
  }
  // '_Bool' type specifier
  public boolean isBool() {
    return(base_type_specifier==BaseTypeSpecifier.BOOL);
  }
  // 'char' type specifier
  public boolean isChar() {
    return(base_type_specifier==BaseTypeSpecifier.CHAR);
  }
  // 'int' type specifier
  public boolean isInt() {
    return(base_type_specifier==BaseTypeSpecifier.INT);
  }
  // 'float' type specifier
  public boolean isFloat() {
    return(base_type_specifier==BaseTypeSpecifier.FLOAT);
  }
  // 'double' type specifier
  public boolean isDouble() {
    return(base_type_specifier==BaseTypeSpecifier.DOUBLE);
  }
  // vector type specifier
  public boolean isVector() {
    return(base_type_specifier==BaseTypeSpecifier.VECTOR);
  }
  // 'complex' type specifier
  public boolean isComplex() {
    return(base_type_specifier==BaseTypeSpecifier.COMPLEX);
  }
  // 'imaginary' type specifier
  public boolean isImaginary() {
    return(base_type_specifier==BaseTypeSpecifier.IMAGINARY);
  }
  // 'struct' type specifier
  public boolean isStruct() {
    return(base_type_specifier==BaseTypeSpecifier.STRUCT);
  }
  // 'union' type specifier
  public boolean isUnion() {
    return(base_type_specifier==BaseTypeSpecifier.UNION);
  }
  // 'enum' type specifier
  public boolean isEnum() {
    return(base_type_specifier==BaseTypeSpecifier.ENUM);
  }
  // 'valist' type specifier
  public boolean isValist() {
    return(base_type_specifier==BaseTypeSpecifier.VALIST);
  }

  // 'short' type specifier
  public boolean isShort() {
    return(size_type_specifier==SizeSpecifier.SHORT);
  }
  // 'long' type specifier
  public boolean isLong() {
    return(size_type_specifier==SizeSpecifier.LONG);
  }
  // 'long' type specifier
  public boolean isLongLong() {
    return(size_type_specifier==SizeSpecifier.LONG_LONG);
  }
  // 'signed' type specifier
  public boolean isSigned() {
    return((sign_type_specifier==SignSpecifier.SIGNED)||(sign_type_specifier==SignSpecifier.NO));
  }
  // 'unsigned' type specifier
  public boolean isUnsigned() {
    return(sign_type_specifier==SignSpecifier.UNSIGNED);
  }


  // 'const' type qualifier
  public boolean isConst() {
     return const_type_qualifier;
  }
  // 'volatile' type qualifier
  public boolean isVolatile() {
     return volatile_type_qualifier;
  }
  // 'restrict' type qualifier
  public boolean isRestrict() {
     return restrict_type_qualifier;
  }
  public boolean isAddressSpace() {
    return address_space!=AddressSpace.NO;
  }
  public AddressSpace getAddressSpace() {
    return address_space;
  }

}
