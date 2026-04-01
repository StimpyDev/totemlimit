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
    private static final int TOTEM_LIMIT = 3;
    @Unique
    private static final int CRYSTAL_LIMIT = 32;

    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onInsert(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        
        if (inventory.player == null || inventory.player.getEntityWorld().isClient()) return;
        if (inventory.player.isCreative() || inventory.player.isSpectator()) return;

        if (!stack.isEmpty()) {
            if (stack.isOf(Items.TOTEM_OF_UNDYING) && inventory.count(Items.TOTEM_OF_UNDYING) >= TOTEM_LIMIT) {
                dropExcessItem(inventory, stack, "Totem", TOTEM_LIMIT);
                ci.setReturnValue(false);
            } else if (stack.isOf(Items.END_CRYSTAL) && inventory.count(Items.END_CRYSTAL) >= CRYSTAL_LIMIT) {
                dropExcessItem(inventory, stack, "End Crystal", CRYSTAL_LIMIT);
                ci.setReturnValue(false);
            }
        }
    }

    @Inject(method = "updateItems", at = @At("TAIL"))
    private void onUpdateItems(CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;

        if (inventory.player == null || inventory.player.getEntityWorld().isClient()) return;
        if (inventory.player.isCreative() || inventory.player.isSpectator()) return;

        checkAndDrop(inventory, Items.TOTEM_OF_UNDYING, TOTEM_LIMIT, "Totem");
        checkAndDrop(inventory, Items.END_CRYSTAL, CRYSTAL_LIMIT, "End Crystal");
    }

    @Unique
    private void checkAndDrop(PlayerInventory inventory, net.minecraft.item.Item item, int limit, String name) {
        int total = inventory.count(item);
        if (total > limit) {
            int toRemove = total - limit;
            for (int i = inventory.size() - 1; i >= 0 && toRemove > 0; i--) {
                ItemStack stack = inventory.getStack(i);
                if (stack.isOf(item)) {
                    int countInStack = stack.getCount();
                    if (countInStack <= toRemove) {
                        dropExcessItem(inventory, stack, name, limit);
                        toRemove -= countInStack;
                    } else {
                        ItemStack dropCopy = stack.split(toRemove);
                        dropExcessItem(inventory, dropCopy, name, limit);
                        toRemove = 0;
                    }
                }
            }
        }
    }

    @Unique
    private void dropExcessItem(PlayerInventory inventory, ItemStack stack, String itemName, int limit) {
        if (inventory.player == null || stack.isEmpty()) return;

        if (!TotemManager.isMessageOnCooldown(inventory.player.getUuid(), MESSAGE_COOLDOWN_MS)) {
            inventory.player.sendMessage(Text.literal("§d§l" + itemName + " limiet bereikt van " + limit), false);
        }
        
        ItemStack copy = stack.copy();
        stack.setCount(0); 
        inventory.player.dropItem(copy, false, false); 
    }
}
