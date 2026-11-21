package org.stepan1411.pvp_bot.network;

import java.util.*;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9139;

public class SettingsPayloads {
    public static final class_2960 REQUEST_ID = class_2960.method_60655("pvp_bot_gui", "settings_request");
    public static final class_2960 RESPONSE_ID = class_2960.method_60655("pvp_bot_gui", "settings_response");
    public static final class_2960 UPDATE_ID = class_2960.method_60655("pvp_bot_gui", "settings_update");

    public record SettingsRequestPayload() implements class_8710 {
        public static final class_8710.class_9154<SettingsRequestPayload> ID = new class_8710.class_9154<>(REQUEST_ID);
        public static final class_9139<class_2540, SettingsRequestPayload> CODEC = class_9139.method_56431(new SettingsRequestPayload());

        @Override
        public class_9154<? extends class_8710> method_56479() {
            return ID;
        }
    }

    public record SettingsResponsePayload(Map<String, String> settings) implements class_8710 {
        public static final class_8710.class_9154<SettingsResponsePayload> ID = new class_8710.class_9154<>(RESPONSE_ID);

        public static final class_9139<class_2540, SettingsResponsePayload> CODEC = class_9139.method_56438(
            (value, buf) -> {
                buf.method_10804(value.settings.size());
                for (Map.Entry<String, String> e : value.settings.entrySet()) {
                    buf.method_10814(e.getKey());
                    buf.method_10814(e.getValue());
                }
            },
            buf -> {
                int size = buf.method_10816();
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    map.put(buf.method_19772(), buf.method_19772());
                }
                return new SettingsResponsePayload(Collections.unmodifiableMap(map));
            }
        );

        @Override
        public class_9154<? extends class_8710> method_56479() {
            return ID;
        }
    }

    public record SettingsUpdatePayload(String key, String value) implements class_8710 {
        public static final class_8710.class_9154<SettingsUpdatePayload> ID = new class_8710.class_9154<>(UPDATE_ID);

        public static final class_9139<class_2540, SettingsUpdatePayload> CODEC = class_9139.method_56438(
            (p, buf) -> {
                buf.method_10814(p.key);
                buf.method_10814(p.value);
            },
            buf -> new SettingsUpdatePayload(buf.method_19772(), buf.method_19772())
        );

        @Override
        public class_9154<? extends class_8710> method_56479() {
            return ID;
        }
    }
}
