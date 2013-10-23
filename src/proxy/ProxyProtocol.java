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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import common.ResourceManager;

public class ProxyProtocol {

  // Write an integer to the output stream
  public static void writeInt(OutputStream os, int value) throws IOException {
    for(int i=0;i<4;i++,value>>=8) {
      os.write((byte)value);
    }
  }
  
  // Write an integer to the output stream
  public static void writeLong(OutputStream os, long value) throws IOException {
    for(int i=0;i<8;i++,value>>=8) {
      os.write((byte)value);
    }
  }

  // Read an integer from the input stream
  public static int readInt(InputStream is)
  throws IOException, ProtocolException, EndOfFileException {
    int value=0;
    int shift=0;
    int r;
    for(int i=0;i<4;i++,shift+=8) {
      if ((r=is.read())<0) {
        if (i==0) throw new EndOfFileException();
        else throw new ProtocolException("readInt: not enough bytes on socket");
      }
      value|=r<<shift;
    }

    return value;
  }

  // Read an integer from the input stream
  public static long readLong(InputStream is)
  throws IOException, ProtocolException, EndOfFileException {
    long value=0;
    int shift=0;
    long r;
    for(int i=0;i<8;i++,shift+=8) {
      if ((r=is.read())<0) {
        if (i==0) throw new EndOfFileException();
        else throw new ProtocolException("readLong: not enough bytes on socket ("+i+" read)");
      }
      value|=r<<shift;
    }

    return value;
  }

  // Read an string of 'nb_bytes' from the input stream
  public static String readString(InputStream is, int nb_bytes)
  throws IOException, ProtocolException, EndOfFileException {
    byte[] byteArray = new byte[nb_bytes];

    for(int i=0;i<nb_bytes;i++) {
      int value;
      if ((value=is.read())<0) {
        if (i==0) throw new EndOfFileException();
        else throw new ProtocolException("readString: not enough bytes on socket");
      }
      byteArray[i]=(byte)value;
    }
    return new String(byteArray);
  }

  // Copy nb_bytes from 'is' to file 'dest'
  public static void streamToFile(InputStream is, int nb_bytes, File dest)
  throws EndOfFileException, ProtocolException, IOException {
    FileOutputStream fd=ResourceManager.openOutputStream(dest);
    try {
      int val;
      for(int i=0;i<nb_bytes;i++) {
        if ((val=is.read())<0) {
          if (i==0) throw new EndOfFileException();
          else throw new ProtocolException();
        }
        else {
          fd.write((byte)val);
        }
      }
    }
    finally {
      ResourceManager.closeStream(fd);
    }
  }

  // Send file 'source' to stream os
  public static void fileToStream(OutputStream os, File source) throws IOException {
    FileInputStream fi=ResourceManager.openInputStream(source);
    try {
      int val;
      while ((val=fi.read())>=0) {
        os.write((byte)val);
      }
    }
    finally {
      ResourceManager.closeStream(fi);
    }
  }
  
}
