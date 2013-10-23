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

package common;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import utility.env.FileUtilities;

public class ResourceManager {

  //--------------------------------------------------------------------
  // ABI as a local thread storage (one compiler instance)
  //--------------------------------------------------------------------
  // By default, both source and target are 32 bits C ABI
  private static InheritableThreadLocal<LinkedList<File>> tempDirList = new InheritableThreadLocal<LinkedList<File>>() {
    @Override
    protected LinkedList<File> initialValue() {
      return new LinkedList<File>();
    }
  };
  private static InheritableThreadLocal<HashSet<Closeable>>  openStreams= new InheritableThreadLocal<HashSet<Closeable>>() {
    @Override
    protected HashSet<Closeable> initialValue() {
      return new HashSet<Closeable>();
    }
  };

  public static LinkedList<File> getTempDirList() {
    return tempDirList.get();
  }
  public static HashSet<Closeable> getOpenStreams() {
    return openStreams.get();
  }

  
  
  public static void registerTempDirectory(File td) {
    getTempDirList().add(td);
  }

  public static FileInputStream openInputStream(File f) throws FileNotFoundException {
    FileInputStream is=new FileInputStream(f);
    getOpenStreams().add(is);
    return is;
  }
  public static FileOutputStream openOutputStream(File f) throws FileNotFoundException  {
    FileOutputStream os=new FileOutputStream(f);
    getOpenStreams().add(os);
    return os;
  }
  public static void registerStream(Closeable c) {
    getOpenStreams().add(c);
  }
  public static void closeStream(Closeable c) throws IOException {
    c.close();
    getOpenStreams().remove(c);
  }

  // ******************************************************************
  // shutdown :
  //
  // Close all open streams and delete temporary directories
  // ******************************************************************
  public static void shutdown(boolean removeTempDirList) {
	  // Close all open streams
	  for(Closeable c:getOpenStreams()) {
		  try { c.close(); }
		  catch (Exception e) {
			  System.err.print("shutdown: could not close an open stream");
		  }
	  }
	  getOpenStreams().clear();
	  
	  // Delete temporary directories
	  if (removeTempDirList) {
		  for(File tempDir:getTempDirList()) {
			  if (!FileUtilities.recursiveDelete(tempDir)) {
				  System.err.print("shutdown: could not delete temporary directory: "+tempDir.getPath());
			  }
		  }
	  }
	  getTempDirList().clear();
  }

  public static void shutdown() {
	  shutdown(true);
  }

}
