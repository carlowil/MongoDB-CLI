import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.m3y.kformat.Table
import de.m3y.kformat.table
import org.bson.types.ObjectId

suspend fun getStorageItems(
    database: MongoDatabase,
    storageId: ObjectId
) : List<Item> {
    val list = mutableListOf<Item>()
    val queryParams = Filters.eq("storage_id", storageId)
    database.getCollection<Item>("items")
        .find(queryParams)
        .collect {
            list.add(it)
        }
    return list
}

suspend fun deleteStorageItem(
    database: MongoDatabase,
    itemId: ObjectId
) {
    val collection = database.getCollection<Item>("items")
    val filters = Filters.eq("_id", itemId)
    collection.deleteOne(filters).also {
        println("Deleted ${it.deletedCount} lines!")
    }
}

suspend fun updateStorageItem(
    database: MongoDatabase,
    itemId: ObjectId,
    newAmount : Int
) {
    val collection = database.getCollection<Item>("items")
    val filters = Filters.eq("_id", itemId)
    val updateParams = Updates.set("amount", newAmount)
    collection.updateOne(filters, updateParams).also {
        println("Modified lines ${it.modifiedCount}")
    }
}

fun outputStorageItems(items: List<Item>) {
    if (items.isEmpty()) {
        println("This storage has no items!")
        return
    }
    val string = StringBuilder()
    table {
        header("Id", "StorageId", "Name", "Mas", "Size", "Amount")
        for (item in items) {
            val type = item.type
            row(item.id, item.storageId, type.name, type.mas, type.size, item.amount)
        }
        hints {
            alignment("Id", Table.Hints.Alignment.LEFT)
            alignment("StorageId", Table.Hints.Alignment.LEFT)
            alignment("Name", Table.Hints.Alignment.LEFT)
            alignment("Mas", Table.Hints.Alignment.LEFT)
            alignment("Size", Table.Hints.Alignment.LEFT)
            alignment("Amount", Table.Hints.Alignment.LEFT)
            precision("Mas", 2)
            borderStyle = Table.BorderStyle.SINGLE_LINE
        }
    }.render(string)
    println(string)
}

suspend fun addStorageItem(
    database: MongoDatabase,
    item: Item
) {
    val collection = database.getCollection<Item>("items")
    collection.insertOne(item).also {
        println("Item added with id - ${it.insertedId}")
    }
}

suspend fun getInputItem(database: MongoDatabase) : Item {
    val storageId = getInputStorageId(database)
    print("Name: ")
    val name = readln()
    print("Mas (Double): ")
    val mas = readln()
    print("Size: ")
    val size = readln()
    print("Amount (Int): ")
    val amount = readln()
    return Item(
        id = ObjectId(),
        storageId = storageId,
        type = ItemType(
            name = name,
            mas = mas.toDouble(),
            size = size
        ),
        amount = amount.toInt()
    )
}

suspend fun getInputItemId(database: MongoDatabase) : ObjectId {
    outputStorageItems(
        getStorageItems(
            database,
            getInputStorageId(database)
        )
    )
    print("ItemId: ")
    return ObjectId(readln())
}

fun getInputNewAmount() : Int {
    print("New amount: ")
    return readln().toInt()
}