package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.api.FancyText;

public class FancyHeader implements FancyText {
   private FancyColorScheme colors;
   private StringBuilder text;

   public FancyHeader(String text, FancyColorScheme scheme) {
      super();
      this.colors = scheme;
      this.text = new StringBuilder(text);
   }

   public String getFancyText() {
      return String.format("%s--- %s%s ---", this.colors.getHeader(), this.text.toString(), this.colors.getHeader());
   }

   public void appendText(String string) {
      this.text.append(string);
   }
}
