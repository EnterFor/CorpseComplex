/*
 * Copyright (c) 2017. <C4>
 *
 * This Java class is distributed as a part of Corpse Complex.
 * Corpse Complex is open source and licensed under the GNU General Public License v3.
 * A copy of the license can be found here: https://www.gnu.org/licenses/gpl.text
 */

package c4.corpsecomplex.common.modules.inventory.helpers;

import c4.corpsecomplex.common.modules.inventory.enchantment.EnchantmentModule;
import c4.corpsecomplex.common.modules.inventory.InventoryModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Map;
import java.util.Random;

public final class DeathStackHelper {

    private static Random generator = new Random();

    private DeathStackHelper() {}

    public static ItemStack stackToStore(EntityPlayer player, ItemStack stack, boolean cfgStore) {

        boolean essential = isEssential(stack);
        boolean cursed = !essential && isCursed(stack);
        boolean store = ((cfgStore && !cursed) || essential);

        if (EnchantmentModule.registerEnchant) {
            int level = EnchantmentHelper.getEnchantmentLevel(EnchantmentModule.soulbound, stack);
            if (level != 0) {
                essential = handleSoulbound(stack, level);
                cursed = !essential && cursed;
                if (!store && essential) {
                    store = true;
                }
            }
        }

        if (cursed && InventoryModule.destroyCursed) {
            destroyStack(stack);
            return stack;
        }

        if (InventoryModule.dropLoss > 0 || InventoryModule.keptLoss > 0) {
            loseDurability(player, stack, store);
        }

        if (InventoryModule.dropDrain > 0 || InventoryModule.keptDrain > 0) {
            loseEnergy(stack, store);
        }

        if (stack.isEmpty()) { return ItemStack.EMPTY; }

        if (store) {
            if (!essential && InventoryModule.randomDrop > 0) {
                int keepAmount = stack.getCount();
                keepAmount -= randomlyDrop(stack);
                return stack.splitStack(keepAmount);
            } else {
                ItemStack stack1 = stack.copy();
                stack.setCount(0);
                return stack1;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static void destroyStack(ItemStack stack) {

        stack.setCount(0);
    }

    public static int randomlyDrop(ItemStack stack) {

        int dropAmount = 0;
        for (int i = 0; i < stack.getCount(); i++) {
            if (generator.nextDouble() < InventoryModule.randomDrop) {
                dropAmount++;
            }
        }

        return dropAmount;
    }

    public static void randomlyDestroy(ItemStack stack) {

        int destroyAmount = 0;
        for (int i = 0; i < stack.getCount(); i++) {
            if (generator.nextDouble() < InventoryModule.randomDestroy) {
                destroyAmount++;
            }
        }

        stack.shrink(destroyAmount);
    }

    public static void loseDurability (EntityPlayer player, ItemStack stack, boolean store) {

        if (!stack.isItemStackDamageable()) { return;}

        if (store) {
            stack.damageItem((int) Math.round(stack.getMaxDamage()* InventoryModule.keptLoss), player);
        } else {
            stack.damageItem((int) Math.round(stack.getMaxDamage()* InventoryModule.dropLoss), player);
        }
    }

    public static void loseEnergy (ItemStack stack, boolean store) {

        IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);

        if (energy == null) { return;}

        int energyToLose;

        if (store) {
            energyToLose = (int) Math.round(energy.getMaxEnergyStored() * InventoryModule.keptDrain);
        } else {
            energyToLose = (int) Math.round(energy.getMaxEnergyStored() * InventoryModule.dropDrain);
        }

        while (energyToLose > 0 && energy.getEnergyStored() > 0) {
            energyToLose -= energy.extractEnergy(energyToLose, false);
        }
    }

    public static boolean isEssential(ItemStack stack) {

        for (String s : InventoryModule.essentialItems) {

            ResourceLocation name = stack.getItem().getRegistryName();

            if (name == null) { continue;}

            if (s.equals(name.toString())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCursed(ItemStack stack) {

        if (EnchantmentHelper.hasVanishingCurse(stack)) {
            return true;
        }

        for (String s : InventoryModule.cursedItems) {

            ResourceLocation name = stack.getItem().getRegistryName();

            if (name == null) { continue;}

            if (s.equals(name.toString())) {
                return true;
            }
        }

        return false;
    }

    public static boolean handleSoulbound(ItemStack stack, int level) {

        double savePercent = EnchantmentModule.baseSave + EnchantmentModule.extraPerLevel * (level - 1);
        boolean activated = false;

        if (generator.nextDouble() < savePercent) {
            activated = true;
        }

        if (EnchantmentModule.levelDrop != 0 && activated) {
            if (generator.nextDouble() < EnchantmentModule.levelDrop) {
                level = Math.max(0, level-1);
            }

            Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(stack);
            enchMap.remove(EnchantmentModule.soulbound);

            if (level > 0) {
                enchMap.put(EnchantmentModule.soulbound, level);
            }

            EnchantmentHelper.setEnchantments(enchMap, stack);
        }

        return activated;
    }
}