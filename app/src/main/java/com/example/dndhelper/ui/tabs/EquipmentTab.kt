package com.example.dndhelper.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.*
import com.example.dndhelper.tr
import com.example.dndhelper.ui.dialogs.MagicItemDetailDialog

@Composable
fun EquipmentTab(
    isEn: Boolean,
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit
) {
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var customItemType by remember { mutableIntStateOf(0) } // 0=Item, 1=Weapon, 2=Armor, 3=Treasure
    var showRatesDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- МОНЕТЫ ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("КОШЕЛЁК", "COIN PURSE"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { showRatesDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Help, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CoinRow(tr("Золотые (зм)", "Gold (gp)"), character.gp, { onCharacterChange(character.copy(gp = it)) }, Color(0xFFFFD700))
                    CoinRow(tr("Серебряные (см)", "Silver (sp)"), character.sp, { onCharacterChange(character.copy(sp = it)) }, Color(0xFFC0C0C0))
                    CoinRow(tr("Медные (мм)", "Copper (cp)"), character.cp, { onCharacterChange(character.copy(cp = it)) }, Color(0xFFCD7F32))
                    CoinRow(tr("Платиновые (пм)", "Platinum (pp)"), character.pp, { onCharacterChange(character.copy(pp = it)) }, Color(0xFFE5E4E2))
                    CoinRow(tr("Электрумовые (эм)", "Electrum (ep)"), character.ep, { onCharacterChange(character.copy(ep = it)) }, Color(0xFF4682B4))
                }
            }
            Spacer(Modifier.height(24.dp))

            if (showRatesDialog) {
                AlertDialog(
                    onDismissRequest = { showRatesDialog = false },
                    title = { Text(tr("Курс валют", "Currency Rates")) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1 ${tr("платиновая (пм)", "platinum (pp)")} = 10 ${tr("золотых (зм)", "gold (gp)")}")
                            Text("1 ${tr("золотая (зм)", "gold (gp)")} = 10 ${tr("серебряных (см)", "silver (sp)")}")
                            Text("1 ${tr("серебряная (см)", "silver (sp)")} = 10 ${tr("медных (мм)", "copper (cp)")}")
                            Text("1 ${tr("электрумовая (эм)", "electrum (ep)")} = 5 ${tr("серебряных (см)", "silver (sp)")}")
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Text(tr("1 зм = 2 эм = 10 см = 100 мм", "1 gp = 2 ep = 10 sp = 100 cp"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    },
                    confirmButton = { TextButton(onClick = { showRatesDialog = false }) { Text("OK") } }
                )
            }
        }

        // --- ЗЕЛЬЯ ЛЕЧЕНИЯ (СНАРЯЖЕНИЕ) ---
        item {
            Text(tr("ЗЕЛЬЯ ЛЕЧЕНИЯ", "HEALING POTIONS"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PotionManagementRow(tr("Обычное (2к4+2)", "Healing (2d4+2)"), character.potionHealing) { onCharacterChange(character.copy(potionHealing = it)) }
                    PotionManagementRow(tr("Большое (4к4+4)", "Greater (4d4+4)"), character.potionGreater) { onCharacterChange(character.copy(potionGreater = it)) }
                    PotionManagementRow(tr("Отличное (8к4+8)", "Superior (8d4+8)"), character.potionSuperior) { onCharacterChange(character.copy(potionSuperior = it)) }
                    PotionManagementRow(tr("Превосходное (10к4+20)", "Supreme (10d4+20)"), character.potionSupreme) { onCharacterChange(character.copy(potionSupreme = it)) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tr("ИНВЕНТАРЬ", "INVENTORY"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row {
                    IconButton(onClick = { showQuickAddDialog = true }) { Icon(Icons.Default.Add, "Quick Add", tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = { customItemType = 0; showCustomItemDialog = true }) { Icon(Icons.Default.Edit, "Custom", tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        items(character.inventoryItems) { item ->
            ListItem(
                headlineContent = { Text(item) },
                trailingContent = {
                    IconButton(onClick = {
                        val updated = character.inventoryItems.toMutableList().apply { remove(item) }
                        onCharacterChange(character.copy(inventoryItems = updated))
                    }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }



        // --- СОКРОВИЩА ---
        item {
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tr("СОКРОВИЩА", "TREASURES"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                IconButton(onClick = { customItemType = 3; showCustomItemDialog = true }) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.secondary) }
            }
        }

        items(character.treasures) { treasure ->
            ListItem(
                headlineContent = { Text(treasure, color = MaterialTheme.colorScheme.secondary) },
                trailingContent = {
                    IconButton(onClick = {
                        val updated = character.treasures.toMutableList().apply { remove(treasure) }
                        onCharacterChange(character.copy(treasures = updated))
                    }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                }
            )
        }

        // --- МАГИЧЕСКИЕ ПРЕДМЕТЫ ---
        if (character.magicItems.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Text(tr("МАГИЧЕСКИЕ ПРЕДМЕТЫ", "MAGIC ITEMS"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            }

            items(character.magicItems) { item ->
                var showDetail by remember { mutableStateOf(false) }
                val name = if (isEn) item.nameEn else item.nameRu
                val isAttuned = item.isAttuned

                ListItem(
                    modifier = Modifier.clickable { showDetail = true },
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
                            Icons.Default.AutoFixHigh, 
                            null, 
                            tint = if (isAttuned) MaterialTheme.colorScheme.tertiary else Color.Gray
                        ) 
                    },
                    trailingContent = {
                        IconButton(onClick = {
                            val updated = character.magicItems.filterNot { it.slug == item.slug && it.nameEn == item.nameEn }
                            onCharacterChange(character.copy(magicItems = updated))
                        }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                    }
                )
                if (showDetail) {
                    MagicItemDetailDialog(
                        item = item,
                        character = character,
                        onCharacterChange = onCharacterChange,
                        onDismiss = { showDetail = false },
                        isEn = isEn
                    )
                }
            }
        }
        
        item {
            Spacer(Modifier.height(100.dp))
        }
    }

    // --- ДИАЛОГ БЫСТРОГО ДОБАВЛЕНИЯ ---
    if (showQuickAddDialog) {
        AlertDialog(
            onDismissRequest = { showQuickAddDialog = false },
            title = { Text(tr("Снаряжение 2024", "2024 Equipment")) },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {

                    Text(tr("Снаряжение", "Gear"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    StandardEquipment.getGear().forEach { item ->
                        QuickAddItem(
                            item = item, 
                            isEn = isEn,
                            onAdd = {
                                val name = if (isEn) item.nameEn else item.nameRu
                                val cost = if (isEn) item.costEn else item.costRu
                                val updated = character.inventoryItems + "$name ($cost)"
                                onCharacterChange(character.copy(inventoryItems = updated))
                            }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showQuickAddDialog = false }) { Text(tr("Закрыть", "Close")) } }
        )
    }

    // --- ДИАЛОГ КАСТОМНОГО ПРЕДМЕТА ---
    if (showCustomItemDialog) {
        var name by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCustomItemDialog = false },
            title = { Text(when(customItemType) {
                0 -> tr("Новый предмет", "New Item")
                else -> tr("Новое сокровище", "New Treasure")
            }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(tr("Название", "Name")) }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        when (customItemType) {
                            0 -> onCharacterChange(character.copy(inventoryItems = character.inventoryItems + name))
                            else -> onCharacterChange(character.copy(treasures = character.treasures + name))
                        }
                        showCustomItemDialog = false
                    }
                }) { Text(tr("Добавить", "Add")) }
            },
            dismissButton = { TextButton(onClick = { showCustomItemDialog = false }) { Text(tr("Отмена", "Cancel")) } }
        )
    }
}

@Composable
fun CoinRow(label: String, value: Int, onValueChange: (Int) -> Unit, color: Color) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(12.dp)) {}
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onValueChange(value - 1) }, modifier = Modifier.size(32.dp)) {
                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable {
                    editText = value.toString()
                    showEditDialog = true
                }
            ) {
                Text(
                    "$value",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { onValueChange(value + 1) }, modifier = Modifier.size(32.dp)) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(label) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { newVal -> if (newVal.all { it.isDigit() }) editText = newVal },
                    label = { Text(tr("Количество", "Amount")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(editText.toIntOrNull() ?: 0)
                    showEditDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(tr("Отмена", "Cancel")) }
            }
        )
    }
}

@Composable
fun PotionManagementRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MedicalServices, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onValueChange(value - 1) }, modifier = Modifier.size(32.dp)) {
                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("$value", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
            IconButton(onClick = { onValueChange(value + 1) }, modifier = Modifier.size(32.dp)) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickAddItem(
    item: StandardItem, 
    isEn: Boolean, 
    isEquipped: Boolean = false,
    onAdd: () -> Unit,
    onEquip: (() -> Unit)? = null
) {
    val name = if (isEn) item.nameEn else item.nameRu
    val extra = if (isEn) {
        item.damageEn ?: (if (item.acEn != null) "AC: ${item.acEn}" else null) ?: (if (item.weightEn != "—") item.weightEn else null) ?: item.costEn
    } else {
        item.damage ?: (if (item.acRu != null) "КД: ${item.acRu}" else null) ?: (if (item.weightRu != "—") item.weightRu else null) ?: item.costRu
    }

    ListItem(
        modifier = Modifier.clickable { onAdd() },
        headlineContent = { Text(name) },
        supportingContent = { Text(extra) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onEquip != null) {
                    TextButton(onClick = onEquip) {
                        Text(
                            text = if (isEquipped) tr("Снять", "Unfit") else tr("Экип.", "Equip"),
                            fontSize = 12.sp,
                            color = if (isEquipped) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
