package cl.tuapp.calculocuentas.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.*

object DBProvider {
  @Volatile private var INSTANCE:AppDatabase?=null
  fun get(ctx:Context):AppDatabase= INSTANCE?: synchronized(this){
    val db=Room.databaseBuilder(ctx.applicationContext,AppDatabase::class.java,"calculo_cuentas.db")
      .addCallback(object:RoomDatabase.Callback(){
        override fun onCreate(dbx:SupportSQLiteDatabase){
          super.onCreate(dbx)
          CoroutineScope(Dispatchers.IO).launch{
            val dao=get(ctx).houseDao()
            dao.insertAll(
              House(name="Casa 1", type=BillType.LUZ),
              House(name="Casa 2", type=BillType.LUZ),
              House(name="Casa 3", type=BillType.LUZ),
              House(name="Casa 4", type=BillType.LUZ),
              House(name="Casa 5", type=BillType.LUZ),
              House(name="Casa 1", type=BillType.AGUA),
              House(name="Casa 2", type=BillType.AGUA),
              House(name="Casa 3", type=BillType.AGUA),
              House(name="Casa 4", type=BillType.AGUA),
              House(name="Casa 5", type=BillType.AGUA)
            )
          }
        }
      }).build()
    INSTANCE=db; db
  }
}
