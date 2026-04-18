package com.lishid.orebfuscator.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

public interface IChunkCache {
   void clearCache();

   DataInputStream getInputStream(File var1, int var2, int var3);

   DataOutputStream getOutputStream(File var1, int var2, int var3);
}
