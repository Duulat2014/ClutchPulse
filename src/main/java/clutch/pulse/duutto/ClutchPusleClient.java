package clutch.pulse.duutto;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClutchPulseClient implements ClientModInitializer {
    private static boolean lowHealthAlert = false;
    private static boolean fallDangerAlert = false;
    private static String lowDurabilityInfo = null;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClutchPulse.LOGGER.info("ClutchPulse client initialized");
    }

    private void onClientTick(Minecraft client) {
        Player player = client.player;
        if (player == null || player.isSpectator()) {
            lowHealthAlert = false;
            fallDangerAlert = false;
            lowDurabilityInfo = null;
            return;
        }

        // Низкое здоровье (<= 3 сердец = 6.0 HP)
        lowHealthAlert = !player.isCreative() && player.getHealth() <= 6.0f;

        // Опасное падение
        boolean falling = !player.onGround()
                && !player.isInWater()
                && !player.onClimbable()
                && player.getDeltaMovement().y < -0.35;
        fallDangerAlert = falling && player.fallDistance > 6.0f;

        // Критическая прочность предмета в главной руке (<= 10%)
        lowDurabilityInfo = checkMainHandDurability(player);
    }

    private String checkMainHandDurability(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return null;
        }
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return null;
        }
        int remaining = maxDamage - stack.getDamageValue();
        if (remaining <= maxDamage * 0.10f) {
            return stack.getHoverName().getString() + " (" + remaining + ")";
        }
        return null;
    }

    public static boolean isLowHealthAlert() {
        return lowHealthAlert;
    }

    public static boolean isFallDangerAlert() {
        return fallDangerAlert;
    }

    public static String getLowDurabilityInfo() {
        return lowDurabilityInfo;
    }
}
