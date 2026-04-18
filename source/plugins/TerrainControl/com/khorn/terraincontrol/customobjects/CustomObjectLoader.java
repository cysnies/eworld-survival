package com.khorn.terraincontrol.customobjects;

import java.io.File;

public interface CustomObjectLoader {
   CustomObject loadFromFile(String var1, File var2);

   void onShutdown();
}
