package com.onarandombox.MultiverseCore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class FileUtils {
   private static final int COPY_BLOCK_SIZE = 1024;

   protected FileUtils() {
      super();
      throw new UnsupportedOperationException();
   }

   public static boolean deleteFolder(File file) {
      if (!file.exists()) {
         return false;
      } else {
         boolean ret = true;
         if (file.isDirectory()) {
            for(File f : file.listFiles()) {
               ret = ret && deleteFolder(f);
            }
         }

         return ret && file.delete();
      }
   }

   public static boolean deleteFolderContents(File file) {
      if (!file.exists()) {
         return false;
      } else {
         boolean ret = true;
         if (file.isDirectory()) {
            for(File f : file.listFiles()) {
               ret = ret && deleteFolder(f);
            }
         }

         return ret;
      }
   }

   public static boolean copyFolder(File source, File target, Logger log) {
      InputStream in = null;
      OutputStream out = null;

      try {
         if (source.isDirectory()) {
            if (!target.exists()) {
               target.mkdir();
            }

            String[] children = source.list();

            for(String child : children) {
               copyFolder(new File(source, child), new File(target, child), log);
            }
         } else {
            in = new FileInputStream(source);
            out = new FileOutputStream(target);
            byte[] buf = new byte[1024];

            int len;
            while((len = in.read(buf)) > 0) {
               out.write(buf, 0, len);
            }
         }

         boolean e = true;
         return e;
      } catch (FileNotFoundException e) {
         log.warning("Exception while copying file: " + e.getMessage());
      } catch (IOException e) {
         log.warning("Exception while copying file: " + e.getMessage());
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException var24) {
            }
         }

         if (out != null) {
            try {
               out.close();
            } catch (IOException var23) {
            }
         }

      }

      return false;
   }
}
