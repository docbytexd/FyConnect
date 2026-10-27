package net.areofy.fyconnect;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class PlatformUtil {

    private static final boolean FOLIA;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {}
        FOLIA = folia;
    }

    private PlatformUtil() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runSync(Plugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static int runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin, t -> task.run(),
                delayTicks * 50L,
                periodTicks * 50L,
                java.util.concurrent.TimeUnit.MILLISECONDS
            );
            return -1;
        } else {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks).getTaskId();
        }
    }

    public static void cancel(int taskId) {
        if (!FOLIA && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public static void runLaterForPlayer(Plugin plugin, org.bukkit.entity.Player player, Runnable task, long delayTicks) {
        if (FOLIA) {
            player.getScheduler().runDelayed(plugin, t -> task.run(), null, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void cancelAll(Plugin plugin) {
        if (!FOLIA) {
            Bukkit.getScheduler().cancelTasks(plugin);
        } else {
            try {
                Bukkit.getAsyncScheduler().cancelTasks(plugin);
            } catch (Exception ignored) {}
        }
    }
}
