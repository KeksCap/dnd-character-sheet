package com.example.dndhelper.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.GameLogEntry
import com.example.dndhelper.tr
import com.example.dndhelper.utils.GameLogManager
import com.example.dndhelper.utils.getStatMod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRestBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    conMod: Int,
    onAddLog: (GameLogEntry) -> Unit
) {
    var maxHpInput by remember(character.id) { mutableStateOf(character.maxHp.toString()) }
    val maxHp = maxHpInput.toIntOrNull() ?: 1
    val isHpMaxHalved = character.exhaustionLevel >= 4
    val effectiveMaxHp = if (isHpMaxHalved) maxHp / 2 else maxHp
    val currentHp = character.currentHp.coerceAtMost(effectiveMaxHp)

    var isEditingHp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(48.dp))
            Text(
                text = tr("ЗДОРОВЬЕ", "HEALTH"),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            )
            IconButton(onClick = { isEditingHp = !isEditingHp }) {
                Icon(
                    imageVector = if (isEditingHp) Icons.Default.Check else Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                if (isEditingHp) {
                    OutlinedTextField(
                        value = maxHpInput,
                        onValueChange = { 
                            if (it.all { c -> c.isDigit() }) {
                                maxHpInput = it
                                val newMax = it.toIntOrNull() ?: character.maxHp
                                if (newMax != character.maxHp) {
                                    onCharacterChange(character.copy(maxHp = newMax))
                                }
                            }
                        },
                        label = { Text(tr("Макс. ХП", "Max HP")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(150.dp).padding(bottom = 8.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(tr("Кости Хитов: ", "Hit Dice: "), style = MaterialTheme.typography.bodySmall)
                        
                        var showDiceMenu by remember { mutableStateOf(false) }
                        TextButton(onClick = { showDiceMenu = true }) {
                            Text("d${character.hitDiceType}", fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(expanded = showDiceMenu, onDismissRequest = { showDiceMenu = false }) {
                            listOf(6, 8, 10, 12).forEach { dice ->
                                DropdownMenuItem(
                                    text = { Text("d$dice") },
                                    onClick = { 
                                        onCharacterChange(character.copy(hitDiceType = dice))
                                        showDiceMenu = false
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))
                        
                        OutlinedTextField(
                            value = character.maxHitDice.toString(),
                            onValueChange = { 
                                val newVal = it.toIntOrNull() ?: character.maxHitDice
                                onCharacterChange(character.copy(maxHitDice = newVal, currentHitDice = newVal.coerceAtMost(newVal))) 
                            },
                            label = { Text(tr("Макс. кол-во", "Max dice")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            textStyle = TextStyle(fontSize = 12.sp)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$currentHp / $effectiveMaxHp", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = if (isHpMaxHalved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                        if (isHpMaxHalved) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.TrendingDown, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                val progress = (currentHp.toFloat() / effectiveMaxHp.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                                    if (scrollDelta != 0f) {
                                        val newHp = if (scrollDelta < 0) (currentHp + 1).coerceAtMost(effectiveMaxHp)
                                                    else (currentHp - 1).coerceAtLeast(0)
                                        if (newHp != currentHp) {
                                            onCharacterChange(character.copy(currentHp = newHp))
                                        }
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                        },
                    color = if (progress > 0.5f) Color(0xFF4CAF50) else Color(0xFFF44336),
                    trackColor = Color(0xFFFF5252)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val damageRed = Color(0xFFB71C1C)
                    Button(
                        onClick = {
                            val newHp = (currentHp - 5).coerceAtLeast(0)
                            val dmg = currentHp - newHp
                            onCharacterChange(character.copy(currentHp = newHp))
                            if (dmg > 0) onAddLog(GameLogManager.logDamage(dmg, newHp))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = damageRed.copy(alpha = 0.75f)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("-5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            val newHp = (currentHp - 1).coerceAtLeast(0)
                            val dmg = currentHp - newHp
                            onCharacterChange(character.copy(currentHp = newHp))
                            if (dmg > 0) onAddLog(GameLogManager.logDamage(dmg, newHp))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = damageRed),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("-1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(Modifier.width(4.dp))

                    val healGreen = Color(0xFF2E7D32)
                    Button(
                        onClick = {
                            val newHp = (currentHp + 1).coerceAtMost(effectiveMaxHp)
                            val heal = newHp - currentHp
                            onCharacterChange(character.copy(currentHp = newHp))
                            if (heal > 0) onAddLog(GameLogManager.logHeal(heal, newHp))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = healGreen.copy(alpha = 0.75f)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("+1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            val newHp = (currentHp + 5).coerceAtMost(effectiveMaxHp)
                            val heal = newHp - currentHp
                            onCharacterChange(character.copy(currentHp = newHp))
                            if (heal > 0) onAddLog(GameLogManager.logHeal(heal, newHp))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = healGreen),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("+5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // --- Произвольное количество HP ---
                var customHpInput by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val damageRed = Color(0xFFB71C1C)
                    val healGreen = Color(0xFF2E7D32)
                    val amount = customHpInput.toIntOrNull() ?: 0

                    Button(
                        onClick = {
                            if (amount > 0) {
                                val newHp = (currentHp - amount).coerceAtLeast(0)
                                val dmg = currentHp - newHp
                                onCharacterChange(character.copy(currentHp = newHp))
                                if (dmg > 0) onAddLog(GameLogManager.logDamage(dmg, newHp))
                                customHpInput = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = damageRed),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        enabled = amount > 0
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    }

                    OutlinedTextField(
                        value = customHpInput,
                        onValueChange = { newVal ->
                            if (newVal.all { it.isDigit() } && newVal.length <= 4) {
                                customHpInput = newVal
                            }
                        },
                        modifier = Modifier.weight(2f).height(48.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        placeholder = { 
                            Text(
                                tr("Кол-во", "Amount"),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )

                    Button(
                        onClick = {
                            if (amount > 0) {
                                val newHp = (currentHp + amount).coerceAtMost(effectiveMaxHp)
                                val heal = newHp - currentHp
                                onCharacterChange(character.copy(currentHp = newHp))
                                if (heal > 0) onAddLog(GameLogManager.logHeal(heal, newHp))
                                customHpInput = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = healGreen),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        enabled = amount > 0
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                }
                val potions = listOf(
                    Triple(character.potionHealing, tr("Зелье", "Potion"), "2d4+2"),
                    Triple(character.potionGreater, tr("Большое", "Greater"), "4d4+4"),
                    Triple(character.potionSuperior, tr("Отличное", "Superior"), "8d4+8"),
                    Triple(character.potionSupreme, tr("Превосходное", "Supreme"), "10d4+20")
                )
                
                if (potions.any { it.first > 0 }) {
                    Text(
                        tr("ВЫПИТЬ ЗЕЛЬЕ:", "DRINK POTION:"), 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.padding(top = 12.dp),
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        potions.forEach { (count, name, formula) ->
                            if (count > 0) {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        val healAmount = when(formula) {
                                            "2d4+2" -> (1..2).sumOf { (1..4).random() } + 2
                                            "4d4+4" -> (1..4).sumOf { (1..4).random() } + 4
                                            "8d4+8" -> (1..8).sumOf { (1..4).random() } + 8
                                            "10d4+20" -> (1..10).sumOf { (1..4).random() } + 20
                                            else -> 0
                                        }
                                        val newHp = (character.currentHp + healAmount).coerceAtMost(effectiveMaxHp)
                                        val actualHeal = newHp - character.currentHp
                                        val potionNameRu = when(formula) {
                                            "2d4+2" -> "Зелье лечения"
                                            "4d4+4" -> "Большое зелье лечения"
                                            "8d4+8" -> "Отличное зелье лечения"
                                            "10d4+20" -> "Превосходное зелье лечения"
                                            else -> "Зелье"
                                        }
                                        val potionNameEn = when(formula) {
                                            "2d4+2" -> "Potion of Healing"
                                            "4d4+4" -> "Greater Healing Potion"
                                            "8d4+8" -> "Superior Healing Potion"
                                            "10d4+20" -> "Supreme Healing Potion"
                                            else -> "Potion"
                                        }
                                        val fieldToUpdate = when(formula) {
                                            "2d4+2" -> character.copy(potionHealing = count - 1, currentHp = newHp)
                                            "4d4+4" -> character.copy(potionGreater = count - 1, currentHp = newHp)
                                            "8d4+8" -> character.copy(potionSuperior = count - 1, currentHp = newHp)
                                            "10d4+20" -> character.copy(potionSupreme = count - 1, currentHp = newHp)
                                            else -> character
                                        }
                                        onCharacterChange(fieldToUpdate)
                                        onAddLog(GameLogManager.logPotion(potionNameRu, potionNameEn, actualHeal, newHp))
                                    },
                                    label = { Text("$name ($count)") },
                                    leadingIcon = { Icon(Icons.Default.LocalPharmacy, null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }

                var showLongRestDialog by remember { mutableStateOf(false) }
                var showShortRestDialog by remember { mutableStateOf(false) }
                
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showShortRestDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Coffee, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(tr("КОРОТКИЙ", "SHORT"), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showLongRestDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.NightsStay, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(tr("ДЛИТЕЛЬНЫЙ", "LONG"), fontWeight = FontWeight.Bold)
                    }
                }

                if (showShortRestDialog) {
                    AlertDialog(
                        onDismissRequest = { showShortRestDialog = false },
                        title = { Text(tr("Короткий отдых", "Short Rest")) },
                        text = {
                            Column {
                                Text(tr("Кости хитов: ${character.currentHitDice} / ${character.maxHitDice} (d${character.hitDiceType})", 
                                       "Hit Dice: ${character.currentHitDice} / ${character.maxHitDice} (d${character.hitDiceType})"))
                                Spacer(Modifier.height(8.dp))
                                if (character.currentHitDice > 0) {
                                    Button(
                                        onClick = {
                                            val roll = (1..character.hitDiceType).random()
                                            val heal = (roll + conMod).coerceAtLeast(1)
                                            val newHp = (character.currentHp + heal).coerceAtMost(effectiveMaxHp)
                                            val updated = character.copy(
                                                currentHitDice = character.currentHitDice - 1,
                                                currentHp = newHp
                                            )
                                            onCharacterChange(updated)
                                            onAddLog(GameLogManager.logShortRest(character.hitDiceType, roll, conMod, heal))
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(tr("Потратить 1 кость и полечиться", "Spend 1 die and heal"))
                                    }
                                } else {
                                    Text(tr("Нет доступных костей хитов!", "No hit dice available!"), color = Color.Gray)
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showShortRestDialog = false }) { Text(tr("Закончить", "Finish")) }
                        }
                    )
                }

                if (showLongRestDialog) {
                    AlertDialog(
                        onDismissRequest = { showLongRestDialog = false },
                        title = { Text(tr("Длительный отдых", "Long Rest")) },
                        text = { Text(tr("Восстановить все хиты, ячейки и часть костей хитов?", "Restore all health, slots, and some hit dice?")) },
                        confirmButton = {
                            TextButton(onClick = {
                                val diceToRestore = (character.maxHitDice / 2).coerceAtLeast(1)
                                val newExhaustion = (character.exhaustionLevel - 1).coerceAtLeast(0)
                                val updated = character.copy(
                                    currentHp = effectiveMaxHp, // Восстанавливаем до текущего максимума
                                    currentSpellSlots = character.maxSpellSlots,
                                    currentHitDice = (character.currentHitDice + diceToRestore).coerceAtMost(character.maxHitDice),
                                    exhaustionLevel = newExhaustion
                                )
                                onCharacterChange(updated)
                                onAddLog(GameLogManager.logLongRest(effectiveMaxHp, diceToRestore))
                                showLongRestDialog = false
                            }) {
                                Text(tr("Да", "Yes"))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLongRestDialog = false }) {
                                Text(tr("Нет", "No"))
                            }
                        }
                    )
                }
            }
        }
    }
}
