/**
 * Copyright © 2018 Aiden Vaughn "ItsTheKais"
 *
 * This file is part of Outfox.
 *
 * The code of Outfox is free and available under the terms of the latest version of the GNU Lesser General
 * Public License. Outfox is distributed with no warranty, implied or otherwise. Outfox should have come with
 * a copy of the GNU Lesser General Public License; if not, see: <https://www.gnu.org/licenses/>
 */

package kais.outfox;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OutfoxTextures {

    public static final ResourceLocation FOX_CROSS_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_cross.png");
    public static final ResourceLocation FOX_MARBLE_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_marble.png");
    public static final ResourceLocation FOX_PALE_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_pale.png");
    public static final ResourceLocation FOX_STRANGE_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_strange.png");
    public static final ResourceLocation FOX_RED_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_red.png");
    public static final ResourceLocation FOX_RIBBON_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_ribbon.png");
    public static final ResourceLocation FOX_RUSTY_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_rusty.png");
    public static final ResourceLocation FOX_SILVER_TEX = new ResourceLocation(OutfoxResources.MODID, "textures/fox/fox_silver.png");

    public static final ResourceLocation[] FOX_TEX_SET = new ResourceLocation[] {FOX_CROSS_TEX, FOX_MARBLE_TEX, FOX_PALE_TEX, FOX_RED_TEX, FOX_RUSTY_TEX, FOX_SILVER_TEX};
}