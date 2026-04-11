package vt.icl.permission;

import net.minecraft.commands.CommandSourceStack;

public interface PermissionHandler {
    boolean hasPermission(CommandSourceStack source, String permission);
}
