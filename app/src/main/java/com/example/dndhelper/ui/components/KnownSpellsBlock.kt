package com.example.dndhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.SpellInfo
import com.example.dndhelper.tr

@Composable
fun KnownSpellsBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    isEn: Boolean
) {
    var showCreateSpellDialog by remember { mutableStateOf(false) }
    var selectedKnownSpellInfo by remember { mutableStateOf<SpellInfo?>(null) }
    val isRussian = !isEn

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tr("ИЗВЕСТНЫЕ ЗАКЛИНАНИЯ", "KNOWN SPELLS"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showCreateSpellDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (character.knownSpells.isEmpty()) {
            Text(
                tr(
                    "Пока нет известных заклинаний. Добавьте их из вкладки Заклинания или создайте своё!", 
                    "No known spells yet. Add them from Reference or create one!"
                ), 
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            character.knownSpells.forEach { spell ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedKnownSpellInfo = spell }
                ) {
                    ListItem(
                        headlineContent = { Text(spell.name ?: "") },
                        supportingContent = {
                            val levelText = if (spell.level == "0") tr("Заговор", "Cantrip") else tr("Уровень:", "Level:") + " ${spell.level}"
                            Text("$levelText | ${spell.castingTime}")
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                val updatedSpells = character.knownSpells.toMutableList().apply { remove(spell) }
                                onCharacterChange(character.copy(knownSpells = updatedSpells))
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = tr("Удалить", "Delete"), tint = Color.Gray)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateSpellDialog) {
        var newName by remember { mutableStateOf("") }
        var newLevel by remember { mutableStateOf("") }
        var newTime by remember { mutableStateOf("") }
        var newRange by remember { mutableStateOf("") }
        var newComponents by remember { mutableStateOf("") }
        var newDuration by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateSpellDialog = false },
            title = { Text(tr("Создать заклинание", "Create Spell")) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text(tr("Имя", "Name")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newLevel, onValueChange = { newLevel = it }, label = { Text(tr("Уровень (0-9)", "Level (0-9)")) }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = newTime, onValueChange = { newTime = it }, label = { Text(tr("Время (напр. 1 действие)", "Casting time (e.g. 1 action)")) }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newRange, onValueChange = { newRange = it }, label = { Text(tr("Дистанция", "Range")) }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = newDuration, onValueChange = { newDuration = it }, label = { Text(tr("Длительность", "Duration")) }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = newComponents, onValueChange = { newComponents = it }, label = { Text(tr("Компоненты (В, С, М)", "Components (V, S, M)")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text(tr("Описание заклинания", "Spell description")) }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val customSpell = SpellInfo(
                                name = newName,
                                level = newLevel.ifBlank { "0" },
                                school = "Авторское",
                                castingTime = newTime.ifBlank { "-" },
                                range = newRange.ifBlank { "-" },
                                components = newComponents.ifBlank { "-" },
                                duration = newDuration.ifBlank { "-" },
                                text = newDesc.ifBlank { "Описание отсутствует." }
                            )
                            val updatedSpells = character.knownSpells + customSpell
                            onCharacterChange(character.copy(knownSpells = updatedSpells))
                            showCreateSpellDialog = false
                        }
                    }
                ) { Text(tr("Сохранить", "Save")) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSpellDialog = false }) { Text(tr("Отмена", "Cancel")) }
            }
        )
    }

    selectedKnownSpellInfo?.let { spell ->
        AlertDialog(
            onDismissRequest = { selectedKnownSpellInfo = null },
            title = { Text(spell.name ?: "") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(if (isRussian) "Время: ${spell.castingTime}" else "Casting Time: ${spell.castingTime}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Дистанция: ${spell.range}" else "Range: ${spell.range}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Компоненты: ${spell.components}" else "Components: ${spell.components}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Длительность: ${spell.duration}" else "Duration: ${spell.duration}", style = MaterialTheme.typography.labelLarge)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text(spell.text ?: "")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedKnownSpellInfo = null }) { Text(tr("Закрыть", "Close")) }
            }
        )
    }
}
