package com.emi.chestTest.events;

import com.emi.chestTest.api.IChestBlockEntity;
import com.emi.chestTest.config.BiomeLootConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChestOpenHandler {
    public static void registerEvents() {
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

    private static void handleChestOpen(World world, BlockPos pos, ServerPlayerEntity player) {
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity chest && blockEntity instanceof IChestBlockEntity chestMixin) {
            if (!chestMixin.isUsed()) {
                String biome = world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.BIOME).getId(world.getBiome(pos).value()).toString();
                Map<String, List<String>> biomeToLootTable = BiomeLootConfig.getBiomeToLootTable();

                List<String> lootTables = biomeToLootTable.getOrDefault(biome, biomeToLootTable.get("default"));
                if (!lootTables.isEmpty()) {
                    Random random = new Random();
                    String selectedTable = lootTables.get(random.nextInt(lootTables.size()));
                    Identifier lootTableId = new Identifier(selectedTable);
                    chest.setLootTable(lootTableId, random.nextLong());
                } else {
                    System.out.println("No hay loot tables definidas para el bioma: " + biome);
                }

                chestMixin.setUsed(true);
            } else {
                System.out.println("Este cofre ya ha sido usado.");
            }
        }
    }
}