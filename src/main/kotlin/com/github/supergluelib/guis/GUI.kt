package com.github.supergluelib.guis

import com.github.supergluelib.foundation.connectToBungeeServer
import com.github.supergluelib.foundation.isValid
import com.github.supergluelib.foundation.toColor
import com.github.supergluelib.foundation.util.ItemBuilder
import com.github.supergluelib.guis.guiparts.Button
import com.github.supergluelib.guis.guiparts.Button.Companion.getId
import com.github.supergluelib.guis.guiparts.Button.Companion.isButton
import com.github.supergluelib.guis.guiparts.DynamicButton
import com.github.supergluelib.guis.types.CloseEvent
import com.github.supergluelib.guis.types.ForceKeepOpen
import com.github.supergluelib.guis.types.OpenEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.scheduler.BukkitTask
import java.util.*

/*
TODO
add a re-open method which refreshes and re-opens
add a close-for-all method which closes the inventory for all viewers, safely.
ability to set dynamic button to multiple slots, ties in with above ^
setLiveButton -- Polls the function every second until the inventory is closed.

---- PAGE SUPPORT ----
 */

abstract class GUI {
    private var inventory: Inventory? = null

    private var backslot: Int? = null
    private var backgui: GUI? = null
    val settings = Settings()

    /**
     * @param requireTopInventory Require the top inventory to be clicked before running any on-click or button code.
     * @param cancelAutomatically Should all clicks be cancelled by default
     * @param notCancelledSlots Raw slots that will not cause the event to be automatically cancelled regardless of [cancelAutomatically]
     * @param shouldCancel Overrides all the other settings regarding whether to cancel an event when not null.
     */
    data class Settings(
        var requireTopInventory: Boolean = true,
        var cancelAutomatically: Boolean = true,
        var notCancelledSlots: List<Int> = listOf(),
        var shouldCancel: (ClickData.() -> Boolean)? = null
    ) {
        /**
         * Determines whether a click event will be cancelled based on the GUI settings.
         */
        fun shouldCancel(click: ClickData) = shouldCancel?.invoke(click) ?:
            (requireTopInventory && !click.event.clickedTopInventory()) || (cancelAutomatically && click.event.rawSlot !in notCancelledSlots)
    }

    // Builder methods
    fun setBackButton(slot: Int, gui: GUI) = apply {
        this.backslot = slot
        this.backgui = gui
    }

    // Manager methods
    fun open(player: Player) = Bukkit.getScheduler().runTaskLater(GUIManager.getPlugin(), Runnable {
        navigating.add(player.uniqueId) // Allows player to use navigation buttons while being forced into an inventory.
        if (inventory == null) inventory = getInventory()
        player.openInventory(inventory!!)
        navigating.remove(player.uniqueId)
        GUIManager.openGUIs[player.uniqueId] = this
        if (this is OpenEvent) this.onOpen(player)
    }, 1)

    internal fun onClose(player: Player) {
        // Usually only run if the player closes inventory manually.
        if (this is ForceKeepOpen && !canClose(player) && !navigating.contains(player.uniqueId)) open(player).also { return }  // Re-opens gui if not allowed to close
//        runOnClose(player)
        GUIManager.openGUIs.remove(player.uniqueId)
        if (inventory?.viewers?.size == 1) {
            inventory = null
            temporaryTasks.forEach { it.cancel() }
            temporaryTasks.clear()
        }
        if (this is CloseEvent) this.whenClosed(player)
    }

    private fun getInventory(): Inventory {
        buttons.clear()
        val inv = generateInventory()
        if (backslot != null) inv.setItem(backslot!!, RETURN)
        return inv
    }

    // manager methods
    private fun isBackButton(item: ItemStack) = item.itemMeta?.localizedName?.equals("return") == true

    internal fun runClick(clickdata: ClickData) {
        val item = clickdata.item
        val player = clickdata.player
        val event = clickdata.event
        if (isBackButton(item) && this.backgui != null) backgui!!.open(player).also { return }
        if (item.isButton()) {
            val button = item.getButton() ?: throw NullPointerException("Found an untracked button in a gui, this is usually a bug, join the discord for support")
            button.onClick?.invoke(clickdata)
            if (button is DynamicButton && button.updateOnClick) event.clickedInventory?.setItem(event.slot, button.getItem())
            return
        }
        onClick(clickdata)
    }

    // Public methods

    // These are disabled for now because they don't work all that great
/*    *//**
     * Refreshes the inventory value by invalidating the inventory of all current viewers by overriding the existing inventory.
     * Will only take effect on re-open, for an instant effect, use #refreshContents
     *
     * @see refreshContents
     *//*
    fun refresh() = apply { inventory = getInventory() }

    *//**
     * - Regenerates and broadcasts an inventory update to all current viewers.
     * - Is optimised to reduce flickering on high ping by only broadcasting changes to modified items
     * - If using a dynamically sized inventory use #refresh() and then re-open the inventory.
     * - You should also use #refresh() and re-open if the inventory contains buttons that are supplied outside of the generateInventory method, such as in the onOpen
     *
     * @see refresh
     *//*
    @Deprecated("This is highly highly unstable and not recommenkded.")
    fun refreshContents() {
        if (inventory == null) return
        val newinv = getInventory()
        for (i in 0 until inventory!!.size) {
            val existingitem = inventory!!.getItem(i)
            val newitem = newinv.getItem(i)
            if ((existingitem == null && newitem != null) || (existingitem != null && newitem == null)) {
                inventory!!.setItem(i, newitem)
            } else if (newitem != null && existingitem != null) {
                if (existingitem != newitem) inventory!!.setItem(i, newitem)
            }
        }
    }*/

    // Abstract methods
    protected abstract fun generateInventory(): Inventory

    /**
     * - Run when a player clicks in the inventory
     * - This will not run when a player clicks on a button
     * - Overriding this method is optional
     */
    protected open fun onClick(click: ClickData) {}

    data class ClickData(
        val player: Player,
        val item: ItemStack,
        val event: InventoryClickEvent,
        val clickType: ClickType = event.click,
        val meta: ItemMeta? = item.itemMeta,
        val locname: String? = meta?.localizedName?.ifEmpty { null },
        val pdc: PersistentDataContainer? = meta?.persistentDataContainer
    )

    // Utility methods
    fun createInventory(name: String, size: Int, display: Inventory.() -> Unit) = Bukkit.createInventory(null, size, name.toColor()).apply(display)
    fun easyGUI(name: String, size: Int, inventory: Inventory.() -> Unit) = object: GUI() {
        override fun generateInventory(): Inventory {
            return createInventory(name, size, inventory)
        }
    }
    fun closeForAllViewers() = inventory?.viewers?.forEach { GUIManager.closeGui(it as Player) }
    fun closeFor(player: Player) = GUIManager.closeGui(player)

    // Inventory creation utilities
    private val buttons = HashMap<Int, Button>()

    private fun ItemStack.getButton() = buttons[getId()]

    /**
     * Creates and places a [Button]
     *
     * @param action The action to execute **when this button is clicked**
     */
    protected fun Inventory.setButton(slot: Int, item: ItemStack, action: ClickData.() -> Unit): Button {
        if (!item.isValid()) throw NullPointerException("Invalid Item found in a GUI at slot $slot")
        val key = buttons.size
        val button = Button(key, item, action)
        buttons[key] = button
        setItem(slot, button.getItem())
        return button
    }

    /**
     * Creates and places multiple individual [Button]s at all the designated slots.
     *
     * @param action The action to execute **when these buttons are clicked**
     */
    protected fun Inventory.setButtons(item: ItemStack, vararg slots: Int, action: ClickData.() -> Unit): List<Button> {
        if (!item.isValid()) throw NullPointerException("Invalid Item passed into `setButtons` method for slots ${slots.joinToString()}")
        var key = buttons.size
        val newButtons = mutableListOf<Button>()
        for (slot in slots) {
            val button = Button(key, item, action)
            buttons[key] = button
            newButtons.add(button)
            setItem(slot, item)
            key++
        }
        return newButtons
    }

    /**
     * Creates and places a [DynamicButton]
     */
    protected fun Inventory.setDynamicButton(slot: Int, item: () -> ItemStack): DynamicButton {
        val button = DynamicButton(buttons.size, item)
        buttons[button.id] = button
        val stack = button.getItem()
        if (!stack.isValid()) throw NullPointerException("Invalid Item found in a GUI at slot $slot")
        setItem(slot, button.getItem())
        return button
    }

    /**
     * Creates and places a GUI Button, otherwise known as a navigation button.
     * Clicking on this button opens the [gui]
     * @param slot the slot to place this button at
     * @param item the item to put in the slot
     * @param gui the gui to take the player too
     */
    protected fun Inventory.setGUIButton(slot: Int, item: ItemBuilder, gui: () -> GUI) = setGUIButton(slot, item.build(), gui)
    /**
     * Creates and places a GUI Button, otherwise known as a navigation button.
     * Clicking on this button opens the [gui]
     * @param slot the slot to place this button at
     * @param item the item to put in the slot
     * @param gui the gui to take the player too
     */
    protected fun Inventory.setGUIButton(slot: Int, item: ItemStack, gui: () -> GUI) = setButton(slot, item) { gui.invoke().open(player) }

    /**
     * Create a new GUI class on the spot!
     * @param slot the slot to place the button in
     * @param item the item display of the button
     * @param name the name of the new GUI's inventory
     * @param size the size of the new GUI's inventory
     */
    protected fun Inventory.setGUIButton(slot: Int, item: ItemStack, name: String, size: Int, display: Inventory.() -> Unit) =
        setGUIButton(slot, item) { easyGUI(name, size, display) }

    private val temporaryTasks = ArrayList<BukkitTask>()

    /**
     * Temporarily sets this slot to the temporary [item] passed and then sets it back to the itemstack present when it was clicked.
     *
     * @param slot the slot to change
     * @param ticks the amount of ticks to change the item for
     * @param item the item to change this slot to
     */
    protected fun Inventory.setTemporaryButton(slot: Int, ticks: Int, item: ItemStack) {
        val old = getItem(slot)
        setItem(slot, item)
        temporaryTasks.add(Bukkit.getScheduler().runTaskLater(GUIManager.getPlugin(), Runnable {
            if( this.viewers.size == 0 ) return@Runnable
            setItem(slot, old)
        }, ticks.toLong()))
    }

    /**
     * When clicked, connects the player to the proxy server named [serverName]
     *
     * @param slot the slot to put the item in
     * @param item the item to set the slot to
     * @param serverName the name of the server to connect the player to
     */
    protected fun Inventory.setServerConnectionButton(slot: Int, item: ItemStack, serverName: String): Button {
        return setButton(slot, item) {
            player.connectToBungeeServer(GUIManager.getPlugin(), serverName)
        }
    }

    // Cached objects
    companion object {
        val AIR = ItemStack(Material.AIR)
        val RETURN = ItemBuilder(Material.BARRIER).name("&c&lBACK").locname("return").build()

        private var navigating = HashSet<UUID>()
    }
}
