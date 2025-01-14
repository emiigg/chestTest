package com.emi.chestTest.commands;

import com.emi.chestTest.config.BiomeLootConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.CommandDispatcher;

public class ReloadLootCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chestloot")
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            BiomeLootConfig.loadConfig();
                            context.getSource().sendFeedback(Text.of("ChestLoot cargado exitosamente!"), false);
                            return 1;
                        })
                )
        );
    }
}