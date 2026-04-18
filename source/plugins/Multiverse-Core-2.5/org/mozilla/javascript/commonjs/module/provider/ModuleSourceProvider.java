package org.mozilla.javascript.commonjs.module.provider;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import org.mozilla.javascript.Scriptable;

public interface ModuleSourceProvider {
   ModuleSource NOT_MODIFIED = new ModuleSource((Reader)null, (Object)null, (URI)null, (URI)null, (Object)null);

   ModuleSource loadSource(String var1, Scriptable var2, Object var3) throws IOException, URISyntaxException;

   ModuleSource loadSource(URI var1, URI var2, Object var3) throws IOException, URISyntaxException;
}
