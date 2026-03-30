package com.example.dndhelper.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dndhelper.data.Attunement
import com.example.dndhelper.data.MagicItem
import com.example.dndhelper.tr

@Composable
fun AddCustomMagicItemDialog(
    onDismiss: () -> Unit,
    onAdd: (MagicItem) -> Unit,
    isEn: Boolean
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var rarity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attunementRequired by remember { mutableStateOf(false) }
    var attunementCondition by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Новый магический предмет", "New Magic Item")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(tr("Название", "Name")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text(tr("Тип (напр. Жезл)", "Type (e.g. Wand)")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rarity, onValueChange = { rarity = it }, label = { Text(tr("Редкость", "Rarity")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text(tr("Описание", "Description")) }, 
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = attunementRequired, onCheckedChange = { attunementRequired = it })
                    Text(tr("Требуется настройка", "Attunement Required"))
                }
                
                if (attunementRequired) {
                    OutlinedTextField(
                        value = attunementCondition, 
                        onValueChange = { attunementCondition = it }, 
                        label = { Text(tr("Условие настройки", "Attunement Condition")) }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val newItem = MagicItem(
                        slug = "custom-${System.currentTimeMillis()}",
                        nameRu = name,
                        nameEn = name,
                        typeRu = type,
                        typeEn = type,
                        subtypeRu = null,
                        subtypeEn = null,
                        rarity = rarity,
                        attunement = Attunement(attunementRequired, attunementCondition.ifBlank { null }),
                        descriptionRu = description,
                        descriptionEn = description,
                        isCustom = true
                    )
                    onAdd(newItem)
                }
            }) { Text(tr("Добавить", "Add")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Отмена", "Cancel")) }
        }
    )
}
