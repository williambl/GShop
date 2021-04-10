package com.williambl.gshop.shop

import com.williambl.gshop.Screen
import com.williambl.gshop.shop.entry.ItemStackShopEntry
import com.williambl.gshop.shop.entry.ShopEntry
import io.github.gunpowder.api.builders.ChestGui
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.LiteralText

data class ShopCategory(val name: String, val icon: ItemStackShopEntry, val entries: List<ShopEntry>) {
    fun screen(previous: (() -> Screen)? = null): Screen = { player -> {
        clearButtons()
        entries.forEachIndexed { i, entry ->
            button(i, 0, entry.icon) { actionType, container -> entry.screen {screen(previous)}(player)(container) }
        }

        if (previous != null) {
            button(8, 5, Items.FEATHER.defaultStack.setCustomName(LiteralText("Back"))) { _, container -> previous()(player)(container) }
        }
    }}
}
