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

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * style note: the individual lines of multiline comments should be no longer than 100 characters long;
 * including indentation & quotes, that's 115 characters total, 116 with a comma;
 * this makes the config file look a bit nicer because the comments won't be longer than the category headers;
 * never let anyone tell you the little things don't count!
 */
@Mod.EventBusSubscriber(modid = OutfoxResources.MODID)
@Config(modid = OutfoxResources.MODID, category = "")
public class OutfoxConfig {

    @Config.Comment("Fox spawn biome configuration")
    @Config.LangKey("outfox.config.category_biomes")
    public static final Biomes biomes = new Biomes();

    @Config.Comment("Cross-mod compatibility settings")
    @Config.LangKey("outfox.config.category_compat")
    public static final Compat compat = new Compat();

    @Config.Comment("Miscellaneous config settings")
    @Config.LangKey("outfox.config.category_general")
    public static final General general = new General();

    @Config.Comment("Block searching AI configuration")
    @Config.LangKey("outfox.config.category_search")
    public static final Search search = new Search();

    //@Config.Comment("Item stealing AI configuration")
    //@Config.LangKey("outfox.config.category_stealing")
    //public static final Stealing stealing = new Stealing();

    public static class Biomes {

        @Config.Comment({
            "List of biomes that foxes should be common spawns in.",
            "This setting requires a Minecraft restart if changed from the in-game config menu!",
            "Default:",
            "  minecraft:roofed_forest",
            "  minecraft:mutated_roofed_forest"
        })
        @Config.LangKey("outfox.config.common_biomes")
        @Config.RequiresMcRestart
        public String[] common_biomes = OutfoxResources.DEFAULT_COMMON_BIOMES;

        @Config.Comment({
            "List of biome dictionary types that foxes should be common spawns in.",
            "This is for modpacks with large numbers of biomes to lazily add foxes to many biomes at once.",
            "For a list of valid biome types, check the Forge biome dictionary source at:",
            "https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/common/BiomeDictionary.java", // except for this line :/
            "This setting requires a Minecraft restart if changed from the in-game config menu!",
            "Default: (empty)"
        })
        @Config.LangKey("outfox.config.common_types")
        @Config.RequiresMcRestart
        public String[] common_types = {};

        @Config.Comment({
            "List of biomes that foxes should be rare spawns in.",
            "This setting requires a Minecraft restart if changed from the in-game config menu!",
            "Default:",
            "  minecraft:forest",
            "  minecraft:forest_hills",
            "  minecraft:birch_forest",
            "  minecraft:birch_forest_hills",
            "  minecraft:mutated_forest",
            "  minecraft:mutated_birch_forest",
            "  minecraft:mutated_birch_forest_hills"
        })
        @Config.LangKey("outfox.config.rare_biomes")
        @Config.RequiresMcRestart
        public String[] rare_biomes = OutfoxResources.DEFAULT_RARE_BIOMES;

        @Config.Comment({
            "List of biome dictionary types that foxes should be rare spawns in.",
            "This is for modpacks with large numbers of biomes to lazily add foxes to many biomes at once.",
            "This setting requires a Minecraft restart if changed from the in-game config menu!",
            "Default: (empty)"
        })
        @Config.LangKey("outfox.config.rare_types")
        @Config.RequiresMcRestart
        public String[] rare_types = {};
    }

    public static class Compat {

        @Config.Comment({
            "Is The One Probe compatibility enabled?",
            "If The One Probe is present, this adds the currently-searched-for block to the standard tooltip, and",
            "the XYZ coordinates of the located block to the Creative Probe tooltip.",
            "This setting requires a Minecraft restart if changed from the in-game config menu!"
        })
        @Config.LangKey("outfox.config.compat_theoneprobe")
        @Config.RequiresMcRestart
        public boolean compat_theoneprobe = true;

        @Config.Comment({
            "Is WAILA/HWYLA compatibility enabled?",
            "If WAILA or HWYLA are present, this adds the currently-searched-for block to the tooltip.",
            "This setting requires a Minecraft restart if changed from the in-game config menu!"
        })
        @Config.LangKey("outfox.config.compat_waila")
        @Config.RequiresMcRestart
        public boolean compat_waila = true;
    }

    public static class General {

        @Config.Comment({
            "Averts workplace accidents in tight spaces.",
            "If the second half of an item's ID name (e.g. minecraft:iron_pickaxe) contains one of these keys,",
            "you will be unable to attack your own foxes with that item. Please submit a bug report if you find",
            "a stone- or dirt-mining tool that is able to attack your foxes so it can be added to the defaults!",
            "Default:",
            "  pick",
            "  shovel",
            "  hammer",
            "  excavator",
            "  mattock",
            "  paxel",
            "  drill",
            "  disassembler",
            "  destructionwand"
        })
        @Config.LangKey("outfox.config.immune_tools")
        public String[] immune_tools = OutfoxResources.DEFAULT_IMMUNE_TOOLS;

        @Config.Comment("Send warning messages to the logfile?")
        @Config.LangKey("outfox.config.logging_enabled")
        public boolean logging_enabled = true;

        @Config.Comment({
            "The chance for each raw rabbit fed to a wild fox to tame it. Odds are one in this number.",
            "Default: 4"
        })
        @Config.LangKey("outfox.config.tame_chance")
        @Config.RangeInt(min=1)
        public int tame_chance = 4;
    }

    public static class Search {

        @Config.Comment({
            "The block distance the search AI will recursively check for unbreakable obstructions (e.g. bedrock)",
            "before giving up and assuming that a block can be reached,",
            "Increasing this value will make foxes less likely to lead you to inaccessible blocks, but may also",
            "heavily impact game performance. The default value should be fine if the only potential problem is",
            "vanilla bedrock gen. Set to 0 to disable this check entirely.",
            "Default: 5"
        })
        @Config.LangKey("outfox.config.obstruction_depth")
        @Config.RangeInt(min = 0)
        public int obstruction_depth = 5;

        @Config.Comment("Is block searching enabled?")
        @Config.LangKey("outfox.config.search_enabled")
        public boolean search_enabled = true;

        @Config.Comment({
            "The frequency, in ticks, with which the block search AI scans the area.",
            "Lower values will cause foxes to find blocks more quickly, but will probably impact game",
            "performance.",
            "Default: 20 (1 second)"
        })
        @Config.LangKey("outfox.config.search_frequency")
        @Config.RangeInt(min = 1)
        public int search_frequency = 20;

        @Config.Comment({"A list of block IDs (e.g. minecraft:diamond_ore) that foxes will not be allowed to",
            "search for.",
            "Default: (empty)"})
        @Config.LangKey("outfox.config.search_list")
        public String[] search_list = { };

        @Config.Comment({"Whether 'search_list' should be used as a whitelist instead. If true, foxes will",
            "only be allowed to search for blocks specified on that list."})
        @Config.LangKey("outfox.config.search_listmode")
        public boolean search_listmode = false;

        @Config.Comment({
            "The probability that any block search will fail to detect a block even if there is one in range.",
            "0 means searches will never fail, 99 means a 1% chance of success.",
            "Default: 0"
        })
        @Config.LangKey("outfox.config.search_odds")
        @Config.RangeInt(min = 0, max = 99)
        public int search_odds = 0;

        @Config.Comment("Should particles be used to convey block search status?")
        @Config.LangKey("outfox.config.particles_enabled")
        public boolean search_particles = true;

        @Config.Comment({
            "The distance, in blocks, that foxes should search away from themselves.",
            "The resulting search area will be a cube, 2n + 1 blocks to a side, centered around the block space",
            "the fox is in. At higher values, this also influences the pathfinding length of foxes' path-",
            "-navigators. Being greedy with this value will probably rapidly impact game performance!",
            "Default: 10 (searches 21^3 blocks)"
        })
        @Config.LangKey("outfox.config.search_range")
        @Config.RangeInt(min = 1)
        public int search_range = 10;

        @Config.Comment({
            "The number of waypoints that the search AI pathfinder should use.",
            "This setting is slightly experimental and the effects of changing it are not guaranteed to be",
            "visible (or even to exist at all). Higher values may make foxes smarter in situations with multiple",
            "paths that all lead somewhat close to the target block, but may also impact performance. Foxes may",
            "become appallingly stupid while searching if this is set too low; most (all?) vanilla pathfinders",
            "use 32 waypoints. If you're not sure, probably leave this alone.",
            "This setting requires a save & quit if changed from the in-game config menu!",
            "Default: 96",
        })
        @Config.LangKey("outfox.config.search_waypoints")
        @Config.RangeInt(min = 4)
        @Config.RequiresWorldRestart
        public int search_waypoints = 96;

        @Config.Comment({
            "The block state properties to be matched when searching for a block.",
            "The entries in this list allow foxes to tell the difference between distinct blocks that share the",
            "same block ID (e.g. different colors of stained glass which are all minecraft:stained_glass with tag",
            "'color', or andesite et al. which are all minecraft:stone with tag 'variant'). If you run into a",
            "problem where foxes track multiple unrelated blocks when searching for one, you can fix it by adding",
            "the relevant block state property (hint: F3) to this list... and also submit a bug report so it can",
            "be added to the defaults for future releases!",
            "Note: be careful of adding tags like 'facing' or 'orientation' as these will cause idiocy such as",
            "foxes only being able to track blocks that use those tags if you click the block on the fox while",
            "facing a certain direction. Which might make for a neat puzzle in a challenge map, but would get",
            "quite annoying in regular survival!",
            "Default:",
            "  color",
            "  colour",
            "  type",
            "  variant",
            "  compression_level_",
            "  decorstates",
            "  foliage",
            "  shade"
        })
        @Config.LangKey("outfox.config.state_matches")
        public String[] state_matches = OutfoxResources.DEFAULT_STATE_MATCHES;
    }

    public static class Stealing {

        @Config.Comment({
            "Override the sit AI's attack target reaction. False is vanilla behavior (what wolves do).",
            "If true, foxes that have been told to sit will not get up when you are attacked.",
            "This setting applies whether stealing is enabled or not."
        })
        @Config.LangKey("outfox.config.sit_override")
        public boolean sit_override = true;

        @Config.Comment("Should foxes be able to steal armor as well as held items?s")
        @Config.LangKey("outfox.config.stealing_armor")
        public boolean stealing_armor = false;

        @Config.Comment("Is item stealing enabled?")
        @Config.LangKey("outfox.config.stealing_enabled")
        public boolean stealing_enabled = true;

        @Config.Comment("Should foxes be able to steal from players?")
        @Config.LangKey("outfox.config.stealing_players")
        public boolean stealing_players = false;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {

        if (e.getModID().equals(OutfoxResources.MODID)) { sync(); }
    }

    public static void sync() {

        ConfigManager.sync(OutfoxResources.MODID, Config.Type.INSTANCE);
    }
}