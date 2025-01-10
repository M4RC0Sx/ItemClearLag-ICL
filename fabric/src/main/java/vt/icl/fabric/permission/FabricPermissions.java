package vt.icl.fabric.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import vt.icl.forge.permission.PermissionHandler;

public class FabricPermissions implements PermissionHandler {

    @Override
    public boolean hasPermission(ServerCommandSource source, String permission) {
        if (source.hasPermissionLevel(4)) {
            return true;
        }
        return Permissions.check(source, permission);
    }
}
