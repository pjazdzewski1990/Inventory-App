package io.scalac.inventory.db.activate

import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage

object InventoryContext extends ActivateContext {

  val storage = new TransientMemoryStorage
}