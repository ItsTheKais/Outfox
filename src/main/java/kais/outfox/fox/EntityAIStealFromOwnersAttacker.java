/**
 * Copyright © 2019 Aiden Vaughn "ItsTheKais"
 *
 * This file is part of Outfox.
 *
 * The code of Outfox is free and available under the terms of the latest version of the GNU Lesser General
 * Public License. Outfox is distributed with no warranty, implied or otherwise. Outfox should have come with
 * a copy of the GNU Lesser General Public License; if not, see: <https://www.gnu.org/licenses/>
 */

package kais.outfox.fox;

import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Iterables;
import javax.annotation.Nullable;
import kais.outfox.OutfoxConfig;
import kais.outfox.OutfoxResources;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class EntityAIStealFromOwnersAttacker extends EntityAIBase {

    private final EntityFox fox;
    private final double speedApproach;
    private final double speedRetreat;
    private boolean running;
    private int minDamage;
    private int maxDamage;
    private int pathCooldown;
    private int attentionTimer;
    private int aggroOdds;
    private boolean approachPhase; // true if we are sneaking up on the target
    private boolean retreatPhase; // true when we have taken the item and are running back to the owner
    @Nullable
    private EntityLivingBase revengeTarget; // the entity we will try to steal from; null if we don't have a target
    @Nullable
    private String revengeSlot; // the item slot we will try to steal the item from; null if we don't have a target

    /**
     * Steals items from monsters that attack the fox's owner and delivers held item to owner;
     * Will not fire if the fox is wild, at low health, or already has a held item;
     * Runs in two phases: approach (sneak up and steal) and retreat (run to owner and drop item).
     * Approach phase begins when the owner has been attacked by an enemy that is holding an item;
     * The fox will walk to the target at input speed A, grab the item, and possibly draw aggro;
     * Retreat phase begins immediately upon stealing an item;
     * The fox will walk to its owner, drop the item, then walk past its owner away from the target, at input speed R;
     * Stops if the target or owner dies or the target loses its held items
     */
    public EntityAIStealFromOwnersAttacker(EntityFox foxIn, double speedAIn, double speedRIn) {

        this.fox = foxIn;
        this.speedApproach = speedAIn;
        this.speedRetreat = speedRIn;
        this.revengeTarget = null;
        this.approachPhase = false;
        this.retreatPhase = false;
        this.updateStats();
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {

        return OutfoxConfig.stealing.stealing_enabled
            && this.fox.isTamed()
            && this.fox.getOwner() != null
            && !this.fox.isChild()
            && !this.fox.isSitting()
            && this.fox.getHealth() >= OutfoxConfig.stealing.stealing_minhealth
            && this.fox.stealCooldown == 0
            && !this.fox.hasStolenItem()
            && this.getRevengeTarget()
            && this.getTargetSlot();
    }

    public boolean shouldContinueExecuting() {

        if (OutfoxConfig.stealing.stealing_enabled
            && this.fox.getOwner() != null
            && !this.fox.getOwner().isDead
            && !this.fox.isSitting()
            && this.revengeTarget != null
            && !this.revengeTarget.isDead
            && this.shouldTargetWithConfig(this.revengeTarget)) {

            if (this.fox.hasStolenItem()) { return true; }

            if (this.revengeSlot == null || !this.targetSlotHasItem(this.revengeSlot)) {

                for (int i = 0; i < 3; i++) { if (this.getTargetSlot()) { return true; } }
                return this.resetToNoTarget();
            }
            else { return true; }
        }

        return this.resetToNoTarget();
    }

    public void startExecuting() {

        this.running = true;
        this.pathCooldown = 10;

        if (this.fox.hasStolenItem()) { this.retreatPhase = true; }
        else { this.approachPhase = true; }
    }

    public void updateTask() {

        if (this.approachPhase) {

            if (this.fox.getDistance(this.revengeTarget) >= 1.25f ) { this.tryPathWithCooldown(this.revengeTarget, this.speedApproach); }
            else if (this.tryStealItem()) {
                this.revengeSlot = null;
                this.approachPhase = false;
                this.retreatPhase = true;
            }
        }
        if (this.retreatPhase) {

            if (this.attentionTimer > 0) {

                this.targetLookAtMe();
                --this.attentionTimer;
            }

            if (this.fox.getDistance(this.fox.getOwner()) >= 1.25F) { this.tryPathWithCooldown(this.fox.getOwner(), this.speedRetreat); }
            else if (this.fox.dropStolenItem()) {

                this.fleeFromTarget();
                this.resetToNoTarget();
                this.fox.stealCooldown = 100;
            }
        }
    }

    public boolean isRunning() {

        return this.running;
    }

    private void updateStats() {

        this.minDamage = OutfoxConfig.stealing.stealing_mindamage;
        this.maxDamage = OutfoxConfig.stealing.stealing_maxdamage;
        this.aggroOdds = OutfoxConfig.stealing.stealing_aggrochance;
    }

    private boolean getRevengeTarget() {

        EntityLivingBase target = this.fox.getOwner().getRevengeTarget();
        if (target != null
            && !target.isDead
            && this.shouldTargetWithConfig(target)) {

            Iterable<ItemStack> items = OutfoxConfig.stealing.stealing_armor ? target.getEquipmentAndArmor() : target.getHeldEquipment();
            if (!Iterables.isEmpty(items)) {

                this.revengeTarget = target;
                return true;
            }
        }

        return this.resetToNoTarget();
    }

    private boolean getTargetSlot() {

        if (this.revengeTarget == null) { return false; }

        Iterable<ItemStack> items = OutfoxConfig.stealing.stealing_armor ? this.revengeTarget.getEquipmentAndArmor() : this.revengeTarget.getHeldEquipment();
        if (!Iterables.isEmpty(items)) {

            String[] slotsAll = this.getPossibleTargetSlots();
            for (int i = 0; i < slotsAll.length; i++) {

                ItemStack item = this.revengeTarget.getItemStackFromSlot(this.getSlotFromName(slotsAll[i]));
                if (item.isEmpty() || !OutfoxResources.checkItemIdIsBlacklisted(item.getItem().getRegistryName().toString())) { slotsAll[i] = null; }
            }

            for (int i = 0; i < 8; i++) {

                String slot = slotsAll[this.fox.getRNG().nextInt(slotsAll.length)];
                if (slot != null && this.targetSlotHasItem(slot)) {
                    this.revengeSlot = slot;
                    return true;
                }
            }

            return this.resetToNoTarget();
        }

        return false;
    }

    private boolean tryStealItem() {

        if (this.fox.hasStolenItem()) { return this.resetToNoTarget(); }

        ItemStack item = this.revengeTarget.getItemStackFromSlot(this.getSlotFromName(this.revengeSlot)).copy();
        if (!item.isEmpty() && OutfoxResources.checkItemIdIsBlacklisted(item.getItem().getRegistryName().toString())) {

            if (this.maxDamage != 0) {

                int damage;
                if (this.maxDamage <= this.minDamage) { damage = Math.round(item.getMaxDamage() * (1 - (this.maxDamage * 0.01F))); }
                else { damage = Math.round(item.getMaxDamage() * (1 - ((this.fox.getRNG().nextInt(this.maxDamage - this.minDamage) + this.minDamage) * 0.01F))); }

                item.damageItem(damage, this.fox);
            }

            this.fox.setStolenItem(item);
            this.revengeTarget.setItemStackToSlot(this.getSlotFromName(this.revengeSlot), ItemStack.EMPTY);
            this.fox.playStealSound();
            this.startleTarget();
            return true;
        }

        return false;
    }

    private void startleTarget() {

        if (!this.revengeTarget.isRiding() && (this.revengeTarget.onGround || this.revengeTarget.isInWater())) { this.revengeTarget.addVelocity(0.0F, 0.5F, 0.0F); }

        if (this.aggroOdds > 0 && (this.aggroOdds == 100 || this.fox.getRNG().nextInt(100) < this.aggroOdds)) {

            if (this.revengeTarget instanceof EntityLiving) { ((EntityLiving)this.revengeTarget).setAttackTarget(this.fox); }
            this.revengeTarget.setRevengeTarget(this.fox);
        }

        //TODO: simple way to play the entity's hurt sound? mojang why would you make that protected
        this.attentionTimer = this.fox.getRNG().nextInt(16) + 25;
    }

    private void targetLookAtMe() { //TODO: works very inconsistently

        if (this.revengeTarget != null && !this.revengeTarget.isDead && this.revengeTarget instanceof EntityLiving) {

            ((EntityLiving)this.revengeTarget).getLookHelper().setLookPositionWithEntity(this.fox, ((EntityLiving)this.revengeTarget).getHorizontalFaceSpeed(), ((EntityLiving)this.revengeTarget).getVerticalFaceSpeed());
        }
        else {
            this.attentionTimer = 0;
        }
    }

    private void fleeFromTarget() {

        if (this.aggroOdds == 0) { return; }

        BlockPos ownerPos = this.fox.getOwner().getPosition();
        BlockPos targetPos = this.revengeTarget.getPosition();

        int targetX = ownerPos.getX() + ((int)Math.signum((ownerPos.getX() - targetPos.getX())) * 7);
        int targetZ = ownerPos.getZ() + ((int)Math.signum((ownerPos.getZ() - targetPos.getZ())) * 7);

        this.fox.getNavigator().tryMoveToXYZ(targetX, ownerPos.getY(), targetZ, this.speedRetreat);
    }

    private boolean shouldTargetWithConfig(EntityLivingBase target) {

        return target instanceof EntityPlayer
            ? OutfoxConfig.stealing.stealing_players
            : OutfoxResources.checkEntityIdIsBlacklisted(EntityList.getKey(target).toString());
    }

    private String[] getPossibleTargetSlots() {

        String[] slotsArmor = new String[] {
            "head",
            "chest",
            "legs",
            "feet"
        };
        String[] slotsHands = new String[] {
            "mainhand",
            "offhand"
        };

        return OutfoxConfig.stealing.stealing_armor
            ? ObjectArrays.concat(slotsArmor, slotsHands, String.class)
            : slotsHands;
    }

    private EntityEquipmentSlot getSlotFromName(String name) {

        return EntityEquipmentSlot.fromString(name);
    }

    private boolean targetSlotHasItem(String slot) {

        return this.revengeTarget.hasItemInSlot(this.getSlotFromName(slot));
    }

    private boolean resetToNoTarget() {

        this.revengeTarget = null;
        this.revengeSlot = null;
        this.approachPhase = false;
        this.retreatPhase = false;
        this.updateStats();
        return false;
    }

    private void tryPathWithCooldown(EntityLivingBase entity, double speed) {

        if (--this.pathCooldown <= 0) {

            this.pathCooldown = 10;
            this.fox.getNavigator().tryMoveToEntityLiving(entity, speed);
        }
    }
}