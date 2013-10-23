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

package driver.options;

import java.util.LinkedList;

import common.CompilerError;
import driver.DriverHelper.STAGE;

public abstract class DriverOptions {

  static class OptionStorage {
    // Package installation directory
    public String installDir = null;

    // Compilation stage option
    public STAGE stopStage = STAGE.NO;

    // Compilation options
    //--------------------
    // No intermediate files between the C2C and the backend compiler
    public boolean pipe = true;
    // Keep intermediate files
    public boolean keepIntermediateFiles = false;
    // Preprocessor directives
    public boolean noPreprocessor = false;
    // Link option
    public boolean forceLink = false;
    // Parse, analyze, but does not emit
    public boolean noEmit = false;

    // Regeneration options
    //---------------------
    // Debug information
    boolean debugInformation=false;
    // Output directory
    public String outputDirectoryName = ".";
    // Output file
    public String outputFileName = null;

    // Preprocessor compiler
    //----------------------
    public String preprocessorTool = "gcc"; // Default preprocessor compiler
    public LinkedList<String> preprocessorOptionList = new LinkedList<String>();

    // Backend compiler
    //-----------------
    public String backendCompiler = "gcc"; // Default preprocessor and backend compiler
    public LinkedList<String> backendCompilerOptionList
    = new LinkedList<String>();
    public LinkedList<String> backendCompilerLibraryOptionList
    = new LinkedList<String>();
    public LinkedList<String> backendSpecificCompilerOptionList
    = new LinkedList<String>();
  }

  private static InheritableThreadLocal<OptionStorage> options = new InheritableThreadLocal<OptionStorage>() {
    @Override
    protected OptionStorage initialValue() {
      return new OptionStorage();
    }
  };

  // Option accessors
  public static String getInstallDir() {
    return options.get().installDir;
  }
  public static void setInstallDir(String s) {
    options.get().installDir=s;
  }
  public static STAGE getStopStage() {
    return options.get().stopStage;
  }
  public static void setStopStage(STAGE s) {
    options.get().stopStage=s;
  }
  public static boolean getPipe() {
    return options.get().pipe;
  }
  public static void setPipe(boolean s) {
    options.get().pipe=s;
  }
  public static boolean getKeepIntermediateFiles() {
    return options.get().keepIntermediateFiles;
  }
  public static void setKeepIntermediateFiles(boolean s) {
    options.get().keepIntermediateFiles=s;
  }
  public static boolean getNoPreprocessor() {
    return options.get().noPreprocessor;
  }
  public static void setNoPreprocessor(boolean s) {
    options.get().noPreprocessor=s;
  }
  public static boolean getForceLink() {
    return options.get().forceLink;
  }
  public static void setForceLink(boolean s) {
    options.get().forceLink=s;
  }
  public static boolean getNoEmit() {
    return options.get().noEmit;
  }
  public static void setNoEmit(boolean s) {
    options.get().noEmit=s;
  }
  public static boolean getDebugInformation() {
    return options.get().debugInformation;
  }
  public static void setDebugInformation(boolean s) {
    options.get().debugInformation=s;
  }
  public static String getOutputDirectoryName() {
    return options.get().outputDirectoryName;
  }
  public static void setOutputDirectoryName(String s) {
    options.get().outputDirectoryName=s;
  }
  public static String getOutputFileName() {
    return options.get().outputFileName;
  }
  public static void setOutputFileName(String s) {
    options.get().outputFileName=s;
  }
  public static String getPreprocessorTool() {
    return options.get().preprocessorTool;
  }
  public static void setPreprocessorTool(String s) {
    options.get().preprocessorTool=s;
  }
  public static LinkedList<String> getPreprocessorOptionList() {
    return options.get().preprocessorOptionList;
  }
  public static String getBackendCompiler() {
    return options.get().backendCompiler;
  }
  public static void setBackendCompiler(String s) {
    options.get().backendCompiler=s;
  }
  public static LinkedList<String> getBackendCompilerOptionList() {
    return options.get().backendCompilerOptionList;
  }
  public static LinkedList<String> getBackendCompilerLibraryOptionList() {
    return options.get().backendCompilerLibraryOptionList;
  }
  public static LinkedList<String> getBackendSpecificCompilerOptionList() {
    return options.get().backendSpecificCompilerOptionList;
  }


  public static int parseOptions(String[] args, int i) {
    String option = args[i];

    // Package installation directory 
    // ------------------------------
    if (option.equals("--install_dir")) {
      i++;
      if (i==args.length) {
        CompilerError.GLOBAL.raiseFatalError("while parsing options: missing <path> after option '" + option + "'");
      }
      setInstallDir(args[i]);
      return 2;
    }

    // Preprocessor device 
    else if (option.startsWith("-D") || option.startsWith("-I")) {
      getPreprocessorOptionList().add(args[i]);
      return 1;
    }

    // Compilation stages
    else if (option.equals("-E")) {
      if (getStopStage()!=STAGE.NO) {
        CompilerError.GLOBAL.raiseFatalError("cannot specify together -E, -C, -c and -S");
      }
      setStopStage(STAGE.PREPROC);
      return 1;
    }
    else if (option.equals("-C")) {
      if (getStopStage()!=STAGE.NO) {
        CompilerError.GLOBAL.raiseFatalError("cannot specify together -E, -C, -c and -S");
      }
      setStopStage(STAGE.C2C);
      return 1;
    }

    else if (option.equals("--parse")) {
      setStopStage(STAGE.C2C);
      setNoEmit(true);
      return 1;
    }

    // Keep intermediate file
    else if (option.equals("--keep")) {
      setKeepIntermediateFiles(true);
      return 1;
    }

    // No preprocessor directive in the C2C generated file
    else if (option.equals("--nopreproc")) {
      setNoPreprocessor(true);
      return 1;
    }

    // Link option
    else if (option.equals("--forcelink")) {
      setForceLink(true);
      return 1;
    }

    // Pipe with the backend compiler
    else if (option.equals("--nopipe")) {
      setPipe(false);
      return 1;
    }


    // Backend compiler configuration option
    // -------------------------------------
    else if (option.equals("--backendcc")) {
      i++;
      if (i==args.length) {
        CompilerError.GLOBAL.
        raiseFatalError("while parsing options: missing <path> after option '" + option + "'");
      }
      setBackendCompiler(args[i]);
      return 2;
    }

    // Backend compilation options
    // ---------------------------
    else if (option.equals("-c") || option.equals("-S")) {
      if (getStopStage()!=STAGE.NO) {
        CompilerError.GLOBAL.raiseFatalError("cannot specify together -E, -C, -c and -S");
      }
      // Pass this option also to the backend compiler
      getBackendSpecificCompilerOptionList().add(option);
      return 1;
    }

    //  Regeneration options
    // ---------------------
    // Debug option
    else if (option.equals("-g")) {
      setDebugInformation(true);
      // This option must also be passed to other compilation stages
      getPreprocessorOptionList().add("-g");
      getBackendCompilerOptionList().add("-g");
      return 1;

    }

    else if (option.equals("-o")) {
      i++;
      if (i==args.length) {
        CompilerError.GLOBAL.raiseFatalError("while parsing options: missing a program name after option '" + option + "'");
      }
      if (getOutputFileName()!=null) {
        CompilerError.GLOBAL.raiseFatalError("output option specified twice");
      }
      setOutputFileName(args[i]);
      // Pass this option also to the backend compiler (if the compilation goes to the backend compiler)
      getBackendSpecificCompilerOptionList().add(option);
      getBackendSpecificCompilerOptionList().add(args[i]);
      return 2;
    }

    else if (option.equals("--outdir")) {
      i++;
      if (i==args.length) {
        CompilerError.GLOBAL.raiseFatalError("while parsing options: missing <name> after option '" + option + "'");
      }
      setOutputDirectoryName(args[i]);
      return 2;
    }
    else if (args[i].startsWith("-L") || args[i].startsWith("-l")) {
      getBackendCompilerLibraryOptionList().add(args[i]);
      if (args[i].equals("-L")) {
        // Take also the next argument of -L alone
        i++;
        if (i==args.length) {
          CompilerError.GLOBAL.raiseFatalError("while parsing options: missing an argument after option '-L'");
        }
        getBackendCompilerLibraryOptionList().add(args[i]);
        return 2;
      }
      return 1;
    }

    return 0;
  }

  
  public static void check() {
    if (getInstallDir()==null) {
      CompilerError.GLOBAL.
      raiseFatalError("option --install_dir is mandatory");
    }


    if ((getStopStage()==STAGE.C2C) && (getPipe())) {
      // Deactivate the pipe option
      setPipe(false);
    }
  }

  public static void verbose() {
    CompilerError.GLOBAL.
    raiseMessage("Option passed to the preprocessor : "
        + getPreprocessorOptionList().toString());
    CompilerError.GLOBAL.
    raiseMessage("Option passed to the backend compiler : "
        + getBackendCompilerOptionList().toString());
    CompilerError.GLOBAL.
    raiseMessage("Library options passed to the backend compiler : "
        + getBackendCompilerLibraryOptionList().toString());
  }


  public static void printHelp() {
    CompilerError.GLOBAL.raiseMessage(
        "Driver options:\n" +
            "  -E                : stop the compilation process after the preprocessing\n" +
            "  -C                : stop the compilation process after the low-level kernel generation\n" +
            "  --keep            : keep intermediate files\n" +
            "  -o <programName>  : name of the generated program\n" +
            "  --outdir <name>   : specifies the output directory of generated files"
        );
  }
  public static void printHelpDevel() {
    CompilerError.GLOBAL.raiseMessage(
            "Driver options (for tool developers):\n" +
            "  --nopreproc       : does not regenerate preprocessing directives (development option)\n" +
            "  --parse           : parse and check but does not generate any output file" +
            "  --nopipe           : file instead of pipe between the C2C and the backend compiler\n" +
            "  --backendcc <path> : sets the backend C compiler (default is 'gcc')\n"
       );
  }
  
}
