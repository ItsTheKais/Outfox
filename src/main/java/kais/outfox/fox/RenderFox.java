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
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFox extends RenderLiving<EntityFox> {

    public RenderFox(RenderManager manager) {

        super(manager, new ModelFox(), 0.5F);
        this.addLayer(new LayerFoxRibbon(this));
        this.addLayer(new LayerFoxItem(this));
    }

    public void doRender(EntityFox fox, double x, double y, double z, float entityYaw, float partialTicks) {

        if (fox.isFoxWet()) {

            float f = fox.getBrightness() * fox.getShadingWhileWet(partialTicks);
            GlStateManager.color(f, f, f);
        }
        super.doRender(fox, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityFox fox) {

        if (fox.hasCustomName() && fox.getCustomNameTag().equals("Kais")) {
            return OutfoxTextures.FOX_STRANGE_TEX;
        } else {
            return OutfoxTextures.FOX_TEX_SET[fox.getVariant()];
        }
    }
}