package com.emi.chestTest.mixin;

import com.emi.chestTest.api.IChestBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements IChestBlockEntity {
    @Unique
    private boolean used = false;

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("used", this.used);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("used")) {
            this.used = nbt.getBoolean("used");
        }
    }

    @Override
    public boolean isUsed() {
        return this.used;
    }

    @Override
    public void setUsed(boolean value) {
        this.used = value;
    }
}