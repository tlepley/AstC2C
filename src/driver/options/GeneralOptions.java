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

import common.CompilerError;

public abstract class GeneralOptions {
  // Thread local storage
  static class OptionStorage {
    public int verboseLevel = 0;
    public int debugLevel = 0;
  }

  private static InheritableThreadLocal<OptionStorage> options = new InheritableThreadLocal<OptionStorage>() {
    @Override
    protected OptionStorage initialValue() {
      return new OptionStorage();
    }
  };
  public static int getVerboseLevel() {
    return options.get().verboseLevel;
  }
  public static void setVerboseLevel(int s) {
    options.get().verboseLevel=s;
  }
  public static int getDebugLevel() {
    return options.get().debugLevel;
  }
  public static void setDebugLevel(int s) {
    options.get().debugLevel=s;
  }


  public static int parseOptions(String[] args, int i) {
    String option = args[i];

    if (option.startsWith("-")) {
      // Verbose
      if (option.equals("--verbose")) {
        i++;
        if (i==args.length) {
          CompilerError.GLOBAL.raiseFatalError("while parsing options: missing <level> after option '" + option + "'");
        }
        try {
          setVerboseLevel(Integer.valueOf(args[i]));
        }
        catch (NumberFormatException e) {
          CompilerError.GLOBAL.raiseWarning("while parsing options: the option verbose level '" + args[i] + "' is not a number");
          setVerboseLevel(0);
        }
        CompilerError.GLOBAL.setVerboseLevel(getVerboseLevel());
        return 2;
      }

      // Debug
      else if (option.equals("--debug")) {
        i++;
        if (i==args.length) {
          CompilerError.GLOBAL.raiseFatalError("while parsing options: missing <level> after option '" + option + "'");
        }
        try {
          setDebugLevel(Integer.valueOf(args[i]));
        }
        catch (NumberFormatException e) {
          CompilerError.GLOBAL.raiseWarning("while parsing options: the option debug level '" + args[i] + "' is not a number");
          setDebugLevel(0);
        }
        return 2;
      }

    }

    return 0;  
  }

  public static void printHelp() {
    CompilerError.GLOBAL.raiseMessage(
        "  --verbose <level> : display more warnings for application developper"
        );
  }
  public static void printHelpDevel() {
    CompilerError.GLOBAL.raiseMessage(
        "  --debug <level>   : display information for tool developper"
        );
  }
  
  // Nothing to check
  public static void check() { }
  
  // Nothing to verbose
  public static void verbose() { }

}
