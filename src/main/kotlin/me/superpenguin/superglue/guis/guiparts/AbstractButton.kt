package me.superpenguin.superglue.guis.guiparts

import me.superpenguin.superglue.guis.GUI
import org.bukkit.inventory.ItemStack

abstract class AbstractButton(
    val id: Int,
    protected val itemstack: ItemStack,
) {
    abstract fun getItem(): ItemStack
    abstract fun onClick(action: (GUI.ClickData.() -> Unit)?): AbstractButton
}