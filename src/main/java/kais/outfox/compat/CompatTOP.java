/**
 * Copyright © 2019 Aiden Vaughn "ItsTheKais"
 *
 * This file is part of Outfox.
 *
 * The code of Outfox is free and available under the terms of the latest version of the GNU Lesser General
 * Public License. Outfox is distributed with no warranty, implied or otherwise. Outfox should have come with
 * a copy of the GNU Lesser General Public License; if not, see: <https://www.gnu.org/licenses/>
 */

package kais.outfox.compat;

import com.google.common.base.Function;

import javax.annotation.Nullable;

import kais.outfox.OutfoxConfig;
import kais.outfox.OutfoxResources;
import kais.outfox.fox.EntityFox;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CompatTOP implements Function<ITheOneProbe, Void> {

    @Nullable
    @Override
    public Void apply(ITheOneProbe probe) {

        probe.registerEntityProvider(new TOPProviderFox());
        return null;
    }

    public class TOPProviderFox implements IProbeInfoEntityProvider {

        @Override
        public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, Entity entity, IProbeHitEntityData data) {

            if (entity instanceof EntityFox) {

                EntityFox fox = (EntityFox)entity;
                if (OutfoxConfig.search.search_enabled && fox.getSearchedBlock() != null && !fox.isSitting()) {

                    probeInfo.text(TextFormatting.GRAY + "Sniffing for "
                        + TextFormatting.YELLOW + fox.getSearchedBlock().getLocalizedName()
                        + TextFormatting.GRAY + "...");

                    if (mode == ProbeMode.DEBUG) {

                        int[] coords = fox.getTargetBlock();
                        if (coords != null) {

                            probeInfo.horizontal(probeInfo.defaultLayoutStyle().borderColor(0xffff4444)).text(
                                TextFormatting.GRAY + "Target XYZ: ("
                                + TextFormatting.DARK_RED + coords[0] + TextFormatting.GRAY + ", "
                                + TextFormatting.DARK_GREEN + coords[1] + TextFormatting.GRAY + ", "
                                + TextFormatting.BLUE + coords[2] + TextFormatting.GRAY + ")"
                            );
                        }
                    }
                }

                if (OutfoxConfig.stealing.stealing_enabled && fox.hasStolenItem()) {

                    ItemStack item = fox.getActiveItemStack();
                    probeInfo.horizontal().item(item).text(TextFormatting.GRAY + " " + item.getDisplayName());
                }
            }
        }

        @Override
        public String getID() {

            return OutfoxResources.MODID + ".entityfox";
        }
    }
}