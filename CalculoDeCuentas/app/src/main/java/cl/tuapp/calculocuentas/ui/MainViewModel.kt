package cl.tuapp.calculocuentas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.tuapp.calculocuentas.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CalcInput(val genPrev:String="", val genCurr:String="", val intPrev:String="", val intCurr:String="", val total:String="")
data class CalcResult(val consG:Double,val consI:Double,val consC:Double,val prop:Double,val montoInt:Double,val montoCom:Double)

class MainViewModel(app:Application):AndroidViewModel(app){
  private val db=DBProvider.get(app)
  private val _type=MutableStateFlow(BillType.LUZ); val type=_type.asStateFlow()
  private val _houses=MutableStateFlow<List<House>>(emptyList()); val houses=_houses.asStateFlow()
  private val _selected=MutableStateFlow<Int?>(null); val selectedHouse=_selected.asStateFlow()
  private val _history=MutableStateFlow<List<Entry>>(emptyList()); val history=_history.asStateFlow()
  private val _input=MutableStateFlow(CalcInput()); val input=_input.asStateFlow()
  private val _result=MutableStateFlow<CalcResult?>(null); val result=_result.asStateFlow()
  private val _error=MutableStateFlow<String?>(null); val error=_error.asStateFlow()
  init{ loadHouses() }
  fun setType(t:BillType){ if(_type.value==t)return; _type.value=t; _selected.value=null; _history.value= emptyList(); loadHouses() }
  private fun loadHouses(){ viewModelScope.launch{ val hs=db.houseDao().byType(_type.value); _houses.value=hs; if(hs.isNotEmpty()) selectHouse(hs.first().id) } }
  fun selectHouse(id:Int){ _selected.value=id; viewModelScope.launch{ _history.value=db.entryDao().byHouse(id) } }
  fun updateInput(i:CalcInput){ _input.value=i }
  fun calculate(){
    _error.value=null; _result.value=null
    val v=_input.value
    val gp=v.genPrev.toDoubleOrNull(); val gc=v.genCurr.toDoubleOrNull(); val ip=v.intPrev.toDoubleOrNull(); val ic=v.intCurr.toDoubleOrNull(); val total=v.total.toDoubleOrNull()
    if(listOf(gp,gc,ip,ic,total).any{it==null}){ _error.value="Completa todos los campos numéricos."; return }
    val cG=gc!!-gp!!; val cI=ic!!-ip!!
    if(cG<0||cI<0){ _error.value="Las lecturas actuales deben ser mayores o iguales a las anteriores."; return }
    if(cI>cG){ _error.value="El consumo interior no puede superar al consumo general."; return }
    val prop= if(cG==0.0) 0.0 else cI/cG; val mI= total!!*prop; val mC= total-mI
    _result.value=CalcResult(cG,cI,cG-cI,prop,mI,mC)
  }
  fun save(){
    val r=_result.value?:return; val house=_selected.value?:return; val v=_input.value
    viewModelScope.launch{ db.entryDao().insert(Entry(house,System.currentTimeMillis(), v.genPrev.toDouble(),v.genCurr.toDouble(),v.intPrev.toDouble(),v.intCurr.toDouble(),v.total.toDouble(), r.consG,r.consI,r.consC,r.prop,r.montoInt,r.montoCom)); _history.value=db.entryDao().byHouse(house) }
  }
  fun deleteEntry(id:Long){ val house=_selected.value?:return; viewModelScope.launch{ db.entryDao().delete(id); _history.value=db.entryDao().byHouse(house) } }
}
