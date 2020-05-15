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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutfoxResources {

    //TODO: these three constants must be commented out, and the three constants below them uncommented, before building!
    //public static final String MODID = "outfox";
    //public static final String NAME = "Outfox";
    //public static final String VERSION = "$DEV$";

    public static final String MODID = "gradle%modid";
    public static final String NAME = "gradle%name";
    public static final String VERSION = "gradle%version";

    private static final Logger LOGGER = LogManager.getLogger(MODID);

    private static final ResourceLocation FOX_IDLE_RL = new ResourceLocation(MODID, "fox.idle");
    private static final ResourceLocation FOX_SNIFF_RL = new ResourceLocation(MODID, "fox.sniff");
    private static final ResourceLocation FOX_STEAL_RL = new ResourceLocation(MODID, "fox.steal");
    private static final ResourceLocation FOX_HURT_RL = new ResourceLocation(MODID, "fox.hurt");
    private static final ResourceLocation FOX_DEATH_RL = new ResourceLocation(MODID, "fox.death");

    public static final SoundEvent FOX_IDLE_SND = new SoundEvent(FOX_IDLE_RL).setRegistryName(FOX_IDLE_RL);
    public static final SoundEvent FOX_SNIFF_SND = new SoundEvent(FOX_SNIFF_RL).setRegistryName(FOX_SNIFF_RL);
    public static final SoundEvent FOX_STEAL_SND = new SoundEvent(FOX_STEAL_RL).setRegistryName(FOX_STEAL_RL);
    public static final SoundEvent FOX_HURT_SND = new SoundEvent(FOX_HURT_RL).setRegistryName(FOX_HURT_RL);
    public static final SoundEvent FOX_DEATH_SND = new SoundEvent(FOX_DEATH_RL).setRegistryName(FOX_DEATH_RL);

    public static final SoundEvent[] FOX_SND_SET = new SoundEvent[] {FOX_IDLE_SND, FOX_SNIFF_SND, FOX_STEAL_SND, FOX_HURT_SND, FOX_DEATH_SND};

    public static final String[] DEFAULT_COMMON_BIOMES = new String[] {

        "minecraft:roofed_forest",
        "minecraft:mutated_roofed_forest"
    };
    public static final String[] DEFAULT_RARE_BIOMES = new String[] {

        "minecraft:forest",
        "minecraft:forest_hills",
        "minecraft:birch_forest",
        "minecraft:birch_forest_hills",
        "minecraft:mutated_forest",
        "minecraft:mutated_birch_forest",
        "minecraft:mutated_birch_forest_hills"
    };
    public static final String[] DEFAULT_IMMUNE_TOOLS = new String[] {

        "pick", // vanilla/common
        "shovel", // vanilla/common
        "hammer", // various
        "excavator", // ticon
        "mattock", // ticon
        "paxel", // various
        "drill", // various
        "disassembler", // mekanism
        "destructionwand" // xu2
    };
    public static final String[] DEFAULT_SEARCH_BLACKLIST = new String[] { };
    public static final String[] DEFAULT_STATE_MATCHES = new String[] {

        "color", // vanilla/common
        "colour", // just in case!
        "type", // vanilla/common
        "variant", // vanilla/common
        "compression_level_", // xu2 compressed blocks
        "decorstates", // xu2 decoratives
        "foliage", // ticon slimegrass
        "shade" // flat-colored-blocks
    };
    public static final String[] DEFAULT_ITEM_ALIASES = new String[] { };
    public static final String[] DEFAULT_UNSTEALABLE_ITEMS = new String[] {

        "minecraft:skull"
    };
    public static final String[] DEFAULT_UNSTEALABLE_ENTITIES =  new String[] {

        "minecraft:witch"
    };

    public static void logInfo(String message) { if (OutfoxConfig.general.logging_enabled) { LOGGER.info(message); }}
    public static void logWarn(String message) { if (OutfoxConfig.general.logging_enabled) { LOGGER.warn(message); }}
    public static void logError(String message) { if (OutfoxConfig.general.logging_enabled) { LOGGER.error(message); }}

    /**
     * converts an array of String ResourceLocations into an ArrayList of Biomes for registering
     */
    private static ArrayList<Biome> stringsToBiomes(String[] input) {

        ArrayList<Biome> biomes = new ArrayList<Biome>();

        for (String i : input) {

            String[] j = i.split(":", 2);
            if (j.length != 2) {

                logWarn("Invalid biome ID '" + i + "' in biome config, skipped");
                continue;
            }
            Biome b = Biome.REGISTRY.getObject(new ResourceLocation(j[0], j[1]));
            if (b != null) { biomes.add(b); }
            else { logWarn("Invalid biome ID '" + i + "' in biome config, skipped"); }
        }

        return biomes;
    }

    /**
     * converts an array of String BiomeDictionary Types into an ArrayList of Biomes for registering
     */
    private static ArrayList<Biome> stringTypesToBiomes(String[] input) {

        ArrayList<Biome> biomes = new ArrayList<Biome>();
        Collection<BiomeDictionary.Type> allBiomes = BiomeDictionary.Type.getAll();
        Map<String, BiomeDictionary.Type> biomesByType = allBiomes.stream().collect(Collectors.toMap(BiomeDictionary.Type::getName, Function.identity()));

        for (String biome : input) {

            String b = biome.toUpperCase();
            if (allBiomes.contains(biomesByType.get(b))) { biomes.addAll((BiomeDictionary.getBiomes(BiomeDictionary.Type.getType(biome)))); }
            else { logWarn("Invalid biome type '" + biome + "' in biome config, skipped"); }
        }

        return biomes;
    }

    public static ArrayList<Biome> mergeBiomes(String[] inputBiomes, String[] inputTypes) {

        ArrayList<Biome> biomes = stringsToBiomes(inputBiomes);
        biomes.addAll(stringTypesToBiomes(inputTypes));
        return biomes;
    }

    /**
     * converts the output of BlockStateBase.toString() into a HashMap of String pairs because I'm too stupid to figure out IProperty
     */
    public static HashMap<String, String> blockStateToHashMap(IBlockState state) {

        return stateStringToHashMap(((BlockStateBase)state).toString(), state.getBlock().getRegistryName().toString());
    }

    public static HashMap<String, String> stateStringToHashMap(String statestring, @Nullable String block) {

        HashMap<String, String> map = new HashMap<String, String>(OutfoxConfig.search.state_matches.length);
        if (!statestring.contains("[")) { return map; }

        Pattern pattern;
        Matcher matcher;

        for (String s : OutfoxConfig.search.state_matches) {

            /**
             * the state string contains properties in the format: [property1=value1,property2=value2,property3=value3]
             * this regex matches: s + "=" + whatever, which all must be bounded on the left by "[" or "," and on the right by "]" or ","
             * the part after the "=" (which is what we ultimately need) can be accessed using group 1
             */
            pattern = Pattern.compile("(?:[\\[,]" + s + ".*?=)(.*?(?=[\\],]))");
            matcher = pattern.matcher(statestring);

            if (matcher.find()) {

                String g = matcher.group(1);
                if (s != null && !s.equals("")) {
                    if (g != null && !g.equals("")) { map.put(s, g); }
                    else { logWarn("The blockstate '" + s + "' was found on block '" + (block == null ? "(unknown)" : block) + "' but had null or blank value, ignoring"); }
                } else { logError("A null or blank blockstate was found on block '" + (block == null ? "(unknown)" : block) + "', this is probably caused by bad configs, it will be ignored"); }
            }
        }

        return map;
    }

    /**
     * checks the item alias list for an entry matching the input item, returns a string array of block ID + state string if there is a match, or null if not
     */
    @Nullable
    public static String[] getAliasedItem(ItemStack itemIn) {

        ResourceLocation rl = itemIn.getItem().getRegistryName();
        for (String alias : OutfoxConfig.search.search_aliases) {

            if (alias.startsWith(rl.toString())) {

                String[] s0 = alias.split("/"); // s0[0] is item id, s0[1] is target block and state string
                if (s0.length != 2) {

                    logError("The item alias entry '" + alias + "' is malformed (must be split by exactly one '/'), it will be ignored");
                    break;
                }

                String[] s1 = s0[0].split(":"); //s1[0] is RL domain, s1[1] is RL path, s1[2] (optional) is item metadata
                if (s1.length > 3) {

                    logError("The item alias entry '" + alias + "' is malformed (item ID must be formatted as item:id OR item:id:meta), it will be ignored");
                    break;
                }
                if (s1.length == 2 || (s1.length == 3 && Integer.valueOf(s1[2]) == itemIn.getMetadata())) { // it's a match

                    String[] s2 = s0[1].split("\\["); // s2[0] is the target block ID, s2[1] is the state string but missing the opening bracket
                    if (Block.getBlockFromName(s2[0]) != null) { return new String[] { s2[0], "[" + s2[1] }; // it's a valid block
                    } else {

                        logError("The item alias entry '" + alias + "' is malformed (block ID '" + s2[0] + "' could not be resolved), it will be ignored");
                        break;
                        }
                    }
            }
        }

        return null;
    }

    /**
     * returns true if the given block ID is allowed to be searched for given the current blacklist and listmode
     */
    public static boolean checkBlockIdIsBlacklisted(String blockname) {

        return OutfoxConfig.search.search_listmode == Arrays.asList(OutfoxConfig.search.search_list).contains(blockname);
    }

    /**
     * returns true if the given item ID is allowed to be stolen given the current blacklist and listmode
     */
    public static boolean checkItemIdIsBlacklisted(String itemname) {

        return OutfoxConfig.stealing.stealing_itemsmode == Arrays.asList(OutfoxConfig.stealing.stealing_items).contains(itemname);
    }

    /**
     * returns true if the given entity ID is allowed to be stolen from given the current blacklist and listmode
     */
    public static boolean checkEntityIdIsBlacklisted(String itemname) {

        return OutfoxConfig.stealing.stealing_entitymode == Arrays.asList(OutfoxConfig.stealing.stealing_entities).contains(itemname);
    }
}