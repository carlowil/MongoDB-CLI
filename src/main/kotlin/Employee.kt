import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.m3y.kformat.Table
import de.m3y.kformat.table
import org.bson.types.ObjectId

suspend fun getStorageEmployees(
    database: MongoDatabase,
    storageId: ObjectId
):List<Employee> {
    val list = mutableListOf<Employee>()
    val queryParams = Filters.eq("info.storage_id", storageId)
    database.getCollection<Employee>("employees")
        .find(queryParams)
        .collect {
            list.add(it)
        }
    return list
}

suspend fun addStorageEmployee(
    database: MongoDatabase,
    employee: Employee
) {
    val collection = database.getCollection<Employee>("employees")
    collection.insertOne(employee).also {
        println("Item added with id - ${it.insertedId}")
    }
}

suspend fun deleteStorageEmployee(
    database: MongoDatabase,
    employeeId: ObjectId
) {
    val collection = database.getCollection<Employee>("employees")
    val filters = Filters.eq("_id", employeeId)
    collection.deleteOne(filters).also {
        println("Deleted ${it.deletedCount} lines!")
    }
}

fun outputStorageEmployees(employees: List<Employee>) {
    if (employees.isEmpty()) {
        println("This storage has no employees!")
        return
    }
    val string = StringBuilder()
    table {
        header("Id", "StorageId", "Name", "Phone", "Email")
        for (employee in employees) {
            val info = employee.info
            row(employee.id, info.storageId, info.name, info.phone, info.email)
        }
        hints {
            alignment("Id", Table.Hints.Alignment.LEFT)
            alignment("StorageId", Table.Hints.Alignment.LEFT)
            alignment("Name", Table.Hints.Alignment.LEFT)
            alignment("Phone", Table.Hints.Alignment.LEFT)
            alignment("Email", Table.Hints.Alignment.LEFT)
            borderStyle = Table.BorderStyle.SINGLE_LINE
        }
    }.render(string)
    println(string)
}

suspend fun getInputEmployee(database: MongoDatabase) : Employee {
    val storageId = getInputStorageId(database)
    print("Name: ")
    val name = readln()
    print("Tel: ")
    val phone = readln()
    print("Email: ")
    val mail = readln()
    return Employee(
        id = ObjectId(),
        info = EmployeeInfo(
            storageId = storageId,
            name = name,
            phone = phone,
            email = mail
        )
    )
}

suspend fun getInputEmployeeInfo(
    database: MongoDatabase
) : EmployeeInfo {
    val storageId = getInputStorageId(database)
    print("Name: ")
    val name = readln()
    print("Tel: ")
    val phone = readln()
    print("Email: ")
    val mail = readln()
    return EmployeeInfo(storageId, name, phone, mail)
}

suspend fun getInputEmployeeId(database: MongoDatabase) : ObjectId {
    outputStorageEmployees(
        getStorageEmployees(
            database,
            getInputStorageId(database)
        )
    )
    print("EmployeeId: ")
    return ObjectId(readln())
}


suspend fun updateEmployee(
    database: MongoDatabase,
    employeeId: ObjectId,
    info: EmployeeInfo
) {
    val collection = database.getCollection<Employee>("employees")
    val filters = Filters.eq("_id", employeeId)
    val updateParams = Updates.set("info", info)
    collection.updateOne(filters, updateParams).also {
        println("Modified lines ${it.modifiedCount}")
    }
}

