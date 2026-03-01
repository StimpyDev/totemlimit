package com.example.mixin;

import com.example.TotemManager;
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

@Mixin(PlayerInventory.class)
public class TotemLimitMixin {

    @Unique
    private static final long MESSAGE_COOLDOWN_MS = 5000;
    @Unique
    private static final int TOTEM_LIMIT = 2;

    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onInsert(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        
        if (inventory.player != null && (inventory.player.isCreative() || inventory.player.isSpectator())) return;

        if (!stack.isEmpty() && stack.isOf(Items.TOTEM_OF_UNDYING)) {
            if (inventory.count(Items.TOTEM_OF_UNDYING) >= TOTEM_LIMIT) {
                dropExcessTotem(inventory, stack);
                ci.setReturnValue(false);
            }
        }
    }

    @Inject(method = "updateItems", at = @At("TAIL"))
    private void onUpdateItems(CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;

        if (inventory.player == null || inventory.player.getWorld().isClient) return;
        if (inventory.player.isCreative() || inventory.player.isSpectator()) return;

        int totalTotems = inventory.count(Items.TOTEM_OF_UNDYING);
        
        if (totalTotems > TOTEM_LIMIT) {
            int toRemove = totalTotems - TOTEM_LIMIT;
            
            for (int i = inventory.size() - 1; i >= 0 && toRemove > 0; i--) {
                ItemStack stack = inventory.getStack(i);
                if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                    dropExcessTotem(inventory, stack);
                    toRemove--;
                }
            }
        }
    }

    @Unique
    private void dropExcessTotem(PlayerInventory inventory, ItemStack stack) {
        if (inventory.player == null || stack.isEmpty()) return;

        if (!TotemManager.isMessageOnCooldown(inventory.player.getUuid(), MESSAGE_COOLDOWN_MS)) {
            inventory.player.sendMessage(Text.literal("§c§lTotemlimiet bereikt van " + TOTEM_LIMIT), false);
        }
        
        ItemStack copy = stack.copy();
        stack.setCount(0); 
        inventory.player.dropItem(copy, false, false); 
    }
}
