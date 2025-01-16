package vt.icl.neoforge;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import vt.icl.ICLCommon;
import vt.icl.commands.IclCommand;
import vt.icl.neoforge.permission.NeoForgePermissions;

import static vt.icl.ICLCommon.config;


@Mod(ICLCommon.MOD_ID)
public class ICL {

    public ICL(IEventBus eventBus) {
        ICLCommon.init();
        eventBus.register(this);
    }

    @SubscribeEvent
    public void onPermissionNodesRegister(PermissionGatherEvent.Nodes event) {
        NeoForgePermissions.init();
        event.addNodes(NeoForgePermissions.permissionNodesList);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (config.UsePermissionsApi) {
            try {
                Class.forName("net.neoforged.neoforge.server.permission.PermissionAPI");
                ICLCommon.permissionHandler = new NeoForgePermissions();
            } catch (ClassNotFoundException e) {
                ICLCommon.LOGGER.error("PermissionAPI not found, falling back to default permission system");
            }
        } else {
            ICLCommon.LOGGER.info("Using default permission system");
            ICLCommon.permissionHandler = null;
        }
        ICLCommon.onServerStart(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        ICLCommon.onServerStop();
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        IclCommand.register(event.getDispatcher());
    }

}
