import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Storage(
    @BsonId
    val id: ObjectId,
    val address: Address
)

data class Employee(
    @BsonId
    val id: ObjectId,
    val info: EmployeeInfo
)

data class EmployeeInfo(
    @BsonProperty("storage_id")
    val storageId: ObjectId,
    val name: String,
    val phone: String,
    val email: String
)

data class Item(
    @BsonId
    val id: ObjectId,
    @BsonProperty("storage_id")
    val storageId: ObjectId,
    val type: ItemType,
    val amount: Int
)


data class ItemType (
    val name: String,
    val mas: Double,
    val size: String
)

data class Address(
    val building: String,
    val street: String,
    val zipcode: String
)

data class Supply (
    @BsonId
    val id: ObjectId,
    val date: String,
    val amount: Int,
    val items: List<Item>
)