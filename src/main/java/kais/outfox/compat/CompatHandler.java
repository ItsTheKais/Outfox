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

import kais.outfox.OutfoxConfig;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class CompatHandler {

    public static void register() {

        if (OutfoxConfig.compat.compat_theoneprobe && Loader.isModLoaded("theoneprobe")) {
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "kais.outfox.compat.CompatTOP");
        }
        if (OutfoxConfig.compat.compat_waila && Loader.isModLoaded("waila")) {
            FMLInterModComms.sendMessage("waila", "register", "kais.outfox.compat.CompatWaila.register");
        }
    }
}