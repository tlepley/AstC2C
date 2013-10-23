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

/* Thread for a client of the compiler server */

package proxy;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerThread implements Runnable {
  private File tempDir = null;
  private Socket socket;
  private int errorCode = 0;
  private static int counter = 0;
  private String installDir = null;
  private int verboseLevel = 0;

  private static synchronized int getCounter() {
    return counter++;
  }

  ServerThread(Socket s, File temp, String id, int v) {
    socket = s;
    tempDir = temp;
    installDir = id;
    verboseLevel = v;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void run() {
    try {
      InputStream is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();

      while (true) {
        // Input and output files
        String kernelName = "kernel" + getCounter();
        String inputFileName = tempDir + "/" + kernelName + ".cl";
        String outputFileName = tempDir + "/" + kernelName + ".so";
        File inputFile = new File(inputFileName);
        File outputFile = new File(outputFileName);

        // ------------------------------------------------------------
        // READ
        // ------------------------------------------------------------

        if (verboseLevel > 1) {
          System.out.println("> Waiting for request ...");
        }

        // -- Read request ID options
        long requestID = 0;
        try {
          requestID = ProxyProtocol.readLong(is);
        } catch (EndOfFileException e1) {
          // Not more requests from the client
          // Simply stop the thread
          errorCode = 0;
          System.err
              .println("socket closed by client : proxy Thread terminated");
          return;
        }

        // Stop server when ID==-1
        if (requestID == -1) {
          if (verboseLevel > 0) {
            System.out.println("Shutdown command received from the client");
          }
          return;
        }

        if (verboseLevel > 0) {
          System.out.println("> got compilation request ID : " + requestID);
        }

        // -- Read compilation options
        int optionSize = ProxyProtocol.readInt(is);
        String compilerOptions = ProxyProtocol.readString(is, optionSize);
        // System.err.println("  >> Options retrieved ("+optionSize+" characters)");
        // for(String s:optionArray) {System.err.println("       + " + s); }

        // -- Read the input program
        int programSize = ProxyProtocol.readInt(is);
        if (verboseLevel > 1) {
          System.out.println("  >> Program size = " + programSize + " bytes");
        }
        // System.err.println("  >> Saving program to = "+inputFileName);
        ProxyProtocol.streamToFile(is, programSize, inputFile);

        // ------------------------------------------------------------
        // RUN
        // ------------------------------------------------------------

        String optionString = "--install_dir " + installDir + " "
        + compilerOptions + " -o " + outputFileName ;
        if (verboseLevel>2) {
          optionString += " --verbose " + (verboseLevel-1);
        }
        optionString += " -- " + inputFileName;
        
        String[] optionArray = optionString.split(" +");
        if (verboseLevel > 1) {
          System.out.print("  >> Executing astc2c with options = ");
          for (String s : optionArray) {
            System.out.print(" '" + s + "'");
          }
          System.out.println();
        } else if (verboseLevel > 0) {
          System.out.println("  >> Executing astc2c");
        }
        
        // Create and run the compiler as a thread
        // (to preserve compiler thread local storage)
        CompilerThread ct = new CompilerThread(optionArray);
        Thread thread = new Thread(ct);
        thread.start(); // Start
        thread.join();  // Wait
        
        // Get compiler thread results
        int result=ct.result;
        byte[] bufferedErr = ct.bufferedErr;
        byte[]  bufferedOut= ct.bufferedOut;

        if (verboseLevel > 0) {
          if (result == 0) {
            System.out.println("    >>> command completed successfully");
          } else {
            System.out.println("    >>> command completed with error ("
                + result + ")");
          }
        }
        if (verboseLevel > 1) {
          System.out.println("    >>> got " + bufferedOut.length
              + " bytes on 'out'");
          System.out.println(new String(bufferedOut));
          System.out.println("    >>> got " + bufferedErr.length
              + " bytes on 'err'");
          System.out.println(new String(bufferedErr));
        }

        // ------------------------------------------------------------
        // WRITE
        // ------------------------------------------------------------
        if (verboseLevel > 0) {
          System.err.println("  >> sending back results to client");
        }

        // -- Write request ID
        ProxyProtocol.writeLong(os, requestID);
        // Write compilation result
        ProxyProtocol.writeInt(os, result);
        // Write stdout
        ProxyProtocol.writeInt(os, bufferedOut.length);
        os.write(bufferedOut);
        // Write stderr
        ProxyProtocol.writeInt(os, bufferedErr.length);
        os.write(bufferedErr);
        // Write binary
        if (result == 0) {
          ProxyProtocol.writeInt(os, (int) outputFile.length());
          ProxyProtocol.fileToStream(os, outputFile);
        } else {
          ProxyProtocol.writeInt(os, 0);
        }

        // Cleanup temporary files
        inputFile.delete();
        outputFile.delete();
      }
    } catch (final Exception e) {
      errorCode = 1;
      e.printStackTrace();
      System.err.println("ASTC2C Proxy Thread terminated with error");
      return;
    }
  }

}
