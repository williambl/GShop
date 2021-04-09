package com.williambl.gshop.configs

import com.williambl.gshop.shop.Shop
import com.williambl.gshop.shop.ShopPage
import com.williambl.gshop.shop.entry.ItemStackShopEntry
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier

data class GShopConfig(val shops: List<Shop> = listOf(
    Shop("default", listOf(
        ShopPage("page one", ItemStackShopEntry(ItemStack(Items.APPLE)), listOf(ItemStackShopEntry(ItemStack(Items.APPLE), 10, 10)))
    ))
))