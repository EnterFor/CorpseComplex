/*
 * Copyright (c) 2017. <C4>
 *
 * This Java class is distributed as a part of Corpse Complex.
 * Corpse Complex is open source and licensed under the GNU General Public
 * License v3.
 * A copy of the license can be found here: https://www.gnu.org/licenses/gpl
 * .text
 */

package c4.corpsecomplex.common.modules.inventory;

import c4.corpsecomplex.common.modules.inventory.capability.IDeathInventory;
import c4.corpsecomplex.common.modules.inventory.helpers.DeathStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;

public class InventoryHandler extends DeathStackHandler {

  private static final String MOD_ID = "vanilla";
  private InventoryPlayer inventoryPlayer;

  public InventoryHandler(EntityPlayer player) {
    super(player, MOD_ID);
    inventoryPlayer = player.inventory;
    setSize(inventoryPlayer.getSizeInventory());
  }

  public ItemStack getStackInSlot(int slot) {

    return inventoryPlayer.getStackInSlot(slot);
  }

  public boolean checkToStore(int slot) {

    if (slot == inventoryPlayer.currentItem) {
      return InventoryModule.keepMainhand;
    } else if (slot < 9) {
      return InventoryModule.keepHotbar;
    } else if (slot >= 9 && slot < 36) {
      return InventoryModule.keepMainInventory;
    } else if (slot >= 36 && slot < 40) {
      return InventoryModule.keepArmor;
    } else if (slot == 40) {
      return InventoryModule.keepOffhand;
    }
    return false;
  }

  public void retrieveInventory(IDeathInventory oldDeathInventory) {

    NBTTagCompound nbt = oldDeathInventory.getStorage(MOD_ID);
    if (nbt == null) {
      return;
    }

    storage.deserializeNBT(nbt);

    for (int slot = 0; slot < storage.getSlots(); slot++) {
      ItemStack stack = storage.getStackInSlot(slot);
      if (stack.isEmpty()) {
        continue;
      }

      if (!inventoryPlayer.getStackInSlot(slot).isEmpty()) {
        ItemHandlerHelper.giveItemToPlayer(inventoryPlayer.player, stack);
      } else {
        inventoryPlayer.setInventorySlotContents(slot, stack);
      }
    }
  }
}