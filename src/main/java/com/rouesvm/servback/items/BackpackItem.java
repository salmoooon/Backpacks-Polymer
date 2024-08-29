package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.BackpackGui;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BackpackItem extends Item implements PolymerItem {

    private final int slots;
    private final String name;


    public BackpackItem(String id) {
        super(new Settings().maxCount(1));
        this.slots = 0;
        this.name = id;
    }

    public BackpackItem(String id, int slots) {
        super(new Settings().maxCount(1));
        this.slots = slots;
        this.name = id;
    }
    
    public SimpleGui getGui(ServerPlayerEntity player, ItemStack stack) {
        return new BackpackGui(player, stack, getInventory(stack));
    }

    public Inventory getInventory(ItemStack stack) {
        ContainerComponent component = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

        DefaultedList<ItemStack> list = DefaultedList.ofSize(slots, ItemStack.EMPTY);
        component.copyTo(list);

        return new SimpleInventory(list.toArray(ItemStack[]::new));
    }

    @Override
    public Text getName() {
        return Text.of(this.name + " Backpack");
    }

    public Text getType() {
        return Text.of(this.name);
    }

        @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getStackInHand(hand);

        var cast = player.raycast(5,0,false);
        if (cast.getType() == HitResult.Type.BLOCK)
            return TypedActionResult.pass(stack);
        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return TypedActionResult.pass(stack);
        if (player.isSneaking())
            return TypedActionResult.pass(stack);

        getGui(serverPlayer, stack);

        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity player))
            return ActionResult.PASS;
        if (player.isSneaking())
            return ActionResult.PASS;

        getGui(player, context.getStack());

        return ActionResult.PASS;
    }

    public Item getPolymerItem() {
        return Items.LEATHER;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return getPolymerItem();
    }

    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
        ItemStack stack = PolymerItemUtils.createItemStack(itemStack, tooltipType, lookup, player);

        LoreComponent loreComponent = new LoreComponent(List.of(this.getType()));

        ComponentMap.Builder newComp = ComponentMap.builder();
        newComp.add(DataComponentTypes.CUSTOM_NAME, this.getName());
        newComp.add(DataComponentTypes.LORE, loreComponent);
        stack.applyComponentsFrom(newComp.build());

        return stack;
    }

}
