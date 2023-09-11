package com.github.supergluelib.guis.templates

import com.github.supergluelib.guis.GUI
import com.github.supergluelib.guis.GUIManager
import com.github.supergluelib.guis.Panes
import com.github.supergluelib.guis.setColumns
import com.github.supergluelib.guis.types.CloseEvent
import me.superpenguin.superglue.foundations.toColor
import me.superpenguin.superglue.foundations.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * An easy-to-use confirmation GUI which allows the player to either click "confirm" or "cancel"
 * @param onSelect The action to run once the player has made a selection. The function boolean representing
 *      true if they confirmed, and false if they cancelled or closed the inventory.
 *  @param lore the lore of the center 9 items represented by black stained panes, they act as the neutral item,
 *      clarifying what the player is confirming.
 */
class ConfirmationGUI(val lore: List<String>? = null, val onSelect: (Boolean) -> Unit): GUI(), CloseEvent {
    var closeOnClick = true

    override fun generateInventory() = createInventory("&aConfirm", 27) {
        val middlepane = ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, "&r")
            .lore((lore ?: listOf()).map(String::toColor))
            .build()

        setColumns(Panes.LIME, 0, 1, 2)
        setColumns(middlepane, 3, 4, 5)
        setColumns(Panes.RED, 6, 7, 8)

        setButton(10, ItemBuilder(Material.GREEN_WOOL, "&a&lCONFIRM").build()) {
            onSelect.invoke(true)
            if (closeOnClick) closeForAllViewers()
        }

        setButton(16, ItemBuilder(Material.RED_WOOL, "&c&lCANCEL").build()) {
            onSelect.invoke(false)
            if (closeOnClick) closeForAllViewers()
        }
    }

    override fun whenClosed(player: Player) {
        onSelect.invoke(false)
    }
}