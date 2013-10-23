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

/* Thread for a compilation request of a client */

package proxy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import driver.AstC2C;

public class CompilerThread implements Runnable {
  String optionArray[]=null;

  public int result=0;
  public byte[] bufferedErr=null;
  public byte[] bufferedOut=null;

  CompilerThread(String opt[]) {
    optionArray=opt;
  }

  public void run() {
    // Create buffered output streams and sets ERR and OUT
    ByteArrayOutputStream baos_err = new ByteArrayOutputStream();
    ByteArrayOutputStream baos_out = new ByteArrayOutputStream();
    PrintStream ps_err = new PrintStream(baos_err);
    PrintStream ps_out = new PrintStream(baos_out);
    PrintStream backup_err = System.err;
    PrintStream backup_out = System.out;
    System.setErr(ps_err);
    System.setOut(ps_out);

    // Runs AstC2C
    result = AstC2C.mainNonExit(optionArray);

    // Restores OUT and ERR
    System.setErr(backup_err);
    System.setOut(backup_out);

    // Gets Byte arrays
    bufferedErr = baos_err.toByteArray();
    bufferedOut = baos_out.toByteArray();
  }

}
