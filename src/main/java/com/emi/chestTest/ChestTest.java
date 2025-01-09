package com.emi.chestTest;

import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ChestTest implements ModInitializer {
    public static final String MOD_ID = "chest-test";
    private static final Path CONFIG_PATH = Path.of("config/chestloot.json");
    private static final Map<String, List<String>> BIOME_TO_LOOT_TABLE = new HashMap<>();

    @Override
    public void onInitialize() {
        loadConfig();

        // Registrar el comando /chestloot reload
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("chestloot")
                    .then(CommandManager.literal("reload")
                            .executes(context -> {
                                loadConfig();
                                context.getSource().sendFeedback(Text.of("ChestLoot config reloaded!"), false);
                                return 1;
                            })
                    )
            );
        });

        // Hook para detectar cuando se interactúa con un cofre
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient()) {
                BlockPos pos = hitResult.getBlockPos();
                if (world.getBlockState(pos).getBlock() == Blocks.CHEST) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        handleChestOpen(world, pos, serverPlayer);
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private static void loadConfig() {
        try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            BIOME_TO_LOOT_TABLE.clear();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                List<String> lootTables = new ArrayList<>();
                entry.getValue().getAsJsonArray().forEach(element -> lootTables.add(element.getAsString()));
                BIOME_TO_LOOT_TABLE.put(entry.getKey(), lootTables);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleChestOpen(World world, BlockPos pos, ServerPlayerEntity player) {
        // Obtener el identificador del bioma en el que se encuentra el jugador
        String biome = world.getRegistryManager().get(RegistryKeys.BIOME).getId(world.getBiome(pos).value()).toString();

        // Obtener el nombre del jugador
        String playerName = player.getName().getString();

        // Mostrar el bioma y el nombre del jugador en la consola
        System.out.println("El jugador " + playerName + " abrió un cofre en el bioma: " + biome);
    }
}