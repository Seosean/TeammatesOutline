package com.seosean.teammatesoutline.mixins;

import com.seosean.teammatesoutline.TeammatesOutline;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRenderManager<T extends EntityLivingBase> {
    @Inject(method = "setScoreTeamColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.AFTER))
    private void setScoreTeamColor(T entityLivingBaseIn, CallbackInfoReturnable<Point> cir){
        if(TeammatesOutline.getInstance().teammatesOutline && TeammatesOutline.getInstance().isInZombies()) {
            if((entityLivingBaseIn).isPlayerSleeping()) {
                GlStateManager.color(1F, 0F, 0F, 1.0F);
            }else{
                GlStateManager.color(0F, 1F, 0F, 1.0F);
            }
        }
    }
}
