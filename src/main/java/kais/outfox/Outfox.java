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

import kais.outfox.compat.CompatHandler;
import kais.outfox.fox.EntityFox;
import kais.outfox.proxy.ServerProxy;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@Mod.EventBusSubscriber
@Mod(
    modid = OutfoxResources.MODID,
    name = OutfoxResources.NAME,
    version = OutfoxResources.VERSION,
    dependencies = "required-after:forge@[14.23.3.2678,);"
        + "after:theoneprobe@[1.4.28,)",
    acceptedMinecraftVersions = "[1.12.2,]"
)

public class Outfox {

    @Mod.Instance(value = OutfoxResources.MODID)
    public static Outfox instance;

    @SidedProxy(clientSide = "kais.outfox.proxy.ClientProxy", serverSide = "kais.outfox.proxy.ServerProxy")
    public static ServerProxy proxy;

    @ObjectHolder(OutfoxResources.MODID)
    public static class Entities {

        public static final EntityEntry FOX = null;
    }

    @SubscribeEvent
    public static void RegisterEntities(RegistryEvent.Register<EntityEntry> event) {

        event.getRegistry().register(EntityEntryBuilder.create()
            .entity(EntityFox.class)
            .id(new ResourceLocation(OutfoxResources.MODID, "fox"), 0)
            .name(OutfoxResources.MODID + ".fox")
            .spawn(EnumCreatureType.CREATURE, 11, 2, 5, OutfoxResources.mergeBiomes(OutfoxConfig.biomes.common_biomes, OutfoxConfig.biomes.common_types))
            .spawn(EnumCreatureType.CREATURE, 4, 2, 3, OutfoxResources.mergeBiomes(OutfoxConfig.biomes.rare_biomes, OutfoxConfig.biomes.rare_types))
            .egg(0xFF9F2B, 0x404040)
            .tracker(64, 4, false)
            .build());
    }

    @SubscribeEvent
    public static void RegisterSounds(RegistryEvent.Register<SoundEvent> event) {

        for (SoundEvent sound : OutfoxResources.FOX_SND_SET) { event.getRegistry().register(sound); }
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {

        if (ForgeVersion.buildVersion < 2678) { OutfoxResources.logError("Unsupported Forge version! (expected > 14.23.3.2678) This is highly likely to break things, the dev will not fix any problems caused by this"); } // "shouldn't" is a really interesting concept, isn't it?

        OutfoxConfig.sync();
        proxy.registerRender();

        if (OutfoxConfig.biomes.common_biomes.length == 0 && OutfoxConfig.biomes.common_types.length == 0
            && OutfoxConfig.biomes.rare_biomes.length == 0 && OutfoxConfig.biomes.rare_types.length == 0) {

            OutfoxResources.logWarn("Fox has no configured spawn biomes, I hope you know what you're doing");
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        CompatHandler.register();
    }
}