package com.williambl.gshop.configs

import com.williambl.gshop.shop.Shop
import com.williambl.gshop.shop.ShopCategory
import com.williambl.gshop.shop.entry.ItemStackShopEntry
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import java.math.BigDecimal

data class GShopConfig(val shops: List<Shop> = listOf(
    Shop("default", listOf(
        ShopCategory("page one", ItemStackShopEntry(ItemStack(Items.APPLE)), listOf(ItemStackShopEntry(ItemStack(Items.APPLE), BigDecimal.TEN, BigDecimal.TEN)))
    ))
))