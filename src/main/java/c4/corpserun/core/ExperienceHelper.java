package c4.corpserun.core;

import c4.corpserun.config.values.ConfigBool;
import c4.corpserun.config.values.ConfigFloat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;

public final class ExperienceHelper {

    public static void restoreXP(EntityPlayer player, EntityPlayer oldPlayer) {
        player.addExperience(oldPlayer.experienceTotal);
    }

    public static void setExperiencesValues(LivingExperienceDropEvent e) {

        EntityPlayer player = (EntityPlayer) e.getEntityLiving();
        int dropXP = Math.round(player.experienceTotal * (ConfigFloat.XP_LOSS_PERCENT.getValue()) * (ConfigFloat.XP_RECOVER_PERCENT.getValue()));
        int keptXP = Math.round(player.experienceTotal * (1 - ConfigFloat.XP_LOSS_PERCENT.getValue()));
        e.setDroppedExperience(dropXP);
        player.experienceLevel = 0;
        player.experience = 0.0F;
        player.experienceTotal = 0;
        addExperience(player, keptXP);
    }

    private static void addExperience(EntityPlayer player, int amount) {

        int i = Integer.MAX_VALUE - player.experienceTotal;

        if (amount > i) {
            amount = i;
        }

        player.experience += (float)amount / (float)player.xpBarCap();

        for (player.experienceTotal += amount; player.experience >= 1.0F; player.experience /= (float)player.xpBarCap()) {
            player.experience = (player.experience - 1.0F) * (float)player.xpBarCap();
            addExperienceLevel(player,1);
        }
    }

    private static void addExperienceLevel(EntityPlayer player, int levels) {

        player.experienceLevel += 1;

        if (player.experienceLevel < 0) {
            player.experienceLevel = 0;
            player.experience = 0.0F;
            player.experienceTotal = 0;
        }
    }

}
