/**
 * Copyright © 2020 Aiden Vaughn "ItsTheKais"
 *
 * This file is part of Outfox.
 *
 * The code of Outfox is free and available under the terms of the latest version of the GNU Lesser General
 * Public License. Outfox is distributed with no warranty, implied or otherwise. Outfox should have come with
 * a copy of the GNU Lesser General Public License; if not, see: <https://www.gnu.org/licenses/>
 */

package kais.outfox.fox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerFoxItem implements LayerRenderer<EntityFox> {

    //private final RenderFox foxRenderer;

    public LayerFoxItem(RenderFox foxRendererIn) {

        //this.foxRenderer = foxRendererIn;
    }

    public void doRenderLayer(EntityFox fox, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        ItemStack item = fox.getStolenItem();
        if (!item.isEmpty()) {

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, 0.0F, -1.78125F);
            GlStateManager.translate(0.0F, 0.4375F, 0.0F);

            if (item.getItem() instanceof ItemBlock) {

                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                GlStateManager.translate(0.0F, 0.0F, -0.5F);
                GlStateManager.translate(0.0F, 0.5F, 0.0F);
            }

            GlStateManager.rotate(-netHeadYaw, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-headPitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, 1.0625F, 0.0F);
            Minecraft.getMinecraft().getItemRenderer().renderItem(fox, item, ItemCameraTransforms.TransformType.NONE);
            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures() { return false; }
}
