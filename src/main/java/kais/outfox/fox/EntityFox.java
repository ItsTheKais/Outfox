/**
 * Copyright � 2018 Aiden Vaughn "ItsTheKais"
 *
 * This file is part of Outfox.
 *
 * The code of Outfox is free and available under the terms of the latest version of the GNU Lesser General
 * Public License. Outfox is distributed with no warranty, implied or otherwise. Outfox should have come with
 * a copy of the GNU Lesser General Public License; if not, see: <https://www.gnu.org/licenses/>
 */

package kais.outfox.fox;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import kais.outfox.Outfox;
import kais.outfox.OutfoxConfig;
import kais.outfox.OutfoxResources;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathHeap;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.DyeUtils;

public class EntityFox extends EntityTameable {

    /**
     * VARIANT values:
     *   0 = cross;
     *   1 = marble;
     *   2 = pale;
     *   3 = red;
     *   4 = rusty;
     *   5 = silver;
     */
    private static final DataParameter<Integer> VARIANT = EntityDataManager.<Integer>createKey(EntityFox.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RIBBON_COLOR = EntityDataManager.<Integer>createKey(EntityFox.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> STOLEN_ITEM = EntityDataManager.<ItemStack>createKey(EntityFox.class, DataSerializers.ITEM_STACK);
    private EntityAIAvoidEntity<EntityPlayer> aiAvoid;
    private EntityAISit aiSit;
    private EntityAISearchForBlock aiSearch;
    private EntityAITempt aiTempt;
    @Nullable
    protected Block searchedBlock; // the current block id being searched for
    @Nullable
    protected HashMap<String, String> stateTags; // map of block state properties that the block search should match
    protected PathNavigateGround searchNavigator; // customized pathnavigator and pathfinder used for block searching
    protected int stealCooldown;
    private boolean interacted;
    private boolean isWet;
    private boolean isShaking;
    private float timeFoxIsShaking;
    private float prevTimeFoxIsShaking;

    public EntityFox(World worldIn) {

        super(worldIn);
        this.searchNavigator = new PathNavigateTweakable<>(this, worldIn);
        this.setSize(0.6F, 0.7F);
    }

    protected void initEntityAI() {

        this.aiSearch = new EntityAISearchForBlock(this, 0.75D);
        this.aiSit = new AISit<>(this);
        this.aiTempt = new EntityAITempt(this, 0.5D, Items.RABBIT, true);

        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, this.aiTempt);
        //                 4, this.aiAvoid
        this.tasks.addTask(5, new EntityAIMate(this, 0.75D));
        this.tasks.addTask(6, new EntityAIStealFromOwnersAttacker(this, 0.66D, 1.15D));
        this.tasks.addTask(7, this.aiSearch);
        this.tasks.addTask(8, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(9, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
    }

    protected void entityInit() {

        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(RIBBON_COLOR, EnumDyeColor.BLUE.getDyeDamage());
        this.dataManager.register(STOLEN_ITEM, ItemStack.EMPTY);
    }

    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {

        this.setVariant(this.rand.nextInt(6));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    protected void applyEntityAttributes() {

        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.max(56.0D, OutfoxConfig.search.search_range));
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
    }

    public void writeEntityToNBT(NBTTagCompound compound) {

        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setByte("RibbonColor", (byte)this.getRibbonColor().getDyeDamage());
        if (this.searchedBlock != null) {

            compound.setString("SearchedBlock", Block.REGISTRY.getNameForObject(this.searchedBlock).toString());

            if (this.stateTags != null) {

                String tagstring = "[";
                for (Map.Entry<String, String> entry : this.stateTags.entrySet()) { tagstring += entry.getKey() + "=" + entry.getValue() + ","; }
                tagstring += "]";
                compound.setString("SearchedBlockTags", tagstring);
            }
        }

        if (!this.dataManager.get(STOLEN_ITEM).isEmpty()) {

            compound.setTag("StolenItem", this.dataManager.get(STOLEN_ITEM).writeToNBT(new NBTTagCompound()));
        }
    }

    public void readEntityFromNBT(NBTTagCompound compound) {

        super.readEntityFromNBT(compound);

        if (compound.hasKey("Variant", 99)) { this.setVariant(compound.getInteger("Variant")); }
        else {

            OutfoxResources.logWarn("A fox didn't have a saved variant for some reason, defaulting to 0");
            this.setVariant(0);
        }

        if (compound.hasKey("RibbonColor", 99)) { this.setRibbonColor(EnumDyeColor.byDyeDamage(compound.getByte("RibbonColor"))); }
        else {

            OutfoxResources.logWarn("A fox didn't have a saved ribbon color for some reason, defaulting to blue");
            this.setRibbonColor(EnumDyeColor.BLUE);
        }

        if (compound.hasKey("SearchedBlock")) {

            this.searchedBlock = Block.REGISTRY.getObject(new ResourceLocation(compound.getString("SearchedBlock")));

            if (compound.hasKey("SearchedBlockTags")) { this.stateTags = OutfoxResources.stateStringToHashMap(compound.getString("SearchedBlockTags"), compound.getString("SearchedBlock")); }
        }

        if (compound.hasKey("StolenItem")) { this.setStolenItem(new ItemStack(compound.getCompoundTag("StolenItem"))); }
        else { this.setStolenItem(ItemStack.EMPTY); }

        if (this.aiSit != null) { this.aiSit.setSitting(compound.getBoolean("Sitting")); }
    }

    public void onUpdate() {

        super.onUpdate();

        if (this.stealCooldown > 0) { --this.stealCooldown; }

        if (this.isWet()) {

            this.isWet = true;
            this.isShaking = false;
            this.timeFoxIsShaking = 0.0F;
            this.prevTimeFoxIsShaking = 0.0F;
        }
        else if (this.isShaking) {

            if (this.timeFoxIsShaking == 0.0F) {

                this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            }

            this.prevTimeFoxIsShaking = this.timeFoxIsShaking;
            this.timeFoxIsShaking += 0.05F;

            if (this.prevTimeFoxIsShaking >= 2.0F) {

                this.isWet = false;
                this.isShaking = false;
                this.prevTimeFoxIsShaking = 0.0F;
                this.timeFoxIsShaking = 0.0F;
            }

            if (this.prevTimeFoxIsShaking > 0.4F && this.world.isRemote) {

                float py = (float)this.getEntityBoundingBox().minY;

                int i = (int)(MathHelper.sin((this.timeFoxIsShaking - 0.4F) * (float)Math.PI) * 7.0F);
                for (int j = 0; j < i; ++j) {

                    float px = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
                    float pz = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
                    float vx = (this.rand.nextFloat() * 2.0F - 1.0F) * 0.1F;
                    float vz = (this.rand.nextFloat() * 2.0F - 1.0F) * 0.1F;
                    Outfox.proxy.doParticleEffect("shake", this, this.posX + px, py + 0.8F, this.posZ + pz, vx, this.motionY, vz);
                }
            }
        }
    }

    public void onLivingUpdate() {

        super.onLivingUpdate();
        this.interacted = false;

        if (!this.world.isRemote && this.isWet && !this.isShaking && this.onGround && !this.hasPath()) {

            this.isShaking = true;
            this.timeFoxIsShaking = 0.0F;
            this.prevTimeFoxIsShaking = 0.0F;
            this.world.setEntityState(this, (byte)8);
        }
    }

    public void onDeath(DamageSource cause) {

        super.onDeath(cause);
        if (!this.world.isRemote) { this.dropStolenItem(); }
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand) {

        if (this.interacted) { return false; } // the off-hand should only try to interact if the main hand did not
        this.interacted = true;

        ItemStack item = player.getHeldItem(hand);
        boolean isFoxFood = item.getItem() instanceof ItemFood && isBreedingItem(item);

        if (this.isTamed() && this.isOwner(player)) {

            if (!(isFoxFood && (this.getHealth() < 15.0F)) && super.processInteract(player, hand)) { return true; } // breed

            if (!item.isEmpty()) {

                if (isFoxFood && (this.getHealth() < 15.0F)) { // heal

                    ItemFood food = (ItemFood)item.getItem();
                    this.heal(food.getHealAmount(item));
                    if (!player.capabilities.isCreativeMode) { item.shrink(1); }
                    return true;
                }
                else if (DyeUtils.isDye(item)) { // dye ribbon

                    EnumDyeColor color = EnumDyeColor.byDyeDamage(DyeUtils.dyeDamageFromStack(item).orElse(EnumDyeColor.BLUE.getDyeDamage()));

                    if (color != this.getRibbonColor()) {

                        this.setRibbonColor(color);
                        if (!player.capabilities.isCreativeMode) { item.shrink(1); }
                        return true;
                    }
                }
                else if (this.setSearchedBlock(item)) { return true; } // search
            }
            else {

                if (OutfoxConfig.search.search_enabled
                && player.isSneaking()
                && this.searchedBlock != null) { // clear search

                    if (this.aiSearch != null) { this.aiSearch.reset(); }
                    this.searchedBlock = null;
                    Outfox.proxy.doParticlesSimple("block_cancel", this);
                    return true;
                }
            }

            if (!this.world.isRemote && hand.equals(EnumHand.OFF_HAND) && !this.dropStolenItem()) { //drop item, or sit/stand if no item

                this.aiSit.setSitting(!this.isSitting());
                this.isJumping = false;
                this.getNavigator().clearPath(); // TODO: why isn't this working
                return true;
            }
        }
        else if (!this.isTamed()
        && (player.capabilities.isCreativeMode
            || this.aiTempt == null
            || this.aiTempt.isRunning())
        && isFoxFood
        && player.getDistanceSq(this) < 9.0D) { // tame

            if (!player.capabilities.isCreativeMode) { item.shrink(1); }

            if (!this.world.isRemote) {

                if ((player.capabilities.isCreativeMode
                    || this.rand.nextInt(Math.min(1, OutfoxConfig.general.tame_chance)) + 1 == 1)
                && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) { // success

                    this.setTamedBy(player);
                    this.playTameEffect(true);
                    this.aiSit.setSitting(true);
                    this.world.setEntityState(this, (byte)7);
                }
                else { // failure

                    this.playTameEffect(false);
                    this.world.setEntityState(this, (byte)6);
                }
            }

            return true;
        }

        this.interacted = false;
        return false;
    }

    protected void setupTamedAI() {

        if (this.aiAvoid == null) { this.aiAvoid = new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class, 12.0F, 0.7D, 1.2D); }
        this.tasks.removeTask(this.aiAvoid);
        if (!this.isTamed()) { this.tasks.addTask(4, this.aiAvoid); }
    }

    public ItemStack getActiveItemStack() {

        return this.dataManager.get(STOLEN_ITEM);
    }

    public boolean isBreedingItem(ItemStack item) {
        for (String key : OutfoxConfig.general.foods) {
            if (item.getItem() == Item.getByNameOrId(key)) return true;
        }
        return false;
    }

    public boolean canMateWith(EntityAnimal otheranimal) {

        return this.isTamed()
        && !this.isSitting()
        && super.canMateWith(otheranimal)
        && !((EntityTameable)otheranimal).isSitting();
    }

    public EntityFox createChild(EntityAgeable ageable) {

        EntityFox fox = new EntityFox(this.world);
        fox.setVariant(this.rand.nextInt(6));
        UUID uuid = this.getOwnerId();

        if (uuid != null) {

            fox.setOwnerId(uuid);
            fox.setTamed(true);
        }

        return fox;
    }

    public boolean hitByEntity(Entity entityIn) {

        if (entityIn instanceof EntityPlayer
        && (EntityPlayer)entityIn == this.getOwner()
        //&& this.getOwner().isSwingInProgress
        && this.getOwner().swingingHand != null) {

            String item = this.getOwner().getHeldItem(this.getOwner().swingingHand).getItem().getRegistryName().getResourcePath();
            for (String s : OutfoxConfig.general.immune_tools) {

                if (item.contains(s)) { return true; }
            }
        }

        boolean b = super.hitByEntity(entityIn);
        if (!this.world.isRemote) { this.aiSit.setSitting(b); }
        return b;
    }

    public boolean canBePushed() {

       if (this.aiSearch != null && this.aiSearch.isBlockFound()) { return !this.getNavigator().noPath() && super.canBePushed(); } // TODO: why isn't this working
       return super.canBePushed();
    }

    public int getVariant() {

        return MathHelper.clamp(this.dataManager.get(VARIANT).intValue(), 0, 5);
    }

    public EnumDyeColor getRibbonColor() {

        return EnumDyeColor.byDyeDamage(this.dataManager.get(RIBBON_COLOR).intValue() & 15);
    }

    public void setRibbonColor(EnumDyeColor ribboncolor) {

        this.dataManager.set(RIBBON_COLOR, ribboncolor.getDyeDamage());
    }

    @Nullable
    public Block getSearchedBlock() {

        return this.searchedBlock;
    }

    @Nullable
    public int[] getTargetBlock() {

        if (this.aiSearch != null) { return this.aiSearch.getTargetBlock(); }

        return null;
    }

    public boolean hasStolenItem() {

        return this.dataManager.get(STOLEN_ITEM) != ItemStack.EMPTY;
    }

    public boolean setStolenItem(ItemStack item) {

        this.dataManager.set(STOLEN_ITEM, item);
        return this.dataManager.get(STOLEN_ITEM) != ItemStack.EMPTY;
    }

    public boolean dropStolenItem() {

        ItemStack item = this.dataManager.get(STOLEN_ITEM);
        if (!item.isEmpty()) {

            this.entityDropItem(item, 0.5f);
            this.dataManager.set(STOLEN_ITEM, ItemStack.EMPTY);
            return true;
        }

        return false;
    }

    protected boolean canTriggerWalking() {

        if (this.aiSearch != null) { return !this.aiSearch.isRunning() && super.canTriggerWalking(); }

        return super.canTriggerWalking();
    }

    protected void playStealSound() {

        this.playSound(OutfoxResources.FOX_STEAL_SND, this.getSoundVolume(), 1.0f);
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {

        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getAmbientSound() {

        return OutfoxConfig.search.search_enabled
        && !this.isSitting()
        && this.aiSearch != null
        && this.aiSearch.isBlockFound()
            ? OutfoxResources.FOX_SNIFF_SND
            : OutfoxResources.FOX_IDLE_SND;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {

        return OutfoxResources.FOX_HURT_SND;
    }

    protected SoundEvent getDeathSound() {

        return OutfoxResources.FOX_DEATH_SND;
    }

    protected void setVariant(int variant) {

        this.dataManager.set(VARIANT, variant);
    }

    protected boolean setSearchedBlock(ItemStack item) {

        if (!OutfoxConfig.search.search_enabled) { return false; }

        Block b = null;
        String tags = "";
        String[] alias = OutfoxResources.getAliasedItem(item);

        if (alias != null) {
            b = Block.getBlockFromName(alias[0]);
            tags = alias[1];
        } else if (item.getItem() instanceof ItemBlock) { b = ((ItemBlock)item.getItem()).getBlock(); }

        if (b != null) {

            if (this.aiSearch != null) { this.aiSearch.reset(); }
            if (OutfoxResources.checkBlockIdIsBlacklisted(b.getRegistryName().toString())) { // set block

                this.searchedBlock = b;
                if (tags == "") { this.setBlockTags(b, item.getMetadata()); }
                else { this.stateTags = OutfoxResources.stateStringToHashMap(tags, alias[0] + " (from aliased items list, check config!)"); }
                Outfox.proxy.doParticlesSimple("block_set", this);
                return true;
            }
            else { // block is disallowed

                this.searchedBlock = null;
                this.stateTags = new HashMap<String, String>();
                Outfox.proxy.doParticlesSimple("block_notallowed", this);
                return true;
            }
        }

        return false;
    }

    protected void setBlockTags(Block block, int meta) {

        Vec3d v = this.getOwner().getLookVec();
        this.stateTags = OutfoxResources.blockStateToHashMap(block.getStateForPlacement(this.world, this.getPosition(), this.getOwner().getHorizontalFacing(), (float)v.x, (float)v.y, (float)v.z, meta, this.getOwner(), this.getOwner().getActiveHand()));
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {

        if (id == 8) {

            this.isShaking = true;
            this.timeFoxIsShaking = 0.0F;
            this.prevTimeFoxIsShaking = 0.0F;
        }
        else if (id == 9) {

            Vec3d v = Vec3d.fromPitchYaw(0.0F, this.getRotationYawHead());
            Outfox.proxy.doParticleEffect("searching", this, this.posX + (v.x * 0.8D), this.getEntityBoundingBox().minY + 0.6D, this.posZ + (v.z * 0.8D), 0.0D, 0.05D, 0.0D); // TODO: this particle moves down instead of up when the player is within ~1 block, no clue why
        }
        else {

            super.handleStatusUpdate(id);
        }
    }

    @SideOnly(Side.CLIENT)
    protected ItemStack getStolenItem() {

        return this.dataManager.get(STOLEN_ITEM);
    }

    @SideOnly(Side.CLIENT)
    protected boolean isFoxWet() {

        return this.isWet;
    }

    @SideOnly(Side.CLIENT)
    protected float getShadingWhileWet(float partialTicks) {

        return 0.75F + (this.prevTimeFoxIsShaking + (this.timeFoxIsShaking - this.prevTimeFoxIsShaking) * partialTicks) / 2.0F * 0.25F;
    }

    @SideOnly(Side.CLIENT)
    protected float getShakeAngle(float partialTicks, float offset) {

        float f = (this.prevTimeFoxIsShaking + (this.timeFoxIsShaking - this.prevTimeFoxIsShaking) * partialTicks + offset) / 1.8F;

        if (f < 0.0F) { f = 0.0F; }
        else if (f > 1.0F) { f = 1.0F; }

        return MathHelper.sin(f * (float)Math.PI) * MathHelper.sin(f * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
    }

    /**
     * EntityAISit tweak; check config before getting up when owner is attacked
     */
    static class AISit<T extends EntityTameable> extends EntityAISit {

        private final EntityFox fox;

        public AISit(EntityFox foxIn) {

            super(foxIn);
            this.fox = foxIn;
        }

        public boolean shouldExecute() {

            if (!OutfoxConfig.stealing.sit_override) { return super.shouldExecute(); }

            return this.fox.getOwner() != null
            && this.fox.getOwner().getRevengeTarget() == null
                ? super.shouldExecute()
                : this.fox.isSitting();
        }
    }

    /**
     * PathNavigateGround tweak; use PathFinderTweakable instead of PathFinder
     */
    static class PathNavigateTweakable<T extends EntityLiving, U extends World> extends PathNavigateGround {

        private final EntityFox fox;

        public PathNavigateTweakable(EntityLiving entitylivingIn, World worldIn) {

            super(entitylivingIn, worldIn);
            this.fox = (EntityFox)entitylivingIn;
        }

        @Override
        protected PathFinder getPathFinder() {

            this.nodeProcessor = new WalkNodeProcessor();
            this.nodeProcessor.setCanEnterDoors(true);
            return new PathFinderTweakable<>(this.nodeProcessor);
        }

        public boolean setPath(@Nullable Path pathentityIn, double speedIn) {

            return this.fox.getNavigator().setPath(pathentityIn, speedIn);
        }
    }

    /**
     * PathFinder tweak; enables configurable waypoint count
     */
    static class PathFinderTweakable<T extends NodeProcessor> extends PathFinder {

        private final PathPoint[] pathOptions = new PathPoint[OutfoxConfig.search.search_waypoints]; // the entire bottom 15% of this file exists purely for the sake of this one line! �\(o_�)/�
        private final PathHeap path = new PathHeap();
        private final Set<PathPoint> closedSet = Sets.<PathPoint>newHashSet();
        private final NodeProcessor nodeProcessor;

        public PathFinderTweakable(NodeProcessor processorIn) {

            super(processorIn);
            this.nodeProcessor = processorIn;
        }

        @Nullable
        @Override
        public Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, BlockPos targetPos, float maxDistance) {

            return this.findPath(worldIn, entitylivingIn, targetPos.getX() + 0.5F, targetPos.getY() + 0.5F, targetPos.getZ() + 0.5F, maxDistance);
        }

        @Nullable
        private Path findPath(IBlockAccess worldIn, EntityLiving entitylivingIn, double x, double y, double z, float maxDistance) {

            this.path.clearPath();
            this.nodeProcessor.init(worldIn, entitylivingIn);
            PathPoint pathpoint = this.nodeProcessor.getStart();
            PathPoint pathpoint1 = this.nodeProcessor.getPathPointToCoords(x, y, z);
            Path path = this.findPath(pathpoint, pathpoint1, maxDistance);
            this.nodeProcessor.postProcess();
            return path;
        }

        @Nullable
        private Path findPath(PathPoint pathFrom, PathPoint pathTo, float maxDistance) {

            pathFrom.totalPathDistance = 0.0F;
            pathFrom.distanceToNext = pathFrom.distanceManhattan(pathTo);
            pathFrom.distanceToTarget = pathFrom.distanceToNext;
            this.path.clearPath();
            this.closedSet.clear();
            this.path.addPoint(pathFrom);
            PathPoint pathpoint = pathFrom;
            int i = 0;

            while (!this.path.isPathEmpty()) {

                ++i;

                if (i >= 200) { break; }

                PathPoint pathpoint1 = this.path.dequeue();

                if (pathpoint1.equals(pathTo)) {

                    pathpoint = pathTo;
                    break;
                }

                if (pathpoint1.distanceManhattan(pathTo) < pathpoint.distanceManhattan(pathTo)) {

                    pathpoint = pathpoint1;
                }

                pathpoint1.visited = true;
                int j = this.nodeProcessor.findPathOptions(this.pathOptions, pathpoint1, pathTo, maxDistance);

                for (int k = 0; k < j; ++k) {

                    PathPoint pathpoint2 = this.pathOptions[k];
                    float f = pathpoint1.distanceManhattan(pathpoint2);
                    pathpoint2.distanceFromOrigin = pathpoint1.distanceFromOrigin + f;
                    pathpoint2.cost = f + pathpoint2.costMalus;
                    float f1 = pathpoint1.totalPathDistance + pathpoint2.cost;

                    if (pathpoint2.distanceFromOrigin < maxDistance && (!pathpoint2.isAssigned() || f1 < pathpoint2.totalPathDistance)) {

                        pathpoint2.previous = pathpoint1;
                        pathpoint2.totalPathDistance = f1;
                        pathpoint2.distanceToNext = pathpoint2.distanceManhattan(pathTo) + pathpoint2.costMalus;

                        if (pathpoint2.isAssigned()) { this.path.changeDistance(pathpoint2, pathpoint2.totalPathDistance + pathpoint2.distanceToNext); }
                        else {

                            pathpoint2.distanceToTarget = pathpoint2.totalPathDistance + pathpoint2.distanceToNext;
                            this.path.addPoint(pathpoint2);
                        }
                    }
                }
            }

            if (pathpoint == pathFrom) {  return null; }
            else {

                Path path = this.createPath(pathFrom, pathpoint);
                return path;
            }
        }

        private Path createPath(PathPoint start, PathPoint end) {

            int i = 1;
            for (PathPoint p = end; p.previous != null; p = p.previous) { ++i; }

            PathPoint[] path = new PathPoint[i];
            PathPoint p2 = end;
            --i;

            for (path[i] = end; p2.previous != null; path[i] = p2) {

                p2 = p2.previous;
                --i;
            }

            return new Path(path);
        }
    }
}