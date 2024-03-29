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

/* Vector type */

package ir.types.ocl;


import ir.types.Type;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.HashMap;


public abstract class Vector extends Type {
  //==================================================================
  // Private data
  //==================================================================
  int vectorSize;

  //==================================================================
  // Private Constructor
  //==================================================================
  protected Vector(int size) {
    vectorSize=size;
  }

  //==================================================================
  // Type class generic methods
  //==================================================================
  public boolean isVector() {return true;}
  public int getNbVectorElements() {return getNbElements();}
  public boolean isAggregate() {return true;}


  //==================================================================
  // Getters
  //==================================================================
  public int getNbElements() {return vectorSize;}

  //------------------------------------------------------------------
  // getBaseType
  //
  // Returns the base scalar type of the vector
  //------------------------------------------------------------------
  public abstract Type getBaseType();

  //------------------------------------------------------------------
  // getVectorBaseType
  //
  // 'Type' method, equivalent to 'getBaseType'
  //------------------------------------------------------------------
  public Type getVectorBaseType() {return getBaseType();}

  //------------------------------------------------------------------
  // getGeometricStyleIndex
  //
  // Returns the index of the vector element described in geometric
  // style (xyzw)
  //------------------------------------------------------------------
  static public int getGeometricStyleIndex(char c) {
    switch(c) {
    case 'x':
    case 'X':
      return 0;
    case 'y':
    case 'Y':
      return 1;
    case 'z':
    case 'Z':
      return 2;
    case 'w':
    case 'W':
      return 3;
    default:
      return -1;
    }
  }

  //------------------------------------------------------------------
  // getSStyleIndex
  //
  // Returns the index of the vector element described in 's style'
  // style (s0123456789abcdef)
  //------------------------------------------------------------------
  static public int getSStyleIndex(char c) {
    if ((c>='0')&&(c<='9')) {
      return c-'0';
    }
    else if ((c>='a')&&(c<='f')) {
      return 10+(c-'a');
    }
    else if ((c>='A')&&(c<='F')) {
      return 10+(c-'A');
    }
    else {
      return -1;
    }
  }

  //------------------------------------------------------------------
  // getElementList
  //
  // Returns the list of integer index specified by the string passed
  // in parameter.
  // Note: no 'out-of-bound' check is performed in case of 's' or
  //       geometric style indexing
  //------------------------------------------------------------------
  public LinkedList<Integer> getElementList(String element_string) {
    LinkedList<Integer> element_list=new LinkedList<Integer>();

    if ((element_string.charAt(0)=='s')||(element_string.charAt(0)=='S')) {
      // S style element indexing
      char [] element_array=element_string.substring(1).toCharArray();
      for(char c:element_array) {
	element_list.add(getSStyleIndex(c));
      }
    }
    else if (element_string.equals("lo")) {
      int index;
      int n=getNbElements();
      
      // A 3 elements vectors has physically 4 elements
      if (n==3) { n=4; }

      for(index=0;index<(n>>1);index++) {
	element_list.add(index);
      }
    }
    else if (element_string.equals("hi")) {
      int index;
      int n=getNbElements();
      
      // A 3 elements vectors has physically 4 elements
      if (n==3) { n=4; }

      for(index=n>>1;index<n;index++) {
	element_list.add(index);
      }
    }
    else if (element_string.equals("odd")) {
      int index;
      int n=getNbElements();
      
      // A 3 elements vectors has physically 4 elements
      if (n==3) { n=4; }

      for(index=1;index<n;index+=2) {
	element_list.add(index);
      }
    }
    else if (element_string.equals("even")) {
      int index;
      int n=getNbElements();
      
      // A 3 elements vectors has physically 4 elements
      if (n==3) { n=4; }

      for(index=0;index<n;index+=2) {
	element_list.add(index);
      }
    }
    else {
      // Geometric style
      char [] element_array=element_string.toCharArray();
      for(char c:element_array) {
	element_list.add(getGeometricStyleIndex(c));
      }
    }

    return element_list;
  }
  

  //------------------------------------------------------------------
  // getEquivalentType
  //
  // Returns the equivalent vector of size n.
  // Returns a scalar in case n==1
  // Returns null in case of non allowed n
  //------------------------------------------------------------------
  public abstract Type getEquivalentType(int n);


  //==================================================================
  // Comparisons
  //==================================================================

  //------------------------------------------------------------------
  // isSameNbElements
  //
  // Returns:
  // -1 if the vector has less elements than v
  //  0 if the vector has the same number of elements as v
  //  1 if the vector has more elements than v
  //------------------------------------------------------------------
  public int compareNbElements(Vector v) {
    int c= vectorSize-v.vectorSize;
      return c<0? -1 : c>0 ? 1 : 0; 
  }


  //==================================================================
  // Verbose function
  //==================================================================

  //------------------------------------------------------------------
  // toStringInternal:
  //
  // Returns string a textual representation of the type. Use 'ts' and
  // 'cs' to avoid displaying multiple times the same type
  // (and avoid cycles)
   //------------------------------------------------------------------
  public String toStringInternal(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    return "vec["+vectorSize+"]";
  }

  public abstract String dump(); 
  public abstract String dumpBaseType(); 
}
