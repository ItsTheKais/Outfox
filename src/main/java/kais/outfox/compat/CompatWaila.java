/**
 * Copyright © 2018 Aiden Vaughn "ItsTheKais"
 *
 * This file is part of Outfox.
 *
 * The code of Outfox is free and available under the terms of the latest version of the GNU Lesser General
 * Public License. Outfox is distributed with no warranty, implied or otherwise. Outfox should have come with
 * a copy of the GNU Lesser General Public License; if not, see: <https://www.gnu.org/licenses/>
 */

package kais.outfox.compat;

import java.util.List;
import kais.outfox.fox.EntityFox;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;

public class CompatWaila implements IWailaEntityProvider {

    public static void register(IWailaRegistrar registrar) {

        registrar.registerBodyProvider(new CompatWaila(), EntityFox.class);
    }

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {

        if (entity instanceof EntityFox) {

            EntityFox fox = (EntityFox)entity;
            if (fox.getSearchedBlock() != null && !fox.isSitting()) {

                currenttip.add(TextFormatting.GRAY + "Sniffing for "
                    + TextFormatting.YELLOW + fox.getSearchedBlock().getLocalizedName()
                    + TextFormatting.GRAY + "..."
                );
            }
        }

        return currenttip;
    }
}