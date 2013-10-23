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


package driver;

import common.CompilerError;
import common.CompilerExit;
import common.ResourceManager;
import engine.ReentrantEngine;
import parser.SymbolTableBuilder;

import java.io.File;
import java.util.LinkedList;
import java.util.UUID;

import driver.options.CodegenOptions;
import driver.options.DriverOptions;
import driver.options.GeneralOptions;


public class ReentrantC2C {

  // ##################################################################
  //  Options management
  // ##################################################################

  // Additional preprocessing options (builtins)
  LinkedList<String> additionalCPreprocessingOptions
  =new LinkedList<String>();

  // List of input files
  //--------------------
  LinkedList<File> inputFileList = new LinkedList<File>();

  // Version
  //--------
  private int versionMajor = 1;
  private int versionMinor = 0;
  private int versionSubMinor = 0;

  protected void printVersion() {
    CompilerError.GLOBAL.raiseMessage(
        "version : " + versionMajor + "." + versionMinor + "." + versionSubMinor
        );
  }


  // ##################################################################
  // Options management
  // ##################################################################


  // ******************************************************************
  //  Help display:
  // ******************************************************************
  private void printHelp() {
    CompilerError.GLOBAL.raiseMessage("RC2C compiler");
    printVersion();
    CompilerError.GLOBAL.raiseMessage("Command : rc2c [options]* [input files]+");
    CompilerError.GLOBAL.raiseMessage(
        "General options:\n" +
            "  -v           : display compiler version\n" +
            "  --help       : help\n" 
        );
    GeneralOptions.printHelp();
    DriverOptions.printHelp();
    CodegenOptions.printHelp();
  }
  private void printHelpDevel() {
    printHelp();
    CompilerError.GLOBAL.raiseMessage(
        "General options (for tool developers):\n" +
            "  --help-devel      : help"
        );
    GeneralOptions.printHelpDevel();
    DriverOptions.printHelpDevel();
    CodegenOptions.printHelpDevel();
  }



  // ******************************************************************
  //  processOptions:
  //
  //  Process options of the command line
  // 
  // ******************************************************************
  private void processOptions(String[] args) {
    boolean inputFileMode=false;

    for (int i=0; i<args.length; i++) {

      String option = args[i];

      // It can be an input file or the element following a backend option
      if (inputFileMode) {
        // Put in the list to process only if the file exists
        File file=new File(args[i]);
        if (DriverHelper.checkInputFile(file)) {
          inputFileList.add(file);
        }
      }

      else {

        // End of options marker
        if (option.equals("--")) {
          // Subsequent files are input source files
          inputFileMode=true;
        }

        // Command line help options
        // -------------------------

        // Help
        else if (option.equals("--help")) {
          printHelp();
          CompilerError.GLOBAL.exitNormally();
        }
        else if (option.equals("--help-devel")) {
          printHelpDevel();
          CompilerError.GLOBAL.exitNormally();
        }

        // Version
        else if (option.equals("-v")) {
          printVersion();
          CompilerError.GLOBAL.exitNormally();
        }      

        // Unrecognized option
        //--------------------     
        else {
          // General options
          int nb_general=GeneralOptions.parseOptions(args,i);
          if (nb_general!=0) {
            i+=nb_general-1;
            continue;
          }
          // Driver options
          int nb_driver=DriverOptions.parseOptions(args,i);
          if (nb_driver!=0) {
            i+=nb_driver-1;
            continue;
          }
          // Code generation options
          int nb_codegen=CodegenOptions.parseOptions(args,i);
          if (nb_codegen!=0) {
            i+=nb_codegen-1;
            continue;
          }

          // Considered as backend compiler option
          DriverOptions.getPreprocessorOptionList().add(args[i]);
          DriverOptions.getBackendCompilerOptionList().add(args[i]); 
        }
      }
    }

    // Check for correct option setup
    //-------------------------------
    GeneralOptions.check();
    DriverOptions.check();
    CodegenOptions.check();

    // Check that at least one file is to be processed
    if (inputFileList.size() == 0) {
      CompilerError.GLOBAL.
      raiseFatalError("no file to compile in the command line");
    }

    if (  (inputFileList.size()>1)&&
        (DriverOptions.getOutputFileName()!=null)&&
        (DriverOptions.getStopStage()!=DriverHelper.STAGE.NO)) {
      CompilerError.GLOBAL.
      raiseFatalError("cannot specify -o with -E, -C, -c or -S, with multiple files");
    }

    // Verbosing
    //----------
    if (GeneralOptions.getVerboseLevel()>1) {
      GeneralOptions.verbose();
      DriverOptions.verbose();
      CodegenOptions.verbose();
    }
  }


  // ##################################################################
  //
  //    			 Main
  //
  // ##################################################################

  // For execution from external shell
  public static void main(String[] args) {
    try { new ReentrantC2C().mainNonStatic(args); }
    catch (CompilerExit e) {
      ResourceManager.shutdown();
      System.exit(e.getReturnStatus());
    }
    // In case of normal termination
    ResourceManager.shutdown();
  }

  // For execution from java proxy
  public static int mainNonExit(String[] args) {
    int returnValue=0;

    try { new ReentrantC2C().mainNonStatic(args); }
    catch (CompilerExit e) {
      returnValue=e.getReturnStatus();
    }
    ResourceManager.shutdown();
    return returnValue;
  }

  public void mainNonStatic(String[] args) {

    //==================================================================
    //  Parse the command line and process options
    //==================================================================

    // Process command line options
    processOptions(args);

    //********************************************
    //**** Additional preprocessing options   ****
    //********************************************
    //URL url;
    //url=ClassLoader.getSystemResource("C/macros.h");
    //if (url==null) {
    //  CompilerError.GLOBAL.raiseFatalError("Missing file 'C/macros.hl");      
    //}
    //additionalCPreprocessingOptions.add("-include");
    //additionalCPreprocessingOptions.add(url.getFile());
    //-> External type limits

    //*********************************************
    //**** Additional backend compiler options ****
    //*********************************************

    LinkedList<String> BackendAdditionnalOptionList=new LinkedList<String>();
    //BackendAdditionnalOptionList.add("-lm");
    //BackendAdditionnalOptionList.add("-I"+DriverOptions.optionInstallDir);
    //BackendAdditionnalOptionList.add("-I"+DriverOptions.optionInstallDir+"/lib/include");


    // We do not continue if some errors occurred and are still pending
    CompilerError.GLOBAL.exitIfError();

    // Check that at least one file is to be processed
    if (inputFileList.isEmpty()) {
      CompilerError.GLOBAL.
      raiseFatalError("no file to compiler in the command line");
    }


    //==================================================================
    //  Create the output directory if necessary
    //==================================================================
    DriverHelper.createOutputDirectory(DriverOptions.getOutputDirectoryName());


    //==================================================================
    //  Create the output directory if necessary
    //==================================================================
    String tempDirName="_C2C"+UUID.randomUUID().toString();
    File tempDir=new File(tempDirName);
    try {
      tempDir.mkdirs();
    }
    catch (Exception e) {
      e.printStackTrace();
      CompilerError.GLOBAL.
      raiseFatalError("Can not create the temporary directory: "
      +tempDir.getPath());
    } 
    ResourceManager.registerTempDirectory(tempDir);
    if (GeneralOptions.getDebugLevel() > 0) {
      CompilerError.GLOBAL.raiseMessage("  ... Temporary directory =  '"
          + tempDir.getPath() + "'");
    }

    //==================================================================
    //  The temp directory will be deleted at the JV shutdown 
    //==================================================================
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        if (GeneralOptions.getDebugLevel() > 0) {
          CompilerError.GLOBAL
          .raiseMessage("  ... closing all streams and cleaning temporary files");
        }
        ResourceManager.shutdown();
      }
    });


    try {

      //==================================================================
      //  Build File array for inputs files to process
      //==================================================================
      LinkedList<File> cFileList = new LinkedList<File>();
      LinkedList<File> iFileList = new LinkedList<File>();

      LinkedList<File> backendFileList = new LinkedList<File>();

      for (File file:inputFileList) {
        String path=file.getPath();
        if (path.endsWith(".c")) {
          cFileList.add(file);
        }
        else if (path.endsWith(".i")) {
          iFileList.add(file);
        }
        else if (path.endsWith(".o")) {
          backendFileList.add(file);
        }
        else if (path.endsWith(".a")) {
          backendFileList.add(file);
        }
        else {
          CompilerError.GLOBAL.raiseFatalError("Language of file '"+
              path+"' not recognized");
        }       
      }


      //==================================================================
      // Preprocessing
      //==================================================================

      if (!cFileList.isEmpty()) {
        LinkedList<File> cPreprocOutputFileList;
        cPreprocOutputFileList
        =DriverHelper.runPreprocessor(cFileList,"c","i",tempDir,
            DriverOptions.getPreprocessorTool(),
            DriverOptions.getPreprocessorOptionList(),
            additionalCPreprocessingOptions);

        // Copy back temporary files
        DriverHelper.copyBackTempFiles(DriverHelper.STAGE.PREPROC,
            DriverOptions.getStopStage(), DriverOptions.getKeepIntermediateFiles(),
            cPreprocOutputFileList, null,
            DriverOptions.getOutputFileName(),
            DriverOptions.getOutputDirectoryName());

        // Add the temporary files
        iFileList.addAll(cPreprocOutputFileList);
      }


      //==================================================================
      // C2C stage
      //==================================================================

      // Output C2C files
      LinkedList<File> cC2COutputFileList = null;

      if ((!iFileList.isEmpty())&&
          (DriverOptions.getStopStage()!=DriverHelper.STAGE.PREPROC)) {

        // Create the target files
        cC2COutputFileList=DriverHelper.makeOutputFileList(iFileList,
            "i","c2c.c",tempDir);


        //==================================================================
        //==================================================================

        // List of file engines
        LinkedList<ReentrantEngine> engine_list = new LinkedList<ReentrantEngine>();
        LinkedList<File> additionalGeneratedFiles= new LinkedList<File>();


        // ==================================================================
        // Parse input files, split multiple declarations and generate the
        // symbol table
        // ==================================================================

        // Global mangling counter
        int globalManglingCounter = 0;

        int fileNum=0;
        for (File fileToProcess : iFileList) {      
          // Check for correct input file
          if (!DriverHelper.checkInputFile(fileToProcess)) {
            continue;
          }

          // Output file
          File fileToGenerate=cC2COutputFileList.get(fileNum);

          // -------------------------------
          // Parse the input file
          // -------------------------------
          if (GeneralOptions.getDebugLevel() > 0) {
            CompilerError.GLOBAL.raiseMessage("  ... Compiling (C2C) '"
                + fileToProcess.getName() + "'");
          }

          // C language
          ReentrantEngine eng=new ReentrantEngine(fileToProcess,fileToGenerate,
              GeneralOptions.getVerboseLevel(),GeneralOptions.getDebugLevel(),
              CompilerError.GLOBAL);
          SymbolTableBuilder symbolBuilder=eng.getSymbolTableBuilder();
          engine_list.add(eng); fileNum++;
          // Set the mangling counter
          symbolBuilder.setManglingCounter(globalManglingCounter);
          // Parse and build the symbol table
          eng.parseAndBuildSymbolTable();
          // Update the global mangling counter
          globalManglingCounter = symbolBuilder.getManglingCounter();
        }

        // ==================================================================
        // Perform global Link and global data file generation
        // ==================================================================
        if (GeneralOptions.getDebugLevel() > 0) {
          StringBuffer sb=new StringBuffer();
          boolean flag=false;
          sb.append("  ... Linking (C2C) [");
          for(File file: iFileList) {
            if (flag) sb.append(" ");
            sb.append(file.getName());
            flag=true;
          }
          sb.append("]");
          CompilerError.GLOBAL.raiseMessage(sb.toString());
        }

        ReentrantEngine.link(engine_list,
            DriverOptions.getForceLink(),GeneralOptions.getDebugLevel(),
            CompilerError.GLOBAL);

        // Global data file generation (in the temporary directory)
        File datafileH = DriverHelper.makeOutputFile("DATA.h"  ,tempDir);
        File datafileC = DriverHelper.makeOutputFile("DATA.h.c",tempDir);

        ReentrantEngine.generateGlobalFiles(datafileH, datafileC,
            GeneralOptions.getDebugLevel(),
            CompilerError.GLOBAL);

        additionalGeneratedFiles.add(datafileH);
        additionalGeneratedFiles.add(datafileC);

        // Add the global C file to the backend compilation process
        backendFileList.add(datafileC);


        // ==================================================================
        // Post processing and output C2C file(s) generation
        // ==================================================================
        for(ReentrantEngine eng : engine_list) {
          eng.postLinkAndEmit(DriverOptions.getNoPreprocessor(),
              additionalGeneratedFiles);     
        }


        //==================================================================
        //==================================================================

        // Copy back temporary files
        DriverHelper.copyBackTempFiles(DriverHelper.STAGE.C2C,
            DriverOptions.getStopStage(), DriverOptions.getKeepIntermediateFiles(),
            cC2COutputFileList, additionalGeneratedFiles,
            DriverOptions.getOutputFileName(), DriverOptions.getOutputDirectoryName());

        // Add the temporary files to the backend compilation process
        backendFileList.addAll(cC2COutputFileList);
      }


      //==================================================================
      //  Backend compiler stage
      //==================================================================

      if (
          (!backendFileList.isEmpty())&&
          (DriverOptions.getStopStage()!=DriverHelper.STAGE.PREPROC)&&
          (DriverOptions.getStopStage()!=DriverHelper.STAGE.C2C)
          ) {
        // Add options which were removed from previous compilation stages
        DriverOptions.getBackendCompilerOptionList().addAll(DriverOptions.getBackendSpecificCompilerOptionList());
        // Add AstC2C specific options
        DriverOptions.getBackendCompilerOptionList().addAll(BackendAdditionnalOptionList);

        DriverHelper.runBackendCompiler(backendFileList,
            DriverOptions.getBackendCompiler(),
            DriverOptions.getBackendCompilerOptionList(),
            DriverOptions.getBackendCompilerLibraryOptionList());
      }


    } catch (Exception e) {
      // Delete the temporary directory
      e.printStackTrace();
      CompilerError.GLOBAL.raiseFatalError("");
    } 


  } // void main()



}


