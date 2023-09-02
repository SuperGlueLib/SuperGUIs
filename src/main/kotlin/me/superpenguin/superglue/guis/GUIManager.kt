package me.superpenguin.superglue.guis

import me.superpenguin.superglue.foundations.register
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

object GUIManager: Listener {

    private lateinit var plugin: JavaPlugin
    fun getPlugin() = runCatching { plugin }.getOrElse { throw UninitializedPropertyAccessException("You must call GUIManager.setup() in your onEnable!") }

    fun setup(plugin: JavaPlugin) {
        this.plugin = plugin
        this.register(plugin)
    }


    val openGUIs = HashMap<UUID, GUI>()

    fun hasOpenInventory(player: Player) = openGUIs.containsKey(player.uniqueId)
    fun getGUI(player: HumanEntity) = openGUIs[player.uniqueId]

    fun closeGUI(player: Player) {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { player.closeInventory() }, 1)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (!hasOpenInventory(event.whoClicked as Player)) return
        if (event.currentItem == null || event.currentItem!!.type.isAir) return
        getGUI(event.whoClicked)
            ?.also {
                if (event.clickedTopInventory() || !it.requiresClickTopInventory()) it.runClick(event.whoClicked as Player, event.currentItem!!, event)
                event.isCancelled = true
            }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent){
        getGUI(event.player)?.onClose(event.player as Player)
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        getGUI(event.player)?.onClose(event.player)
    }


}