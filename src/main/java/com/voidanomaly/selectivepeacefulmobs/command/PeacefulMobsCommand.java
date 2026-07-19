package com.voidanomaly.selectivepeacefulmobs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.voidanomaly.selectivepeacefulmobs.config.SpmConfig;
import com.voidanomaly.selectivepeacefulmobs.state.PlayerPeacefulState;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

public class PeacefulMobsCommand {
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("peacefulmobs")
                .then(Commands.literal("on")
                        .executes(ctx -> setSelf(ctx, true)))
                .then(Commands.literal("off")
                        .executes(ctx -> setSelf(ctx, false)))
                .then(Commands.literal("status")
                        .executes(this::statusSelf))
                .then(Commands.literal("reset")
                        .executes(this::resetSelf)
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(this::resetOther)))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(this::setOther))))
                .then(Commands.literal("check")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(this::statusOther)))
        );
    }

    private int setSelf(CommandContext<CommandSourceStack> ctx, boolean enabled) throws CommandSyntaxException {
        if (!requirePlayerChoicesEnabled(ctx.getSource())) {
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PlayerPeacefulState.get(player).setPeaceful(player, enabled);
        sendStatus(ctx.getSource(), player, enabled, true);
        return 1;
    }

    private int setOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!requirePlayerChoicesEnabled(ctx.getSource())) {
            return 0;
        }
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        PlayerPeacefulState.get(player).setPeaceful(player, enabled);
        sendStatus(ctx.getSource(), player, enabled, false);
        player.sendSystemMessage(Component.literal("Your peaceful mobs setting was changed by an admin: " + onOff(enabled)).withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private int statusSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        sendDetailedStatus(ctx.getSource(), player, true);
        return 1;
    }

    private int statusOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        sendDetailedStatus(ctx.getSource(), player, false);
        return 1;
    }

    private int resetSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PlayerPeacefulState.get(player).clearChoice(player);
        ctx.getSource().sendSuccess(() -> Component.literal("Your peaceful mobs setting was reset to server default: " + onOff(SpmConfig.DEFAULT_PEACEFUL.get())).withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private int resetOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        PlayerPeacefulState.get(player).clearChoice(player);
        ctx.getSource().sendSuccess(() -> Component.literal("Reset " + player.getGameProfile().getName() + " to server default: " + onOff(SpmConfig.DEFAULT_PEACEFUL.get())).withStyle(ChatFormatting.YELLOW), true);
        player.sendSystemMessage(Component.literal("Your peaceful mobs setting was reset to the server default: " + onOff(SpmConfig.DEFAULT_PEACEFUL.get())).withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private void sendStatus(CommandSourceStack source, ServerPlayer player, boolean enabled, boolean self) {
        String target = self ? "Your" : player.getGameProfile().getName() + "'s";
        source.sendSuccess(() -> Component.literal(target + " peaceful mobs setting is now " + onOff(enabled) + ".").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), !self);
    }

    private void sendDetailedStatus(CommandSourceStack source, ServerPlayer player, boolean self) {
        PlayerPeacefulState state = PlayerPeacefulState.get(player);
        boolean enabled = state.isPeaceful(player);
        Optional<Boolean> explicit = state.getExplicitChoice(player);
        String target = self ? "Your" : player.getGameProfile().getName() + "'s";
        String choice = SpmConfig.REMEMBER_PLAYER_CHOICE.get()
                ? explicit.map(value -> "player choice").orElse("server default")
                : "server default; player choices are disabled";

        source.sendSuccess(() -> Component.literal(target + " peaceful mobs setting is " + onOff(enabled) + " (" + choice + ").").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
    }

    private boolean requirePlayerChoicesEnabled(CommandSourceStack source) {
        if (SpmConfig.REMEMBER_PLAYER_CHOICE.get()) {
            return true;
        }

        source.sendFailure(Component.literal(
                "Player choices are disabled by the server; the default is " + onOff(SpmConfig.DEFAULT_PEACEFUL.get()) + "."
        ).withStyle(ChatFormatting.RED));
        return false;
    }

    private String onOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }
}
