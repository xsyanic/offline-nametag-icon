package com.syanicxd.offlinenametagicon.mixin;

import net.minecraft.client.render.OverlayTexture;
import com.syanicxd.offlinenametagicon.offlinenametagiconMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.joml.Matrix4f;

@Mixin(PlayerEntityRenderer.class)
public abstract class EntityRendererMixin {

    private static final Identifier BADGE_TEXTURE = Identifier.of(offlinenametagiconMod.MOD_ID, "textures/badge.png");
    private static final Identifier WHITE_TEXTURE = Identifier.ofVanilla("textures/misc/white.png");

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void renderBadge(PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        // only render for current player, you can simply modifiy this to render for other players if you want to
        if (client.player == null || !isCurrentPlayer(state, client)) return;
        
        // you can ignore all the logic below, I looked at essentials mod code and copied their logic for rendering the nametag
        Text text = state.displayName != null ? state.displayName : state.playerName;
        if (text == null) return;

        int textWidth = client.textRenderer.getWidth(text); 
        float badgeSize = 10.0F; 
        float yOffset = state.height + 0.5F; 

        matrices.push();
        matrices.translate(0.0, yOffset, 0.0);
        matrices.multiply(client.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);

        float badgeX = textWidth / 2.0F + 1.0F;
        float badgeY = -badgeSize / 10.0F;

        renderBadgeIcon(matrices, queue, client, state.light, badgeX, badgeY, badgeSize);

        matrices.pop();
    }

    private void renderBadgeIcon(MatrixStack matrices, OrderedRenderCommandQueue queue, MinecraftClient client, int light, float x, float y, float size) {
        int overlay = OverlayTexture.DEFAULT_UV;
        float padding = 1.5F; // background bleed around the icon, in local quad units
        int nametagBackgroundColor = getNameTagBackgroundColor(client);

        // Background quad
        renderQuad(
            matrices, queue,
            RenderLayers.entityTranslucent(WHITE_TEXTURE),
            light, overlay,
            x, y, size, -0.02F,
            nametagBackgroundColor
        );

        // Nametag icon itself
        renderQuad(
            matrices, queue,
            RenderLayers.entityTranslucent(BADGE_TEXTURE),
            light, overlay,
            x, y, size, 0.0F,
            0xFFFFFFFF
        );
    }

    private void renderQuad(MatrixStack matrices, OrderedRenderCommandQueue queue, RenderLayer layer,
                             int light, int overlay, float x, float y, float size, float z, int argbColor) {
        queue.submitCustom(matrices, layer, (entry, buffer) -> {
            Matrix4f matrix = entry.getPositionMatrix();

            buffer.vertex(matrix, x, y + size, z)
                .color(argbColor).texture(1, 1).overlay(overlay).light(light).normal(0, 0, 1);
            buffer.vertex(matrix, x + size, y + size, z)
                .color(argbColor).texture(0, 1).overlay(overlay).light(light).normal(0, 0, 1);
            buffer.vertex(matrix, x + size, y, z)
                .color(argbColor).texture(0, 0).overlay(overlay).light(light).normal(0, 0, 1);
            buffer.vertex(matrix, x, y, z)
                .color(argbColor).texture(1, 0).overlay(overlay).light(light).normal(0, 0, 1);
        });
    }

    private boolean isCurrentPlayer(PlayerEntityRenderState playerState, MinecraftClient client) {
        if (client.player == null) return false;

        if (playerState.id == client.player.getId()) return true;

        var cameraEntity = client.getCameraEntity();
        return cameraEntity != null && playerState.id == cameraEntity.getId();
    }

    private int getNameTagBackgroundColor(MinecraftClient client) {
        float opacity = client.options.getTextBackgroundOpacity(0.25F);
        int alpha = (int) (opacity * 255.0F) & 0xFF;
        return (alpha << 24); // black RGB, matched alpha
    }
}