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

package proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

public class BufferedOutStreams {
  String target=null;
  String replacement=null;
  LinkedList<byte[]> outByteList = new LinkedList<byte[]>();
  LinkedList<byte[]> errByteList = new LinkedList<byte[]>();
  
  public BufferedOutStreams(String t, String r) {
	  target=t;
	  replacement=r;
  }
  
  // Add a string to the output buffer
  public void addLineToOut(String s) {
	  outByteList.add((s.replace(target,replacement)+'\n').getBytes());
  }
  
  // Add a string to the err buffer
  public void addLineToErr(String s) {
	  errByteList.add((s.replace(target,replacement)+'\n').getBytes());
  }
  
  // Returns the size in bytes if the buffered output stream
  public int getOutSize() {
	  int size=0;
	  for(byte[] byteArray:outByteList) {
		  size+=byteArray.length;
	  }
	  return size;
  }

  // Returns the size in bytes if the buffered output stream
  public int getErrSize() {
	  int size=0;
	  for(byte[] byteArray:errByteList) {
		  size+=byteArray.length;
	  }
	  return size;
  }

  // Send the buffered output stream to 'out'
  public void sendOutToStream(OutputStream out) throws IOException {
    for(byte[] byteArray:outByteList) {
      out.write(byteArray);
    }
  }

  // Send the buffered error stream to 'err'
  public void sendErrToStream(OutputStream out) throws IOException {
    for(byte[] byteArray:errByteList) {
      out.write(byteArray);
    }
  }
  
}

