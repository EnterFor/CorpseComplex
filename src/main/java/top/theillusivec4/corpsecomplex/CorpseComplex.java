/*
 * Copyright (c) 2017. <C4>
 *
 * This Java class is distributed as a part of Corpse Complex.
 * Corpse Complex is open source and licensed under the GNU General Public
 * License v3.
 * A copy of the license can be found here: https://www.gnu.org/licenses/gpl
 * .text
 */

package top.theillusivec4.corpsecomplex;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.modules.InventoryModule;

@Mod(CorpseComplex.MODID)
public class CorpseComplex {

  public static final String MODID = "corpsecomplex";
  public static final Logger LOGGER = LogManager.getLogger();

  public CorpseComplex() {
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    MinecraftForge.EVENT_BUS.register(new InventoryModule());
  }

  private void setup(final FMLCommonSetupEvent evt) {
    DeathStorageCapability.register();
    InventoryModule.STORAGE_ADDONS.forEach((modid, clazz) -> {
      if (ModList.get().isLoaded(modid)) {
        try {
          InventoryModule.STORAGE.add(clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
          LOGGER.error("Error trying to instantiate storage module for mod " + modid);
        }
      }
    });
  }
}