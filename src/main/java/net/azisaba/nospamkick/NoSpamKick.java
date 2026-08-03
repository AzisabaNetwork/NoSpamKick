package net.azisaba.nospamkick;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.TickThrottler;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public final class NoSpamKick extends JavaPlugin {

    private static final Field chatSpamThrottlerField;
    private static final Field countField;

    public NoSpamKick() {
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("nospamkick.no-kick")) {
                    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
                    if (connection != null) {
                        try {
                            TickThrottler throttler = (TickThrottler) chatSpamThrottlerField.get(connection);
                            if (throttler != null) {
                                AtomicInteger count = (AtomicInteger) countField.get(throttler);
                                if (count != null) {
                                    count.set(0);
                                }
                            }
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                }
            }
        }, 1L, 1L);
    }

    static {
        try {
            chatSpamThrottlerField = ServerGamePacketListenerImpl.class.getDeclaredField("chatSpamThrottler");
            chatSpamThrottlerField.setAccessible(true);

            countField = TickThrottler.class.getDeclaredField("count");
            countField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get chatSpamThrottler or count field", e);
        }
    }
}

