package vt.icl.permission;

import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PermissionHandler {
    boolean hasPermission(CommandSource source, String permission);
}
