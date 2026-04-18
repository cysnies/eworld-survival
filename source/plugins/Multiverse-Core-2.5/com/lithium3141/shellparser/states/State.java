package com.lithium3141.shellparser.states;

import com.lithium3141.shellparser.ParseException;
import java.util.List;

public abstract class State {
   public State() {
      super();
   }

   public abstract List parse(String var1, String var2, List var3, State var4) throws ParseException;
}
