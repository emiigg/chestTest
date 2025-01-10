package com.emi.chestTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ChestTest implements ModInitializer {
    public static final String MOD_ID = "chest-test";
    private static final Path CONFIG_PATH = Path.of("config/chestloot.json");
    public static final Map<String, List<String>> BIOME_TO_LOOT_TABLE = new HashMap<>();


    @Override
    public void onInitialize() {
        loadConfig();
        // Registrar el BlockEntityType


        /// Registrar el comando /chestloot reload
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

    private static ActionResult handleChestOpen(World world, BlockPos pos, ServerPlayerEntity player) {
        // Obtener el bioma en el que se encuentra el cofre
        String biome = world.getRegistryManager().get(RegistryKeys.BIOME).getId(world.getBiome(pos).value()).toString();

        // Mostrar el bioma y el jugador en la consola para depuración
        System.out.println("El jugador " + player.getName().getString() + " abrió un cofre en el bioma: " + biome);

        // Verificar si el bloque es un cofre
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof ChestBlockEntity chest) {
            // Obtener el NBT actual del cofre
            NbtCompound chestNbt = chest.createNbt();

            // Verificar si el cofre ya fue usado
            if (chestNbt.getBoolean("used")) {
                System.out.println("El cofre ya fue usado.");
                return ActionResult.PASS; // No hacemos nada si ya fue usado
            }

            // Obtener loot tables configuradas para el bioma
            List<String> lootTables = BIOME_TO_LOOT_TABLE.getOrDefault(biome, BIOME_TO_LOOT_TABLE.get("default"));

            if (!lootTables.isEmpty()) {
                Random random = new Random();
                String selectedTable = lootTables.get(random.nextInt(lootTables.size()));
                Identifier lootTableId = new Identifier(selectedTable);

                // Generar loot usando la loot table
                //chest.setLootTable(lootTableId, random.nextLong());
                System.out.println("Se genera loot");

                // Marcar el cofre como usado en el NBT
                chestNbt.putBoolean("used", true);

                //chest.writeNbt(chestNbt);
                chest.readNbt(chestNbt);
                chest.markDirty(); // Registrar los cambios para sincronizar el estado del cofre

                world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);

            } else {
                System.out.println("No hay loot tables definidas para el bioma: " + biome);
            }
        }else {
            System.out.println("No se entro");
        }
        return ActionResult.PASS;
    }
}


