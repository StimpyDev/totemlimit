\package com.example.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Unique
    public static void removePlayerData(UUID uuid) {
        lastMessageTime.remove(uuid);
    }

    @Inject(method = "insertStack", at = @At("HEAD"), cancellable = true)
    private void onInsert(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        if (!stack.isEmpty() && stack.isOf(Items.TOTEM_OF_UNDYING)) {
            if (shouldEnforceLimit((PlayerInventory) (Object) this)) {
                dropExcessTotem((PlayerInventory) (Object) this, stack);
                ci.setReturnValue(false);
                ci.cancel();
            }
        }
    }

    @Inject(method = "updateItems", at = @At("TAIL"))
    private void onUpdateItems(CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        if (inventory.player != null && inventory.count(Items.TOTEM_OF_UNDYING) > 2) {
            // If they somehow bypassed insertion, find the extra and drop it
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (stack.isOf(Items.TOTEM_OF_UNDYING) && inventory.count(Items.TOTEM_OF_UNDYING) > 2) {
                    dropExcessTotem(inventory, stack);
                }
            }
        }
    }

    @Unique
    private boolean shouldEnforceLimit(PlayerInventory inventory) {
        return inventory.count(Items.TOTEM_OF_UNDYING) >= 2;
    }

    @Unique
    private void dropExcessTotem(PlayerInventory inventory, ItemStack stack) {
        if (inventory.player == null) return;

        UUID uuid = inventory.player.getUuid();
        long now = System.currentTimeMillis();
        if (now - lastMessageTime.getOrDefault(uuid, 0L) > MESSAGE_COOLDOWN_MS) {
            inventory.player.sendMessage(Text.literal("§c§lTotemlimiet bereikt van 2"), false);
            lastMessageTime.put(uuid, now);
        }

        inventory.player.dropItem(stack.copy(), false);
        stack.setCount(0);
    }
}
