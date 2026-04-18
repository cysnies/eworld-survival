package net.citizensnpcs.api.command.exception;

public class NoPermissionsException extends CommandException {
   private static final long serialVersionUID = -602374621030168291L;

   public NoPermissionsException() {
      super("citizens.commands.requirements.missing-permission");
   }
}
