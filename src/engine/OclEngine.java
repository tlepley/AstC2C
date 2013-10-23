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

import ir.literals.Literal;
import ir.symboltable.SimpleSymbolTable;
import ir.symboltable.Linker;
import ir.types.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import ir.base.CToken;
import ir.base.LineObject;
import ir.base.NodeAST;

import parser.EmitterVectorEmulation;
import parser.GNUCTokenTypes;
import parser.GnuCLexer;
import parser.GnuCParser;
import parser.OclRewriter;
import parser.OclSymbolTableBuilder;
import abi.C_ABI_ilp32;
import abi.C_ABI_lp64;
import abi.OCL_ABI;

import common.CompilerError;
import common.ResourceManager;


public class OclEngine extends AstC2CEngine {

  OclSymbolTableBuilder oclSymbolBuilder =null;

  // ##################################################################
  //  Constructor
  // ##################################################################
  public OclEngine(File input_file, File output_file, int verbose_level,
      int debug_level, CompilerError globalCE) {
    super(input_file,output_file,verbose_level,debug_level,globalCE);

    oclSymbolBuilder = new OclSymbolTableBuilder(currentCompilerError);
  }

  // ##################################################################
  //  Getter functions
  // ##################################################################
  public OclSymbolTableBuilder getOclSymbolTableBuilder() {
    return oclSymbolBuilder;
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
      // Set source and target ABIs
      Type.setSourceABI(OCL_ABI.abi);
      switch(targetDevice) {
      case ILP32:
        Type.setTargetABI(C_ABI_ilp32.abi);
        break;
      case LP64:
        Type.setTargetABI(C_ABI_lp64.abi);
        break;
      default:
        globalCompilerError
        .raiseInternalError("(parseAndBuildSymbolTable) unknown target device");
      }


      // Parser simple symbol table
      SimpleSymbolTable st=new SimpleSymbolTable();

      // Target ABI specific file
      String abi_specific_file=null;
      switch(targetDevice) {
      case ILP32:
        abi_specific_file="OCL/target_abi_specific/ilp32.hl";
        break;
      case LP64:
        abi_specific_file="OCL/target_abi_specific/lp64.hl";
        break;
      default:
        globalCompilerError
        .raiseInternalError("(parseAndBuildSymbolTable) unknown target device");
      }

      // ----------------------------------------------------
      // First parse builtin files
      // ----------------------------------------------------	
      String[] builtin_filenames=new String[]{abi_specific_file, "OCL/kernel_builtins_runtime.hl"};

      for(String builtin_include_filepath : builtin_filenames) {
        // --- get the builtin file ---
        InputStream istream=ClassLoader
            .getSystemResourceAsStream(builtin_include_filepath);
        ResourceManager.registerStream(istream);
        if (istream==null) {
          // File does not exist
          globalCompilerError.raiseFatalError("compiler builtin file '"
              + builtin_include_filepath
              + "' not found");
        }
        // --- lexical analyzer ---
        GnuCLexer lexer_builtin = new GnuCLexer(istream);
        lexer_builtin.setOclLanguage();      // Sets the Language option
        lexer_builtin.setTokenObjectClass(CToken.class.getName());
        lexer_builtin.initialize(builtin_include_filepath);

        // -------   Parser   --------
        GnuCParser parser_builtin = new GnuCParser(lexer_builtin);
        // Set AST node type to TNode or it does not work ([TOBEFIXED])
        parser_builtin.setASTNodeClass(NodeAST.class.getName());
        NodeAST.setTokenVocabulary(GNUCTokenTypes.class.getName());
        // Sets the input language as OCL
        parser_builtin.setOclLanguage();
        // Set the initial symbol table as the builtin file symbol table
        parser_builtin.symbolTable=st;
        try {
          if (debugOption > 0) {
            globalCompilerError.raiseMessage("   -> parsing builtin file '"
                + builtin_include_filepath + "'");
          }
          parser_builtin.translationUnit();
        } catch (Exception e) {
          globalCompilerError.raiseFatalError("Fatal error while parsing: "
              + e);
          e.printStackTrace(); globalCompilerError.exitWithError(1);
        }
        // Close the input stream
        ResourceManager.closeStream(istream);

        // -------   symbol table   --------
        try {
          symbolBuilder.run(parser_builtin.getAST());
        } catch (Exception e) {
          System.err.println("Fatal error while building symbol table: "
              + e);
          e.printStackTrace(); globalCompilerError.exitWithError(1);
        }

        // We do not continue if some errors occurred and are still pending
        currentCompilerError.exitIfError();	
      }


      // ----------------------------------------------------
      // Parse the input source file
      // ----------------------------------------------------	  

      // --- lexical analyzer ---
      // Create the input stream
      FileInputStream dis = ResourceManager.openInputStream(fileToProcess);

      // Create a lexical analyzer and link it to the data stream
      GnuCLexer lexer = new GnuCLexer(dis); 
      lexer.setOclLanguage();      // Sets the Language option
      lexer.setTokenObjectClass(CToken.class.getName());
      lexer.initialize(fileToProcess.getPath());

      // Add the preprocessor info channel to the list (for the second pass)
      current_pic=lexer.getPreprocessorInfoChannel();


      // -------   Parser   --------
      // Create a parser and link it to the lexical analyzer
      GnuCParser parser = new GnuCParser(lexer);
      parser.setCompilerError(currentCompilerError);
      // Set AST node type to TNode or get nasty cast class errors
      parser.setASTNodeClass(NodeAST.class.getName());
      NodeAST.setTokenVocabulary(GNUCTokenTypes.class.getName());
      // Sets the input language as OCL
      parser.setOclLanguage();
      // Set the initial symbol table as the builtin files symbol table
      parser.symbolTable=st;
      try {
        if (debugOption > 0) {
          globalCompilerError.raiseMessage("   -> parsing file '"
              + fileToProcess.getName() + "'");
        }
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


      //---- Symbol table ---
      try {
        switch(mode) {
        case STANDARD:
          oclSymbolBuilder.run(parser.getAST(),
              symbolBuilder.getSymbolTable());
          break;
        case SPLIT:
          oclSymbolBuilder.runSplit(parser.getAST(),
              symbolBuilder.getSymbolTable());
          break;
        }
      } catch (Exception e) {
        System.err.println("Fatal error while building symbol table:\n" + e);
        e.printStackTrace(); globalCompilerError.exitWithError(1);
      }

      // Get the symbol table and the AST
      current_symbol_table = oclSymbolBuilder.getSymbolTable();
      current_ast=(NodeAST)oclSymbolBuilder.getAST();

      // Optional symbol table dump
      if (debugOption > 1) {
        LinkedList<Literal> literalList = oclSymbolBuilder.getLiteralList();
        System.out.println();System.out.println();
        printSymbolsAndLiterals(current_symbol_table, literalList,
            fileToProcess);
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
  static public void link(LinkedList<AstC2CEngine> engine_list,
      boolean optionLink,
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
      // Set source and target ABIs
      Type.setSourceABI(OCL_ABI.abi);
      switch(targetDevice) {
      case ILP32:
        Type.setTargetABI(C_ABI_ilp32.abi);
        break;
      case LP64:
        Type.setTargetABI(C_ABI_lp64.abi);
        break;
      default:
        globalCompilerError
        .raiseInternalError("(postLinkAndEmit) unknown target device");
      }


      // ----------------------------------------------------
      // OpenCL specific AST processing:
      //   - Remove OCL keywords
      // ----------------------------------------------------
      OclRewriter oclRewriter = new OclRewriter(currentCompilerError);
      // Set AST node type to TNode or it does not work (a voir ...)
      oclRewriter.setASTNodeClass(NodeAST.class.getName());

      try {
        oclRewriter.run(current_ast);
      } catch (Exception e) {
        System.err.println("Fatal error while walking AST in oclRewriter:\n"
            + e);
        e.printStackTrace(); globalCompilerError.exitWithError(1);
      }
      //current_ast=(TNode)oclRewriter.getAST();

      // Print AST
      if (debugOption > 2) {
        System.out.println();
        printAST(current_ast, fileToProcess, ": AST after Ocl rewriter");
      }

      // We do not continue if some errors occurred and are still pending
      currentCompilerError.exitIfError();




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

      EmitterVectorEmulation emitter;
      if (noPreprocessor) { emitter = new EmitterVectorEmulation(dos,currentCompilerError); }
      else    { emitter = new EmitterVectorEmulation(dos,current_pic,currentCompilerError); }

      dos.println("/*");
      dos.println("   File generated automatically, do not modify");
      dos.println("*/");
      dos.println();
      dos.println("// Defines target device specific types");
      dos.println("#include <stdint.h>");
      dos.println("#include <stddef.h>");
      dos.println();
      dos.println("// For builtin functions emulation");
      dos.println("#include <float.h>");
      dos.println("#include <math.h>");
      dos.println();

      // Management of typer limits
      dos.println("#  define OCL_SCHAR_MIN	(-128)");
      dos.println("#  define OCL_SCHAR_MAX	127");
      dos.println("#  define OCL_UCHAR_MAX	255");
      dos.println("#  define OCL_SHRT_MIN    	(-32768)");
      dos.println("#  define OCL_SHRT_MAX    	32767");
      dos.println("#  define OCL_USHRT_MAX	65535");
      dos.println("#  define OCL_INT_MIN	(-OCL_INT_MAX - 1)");
      dos.println("#  define OCL_INT_MAX	2147483647");
      dos.println("#  define OCL_UINT_MAX	4294967295U");

      // Manage the generation of 64 bits OpenCL long/ulong type
      if (Type.getTargetABI().getLongSize()==8) {
        dos.println("#  define OCL_LONG_MAX	9223372036854775807L");
        dos.println("#  define OCL_LONG_MIN	(-OCL_LONG_MAX - 1L)");
        dos.println("#  define OCL_ULONG_MAX	18446744073709551615UL");
        dos.println();

        if (Type.getTargetABI().getLongAlignment()!=8) {
          // Force alignment
          dos.println("#define long  long __attribute__ ((aligned (8)))");
        }
        // else, nothing to do, well aligned
      }
      else if ((Type.getTargetABI().getLongSize()!=8)&&(Type.getTargetABI().getLonglongSize()==8)) {
        dos.println("#  define OCL_LONG_MAX	9223372036854775807LL");
        dos.println("#  define OCL_LONG_MIN	(-OCL_LONG_MAX - 1LL)");
        dos.println("#  define OCL_ULONG_MAX	18446744073709551615ULL");
        dos.println();

        if (Type.getTargetABI().getLonglongAlignment()!=8) {
          // Force alignment
          dos.println("#define long  long long __attribute__ ((aligned (8)))");
        }
        else {
          dos.println("#define long  long long");
        }
      }
      else {
        globalCompilerError.raiseFatalError("Can not generate OpenCL 'long' type with the given target device");
      }

      dos.println("#include \"builtins/OCL/external_builtin_types.h\"");
      dos.println("#include \"vector_emulation_OCL.h\"");
      dos.println("#include \"vector_emulation_builtins.h\"");
      dos.println("#include \"builtins/OCL/kernel_builtins_runtime.h\"");
      dos.println();

      //dos.println("# 1 \"" + lo + "\"");
      LineObject lo=new LineObject(fileToProcess.getName());lo.setLine(1);
      dos.println(lo);
      try {
        emitter.translationUnit(current_ast);
      } catch (Exception e) {
        System.err.println("Fatal error while walking AST in emitter:\n"
            + e);
        e.printStackTrace(); globalCompilerError.exitWithError(1);
      }
      dos.println();
      dos.println("#undef long");

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
