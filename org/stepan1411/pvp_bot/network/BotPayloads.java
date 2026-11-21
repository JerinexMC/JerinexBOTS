package org.stepan1411.pvp_bot.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9139;

public class BotPayloads {
    public static final class_2960 LIST_REQUEST_ID = class_2960.method_60655("pvp_bot_gui", "bot_list_request");
    public static final class_2960 LIST_RESPONSE_ID = class_2960.method_60655("pvp_bot_gui", "bot_list_response");
    public static final class_2960 ACTION_ID = class_2960.method_60655("pvp_bot_gui", "bot_action");

    public record BotListRequestPayload() implements class_8710 {
        public static final class_8710.class_9154<BotListRequestPayload> ID = new class_8710.class_9154<>(LIST_REQUEST_ID);
        public static final class_9139<class_2540, BotListRequestPayload> CODEC = class_9139.method_56431(new BotListRequestPayload());
        @Override public class_9154<? extends class_8710> method_56479() { return ID; }
    }

    public record BotListResponsePayload(List<String> botNames) implements class_8710 {
        public static final class_8710.class_9154<BotListResponsePayload> ID = new class_8710.class_9154<>(LIST_RESPONSE_ID);
        public static final class_9139<class_2540, BotListResponsePayload> CODEC = class_9139.method_56438(
            (p, buf) -> {
                buf.method_10804(p.botNames.size());
                for (String name : p.botNames) buf.method_10814(name);
            },
            buf -> {
                int size = buf.method_10816();
                List<String> list = new ArrayList<>();
                for (int i = 0; i < size; i++) list.add(buf.method_19772());
                return new BotListResponsePayload(list);
            }
        );
        @Override public class_9154<? extends class_8710> method_56479() { return ID; }
    }

    public record BotActionPayload(String botName, String action) implements class_8710 {
        public static final class_8710.class_9154<BotActionPayload> ID = new class_8710.class_9154<>(ACTION_ID);
        public static final class_9139<class_2540, BotActionPayload> CODEC = class_9139.method_56438(
            (p, buf) -> {
                buf.method_10814(p.botName);
                buf.method_10814(p.action);
            },
            buf -> new BotActionPayload(buf.method_19772(), buf.method_19772())
        );
        @Override public class_9154<? extends class_8710> method_56479() { return ID; }
    }
}
