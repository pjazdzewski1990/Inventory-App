package io.scalac.inventory.db.activate

import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage

object InventoryContext extends ActivateContext {

  val storage = new TransientMemoryStorage

  //	val storage = new PooledJdbcRelationalStorage {
  //		val jdbcDriver = "org.postgresql.Driver"
  //		val user = Some("postgres")
  //		val password = None
  //		val url = "jdbc:postgresql://127.0.0.1/postgres"
  //		val dialect = postgresqlDialect
  //	}
}