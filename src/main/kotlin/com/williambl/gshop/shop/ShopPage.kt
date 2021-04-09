package com.williambl.gshop.shop

import com.williambl.gshop.shop.entry.ItemStackShopEntry
import com.williambl.gshop.shop.entry.ShopEntry
import net.minecraft.item.ItemStack

data class ShopPage(val name: String, val icon: ItemStackShopEntry, val entries: List<ShopEntry>)
