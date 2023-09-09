package com.seosean.teammatesoutline.mixins;


import com.seosean.teammatesoutline.TeammatesOutline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Shadow
    private WorldClient theWorld;
    @Final
    @Shadow
    private RenderManager renderManager;
    @Final
    @Shadow
    private Minecraft mc;
    @Shadow
    private Framebuffer entityOutlineFramebuffer;
    @Shadow
    private ShaderGroup entityOutlineShader;

    @Shadow
    protected abstract boolean isRenderEntityOutlines();

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z", ordinal = 0))
    private boolean onRenderEntities(RenderGlobal renderGlobal) {
        return false;
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 2, args = {"ldc=entities"}), locals = LocalCapture.CAPTURE_FAILSOFT)
    // Optifine version
    private void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci, int pass, double d0, double d1, double d2, Entity entity, double d3, double d4, double d5, List<Entity> list, boolean bool0, boolean bool1) {
        displayOutlines(list, d0, d1, d2, camera, partialTicks);
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 2, args = {"ldc=entities"}), locals = LocalCapture.CAPTURE_FAILSOFT)
    // Non-optifine version
    private void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci, int pass, double d0, double d1, double d2, Entity entity, double d3, double d4, double d5, List<Entity> list) {
        displayOutlines(list, d0, d1, d2, camera, partialTicks);
    }

    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z", ordinal = 0))
    private boolean isSpectatorDisableCheck(EntityPlayerSP entityPlayerSP) {
        return true;
    }

    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z", ordinal = 0))
    private boolean isKeyDownDisableCheck(KeyBinding keyBinding) {
        return true;
    }

    private void displayOutlines(List<Entity> entities, double x, double y, double z, ICamera camera, float partialTicks) {
        if (isRenderEntityOutlines()) {
            GlStateManager.depthFunc(519);
            GlStateManager.disableFog();
            this.entityOutlineFramebuffer.framebufferClear();
            this.entityOutlineFramebuffer.bindFramebuffer(false);
            this.theWorld.theProfiler.endStartSection("entityOutlines");
            RenderHelper.disableStandardItemLighting();
            this.renderManager.setRenderOutlines(true);
            entities.stream()
                    .filter(entity -> entity instanceof EntityPlayer)
                    .map(entity -> (EntityPlayer) entity)
                    .collect(Collectors.toList());
            for (Entity entity : entities) {
                if(entity instanceof EntityPlayer) {
                    boolean entityABitFar = entity.getDistance(Minecraft.getMinecraft().thePlayer.getPosition().getX(), Minecraft.getMinecraft().thePlayer.getPosition().getY(), Minecraft.getMinecraft().thePlayer.getPosition().getZ()) > 2;
                    boolean flag = this.mc.getRenderViewEntity() instanceof EntityLivingBase;
                    boolean flag1 = entity.isInRangeToRender3d(x, y, z) && (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox())); //&& entity instanceof EntityPlayer
                    if (TeammatesOutline.getInstance().teammatesOutline && flag1 && entityABitFar && (entity != this.mc.getRenderViewEntity() || flag) && TeammatesOutline.getInstance().isInZombies()) { //LobbyGlow.INSTANCE.getUtils().shouldGlow(entity)
                        this.renderManager.renderEntitySimple(entity, partialTicks);

                    }
                }
            }

            this.renderManager.setRenderOutlines(false);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.depthMask(false);
            this.entityOutlineShader.loadShaderGroup(partialTicks);
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            this.mc.getFramebuffer().bindFramebuffer(false);
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.depthFunc(515);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
        }
    }
}