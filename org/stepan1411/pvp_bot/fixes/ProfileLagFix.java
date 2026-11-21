package org.stepan1411.pvp_bot.fixes;

import net.minecraft.class_11560;
import net.minecraft.server.MinecraftServer;
import java.util.UUID;

public class ProfileLagFix {

    public static void preloadProfileCache(MinecraftServer server, String name) {
        try {
            var cache = server.method_73550().comp_4407();
            if (cache != null) {
                UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                cache.method_14508(new class_11560(offlineUuid, name));
            }
        } catch (Exception e) {
        }
    }
}
