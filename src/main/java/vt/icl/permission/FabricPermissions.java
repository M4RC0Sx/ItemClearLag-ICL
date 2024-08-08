package vt.icl.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;

public class FabricPermissions implements PermissionHandler {

    @Override
    public boolean hasPermission(ServerPlayerEntity player, String permission) {
        return Permissions.check(player, permission);
    }
}
