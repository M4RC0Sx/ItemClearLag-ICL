package vt.icl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vt.icl.config.ConfigManager;
import vt.icl.config.Configuration;
import vt.icl.config.lang.IclTranslationManager;
import vt.icl.mixin.ItemEntityAccessor;
import vt.icl.permission.PermissionHandler;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static vt.icl.config.lang.IclTranslationManager.createDefaultTranslationFiles;

public class ICLCommon {
    public static final String MOD_ID = "icl";
    public static final String MOD_PREFIX = "[" + MOD_ID.toUpperCase() + "] ";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID.toUpperCase());
    public static final Path CONFIG_DIR = new File("./config/" + MOD_ID.substring(0, 1).toUpperCase() + MOD_ID.substring(1)).toPath();
    public static Configuration config = ConfigManager.getConfig();
    private static Timer TIMER = new Timer(MOD_ID.toUpperCase());
    public static Map<String, String> translations;
    private static Map<String, String> defaultTranslations;
    public static PermissionHandler permissionHandler;
    private static MinecraftServer server;

    public static void init() {
        LOGGER.info("Initializing " + MOD_ID.toUpperCase());
        createDefaultTranslationFiles();
        translations = IclTranslationManager.loadTranslation(config.NotificationLang);
        defaultTranslations = IclTranslationManager.loadTranslation("en_us");
    }

    public static void onServerStart(MinecraftServer server) {
        ICLCommon.server = server;
        if (config.Delay > 0) {
            doItemClean(server);
            if (config.doShowNotification) {
                setupNotificationTimers(server);
            }
            if (config.doNotificationCountdown) {
                setupCountdownTimer(server);
            }
        } else {
            LOGGER.info(MOD_ID.toUpperCase() + " disabled, delay is less than 0");
        }
        LOGGER.info(MOD_ID.toUpperCase() + " initialized");
    }

    public static void onServerStop() {
        TIMER.cancel();
        LOGGER.info(MOD_ID.toUpperCase() + " stopped");
    }

    public static void doItemClean(MinecraftServer server) {
        long delay = ICLCommon.config.Delay;
        if (delay < 0) {
            return;
        }
        ICLCommon.TIMER.schedule(new TimerTask() {
            @Override
            public void run() {

                if (ICLCommon.config.doShowNotification) {
                    setupNotificationTimers(server);
                }
                if (ICLCommon.config.doNotificationCountdown) {
                    setupCountdownTimer(server);
                }

                server.execute(() -> clearItems(server));

                ICLCommon.TIMER.purge();

                doItemClean(server);
            }
        }, delay * 1000);
    }

    public static void setupNotificationTimers(MinecraftServer server) {
        for (int i = 0; i < ICLCommon.config.NotificationTimes; i++) {
            int finalI = i;
            long delay = ICLCommon.config.Delay - ICLCommon.config.NotificationStart + ICLCommon.config.NotificationDelay * i;
            if (delay < 0 || delay > ICLCommon.config.Delay) {
                continue;
            }
            ICLCommon.TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    ICLCommon.LOGGER.info("{} seconds left", "Clearing items " + (ICLCommon.config.NotificationStart - ICLCommon.config.NotificationDelay * finalI));
                    for (var player : server.getPlayerManager().getPlayerList()) {
                        MutableText message = Text.literal(ICLCommon.MOD_PREFIX + IclTranslate("text.icl.notification", (ICLCommon.config.NotificationStart - ICLCommon.config.NotificationDelay * finalI)) + " ")
                                .formatted(Formatting.valueOf(ICLCommon.config.NotificationColor));
                        IclMessage(player, message);
                        try {
                            if (ICLCommon.config.doNotificationSound) {
                                IclPlaysound(player, false);
                            }
                        } catch (Exception e) {
                            player.sendMessage(Text.literal(e.getMessage()).formatted(Formatting.valueOf(ICLCommon.config.NotificationColor)));
                            ICLCommon.LOGGER.error("Failed to play sound: " + e.getMessage());
                        }
                    }
                }
            }, delay * 1000);
        }
    }

    private static void IclMessage(ServerPlayerEntity player, MutableText message) {
        if (permissionCheckforCancel(player.getCommandSource())) {
            message.append(Text.literal(IclTranslate("text.icl.cancel.button"))
                    .styled(style -> style.withClickEvent(IclCancelEvent()))
                    .formatted(Formatting.RED));
        }
        player.sendMessage(message);
    }

    public static void setupCountdownTimer(MinecraftServer server) {
        long countdownstart = ICLCommon.config.CountdownStart;
        if (countdownstart > ICLCommon.config.Delay) {
            countdownstart = ICLCommon.config.Delay;
        }
        long delay = ICLCommon.config.Delay - countdownstart;
        if (delay < 0 || delay > ICLCommon.config.Delay) {
            return;
        }
        if (countdownstart < 0) {
            return;
        }


        long finalCountdownstart = countdownstart;
        ICLCommon.TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < finalCountdownstart; i++) {
                    int finalI = i;
                    ICLCommon.TIMER.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ICLCommon.LOGGER.info("{} seconds left", "Clearing items " + (finalCountdownstart - finalI));
                            for (var player : server.getPlayerManager().getPlayerList()) {
                                MutableText message = Text.literal(ICLCommon.MOD_PREFIX + IclTranslate("text.icl.countdown", (finalCountdownstart - finalI)) + " ")
                                        .formatted(Formatting.valueOf(ICLCommon.config.NotificationColor));
                                IclMessage(player, message);
                            }
                        }
                    }, finalI * 1000L);
                }
            }
        }, (delay) * 1000);
    }

    public static void clearItems(MinecraftServer server) {
        ICLCommon.LOGGER.info("Clearing items");
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (ICLCommon.config.doShowNotification) {
                player.sendMessage(Text.literal(ICLCommon.MOD_PREFIX + IclTranslate("text.icl.clear")).formatted(Formatting.valueOf(ICLCommon.config.NotificationColor)));
                try {
                    if (ICLCommon.config.doLastNotificationSound) {
                        IclPlaysound(player, true);
                    }
                } catch (Exception e) {
                    player.sendMessage(Text.literal(e.getMessage()).formatted(Formatting.valueOf(ICLCommon.config.NotificationColor)));
                    ICLCommon.LOGGER.error("Failed to play sound: " + e.getMessage());
                }
            }
        }
        int count = 0;
        for (var world : server.getWorlds()) {
            for (var entity : world.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), Entity::isAlive)) {
                if (ICLCommon.config.preserveNoPickupItems) {
                    ItemEntityAccessor accessor = (ItemEntityAccessor) entity;
                    if (accessor.getPickupDelay() == Short.MAX_VALUE) {
                        continue;
                    }
                }
                if (ICLCommon.config.preserveNoDespawnItems) {
                    if (entity.getItemAge() == Short.MIN_VALUE) {
                        continue;
                    }
                }
                count += entity.getStack().getCount();
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (ICLCommon.config.doShowNotification) {
                player.sendMessage(Text.literal(ICLCommon.MOD_PREFIX + IclTranslate("text.icl.clear.finish", count)).formatted(Formatting.valueOf(ICLCommon.config.NotificationColor)));
            }
        }
        ICLCommon.LOGGER.info("Items cleared: {}", count);
    }

    public static void reloadIcl() {
        ICLCommon.TIMER.cancel();
        ICLCommon.TIMER = new Timer(ICLCommon.MOD_ID.toUpperCase());
        ICLCommon.config = ConfigManager.getConfig();
        if (ICLCommon.config.Delay > 0) {
            doItemClean(ICLCommon.server);
            if (ICLCommon.config.doShowNotification) {
                setupNotificationTimers(ICLCommon.server);
            }
            if (ICLCommon.config.doNotificationCountdown) {
                setupCountdownTimer(ICLCommon.server);
            }
        } else {
            ICLCommon.LOGGER.info(ICLCommon.MOD_ID.toUpperCase() + " disabled, delay is less than 0");
        }
    }

    public static void CancelIcl(int tempDelay) {
        ICLCommon.TIMER.cancel();
        ICLCommon.TIMER = new Timer(ICLCommon.MOD_ID.toUpperCase());
        if (tempDelay > 0) {
            ICLCommon.TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    ICLCommon.config = ConfigManager.getConfig();
                    if (ICLCommon.config.Delay > 0) {
                        doItemClean(ICLCommon.server);
                        if (ICLCommon.config.doShowNotification) {
                            setupNotificationTimers(ICLCommon.server);
                        }
                        if (ICLCommon.config.doNotificationCountdown) {
                            setupCountdownTimer(ICLCommon.server);
                        }
                    } else {
                        ICLCommon.LOGGER.info(ICLCommon.MOD_ID.toUpperCase() + " disabled, delay is less than 0");
                    }
                }
            }, tempDelay * 1000L);
        } else {
            ICLCommon.config = ConfigManager.getConfig();
            if (ICLCommon.config.Delay > 0) {
                doItemClean(ICLCommon.server);
                if (ICLCommon.config.doShowNotification) {
                    setupNotificationTimers(ICLCommon.server);
                }
                if (ICLCommon.config.doNotificationCountdown) {
                    setupCountdownTimer(ICLCommon.server);
                }
            } else {
                ICLCommon.LOGGER.info(ICLCommon.MOD_ID.toUpperCase() + " disabled, delay is less than 0");
            }
        }
    }

    public static ClickEvent IclCancelEvent() {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/icl cancel");
    }

    public static String IclTranslate(String key, Object... args) {
        String translation = null;
        if (ICLCommon.translations != null) {
            translation = ICLCommon.translations.get(key);
            ICLCommon.LOGGER.debug("Translation: {}", translation);
        }
        if (translation == null && ICLCommon.defaultTranslations != null) {
            translation = ICLCommon.defaultTranslations.get(key);
            ICLCommon.LOGGER.debug("Default Translation: {}", translation);
        }
        if (translation != null) {
            if (args != null && args.length > 0) {
                return String.format(translation, args);
            } else {
                return translation;
            }
        } else {
            return key;
        }
    }

    public static void IclPlaysound(ServerPlayerEntity player, boolean isLastSound) {
        Vec3d vec3d;
        double e = player.getX();
        double f = player.getY();
        double g = player.getZ();
        double h = e * e + f * f + g * g;
        double k = Math.sqrt(h);
        vec3d = new Vec3d(player.getX() + e / k * 2.0, player.getY() + f / k * 2.0, player.getZ() + g / k * 2.0);
        Identifier sound;
        if (isLastSound) {
            sound = Identifier.of(ICLCommon.config.LastNotificationSound);
        } else {
            sound = Identifier.of(ICLCommon.config.NotificationSound);
        }
        RegistryEntry<SoundEvent> registryEntry = RegistryEntry.of(SoundEvent.of(sound));
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(registryEntry,
                SoundCategory.PLAYERS, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1, 1, 1));
    }

    private static boolean permissionCheckforCancel(ServerCommandSource source) {
        if (ICLCommon.permissionHandler != null) {
            return ICLCommon.permissionHandler.hasPermission(source, ICLCommon.MOD_ID + "." + "cancel");
        } else {
            return !ICLCommon.config.RequireOpCancel || source.hasPermissionLevel(2);
        }
    }
}
