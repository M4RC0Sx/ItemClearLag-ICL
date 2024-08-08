package vt.icl.permission;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PermissionHandler {
    boolean hasPermission(ServerPlayerEntity player, String permission);
}
