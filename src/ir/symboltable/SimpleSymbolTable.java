/*
  This file is part of AstC2C.
  
  Authors: Monty Zukoski, Thierry Lepley
*/

/* Very simple symbol table for the front-end parser. This symbol is necessary
   for managing the disambiguation of typedef names. */

package ir.symboltable;

import ir.base.NodeAST;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class SimpleSymbolTable {

  /** holds list of scopes */
  private List<String> scopeStack;

  /** table where all defined names are mapped to TNode tree nodes */
  private Map<String, NodeAST> symTable;

  public SimpleSymbolTable()  {
    scopeStack = new ArrayList<String>(10);
    symTable = new HashMap<String, NodeAST>(533);
  }


  /** push a new scope onto the scope stack.
    */
  public void pushScope(String s) {
      //System.out.println("push scope:" + s);
    scopeStack.add(s);
  }

  /** pop the last scope off the scope stack.
    */
  public void popScope() {
      //System.out.println("pop scope");
    int size = scopeStack.size();
    if(size > 0)
      scopeStack.remove(size - 1);
  }

  /** return the current scope as a string 
   */
  public String currentScopeAsString() {
      StringBuilder buf = new StringBuilder(100);
      boolean first = true;
      for(String s:scopeStack) {
        if(first) 
          first = false;
        else
          buf.append("::");
        buf.append(s);
      }
      return buf.toString();
  }

  /** given a name for a type, append it with the 
    current scope.
    */
  public String addCurrentScopeToName(String name) {
    String currScope = currentScopeAsString();
    return addScopeToName(currScope, name);
  }

  /** given a name for a type, append it with the 
    given scope.  MBZ
    */
  public String addScopeToName(String scope, String name) {
    if(scope == null || scope.length() > 0)
      return scope + "::" + name;
    else
      return name;
  }

  /** remove one level of scope from name MBZ*/
  public String removeOneLevelScope(String scopeName) {
    int index = scopeName.lastIndexOf("::");
    if (index > 0) {
      return scopeName.substring(0,index);
    }
    if (scopeName.length() > 0) {
        return "";
    }
    return null;
  }
  
  /** add a node to the table with it's key as
    the current scope and the name */
  public NodeAST add(String name, NodeAST node) {
    return symTable.put(addCurrentScopeToName(name),node);
  }


  /** lookup a fully scoped name in the symbol table */
  public NodeAST lookupScopedName(String scopedName) {
    return symTable.get(scopedName);
  }

  /** lookup an unscoped name in the table by prepending
    the current scope.
    MBZ -- if not found, pop scopes and look again
    */
  public NodeAST lookupNameInCurrentScope(String name) {
    String scope = currentScopeAsString();
    String scopedName;
    NodeAST tnode = null;

    //System.out.println( "\n"+ this.toString() );

    while (tnode == null && scope != null) {
      scopedName = addScopeToName(scope, name);
      //System.out.println("lookup trying " + scopedName);
      tnode = symTable.get(scopedName);
      scope = removeOneLevelScope(scope);
    }
    return tnode;
  }

  /** convert this table to a string */
  public String toString() {
    StringBuffer buff = new StringBuffer(300);
    buff.append("CSymbolTable { \nCurrentScope: " + currentScopeAsString() + 
                "\nDefinedSymbols:\n");
    for(Map.Entry<String,NodeAST> entry:symTable.entrySet()) {
      buff.append(entry.getKey().toString() + " (" + 
                  NodeAST.getNameForType(entry.getValue().getType()) + ")\n");
    }
    buff.append("}\n");
    return buff.toString();
  }

};
