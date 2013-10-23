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

/* Compiler server */

package proxy;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import common.ResourceManager;

public class ProxyServer {

  private static int portNumber = 22318;

  private String optionInstallDir = null;
  private int verboseLevel = 0;

  // ******************************************************************
  // printHelp:
  //
  // Display to the tool options for the application developer
  //
  // ******************************************************************
  private void printHelp() {
    System.err.println("CLAM OpenCL C compiler server");
    System.err.println("Command : clamc_proxy [options]*");
    System.err.println("  --help             : help");
    System.err.println("  --verbose <level>  : degree of verbosing");
    System.err.println();
  }

  // ******************************************************************
  // processOptions:
  //
  // Process options of the command line
  //
  // ******************************************************************
  private void processOptions(String[] args) {

    for (int i = 0; i < args.length; i++) {
      String option = args[i];

      // Help
      if (option.equals("--help")) {
        printHelp();
        System.exit(0);
      }

      // Package installation directory
      else if (option.equals("--verbose")) {
        i++;
        if (i == args.length) {
          System.err
          .println("while parsing options: missing <path> after option '"
              + option + "'");
        }
        try {
          verboseLevel = Integer.valueOf(args[i]);
        } catch (NumberFormatException e) {
          System.err.println("while parsing options: the option debug level '"
              + args[i] + "' is not a number");
        }
      }

      // Package installation directory
      else if (option.equals("--install_dir")) {
        i++;
        if (i == args.length) {
          System.err
          .println("while parsing options: missing <path> after option '"
              + option + "'");
        }
        optionInstallDir = args[i];
      }

      else {
        // Unrecognized option
        System.err.println("unknown option '" + option + "'");
      }
    }

    // Perform global checks
    // ----------------------
    if (optionInstallDir == null) {
      System.err.println("option --install_dir is mandatory");
    }
  }

  // ##################################################################
  //
  // Main
  //
  // ##################################################################

  public static void main(String[] args) {
    // Attach the resource manager shutdown service to the JVM
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        ResourceManager.shutdown(true);
      }
    });

    // Non static main
    ProxyServer a = new ProxyServer();
    a.mainNonStatic(args);
  }

  public void mainNonStatic(String[] args) {
    processOptions(args);

    ServerSocket mainSocket = null;
    try {
      mainSocket = new ServerSocket(portNumber);
    } catch (Exception e) {
      System.err.println("Can not open the server socket (port " + portNumber
          + ")");
      System.exit(1);
    }

    // Create and register the output directory
    String tempDirName = "/tmp/ClamProxy" + UUID.randomUUID().toString();
    File tempDir = new File(tempDirName);
    try {
      tempDir.mkdirs();
    } catch (Exception e) {
      System.err.println("Can not create temporary directory :"
          + tempDir.getPath());
      System.exit(1);
    }
    ResourceManager.registerTempDirectory(tempDir);

    if (verboseLevel>0) {
      System.out.println(
          "+-------------------------------+\n"+
          "| CLAM OpenCL C compiler server |\n"+
          "| (c) STMicroelectronics        |\n"+
          "+-------------------------------+\n");
    }
    if (verboseLevel>1) {
      System.out.println("Server port : "+portNumber);
      System.out.println();
    }

    // Today, we manage only one connection at a time to avoid
    // interactions on system.err and system.out
    try {
      while (true) {
        Socket sock = mainSocket.accept();

        // Got a client
        if (verboseLevel > 0) {
          System.out.println("Connected to client : "
              + sock.getInetAddress().toString());
        }

        // Create a thread for the client
        ServerThread p = new ServerThread(sock, tempDir, optionInstallDir, verboseLevel);
        Thread thread = new Thread(p);
        thread.start();
        thread.join();
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

}
