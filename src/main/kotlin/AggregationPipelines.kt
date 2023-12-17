import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates.group
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.m3y.kformat.Table
import de.m3y.kformat.table
import kotlinx.coroutines.flow.collect
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ItemsStorageCount(
    @BsonId
    val id: ObjectId,
    val count: Int
)

suspend fun itemsAggregation(
    database: MongoDatabase
) : List<ItemsStorageCount> {
    val list = mutableListOf<ItemsStorageCount>()
    val collection = database.getCollection<Item>("items")
    val pipeline = listOf(
        group("\$storage_id", Accumulators.sum("count", 1))
    )
    val result = collection.aggregate<ItemsStorageCount>(pipeline)
    result.collect {
        list.add(it)
    }
    return list
}

fun outputAggregation(
    aggregation: List<ItemsStorageCount>
) {
    if (aggregation.isEmpty()) {
        println("No aggregation!")
        return
    }
    val string = StringBuilder()
    table {
        header("StorageId", "ItemsCount")
        for (item in aggregation) {
            row(item.id, item.count)
        }
        hints {
            alignment("StorageId", Table.Hints.Alignment.LEFT)
            alignment("ItemsCount", Table.Hints.Alignment.LEFT)
            borderStyle = Table.BorderStyle.SINGLE_LINE
        }
    }.render(string)
    println(string)
}


