import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.m3y.kformat.Table
import de.m3y.kformat.table
import org.bson.types.ObjectId

suspend fun addSupply(
    database: MongoDatabase,
    supply: Supply
) {
    val collection = database.getCollection<Supply>("supplies")
    collection.insertOne(supply).also {
        println("Item added with id - ${it.insertedId}")
    }
    supply.items.forEach { item ->
        addStorageItem(database, item)
    }
}

suspend fun deleteSupply(
    database: MongoDatabase,
    supplyId: ObjectId
) {
    val collection = database.getCollection<Supply>("supplies")
    val filters = Filters.eq("_id", supplyId)
    collection.deleteOne(filters).also {
        println("Deleted ${it.deletedCount} lines!")
    }
}

suspend fun getSupplies(
    database: MongoDatabase
) : List<Supply> {
    val list = mutableListOf<Supply>()
    database.getCollection<Supply>("supplies")
        .find<Supply>()
        .collect {
            list.add(it)
        }
    return list
}

suspend fun getInputSupply(
    database: MongoDatabase
):Supply {
    val list = mutableListOf<Item>()
    print("Date (Year:Month:Day): ")
    val date = readln()
    print("How many items do you want to add to supply: ")
    val num = readln().toInt()
    repeat(num) {
        list.add(getInputItem(database))
    }
    return Supply(ObjectId(), date, num, list)
}

fun outputSupplies(supplies: List<Supply>) {
    if (supplies.isEmpty()) {
        println("Database has no supplies yet!")
        return
    }
    val string = StringBuilder()
    table {
        header("Id", "Date", "Amount", "Items")
        for (supply in supplies) {
            row(supply.id, supply.date, supply.amount, supply.items.joinToString(","))
        }
        hints {
            alignment("Id", Table.Hints.Alignment.LEFT)
            alignment("Date", Table.Hints.Alignment.LEFT)
            alignment("Amount", Table.Hints.Alignment.LEFT)
            alignment("Items", Table.Hints.Alignment.LEFT)
            borderStyle = Table.BorderStyle.SINGLE_LINE
        }
    }.render(string)
    println(string)
}

suspend fun getInputNewDate() : String {
    print("Write date (Year:Month:Day):")
    return readln()
}

suspend fun getInputSupplyId (
    database: MongoDatabase
) : ObjectId {
    println("Choose Supply Id: ")
    outputSupplies(getSupplies(database))
    print("Id: ")
    return ObjectId(readln())
}

suspend fun updateSupply (
    database: MongoDatabase,
    supplyId: ObjectId,
    date : String
) {
    val collection = database.getCollection<Supply>("supplies")
    val filters = Filters.eq("_id", supplyId)
    val updateParams = Updates.set("date", date)
    collection.updateOne(filters, updateParams).also {
        println("Modified lines ${it.modifiedCount}")
    }
}
