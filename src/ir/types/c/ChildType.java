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

/* Generic type for final types which have a sub-type */

package ir.types.c;

import ir.types.Type;

import java.util.HashSet;
import java.util.HashMap;


public abstract class ChildType extends Type {
  //==================================================================
  // Private data
   //==================================================================
  private Type child=null;


  //==================================================================
  // Constructors
  //==================================================================

  public ChildType() {
    ;
  }

  public ChildType(Type c) {
    setChild(c);
  }


  //==================================================================
  // Setters
  //==================================================================

  public void setChild(Type c) {
    if (c instanceof Marker) {
      ((Marker)c).setParent(this);
    }
    else {
      child=c;
    }
  }


  //==================================================================
  // Getters
  //==================================================================

  public Type getChild() {
    return child;
  }



  //==================================================================
  // Verbose functions
  //==================================================================

  public void getTreeSet(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    child.getTreeSet(ts, cs);
  }

  //------------------------------------------------------------------
  // toStringInternal:
  //
  // Returns string a textual representation of the type. Use 'ts' and
  // 'cs' to avoid displaying multiple times the same type
  // (and avoid cycles)
  //------------------------------------------------------------------
  public String toStringInternal(HashSet<Type> ts, HashMap<Type,Integer> cs) {
    StringBuffer buff = new StringBuffer();
    buff.append("<...> of {").append(getChild().toStringInternal(ts,cs)).append("}");
    return(buff.toString());
  }




/* ******************************************************************
   Description:
         Marker for the type construction in declarators
****************************************************************** */
  public static class Marker extends Type {
    //==================================================================
    // Private data
    //==================================================================
    private Type parent=null;

    //==================================================================
    // Constructor / building
    //==================================================================
    public Marker() {
    }
    public void setParent(Type p) {
      parent=p;
    }
    public boolean hasParent() {
      return parent!=null;
    }

    //==================================================================
    // Getters
    //==================================================================
    public Type getParent() {
      return parent;
    }

    // Signature: should never be called
    public String getSignature() {
      return null;
    }


    //==================================================================
    // Target Specific information
    //==================================================================

    public int sizeof() {return 0;}
    public int alignof() {return 1;}


    //==================================================================
    // Verbose functions
    //==================================================================
    
    //------------------------------------------------------------------
    // toStringInternal:
    //
    // Returns string a textual representation of the type. Use 'ts' and
    // 'cs' to avoid displaying multiple times the same type
    // (and avoid cycles)
    //------------------------------------------------------------------
    public String toStringInternal(HashSet<Type> ts, HashMap<Type,Integer> cs) {
      return("[CHILDTYPE MARKER]");
    }

  }

}
