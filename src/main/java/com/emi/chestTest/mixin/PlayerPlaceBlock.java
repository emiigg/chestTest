package com.emi.chestTest.mixin;

import com.emi.chestTest.api.IChestBlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class PlayerPlaceBlock {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"))
    private void markChestUsed(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();

        if (world.isClient()) {
            return;
        }

        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return;
        }

        if (!player.isCreative() && context.getStack().getItem() == Blocks.CHEST.asItem()) {
            world.getServer().submit(() -> {
                if (world.getBlockEntity(context.getBlockPos()) instanceof IChestBlockEntity chestMixin) {
                    chestMixin.setUsed(true);
                    System.out.println("El estado del cofre se ha establecido como 'usado'.");
                } else {
                    System.out.println("La entidad de bloque no está inicializada aún.");
                }
            });
        }

    }
}