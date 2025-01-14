package vt.icl.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import vt.icl.ICLCommon;
import vt.icl.commands.IclCommand;
import vt.icl.forge.permission.ForgePermissions;

import static vt.icl.ICLCommon.config;


@Mod(ICLCommon.MOD_ID)
public class ICL {

    public ICL() {
        ICLCommon.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPermissionNodesRegister(PermissionGatherEvent.Nodes event) {
        ForgePermissions.init();
        event.addNodes(ForgePermissions.permissionNodesList);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (config.UsePermissionsApi) {
            try {
                Class.forName("net.minecraftforge.server.permission.PermissionAPI");
                ICLCommon.permissionHandler = new ForgePermissions();
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
