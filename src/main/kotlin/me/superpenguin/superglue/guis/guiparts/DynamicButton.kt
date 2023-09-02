package me.superpenguin.superglue.guis.guiparts

import me.superpenguin.superglue.guis.GUI
import org.bukkit.inventory.ItemStack

class DynamicButton(
    id: Int,
    private val generateItem: () -> ItemStack,
    onClick: (GUI.ClickData.() -> Unit)? = null,
    var updateOnClick: Boolean = true
): Button(id, generateItem.invoke(), onClick) {

    override fun getItem() = generateItem.invoke().applyID(id)

    // Builder methods
    override fun onClick(action: (GUI.ClickData.() -> Unit)?) = apply { onClick = action }
    fun updateOnClick(shouldUpdate: Boolean = true) = apply { updateOnClick = shouldUpdate }

}
