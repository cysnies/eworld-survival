package com.lithium3141.shellparser.states;

import com.lithium3141.shellparser.ParseException;
import java.util.List;

public class QuoteState extends State {
   char quote;

   public QuoteState(char quote) {
      super();
      this.quote = quote;
   }

   public List parse(String parsing, String accumulator, List parsed, State referrer) throws ParseException {
      if (parsing.length() == 0) {
         throw new ParseException("Mismatched quote character: " + this.quote);
      } else {
         char c = (char)parsing.getBytes()[0];
         if (c == '\\') {
            return (new EscapeState()).parse(parsing.substring(1), accumulator, parsed, this);
         } else {
            return c == this.quote ? (new StartState()).parse(parsing.substring(1), accumulator, parsed, this) : (new QuoteState(this.quote)).parse(parsing.substring(1), accumulator + c, parsed, this);
         }
      }
   }
}
