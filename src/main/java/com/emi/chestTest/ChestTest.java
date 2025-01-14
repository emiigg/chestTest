package com.emi.chestTest;

import com.emi.chestTest.commands.ReloadLootCommand;
import com.emi.chestTest.config.BiomeLootConfig;
import com.emi.chestTest.events.ChestOpenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.nio.file.Path;

public class ChestTest implements ModInitializer {
    public static final String MOD_ID = "chest-test";

    @Override
    public void onInitialize() {
        BiomeLootConfig.loadConfig();
        ChestOpenHandler.registerEvents();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ReloadLootCommand.register(dispatcher));
    }
}