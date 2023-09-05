package com.github.supergluelib.guis.guiparts

import com.github.supergluelib.guis.GUI
import org.bukkit.inventory.ItemStack

abstract class AbstractButton(
    val id: Int,
    protected val itemstack: ItemStack,
) {
    abstract fun getItem(): ItemStack
    abstract fun onClick(action: (GUI.ClickData.() -> Unit)?): AbstractButton
}