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

/* Enum tag symbol */

package ir.symboltable.symbols;

import java.util.Vector;

public class EnumTag extends TagSymbol {

  //==================================================================
  // Constructors
  //==================================================================

  public EnumTag(String name) {
    super(name);
  }

  public EnumTag(EnumTag symb) {
    super(symb);

    children 	         = symb.children;
    if (children!=null) {
      children 	         = new Vector<EnumConstant>(symb.children);
    }
  }

  public EnumTag(Symbol symb) {
    super(symb);
  }



  //==================================================================
  // 'Children' management
  // 
  // -> Enum specific.Children of an enum tag are enum fields that it
  //    defines 
  //------------------------------------------------------------------

  // For ENUM symbols
  Vector<EnumConstant> children = null;

  // Setting
  public void setChildren(Vector<EnumConstant> v) {
    children=v;
  }
  public void addChild(EnumConstant symb) {
    if (children==null) {
      children=new Vector<EnumConstant>(10);
    }
    children.addElement(symb);
  }

  // Query
  public int getNbChildren() {
    if (children==null) {
      return(0);
    }
    return(children.size());
  }
  public Vector<EnumConstant> getChildren() {
    return children;
  }



  //==================================================================
  // Verbose functions
  //==================================================================

   //------------------------------------------------------------------
  // getMessageName:
  //
  // Return the symbol reference name as i should appear in a message
  // or error
  //------------------------------------------------------------------
  public String getMessageName() {
    return "enum '"+ getName() +"'";
  }

  //------------------------------------------------------------------
  // toString:
  //
  // Dump the symbol to a string
  //------------------------------------------------------------------
  public String toString() {
    StringBuffer buff = new StringBuffer();

    buff.append("Enum: ");

    // Common symbol info
    buff.append(super.toString());

    // 'Children'
    if (children!=null) {
      int i=0;
      buff.append(", children=[");
      for(EnumConstant obj:children) {
	if (i==0) {
	  i=1;
	}
	else {
	  buff.append(" ");
	}
	buff.append(obj.getId());
      }
      buff.append("]");
    }


    // Return the final string
    return buff.toString();
  }
}