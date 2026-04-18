package com.lithium3141.shellparser;

import com.lithium3141.shellparser.states.StartState;
import com.lithium3141.shellparser.states.State;
import java.util.ArrayList;
import java.util.List;

public class ShellParser {
   public ShellParser() {
      super();
   }

   public static List parseString(String string) throws ParseException {
      return (new StartState()).parse(string, "", new ArrayList(), (State)null);
   }

   public static List safeParseString(String string) {
      try {
         return parseString(string);
      } catch (ParseException var2) {
         return null;
      }
   }
}
