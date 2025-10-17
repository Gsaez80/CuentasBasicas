package cl.tuapp.calculocuentas.data

import androidx.room.*

enum class BillType { LUZ, AGUA }

@Entity(tableName="houses")
data class House(@PrimaryKey(autoGenerate=true) val id:Int=0, val name:String, val type:BillType)

@Entity(tableName="entries",
  foreignKeys=[ForeignKey(entity=House::class,parentColumns=["id"],childColumns=["houseId"],onDelete=ForeignKey.CASCADE)],
  indices=[Index("houseId")]
)
data class Entry(
  @PrimaryKey(autoGenerate=true) val id:Long=0,
  val houseId:Int, val ts:Long,
  val genPrev:Double, val genCurr:Double, val intPrev:Double, val intCurr:Double, val total:Double,
  val consG:Double, val consI:Double, val consC:Double, val prop:Double, val montoInt:Double, val montoCom:Double
)

@Dao interface HouseDao {
  @Query("SELECT * FROM houses WHERE type=:type ORDER BY id ASC") suspend fun byType(type:BillType):List<House>
  @Insert suspend fun insertAll(vararg houses:House)
}
@Dao interface EntryDao {
  @Query("SELECT * FROM entries WHERE houseId=:houseId ORDER BY ts DESC") suspend fun byHouse(houseId:Int):List<Entry>
  @Insert suspend fun insert(entry:Entry)
  @Query("DELETE FROM entries WHERE id=:id") suspend fun delete(id:Long)
}
@Database(entities=[House::class, Entry::class], version=1)
@TypeConverters(Converters::class)
abstract class AppDatabase:RoomDatabase(){
  abstract fun houseDao():HouseDao
  abstract fun entryDao():EntryDao
}
class Converters{
  @TypeConverter fun toType(v:String)=BillType.valueOf(v)
  @TypeConverter fun fromType(t:BillType)=t.name
}
