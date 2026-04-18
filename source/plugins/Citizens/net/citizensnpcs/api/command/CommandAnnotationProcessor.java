package net.citizensnpcs.api.command;

import java.lang.annotation.Annotation;
import net.citizensnpcs.api.command.exception.CommandException;
import org.bukkit.command.CommandSender;

public interface CommandAnnotationProcessor {
   Class getAnnotationClass();

   void process(CommandSender var1, CommandContext var2, Annotation var3, Object[] var4) throws CommandException;
}
