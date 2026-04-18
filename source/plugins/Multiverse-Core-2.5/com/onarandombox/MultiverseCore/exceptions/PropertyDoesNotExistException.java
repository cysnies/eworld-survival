package com.onarandombox.MultiverseCore.exceptions;

public class PropertyDoesNotExistException extends Exception {
   public PropertyDoesNotExistException(String name) {
      super(name);
   }

   public PropertyDoesNotExistException(String name, Throwable cause) {
      super(name, cause);
   }
}
