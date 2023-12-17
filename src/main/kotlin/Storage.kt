import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.m3y.kformat.Table
import de.m3y.kformat.table
import org.bson.types.ObjectId

suspend fun deleteStorage(
    database: MongoDatabase,
    storageId: ObjectId
) {
    val collection = database.getCollection<Storage>("storages")
    val filters = Filters.eq("_id", storageId)
    collection.deleteOne(filters).also {
        println("Deleted ${it.deletedCount} lines!")
    }
}

suspend fun addStorage(
    database: MongoDatabase,
    storage: Storage
){
    val collection = database.getCollection<Storage>("storages")
    collection.insertOne(storage).also {
        println("Item added with id - ${it.insertedId}")
    }
}

suspend fun getStorages(database: MongoDatabase) : List<Storage> {
    val list = mutableListOf<Storage>()
    database.getCollection<Storage>("storages")
        .find<Storage>()
        .collect {
            list.add(it)
        }
    return list
}

fun outputStorages(storages: List<Storage>) {
    if (storages.isEmpty()) {
        println("Database has no storages!")
        return
    }
    val string = StringBuilder()
    table {
        header("Id", "Building", "Street", "Zipcode")
        for (storage in storages) {
            val address = storage.address
            row(storage.id, address.building, address.street, address.zipcode)
        }
        hints {
            alignment("Id", Table.Hints.Alignment.LEFT)
            alignment("Building", Table.Hints.Alignment.LEFT)
            alignment("Street", Table.Hints.Alignment.LEFT)
            alignment("Zipcode", Table.Hints.Alignment.LEFT)
            borderStyle = Table.BorderStyle.SINGLE_LINE
        }
    }.render(string)
    println(string)
}

fun getInputStorage() : Storage {
    print("Building: ")
    val building = readln()
    print("Street: ")
    val street = readln()
    print("Zipcode: ")
    val zipcode = readln()
    return Storage(ObjectId(), Address(building, street, zipcode))
}

suspend fun getInputStorageId(database: MongoDatabase) : ObjectId {
    println("Choose Storage Id: ")
    outputStorages(getStorages(database))
    print("Id: ")
    return ObjectId(readln())
}

suspend fun getInputNewAddress() : Address {
    print("Building: ")
    val building = readln()
    print("Street: ")
    val street = readln()
    print("Zipcode: ")
    val zipcode = readln()
    return Address(building, street, zipcode)
}

suspend fun updateStorage(
    database: MongoDatabase,
    storageId: ObjectId,
    address: Address
) {
    val collection = database.getCollection<Storage>("storages")
    val filters = Filters.eq("_id", storageId)
    val updateParams = Updates.set("address", address)
    collection.updateOne(filters, updateParams).also {
        println("Modified lines ${it.modifiedCount}")
    }
}



