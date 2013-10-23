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

import java.io.File;
import common.CompilerError;
import java.util.LinkedList;


public abstract class AstC2CEngine extends CommonEngine {

  public enum MODE {STANDARD, SPLIT};

  // ##################################################################
  //  Constructor
  // ##################################################################

  protected AstC2CEngine(File input_file, File output_file,
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
  public abstract void parseAndBuildSymbolTable(TARGET_ABI targetDevice, MODE mode);

  //------------------------------------------------------------------
  // postLinkAndEmit :
  //
  // Function performing processing after program link and generating
  // output file(s)
  //------------------------------------------------------------------
  public abstract void postLinkAndEmit(TARGET_ABI targetDevice,
				       boolean noPreprocessor,
				       boolean vec_emul,
				       LinkedList<File> additionalGeneratedFiles);
}
