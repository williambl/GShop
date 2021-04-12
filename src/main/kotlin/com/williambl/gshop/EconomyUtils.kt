package com.williambl.gshop

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.module.currency.modelhandlers.BalanceHandler
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import java.math.BigDecimal

private val balanceHandler by lazy {
    GunpowderMod.instance.registry.getModelHandler(BalanceHandler::class.java)
}

fun ServerPlayerEntity.canBuy(amount: BigDecimal): Boolean {
    return balanceHandler.getUser(uuid).balance >= amount
}

fun ServerPlayerEntity.canSell(stack: ItemStack): Boolean {
    return inventory.method_7371(stack).let {
        it >= 0 && inventory.getStack(it).count >= stack.count
    }
}

fun ServerPlayerEntity.buy(stack: ItemStack, amount: BigDecimal) {
    balanceHandler.modifyUser(uuid) { it.apply { balance -= amount } }
    this.inventory.offerOrDrop(world, stack)
}

fun ServerPlayerEntity.sell(stack: ItemStack, amount: BigDecimal) {
    balanceHandler.modifyUser(uuid) { it.apply { balance += amount } }
    this.inventory.removeStack(this.inventory.method_7371(stack), stack.count)
}

fun ServerPlayerEntity.buy(amount: BigDecimal) {
    balanceHandler.modifyUser(uuid) { it.apply { balance -= amount } }
}

fun ServerPlayerEntity.sell(amount: BigDecimal) {
    balanceHandler.modifyUser(uuid) { it.apply { balance += amount } }
}