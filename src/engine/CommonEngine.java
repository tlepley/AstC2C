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

package engine;

import ir.symboltable.SymbolTable;
import ir.symboltable.symbols.ObjectLabel;
import ir.symboltable.symbols.Symbol;
import ir.literals.Literal;
import java.io.File;

import ir.base.PreprocessorInfoChannel;
import ir.base.NodeAST;
import common.CompilerError;
import java.util.LinkedList;
import parser.SymbolTableBuilder;


public abstract class CommonEngine {

  public enum TARGET_ABI  {ILP32, LP64};

  // ##################################################################
  //  internal data
  // ##################################################################
  protected int verboseOption;
  protected int debugOption;

  protected CompilerError globalCompilerError=null;
  protected File fileToProcess=null;
  protected File fileToGenerate=null;

  protected NodeAST         current_ast=null;
  protected SymbolTable   current_symbol_table=null;
  protected PreprocessorInfoChannel current_pic=null;
  protected CompilerError currentCompilerError=null;
  protected SymbolTableBuilder symbolBuilder = null;



  // ##################################################################
  //  Constructor
  // ##################################################################

  protected CommonEngine(File input_file, File output_file,
			 int verbose_level, int debug_level,
			 CompilerError globalCE) {
    fileToProcess       = input_file;
    fileToGenerate      = output_file;
    verboseOption       = verbose_level;
    debugOption         = debug_level;
    globalCompilerError = globalCE;

    // Create a error manager for the file
    currentCompilerError=new CompilerError(verbose_level,input_file.getPath());

    // Create a symbol table builder
    symbolBuilder = new SymbolTableBuilder(currentCompilerError);

    // TEMPORARY ** TO BE REMOVED **
    //symbolBuilder.setManglingMode();
  }


  // ##################################################################
  //  Getter functions
  // ##################################################################

  public File getInputFile() {return fileToProcess;}
  public File getOutputFile() {return fileToGenerate;}
  public SymbolTable getSymbolTable() {return current_symbol_table;}
  public SymbolTableBuilder getSymbolTableBuilder() {return symbolBuilder;}


  // ##################################################################
  //  Dump functions
  // ##################################################################

  //------------------------------------------------------------------
  // printAST
  //
  // Dumps to stdout an AST given in parameter
  //------------------------------------------------------------------
  public static void printAST(NodeAST ast, File file, String message) {
    System.out.println("FILE '" + file.getName() + "'" + message);
    System.out.print("-------");
    for (int j = 0; j < (file.getName().length() + message.length()); j++) {
      System.out.print("-");
    }
    System.out.println();
    NodeAST.printTree(ast);
    System.out.flush();
  }

  //------------------------------------------------------------------
  // printSymbolsAndLiterals
  //
  // Dumps to stdout the symbol table and the list of literals given
  // in parameters
  //------------------------------------------------------------------
  public static void printSymbolsAndLiterals(SymbolTable st,
					      LinkedList<Literal> ll,
					      File file) {
    System.out.println("C FILE '" + file.getName() + "'");
    System.out.print("---------");
    for (int j = 0; j < (file.getName().length()); j++) {
      System.out.print("-");
    }
    System.out.println();
    System.out.println();
    System.out.println(st.toString());
    System.out.println();
    System.out.println("ComplexLiteralList {");
    for(Literal l:ll) {
      System.out.println("  " + l.toStringLong());
    }
    System.out.println("}");
    System.out.println();
    System.out.flush();
  }

  public static void printSymbolTable(SymbolTable st, File file,
      String message) {
    String fileName=file.getName();
    System.out.println("FILE '" + fileName + "'" + message);
    System.out.print("-------");
    for (int j = 0; j < (fileName.length() + message.length()); j++) {
      System.out.print("-");
    }
    System.out.println();
    System.out.println(st.toString());
    System.out.flush();
  }

  public static void printTagSymbolList(
      Iterable<Symbol> symb_list, File file, String message) {
    String fileName=file.getName();
    System.out.println("FILE '" + fileName + "'" + message);
    System.out.print("-------");
    for (int j = 0; j < (fileName.length() + message.length()); j++) {
      System.out.print("-");
    }
    System.out.println();
    for (Symbol symb : symb_list) {
      System.out.println();
      System.out.println("*** TAG '" + symb.getName()
          + "' (original name : '"
          + symb.getOriginalName() + "')");
      System.out.print("--> Declaration");
      NodeAST.printTree(symb.getDeclarationNode());
    }
    System.out.flush();
  }

  public static void printObjectSymbolList(
      Iterable<ObjectLabel> symb_list, File file, String message) {
    String fileName=file.getName();
    System.out.println("FILE '" + fileName + "'" + message);
    System.out.print("-------");
    for (int j = 0; j < (fileName.length() + message.length()); j++) {
      System.out.print("-");
    }
    System.out.println();
    for (Symbol symb : symb_list) {
      System.out.println();
      System.out.println("*** VARIABLE '" + symb.getName()
          + "' (original name : '" + symb.getOriginalName() + "')");
      System.out.print("--> Declaration");
      NodeAST.printTree(symb.getDeclarationNode());
      NodeAST init = ((ObjectLabel)symb).getInitializationNode();
      if (init == null) {
        System.out.println("--> No initializer");
      } else {
        System.out.print("--> Initializer");
        NodeAST.printTree(init);
      }
    }
  }

  private static void printVariableSymbolList(
      Iterable<Symbol> symb_list, File file, String message) {
    String fileName=file.getName();
    System.out.println("FILE '" + fileName + "'" + message);
    System.out.print("-------");
    for (int j = 0; j < (fileName.length() + message.length()); j++) {
      System.out.print("-");
    }
    System.out.println();
    for (Symbol symb : symb_list) {
      System.out.println();
      System.out.println("*** VARIABLE '" + symb.getName()
          + "' (original name : '" + symb.getOriginalName() + "')");
      System.out.print("--> Declaration");
      NodeAST.printTree(symb.getDeclarationNode());
      if (symb instanceof ObjectLabel) {
        NodeAST init = ((ObjectLabel)symb).getInitializationNode();
        if (init == null) {
          System.out.println("--> No initializer");
        } else {
          System.out.print("--> Initializer");
          NodeAST.printTree(init);
        }
      }
    }
  }

  
}
