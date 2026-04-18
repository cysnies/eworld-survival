package com.onarandombox.MultiverseCore.utils.webpaste;

import java.net.URL;

public interface PasteService {
   String encodeData(String var1);

   URL getPostURL();

   String postData(String var1, URL var2) throws PasteFailedException;
}
