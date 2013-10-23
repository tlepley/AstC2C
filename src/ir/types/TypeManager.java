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

/* OpenCL C type checking function */

package ir.types;

import abi.ABI;
import ir.symboltable.symbols.*;
import ir.types.c.ChildType;
import ir.types.c.FloatingPointScalar;
import ir.types.c.Function;
import ir.types.c.IntegerScalar;
import ir.types.c.StructOrUnion;
import ir.types.ocl.AddressSpace;
import ir.types.ocl.IntegerVector;
import ir.types.ocl.Vector;
import common.CompilerError;
import java.util.LinkedList;
import parser.SymbolTableBuilder;
import parser.SymbolTableBuilderTokenTypes;
import antlr.collections.AST;
import ir.base.EnrichedType;
import ir.base.NodeAST;

import java.math.BigInteger;


public class TypeManager {

  // ##################################################################
  // Input language management
  // ##################################################################

  private boolean oclLanguage = false;
  private boolean vxLanguage = false;

  public void setOclLanguage() {
    oclLanguage=true;
  }
  public void setVxLanguage() {
    vxLanguage=true;
  }


  // ##################################################################
  // Type conversion management
  // ##################################################################

  public static void addConvertNode(Type dest_type, NodeAST e_right) {
    NodeAST converted=new NodeAST(e_right);
    EnrichedType etype=new EnrichedType(dest_type);
    // The original noed becomes the conversion
    ((AST)e_right).setType(SymbolTableBuilderTokenTypes.NConvert);
    e_right.setDataType(etype);
    ((AST)e_right).setFirstChild(converted);
  }



  // ##################################################################
  // Miscelaneous Type checking
  // ##################################################################

  public static void checkChildAsFunction(NodeAST id_node, CompilerError cp,
      ChildType parent) {
    if (parent.isFunction()) {
      // Not necessarily the symbol which is declared as function
      // returning function, but it is in its declaration
      // Note: gcc does the same simplification
      cp.raiseError((NodeAST)id_node,
          "function `"+id_node.getText()+
          "' declared as function returning a function");
    }
    if (parent.isArray()) {
      // Not necessarily the symbol which is declared as array of
      // function, but it is in its declaration
      // Note: gcc does the same simplification
      cp.raiseError((NodeAST)id_node,
          "declaration of `"+id_node.getText()+
          "' as array of functions");
    }
  }
  public static void checkChildAsFunctionAbstract(NodeAST node, CompilerError cp,
      ChildType parent) {
    if (parent.isFunction()) {
      cp.raiseError((NodeAST)node,
          "declaration of a function returning a function");
    }
    if (parent.isArray()) {
      cp.raiseError((NodeAST)node,
          "declaration of an array of functions");
    }
  }

  public static void checkChildAsArray(NodeAST id_node, CompilerError cp,
      ChildType parent) {
    if (parent.isFunction()) {
      // Not necessarily the symbol which is declared as function
      // returning function, but it is in its declaration
      // Note: gcc does the same simplification
      cp.raiseError((NodeAST)id_node,
          "function `"+id_node.getText()+
          "' declared as function returning an array");
    }
  }
  public static void checkChildAsArrayAbstract(NodeAST node, CompilerError cp,
      ChildType parent) {
    if (parent.isFunction()) {
      cp.raiseError((NodeAST)node,
          "declaration of a function returning an array");
    }
  }



  // ##################################################################
  // Expression type management
  // ##################################################################


  // ******************************************************************
  // getIntegralCommonTypeNoQualifier :
  //
  // Check that operands are integral (raise a fatal error otherwise),
  // promote operands and return the resulting type
  // Note: It assumes that operand types are not qualified
  // ******************************************************************
  private static Type getIntegralCommonTypeNoQualifier(Type left, Type right) {
    // Here, no pointer nor float, this is arithmetic statement

    // Performs operands promotion (only remaining int, long int, long long int,
    // float, double, long double)
    left =left.promote();
    right=right.promote();

    // Step 2: Integer operands
    if ( (right==IntegerScalar.Tulonglong) || (left==IntegerScalar.Tulonglong)) {
      return IntegerScalar.Tulonglong;
    }
    if ( (right==IntegerScalar.Tslonglong) || (left==IntegerScalar.Tslonglong)) {
      return IntegerScalar.Tslonglong;
    }
    if ( (right==IntegerScalar.Tulong) || (left==IntegerScalar.Tulong)) {
      return IntegerScalar.Tulong;
    }
    if ( (right==IntegerScalar.Tslong) || (left==IntegerScalar.Tslong)) {
      return IntegerScalar.Tslong;
    }
    if ( (right==IntegerScalar.Tuint) || (left==IntegerScalar.Tuint)) {
      return IntegerScalar.Tuint;
    }
    return IntegerScalar.Tsint;
  }

  // ******************************************************************
  // getArithmeticCommonTypeNoQualifier :
  //
  // Check that operands are arithmetic (raise a fatal error otherwise),
  // promote operands and return the resulting type
  // Note: It assumes that operand types are not qualified
  // ******************************************************************
  public static Type getArithmeticCommonTypeNoQualifier(Type left, Type right) {
    // Here, no pointer, this is arithmetic statement

    // Performs operands promotion (only remaining int, long int, long long int,
    // float, double, long double)
    left =left.promote();
    right=right.promote();

    // Step 1: Floating points operands
    if (right.isFloatingPointScalar()) {
      if (left.isFloatingPointScalar()) {
        if (
            (right==FloatingPointScalar.Tlongdouble) ||
            (left==FloatingPointScalar.Tlongdouble)
            ) {
          return FloatingPointScalar.Tlongdouble;
        }
        if (
            (right==FloatingPointScalar.Tdouble) ||
            (left==FloatingPointScalar.Tdouble)
            ) {
          return FloatingPointScalar.Tdouble;
        }
        // By default, return a float
        return FloatingPointScalar.Tfloat;
      }
      else {
        // The integer type must be converted to floating point
        // [Node insertion TBW]
        return right;
      }
    }
    else {
      if (left.isFloatingPointScalar()) {
        // The integer type must be converted to floating point
        // [Node insertion TBW]
        return left;
      }
      // Here, only integer operands
    }


    // Step 2: Integral operands
    return getIntegralCommonTypeNoQualifier(left,right);
  }



  // ******************************************************************
  // isDownScalarConversion :
  //
  // Returns true if transforming a a scalar to the base type of a
  // vector may result in a down conversion
  // Note: Down conversion is considered here as reducing the size, in
  //       bits of the data type. For exmaple, conversion from
  //       uint to int and reversly is not considered as a down
  //       conversion
  // ******************************************************************
  public static boolean isDownScalarConversion(CompilerError cp,
      Vector vectorType, Type scalarType) {
    Type vectorBaseType=vectorType.getVectorBaseType();

    // We consider the pointer type as a int, which may be wrong
    // [TBW] -> with target specific information
    if (scalarType.isPointerOrLabel()) {
      scalarType=IntegerScalar.Tsint;
    }

    // Integral scalar
    if (scalarType.isIntegralScalar()) {
      if (vectorBaseType.isFloatingPointScalar()) {
        return false;
      }
      // Else, it is an integral vector
      else if (vectorBaseType.isSchar()) {
        if (!scalarType.isSchar()) {
          return true;
        }
        return false;
      }
      else if (vectorBaseType.isUchar()) {
        if (!scalarType.isCharScalar()) {
          return true;
        }
        return false;
      }
      else if (vectorBaseType.isSshort()) {
        if ( scalarType.isUshort()    ||
            scalarType.isIntScalar() ||
            scalarType.isLongScalar() ) {
          return true;
        }
        return false;
      }
      else if (vectorBaseType.isUshort()) {
        if ( scalarType.isIntScalar() ||
            scalarType.isLongScalar() ) {
          return true;
        }
        return false;
      }
      else if (vectorBaseType.isSint()) {
        if (scalarType.isUint() || scalarType.isLongScalar()) {
          return true;
        }
        return false;
      }
      else if (vectorBaseType.isUint()) {
        if (scalarType.isLongScalar()) {
          return true;
        }
        return false;
      }
      else if (vectorBaseType.isSlong()) {
        if (scalarType.isUlong()) {
          return true;
        }
        return false;
      }
      else if (vectorBaseType.isUlong()) {
        return false;
      }
      else {
        // Should never come here
        cp.raiseInternalError("isDownScalarConversion 1");
        return true;
      }
    }

    // Floating point scalar
    else if (scalarType.isFloatingPointScalar()) {
      if (vectorBaseType.isFloatingPointScalar()) {
        return false;
      }
      else if (vectorBaseType.isIntegerScalar()) {
        return true;
      }
      else {
        // Should never come here
        cp.raiseInternalError("isDownScalarConversion 2");
        return true;
      }
    }
    else {
      // Should never come here
      cp.raiseInternalError("isDownScalarConversion 3");
      return true;
    } 
  }



  // ******************************************************************
  //  getTypeAdditiveBinaryOperator:
  //
  // Check that operands of a + or - operation are coherent (raise a
  // fatal error otherwise), promote operands and return the resulting
  // type
  // Note: If the type is a qualified pointer, the qualification is
  //       propagated
  // ******************************************************************
  public Type getTypeAdditiveBinaryOperator(NodeAST node, CompilerError cp,
      SymbolTableBuilder stb,
      Type l, Type r, boolean is_minus) {
    Type left, right;

    // Remove potential qualifier
    left  = l.unqualify();
    right = r.unqualify();

    // From void cast
    if (left.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return right;
    }
    if (right.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return left;
    }

    // Check for operand type conformance
    // Check operands
    if ( (!left.isScalarOrLabel() && (!left.isVector())) ||
        (!right.isScalarOrLabel() && (!right.isVector())) 
        ) {
      cp.raiseFatalError(node,"invalid operands to binary " + node.getText());
    }

    // Manage vectors
    if (right.isVector()) {
      Type tmp=left;left=right;right=tmp;
    }
    if (left.isVector()) {
      if (right.isVector()) {
        // Implicit conversion of a vector into an other vector is not allowed
        // Check that both types are the same
        if (right!=left) {
          cp.raiseError(node,"invalid operands to binary " + node.getText() + " ([OCL] no implicit conversions between vectors)");
        }
        return left;
      }
      else if (right.isScalarOrLabel()) {
        // Implicit conversions are allowed from scalar to vectors
        if (isDownScalarConversion(cp,(Vector)left,right)) {
          // Implicit down-conversions are allowed from scalar to vectors
          cp.raiseError(node,"invalid operands to binary " + node.getText() + " ([OCL] no implicit scalar down-conversion to vector)");
        }
        else {
          // Potentialy raise warning
          checkAssignOperands(node, cp,
              left.getVectorBaseType(),
              right,
              "implicit scalar conversion to vector"
              );
        }
        return left;
      }
      else {
        cp.raiseFatalError(node,"invalid operands to binary " + node.getText());
        return null;
      }
    }

    // Manage pointers
    if (right.isPointerOrLabel()) {
      Type tmp=left;left=right;right=tmp;
    }
    if (left.isPointerOrLabel()) {
      if (right.isPointerOrLabel()) {
        if (is_minus) {
          // Subtraction of two pointers is allowed, it gives ptrdiff_t
          return stb.getType_ptrdiff_t();
        }
        else {
          cp.raiseFatalError(node,"invalid operands to binary " + node.getText());
        }
      }
      else {
        // The resulting type is a pointer

        // Check for correct pointer arithmetic
        if (left.isPointer()) {
          if (left.getPointedType().isIncomplete()) {
            cp.raiseFatalError(node,"arithmetic of pointer to incomplete type");
          }
        }
        else if (left.isArray()) {
          // Should never happen since arrays are checked at declaration
          if (left.getElementType().isIncomplete()) {
            cp.raiseFatalError(node,"arithmetic of pointer to incomplete type");
          }
        }
        // else, its a function prototype (pointer to code)
        // No error

        return left;
      }
    }

    // Here, no pointer, this is arithmetic statement
    // -> no need to propagate the qualifier
    return getArithmeticCommonTypeNoQualifier(left,right);
  }


  // ******************************************************************
  // getTypeArithmeticBinaryOperator :
  //
  // Check that operands of an arithmetic binary operation are coherent
  // (raise a fatal error otherwise), promote operands and return the
  // resulting type
  // Note: potential qualifiers of operands are not propagated
  // ******************************************************************
  public Type getTypeArithmeticBinaryOperator(NodeAST node, CompilerError cp,
      Type l, Type r) {
    Type left, right;

    // Remove potential qualifier
    left  = l.unqualify();
    right = r.unqualify();

    // From void cast
    if (left.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return right;
    }
    if (right.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return left;
    }


    // Manage vectors
    if (right.isVector()) {
      Type tmp=left;left=right;right=tmp;
    }
    if (left.isVector()) {
      if (right.isVector()) {
        // Implicit conversion of a vector into an other vector is not allowed
        // Check that both types are the same
        if (right!=left) {
          cp.raiseError(node,"invalid operands to binary " + node.getText() + " ([OCL] no implicit conversions between vectors)");
        }
        return left;
      }
      else if (right.isScalarOrLabel()) {
        // Implicit conversions are allowed from scalar to vectors
        if (isDownScalarConversion(cp,(Vector)left,right)) {
          // Implicit down-conversions are allowed from scalar to vectors
          cp.raiseError(node,"invalid operands to binary " + node.getText() + " ([OCL] no implicit scalar down-conversion to vector)");
        }
        else {
          // Potentially raise warning
          checkAssignOperands(node, cp,
              left.getVectorBaseType(),
              right,
              "implicit scalar conversion to vector"
              );
        }
        return left;
      }
      else {
        cp.raiseFatalError(node,"invalid operands to binary " + node.getText());
        return null;
      }
    }


    // Check for operand type conformance
    if ( (!left.isArithmeticScalar()) ||
        (!right.isArithmeticScalar()) 
        ) {
      cp.raiseFatalError(node,"invalid arithmetic operands to binary " + node.getText());
    }

    // Here operands are Arithmetic
    // -> no need to propagate the qualifier
    return getArithmeticCommonTypeNoQualifier(left,right);
  }


  // ******************************************************************
  // getTypeShiftOperator :
  //
  // Check that operands of a shift operation are coherent (raise a
  // fatal error otherwise), promote operands and return the resulting
  // type
  // Note: potential qualifiers of operands are not propagated
  // ******************************************************************
  public Type getTypeShiftOperator(NodeAST node, CompilerError cp,
      Type l, Type r) {
    Type left, right;

    // Remove potential qualifier
    left  = l.unqualify();
    right = r.unqualify();

    // From void cast
    if (left.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return left;
    }
    if (right.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return left;
    }

    //----------------------
    //---  Vector shift  ---
    //----------------------

    if (left.isVector()) {
      if (right.isVector()) {
        // Both operands must be integral
        if ( (!left.isIntegralVector()) ||
            (!right.isIntegralVector()) 
            ) {
          cp.raiseFatalError(node,"invalid integral vector operands to binary " + node.getText());
          return null;
        }

        // [TBW] We make this test to avoid creating too many combinations of vector types for
        // shifts, but the spec allows  char4 << int4
        // -> Only the number of elements in vectors should be checked
        if (right!=left) {
          cp.raiseError(node,"invalid operands to binary " + node.getText() + " ([OCL] no implicit conversions between vectors)");
        }
        return left;
      }
      else if (right.isScalarOrLabel()) {
        // Both operands must be integer (a pointer is not allowed as right operand)
        if ( (!left.isIntegralVector()) ||
            (!right.isIntegralScalar()) 
            ) {
          cp.raiseError(node,"invalid integer operands to binary " + node.getText());
        }

        // No down-scalar conversion check needed to the right operand, just a standard
        // promotion for the right operand
        return left;
      }
      else {
        // Implicit conversions are not allowed for built-in vector data types
        cp.raiseFatalError(node,"invalid integral operands to binary " + node.getText());
        return null;
      }
    }

    //----------------------
    //---  Scalar shift  ---
    //----------------------

    // Check operands
    if (right.isVector()) {
      // It is an error, since the vector right operand is only valid when the left operand
      // is itself a vector
      cp.raiseFatalError(node,"invalid operands to binary " + node.getText());
      return null;
    }
    if ( (!left.isIntegralScalar()) ||
        (!right.isIntegralScalar()) 
        ) {
      cp.raiseFatalError(node,"invalid integer operands to binary " + node.getText());
      return null;
    }

    // Promote the left operand
    return left.promote();
  }



  // ******************************************************************
  // getTypeIntegralBinaryOperator :
  //
  // Check that operands of an integral binary operation are coherent
  // (raise a fatal error otherwise), promote operands and return the
  // resulting type
  // Note: potential qualifiers of operands are not propagated
  // ******************************************************************
  public Type getTypeIntegralBinaryOperator(NodeAST node, CompilerError cp,
      Type l, Type r) {
    Type left, right;

    // Remove potential qualifier
    left  = l.unqualify();
    right = r.unqualify();

    // From void cast
    if (left.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return right;
    }
    if (right.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return left;
    }


    // Manage vectors
    if (right.isVector()) {
      Type tmp=left;left=right;right=tmp;
    }
    if (left.isVector()) {
      if (right.isVector()) {
        // Both operands must be integral
        if ( (!left.isIntegralVector()) ||
            (!right.isIntegralVector()) 
            ) {
          cp.raiseFatalError(node,"invalid integral vector operands to binary " + node.getText());
          return null;
        }

        // Implicit conversion of a vector into an other vector is not allowed
        // -> check that types are the same
        if (right!=left) {
          cp.raiseError(node,"invalid operands to binary " + node.getText() + " ([OCL] no implicit conversions between vectors)");
        }
        return left;
      }
      else if (right.isScalarOrLabel()) {
        // Both operands must be integral
        if ( (!left.isIntegralVector()) ||
            (!right.isIntegralScalar()) 
            ) {
          cp.raiseError(node,"invalid integral operands to binary " + node.getText());
        }

        // Implicit conversions are allowed from scalar to vectors
        if (isDownScalarConversion(cp,(Vector)left,right)) {
          // Implicit down-conversions are allowed from scalar to vectors
          cp.raiseError(node,"invalid operands to binary " + node.getText() + " ([OCL] no implicit scalar down-conversion to vector)");
        }
        else {
          // Potentialy raise warning
          checkAssignOperands(node, cp,
              left.getVectorBaseType(),
              right,
              "implicit scalar conversion to vector"
              );
        }
        return left;
      }
      else {
        // Implicit conversions are not allowed for built-in vector data types
        cp.raiseFatalError(node,"invalid integral operands to binary " + node.getText());
        return null;
      }
    }

    // Check operands
    if ( (!left.isIntegralScalar()) ||
        (!right.isIntegralScalar()) 
        ) {
      cp.raiseFatalError(node,"invalid integral operands to binary " + node.getText());
      return null;
    }

    // -> no need to propagate the qualifier
    return getIntegralCommonTypeNoQualifier(left,right);
  }


  // ******************************************************************
  // getTypeLogicalUnaryOperator :
  //
  // Check that the operand of a logical unary operator is valid
  // (raise an error otherwise), and return the resulting type
  // ******************************************************************
  public static Type getTypeLogicalUnaryOperator(NodeAST node, CompilerError cp,
      Type l, String s) {
    Type left= l.unqualify();

    if (left.isVector()) {
      // Return the right signed vector type
      if (left.isCharVector()) {
        return IntegerVector.getScharVector(((Vector)left).getNbElements());
      }
      else if (left.isShortVector()) {
        return IntegerVector.getSshortVector(((Vector)left).getNbElements());
      }
      else if (left.isIntVector()) {
        return IntegerVector.getSintVector(((Vector)left).getNbElements());
      }
      else if (left.isLongVector()) {
        return IntegerVector.getSlongVector(((Vector)left).getNbElements());
      }
      else if (left.isFloatVector()) {
        // Vector of floats forbidden in OpenCL C
        cp.raiseWarning("logical unary "+ node.getText()+" not allowed on float vectors in OpenCL C");
        return IntegerVector.getSintVector(((Vector)left).getNbElements());
      }
      else {
        cp.raiseInternalError("getTypeLogicalUnaryOperator");
        return left;
      }
    }
    else if (!left.isScalarOrLabel()) {
      cp.raiseError(node,"wrong type argument to logical unary "+s);
    }

    // The type is integer
    return IntegerScalar.Tsint;
  }



  // ******************************************************************
  // getTypeLogicalBinaryOperator :
  //
  // Check that operands of a logical binary operator is scalar or
  // vector (raise an error otherwise), and return the resulting type
  // ******************************************************************
  public Type getTypeLogicalBinaryOperator(NodeAST node, CompilerError cp,
      Type l, Type r,
      NodeAST e_left, NodeAST e_right) {
    // Remove potential qualifier
    Type left, right;
    left  = l.unqualify();
    right = r.unqualify();

    // From void cast
    if (left.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return IntegerScalar.Tsint;
    }
    if (right.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return IntegerScalar.Tsint;
    }

    // Manage vectors
    if (right.isVector()) {
      // Invert left and right
      Type tmp=left;left=right;right=tmp;
      NodeAST e_tmp=e_left;e_left=e_right;e_right=e_tmp;
    }
    if (left.isVector()) {
      if (right.isVector()) {
        // Implicit conversion of a vector into an other vector is not allowed
        // -> check that both types must be the same
        if (right!=left) {
          cp.raiseError(node,"invalid operands to logical binary " + node.getText() + " ([OCL] invalid implicit conversions between vectors)");
        }
      }
      else if (right.isScalarOrLabel()) {
        // Implicit conversions are allowed from scalar to vectors
        if (isDownScalarConversion(cp,(Vector)left,right)) {
          // Implicit down-conversions are allowed from scalar to vectors
          cp.raiseError(node,"invalid operands to logical binary " + node.getText() + " ([OCL] no implicit scalar down-conversion to vector)");
        }
        else {
          // Potentially raise warning
          checkAssignOperands(node, cp,
              left.getVectorBaseType(),
              right,
              "implicit scalar conversion to vector"
              );
          addConvertNode(left,e_right);
        }
      }
      else {
        cp.raiseError(node,"invalid operands to logical binary " + node.getText());
      }

      // Return the right signed vector type
      if (left.isCharVector()) {
        return IntegerVector.getScharVector(((Vector)left).getNbElements());
      }
      else if (left.isShortVector()) {
        return IntegerVector.getSshortVector(((Vector)left).getNbElements());
      }
      else if (left.isIntVector()) {
        return IntegerVector.getSintVector(((Vector)left).getNbElements());
      }
      else if (left.isLongVector()) {
        return IntegerVector.getSlongVector(((Vector)left).getNbElements());
      }
      else if (left.isFloatVector()) {
        // Vector of floats forbidden in OpenCL C
        cp.raiseWarning("logical binary "+ node.getText()+" not allowed on float vectors in OpenCL C");
        return IntegerVector.getSintVector(((Vector)left).getNbElements());
      }
      else {
        cp.raiseInternalError("getTypeLogicalBinaryOperator");
        return left;
      }
    }

    else if ( (!left.isScalarOrLabel()) ||
        (!right.isScalarOrLabel()) 
        ) {
      cp.raiseError(node,"invalid operands to logical binary " + node.getText());
    }

    // The type is integer
    return IntegerScalar.Tsint;
  }


  // ******************************************************************
  // getTypeRelationalOperator :
  //
  // Check that both operands of a relational operator are compatible
  // ((raise an error otherwise) and return the type of the expression
  // ******************************************************************
  public Type getTypeRelationalOperator(NodeAST node, CompilerError cp,
      EnrichedType el, EnrichedType er,
      NodeAST e_left, NodeAST e_right) {
    // NOTE: More tests should be done with pointers, in particular regarding
    // 'object compatibility' [TBW]

    // Remove potential qualifier
    Type left, right;
    left  = el.getType().unqualify();
    right = er.getType().unqualify();

    // From void cast
    if (left.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return IntegerScalar.Tsint;
    }
    if (right.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return IntegerScalar.Tsint;
    }

    if (right.isVector()) {
      // Invert left and right
      Type tmp=left;left=right;right=tmp;
      EnrichedType etmp=el;el=er;er=etmp;
      NodeAST e_tmp=e_left;e_left=e_right;e_right=e_tmp;
    }
    if (left.isVector()) {
      if (right.isVector()) {
        // Implicit conversion is not allowed for built-in vector data types
        // Both types must be the same
        if (right!=left) {
          cp.raiseError(node,"invalid operands to binary " + node.getText() +
              " ([OCL] invalid implicit conversions between vectors)");
        }
      }
      else if (right.isScalarOrLabel()) {
        // Implicit conversions are allowed from scalar to vectors
        if (isDownScalarConversion(cp,(Vector)left,right)) {
          // Implicit down-conversions are allowed from scalar to vectors
          cp.raiseError(node,"invalid operands to binary " + node.getText() +
              " ([OCL] no implicit scalar down-conversion to vector)");
        }
        else {
          // Potentially raise warning
          checkAssignOperands(node, cp,
              left.getVectorBaseType(),
              right,
              "implicit scalar conversion to vector"
              );
          addConvertNode(left,e_right);
        }
      }
      else {
        cp.raiseError(node,"invalid operands to binary " + node.getText());
      }

      // Return the right signed vector type
      if (left.isCharVector()) {
        return IntegerVector.getScharVector(((Vector)left).getNbElements());
      }
      else if (left.isShortVector()) {
        return IntegerVector.getSshortVector(((Vector)left).getNbElements());
      }
      else if (left.isIntVector() || left.isFloatVector()) {
        // Vectors authorized for relational operators
        return IntegerVector.getSintVector(((Vector)left).getNbElements());
      }
      else if (left.isLongVector()) {
        return IntegerVector.getSlongVector(((Vector)left).getNbElements());
      }
      else {
        cp.raiseInternalError("getTypeRelationalOperator");
      }
      // Should never come here
      return left;
    }

    else if (left.isArithmeticScalar()) {
      if (!right.isArithmeticScalar()) {
        // Both should be arithmetic, but a pointer can be compared to an integral
        if (left.isIntegralScalar() && right.isPointerOrLabel()) {
          if (!el.isConstantZero()) {
            cp.raiseWarning(node,"comparison between pointer and integer");
          }
        }
        else {
          cp.raiseError(node,"invalid operands to binary " + node.getText());
        }
      }
      // else OK
    }

    else if (left.isPointerOrLabel()) {
      if (!right.isPointerOrLabel()) {
        // Both should be pointer, but a pointer can be compared to an integral
        if (right.isIntegralScalar()) {
          if (!er.isConstantZero()) {
            cp.raiseWarning(node,"comparison between pointer and integer");
          }
        }
        else {
          cp.raiseError(node,"invalid operands to binary " + node.getText());
        }
      }
      //else {
      // Do not generate compatibility warning yet, because compatibility for function
      // prototypes and array is not just reference equality like for arithmetic types
      // struct or union
      // [TBW]
      //if (left.isPointer() && right.isPointer()) {
      //  if (left.getPointedType().unqualify()!=right.getPointedType().unqualify()) {
      //    cp.raiseWarning(node,"comparison of distinct pointer types lacks a cast");
      //  }
      //	}
      //else if (left.isArray() && right.isArray()) {
      //  if (left.getElementType().unqualify()!=right.getElementType().unqualify()) {
      //    cp.raiseWarning(node,"comparison of distinct pointer types lacks a cast");
      //  }
      //	}
      //	else if (left.isPointer() && right.isArray()) {
      //  if (left.getPointedType().unqualify()!=right.getElementType().unqualify()) {
      //    cp.raiseWarning(node,"comparison of distinct pointer types lacks a cast");
      //  }
      //}
      //else if (left.isArray() && right.isPointer()) {
      //  if (left.getElementType().unqualify()!=right.getPointedType().unqualify()) {
      //    cp.raiseWarning(node,"comparison of distinct pointer types lacks a cast");
      //  }
      //}
      //}
      // else OK
    }

    else {
      cp.raiseError(node,"invalid operands to binary " + node.getText());
    }

    // Relational on non-vector always returns signed int
    return IntegerScalar.Tsint;
  }


  // ******************************************************************
  // getTypeConditionalOperator :
  //
  // Check that operands of a conditional operator are
  // coherent (raise a fatal error otherwise), promote operands
  // and return the resulting type
  //
  // Note: qualifiers are not propagated
  // ******************************************************************
  public Type getTypeConditionalOperator(NodeAST node, CompilerError cp,
      Type t, Type l, Type r) {
    // NOTE: More tests should be done in particular regarding
    // 'object compatibility' [TBW]

    // Left optional (GNU-C ??)
    if (l==null) {
      // It will returns the type of the right operand
      l=r;
    }

    // Remove potential qualifier
    Type test, left, right;
    test  = t.unqualify();
    left  = l.unqualify();
    right = r.unqualify();

    if (right.isVector()) {
      Type tmp=left;left=right;right=tmp;
    }

    // Vector case
    if (left.isVector()) {
      if (right.isVector()) {
        // Implicit conversion is not allowed between vector data types
        // Both types must be exactly the same
        if (right!=left) {
          cp.raiseError(node,"type mismatch in conditional expression ([OCL] no implicit conversion between vectors)");
        }	
      }
      else if (right.isScalarOrLabel()) {
        // Implicit conversions are allowed from scalar to vectors
        if (isDownScalarConversion(cp,(Vector)left,right)) {
          // Implicit down-conversions are allowed from scalar to vectors
          cp.raiseError(node,"type mismatch in conditional expression ([OCL] no implicit scalar down-conversion to vector)");
        }
        else {
          // Potentialy raise warning
          checkAssignOperands(node, cp,
              left.getVectorBaseType(),
              right,
              "implicit scalar conversion to vector"
              );
        }
      }
      else {
        cp.raiseError(node,"type mismatch in conditional expression");
      }

      // The test operand must be a vector with the same size
      if (test.isVector()) {
        // Vectors must have the same size but do not have necessarily
        // the same base type
        // [TBW] What about the base type of the test, if it bigger
        //       than other vectors, we may loose information
        //       (down conversion) when generating code for the
        //       vector unit ...
        //       => to be clarified
        if (test.getNbVectorElements()!=left.getNbVectorElements()) {
          cp.raiseError(node,"type mismatch between the test and other operands in conditional expression ([OCL] not the same number of elements in vectors)");
        }
      }
      else if (test.isScalarOrLabel()) {
        // The test condition must be a scalar, so OK
        // This test condition must not be converted to vector
      }
      else {
        cp.raiseError(node,"invalid test operand to conditional operator");
      }

      return left;
    }

    else {
      // The test must be a scalar
      if (!test.isScalarOrLabel()) {
        cp.raiseError(node,"invalid test operand to conditional operator");
      }

      // Check other operands
      if (left.isArithmeticScalar()) {
        if (!right.isArithmeticScalar()) {
          // Both should be arithmetic, but a pointer can be compared to an integral
          if (left.isIntegralScalar() && (right.isPointerOrLabel())) {
            cp.raiseWarning(node,"pointer/integer type mismatch in conditional expression");
            // Returns the pointer type
            return right;
          }
          else {
            cp.raiseFatalError(node,"type mismatch in conditional expression");
            return null;
          }
        }
        // else OK
        return getArithmeticCommonTypeNoQualifier(left,right);
      }
      else if (left.isPointerOrLabel()) {
        if (!right.isPointerOrLabel()) {
          // Both should be pointer, but a pointer can be compared to an integral
          if (right.isIntegralScalar()) {
            cp.raiseWarning(node,"pointer/integer type mismatch in conditional expression");
            // Returns the pointer type
            return left;
          }
          else {
            cp.raiseFatalError(node,"type mismatch in conditional expression");
            return null;
          }
        }
        // else OK
        return left;
      }
      else if (left.isStruct()) {
        if (!right.isStruct()) {
          // Both should be a compatible structure
          cp.raiseFatalError(node,"type mismatch in conditional expression");
          return null;
        }
        else if (left!=right) {
          // It must be the same structure
          cp.raiseFatalError(node,"struct type mismatch in conditional expression");
          return null;
        }
        // else OK
        return left;
      }
      else if (left.isUnion()) {
        if (!right.isUnion()) {
          // Both should be a compatible structure
          cp.raiseFatalError(node,"type mismatch in conditional expression");
          return null;
        }
        else if (left!=right) {
          // It must be the same union
          cp.raiseFatalError(node,"union type mismatch in conditional expression");
          return null;
        }
        // else OK
        return left;
      }
      else if (left.isVoid()) {
        if (!right.isVoid()) {
          // Both should be void
          cp.raiseFatalError(node,"type mismatch in conditional expression");
          return null;
        }
        // else OK
        return left;
      }
      else {
        // Should not be correct [TBW]
        cp.raiseFatalError(node,"type mismatch in conditional expression");
        return null;
      }
    }
  }



  // ******************************************************************
  // getVectorElementEnrichedType :
  //
  // Check that the element decriptor is correct, that the generated
  // type if correct and return the resulting type
  //
  // ******************************************************************
  public static EnrichedType getVectorElementEnrichedType(NodeAST node, CompilerError cp,
      EnrichedType etype,
      Vector v,
      String element_string) {

    // ------------------------------------------------
    // + Check the validity of the elements description
    // + Compute the size of the new vector
    // ------------------------------------------------
    if (element_string.length()==0) {
      cp.raiseInternalError("Empty element descriptor for vectors");
      return null;
    }
    int vector_size=v.getNbElements();

    int new_size=0;
    char [] element_array;
    boolean isMultipleElementReference=false;

    if ((element_string.charAt(0)=='s')||(element_string.charAt(0)=='S')) {
      boolean[] elementReference=new boolean[vector_size];
      for (int i=0;i<elementReference.length;i++) {elementReference[i]=false;}

      // S style element indexing
      element_array=element_string.substring(1).toCharArray();
      for(char c:element_array) {
        int index=Vector.getSStyleIndex(c);
        if (index<0) {
          cp.raiseError(node,"invalid vector index '"+c+"' for s-style vector indexing");
        }
        else if (index>=vector_size) {
          cp.raiseError(node,"invalid vector index '"+c+"' for vector of size "+vector_size);
        }
        else {
          // Correct index
          if (elementReference[index]!=false) {
            isMultipleElementReference=true;
          }
          elementReference[index]=true;
        }

        // Increment the new size
        new_size++;
      }
    }
    else if (
        element_string.equals("lo")  ||
        element_string.equals("hi")  ||
        element_string.equals("odd") ||
        element_string.equals("even")
        ) {
      if (vector_size==3) {
        new_size=2;
      }
      else {
        // The size of the vector is divided by 2
        new_size=vector_size>>1;
      }
      // No multiple reference
      isMultipleElementReference=false;
    }
    else {
      boolean[] elementReference=new boolean[vector_size];
      for (int i=0;i<elementReference.length;i++) {elementReference[i]=false;}

      // It should be a geometric style indexing, which is valid only for vectors of size
      element_array=element_string.toCharArray();
      for(char c:element_array) {
        int index=Vector.getGeometricStyleIndex(c);
        if (index<0) {
          cp.raiseError(node,"invalid vector index '"+c+"' for geometric style vector indexing");
        }
        else if (index>=vector_size) {
          cp.raiseError(node,"invalid vector index '"+c+"' for vector of size "+vector_size);
        }
        else {
          // Correct index
          if (elementReference[index]!=false) {
            isMultipleElementReference=true;
          }
          elementReference[index]=true;
        }

        // Increment the new size
        new_size++;
      }
      // Geometric indexing style is not valid for vectors larger
      // than 4 elements
      if (vector_size>4) {
        cp.raiseError(node,"geometric style vector indexing not allowed for vectors of size "+vector_size);
      }

    }

    // ------------------------------------------------
    // Find the resulting type
    // ------------------------------------------------
    Type resulting_type=v.getEquivalentType(new_size);

    if (resulting_type==null) {
      // Non allowed vector size
      cp.raiseFatalError(node,""+new_size+" is not a valid size for vector type");
    }


    // ------------------------------------------------
    // Get the element list (potentially merging with
    // the preceding one)
    // ------------------------------------------------
    LinkedList<Integer> element_list=v.getElementList(element_string);
    LinkedList<Integer> new_element_list=element_list;
    LinkedList<Integer> old_element_list=etype.getVectorElementReference();
    if (old_element_list!=null) {
      // It was previously a swizzling operator
      new_element_list=new LinkedList<Integer>();
      for(int i : element_list) {
        new_element_list.add(old_element_list.get(i));
      }
    }
    //for(int i : new_element_list) {
    //  System.err.print(""+i+",");
    //}
    //System.err.println();


    // ------------------------------------------------
    // Create the resulting etype
    // ------------------------------------------------
    EnrichedType resulting_etype=new EnrichedType(resulting_type);

    // Set if there is a duplication in vector component reference
    // propaged info in element reference is applied on an other
    // element reference which has duplication
    if (etype.isVectorElementReferenceWithDuplication() ||
        isMultipleElementReference) {
      resulting_etype.setVectorElementReferenceWithDuplication(new_element_list);
    }
    else {
      resulting_etype.setVectorElementReference(new_element_list);
    }

    return resulting_etype;
  }



  // ******************************************************************
  // checkAssignOperands :
  //
  // Check that both operands of an assign operator are compatible
  // It raises an error in case of non correct check
  // ******************************************************************
  public void checkAssignOperands(NodeAST node, CompilerError cp,
      Type left, EnrichedType eright, String s) {
    Type right=eright.getType();
    if (left.isPointerOrLabel() &&
        right.isIntegralScalar() && eright.isConstantZero()) {
      // It is a common situation which should not generates warning
      // int *i=0;
      return;
    }
    checkAssignOperands(node,cp,left,right,s);
  }



  // ******************************************************************
  // checkAssignOperands :
  //
  // Check that both operands of an assign operator are compatible
  // It raises an error in case of non correct check
  // ******************************************************************
  public void checkAssignOperands(NodeAST node, CompilerError cp,
      Type left, Type right, String s) {
    // NOTE: More tests should be done in particular regarding
    // 'object compatibility' [TBW]

    // From void cast
    if (right.isVoid()) {
      cp.raiseError(node,"void value not ignored as it ought to be");
      return;
    }

    if (left.isVector()) {
      if (right.isVector()) {
        // Implicit conversion is not allowed for built-in vector data types
        // Check that both types are the same
        if (right.unqualify()!=left.unqualify()) {
          cp.raiseError(node,"incompatible types in "+s+" ([OCL] no implicit conversions between vectors)");
        }
      }
      else if (right.isScalarOrLabel()) {
        // Implicit conversions are allowed from scalar to vectors
        if (isDownScalarConversion(cp,(Vector)left.unqualify(),right)) {
          // Implicit down-conversions are allowed from scalar to vectors
          cp.raiseError(node,"incompatible types in "+s+" ([OCL] no implicit scalar down-conversion to vector)");
        }
        else {
          // Potentially raise warning
          checkAssignOperands(node, cp,
              left.getVectorBaseType(),
              right,
              "implicit scalar conversion to vector"
              );
        }
      }
      else {
        cp.raiseError(node,"incompatible types in "+s);
      }
    }
    else if (left.isArithmeticScalar()) {
      if (!right.isArithmeticScalar()) {
        // Both should be arithmetic, but a pointer can be compared to an integral
        if (left.isIntegralScalar() && (right.isPointerOrLabel())) {
          cp.raiseWarning(node,s+" makes integer from pointer without a cast");
        }
        else {
          cp.raiseError(node,"incompatible types in "+s);
        }
      }
      // else OK
    }
    else if (left.isPointerOrLabel()) {
      if (!right.isPointerOrLabel()) {
        // Both should be pointer, but a pointer can be compared to an integral
        if (right.isIntegralScalar()) {
          cp.raiseWarning(node,s+" makes pointer from integer without a cast");
        }
        else {
          cp.raiseError(node,"incompatible types in "+s);
        }
      }
      else {
        // [OCL] Check for address space coherence
        if (oclLanguage) {
          AddressSpace left_as, right_as;
          left_as=getPointerOrLabelAddressSpace(cp,left);
          right_as=getPointerOrLabelAddressSpace(cp,right);

          // By default, a non qualified object if private static in kernels
          // (except images which are global but not supported here)
          if (left_as==AddressSpace.NO) {
            left_as=AddressSpace.PRIVATE;
          }
          if (right_as==AddressSpace.NO) {
            right_as=AddressSpace.PRIVATE;
          }

          if (left_as!=right_as) {
            //cp.raiseError(node,"[OCL] incompatible address space for pointer types in "+s);
          }
        }
      }
    }
    else if (left.isStruct()) {
      if (!right.isStruct()) {
        // Both should be a compatible structure
        cp.raiseFatalError(node,"incompatible types in "+s);
      }
      else if (left.unqualify()!=right.unqualify()) {
        // It must be the same structure
        cp.raiseError(node,"incompatible struct types in "+s);
      }
      // else OK
    }
    else if (left.isUnion()) {
      if (!right.isUnion()) {
        // Both should be a compatible structure
        cp.raiseFatalError(node,"incompatible types in "+s);
      }
      else if (left.unqualify()!=right.unqualify()) {
        // It must be the same union
        cp.raiseError(node,"incompatible union types in "+s);
      }
      // else OK
    }
    else {
      // Should not be correct [TBW]
      cp.raiseError(node,"type mismatch in "+s);
    } 
  }



  // ******************************************************************
  // isConvertible :
  //
  // Check that the source can be converted to the destination
  // Note: the result is equivalent to 'checkAssignOperands'
  // ******************************************************************
  public boolean isConvertible(CompilerError cp, Type target, Type source) {
    // NOTE: More tests should be done in particular regarding
    // 'object compatibility' [TBW]

    // From void cast
    if (source.isVoid()) {
      return false;
    }

    if (target.isVector()) {
      if (source.isVector()) {
        // Implicit conversion is not allowed for built-in vector data types
        // Check that both types are the same
        if (source.unqualify()!=target.unqualify()) {
          return false;
        }
      }
      else if (source.isScalarOrLabel()) {
        // Implicit conversions are allowed from scalar to vectors
        // only if there is no down-conversion
        if (isDownScalarConversion(cp,(Vector)target.unqualify(),source)) {
          return false;
        }
        // OK
      }
      else {
        // Implicit conversions are possible
        return false;
      }
    }
    else if (target.isArithmeticScalar()) {
      if (!source.isArithmeticScalar()) {
        // Both should be arithmetic, but a pointer can be compared to an integral
        if (target.isIntegralScalar() && (source.isPointerOrLabel())) {
          // OK
        }
        else {
          return false;
        }
      }
      // else OK
    }
    else if (target.isPointerOrLabel()) {
      if (!source.isPointerOrLabel()) {
        // Both should be pointer, but a pointer can be compared to an integral
        if (source.isIntegralScalar()) {
          // OK
        }
        else {
          return false;
        }
      }
      else {
        // [OCL] Check for address space coherence
        if (oclLanguage) {
          AddressSpace target_as, source_as;
          target_as=getPointerOrLabelAddressSpace(null,target);
          source_as=getPointerOrLabelAddressSpace(null,source);

          // By default, a non qualified object if private static in kernels
          // (except images which are global but not supported here)
          if (target_as==AddressSpace.NO) {
            target_as=AddressSpace.PRIVATE;
          }
          if (source_as==AddressSpace.NO) {
            source_as=AddressSpace.PRIVATE;
          }

          if (target_as!=source_as) {
            return false;
          }
        }
      }
    }
    else if (target.isStruct()) {
      if (!source.isStruct()) {
        // Both should be a compatible structure
        return false;
      }
      else if (target.unqualify()!=source.unqualify()) {
        // It must be the same structure
        return false;
      }
      // else OK
    }
    else if (target.isUnion()) {
      if (!source.isUnion()) {
        // Both should be a compatible structure
        return false;
      }
      else if (target.unqualify()!=source.unqualify()) {
        // It must be the same union
        return false;
      }
      // else OK
    }
    else {
      // Should not be correct [TBW]
      return false;
    }
    return true;
  }



  // ******************************************************************
  // checkCastOperands :
  //
  // Check that both operands of an cast operator are compatible
  // It raises an error in case of non correct check
  // ******************************************************************
  public void checkCastOperands(NodeAST node, CompilerError cp,
      Type left, Type right) {
    // NOTE: More tests should be done in particular regarding
    if (left.isVoid()) {
      // Simply voids the type of 'right'
      // Nothing to do
      return;
    }

    if (left.isVector()) {
      if (right.isVector()) {
        // Explicit cast between vectors is illegal
        if (right.unqualify()!=left.unqualify()) {
          cp.raiseError(node,"[OCL] explicit cast between vectors is illegal");
        }
        // Else ok, since it is the same type
      }
      else {
        // Explicit cast does not have the 'down-conversion' restriction, on
        // the contrary to the implicit cast
        checkCastOperands(node, cp, left.getVectorBaseType(), right);
      }
    }
    else if (!left.isScalarOrLabel()) {
      if (left.unqualify()==right.unqualify()) {
        cp.raiseWarning(node,"ISO C forbids casting non-scalar to the same type");
      }
      else {
        cp.raiseError(node,"conversion to non-scalar type requested");
      }
    }
    else if (!right.isScalarOrLabel()) {
      if (right.isVoid()) {
        cp.raiseError(node,"void value not ignored as it ought to be");
      }
      else {
        cp.raiseError(node,"conversion of non-scalar type requested");
      }
    }

    // Here, left and right are either arithmetic, either pointer or label
    else if (left.isArithmeticScalar()) {
      if (left.isFloatingPointScalar() && right.isPointerOrLabel()) {
        cp.raiseError(node,"casting a pointer into floating point");
      }
      // else OK
    }
    else if (left.isPointerOrLabel()) {
      if (right.isFloatingPointScalar()) {
        cp.raiseError(node,"casting a floating point into pointer");
      }
      else if (oclLanguage && 
          right.isPointerOrLabel()) {
        // [OCL] Check for address space coherency
        AddressSpace left_as, right_as;
        left_as=getPointerOrLabelAddressSpace(cp,left);
        right_as=getPointerOrLabelAddressSpace(cp,right);

        // By default, a non qualified object if private static in kernels
        // (except images which are global but not supported here)
        if (left_as==AddressSpace.NO) {
          left_as=AddressSpace.PRIVATE;
        }
        if (right_as==AddressSpace.NO) {
          right_as=AddressSpace.PRIVATE;
        }
        // Address space must be the same
        if (left_as!=right_as) {
          // Temporary patch for PGIacc
          //cp.raiseError(node,"[OCL] casting a pointer to an other address space is forbidden");
        }
      }
      // else OK
    }
    else {
      // Should never come here
      cp.raiseInternalError("checkCastOperands");
    }
  }

  // ******************************************************************
  // checkLvalue :
  //
  // Check that 'etype' is a correct lvalue
  // It raises an error in case of non correct check
  // ******************************************************************
  public static void checkLvalue(NodeAST node, CompilerError cp,
      EnrichedType etype, String s) {
    if (!etype.designateAnObject()) {
      cp.raiseError(node,"invalid lvalue in "+s);
    }

    if (etype.getType().isVoid()) {
      cp.raiseError(node,"invalid lvalue in "+s);
    }
  }

  // ******************************************************************
  // checkModifiableLvalue :
  //
  // Check that 'etype' is a correct modifiable lvalue
  // It raises an error in case of non correct check
  // ******************************************************************
  public void checkModifiableLvalue(NodeAST node, CompilerError cp,
      EnrichedType etype, String s) {
    // Array labels are not object references
    if (!etype.designateAnObject()) {
      cp.raiseError(node,"invalid lvalue in "+s);
    }

    // It is forbidden to reference several times the same vector component
    // in a modifiable l-value (ex: v.xx)
    if (etype.isVectorElementReferenceWithDuplication()) {
      cp.raiseError(node,"invalid lvalue in "+s+
          " (multiple reference to the same vector component)");
    }

    Type t=etype.getType();
    // Array type is not modifiable
    if (t.isIncompleteOrVoid()||t.isArray()) {
      cp.raiseError(node,"invalid lvalue in "+s);
    }
    // A 'const' qualified type is not modifiable
    else if (t.isConstQualified()) {
      cp.raiseError(node,s+" of read-only object");
    }
    // An empty aggregate is not a lvalue
    // An aggregate with a const field is not modifiable
    else if (t.isStructOrUnion()) {
      if (
          (((StructOrUnion)(t.unqualify())).hasEmptyBody()) 
          // [TBW]
          //	  ||(((StructOrUnion)t).hasConstQualifiedElements())
          ) {
        cp.raiseError(node,"invalid lvalue in "+s);
      }
    }

    // [OCL] checks
    if (oclLanguage) {
      if (t.isQualified()) {
        // __constant data must be initialized
        if ((t.getQualifier()).getAddressSpace()==AddressSpace.CONSTANT) {
          cp.raiseError(node,"[OCL] "+s+
              " of variables declared in the __constant address space is forbidden");
        }
      }
    }
  }

  // ******************************************************************
  // getPointerOrLabelAddressSpace :
  //
  // Return the address space qualifier of the object referenced by
  // a 'pointer' type
  // ******************************************************************
  private static AddressSpace getPointerOrLabelAddressSpace(CompilerError cp, Type t) {
    if (t.isPointer()) {
      Type subType=t.getPointedType();
      if (subType.isQualified()) {
        return (subType.getQualifier()).getAddressSpace();
      }
      return AddressSpace.NO;
    }
    else if (t.isArray()) {
      if (t.isQualified()) {
        return (t.getQualifier()).getAddressSpace();
      }
      return AddressSpace.NO;
    }
    else if (t.isFunction()) {
      return AddressSpace.CODE;
    }
    else {
      // Internal error
      cp.raiseInternalError("(getPointerOrLabelAddressSpace)");
      return AddressSpace.NO;
    }
  }



  // ##################################################################
  // Function call management
  // ##################################################################



  // ******************************************************************
  // checkMangledFunctionCall :
  //
  // Returns the function symbol that is the most compatible with the
  // function call. It raises an potential warnings and errors in
  // case of ambiguity or no match with available function prototypes
  // 
  // ******************************************************************
  public FunctionLabel checkMangledFunctionCall(NodeAST node, CompilerError cp,
      String functionName,
      MangledFunctionPseudoLabel mangled_symbol,
      Function call) {
    String functionToPrint;
    if (functionName==null) {
      functionToPrint="";
    }
    else {
      functionToPrint=" '"+functionName+"'";
    }

    LinkedList<FunctionLabel> the_list=mangled_symbol.getManglingList();

    //########################################################################
    // Look for perfect match and equivalent match (natural match)
    // => use of function signature
    //########################################################################

    // [TBW] 'void *' match perfectly all pointers

    // Iterate over function declared
    String callSignature=call.getSignatureForMangling();
    //System.err.println("call = "+callSignature);

    for (FunctionLabel prototype_symbol:the_list) {
      Function prototype=(Function)prototype_symbol.getType();
      // System.err.println("proto = "+prototype.getSignatureForMangling());

      if (callSignature.equals(prototype.getSignatureForMangling())) {
        // Found perfect match
        // This check should not raise any error (and any warning ?)
        checkFunctionCall(node, cp, prototype_symbol.getName(),
            prototype, call);
        return prototype_symbol;
      }
    }


    //########################################################################
    // Look for compatible match (less natural match)
    // => try to convert function arguments
    //########################################################################


    LinkedList<FunctionLabel> compatible_symbol
    =new LinkedList<FunctionLabel>();
    int nb_conversions_for_compatible_symbol=0;

    // Iterate over function declared
    for (FunctionLabel prototype_symbol:the_list) {
      Function prototype=(Function)prototype_symbol.getType();

      // Check that both prototypes have the same number of parameters
      if (call.getNbParameters()>prototype.getNbParameters()) {
        if (prototype.hasVararg()) {
          // Compatible
          compatible_symbol.add(prototype_symbol);
        }
        // else not compatible
        continue;
      }

      if (call.getNbParameters()<prototype.getNbParameters()) {
        //  not compatible and not candidate
        continue;
      }


      // Check parameters one by one
      int i; boolean ok=true; int nb_conversion=0;
      for (i=0;i<prototype.getNbParameters();i++) {
        if (!prototype.getParameterSignature(i)
            .equals(call.getParameterSignature(i))) {
          // No perfect match for this parameter
          if (isConvertible(cp,prototype.getParameterType(i),
              call.getParameterType(i))) {
            nb_conversion++;
          }
          else {
            ok=false;
            break;
          }
        }
      }

      if (ok) {
        // The current function prototype is compatible
        if (compatible_symbol.size()!=0) {
          if (nb_conversion<nb_conversions_for_compatible_symbol) {
            // This prototype has less conversions and is then chosen
            compatible_symbol.clear(); // reset the list
            compatible_symbol.add(prototype_symbol);
            nb_conversions_for_compatible_symbol=nb_conversion;
          }
          else if (nb_conversion==nb_conversions_for_compatible_symbol) {
            // There is possibly an ambiguity
            compatible_symbol.add(prototype_symbol);
          }
          // else the function is not candidate
        }
        else {
          // Simply add the current proto
          compatible_symbol.add(prototype_symbol);
          nb_conversions_for_compatible_symbol=nb_conversion;
        }
      }
    }

    // Did we find a compatible function ?
    if (compatible_symbol.size()==0) {
      // We did not find any compatible symbol
      cp.raiseFatalError("no matching function for call to"+functionToPrint);
      return null;
    }
    else if (nb_conversions_for_compatible_symbol>1) {
      // There is an ambiguity
      cp.raiseError(node,"ambiguity for call to function" +functionToPrint);
      for (FunctionLabel prototype_symbol:compatible_symbol) {
        cp.raiseMessage(prototype_symbol.getIdNode(),
            "Candidate function"+functionToPrint+" here");
      }
      // Return any of the functions
      return compatible_symbol.getFirst();
    }
    // Else no ambiguity


    //########################################################################
    // If compatible match, potentially raise warnings to the user
    //#########################################################################

    // This check should not raise any error
    checkFunctionCall(node, cp, compatible_symbol.getFirst().getName(),
        (Function)compatible_symbol.getFirst().getType(),
        call);

    return compatible_symbol.getFirst();
  }


  // ******************************************************************
  // checkFunctionCall :
  //
  // Check that arguments of a function call are compatible with the
  // function prototype. It raises an error in case of non correct
  // check.
  // Note:if functionName is be 'null', it means that the function name
  //      is not available (ex: function pointer)
  // ******************************************************************
  public void checkFunctionCall(NodeAST node, CompilerError cp,
      String functionName,
      Function prototype,
      Function call) {
    String functionToPrint;

    // Check parameter list
    if ( (!prototype.hasParameter()) && (!prototype.isVoidParameterList())) {
      // The function prototype is compatible with all function calls
      return;
    }

    if (functionName==null) {
      functionToPrint="";
    }
    else {
      functionToPrint=" '"+functionName+"'";
    }

    // Check that both prototypes have the same number of parameters
    if (call.getNbParameters()>prototype.getNbParameters()) {
      if (!prototype.hasVararg()) {
        cp.raiseError(node,"too many arguments to function" + functionToPrint);
      }
      return;
    }
    if (call.getNbParameters()<prototype.getNbParameters()) {
      cp.raiseError(node,"too few arguments to function" + functionToPrint);
      return;
    }

    // Check parameters one by one
    int i;
    for (i=0;i<prototype.getNbParameters();i++) {

      checkAssignOperands(node, cp,
          prototype.getParameterType(i),
          call.getParameterType(i),
          "argument "+(i+1)+" of call to function" + functionToPrint
          );
    }

    return;
  }



  // ##################################################################
  // Constant type management
  // ##################################################################

  // ******************************************************************
  // getIntegralType :
  //
  // Returns the type of an integer terminal number
  // ******************************************************************
  private static Type getIntegralType(String s) {
    String s_num=s.toLowerCase();

    if (s_num.endsWith("ull")) {
      return IntegerScalar.Tulonglong;
    }
    if (s_num.endsWith("llu")) {
      return IntegerScalar.Tulonglong;
    }
    if (s_num.endsWith("ll")) {
      return IntegerScalar.Tslonglong;
    }
    if (s_num.endsWith("ul")) {
      return IntegerScalar.Tulong;
    }
    if (s_num.endsWith("lu")) {
      return IntegerScalar.Tulong;
    }
    if (s_num.endsWith("l")) {
      return IntegerScalar.Tslong;
    }
    if (s_num.endsWith("u")) {
      return IntegerScalar.Tuint;
    }
    return IntegerScalar.Tsint;
  }


  // ******************************************************************
  // getFloatType :
  //
  // Returns the type of a floating point terminal number
  // ******************************************************************
  private static Type getFloatType(String s) {
    String s_num=s.toLowerCase();

    if (s_num.endsWith("l")) {
      return FloatingPointScalar.Tlongdouble;
    }
    if (s_num.endsWith("f")) {
      return FloatingPointScalar.Tfloat;
    }
    return FloatingPointScalar.Tdouble;
  }


  // ******************************************************************
  // getIntegralNumberEnrichedType :
  //
  // Returns the type of a terminal number
  // ******************************************************************

  // Enum to characterize integer constant literals
  enum INTEGER_CONSTANT_TYPE {DECIMAL, OCTAL, HEXADECIMAL};
  enum SUFFIX {NO, U, L, UL, LL, ULL};

  public static EnrichedType getIntegralNumberEnrichedType(NodeAST node, CompilerError cp, String s) {
    // To lower case to simplify the parsing
    // [TBW] note that in C, 2lL is forbidden in C99
    String str=s.toLowerCase();


    //------------------------
    // Determinate the suffix
    //------------------------
    SUFFIX suffix;
    if (str.endsWith("ull")) {
      suffix=SUFFIX.ULL;
      str=str.substring(0,str.length()-3);
    }
    else if (str.endsWith("llu")) {
      suffix=SUFFIX.ULL;
      str=str.substring(0,str.length()-3);
    }
    else if (str.endsWith("ll")) {
      suffix=SUFFIX.LL;
      str=str.substring(0,str.length()-2);
    }
    else if (str.endsWith("ul")) {
      suffix=SUFFIX.UL;
      str=str.substring(0,str.length()-2);
    }
    else if (str.endsWith("lu")) {
      suffix=SUFFIX.UL;
      str=str.substring(0,str.length()-2);
    }
    else if (str.endsWith("l")) {
      suffix=SUFFIX.L;
      str=str.substring(0,str.length()-1);
    }
    else if (str.endsWith("u")) {
      suffix=SUFFIX.U;
      str=str.substring(0,str.length()-1);
    }
    else {
      suffix=SUFFIX.NO;
    }


    //------------------------------------------------
    // Determinate the kind of constant and its value
    //------------------------------------------------
    BigInteger value = BigInteger.ZERO;
    INTEGER_CONSTANT_TYPE constant_type;

    // -> Hexadecimal integer
    if (str.startsWith("0x")) {
      constant_type=INTEGER_CONSTANT_TYPE.HEXADECIMAL;
      try {
        //System.out.println( "  STRING: " + str);
        value=new BigInteger(str.substring(2),16);
        //System.out.println( "  VALUE: " + value);
      }
      catch (NumberFormatException ex) {
        cp.raiseInternalError("Wrong hexadecimal integral constant format");
      }
    }

    // -> Octal integer and 0 value
    else if (str.startsWith("0")) {
      constant_type = INTEGER_CONSTANT_TYPE.OCTAL;
      try {
        //System.out.println( "  STRING: " + str);
        value=new BigInteger(str,8);
        //System.out.println( "  VALUE: " + value);
      }
      catch (NumberFormatException ex) {
        cp.raiseInternalError("Wrong octal integral constant format");
      }
    }

    // -> Decimal integer and 0 value
    else {
      constant_type = INTEGER_CONSTANT_TYPE.DECIMAL;
      try {
        //System.out.println( "  STRING: " + str);
        value=new BigInteger(str);
        //System.out.println( "  VALUE: " + value);
      }
      catch (NumberFormatException ex) {
        cp.raiseInternalError("Wrong decimal integral constant format");
      }
    }


    //--------------------------------------
    // Determinate the type of the constant
    //--------------------------------------
    Type the_type=IntegerScalar.Tsint;
    ABI abi=Type.getSourceABI();

    switch (constant_type) {
    //  Decimal
    case DECIMAL:
      switch (suffix) {
      case NO:
        if (value.compareTo(abi.getINT_MAX())<=0) {
          the_type=IntegerScalar.Tsint;
        }
        else if (value.compareTo(abi.getLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslong;
        }
        else if (value.compareTo(abi.getLLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslonglong;
        }
        else {
          the_type=IntegerScalar.Tslonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case U:
        if (value.compareTo(abi.getUINT_MAX())<=0) {
          the_type=IntegerScalar.Tuint;
        }
        else if (value.compareTo(abi.getULONG_MAX())<=0) {
          the_type=IntegerScalar.Tulong;
        }
        else if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case L:
        if (value.compareTo(abi.getLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslong;
        }
        else if (value.compareTo(abi.getLLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslonglong;
        }
        else {
          the_type=IntegerScalar.Tslonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case UL:
        if (value.compareTo(abi.getULONG_MAX())<=0) {
          the_type=IntegerScalar.Tulong;
        }
        else if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case LL:
        if (value.compareTo(abi.getLLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslonglong;
        }
        else {
          the_type=IntegerScalar.Tslonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case ULL:
        if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      }
      break;

      //  Octal and Hexadecimal
    case OCTAL:
    case HEXADECIMAL:
      switch (suffix) {
      case NO:
        if (value.compareTo(abi.getINT_MAX())<=0) {
          the_type=IntegerScalar.Tsint;
        }
        else if (value.compareTo(abi.getUINT_MAX())<=0) {
          the_type=IntegerScalar.Tuint;
        }
        else if (value.compareTo(abi.getLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslong;
        }
        else if (value.compareTo(abi.getULONG_MAX())<=0) {
          the_type=IntegerScalar.Tulong;
        }
        else if (value.compareTo(abi.getLLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslonglong;
        }
        else if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case U:
        if (value.compareTo(abi.getUINT_MAX())<=0) {
          the_type=IntegerScalar.Tuint;
        }
        else if (value.compareTo(abi.getULONG_MAX())<=0) {
          the_type=IntegerScalar.Tulong;
        }
        else if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case L:
        if (value.compareTo(abi.getLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslong;
        }
        else if (value.compareTo(abi.getULONG_MAX())<=0) {
          the_type=IntegerScalar.Tulong;
        }
        else if (value.compareTo(abi.getLLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslonglong;
        }
        else if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case UL:
        if (value.compareTo(abi.getULONG_MAX())<=0) {
          the_type=IntegerScalar.Tulong;
        }
        else if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case LL:
        if (value.compareTo(abi.getLLONG_MAX())<=0) {
          the_type=IntegerScalar.Tslonglong;
        }
        else if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      case ULL:
        if (value.compareTo(abi.getULLONG_MAX())<=0) {
          the_type=IntegerScalar.Tulonglong;
        }
        else {
          the_type=IntegerScalar.Tulonglong;
          cp.raiseWarning(node,"integer constant is too large");
        }
        break;
      }
      break;
    }


    //--------------------------
    // Create the enriched type
    //--------------------------
    EnrichedType etype=new EnrichedType(the_type);
    etype.setConstantIntegral(value);

    return etype;
  }



  // ******************************************************************
  // getFloatingPointNumberEnrichedType :
  //
  // Returns the type of a terminal number
  // ******************************************************************
  public static EnrichedType getFloatingPointNumberEnrichedType(CompilerError cp, String s) {
    Type the_type;

    int i;

    the_type=getFloatType(s);
    EnrichedType etype=new EnrichedType(the_type);

    String str=s.toLowerCase();

    // Removes the suffix
    for(i=str.length()-1;
        (i>=0)&&((str.charAt(i)=='f')||(str.charAt(i)=='l'));
        i--);
    if (i<0) {
      cp.raiseInternalError("getNumberEnrichedType - 2");
    }
    str=str.substring(0,i+1);

    // Get the number
    try {
      //System.out.println( "  STRING: " + str);
      etype.setConstantFloatingpoint(Double.parseDouble(str));
      //System.out.println( "  VALEUR: " + etype.getConstantFloatingpointValue());
    }
    catch (NumberFormatException ex) {
      cp.raiseInternalError("Wrong floating point constant format");
    }
    return etype;
  }

}
