package com.example.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerInventory.class)
public class TotemLimitMixin {
    
    @Unique
    private static final Map<UUID, Long> lastMessageTime = new HashMap<>();
    
    @Unique
    private static final long MESSAGE_COOLDOWN_MS = 5000;

    @Inject(method = "insertStack", at = @At("HEAD"), cancellable = true)
    private void preventTotemExploits(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;

        if (inventory.player == null) return;
        if (stack.getItem() != Items.TOTEM_OF_UNDYING) return;

        int currentTotems = inventory.count(Items.TOTEM_OF_UNDYING);
        
        if (currentTotems >= 2) {
            UUID playerUuid = inventory.player.getUuid();
            long currentTime = System.currentTimeMillis();
            Long lastTime = lastMessageTime.get(playerUuid);
            
            if (lastTime == null || currentTime - lastTime > MESSAGE_COOLDOWN_MS) {
                inventory.player.sendMessage(
                    Text.literal("§c§lTotemlimiet bereikt van 2"),
                    false
                );
                lastMessageTime.put(playerUuid, currentTime);
            }
            
            // Drop the item and cancel insertion
            inventory.player.dropItem(stack, false);
            stack.setCount(0);
            ci.setReturnValue(false);
            ci.cancel();
        }
    }
}
