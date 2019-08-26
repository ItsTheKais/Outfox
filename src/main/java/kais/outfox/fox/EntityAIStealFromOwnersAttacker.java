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

import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

import kais.outfox.OutfoxConfig;
import kais.outfox.OutfoxResources;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class EntityAIStealFromOwnersAttacker extends EntityAIBase {

    private final EntityFox fox;
    private final double speedApproach;
    private final double speedRetreat;
    private boolean approachPhase; // true if we are sneaking up on the target
    private boolean retreatPhase; // true when we have taken the item and are running back to the owner
    @Nullable
    private EntityLivingBase revengeTarget; // the entity we will try to steal from; null if we don't have a target
    @Nullable
    private String revengeSlot; // the item slot we will try to steal the item from; null if we don't have a target

    /**
     * under construction!
     * no snitching! >:/
     */
    public EntityAIStealFromOwnersAttacker(EntityFox foxIn, double speedAIn, double speedRIn) {

        this.fox = foxIn;
        this.speedApproach = speedAIn;
        this.speedRetreat = speedRIn;
        this.revengeTarget = null;
        this.approachPhase = false;
        this.retreatPhase = false;
    }

    public boolean shouldExecute() {

        return /*OutfoxConfig.stealing.stealing_enabled
            &&*/ this.fox.isTamed()
            && this.fox.getOwner() != null
            && !this.fox.isSitting()
            && this.getRevengeTarget()
            && this.getTargetSlot() != null;
    }

    public boolean shouldContinueExecuting() {

        if (/*OutfoxConfig.stealing.stealing_enabled
            &&*/ this.fox.getOwner() != null
            && !this.fox.isSitting()
            && this.revengeTarget != null
            && !this.revengeTarget.isDead) {

            if (this.revengeSlot == null || !this.targetSlotHasItem(this.revengeSlot)) {

                for (int i = 0; i < 3; i++) {

                    this.revengeSlot = getTargetSlot();
                    if (this.revengeSlot != null) { return true; }
                }

                return this.resetToNoTarget();
            } else { return true; }
        }

        return this.resetToNoTarget();
    }

    private boolean getRevengeTarget() {

        EntityLivingBase target = this.fox.getOwner().getRevengeTarget();
        if (target != null
            && !target.isDead
            && this.shouldTargetIfPlayer(target)) {

            Iterable<ItemStack> items = /*OutfoxConfig.stealing.stealing_armor ? target.getEquipmentAndArmor() :*/ target.getHeldEquipment();
            if (!Iterables.isEmpty(items)) {

                this.revengeTarget = target;
                return true;
            }
        }

        return this.resetToNoTarget();
    }

    @Nullable
    private String getTargetSlot() {

        Iterable<ItemStack> items = /*OutfoxConfig.stealing.stealing_armor ? this.revengeTarget.getEquipmentAndArmor() :*/ this.revengeTarget.getHeldEquipment();
        if (!Iterables.isEmpty(items)) {

            String[] slotsAll = this.getPossibleTargetSlots();
            for (int i = 0; i < slotsAll.length; i++) {

                if (this.revengeTarget.getItemStackFromSlot(this.getSlotFromName(slotsAll[i])).isEmpty()) { slotsAll[i] = null; }
            }
            for (int i = 0; i < 8; i++) {

                String slot = slotsAll[this.fox.getRNG().nextInt(slotsAll.length)];
                if (slot != null && this.targetSlotHasItem(slot)) { return slot; }
            }

            return null;
        }

        return null;
    }

    private boolean shouldTargetIfPlayer(EntityLivingBase target) {

        return target instanceof EntityPlayer
            /*? OutfoxConfig.stealing.stealing_players
            : true*/;
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

        return /*OutfoxConfig.stealing.stealing_armor
            ? ObjectArrays.concat(slotsArmor, slotsHands, String.class)
            :*/ slotsHands;
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
        return false;
    }
}