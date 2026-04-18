package com.lithium3141.shellparser.states;

import com.lithium3141.shellparser.ParseException;
import java.util.List;

public class StartState extends State {
   public StartState() {
      super();
   }

   public List parse(String parsing, String accumulator, List parsed, State referrer) throws ParseException {
      if (parsing.length() == 0) {
         if (accumulator.length() > 0) {
            parsed.add(accumulator);
         }

         return parsed;
      } else {
         char c = (char)parsing.getBytes()[0];
         if (c == ' ') {
            if (accumulator.length() > 0) {
               parsed.add(accumulator);
            }

            return (new StartState()).parse(parsing.substring(1), "", parsed, this);
         } else if (c == '\\') {
            return (new EscapeState()).parse(parsing.substring(1), accumulator, parsed, this);
         } else {
            return c != '"' && c != '\'' ? (new StartState()).parse(parsing.substring(1), accumulator + c, parsed, this) : (new QuoteState(c)).parse(parsing.substring(1), accumulator, parsed, this);
         }
      }
   }
}
