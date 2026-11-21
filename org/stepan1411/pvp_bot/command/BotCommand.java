package org.stepan1411.pvp_bot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import org.stepan1411.pvp_bot.bot.BotCombat;
import org.stepan1411.pvp_bot.bot.BotFaction;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotNameGenerator;
import org.stepan1411.pvp_bot.bot.BotPath;
import org.stepan1411.pvp_bot.bot.BotSettings;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BotCommand {

    private static final SuggestionProvider<class_2168> BOT_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String bot : BotManager.getAllBots()) {
                if (bot.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(bot);
                }
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<class_2168> KIT_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String kit : BotKits.getKitNames()) {
                if (kit.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(kit);
                }
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<class_2168> PLAYER_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (var player : ctx.getSource().method_9211().method_3760().method_14571()) {
                String name = player.method_5477().getString();
                if (name.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<class_2168> PATH_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String pathName : BotPath.getAllPaths().keySet()) {
                if (pathName.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(pathName);
                }
            }
            return builder.buildFuture();
        };

    private static LiteralArgumentBuilder<class_2168> cmd(String literal, String description) {
        return class_2170.method_9247(literal)
            .executes(ctx -> {
                ctx.getSource().method_9226(() -> class_2561.method_43470("§7[Tip] §a" + description), false);
                return 1;
            });
    }

    public static void register(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(cmd("pvpbot", "PvP Bot control")
            .requires(s -> true)

            // ========== SPAWN ==========
            .then(class_2170.method_9247("spawn")
                .executes(ctx -> spawn(ctx, null))
                .then(class_2170.method_9244("name", StringArgumentType.word())
                    .executes(ctx -> spawn(ctx, StringArgumentType.getString(ctx, "name")))))

            // ========== REMOVE ==========
            .then(cmd("remove", "Remove a bot")
                .then(class_2170.method_9244("name", StringArgumentType.word())
                    .suggests(BOT_SUGGESTIONS)
                    .executes(BotCommand::remove)))

            // ========== REMOVEALL ==========
            .then(class_2170.method_9247("reload")
                .executes(BotCommand::reload))
            .then(class_2170.method_9247("removeall")
                .executes(BotCommand::removeAll))

            // ========== SETTINGS ==========
            .then(buildSettings())

            // ========== BOT-MANAGEMENT ==========
            .then(cmd("bot-management", "Bot management")
                .then(cmd("mass-spawn", "Mass spawn bots")
                    .then(class_2170.method_9244("count", IntegerArgumentType.integer(1, 10000))
                        .executes(BotCommand::massSpawn)))
                .then(cmd("attack", "Attack a target")
                    .then(class_2170.method_9244("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .then(class_2170.method_9244("target", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::botAttack))))
                .then(cmd("stop-attack", "Stop attack")
                    .then(class_2170.method_9244("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(BotCommand::botStopAttack)))
                .then(cmd("inventory", "Show bot inventory")
                    .then(class_2170.method_9244("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(BotCommand::botInventory)))
                .then(class_2170.method_9247("list")
                    .executes(BotCommand::botList))
                .then(cmd("path", "Path management")
                    .then(cmd("create", "Create a path")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .executes(BotCommand::pathCreate)))
                    .then(cmd("delete", "Delete a path")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathDelete)))
                    .then(cmd("add-point", "Add point to path")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathAddPoint)))
                    .then(cmd("remove-point", "Remove point from path")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(class_2170.method_9244("index", IntegerArgumentType.integer())
                                .executes(BotCommand::pathRemovePoint))
                            .executes(BotCommand::pathRemovePointLast)))
                    .then(cmd("clear", "Clear all path points")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathClear)))
                    .then(cmd("loop", "Toggle path looping")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(class_2170.method_9244("value", BoolArgumentType.bool())
                                .executes(BotCommand::pathLoop))))
                    .then(cmd("start", "Start bot on path")
                        .then(class_2170.method_9244("bot", StringArgumentType.word())
                            .suggests(BOT_SUGGESTIONS)
                            .then(class_2170.method_9244("path", StringArgumentType.word())
                                .suggests(PATH_SUGGESTIONS)
                                .executes(BotCommand::pathStart))))
                    .then(cmd("stop", "Stop bot on path")
                        .then(class_2170.method_9244("bot", StringArgumentType.word())
                            .suggests(BOT_SUGGESTIONS)
                            .executes(BotCommand::pathStop)))
                    .then(class_2170.method_9247("list")
                        .executes(BotCommand::pathList))
                    .then(cmd("show", "Show/hide path particles")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(class_2170.method_9244("visible", BoolArgumentType.bool())
                                .executes(BotCommand::pathShow))))
                    .then(cmd("info", "Path info")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathInfo)))
                    .then(cmd("distribute", "Distribute bots along path")
                        .then(class_2170.method_9244("path", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathDistribute)))
                    .then(cmd("start-near", "Start bots near path")
                        .then(class_2170.method_9244("path", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(class_2170.method_9244("radius", DoubleArgumentType.doubleArg(1))
                                .executes(BotCommand::pathStartNear))))
                    .then(cmd("stop-all", "Stop all bots on path")
                        .then(class_2170.method_9244("path", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathStopAll)))
                    .then(cmd("walk-type", "Path walk type")
                        .then(class_2170.method_9244("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(class_2170.method_9244("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("bhop");
                                    builder.suggest("sprint");
                                    builder.suggest("walk");
                                    return builder.buildFuture();
                                })
                                .executes(BotCommand::pathWalkType))))))

            // ========== KIT ==========
            .then(cmd("kit", "Kit management")
                .then(cmd("create-kit", "Create kit from inventory")
                    .then(class_2170.method_9244("name", StringArgumentType.word())
                        .executes(BotCommand::kitCreate)))
                .then(cmd("delete-kit", "Delete a kit")
                    .then(class_2170.method_9244("name", StringArgumentType.word())
                        .suggests(KIT_SUGGESTIONS)
                        .executes(BotCommand::kitDelete)))
                .then(cmd("give-kit", "Give kit to player")
                    .then(class_2170.method_9244("playername", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTIONS)
                        .then(class_2170.method_9244("kitname", StringArgumentType.word())
                            .suggests(KIT_SUGGESTIONS)
                            .executes(BotCommand::kitGive))))
                .then(cmd("give-kit-near", "Give kit to bots within radius")
                    .then(class_2170.method_9244("kitname", StringArgumentType.word())
                        .suggests(KIT_SUGGESTIONS)
                        .executes(ctx -> kitGiveNear(ctx, 10.0))
                        .then(class_2170.method_9244("radius", DoubleArgumentType.doubleArg(1, 1000))
                            .executes(ctx -> kitGiveNear(ctx, DoubleArgumentType.getDouble(ctx, "radius"))))))
                .then(cmd("give-kit-near-random", "Give random kit to bots within radius")
                    .then(class_2170.method_9244("radius", DoubleArgumentType.doubleArg(1, 1000))
                        .then(class_2170.method_9244("kits", StringArgumentType.greedyString())
                            .suggests(RANDOM_KIT_SUGGESTIONS)
                            .executes(BotCommand::kitGiveNearRandom))))
                .then(cmd("kits", "List all kits")
                    .executes(BotCommand::kitList)))

            // ========== FACTION ==========
            .then(cmd("faction", "Faction management")
                .then(cmd("list", "List all factions")
                    .executes(BotCommand::factionList))
                .then(cmd("create", "Create a faction")
                    .then(class_2170.method_9244("name", StringArgumentType.word())
                        .executes(BotCommand::factionCreate)))
                .then(cmd("delete", "Delete a faction")
                    .then(class_2170.method_9244("name", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .executes(BotCommand::factionDelete)))
                .then(cmd("add", "Add player to faction")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(class_2170.method_9244("player", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionAdd))))
                .then(cmd("remove", "Remove player from faction")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(class_2170.method_9244("player", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionRemove))))
                .then(cmd("hostile", "Set faction hostility")
                    .then(class_2170.method_9244("faction1", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(class_2170.method_9244("faction2", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(class_2170.method_9244("hostile", BoolArgumentType.bool())
                                .executes(ctx -> factionHostile(ctx, BoolArgumentType.getBool(ctx, "hostile"))))
                            .executes(ctx -> factionHostile(ctx, true)))))
                .then(cmd("info", "Faction info")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .executes(BotCommand::factionInfo)))
                .then(cmd("add-near", "Add nearby players to faction")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(class_2170.method_9244("radius", DoubleArgumentType.doubleArg(1, 10000))
                            .executes(BotCommand::factionAddNear))))
                .then(cmd("add-all", "Add all players to faction")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .executes(BotCommand::factionAddAll)))
                .then(cmd("give", "Give item to faction")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(class_2170.method_9244("item", StringArgumentType.greedyString())
                            .executes(BotCommand::factionGive))))
                .then(cmd("attack", "Faction attack target")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(class_2170.method_9244("target", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionAttack))))
                .then(cmd("path", "Faction path control")
                    .then(cmd("start", "Start faction on path")
                        .then(class_2170.method_9244("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(class_2170.method_9244("path", StringArgumentType.word())
                                .suggests(PATH_SUGGESTIONS)
                                .executes(BotCommand::factionStartPath))))
                    .then(cmd("stop", "Stop faction on path")
                        .then(class_2170.method_9244("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .executes(BotCommand::factionStopPath))))
                .then(cmd("tp", "Teleport faction bots to location")
                    .then(class_2170.method_9244("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(class_2170.method_9244("location", StringArgumentType.greedyString())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionTp))))
                .then(cmd("kit", "Faction kit management")
                    .then(cmd("give-kit", "Give kit to faction")
                        .then(class_2170.method_9244("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(class_2170.method_9244("kitname", StringArgumentType.word())
                                .suggests(KIT_SUGGESTIONS)
                                .executes(BotCommand::factionGiveKit))))
                    .then(cmd("give-kit-random", "Give random kit to faction by percentage")
                        .then(class_2170.method_9244("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(class_2170.method_9244("kits", StringArgumentType.greedyString())
                                .suggests(RANDOM_KIT_SUGGESTIONS)
                                .executes(BotCommand::factionGiveKitRandom))))))

        );
    }

    private static final SuggestionProvider<class_2168> RANDOM_KIT_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining();
            int tokenCount;
            String partial;
            if (remaining.isEmpty()) {
                tokenCount = 0;
                partial = "";
            } else {
                int lastSpace = remaining.lastIndexOf(' ');
                if (lastSpace >= 0) {
                    partial = remaining.substring(lastSpace + 1);
                    tokenCount = remaining.substring(0, lastSpace).split(" ", -1).length;
                    if (tokenCount == 1 && remaining.substring(0, lastSpace).isEmpty()) tokenCount = 0;
                } else {
                    partial = remaining;
                    tokenCount = 0;
                }
            }
            var wordBuilder = builder.createOffset(builder.getStart() + remaining.length() - partial.length());
            if (tokenCount % 2 == 1) {
                for (int i = 1; i <= 100; i++) {
                    wordBuilder.suggest(i + "%");
                }
            } else {
                for (String kit : BotKits.getKitNames()) {
                    if (kit.regionMatches(true, 0, partial, 0, partial.length())) {
                        wordBuilder.suggest(kit);
                    }
                }
            }
            return wordBuilder.buildFuture();
        };

    // ========== SETTINGS BUILDER ==========

    private static LiteralArgumentBuilder<class_2168> buildSettings() {
        var settings = class_2170.method_9247("settings")
            .executes(BotCommand::settings);

        settings.then(boolSetting("shield-break", "Auto-axe enemy shield", () -> BotSettings.get().isShieldBreakEnabled(), v -> BotSettings.get().setShieldBreakEnabled(v), BotSettings.DEFAULTS::isShieldBreakEnabled));
        settings.then(intSetting("shield-break-chance", "Shield break attempt chance (%)", () -> BotSettings.get().getShieldBreakChance(), v -> BotSettings.get().setShieldBreakChance(v), 0, 100, BotSettings.DEFAULTS::getShieldBreakChance));
        settings.then(boolSetting("stun", "Follow up axe shield break with stun hit after 2 ticks", () -> BotSettings.get().isStunEnabled(), v -> BotSettings.get().setStunEnabled(v), BotSettings.DEFAULTS::isStunEnabled));
        settings.then(intSetting("stun-chance", "Chance to perform stun hit after axe shield hit (1-100%)", () -> BotSettings.get().getStunChance(), v -> BotSettings.get().setStunChance(v), 1, 100, BotSettings.DEFAULTS::getStunChance));
        settings.then(boolSetting("auto-armor", "Auto-equip armor", () -> BotSettings.get().isAutoEquipArmor(), v -> BotSettings.get().setAutoEquipArmor(v), BotSettings.DEFAULTS::isAutoEquipArmor));
        settings.then(boolSetting("auto-weapon", "Auto-equip best weapon", () -> BotSettings.get().isAutoEquipWeapon(), v -> BotSettings.get().setAutoEquipWeapon(v), BotSettings.DEFAULTS::isAutoEquipWeapon));
        settings.then(boolSetting("drop-armor", "Drop worse armor on pickup", () -> BotSettings.get().isDropWorseArmor(), v -> BotSettings.get().setDropWorseArmor(v), BotSettings.DEFAULTS::isDropWorseArmor));
        settings.then(boolSetting("drop-weapon", "Drop worse weapons on pickup", () -> BotSettings.get().isDropWorseWeapons(), v -> BotSettings.get().setDropWorseWeapons(v), BotSettings.DEFAULTS::isDropWorseWeapons));
        settings.then(doubleSetting("drop-distance", "Drop check distance", () -> BotSettings.get().getDropDistance(), v -> BotSettings.get().setDropDistance(v), 1.0, 10.0, BotSettings.DEFAULTS::getDropDistance));
        settings.then(intSetting("interval", "Equipment check interval (ticks)", () -> BotSettings.get().getCheckInterval(), v -> BotSettings.get().setCheckInterval(v), 1, 100, BotSettings.DEFAULTS::getCheckInterval));
        settings.then(boolSetting("combat", "Enable combat AI", () -> BotSettings.get().isCombatEnabled(), v -> BotSettings.get().setCombatEnabled(v), BotSettings.DEFAULTS::isCombatEnabled));
        settings.then(boolSetting("revenge", "Auto-attack last damager", () -> BotSettings.get().isRevengeEnabled(), v -> BotSettings.get().setRevengeEnabled(v), BotSettings.DEFAULTS::isRevengeEnabled));
        settings.then(boolSetting("auto-target", "Auto-target nearest player", () -> BotSettings.get().isAutoTargetEnabled(), v -> BotSettings.get().setAutoTargetEnabled(v), BotSettings.DEFAULTS::isAutoTargetEnabled));
        settings.then(boolSetting("target-players", "Target real players", () -> BotSettings.get().isTargetPlayers(), v -> BotSettings.get().setTargetPlayers(v), BotSettings.DEFAULTS::isTargetPlayers));
        settings.then(boolSetting("target-mobs", "Target hostile mobs", () -> BotSettings.get().isTargetHostileMobs(), v -> BotSettings.get().setTargetHostileMobs(v), BotSettings.DEFAULTS::isTargetHostileMobs));
        settings.then(boolSetting("target-bots", "Target other bots", () -> BotSettings.get().isTargetOtherBots(), v -> BotSettings.get().setTargetOtherBots(v), BotSettings.DEFAULTS::isTargetOtherBots));
        settings.then(boolSetting("criticals", "Jump-crit on melee attacks", () -> BotSettings.get().isCriticalsEnabled(), v -> BotSettings.get().setCriticalsEnabled(v), BotSettings.DEFAULTS::isCriticalsEnabled));
        settings.then(intSetting("crit-fall-ticks", "Falling ticks before crit attack", () -> BotSettings.get().getCriticalFallTicks(), v -> BotSettings.get().setCriticalFallTicks(v), 1, 10, BotSettings.DEFAULTS::getCriticalFallTicks));
        settings.then(boolSetting("ranged", "Use bows/crossbows", () -> BotSettings.get().isRangedEnabled(), v -> BotSettings.get().setRangedEnabled(v), BotSettings.DEFAULTS::isRangedEnabled));
        settings.then(doubleSetting("ranged-min-range", "Min bow distance", () -> BotSettings.get().getRangedMinRange(), v -> BotSettings.get().setRangedMinRange(v), 3.0, 20.0, BotSettings.DEFAULTS::getRangedMinRange));
        settings.then(doubleSetting("ranged-optimal-range", "Ideal bow distance", () -> BotSettings.get().getRangedOptimalRange(), v -> BotSettings.get().setRangedOptimalRange(v), 10.0, 50.0, BotSettings.DEFAULTS::getRangedOptimalRange));
        settings.then(doubleSetting("ranged-max-range", "Max bow engagement range", () -> BotSettings.get().getRangedMaxRange(), v -> BotSettings.get().setRangedMaxRange(v), 15.0, 100.0, BotSettings.DEFAULTS::getRangedMaxRange));
        settings.then(intSetting("bow-draw-ticks", "Bow full draw time (ticks)", () -> BotSettings.get().getBowMinDrawTime(), v -> BotSettings.get().setBowMinDrawTime(v), 5, 100, BotSettings.DEFAULTS::getBowMinDrawTime));
        settings.then(boolSetting("arrow-prediction", "Predictive bow aiming", () -> BotSettings.get().isArrowPredictionEnabled(), v -> BotSettings.get().setArrowPredictionEnabled(v), BotSettings.DEFAULTS::isArrowPredictionEnabled));
        settings.then(boolSetting("ranged-strafe", "Strafe while shooting bow", () -> BotSettings.get().isRangedStrafeEnabled(), v -> BotSettings.get().setRangedStrafeEnabled(v), BotSettings.DEFAULTS::isRangedStrafeEnabled));
        settings.then(boolSetting("ranged-retreat", "Retreat with bow when target close", () -> BotSettings.get().isRangedRetreatOnClose(), v -> BotSettings.get().setRangedRetreatOnClose(v), BotSettings.DEFAULTS::isRangedRetreatOnClose));
        settings.then(boolSetting("mace", "Use mace smash attack", () -> BotSettings.get().isMaceEnabled(), v -> BotSettings.get().setMaceEnabled(v), BotSettings.DEFAULTS::isMaceEnabled));
        settings.then(boolSetting("special-names", "Use special bot names", () -> BotSettings.get().isUseSpecialNames(), v -> BotSettings.get().setUseSpecialNames(v), BotSettings.DEFAULTS::isUseSpecialNames));
        settings.then(boolSetting("shield-mace", "Auto-shield against mace", () -> BotSettings.get().isShieldMace(), v -> BotSettings.get().setShieldMace(v), BotSettings.DEFAULTS::isShieldMace));
        settings.then(intSetting("attack-cooldown", "Ticks between melee attacks", () -> BotSettings.get().getAttackCooldown(), v -> BotSettings.get().setAttackCooldown(v), 1, 40, BotSettings.DEFAULTS::getAttackCooldown));
        settings.then(doubleSetting("melee-range", "Melee attack range", () -> BotSettings.get().getMeleeRange(), v -> BotSettings.get().setMeleeRange(v), 2.0, 6.0, BotSettings.DEFAULTS::getMeleeRange));
        settings.then(doubleSetting("move-speed", "Movement speed multiplier", () -> BotSettings.get().getMoveSpeed(), v -> BotSettings.get().setMoveSpeed(v), 0.1, 2.0, BotSettings.DEFAULTS::getMoveSpeed));
        settings.then(boolSetting("auto-totem", "Auto-equip totem to offhand", () -> BotSettings.get().isAutoTotemEnabled(), v -> BotSettings.get().setAutoTotemEnabled(v), BotSettings.DEFAULTS::isAutoTotemEnabled));
        settings.then(boolSetting("totem-priority", "Keep totem over shield in offhand", () -> BotSettings.get().isTotemPriority(), v -> BotSettings.get().setTotemPriority(v), BotSettings.DEFAULTS::isTotemPriority));
        settings.then(boolSetting("auto-shield", "Auto-use shield", () -> BotSettings.get().isAutoShieldEnabled(), v -> BotSettings.get().setAutoShieldEnabled(v), BotSettings.DEFAULTS::isAutoShieldEnabled));
        settings.then(boolSetting("auto-potion", "Auto-use potions", () -> BotSettings.get().isAutoPotionEnabled(), v -> BotSettings.get().setAutoPotionEnabled(v), BotSettings.DEFAULTS::isAutoPotionEnabled));
        settings.then(boolSetting("shield-break", "Auto-axe enemy shield", () -> BotSettings.get().isShieldBreakEnabled(), v -> BotSettings.get().setShieldBreakEnabled(v), BotSettings.DEFAULTS::isShieldBreakEnabled));
        settings.then(boolSetting("prefer-sword", "Prefer sword over axe", () -> BotSettings.get().isPreferSword(), v -> BotSettings.get().setPreferSword(v), BotSettings.DEFAULTS::isPreferSword));
        settings.then(boolSetting("bhop", "Bunny-hop movement", () -> BotSettings.get().isBhopEnabled(), v -> BotSettings.get().setBhopEnabled(v), BotSettings.DEFAULTS::isBhopEnabled));
        settings.then(boolSetting("idle", "Wander when idle", () -> BotSettings.get().isIdleWanderEnabled(), v -> BotSettings.get().setIdleWanderEnabled(v), BotSettings.DEFAULTS::isIdleWanderEnabled));
        settings.then(doubleSetting("idle-radius", "Idle wander radius", () -> BotSettings.get().getIdleWanderRadius(), v -> BotSettings.get().setIdleWanderRadius(v), 3.0, 50.0, BotSettings.DEFAULTS::getIdleWanderRadius));
        settings.then(boolSetting("friendly-fire", "Allow attacking allies", () -> BotSettings.get().isFriendlyFireEnabled(), v -> BotSettings.get().setFriendlyFireEnabled(v), BotSettings.DEFAULTS::isFriendlyFireEnabled));
        settings.then(intSetting("miss-chance", "Chance to miss (%)", () -> BotSettings.get().getMissChance(), v -> BotSettings.get().setMissChance(v), 0, 100, BotSettings.DEFAULTS::getMissChance));
        settings.then(intSetting("mistake-chance", "Chance to aim wrong (%)", () -> BotSettings.get().getMistakeChance(), v -> BotSettings.get().setMistakeChance(v), 0, 100, BotSettings.DEFAULTS::getMistakeChance));
        settings.then(intSetting("shield-break-chance", "Shield break attempt chance (%)", () -> BotSettings.get().getShieldBreakChance(), v -> BotSettings.get().setShieldBreakChance(v), 0, 100, BotSettings.DEFAULTS::getShieldBreakChance));
        settings.then(intSetting("shield-hold-ticks", "Max ticks to hold shield", () -> BotSettings.get().getShieldHoldTicks(), v -> BotSettings.get().setShieldHoldTicks(v), 10, 200, BotSettings.DEFAULTS::getShieldHoldTicks));
        settings.then(intSetting("shield-raise-ticks", "Ticks to predict enemy attack", () -> BotSettings.get().getShieldRaiseTicks(), v -> BotSettings.get().setShieldRaiseTicks(v), 2, 40, BotSettings.DEFAULTS::getShieldRaiseTicks));
        settings.then(boolSetting("retreat", "Retreat when low HP", () -> BotSettings.get().isRetreatEnabled(), v -> BotSettings.get().setRetreatEnabled(v), BotSettings.DEFAULTS::isRetreatEnabled));
        settings.then(boolSetting("auto-eat", "Auto-eat when hungry", () -> BotSettings.get().isAutoEatEnabled(), v -> BotSettings.get().setAutoEatEnabled(v), BotSettings.DEFAULTS::isAutoEatEnabled));
        settings.then(boolSetting("auto-mend", "Auto-use XP for mending", () -> BotSettings.get().isAutoMendEnabled(), v -> BotSettings.get().setAutoMendEnabled(v), BotSettings.DEFAULTS::isAutoMendEnabled));
        settings.then(boolSetting("bot-leave-on-death", "Bot leaves on death", () -> BotSettings.get().isBotLeaveOnDeath(), v -> BotSettings.get().setBotLeaveOnDeath(v), BotSettings.DEFAULTS::isBotLeaveOnDeath));
        settings.then(boolSetting("attack-invincible", "Attack creative/spectator", () -> BotSettings.get().isAttackInvincible(), v -> BotSettings.get().setAttackInvincible(v), BotSettings.DEFAULTS::isAttackInvincible));
        settings.then(boolSetting("profile-lagg-fix", "Pre-populate profile cache to prevent lag on bot spawn", () -> BotSettings.get().isProfileLagFix(), v -> BotSettings.get().setProfileLagFix(v), BotSettings.DEFAULTS::isProfileLagFix));
        settings.then(boolSetting("safe-spawn", "Spread mass-spawned bots to prevent entity cramming", () -> BotSettings.get().isSafeSpawn(), v -> BotSettings.get().setSafeSpawn(v), BotSettings.DEFAULTS::isSafeSpawn));
        settings.then(boolSetting("clear-on-remove", "Clear bot inventory before remove/kill", () -> BotSettings.get().isClearOnRemove(), v -> BotSettings.get().setClearOnRemove(v), BotSettings.DEFAULTS::isClearOnRemove));
        settings.then(doubleSetting("aim-speed", "Aim rotation speed", () -> BotSettings.get().getAimSpeed(), v -> BotSettings.get().setAimSpeed(v), 3.0, 45.0, BotSettings.DEFAULTS::getAimSpeed));
        settings.then(doubleSetting("view-distance", "Max target acquisition range", () -> BotSettings.get().getMaxTargetDistance(), v -> BotSettings.get().setMaxTargetDistance(v), 5.0, 128.0, BotSettings.DEFAULTS::getMaxTargetDistance));
        settings.then(intSetting("max-mass-spawn", "Max bots per mass-spawn command", () -> BotSettings.get().getMaxMassSpawn(), v -> BotSettings.get().setMaxMassSpawn(v), 50, 10000, BotSettings.DEFAULTS::getMaxMassSpawn));

        return settings;
    }

    // ========== FAST COMMANDS ==========

    private static final SuggestionProvider<class_2168> FACTION_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String f : BotFaction.getAllFactions()) {
                if (f.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(f);
                }
            }
            return builder.buildFuture();
        };

    // ========== SETTING HELPERS ==========

    private static LiteralArgumentBuilder<class_2168> boolSetting(String name, String desc, BooleanSupplier getter, Consumer<Boolean> setter, BooleanSupplier defaultGetter) {
        return class_2170.method_9247(name)
            .executes(ctx -> {
                ctx.getSource().method_9226(() -> class_2561.method_43470("§7[Tip] §a" + desc + " §7(current: " + getter.getAsBoolean() + ", default: " + defaultGetter.getAsBoolean() + ")"), false);
                return 1;
            })
            .then(class_2170.method_9244("value", BoolArgumentType.bool())
                .executes(ctx -> {
                    boolean value = BoolArgumentType.getBool(ctx, "value");
                    setter.accept(value);
                    ctx.getSource().method_9226(() -> class_2561.method_43470(name + ": " + value), true);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<class_2168> intSetting(String name, String desc, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, int min, int max, java.util.function.IntSupplier defaultGetter) {
        return class_2170.method_9247(name)
            .executes(ctx -> {
                ctx.getSource().method_9226(() -> class_2561.method_43470("§7[Tip] §a" + desc + " §7(current: " + getter.getAsInt() + ", default: " + defaultGetter.getAsInt() + ")"), false);
                return 1;
            })
            .then(class_2170.method_9244("value", IntegerArgumentType.integer(min, max))
                .executes(ctx -> {
                    int value = IntegerArgumentType.getInteger(ctx, "value");
                    setter.accept(value);
                    ctx.getSource().method_9226(() -> class_2561.method_43470(name + ": " + value), true);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<class_2168> doubleSetting(String name, String desc, java.util.function.DoubleSupplier getter, java.util.function.DoubleConsumer setter, double min, double max, java.util.function.DoubleSupplier defaultGetter) {
        return class_2170.method_9247(name)
            .executes(ctx -> {
                ctx.getSource().method_9226(() -> class_2561.method_43470("§7[Tip] §a" + desc + " §7(current: " + getter.getAsDouble() + ", default: " + defaultGetter.getAsDouble() + ")"), false);
                return 1;
            })
            .then(class_2170.method_9244("value", DoubleArgumentType.doubleArg(min, max))
                .executes(ctx -> {
                    double value = DoubleArgumentType.getDouble(ctx, "value");
                    setter.accept(value);
                    ctx.getSource().method_9226(() -> class_2561.method_43470(name + ": " + value), true);
                    return 1;
                }));
    }

    // ========== COMMAND HANDLERS ==========

    private static int spawn(CommandContext<class_2168> ctx, String name) {
        var source = ctx.getSource();
        String botName = name != null ? name : BotNameGenerator.generateUniqueName();
        var server = source.method_9211();
        var existingPlayer = server.method_3760().method_14566(botName);
        if (existingPlayer != null && !BotManager.getAllBots().contains(botName)) {
            source.method_9213(class_2561.method_43470("Cannot create bot '" + botName + "': a real player with this name is online!"));
            return 0;
        }
        if (BotManager.spawnBot(server, botName, source)) {
            source.method_9226(() -> class_2561.method_43470("PvP Bot '" + botName + "' spawned!"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Failed to spawn bot '" + botName + "' (bot already exists or name is taken)"));
            return 0;
        }
    }

    private static int massSpawn(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        var server = source.method_9211();
        int count = IntegerArgumentType.getInteger(ctx, "count");
        int maxAllowed = BotSettings.get().getMaxMassSpawn();
        if (count > maxAllowed) {
            source.method_9213(class_2561.method_43470("Cannot spawn more than " + maxAllowed + " bots at once. Use /pvpbot settings max-mass-spawn to increase the limit."));
            return 0;
        }
        source.method_9226(() -> class_2561.method_43470("Spawning " + count + " bots..."), false);
        int[] spawned = {0};
        int[] current = {0};
        class_243 basePos = null;
        if (BotSettings.get().isSafeSpawn()) {
            var player = source.method_44023();
            if (player != null) {
                basePos = new class_243(player.method_23317(), player.method_23318(), player.method_23321());
            }
        }
        scheduleSpawn(server, source, count, spawned, current, basePos);
        return 1;
    }

    private static void scheduleSpawn(MinecraftServer server, class_2168 source, int total, int[] spawned, int[] current, class_243 basePos) {
        if (current[0] >= total) {
            source.method_9226(() -> class_2561.method_43470("Finished! Spawned " + spawned[0] + " bots."), true);
            return;
        }
        String name = BotNameGenerator.generateUniqueName();
        class_243 spawnPos = null;
        if (basePos != null) {
            var rng = java.util.concurrent.ThreadLocalRandom.current();
            double dx = (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
            double dz = (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
            spawnPos = new class_243(basePos.field_1352 + dx, basePos.field_1351, basePos.field_1350 + dz);
        }
        if (BotManager.spawnBot(server, name, source, spawnPos)) {
            spawned[0]++;
        }
        current[0]++;
        server.execute(() -> {
            int[] delay = {0};
            server.execute(new Runnable() {
                @Override
                public void run() {
                    delay[0]++;
                    if (delay[0] < 5) {
                        server.execute(this);
                    } else {
                        scheduleSpawn(server, source, total, spawned, current, basePos);
                    }
                }
            });
        });
    }

    private static int remove(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotManager.removeBot(source.method_9211(), name, source)) {
            source.method_9226(() -> class_2561.method_43470("Bot '" + name + "' removed!"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Bot '" + name + "' not found!"));
            return 0;
        }
    }

    private static int removeAll(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        int count = BotManager.getBotCount();
        BotManager.removeAllBots(source.method_9211(), source);
        source.method_9226(() -> class_2561.method_43470("Removed " + count + " bots"), true);
        return count;
    }

    private static int reload(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        var server = source.method_9211();
        BotSettings.load();
        BotKits.reload(server);
        BotPath.init();
        BotManager.reloadBots();
        source.method_9226(() -> class_2561.method_43470("All configurations reloaded!"), true);
        return 1;
    }

    private static int settings(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        BotSettings s = BotSettings.get();
        source.sendFeedback(() -> Text.literal("shield-break: " + s.isShieldBreakEnabled()), false);
        source.sendFeedback(() -> Text.literal("shield-break-chance: " + s.getShieldBreakChance() + "%"), false);
        source.sendFeedback(() -> Text.literal("stun: " + s.isStunEnabled()), false);
        source.sendFeedback(() -> Text.literal("stun-chance: " + s.getStunChance() + "%"), false);
        source.method_9226(() -> class_2561.method_43470("=== Equipment Settings ==="), false);
        source.method_9226(() -> class_2561.method_43470("auto-armor: " + s.isAutoEquipArmor()), false);
        source.method_9226(() -> class_2561.method_43470("auto-weapon: " + s.isAutoEquipWeapon()), false);
        source.method_9226(() -> class_2561.method_43470("drop-armor: " + s.isDropWorseArmor()), false);
        source.method_9226(() -> class_2561.method_43470("drop-weapon: " + s.isDropWorseWeapons()), false);
        source.method_9226(() -> class_2561.method_43470("drop-distance: " + s.getDropDistance()), false);
        source.method_9226(() -> class_2561.method_43470("interval: " + s.getCheckInterval() + " ticks"), false);
        source.method_9226(() -> class_2561.method_43470("=== Combat Settings ==="), false);
        source.method_9226(() -> class_2561.method_43470("combat: " + s.isCombatEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("revenge: " + s.isRevengeEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("auto-target: " + s.isAutoTargetEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("target-players: " + s.isTargetPlayers()), false);
        source.method_9226(() -> class_2561.method_43470("target-mobs: " + s.isTargetHostileMobs()), false);
        source.method_9226(() -> class_2561.method_43470("target-bots: " + s.isTargetOtherBots()), false);
        source.method_9226(() -> class_2561.method_43470("criticals: " + s.isCriticalsEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("ranged: " + s.isRangedEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("ranged-min-range: " + s.getRangedMinRange()), false);
        source.method_9226(() -> class_2561.method_43470("ranged-optimal-range: " + s.getRangedOptimalRange()), false);
        source.method_9226(() -> class_2561.method_43470("ranged-max-range: " + s.getRangedMaxRange()), false);
        source.method_9226(() -> class_2561.method_43470("bow-draw-ticks: " + s.getBowMinDrawTime() + " ticks"), false);
        source.method_9226(() -> class_2561.method_43470("arrow-prediction: " + s.isArrowPredictionEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("ranged-strafe: " + s.isRangedStrafeEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("ranged-retreat: " + s.isRangedRetreatOnClose()), false);
        source.method_9226(() -> class_2561.method_43470("mace: " + s.isMaceEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("special-names: " + s.isUseSpecialNames()), false);
        source.method_9226(() -> class_2561.method_43470("shield-mace: " + s.isShieldMace()), false);
        source.method_9226(() -> class_2561.method_43470("attack-cooldown: " + s.getAttackCooldown() + " ticks"), false);
        source.method_9226(() -> class_2561.method_43470("melee-range: " + s.getMeleeRange()), false);
        source.method_9226(() -> class_2561.method_43470("move-speed: " + s.getMoveSpeed()), false);
        source.method_9226(() -> class_2561.method_43470("=== Utilities ==="), false);
        source.method_9226(() -> class_2561.method_43470("auto-totem: " + s.isAutoTotemEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("totem-priority: " + s.isTotemPriority() + " (don't replace totem with shield)"), false);
        source.method_9226(() -> class_2561.method_43470("auto-shield: " + s.isAutoShieldEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("auto-potion: " + s.isAutoPotionEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("shield-break: " + s.isShieldBreakEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("prefer-sword: " + s.isPreferSword()), false);
        source.method_9226(() -> class_2561.method_43470("bot-leave-on-death: " + s.isBotLeaveOnDeath()), false);
        source.method_9226(() -> class_2561.method_43470("attack-invincible: " + s.isAttackInvincible()), false);
        source.method_9226(() -> class_2561.method_43470("aim-speed: " + s.getAimSpeed()), false);
        source.method_9226(() -> class_2561.method_43470("=== Navigation Settings ==="), false);
        source.method_9226(() -> class_2561.method_43470("bhop: " + s.isBhopEnabled()), false);

        source.method_9226(() -> class_2561.method_43470("idle: " + s.isIdleWanderEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("idle-radius: " + s.getIdleWanderRadius()), false);
        source.method_9226(() -> class_2561.method_43470("=== Factions & Mistakes ==="), false);
        source.method_9226(() -> class_2561.method_43470("factions: " + s.isFactionsEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("friendly-fire: " + s.isFriendlyFireEnabled()), false);
        source.method_9226(() -> class_2561.method_43470("miss-chance: " + s.getMissChance() + "%"), false);
        source.method_9226(() -> class_2561.method_43470("mistake-chance: " + s.getMistakeChance() + "%"), false);
        source.method_9226(() -> class_2561.method_43470("shield-break-chance: " + s.getShieldBreakChance() + "%"), false);

        return 1;
    }

    // ========== BOT-MANAGEMENT HANDLERS ==========

    private static int botAttack(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String botname = StringArgumentType.getString(ctx, "botname");
        String target = StringArgumentType.getString(ctx, "target");
        if (!BotManager.getAllBots().contains(botname)) {
            source.method_9213(class_2561.method_43470("Bot '" + botname + "' not found!"));
            return 0;
        }
        BotCombat.setTarget(botname, target);
        source.method_9226(() -> class_2561.method_43470("Bot '" + botname + "' now attacking '" + target + "'"), true);
        return 1;
    }

    private static int botStopAttack(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String botname = StringArgumentType.getString(ctx, "botname");
        if (!BotManager.getAllBots().contains(botname)) {
            source.method_9213(class_2561.method_43470("Bot '" + botname + "' not found!"));
            return 0;
        }
        BotCombat.clearTarget(botname);
        source.method_9226(() -> class_2561.method_43470("Bot '" + botname + "' stopped attacking"), true);
        return 1;
    }

    private static int botInventory(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String botname = StringArgumentType.getString(ctx, "botname");
        if (!BotManager.getAllBots().contains(botname)) {
            source.method_9213(class_2561.method_43470("Bot '" + botname + "' not found!"));
            return 0;
        }
        var bot = source.method_9211().method_3760().method_14566(botname);
        if (bot == null) {
            source.method_9213(class_2561.method_43470("Bot '" + botname + "' not online!"));
            return 0;
        }
        var inventory = bot.method_31548();
        source.method_9226(() -> class_2561.method_43470("=== Inventory: " + botname + " ==="), false);
        StringBuilder hotbar = new StringBuilder("Hotbar: ");
        for (int i = 0; i < 9; i++) {
            var stack = inventory.method_5438(i);
            if (!stack.method_7960()) {
                hotbar.append("[").append(stack.method_7964().getString()).append(" x").append(stack.method_7947()).append("] ");
            }
        }
        source.method_9226(() -> class_2561.method_43470(hotbar.toString().trim()), false);
        StringBuilder mainInv = new StringBuilder("Main: ");
        for (int i = 9; i < 36; i++) {
            var stack = inventory.method_5438(i);
            if (!stack.method_7960()) {
                mainInv.append("[").append(stack.method_7964().getString()).append(" x").append(stack.method_7947()).append("] ");
            }
        }
        source.method_9226(() -> class_2561.method_43470(mainInv.toString().trim()), false);
        StringBuilder armor = new StringBuilder("Armor: ");
        for (int i = 36; i < 40; i++) {
            var stack = inventory.method_5438(i);
            if (!stack.method_7960()) {
                armor.append("[").append(stack.method_7964().getString()).append("] ");
            }
        }
        String armorStr = armor.toString().trim();
        if (!armorStr.equals("Armor:")) {
            source.method_9226(() -> class_2561.method_43470(armorStr), false);
        }
        var offhand = inventory.method_5438(40);
        if (!offhand.method_7960()) {
            source.method_9226(() -> class_2561.method_43470("Offhand: [" + offhand.method_7964().getString() + " x" + offhand.method_7947() + "]"), false);
        }
        source.method_9226(() -> class_2561.method_43470("HP: " + String.format("%.1f", bot.method_6032()) + "/" + String.format("%.1f", bot.method_6063()) +
            " | Food: " + bot.method_7344().method_7586() +
            " | XP: " + bot.field_7520), false);
        return 1;
    }

    private static int botList(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        var bots = BotManager.getAllBots();
        if (bots.isEmpty()) {
            source.method_9226(() -> class_2561.method_43470("No active PvP bots"), false);
        } else {
            source.method_9226(() -> class_2561.method_43470("Active PvP bots (" + bots.size() + "):"), false);
            for (String botName : bots) {
                source.method_9226(() -> class_2561.method_43470(" - " + botName), false);
            }
        }
        return bots.size();
    }

    // ========== PATH HANDLERS ==========

    private static int pathCreate(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.createPath(name)) {
            BotPath.setPathVisible(name, true);
            source.method_9226(() -> class_2561.method_43470("Path '" + name + "' created"), true);
            source.method_9226(() -> class_2561.method_43470("Visualization enabled. To disable: /pvpbot path show " + name + " false"), false);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + name + "' already exists"));
            return 0;
        }
    }

    private static int pathDelete(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.deletePath(name)) {
            source.method_9226(() -> class_2561.method_43470("Path '" + name + "' deleted"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathAddPoint(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        class_3222 player = source.method_44023();
        if (player == null) {
            source.method_9213(class_2561.method_43470("Only players can add path points"));
            return 0;
        }
        class_243 pos = new class_243(player.method_23317(), player.method_23318(), player.method_23321());
        if (BotPath.addPoint(name, pos)) {
            var path = BotPath.getPath(name);
            if (!BotPath.isPathVisible(name)) {
                BotPath.setPathVisible(name, true);
                source.method_9226(() -> class_2561.method_43470("Visualization enabled. To disable: /pvpbot path show " + name + " false"), false);
            }
            source.method_9226(() -> class_2561.method_43470(String.format("Point #%d added to path '%s' at (%.1f, %.1f, %.1f)", path.points.size(), name, pos.field_1352, pos.field_1351, pos.field_1350)), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathRemovePoint(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        int index = IntegerArgumentType.getInteger(ctx, "index");
        if (BotPath.removePoint(name, index)) {
            source.method_9226(() -> class_2561.method_43470("Point #" + index + " removed from path '" + name + "'"), true);
            return 1;
        }
        source.method_9213(class_2561.method_43470("Invalid path or index"));
        return 0;
    }

    private static int pathRemovePointLast(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.removeLastPoint(name)) {
            source.method_9226(() -> class_2561.method_43470("Last point removed from path '" + name + "'"), true);
            return 1;
        }
        source.method_9213(class_2561.method_43470("Invalid path or index"));
        return 0;
    }

    private static int pathClear(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.clearPath(name)) {
            source.method_9226(() -> class_2561.method_43470("All points cleared from path '" + name + "'"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathLoop(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        boolean value = BoolArgumentType.getBool(ctx, "value");
        if (BotPath.setLoop(name, value)) {
            source.method_9226(() -> class_2561.method_43470("Path '" + name + "' loop: " + value), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathStart(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String bot = StringArgumentType.getString(ctx, "bot");
        String path = StringArgumentType.getString(ctx, "path");
        if (BotPath.startFollowing(bot, path)) {
            source.method_9226(() -> class_2561.method_43470("Bot '" + bot + "' started following path '" + path + "'"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + path + "' not found or empty"));
            return 0;
        }
    }

    private static int pathStop(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String bot = StringArgumentType.getString(ctx, "bot");
        if (BotPath.stopFollowing(bot)) {
            source.method_9226(() -> class_2561.method_43470("Bot '" + bot + "' stopped following path"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Bot '" + bot + "' is not following any path"));
            return 0;
        }
    }

    private static int pathWalkType(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        String type = StringArgumentType.getString(ctx, "type");
        if (!type.equals("bhop") && !type.equals("sprint") && !type.equals("walk")) {
            source.method_9213(class_2561.method_43470("Invalid walk type. Use: bhop, sprint, or walk"));
            return 0;
        }
        if (BotPath.setWalkType(name, type)) {
            source.method_9226(() -> class_2561.method_43470("Path '" + name + "' walk type: " + type), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathList(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        var paths = BotPath.getAllPaths();
        if (paths.isEmpty()) {
            source.method_9226(() -> class_2561.method_43470("No paths created"), false);
            return 0;
        }
        source.method_9226(() -> class_2561.method_43470("=== Paths ==="), false);
        for (var entry : paths.entrySet()) {
            String name = entry.getKey();
            var path = entry.getValue();
            source.method_9226(() -> class_2561.method_43470(String.format("%s: %d points, loop: %s, attack: %s, walk-type: %s", name, path.points.size(), path.loop, path.attack, path.walkType)), false);
        }
        return paths.size();
    }

    private static int pathShow(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        boolean visible = BoolArgumentType.getBool(ctx, "visible");
        if (BotPath.setPathVisible(name, visible)) {
            if (visible) {
                source.method_9226(() -> class_2561.method_43470("Path '" + name + "' visualization enabled"), true);
                source.method_9226(() -> class_2561.method_43470("To disable: /pvpbot path show " + name + " false"), false);
            } else {
                source.method_9226(() -> class_2561.method_43470("Path '" + name + "' visualization disabled"), true);
            }
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathInfo(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        var path = BotPath.getPath(name);
        if (path == null) {
            source.method_9213(class_2561.method_43470("Path '" + name + "' not found"));
            return 0;
        }
        source.method_9226(() -> class_2561.method_43470("=== Path: " + name + " ==="), false);
        source.method_9226(() -> class_2561.method_43470("Points: " + path.points.size()), false);
        source.method_9226(() -> class_2561.method_43470("Loop: " + path.loop), false);
        source.method_9226(() -> class_2561.method_43470("Attack: " + path.attack), false);
        source.method_9226(() -> class_2561.method_43470("Walk type: " + path.walkType), false);
        for (int i = 0; i < path.points.size(); i++) {
            var point = path.points.get(i);
            int index = i;
            source.method_9226(() -> class_2561.method_43470(String.format("#%d: (%.1f, %.1f, %.1f)", index, point.field_1352, point.field_1351, point.field_1350)), false);
        }
        return 1;
    }

    private static int pathDistribute(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String pathName = StringArgumentType.getString(ctx, "path");
        var path = BotPath.getPath(pathName);
        if (path == null) {
            source.method_9213(class_2561.method_43470("Path '" + pathName + "' not found"));
            return 0;
        }
        if (path.points.isEmpty()) {
            source.method_9213(class_2561.method_43470("Path '" + pathName + "' has no points"));
            return 0;
        }
        var server = source.method_9211();
        var botsOnPath = new java.util.ArrayList<String>();
        for (String botName : BotManager.getAllBots()) {
            if (BotPath.isFollowing(botName, pathName)) {
                botsOnPath.add(botName);
            }
        }
        if (botsOnPath.isEmpty()) {
            source.method_9213(class_2561.method_43470("No bots are following path '" + pathName + "'"));
            return 0;
        }
        int totalPoints = path.points.size();
        int botCount = botsOnPath.size();
        for (int i = 0; i < botCount; i++) {
            String botName = botsOnPath.get(i);
            int pointIndex = (i * totalPoints) / botCount;
            BotPath.setBotPathIndex(botName, pointIndex);
            var point = path.points.get(pointIndex);
            try {
                String tpCommand = String.format(java.util.Locale.US, "tp %s %.2f %.2f %.2f", botName, point.field_1352, point.field_1351 + 1.0, point.field_1350);
                server.method_3734().method_9235().execute(tpCommand, server.method_3739());
            } catch (Exception e) {
            }
        }
        source.method_9226(() -> class_2561.method_43470("Distributed " + botCount + " bots along path '" + pathName + "'"), true);
        return botCount;
    }

    private static int pathStartNear(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String pathName = StringArgumentType.getString(ctx, "path");
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        var path = BotPath.getPath(pathName);
        if (path == null) {
            source.method_9213(class_2561.method_43470("Path '" + pathName + "' not found"));
            return 0;
        }
        class_3222 player = source.method_44023();
        if (player == null) {
            source.method_9213(class_2561.method_43470("This command can only be used by a player"));
            return 0;
        }
        var server = source.method_9211();
        int started = 0;
        for (String botName : BotManager.getAllBots()) {
            class_3222 bot = server.method_3760().method_14566(botName);
            if (bot != null && bot.method_5739(player) <= radius) {
                if (BotPath.startFollowing(botName, pathName)) {
                    started++;
                }
            }
        }
        if (started > 0) {
            int finalStarted = started;
            source.method_9226(() -> class_2561.method_43470("Started path '" + pathName + "' for " + finalStarted + " bots within " + radius + " blocks"), true);
            return started;
        } else {
            source.method_9213(class_2561.method_43470("No bots found within " + radius + " blocks"));
            return 0;
        }
    }

    private static int pathStopAll(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String pathName = StringArgumentType.getString(ctx, "path");
        var path = BotPath.getPath(pathName);
        if (path == null) {
            source.method_9213(class_2561.method_43470("Path '" + pathName + "' not found"));
            return 0;
        }
        int stopped = 0;
        for (String botName : BotManager.getAllBots()) {
            if (BotPath.isFollowing(botName, pathName)) {
                if (BotPath.stopFollowing(botName)) {
                    stopped++;
                }
            }
        }
        if (stopped > 0) {
            int finalStopped = stopped;
            source.method_9226(() -> class_2561.method_43470("Stopped " + finalStopped + " bots on path '" + pathName + "'"), true);
            return stopped;
        } else {
            source.method_9213(class_2561.method_43470("No bots are following path '" + pathName + "'"));
            return 0;
        }
    }

    // ========== KIT HANDLERS ==========

    private static int kitCreate(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        var player = source.method_44023();
        if (player == null) {
            source.method_9213(class_2561.method_43470("This command must be run by a player!"));
            return 0;
        }
        if (BotKits.kitExists(name)) {
            source.method_9213(class_2561.method_43470("Kit '" + name + "' already exists!"));
            return 0;
        }
        if (BotKits.createKit(name, player)) {
            source.method_9226(() -> class_2561.method_43470("Kit '" + name + "' created from your inventory!"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Failed to create kit (empty inventory?)"));
            return 0;
        }
    }

    private static int kitDelete(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotKits.deleteKit(name)) {
            source.method_9226(() -> class_2561.method_43470("Kit '" + name + "' deleted!"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Kit '" + name + "' not found!"));
            return 0;
        }
    }

    private static int kitGive(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String playername = StringArgumentType.getString(ctx, "playername");
        String kitname = StringArgumentType.getString(ctx, "kitname");
        if (!BotKits.kitExists(kitname)) {
            source.method_9213(class_2561.method_43470("Kit '" + kitname + "' not found!"));
            return 0;
        }
        var player = source.method_9211().method_3760().method_14566(playername);
        if (player == null) {
            source.method_9213(class_2561.method_43470("Player '" + playername + "' not found!"));
            return 0;
        }
        if (BotKits.giveKit(kitname, player)) {
            source.method_9226(() -> class_2561.method_43470("Gave kit '" + kitname + "' to '" + playername + "'"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Failed to give kit!"));
            return 0;
        }
    }

    private static int kitList(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        var kits = BotKits.getKitNames();
        if (kits.isEmpty()) {
            source.method_9226(() -> class_2561.method_43470("No kits created. Use /pvpbot kit create-kit <name> to create one."), false);
        } else {
            source.method_9226(() -> class_2561.method_43470("Kits (" + kits.size() + "): " + String.join(", ", kits)), false);
        }
        return 1;
    }

    // ========== FACTION HANDLERS ==========

    private static int factionList(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        var factions = BotFaction.getAllFactions();
        if (factions.isEmpty()) {
            source.method_9226(() -> class_2561.method_43470("No factions created"), false);
        } else {
            source.method_9226(() -> class_2561.method_43470("Factions (" + factions.size() + "):"), false);
            for (String faction : factions) {
                var members = BotFaction.getMembers(faction);
                var enemies = BotFaction.getHostileFactions(faction);
                source.method_9226(() -> class_2561.method_43470(" - " + faction + " (" + members.size() + " members, " + enemies.size() + " enemies)"), false);
            }
        }
        return factions.size();
    }

    private static int factionCreate(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotFaction.createFaction(name)) {
            source.method_9226(() -> class_2561.method_43470("Faction '" + name + "' created!"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Faction '" + name + "' already exists!"));
            return 0;
        }
    }

    private static int factionDelete(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotFaction.deleteFaction(name)) {
            source.method_9226(() -> class_2561.method_43470("Faction '" + name + "' deleted!"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Faction '" + name + "' not found!"));
            return 0;
        }
    }

    private static int factionAdd(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String player = StringArgumentType.getString(ctx, "player");
        if (BotFaction.addMember(faction, player)) {
            source.method_9226(() -> class_2561.method_43470("Added '" + player + "' to faction '" + faction + "'"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
    }

    private static int factionRemove(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String player = StringArgumentType.getString(ctx, "player");
        if (BotFaction.removeMember(faction, player)) {
            source.method_9226(() -> class_2561.method_43470("Removed '" + player + "' from faction '" + faction + "'"), true);
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("Failed to remove '" + player + "' from faction '" + faction + "'"));
            return 0;
        }
    }

    private static int factionHostile(CommandContext<class_2168> ctx, boolean isHostile) {
        var source = ctx.getSource();
        String faction1 = StringArgumentType.getString(ctx, "faction1");
        String faction2 = StringArgumentType.getString(ctx, "faction2");
        if (BotFaction.setHostile(faction1, faction2, isHostile)) {
            if (isHostile) {
                source.method_9226(() -> class_2561.method_43470("Factions '" + faction1 + "' and '" + faction2 + "' are now hostile!"), true);
            } else {
                source.method_9226(() -> class_2561.method_43470("Factions '" + faction1 + "' and '" + faction2 + "' are now neutral"), true);
            }
            return 1;
        } else {
            source.method_9213(class_2561.method_43470("One or both factions not found, or same faction!"));
            return 0;
        }
    }

    private static int factionInfo(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        var members = BotFaction.getMembers(faction);
        var enemies = BotFaction.getHostileFactions(faction);
        if (members.isEmpty() && enemies.isEmpty() && !BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        source.method_9226(() -> class_2561.method_43470("=== Faction: " + faction + " ==="), false);
        source.method_9226(() -> class_2561.method_43470("Members (" + members.size() + "): " + String.join(", ", members)), false);
        source.method_9226(() -> class_2561.method_43470("Hostile to (" + enemies.size() + "): " + String.join(", ", enemies)), false);
        return 1;
    }

    private static int factionAddNear(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        var entity = source.method_9228();
        if (entity == null) {
            source.method_9213(class_2561.method_43470("This command must be run by a player!"));
            return 0;
        }
        int count = 0;
        var allBots = BotManager.getAllBots();
        var server = source.method_9211();
        for (String botName : allBots) {
            var bot = server.method_3760().method_14566(botName);
            if (bot != null && bot.method_5739(entity) <= radius) {
                BotFaction.addMember(faction, botName);
                count++;
            }
        }
        int finalCount = count;
        source.method_9226(() -> class_2561.method_43470("Added " + finalCount + " bots to faction '" + faction + "'"), true);
        return count;
    }

    private static int factionAddAll(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        var allBots = BotManager.getAllBots();
        int count = 0;
        for (String botName : allBots) {
            BotFaction.addMember(faction, botName);
            count++;
        }
        int finalCount = count;
        source.method_9226(() -> class_2561.method_43470("Added " + finalCount + " bots to faction '" + faction + "'"), true);
        return count;
    }

    private static int factionGive(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String itemCommand = StringArgumentType.getString(ctx, "item");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        var members = BotFaction.getMembers(faction);
        var server = source.method_9211();
        int count = 0;
        for (String memberName : members) {
            try {
                server.method_3734().method_9235().execute("give " + memberName + " " + itemCommand, server.method_3739());
                count++;
            } catch (Exception e) {
            }
        }
        int finalCount = count;
        source.method_9226(() -> class_2561.method_43470("Gave items to " + finalCount + " members of faction '" + faction + "'"), true);
        return count;
    }

    private static int factionAttack(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String target = StringArgumentType.getString(ctx, "target");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        var members = BotFaction.getMembers(faction);
        int count = 0;
        for (String memberName : members) {
            if (BotManager.getAllBots().contains(memberName)) {
                BotCombat.setTarget(memberName, target);
                count++;
            }
        }
        int finalCount = count;
        source.method_9226(() -> class_2561.method_43470("Faction '" + faction + "' (" + finalCount + " bots) attacking " + target + "!"), true);
        return count;
    }

    private static int factionStartPath(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String path = StringArgumentType.getString(ctx, "path");
        var members = BotFaction.getMembers(faction);
        if (members.isEmpty()) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found or has no members"));
            return 0;
        }
        var botPath = BotPath.getPath(path);
        if (botPath == null) {
            source.method_9213(class_2561.method_43470("Path '" + path + "' not found"));
            return 0;
        }
        int started = 0;
        for (String member : members) {
            if (BotManager.getAllBots().contains(member)) {
                if (BotPath.startFollowing(member, path)) {
                    started++;
                }
            }
        }
        if (started > 0) {
            int finalStarted = started;
            source.method_9226(() -> class_2561.method_43470("Started path '" + path + "' for " + finalStarted + " bots in faction '" + faction + "'"), true);
            return started;
        } else {
            source.method_9213(class_2561.method_43470("No bots in faction '" + faction + "'"));
            return 0;
        }
    }

    private static int factionStopPath(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        var members = BotFaction.getMembers(faction);
        if (members.isEmpty()) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found or has no members"));
            return 0;
        }
        int stopped = 0;
        for (String member : members) {
            if (BotManager.getAllBots().contains(member)) {
                if (BotPath.stopFollowing(member)) {
                    stopped++;
                }
            }
        }
        if (stopped > 0) {
            int finalStopped = stopped;
            source.method_9226(() -> class_2561.method_43470("Stopped path for " + finalStopped + " bots in faction '" + faction + "'"), true);
            return stopped;
        } else {
            source.method_9213(class_2561.method_43470("No bots in faction '" + faction + "' were following a path"));
            return 0;
        }
    }

    private static int factionGiveKit(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String kitname = StringArgumentType.getString(ctx, "kitname");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        if (!BotKits.kitExists(kitname)) {
            source.method_9213(class_2561.method_43470("Kit '" + kitname + "' not found!"));
            return 0;
        }
        var members = BotFaction.getMembers(faction);
        if (members == null || members.isEmpty()) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' has no members!"));
            return 0;
        }
        int count = 0;
        for (String memberName : members) {
            if (BotManager.getAllBots().contains(memberName)) {
                var bot = BotManager.getBot(source.method_9211(), memberName);
                if (bot != null && BotKits.giveKit(kitname, bot)) {
                    count++;
                }
            }
        }
        int finalCount = count;
        source.method_9226(() -> class_2561.method_43470("Gave kit '" + kitname + "' to " + finalCount + " bots in faction '" + faction + "'"), true);
        return 1;
    }

    // ========== GIVE-KIT-NEAR ==========

    private static int kitGiveNear(CommandContext<class_2168> ctx, double radius) {
        var source = ctx.getSource();
        String kitname = StringArgumentType.getString(ctx, "kitname");
        if (!BotKits.kitExists(kitname)) {
            source.method_9213(class_2561.method_43470("Kit '" + kitname + "' not found!"));
            return 0;
        }
        var player = source.method_44023();
        if (player == null) {
            source.method_9213(class_2561.method_43470("This command must be run by a player!"));
            return 0;
        }
        var server = source.method_9211();
        int count = 0;
        for (String botName : BotManager.getAllBots()) {
            class_3222 bot = server.method_3760().method_14566(botName);
            if (bot != null && bot.method_5739(player) <= radius) {
                if (BotKits.giveKit(kitname, bot)) {
                    count++;
                }
            }
        }
        if (count > 0) {
            int finalCount = count;
            source.method_9226(() -> class_2561.method_43470("Gave kit '" + kitname + "' to " + finalCount + " bots within " + radius + " blocks"), true);
            return count;
        } else {
            source.method_9213(class_2561.method_43470("No bots found within " + radius + " blocks"));
            return 0;
        }
    }

    // ========== GIVE-KIT-NEAR-RANDOM ==========

    private static int kitGiveNearRandom(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        String kitsArg = StringArgumentType.getString(ctx, "kits");
        var player = source.method_44023();
        if (player == null) {
            source.method_9213(class_2561.method_43470("This command must be run by a player!"));
            return 0;
        }
        String[] parts = kitsArg.split(" ");
        if (parts.length < 2 || parts.length % 2 != 0) {
            source.method_9213(class_2561.method_43470("Usage: /pvpbot kit give-kit-near-random <radius> <kit> <weight>% [<kit> <weight>% ...]"));
            return 0;
        }
        java.util.LinkedHashMap<String, Integer> weights = new java.util.LinkedHashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            String kitName = parts[i];
            String weightStr = parts[i + 1];
            if (!weightStr.endsWith("%")) {
                source.method_9213(class_2561.method_43470("Invalid weight '" + weightStr + "' for kit '" + kitName + "'. Use format: <weight>%"));
                return 0;
            }
            if (!BotKits.kitExists(kitName)) {
                source.method_9213(class_2561.method_43470("Kit '" + kitName + "' not found!"));
                return 0;
            }
            try {
                int w = Integer.parseInt(weightStr.substring(0, weightStr.length() - 1));
                if (w <= 0) {
                    source.method_9213(class_2561.method_43470("Weight must be positive for kit '" + kitName + "'"));
                    return 0;
                }
                weights.put(kitName, w);
            } catch (NumberFormatException e) {
                source.method_9213(class_2561.method_43470("Invalid weight '" + weightStr + "' for kit '" + kitName + "'"));
                return 0;
            }
        }
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        var server = source.method_9211();
        int count = 0;
        java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();
        for (String botName : BotManager.getAllBots()) {
            class_3222 bot = server.method_3760().method_14566(botName);
            if (bot == null || bot.method_5739(player) > radius) continue;
            int roll = rng.nextInt(totalWeight);
            int cumulative = 0;
            String selected = null;
            for (var entry : weights.entrySet()) {
                cumulative += entry.getValue();
                if (roll < cumulative) {
                    selected = entry.getKey();
                    break;
                }
            }
            if (selected != null && BotKits.giveKit(selected, bot)) {
                count++;
            }
        }
        if (count > 0) {
            int finalCount = count;
            source.method_9226(() -> class_2561.method_43470("Gave random kits to " + finalCount + " bots within " + radius + " blocks"), true);
            return count;
        } else {
            source.method_9213(class_2561.method_43470("No bots found within " + radius + " blocks"));
            return 0;
        }
    }

    // ========== FACTION GIVE-KIT-RANDOM ==========

    private static int factionGiveKitRandom(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        String kitsArg = StringArgumentType.getString(ctx, "kits");
        String[] parts = kitsArg.split(" ");
        if (parts.length < 2 || parts.length % 2 != 0) {
            source.method_9213(class_2561.method_43470("Usage: /pvpbot faction give-kit-random <faction> <kit> <weight>% [<kit> <weight>% ...]"));
            return 0;
        }
        java.util.LinkedHashMap<String, Integer> weights = new java.util.LinkedHashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            String kitName = parts[i];
            String weightStr = parts[i + 1];
            if (!weightStr.endsWith("%")) {
                source.method_9213(class_2561.method_43470("Invalid weight '" + weightStr + "' for kit '" + kitName + "'. Use format: <weight>%"));
                return 0;
            }
            if (!BotKits.kitExists(kitName)) {
                source.method_9213(class_2561.method_43470("Kit '" + kitName + "' not found!"));
                return 0;
            }
            try {
                int w = Integer.parseInt(weightStr.substring(0, weightStr.length() - 1));
                if (w <= 0) {
                    source.method_9213(class_2561.method_43470("Weight must be positive for kit '" + kitName + "'"));
                    return 0;
                }
                weights.put(kitName, w);
            } catch (NumberFormatException e) {
                source.method_9213(class_2561.method_43470("Invalid weight '" + weightStr + "' for kit '" + kitName + "'"));
                return 0;
            }
        }
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        var members = BotFaction.getMembers(faction);
        if (members.isEmpty()) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' has no members!"));
            return 0;
        }
        var server = source.method_9211();
        int count = 0;
        java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();
        for (String memberName : members) {
            if (!BotManager.getAllBots().contains(memberName)) continue;
            var bot = BotManager.getBot(server, memberName);
            if (bot == null) continue;
            int roll = rng.nextInt(totalWeight);
            int cumulative = 0;
            String selected = null;
            for (var entry : weights.entrySet()) {
                cumulative += entry.getValue();
                if (roll < cumulative) {
                    selected = entry.getKey();
                    break;
                }
            }
            if (selected != null && BotKits.giveKit(selected, bot)) {
                count++;
            }
        }
        if (count > 0) {
            int finalCount = count;
            source.method_9226(() -> class_2561.method_43470("Gave random kits to " + finalCount + " bots in faction '" + faction + "'"), true);
            return count;
        } else {
            source.method_9213(class_2561.method_43470("No bots in faction '" + faction + "' received a kit"));
            return 0;
        }
    }

    // ========== FACTION TP ==========

    private static int factionTp(CommandContext<class_2168> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' not found!"));
            return 0;
        }
        String location = StringArgumentType.getString(ctx, "location");
        String[] parts = location.split(" ");
        double tx, ty, tz;
        if (parts.length == 3) {
            var player = source.method_44023();
            double ox = (player != null) ? player.method_23317() : 0;
            double oy = (player != null) ? player.method_23318() : 0;
            double oz = (player != null) ? player.method_23321() : 0;
            try {
                tx = parseCoord(parts[0], ox);
                ty = parseCoord(parts[1], oy);
                tz = parseCoord(parts[2], oz);
            } catch (NumberFormatException e) {
                source.method_9213(class_2561.method_43470("Invalid coordinates. Usage: /pvpbot faction tp <faction> <x y z> or <playername>"));
                return 0;
            }
        } else {
            var server = source.method_9211();
            var target = server.method_3760().method_14566(location);
            if (target == null) {
                source.method_9213(class_2561.method_43470("Player or bot '" + location + "' not found!"));
                return 0;
            }
            tx = target.method_23317();
            ty = target.method_23318();
            tz = target.method_23321();
        }
        var members = new java.util.ArrayList<>(BotFaction.getMembers(faction));
        members.removeIf(m -> !BotManager.getAllBots().contains(m));
        if (members.isEmpty()) {
            source.method_9213(class_2561.method_43470("Faction '" + faction + "' has no bots!"));
            return 0;
        }
        source.method_9226(() -> class_2561.method_43470("Teleporting " + members.size() + " bots in faction '" + faction + "'..."), false);
        var server = source.method_9211();
        boolean safe = BotSettings.get().isSafeSpawn();
        int[] count = {0};
        scheduleFactionTp(server, source, members, tx, ty, tz, safe, 0, count);
        return 1;
    }

    private static double parseCoord(String s, double origin) {
        if (s.startsWith("~")) {
            double offset = s.length() > 1 ? Double.parseDouble(s.substring(1)) : 0;
            return origin + offset;
        }
        return Double.parseDouble(s);
    }

    private static final java.util.concurrent.ScheduledExecutorService TP_SCHEDULER =
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FactionTP");
            t.setDaemon(true);
            return t;
        });

    private static void scheduleFactionTp(MinecraftServer server, class_2168 source,
                                          java.util.List<String> members, double tx, double ty, double tz,
                                          boolean safe, int index, int[] count) {
        if (index >= members.size()) {
            source.method_9226(() -> class_2561.method_43470("Teleported " + count[0] + " bots in faction"), true);
            return;
        }
        int end = Math.min(index + 5, members.size());
        TP_SCHEDULER.schedule(() -> server.execute(() -> {
            var rng = java.util.concurrent.ThreadLocalRandom.current();
            for (int i = index; i < end; i++) {
                String name = members.get(i);
                double x = tx, z = tz;
                if (safe) {
                    x += (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
                    z += (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
                }
                class_3222 bot = BotManager.getBot(server, name);
                if (bot != null) {
                    bot.method_48105((net.minecraft.class_3218) bot.method_51469(), x, ty, tz, java.util.Set.of(), 0.0f, 0.0f, false);
                    count[0]++;
                }
            }
            scheduleFactionTp(server, source, members, tx, ty, tz, safe, end, count);
        }), 100, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}


