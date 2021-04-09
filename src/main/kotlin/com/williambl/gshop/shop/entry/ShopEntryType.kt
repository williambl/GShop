package com.williambl.gshop.shop.entry

import kotlin.reflect.KClass

enum class ShopEntryType(val entryClass: KClass<out ShopEntry>) {
    ITEM_STACK(ItemStackShopEntry::class)
}