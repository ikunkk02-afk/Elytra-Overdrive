package io.github.ikunkk02.elytraoverdrive.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.ikunkk02.elytraoverdrive.ElytraOverdrive;
import io.github.ikunkk02.elytraoverdrive.flight.OverdriveFlightHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class OverdriveCommands {
	private OverdriveCommands() {
	}

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("elytraoverdrive")
						.then(Commands.literal("firework_mode")
								.then(Commands.argument("enabled", BoolArgumentType.bool())
										.executes(context -> setFireworkMode(
												context.getSource(),
												BoolArgumentType.getBool(context, "enabled")
										)))))
		);
	}

	private static int setFireworkMode(CommandSourceStack source, boolean enabled) {
		if (!source.hasPermission(4)) {
			source.sendFailure(Component.translatable("command.elytra_overdrive.no_permission"));
			return 0;
		}

		ElytraOverdrive.CONFIG.allowHeldFireworkOverdrive(enabled);
		ElytraOverdrive.CONFIG.save();
		OverdriveFlightHandler.refreshPolicyForAll(source.getServer());
		String resultKey = enabled
				? "command.elytra_overdrive.firework_mode.enabled"
				: "command.elytra_overdrive.firework_mode.disabled";
		source.sendSuccess(() -> Component.translatable(resultKey), true);
		return Command.SINGLE_SUCCESS;
	}
}
