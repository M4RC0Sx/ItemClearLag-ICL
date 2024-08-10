package vt.icl.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class FabricPermissions implements PermissionHandler {

    @Override
    public boolean hasPermission(CommandSource source, String permission) {
        if (source.hasPermissionLevel(4)) {
            return true;
        }
        return Permissions.check(source, permission);
    }
}
