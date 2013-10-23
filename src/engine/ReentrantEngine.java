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

import ir.base.CToken;
import ir.base.LineObject;
import ir.base.NodeAST;
import common.CompilerError;
import common.ResourceManager;

import abi.C_ABI_ilp32;
import ir.symboltable.ExtractionLinker;
import ir.symboltable.symbols.*;
import ir.types.Type;
import ir.literals.Literal;

import parser.GNUCTokenTypes;
import parser.GnuCLexer;
import parser.GnuCParser;
import parser.SymbolTableBuilder;
import parser.ThisRewriter;
import parser.ReentrantRewriter;
import parser.Emitter;
import parser.EmitterReentrant;

import java.util.LinkedList;
import java.util.ArrayList;
import java.io.File;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class ReentrantEngine extends CommonEngine {

  // Linker
  private static InheritableThreadLocal<ExtractionLinker> linker = new InheritableThreadLocal<ExtractionLinker>() {
    @Override
    protected ExtractionLinker initialValue() {
      return null;
    }
  };
  private static ExtractionLinker getLinker() {
    return linker.get();
  }
  private static void setLinker(ExtractionLinker l) {
    linker.set(l);
  }

  // ##################################################################
  //  Constructor
  // ##################################################################
  public ReentrantEngine(File input_file, File output_file,
      int verbose_level, int debug_level,
      CompilerError globalCE) {
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
  public void parseAndBuildSymbolTable() {
    try {
      // Set target specific type information (by default 32 bits)
      Type.setSourceABI(C_ABI_ilp32.abi);
      Type.setTargetABI(C_ABI_ilp32.abi);

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
      symbolBuilder.runExtraction(parser.getAST());

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



  // Global lists for the global link stage
  static ArrayList<Symbol> global_extracted_type_tag_list
  = new ArrayList<Symbol>();
  static ArrayList<ObjectLabel> global_instance_data_list
  = new ArrayList<ObjectLabel>();
  static ArrayList<ObjectLabel> global_extern_list
  = new ArrayList<ObjectLabel>();

  //------------------------------------------------------------------
  // link :
  //
  // Function performing global linking
  //------------------------------------------------------------------
  static public void link(LinkedList<ReentrantEngine> engine_list, boolean optionLink,
      int debugOption,
      CompilerError globalCompilerError) {

    setLinker(new ExtractionLinker(optionLink, globalCompilerError));

    // Iterates on all engines (each input file)
    for(ReentrantEngine eng : engine_list) {
      SymbolTableBuilder symbolBuilder=eng.getSymbolTableBuilder();

      // List of instance data
      LinkedList<ObjectLabel> module_instance_data_list
      = symbolBuilder.getInstanceDataList();
      // Type tags necessary to instance data declarations
      LinkedList<Symbol> module_extracted_type_tag_list
      = symbolBuilder.getExtractedTypeTagList();
      // List of reference to potential instance data
      LinkedList<ObjectLabel> extern_declaration_list
      = symbolBuilder.getExternDeclarationList();
      // Module AST (without instance data)
      NodeAST residual_AST = (NodeAST) symbolBuilder.getAST();

      // AST Verbose
      if (debugOption > 2) {
        System.out.println();
        System.out.println();
        printObjectSymbolList(module_instance_data_list, eng.getInputFile(),
            ": AST of extracted instance data");
        System.out.println();
        System.out.println();
        printTagSymbolList(module_extracted_type_tag_list, eng.getInputFile(),
            ": AST of tags extracted for instance data");
        System.out.println();
        System.out.println();
        printAST(residual_AST, eng.getInputFile(), ": AST (after extraction)");
      }

      // Build the global list of instance data
      global_instance_data_list.addAll(module_instance_data_list);

      // Build the global list of extracted tags
      global_extracted_type_tag_list.addAll(module_extracted_type_tag_list);

      // Build the global 'extern declaration' list for global link
      global_extern_list.addAll(extern_declaration_list);

      // Add the symbol table to the linker
      getLinker().addSymbolTable(eng.getSymbolTable(),eng.getInputFile().getName());
    }

    // Process instance data symbols
    getLinker().processInstanceData(global_instance_data_list,global_extern_list);

    // Process function symbols
    getLinker().processFunction();

    // getLinker().run();

    if (debugOption > 1) {
      // Dump the global symbol table
      System.out.println();
      System.out.println("** Global symbol table");
      System.out.println(getLinker().getGlobalSymbolTable().toString());
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
  public void postLinkAndEmit(boolean noPreprocessor, LinkedList<File> additionalGeneratedFiles) {

    try {

      // ----------------------------------------------------
      // Add 'void *_this' parameter to internal functions 
      // Add _this parameter to calls to internal functions
      // ----------------------------------------------------
      ThisRewriter thisRewriter = new ThisRewriter(currentCompilerError);
      // Set AST node type to TNode or it does not work (a voir ...)
      thisRewriter.setASTNodeClass(NodeAST.class.getName());
      thisRewriter.setThis("void","_this");

      try {
        thisRewriter.run(current_ast, true);
      } catch (Exception e) {
        System.err.println("Fatal error while walking AST in thisRewriter:\n" + e);
        e.printStackTrace(); globalCompilerError.exitWithError(1);
      }
      //current_ast=(TNode)thisRewriter.getAST();

      // Print AST
      if (debugOption > 2) {
        System.out.println();
        printAST(current_ast, fileToProcess, ": AST after 'this' rewriter");
      }

      // We do not continue if some errors occurred and are still pending
      currentCompilerError.exitIfError();

      // ----------------------------------------------------
      // Changed the main function name
      // ----------------------------------------------------

      ReentrantRewriter reentrantRewriter = new ReentrantRewriter(currentCompilerError);
      // Set AST node type to TNode or it does not work (a voir ...)
      reentrantRewriter.setASTNodeClass(NodeAST.class.getName());

      try {
        reentrantRewriter.run(current_ast);
      } catch (Exception e) {
        System.err.println("Fatal error while walking AST in reentrantRewriter:\n" + e);
        e.printStackTrace(); globalCompilerError.exitWithError(1);
      }

      // Print AST
      if (debugOption > 2) {
        System.out.println();
        printAST(current_ast, fileToProcess, ": AST after 'reentrant' rewriter");
      }

      // We do not continue if some errors occurred and are still pending
      currentCompilerError.exitIfError();


      // ==================================================================
      // Regenerate the C File
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
      EmitterReentrant emitter;
      if (noPreprocessor) { emitter = new EmitterReentrant(dos); }
      else                { emitter = new EmitterReentrant(dos,current_pic); }

      dos.println("/*");
      dos.println("   File generated automatically, do not modify");
      dos.println("   Created from : " + fileToProcess.getName());
      dos.println("*/");
      dos.println();

      // Include the file containing instance data declaration
      dos.println("#include \"DATA.h\"");
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

      // Close the file stream
      ResourceManager.closeStream(dos);


    } catch (Exception e) {
      System.err.println("exception: " + e);
      e.printStackTrace();
      globalCompilerError.exitWithError(1);
    }

  }


  //------------------------------------------------------------------
  // generateGlobalFiles :
  //
  // Generate additional files (DATA.h, DATA.h.c)
  // output file(s)
  //------------------------------------------------------------------
  static public void generateGlobalFiles(File dataFileH, File dataFilec,
      int debugOption,
      CompilerError globalCompilerError) {
    try {

      // ==================================================================
      // Regenerate DATA.h
      // ==================================================================	

      // Create a 'this rewriter' treeparser
      ThisRewriter global_this_rewriter = new ThisRewriter(globalCompilerError);
      global_this_rewriter.setASTNodeClass(NodeAST.class.getName());

      // Create the output stream
      if (!dataFileH.exists()) {
        // File does not exist
        dataFileH.createNewFile();
      }
      if (!dataFileH.canWrite()) {
        // The application can not modify the file
        globalCompilerError.raiseError("can not modify global data file '"
            + dataFileH.toString() + "'");
      }
      else {
        PrintStream dos = new PrintStream(new FileOutputStream(dataFileH));
        ResourceManager.registerStream(dos);

        // Create the AST emitter on the output stream
        EmitterReentrant emitterAST;
        emitterAST = new EmitterReentrant(dos);

        // Dump into the global data file
        dos.println("/*");
        dos.println("   File generated automatically, do not modify");
        //dos.print  ("   Created from:");
        //for (File ofile : inputfiles) {
        //  dos.print(" " + ofile);
        //}
        //dos.println();
        dos.println("*/");
        dos.println();

        dos.println("/* Extracted type tags */");
        for(Symbol symb:global_extracted_type_tag_list) {
          NodeAST tn = symb.getDeclarationNode().deepCopy();
          global_this_rewriter.declaration(tn);
          tn.deepSetLineNumWithRightSiblings(1);
          emitterAST.declaration(tn);
          dos.println();
        }
        dos.println();
        dos.println("/* Extracted instance data */");
        dos.println("struct DATA_STRUCTURE {");

        for(Symbol symb:getLinker().getInstanceData()) {
          NodeAST tn = symb.getDeclarationNode().deepCopy();
          global_this_rewriter.declaration(tn);
          tn.deepSetLineNumWithRightSiblings(1);
          dos.print("  ");emitterAST.declaration(tn);
          dos.println();
        }
        dos.println("};");
        dos.println("#define DATA (*((struct DATA_STRUCTURE *)_this))");

        // Potential Initialization
        ArrayList<ObjectLabel> initData=getLinker().getInitializedInstanceData();
        if (initData.size()!=0) {
          dos.print("#define INIT_DATA {");
          boolean first = true;
          for(ObjectLabel symb:initData) {
            if (first) {
              dos.println("\\");
              first=false;
            }
            else {
              dos.println(",\\");
            }
            dos.print("           ." + symb.getName() + " = ");
            NodeAST init_tn = symb.getInitializationNode();
            init_tn.deepSetLineNumWithRightSiblings(1);
            emitterAST.initializer(init_tn);
          }
          dos.println("\\");
          dos.println("         }");
        }

        // Close the input stream
        ResourceManager.closeStream(dos);
      }


      // ==================================================================
      // Regenerate DATA.h.c
      // ==================================================================	

      File dataFileC = new File(dataFileH.getAbsolutePath() + ".c");
      if (!dataFileC.exists()) {
        // File does not exist
        dataFileC.createNewFile();
      }
      if (!dataFileC.canWrite()) {
        // The application can not modify the file
        globalCompilerError.raiseError("can not modify global data file '"
            + dataFileC.toString() + "'");
      }
      else {
        PrintStream dos = new PrintStream(new FileOutputStream(dataFileC));
        ResourceManager.registerStream(dos);

        // Create the AST emitter on the output stream
        Emitter emitterAST = new Emitter(dos);
        dos.println("/*");
        dos.println("   File generated automatically, do not modify");
        //	dos.print  ("   Created from:");
        //	for (File ofile : inputfiles) {
        //	  dos.print(" " + ofile);
        //	}
        //dos.println("");
        dos.println("*/");
        dos.println("");
        dos.println("#include \"" + dataFileH.getName() + "\"");
        dos.println("");
        dos.println("#define DATA my_data_structure");
        dos.println("");
        dos.println("static struct DATA_STRUCTURE my_data_structure");
        dos.println("#ifdef INIT_DATA");
        dos.println(" = INIT_DATA");
        dos.println("#endif");
        dos.println(";");
        dos.println();
        dos.println("extern int __reentrant_run(void *, int , char * [ ]);");
        dos.println("int main( int argc , char * argv [ ]) {\n" +
            "  return(__reentrant_run(&my_data_structure,argc,argv));\n" +
            "}");

        // Close the input stream
        ResourceManager.closeStream(dos);
      }

    } // try
    catch (Exception e) {
      System.err.println("exception: "+e);
      e.printStackTrace();
      System.exit(1);
    }

    // We do not continue if some errors occurred and are still pending
    globalCompilerError.exitIfError();

  }

}
