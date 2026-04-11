package vt.icl.neoforge.permission;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import vt.icl.ICLCommon;
import vt.icl.permission.PermissionHandler;

import java.util.List;

public class NeoForgePermissions implements PermissionHandler {

    public static final PermissionNode<Boolean> FORCECLEAN = new PermissionNode<>(
            Identifier.fromNamespaceAndPath(ICLCommon.MOD_ID, "forceclean"),
            PermissionTypes.BOOLEAN,
            (player, playerUUID, context) -> false
    );
    public static final PermissionNode<Boolean> RELOAD = new PermissionNode<>(
            Identifier.fromNamespaceAndPath(ICLCommon.MOD_ID, "reload"),
            PermissionTypes.BOOLEAN,
            (player, playerUUID, context) -> false
    );
    public static final PermissionNode<Boolean> CONFIG = new PermissionNode<>(
            Identifier.fromNamespaceAndPath(ICLCommon.MOD_ID, "config"),
            PermissionTypes.BOOLEAN,
            (player, playerUUID, context) -> false
    );
    public static final PermissionNode<Boolean> CANCEL = new PermissionNode<>(
            Identifier.fromNamespaceAndPath(ICLCommon.MOD_ID, "cancel"),
            PermissionTypes.BOOLEAN,
            (player, playerUUID, context) -> false
    );

    public static List<PermissionNode<?>> permissionNodesList = List.of(
           FORCECLEAN, RELOAD, CONFIG, CANCEL
    );

    @Override
    public boolean hasPermission(CommandSourceStack source, String permission) {
        PermissionNode<Boolean> permissionNode = (PermissionNode<Boolean>) PermissionAPI.getRegisteredNodes().stream()
                .filter(p -> p.getNodeName().equals(permission))
                .findFirst()
                .orElse(null);
        if (source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return true;
        }
        if (source.getPlayer() == null) {
            return false;
        }
        if (permissionNode == null) {
            ICLCommon.LOGGER.warn("Permission node {} not found", permission);
            return false;
        }
        return PermissionAPI.getPermission(source.getPlayer(), permissionNode);
    }

    public static void init() {
        FORCECLEAN.setInformation(Component.literal("Force Clean"), Component.literal("Allows the player to force clean the world"));
        RELOAD.setInformation(Component.literal("Reload"), Component.literal("Allows the player to reload the config"));
        CONFIG.setInformation(Component.literal("Config"), Component.literal("Allows the player to view the config"));
        CANCEL.setInformation(Component.literal("Cancel"), Component.literal("Allows the player to cancel the current operation"));
    }


}
