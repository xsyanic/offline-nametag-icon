package com.syanicxd.offlinenametagicon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @ModifyExpressionValue(
        method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"
        )
    )
    private Entity obfuscateCameraEntity(Entity original) {
        return null;
    }
}
