import com.mongodb.MongoException
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.count
import org.bson.BsonInt64
import org.bson.Document
import java.util.Objects

suspend fun main(args: Array<String>) {
    handleApp()
}

suspend fun setupConnection(
    databaseName: String = "storages",
    connectionEnvVariable: String = "MONGODB_URI"
):MongoDatabase? {
    val connectionString = if(System.getenv(connectionEnvVariable) != null) {
        System.getenv(connectionEnvVariable)
    } else {
        "mongodb://LOGIN:PASSWORD@YOURS_IP_DOMEN/?retryWrites=true&w=majority" // your connection
    }

    val client = MongoClient.create(connectionString = connectionString)
    val database = client.getDatabase(databaseName)

    return try {
        val command = Document("ping", BsonInt64(1))
        database.runCommand(command)
        println("Pinged your deployment. You successfully connected to MongoDB!")
        database
    } catch (me: MongoException) {
        System.err.println(me)
        println("Bad connection :( Something wrong with db!")
        null
    }
}

suspend fun listAllCollections(database: MongoDatabase) {
    val count = database.listCollectionNames().count()
    println("Collection count: $count")
    print("Collection in this database are -----------> ")
    database.listCollectionNames().collect { print(" $it") }
    println()
}

suspend fun dropCollection(
    database: MongoDatabase,
    collectionName: String
) {
    try {
        database.getCollection<Objects>(collectionName).drop()
    } catch (e : MongoException) {
        System.err.println("There is no collection $collectionName")
    }
}

fun getHelp() {
    println("Help page:\n" +
            "- look <[storages, employees, items, supplies, storages_items]> " +
            "- you can look for storage, supplies," +
            "storage employees, storage items\n" +
            "- add <[storage, employee, item, supply]> - you can add a new storage, an employee " +
            "to storage, an item to storage, new supply\n" +
            "- del <[storage, employee, item, supply]> - you can delete storage, employee, item, supply\n" +
            "- update <[item, storage, supply, employee]> - you can update storage items\n" +
            "- exit - exit from program")
}

fun getInput() : List<String> {
    print("Input: ")
    return readln().split(" ")
}


suspend fun handleInput(
    input: List<String>,
    database: MongoDatabase
):Boolean {
    val flags = arrayOf("look", "add", "del", "update")
    if (input[0] == "exit") return true
    if (input.size > 2) println("Too much parametres! Try 'help'!")
    if (input.size == 1 && input[0] != "help" && input[0] in flags) {
        println("Try 'help'!")
        return false
    }
    when(input[0]) {
        flags[0] -> {
            when(input[1]) {
                "storages" -> {
                    outputStorages(getStorages(database))
                }
                "employees" -> {
                    outputStorageEmployees(
                        getStorageEmployees(
                            database,
                            getInputStorageId(database)
                        )
                    )
                }
                "items" -> {
                    outputStorageItems(
                        getStorageItems(
                            database,
                            getInputStorageId(database)
                        )
                    )
                }
                "supplies" -> {
                    outputSupplies(
                        getSupplies(database)
                    )
                }
                "storage_items" -> {
                    outputAggregation(
                        itemsAggregation(database)
                    )
                }
                else -> {
                    println("Second param is wrong!")
                }
            }
        }
        flags[1] -> {
            when(input[1]) {
                "storage" -> {
                    addStorage(database, getInputStorage())
                }
                "employee" -> {
                    addStorageEmployee(database, getInputEmployee(database))
                }
                "item" -> {
                    addStorageItem(database, getInputItem(database))
                }
                "supply" -> {
                    addSupply(database, getInputSupply(database))
                }
                else -> {
                    println("Second param is wrong!")
                }
            }
        }
        flags[2] -> {
            when(input[1]) {
                "storage" -> {
                    deleteStorage(database, getInputStorageId(database))
                }
                "employee" -> {
                    deleteStorageEmployee(
                        database,
                        getInputEmployeeId(database)
                    )
                }
                "item" -> {
                    deleteStorageItem(
                        database,
                        getInputItemId(database)
                    )
                }
                "supply" -> {
                    deleteSupply(
                        database,
                        getInputSupplyId(database)
                    )
                }
                else -> {
                    println("Second param is wrong!")
                }
            }
        }
        flags[3] -> {
            when(input[1]) {
                "item" -> {
                    updateStorageItem(
                        database,
                        getInputItemId(database),
                        getInputNewAmount()
                    )
                }
                "storage" -> {
                    updateStorage(
                        database,
                        getInputStorageId(database),
                        getInputNewAddress()
                    )
                }
                "supply" -> {
                    updateSupply(
                        database,
                        getInputSupplyId(database),
                        getInputNewDate()
                    )
                }
                "employee" -> {
                    updateEmployee(
                        database,
                        getInputEmployeeId(database),
                        getInputEmployeeInfo(database)
                    )
                }
                else -> {
                    println("Second param is wrong!")
                }
            }
        }
        "help" -> {
            getHelp()
        }
        else -> {
            println("Bad input!")
        }
    }

    return false
}

suspend fun handleApp() {
    setupConnection()?.also {
        while (true) {
            if(handleInput(getInput(), it)) return
        }
    }
}

