package com.example.dndhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.MagicItem
import com.example.dndhelper.tr
import com.example.dndhelper.ui.dialogs.AddCustomMagicItemDialog
import com.example.dndhelper.ui.dialogs.MagicItemDetailDialog

@Composable
fun MagicItemsBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    magicItems: List<MagicItem>,
    isEn: Boolean
) {
    var selectedItemInSheet by remember { mutableStateOf<MagicItem?>(null) }
    var showAddMagicItemDialog by remember { mutableStateOf(false) }
    var showCreateCustomMagicItemInSheet by remember { mutableStateOf(false) }
    var magicItemSearchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(tr("МАГИЧЕСКИЕ ПРЕДМЕТЫ", "MAGIC ITEMS"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                TextButton(onClick = { showAddMagicItemDialog = true }) {
                    Icon(Icons.Default.List, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tr("Из списка", "From List"), fontSize = 12.sp, maxLines = 1, softWrap = false)
                }
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = { showCreateCustomMagicItemInSheet = true }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tr("Своё", "Custom"), fontSize = 12.sp, maxLines = 1, softWrap = false)
                }
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (character.magicItems.isEmpty()) {
                    Text(tr("Нет магических предметов", "No magic items"), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                } else {
                    character.magicItems.forEach { item ->
                        val name = if (isEn) item.nameEn else item.nameRu
                        val isAttuned = item.isAttuned
                        
                        ListItem(
                            modifier = Modifier.clickable { selectedItemInSheet = item },
                            headlineContent = { Text(name, fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                Text(
                                    text = buildString {
                                        if (isAttuned) append("${tr("Настроено", "Attuned")} • ")
                                        append(if (isEn) item.typeEn else item.typeRu)
                                    },
                                    color = if (isAttuned) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                    fontSize = 12.sp
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Bolt,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isAttuned) MaterialTheme.colorScheme.tertiary else Color.Gray.copy(alpha = 0.5f)
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    val updated = character.magicItems.filterNot { it.slug == item.slug && it.nameEn == item.nameEn }
                                    onCharacterChange(character.copy(magicItems = updated))
                                }) { Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                            }
                        )
                        if (item != character.magicItems.last()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }

    if (showAddMagicItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddMagicItemDialog = false },
            title = { Text(tr("Выберите предмет", "Select Magic Item")) },
            text = {
                Column {
                    OutlinedTextField(
                        value = magicItemSearchQuery,
                        onValueChange = { magicItemSearchQuery = it },
                        label = { Text(tr("Поиск...", "Search...")) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    val itemsPick = magicItems.filter {
                        val itemName = if (isEn) it.nameEn else it.nameRu
                        itemName.contains(magicItemSearchQuery, ignoreCase = true)
                    }.take(20)
                    
                    Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                        itemsPick.forEach { item ->
                            val name = if (isEn) item.nameEn else item.nameRu
                            val isOwned = character.magicItems.any { it.slug == item.slug && it.nameEn == item.nameEn }
                            ListItem(
                                modifier = Modifier.clickable {
                                    if (!isOwned) {
                                        onCharacterChange(character.copy(magicItems = character.magicItems + item))
                                    }
                                    showAddMagicItemDialog = false
                                    magicItemSearchQuery = ""
                                },
                                headlineContent = { Text(name) },
                                supportingContent = { Text("${item.rarity ?: ""} • ${if (isEn) item.typeEn else item.typeRu}", fontSize = 11.sp) },
                                trailingContent = {
                                    if (isOwned) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                    else Icon(Icons.Default.Add, null)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddMagicItemDialog = false; magicItemSearchQuery = "" }) { Text(tr("Закрыть", "Close")) } }
        )
    }

    if (showCreateCustomMagicItemInSheet) {
        AddCustomMagicItemDialog(
            isEn = isEn,
            onDismiss = { showCreateCustomMagicItemInSheet = false },
            onAdd = { newItem ->
                onCharacterChange(character.copy(magicItems = character.magicItems + newItem))
                showCreateCustomMagicItemInSheet = false
            }
        )
    }

    if (selectedItemInSheet != null) {
        MagicItemDetailDialog(
            item = selectedItemInSheet!!,
            character = character,
            onCharacterChange = onCharacterChange,
            onDismiss = { selectedItemInSheet = null },
            isEn = isEn
        )
    }
}
