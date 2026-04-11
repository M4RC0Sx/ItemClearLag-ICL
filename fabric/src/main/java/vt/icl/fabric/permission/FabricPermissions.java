package vt.icl.fabric.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.minecraft.commands.CommandSourceStack;
import vt.icl.permission.PermissionHandler;

import static net.minecraft.server.permissions.Permissions.*;

public class FabricPermissions implements PermissionHandler {

    @Override
    public boolean hasPermission(CommandSourceStack source, String permission) {
        if (source.permissions().hasPermission(COMMANDS_GAMEMASTER)) {
            return true;
        }
        return Permissions.check(source, permission);
    }
}
