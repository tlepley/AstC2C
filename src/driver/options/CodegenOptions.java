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

public abstract class CodegenOptions {
  // Thread local storage
 static class OptionStorage {
		String targetDeviceName = "host";
		String targetSystemName = "middleware";
	}
	
	private static InheritableThreadLocal<OptionStorage> options = new InheritableThreadLocal<OptionStorage>() {
		@Override
		protected OptionStorage initialValue() {
			return new OptionStorage();
		}
	};
	public static String getTargetDeviceName() {
		return options.get().targetDeviceName;
	}
	public static void setTargetDeviceName(String s) {
		options.get().targetDeviceName=s;
	}
	public static String getTargetSystemName() {
		return options.get().targetSystemName;
	}
	public static void setTargetSystemName(String s) {
		options.get().targetSystemName=s;
	}
  

  
  //========================================================
  // Command line processing
  //========================================================
 
  static public int parseOptions(String[] args, int i) {
    String option = args[i];

    if (option.startsWith("-")) {
      // General options
      // ---------------

      // Target device 
      // -------------
      if (option.equals("--target_device")) {
        i++;
        if (i==args.length) {
          CompilerError.GLOBAL.raiseFatalError("while parsing options: missing device name after option '" + option + "'");
        }
        setTargetDeviceName(args[i]);
        return 2;
      }

      else if (option.equals("--target_system")) {
        i++;
        if (i==args.length) {
          CompilerError.GLOBAL.raiseFatalError("while parsing options: missing system name after option '" + option + "'");
        }
        setTargetSystemName(args[i]);
        return 2;
      }

    }
    return 0;  
  }

  static public void printHelp() {
    CompilerError.GLOBAL.raiseMessage(
        "Code generation options:\n" +
        "  -g                : generate debug information\n"
        );
  }

  static public void printHelpDevel() {
    CompilerError.GLOBAL.raiseMessage(
        "Code generation options (for tool developers):\n" +
        "  --target_device <name> : target device\n"+
        "  --target_system <name> : target system\n" 
        );
  }

  // Nothing to check
  public static void check() { }
  
  // Nothing to verbose
  public static void verbose() { }

}
