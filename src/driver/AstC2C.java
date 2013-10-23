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
import engine.AstC2CEngine;
import engine.CEngine;
import engine.OclEngine;
import java.io.File;
import java.util.LinkedList;
import java.util.UUID;
import java.net.URL;

import driver.options.CodegenOptions;
import driver.options.DriverOptions;
import driver.options.GeneralOptions;

import utility.thread.ExecHelper;


public class AstC2C {

  // ##################################################################
  //  Options management
  // ##################################################################

  // OpenCL Vector handling
  public static boolean optionVectorEmulation = false;

  // Additional preprocessing options (builtins)
  LinkedList<String> additionalOclPreprocessingOptions
  =new LinkedList<String>();
  LinkedList<String> additionalCPreprocessingOptions
  =new LinkedList<String>();

  // List of input files
  //--------------------
  LinkedList<File> inputFileList = new LinkedList<File>();

  // Regeneration options
  //---------------------
  // Declaration split
  protected AstC2CEngine.MODE optionMode = AstC2CEngine.MODE.STANDARD;

  // Target device options
  //----------------------
  // By default, target device is 32 bits
  AstC2CEngine.TARGET_ABI optionTargetDevice
  = AstC2CEngine.TARGET_ABI.ILP32;

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

  // ******************************************************************
  //  Help display:
  // ******************************************************************
  private void printHelp() {
    CompilerError.GLOBAL.raiseMessage("AstC2C compiler");
    printVersion();
    CompilerError.GLOBAL.raiseMessage("Command : astc2c [options]* [input files]+");
    CompilerError.GLOBAL.raiseMessage(
        "General options:\n" +
            "  -v           : display compiler version\n" +
            "  --help       : help\n" 
        );
    GeneralOptions.printHelp();
    DriverOptions.printHelp();
    CodegenOptions.printHelp();
    CompilerError.GLOBAL.raiseMessage(
        "  --split           : split symbol declarations in order to get a more\n" +
        "  --vec_emul        : vector emulation in the generated code\n"
        );
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

        // Specific regeneration option
        // ----------------------------
        else if (option.equals("--split")) {
          optionMode = AstC2CEngine.MODE.SPLIT;
        }
        else if (option.equals("--vec_emul")) {
          optionVectorEmulation = true;
        }


        // Target device: Overrides the common driver option
        // -------------------------------------------------
        else if (option.equals("--target_device")) {
          i++;
          if (i==args.length) {
            CompilerError.GLOBAL.
            raiseFatalError("while parsing options: missing <path> after option '" + option + "'");
          }
          String targetDevice=args[i].toLowerCase();
          if (targetDevice.equals("ilp32")) {
            optionTargetDevice=AstC2CEngine.TARGET_ABI.ILP32;
          }
          else if (targetDevice.equals("lp64")) {
            optionTargetDevice=AstC2CEngine.TARGET_ABI.LP64;
          }
          else {
            CompilerError.GLOBAL.
            raiseFatalError("Unknown target device '" + args[i] + "'");
          }
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
  //    			               Main
  //
  // ##################################################################

  // For execution from external shell
  public static void main(String[] args) {
    try { new AstC2C().mainNonStatic(args); }
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

    try { new AstC2C().mainNonStatic(args); }
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
    
    //$$$$$$$$$$$$$$$$$$$$$$ OpenCL specific $$$$$$$$$$$$$$$$$$$$$$$$$$$
    // Additional preprocessing options
    URL url;
    //-> Generic OpenCL macros
    url=ClassLoader.getSystemResource("OCL/macros.hl");
    if (url==null) {
      CompilerError.GLOBAL.raiseFatalError("Missing file 'OCL/macros.hl");      
    }
    additionalOclPreprocessingOptions.add("-include");
    additionalOclPreprocessingOptions.add(url.getFile());
    //-> External type limits
    url=ClassLoader.getSystemResource("OCL/limits.hl");
    if (url==null) {
      CompilerError.GLOBAL.raiseFatalError("Missing file 'OCL/limits.hl");      
    }
    additionalOclPreprocessingOptions.add("-include");
    additionalOclPreprocessingOptions.add(url.getFile());
    //-> External float macros
    url=ClassLoader.getSystemResource("OCL/float.hl");
    if (url==null) {
      CompilerError.GLOBAL.raiseFatalError("Missing file 'OCL/float.hl");      
    }
    additionalOclPreprocessingOptions.add("-include");
    additionalOclPreprocessingOptions.add(url.getFile());
    //-> External float macros
    url=ClassLoader.getSystemResource("OCL/bool.hl");
    if (url==null) {
      CompilerError.GLOBAL.raiseFatalError("Missing file 'OCL/bool.hl");      
    }
    additionalOclPreprocessingOptions.add("-include");
    additionalOclPreprocessingOptions.add(url.getFile());
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    
    // Additional options for the backend compiler
    LinkedList<String> BackendAdditionnalOptionList=new LinkedList<String>();
    BackendAdditionnalOptionList.add("-lm");
    BackendAdditionnalOptionList.add("-I"+DriverOptions.getInstallDir());
    BackendAdditionnalOptionList.add("-I"+DriverOptions.getInstallDir()+"/lib/include");

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
          + tempDir.getPath());
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
      LinkedList<File> clFileList = new LinkedList<File>();
      LinkedList<File> iclFileList = new LinkedList<File>();

      LinkedList<File> cFileList = new LinkedList<File>();
      LinkedList<File> iFileList = new LinkedList<File>();

      LinkedList<File> backendFileList = new LinkedList<File>();

      for (File file:inputFileList) {
        String path=file.getPath();
        if (path.endsWith(".cl")) {
          clFileList.add(file);
        }
        else if (path.endsWith(".icl")) {
          iclFileList.add(file);
        }
        else if (path.endsWith(".c")) {
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

      LinkedList<File> additionalOutputFileList;


      //==================================================================
      // Preprocessing
      //==================================================================

      // -> OCL files
      if (!clFileList.isEmpty()) {
        LinkedList<File> oclPreprocOutputFileList;

        oclPreprocOutputFileList
        =DriverHelper.runPreprocessor(clFileList,"cl","icl",tempDir,
            DriverOptions.getPreprocessorTool(),
            DriverOptions.getPreprocessorOptionList(),
            additionalOclPreprocessingOptions
            );

        // Copy back temporary files
        DriverHelper.copyBackTempFiles(DriverHelper.STAGE.PREPROC, 
            DriverOptions.getStopStage(), DriverOptions.getKeepIntermediateFiles(),
            oclPreprocOutputFileList, null,
            DriverOptions.getOutputFileName(),
            DriverOptions.getOutputDirectoryName());

        // Add the temporary files to the existing icl file list
        iclFileList.addAll(oclPreprocOutputFileList);
      }

      // -> C files
      if (!cFileList.isEmpty()) {
        LinkedList<File> cPreprocOutputFileList;
        cPreprocOutputFileList
        =DriverHelper.runPreprocessor(cFileList,"c","i",tempDir,
            DriverOptions.getPreprocessorTool(),
            DriverOptions.getPreprocessorOptionList(),
            additionalCPreprocessingOptions
            );

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

      Process process=null;
      LinkedList<File> oclC2COutputFileList=new LinkedList<File>(),
          cC2COutputFileList=new LinkedList<File>();

      if (
          ((!iFileList.isEmpty())||(!iclFileList.isEmpty())) &&
          (DriverOptions.getStopStage()!=DriverHelper.STAGE.PREPROC)
          ) {

        // -> OCL files
        if (!iclFileList.isEmpty()) {
          // Create target files
          oclC2COutputFileList=DriverHelper.makeOutputFileList(iclFileList,
              "icl","cl.c",
              tempDir);
          // Add the temporary files
          backendFileList.addAll(oclC2COutputFileList);
        }
        
        // -> C files
        if (!iFileList.isEmpty()) {
          cC2COutputFileList=DriverHelper.makeOutputFileList(iFileList,
              "i","c2c.c",
              tempDir);
          // Add the temporary files
          backendFileList.addAll(cC2COutputFileList);
        }

        if (DriverOptions.getPipe() &&
            (DriverOptions.getStopStage()!=DriverHelper.STAGE.C2C)) {
          // Create named output pipes
          LinkedList<String> cmdList=new LinkedList<String>();
          cmdList.add("mkfifo");
          for (File file:oclC2COutputFileList) { cmdList.add(file.getPath()); }
          for (File file:cC2COutputFileList)  { cmdList.add(file.getPath()); }
          try {
            if ((ExecHelper.exec(cmdList))!=0) {
              CompilerError.GLOBAL
              .raiseFatalError("Problem while creating output pipes");
            }
          }
          catch (Exception e) {
            CompilerError.GLOBAL
            .raiseFatalError("Problem while creating output pipes");
          }

          // Add options which were removed from previous compilation stages
          DriverOptions.getBackendCompilerOptionList().addAll(DriverOptions.getBackendSpecificCompilerOptionList());
          // Add AstC2C specific options
          DriverOptions.getBackendCompilerOptionList().addAll(BackendAdditionnalOptionList);

          process=DriverHelper.runBackendCompilerAsProcess(backendFileList,
              DriverOptions.getBackendCompiler(),
              DriverOptions.getBackendCompilerOptionList(),
              DriverOptions.getBackendCompilerLibraryOptionList());
        }

        // -> OCL files
        if (!iclFileList.isEmpty()) {
          additionalOutputFileList = runC2C(
              // Compilation option
              DriverOptions.getForceLink(),

              // Language option
              true,	// oclOption

              // Regeneration options
              optionMode,
              DriverOptions.getNoPreprocessor(),

              // Files to process
              iclFileList,
              oclC2COutputFileList
              );

          if (!DriverOptions.getPipe()) {
            // Copy back temporary files
            DriverHelper.copyBackTempFiles(DriverHelper.STAGE.C2C, DriverOptions.getStopStage(), DriverOptions.getKeepIntermediateFiles(),
                oclC2COutputFileList, additionalOutputFileList,
                DriverOptions.getOutputFileName(), DriverOptions.getOutputDirectoryName());
          }
        }

        // -> C files
        if (!iFileList.isEmpty()) {
          additionalOutputFileList = runC2C(
              // Compilation option
              DriverOptions.getForceLink(),

              // Language option
              false,	// oclOption

              // Regeneration options
              optionMode,
              DriverOptions.getNoPreprocessor(),

              // Files to process
              iFileList,
              cC2COutputFileList
              );

          // Copy back temporary files
          if (!DriverOptions.getPipe()) {
            DriverHelper.copyBackTempFiles(DriverHelper.STAGE.C2C, DriverOptions.getStopStage(), DriverOptions.getKeepIntermediateFiles(),
                cC2COutputFileList, additionalOutputFileList,
                DriverOptions.getOutputFileName(), DriverOptions.getOutputDirectoryName());
          }
        }
      }


      //==================================================================
      //  Backend compiler stage
      //==================================================================

      if (process!=null) {
        // A pipe is used between the C2C compiler and the backend compiler
        // -> waits for backend compilation
        final int rValue = process.waitFor();
        // Note: we may also wait for out/err reader threads too here
        if (rValue!=0) {
          CompilerError.GLOBAL.exitWithError(rValue);
        }
      }
      else {
        // Standard file generation between the C2C compiler and the backend compiler
        if (
            (!backendFileList.isEmpty())&&
            (DriverOptions.getStopStage()!=DriverHelper.STAGE.PREPROC)&&(DriverOptions.getStopStage()!=DriverHelper.STAGE.C2C)
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
      }


    } catch (Exception e) {
      // Delete the temporary directory
      e.printStackTrace();
      CompilerError.GLOBAL.raiseFatalError("");
    } 


  } // void main()



  // ******************************************************************
  // * runC2C :                                                       *
  // *                                                                *
  // * Generic entry function for the C2C. Must be called by a        *
  // * wrapper for execution by command line, higher level compiler   *
  // * etc...                                                         *
  // *                                                                *
  // ******************************************************************
  protected LinkedList<File> runC2C(
      // Compiler option
      boolean optionLink,

      // Language option
      boolean oclOption,

      // Regeneration options
      AstC2CEngine.MODE mode,
      boolean noPreprocessor,

      // Files to process
      LinkedList<File> inputFiles,
      LinkedList<File> outputFiles
      ) {
    // List of file engines
    LinkedList<AstC2CEngine> engine_list = new LinkedList<AstC2CEngine>();
    LinkedList<File> additionalGeneratedFiles= new LinkedList<File>();


    // ==================================================================
    // Parse input files, split multiple declarations and generate the
    // symbol table
    // ==================================================================

    int fileNum=0;
    for (File fileToProcess : inputFiles) {      
      // Check for correct input file
      if (!DriverHelper.checkInputFile(fileToProcess)) {
        continue;
      }

      // Output file
      File fileToGenerate=outputFiles.get(fileNum);


      // -------------------------------
      // Parse the input file
      // -------------------------------
      if (GeneralOptions.getDebugLevel() > 0) {
        CompilerError.GLOBAL.raiseMessage("  ... Compiling (C2C) '"
            + fileToProcess.getName() + "'");
      }

      AstC2CEngine eng;
      if (oclOption) {
        // OpenCL C language
        eng=new OclEngine(fileToProcess,fileToGenerate,
            GeneralOptions.getVerboseLevel(),GeneralOptions.getDebugLevel(),
            CompilerError.GLOBAL);
        eng.parseAndBuildSymbolTable(optionTargetDevice,mode);
      }
      else {
        // C language
        eng=new CEngine(fileToProcess,fileToGenerate,
            GeneralOptions.getVerboseLevel(),GeneralOptions.getDebugLevel(),
            CompilerError.GLOBAL);
        eng.parseAndBuildSymbolTable(optionTargetDevice,mode);
      }
      engine_list.add(eng);

      fileNum++;
    }


    // ==================================================================
    // Perform global Link
    // ==================================================================
    if (GeneralOptions.getDebugLevel() > 0) {
      StringBuffer sb=new StringBuffer();
      boolean flag=false;
      sb.append("  ... Linking (C2C) [");
      for(File file: inputFiles) {
        if (flag) sb.append(" ");
        sb.append(file.getName());
        flag=true;
      }
      sb.append("]");
      CompilerError.GLOBAL.raiseMessage(sb.toString());
    }

    CEngine.link(engine_list,optionLink,
        GeneralOptions.getDebugLevel(),CompilerError.GLOBAL);


    // ==================================================================
    // Post processing and output file(s) generation
    // ==================================================================
    for(AstC2CEngine eng : engine_list) {
      eng.postLinkAndEmit(optionTargetDevice,noPreprocessor,
          optionVectorEmulation, additionalGeneratedFiles);     
    }

    // Return the additional file list
    if (additionalGeneratedFiles.size()==0) {
      return null;
    }
    else {
      return additionalGeneratedFiles;
    }
  }

}


