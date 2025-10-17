package cl.tuapp.calculocuentas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.tuapp.calculocuentas.ui.*
import cl.tuapp.calculocuentas.data.*

class CalculocuentasActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
          App(onShare = { text ->
            val intent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_TEXT, text)
              setPackage("com.whatsapp")
            }
            try { startActivity(intent) } catch (e: Exception) {
              startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
              }, "Compartir"))
            }
          })
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(onShare:(String)->Unit, vm: MainViewModel = viewModel()) {
  val type by vm.type.collectAsState()
  val houses by vm.houses.collectAsState()
  val selected by vm.selectedHouse.collectAsState()
  val input by vm.input.collectAsState()
  val result by vm.result.collectAsState()
  val history by vm.history.collectAsState()
  val error by vm.error.collectAsState()

  Scaffold(topBar = { TopAppBar(title = { Text("Cálculo de Cuentas") }) }) {
    Column(Modifier.padding(it).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      TabRow(selectedTabIndex = if (type==BillType.LUZ) 0 else 1) {
        Tab(selected = type==BillType.LUZ, onClick = { vm.setType(BillType.LUZ) }, text = { Text("Electricidad ⚡") })
        Tab(selected = type==BillType.AGUA, onClick = { vm.setType(BillType.AGUA) }, text = { Text("Agua 💧") })
      }

      if (houses.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }
        val current = houses.find { it.id == selected }?.name ?: "Selecciona casa"
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
          OutlinedTextField(readOnly = true, value = current, onValueChange = {}, label = { Text("Casa") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth())
          ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            houses.forEach { h -> DropdownMenuItem(text = { Text(h.name) }, onClick = { vm.selectHouse(h.id); expanded = false }) }
          }
        }
      } else Text("Cargando casas…")

      Entrada("Lectura anterior general (kWh)", input.genPrev) { vm.updateInput(input.copy(genPrev = it.replace(',','.'))) }
      Entrada("Lectura actual general (kWh)", input.genCurr) { vm.updateInput(input.copy(genCurr = it.replace(',','.'))) }
      Entrada("Lectura anterior interior (kWh)", input.intPrev) { vm.updateInput(input.copy(intPrev = it.replace(',','.'))) }
      Entrada("Lectura actual interior (kWh)", input.intCurr) { vm.updateInput(input.copy(intCurr = it.replace(',','.'))) }
      Entrada("Valor total boleta ($)", input.total, KeyboardType.Number) { vm.updateInput(input.copy(total = it.replace(',','.'))) }

      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { vm.calculate() }) { Text("Calcular") }
        OutlinedButton(onClick = { vm.save() }, enabled = result!=null && selected!=null) { Text("Guardar") }
        if (result!=null) {
          OutlinedButton(onClick = {
            val r = result!!
            val text = """Cálculo de Cuentas — {if (type==BillType.LUZ) "Electricidad" else "Agua"}
Casa: {houses.find{it.id==selected}?.name ?: ""}
Consumo general: {r.consG} kWh
Consumo interior: {r.consI} kWh
Consumo común: {r.consC} kWh
Proporción interior: {String.format(java.util.Locale.US, "%.4f", r.prop)}
Monto interior: {r.montoInt}
Monto común: {r.montoCom}"""
            onShare(text)
          }) { Text("WhatsApp") }
        }
      }

      if (error!=null) AssistChip(onClick={} , label={ Text(error!!) }, enabled=false)

      result?.let { r ->
        Divider(); Text("Resultados", style = MaterialTheme.typTypography.titleMedium)
        Text("Consumo general: {r.consG} kWh")
        Text("Consumo interior: {r.consI} kWh")
        Text("Consumo común: {r.consC} kWh")
        Text("Proporción interior: {String.format(java.util.Locale.US, "%.4f", r.prop)}")
        Text("Monto interior: {r.montoInt}")
        Text("Monto común: {r.montoCom}")
      }

      Divider(); Text("Historial", style = MaterialTheme.typography.titleMedium)
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(history, key = { it.id }) { e ->
          ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Fecha: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(e.ts)))
              Text("Gen: {e.genPrev} → {e.genCurr}  |  Int: {e.intPrev} → {e.intCurr}  |  Boleta: {e.total}")
              Text("ConsG={e.consG}  ConsI={e.consI}  Prop={String.format(java.util.Locale.US, "%.4f", e.prop)}")
              Text("Interior={e.montoInt}  Común={e.montoCom}")
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                  val text = "{if (type==BillType.LUZ) "Luz" else "Agua"} — {houses.find{it.id==selected}?.name}\nConsG={e.consG} ConsI={e.consI} ConsC={e.consC}\nProp={String.format(java.util.Locale.US, "%.4f", e.prop)}\nInterior={e.montoInt}  Común={e.montoCom}"
                  onShare(text)
                }) { Text("Compartir") }
                TextButton(onClick = { vm.deleteEntry(e.id) }) { Text("Borrar") }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun Entrada(label:String, value:String, keyboardType:KeyboardType=KeyboardType.Decimal, onChange:(String)->Unit){
  OutlinedTextField(label={ Text(label) }, value=value, onValueChange=onChange,
    singleLine=true, keyboardOptions=KeyboardOptions(keyboardType=keyboardType), modifier=Modifier.fillMaxWidth())
}
