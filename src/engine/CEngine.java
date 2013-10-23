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

import abi.C_ABI_ilp32;
import abi.C_ABI_lp64;
import ir.base.CToken;
import ir.base.LineObject;
import ir.base.NodeAST;

import ir.symboltable.Linker;
import ir.types.*;
import ir.literals.Literal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import parser.GNUCTokenTypes;
import parser.GnuCLexer;
import parser.GnuCParser;
import parser.Emitter;
import parser.EmitterVectorEmulation;
import common.CompilerError;
import common.ResourceManager;


public class CEngine extends AstC2CEngine {

  // ##################################################################
  //  Constructor
  // ##################################################################
  public CEngine(File input_file, File output_file, int verbose_level, int debug_level, CompilerError globalCE) {
    super(input_file,output_file,verbose_level,debug_level,globalCE);
  }


  // ##################################################################
  //  File processing functions
  // ##################################################################

  //------------------------------------------------------------------
  // parseAndBuildSymbolTable :
  //
  // Function parsing and building the symbol table for the input file
  //------------------------------------------------------------------
  public void parseAndBuildSymbolTable(TARGET_ABI targetDevice, MODE mode) {
    try {
      // Set target specific type information
      switch(targetDevice) {
      case ILP32:
        Type.setSourceABI(C_ABI_ilp32.abi);
        Type.setTargetABI(C_ABI_ilp32.abi);
        break;
      case LP64:
        Type.setSourceABI(C_ABI_lp64.abi);
        Type.setTargetABI(C_ABI_lp64.abi);
        break;
      default:
        globalCompilerError
        .raiseInternalError("(parseAndBuildSymbolTable) unknown target device");
      }

      // -------------------------
      // lexical analyzer
      // -------------------------
      // Create the input stream
      FileInputStream dis = ResourceManager.openInputStream(fileToProcess);

      // Create a lexical analyzer and link it to the data stream
      GnuCLexer lexer = new GnuCLexer(dis);
      lexer.setTokenObjectClass(CToken.class.getName());
      lexer.initialize(fileToProcess.getPath());

      // Add the preprocessor info channel to the list (for the second pass)
      current_pic=lexer.getPreprocessorInfoChannel();

      // --------------------------
      // GNU-C Parser
      // --------------------------

      // Create a parser and link it to the lexical analyzer
      GnuCParser parser = new GnuCParser(lexer);
      parser.setCompilerError(currentCompilerError);
      // Set AST node type to TNode or get nasty cast class errors
      parser.setASTNodeClass(NodeAST.class.getName());
      NodeAST.setTokenVocabulary(GNUCTokenTypes.class.getName());

      try {
        parser.translationUnit();
      } catch (Exception e) {
        System.err.println("Fatal error while parsing:\n" + e);
        e.printStackTrace();globalCompilerError.exitWithError(1);
      }

      // Print AST
      if (debugOption > 2) {
        System.out.println();
        printAST((NodeAST) parser.getAST(), fileToProcess, ": Initial AST");
      }

      // Close the input stream
      ResourceManager.closeStream(dis);


      // ======================================================
      // Symbol table builder and optional declaration splitter
      // ======================================================

      // Process the AST
      switch(mode) {
      case STANDARD:
        symbolBuilder.run(parser.getAST());
        break;
      case SPLIT:
        symbolBuilder.runSplit(parser.getAST());
        break;
      }

      // Get the symbol table and the AST
      current_symbol_table = symbolBuilder.getSymbolTable();
      current_ast          = (NodeAST)symbolBuilder.getAST();

      // Symbol table Debug
      if (debugOption > 1) {
        LinkedList<Literal> literalList = symbolBuilder.getLiteralList();
        System.out.println();System.out.println();
        printSymbolsAndLiterals(current_symbol_table, literalList, fileToProcess);
      }

      // We do not continue if some errors occurred and are still pending
      currentCompilerError.exitIfError();

    } catch (Exception e) {
      System.err.println("exception: " + e);
      e.printStackTrace();globalCompilerError.exitWithError(1);
    }
  }



  //------------------------------------------------------------------
  // link :
  //
  // Function performing global linking
  //------------------------------------------------------------------
  static public void link(LinkedList<AstC2CEngine> engine_list, boolean optionLink,
      int debugOption,
      CompilerError globalCompilerError) {
    // Linker
    Linker linker = new Linker(optionLink, globalCompilerError);

    for(AstC2CEngine eng : engine_list) {
      // Add the symbol table to the linker
      linker.addSymbolTable(eng.getSymbolTable(),eng.getInputFile().getName());
    }

    // Process program level link
    linker.run();

    if (debugOption > 1) {
      // Dump the global symbol table
      System.out.println();
      System.out.println("** Global symbol table:");
      System.out.println(linker.getGlobalSymbolTable().toString());
    }

    // We do not continue if some errors occurred and are still pending
    globalCompilerError.exitIfError();
  }



  //------------------------------------------------------------------
  // postLinkAndEmit :
  //
  // Function performing processing after program link and generating
  // output file(s)
  //------------------------------------------------------------------
  public void postLinkAndEmit(TARGET_ABI targetDevice, 
      boolean noPreprocessor,
      boolean vec_emul,
      LinkedList<File> additionalGeneratedFiles) {
    try {
      // ==================================================================
      // C File regeneration
      // ==================================================================	

      // Performs some checks with regard to output files
      if (!fileToGenerate.exists()) {
        // File does not exist
        fileToGenerate.createNewFile();
      }
      if (!fileToGenerate.canWrite()) {
        // The application can not modify the file
        System.err.println("Error: can not modify output file '"
            + fileToGenerate.toString() + "'");
        globalCompilerError.exitWithError(1);
      }

      // Debug information
      if (debugOption > 0) {
        globalCompilerError.raiseMessage("  ... generating file '"
            + fileToGenerate.getName() + "'");
      }
      if (debugOption > 2) {
        System.out.println();System.out.println();
        printAST(current_ast, fileToProcess, ": Regenerated AST");
      }


      // Create the output stream
      PrintStream dos = new PrintStream(new FileOutputStream(fileToGenerate));
      ResourceManager.registerStream(dos);

      // Create the AST emitter on the output stream
      if (vec_emul) {
        EmitterVectorEmulation emitter;
        if (noPreprocessor) { emitter = new EmitterVectorEmulation(dos,currentCompilerError); }
        else                { emitter = new EmitterVectorEmulation(dos,current_pic,currentCompilerError); }

        dos.println("/*");
        dos.println("   File generated automatically, do not modify");
        dos.println("   Created from : " + fileToProcess.getName());
        dos.println("*/");
        dos.println();

        // Management of type limits
        dos.println("#include <limits.h>");
        dos.println("#define OCL_SCHAR_MIN SCHAR_MIN");
        dos.println("#define OCL_SCHAR_MAX SCHAR_MAX");
        dos.println("#define OCL_UCHAR_MAX UCHAR_MAX");
        dos.println("#define OCL_SHRT_MIN  SHRT_MIN");
        dos.println("#define OCL_SHRT_MAX  SHRT_MAX");
        dos.println("#define OCL_USHRT_MAX USHRT_MAX");
        dos.println("#define OCL_INT_MIN   INT_MIN");
        dos.println("#define OCL_INT_MAX   INT_MAX");
        dos.println("#define OCL_UINT_MAX  UINT_MAX");
        dos.println("#define OCL_LONG_MAX  LONG_MAX");
        dos.println("#define OCL_LONG_MIN  LONG_MIN");
        dos.println("#define OCL_ULONG_MAX ULONG_MAX");
        dos.println("#include \"vector_emulation_C.h\"");
        dos.println();
        /*	dos.println("#include <math.h>");
	dos.println("#include \"vector_emulation_builtins.h\"");
	dos.println(); */

        //dos.println("# 1 \"" + lo + "\"");
        LineObject lo=new LineObject(fileToProcess.getName());lo.setLine(1);
        dos.println(lo);
        try {
          emitter.translationUnit(current_ast);
        } catch (Exception e) {
          System.err.println("Fatal error while walking AST in emitter:\n" + e);
          e.printStackTrace(); globalCompilerError.exitWithError(1);
        }
      }
      else {
        Emitter emitter;
        if (noPreprocessor) { emitter = new Emitter(dos); }
        else                { emitter = new Emitter(dos,current_pic); }

        dos.println("/*");
        dos.println("   File generated automatically, do not modify");
        dos.println("   Created from : " + fileToProcess.getName());
        dos.println("*/");
        dos.println();

        //dos.println("# 1 \"" + lo + "\"");
        LineObject lo=new LineObject(fileToProcess.getName());lo.setLine(1);
        dos.println(lo);
        try {
          emitter.translationUnit(current_ast);
        } catch (Exception e) {
          System.err.println("Fatal error while walking AST in emitter:\n" + e);
          e.printStackTrace(); globalCompilerError.exitWithError(1);
        }
      }

      // Close the file stream
      ResourceManager.closeStream(dos);

      // We do not continue if some errors occurred and are still pending
      currentCompilerError.exitIfError();

    } catch (Exception e) {
      System.err.println("exception: " + e);
      e.printStackTrace();
      globalCompilerError.exitWithError(1);
    }

  }

}
