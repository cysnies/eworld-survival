package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.api.FancyText;

public class FancyMessage implements FancyText {
   private String title;
   private String message;
   private boolean main = true;
   private FancyColorScheme colors;

   public FancyMessage(String title, String message, FancyColorScheme scheme) {
      super();
      this.title = title;
      this.message = message;
      this.colors = scheme;
   }

   public void setColorMain() {
      this.main = true;
   }

   public void setColorAlt() {
      this.main = false;
   }

   public String getFancyText() {
      return this.colors.getMain(this.main) + this.title + this.colors.getDefault() + this.message;
   }

   public void setAltColor(boolean altColor) {
      this.main = !altColor;
   }

   public void setMainColor(boolean mainColor) {
      this.main = mainColor;
   }
}
