/*
 * MIT License
 *
 * Copyright (c) 2021 Will BL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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