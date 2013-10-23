/*
  This file is part of AstC2C.
  
  Authors: Monty Zukoski, Thierry Lepley
*/


package ir.base;

public class CToken extends antlr.CommonToken {
  String source = "";
  int tokenNumber;

  public String getSource() 
  { 
    return source;
  }

  public void setSource(String src) 
  {
    source = src;
  }

  public int getTokenNumber() 
  { 
    return tokenNumber;
  }

  public void setTokenNumber(int i) 
  {
    tokenNumber = i;
  }

    public String toString() {
        return "CToken:" +"(" + hashCode() + ")" + "[" + getType() + "] "+ getText() + " line:" + getLine() + " source:" + source ;
    }
}
