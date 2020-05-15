/**
 * Copyright © 2018 Aiden Vaughn "ItsTheKais"
 *
 * This file is part of Outfox.
 *
 * The code of Outfox is free and available under the terms of the latest version of the GNU Lesser General
 * Public License. Outfox is distributed with no warranty, implied or otherwise. Outfox should have come with
 * a copy of the GNU Lesser General Public License; if not, see: <https://www.gnu.org/licenses/>
 */

package kais.outfox.fox;

import kais.outfox.OutfoxTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerFoxRibbon implements LayerRenderer<EntityFox> {

    private final RenderFox foxRenderer;

    public LayerFoxRibbon(RenderFox foxRendererIn) {

        this.foxRenderer = foxRendererIn;
    }

    public void doRenderLayer(EntityFox fox, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (fox.isTamed() && !fox.isInvisible()) {

            this.foxRenderer.bindTexture(OutfoxTextures.FOX_RIBBON_TEX);
            float[] rcolor = fox.getRibbonColor().getColorComponentValues();
            GlStateManager.color(rcolor[0], rcolor[1], rcolor[2]);
            this.foxRenderer.getMainModel().render(fox, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    public boolean shouldCombineTextures() { return true; }
}