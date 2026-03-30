package com.example.dndhelper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.dndhelper.data.ArmorEntry
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.StandardEquipment
import com.example.dndhelper.tr

@Composable
fun CombatStatsBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    armorClass: Int,
    initiativeText: String,
    isEn: Boolean,
    armorList: List<ArmorEntry>
) {
    var showAcDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val shieldEquipped = character.shieldEquipped
    val selectedArmorIndex = character.selectedArmorIndex
    val magicBonus = character.magicBonus
    val dexMod = (character.stats.find { it.name == "Ловкость" || it.name == "Dexterity" }?.baseScore ?: 10 - 10) / 2
    
    val selectedArmor = if (selectedArmorIndex < armorList.size) armorList[selectedArmorIndex] else armorList[0]
    val dexBonus = when (selectedArmor.type) {
        0 -> dexMod
        1 -> dexMod
        2 -> dexMod.coerceAtMost(2)
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ElevatedCard(
            onClick = { showAcDialog = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(tr("КД", "AC"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("$armorClass", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (shieldEquipped) Icon(Icons.Default.Shield, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(tr("изм.", "edit"), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        CombatStatSquare(
            label = tr("ИНИЦ.", "INIT."), 
            value = initiativeText, 
            modifier = Modifier.weight(1f)
        )

        val baseSpeedValue = character.speed.filter { it.isDigit() }.toIntOrNull() ?: 30
        val isSpeedZero = character.activeConditions.any { 
            it.contains("Парализован") || it.contains("Paralyzed") ||
            it.contains("Окаменел") || it.contains("Petrified") ||
            it.contains("Ошеломлен") || it.contains("Stunned") ||
            it.contains("Без сознания") || it.contains("Unconscious") ||
            it.contains("Схвачен") || it.contains("Grappled") ||
            it.contains("Опутан") || it.contains("Restrained")
        } || (character.exhaustionLevel >= 5 && !character.use2024Rules)

        val isSpeedHalved = !character.use2024Rules && character.exhaustionLevel >= 2 && character.exhaustionLevel < 5
        
        val effectiveSpeed = when {
            isSpeedZero -> "0"
            character.use2024Rules && character.exhaustionLevel > 0 -> {
                val reduction = character.exhaustionLevel * 5
                val finalSpeed = (baseSpeedValue - reduction).coerceAtLeast(0)
                "$finalSpeed${character.speed.filter { !it.isDigit() }}"
            }
            isSpeedHalved -> "${baseSpeedValue / 2}${character.speed.filter { !it.isDigit() }}"
            else -> character.speed
        }
        
        val isSpeedEffected = isSpeedZero || isSpeedHalved || (character.use2024Rules && character.exhaustionLevel > 0)

        ElevatedCard(
            onClick = { showSpeedDialog = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = if (isSpeedEffected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer) else CardDefaults.elevatedCardColors()
        ) {
            Column(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(tr("СКОРОСТЬ", "SPEED"), style = MaterialTheme.typography.labelSmall, color = if (isSpeedEffected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    text = effectiveSpeed,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSpeedEffected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                if (isSpeedEffected) {
                    Text(tr("Эффект!", "Effect!"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(tr("изм.", "edit"), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        if (showSpeedDialog) {
            var speedInput by remember { mutableStateOf(character.speed) }
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text(tr("Изменить скорость", "Edit Speed")) },
                text = {
                    OutlinedTextField(
                        value = speedInput,
                        onValueChange = { speedInput = it },
                        label = { Text(tr("Скорость", "Speed")) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onCharacterChange(character.copy(speed = speedInput))
                        showSpeedDialog = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showSpeedDialog = false }) { Text(tr("Отмена", "Cancel")) }
                }
            )
        }
    }

    if (showAcDialog) {
        AlertDialog(
            onDismissRequest = { showAcDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary)
                    Text(tr("Калькулятор КД", "AC Calculator"), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(tr("Выберите броню:", "Select armor:"), fontWeight = FontWeight.Bold)
                    armorList.forEachIndexed { index, armor ->
                        val isSelected = selectedArmorIndex == index
                        val label = if (isEn) armor.nameEn else armor.nameRu

                        val typeLabel = when (armor.type) {
                            0 -> tr("Без брони", "Unarmored")
                            1 -> tr("Лёгкий", "Light")
                            2 -> tr("Средний", "Medium")
                            else -> tr("Тяжёлый", "Heavy")
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onCharacterChange(character.copy(selectedArmorIndex = index)) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                else Icon(Icons.Default.RadioButtonUnchecked, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                    Text(typeLabel, fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(tr("База: ${armor.baseAc}", "Base: ${armor.baseAc}"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onCharacterChange(character.copy(shieldEquipped = !shieldEquipped)) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (shieldEquipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, null, tint = if (shieldEquipped) MaterialTheme.colorScheme.secondary else Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Text(tr("Щит (+2)", "Shield (+2)"), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Switch(checked = shieldEquipped, onCheckedChange = { onCharacterChange(character.copy(shieldEquipped = it)) })
                        }
                    }

                    Text(tr("Магический бонус к броне:", "Magic armor bonus:"), fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { if (magicBonus > 0) onCharacterChange(character.copy(magicBonus = magicBonus - 1)) }, modifier = Modifier.size(40.dp)) { Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text("+$magicBonus", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        }
                        IconButton(onClick = { if (magicBonus < 5) onCharacterChange(character.copy(magicBonus = magicBonus + 1)) }, modifier = Modifier.size(40.dp)) { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                    }

                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tr("Итоговый КД:", "Final AC:"), fontWeight = FontWeight.Bold)
                                Text(tr(
                                    "${selectedArmor.nameRu} + DEX ${if (dexBonus >= 0) "+$dexBonus" else "$dexBonus"}${if (shieldEquipped) " + щит +2" else ""}${if (magicBonus > 0) " + маг +$magicBonus" else ""}",
                                    "${selectedArmor.nameEn} + DEX ${if (dexBonus >= 0) "+$dexBonus" else "$dexBonus"}${if (shieldEquipped) " + shield +2" else ""}${if (magicBonus > 0) " + magic +$magicBonus" else ""}"
                                ), fontSize = 11.sp, color = Color.Gray)
                            }
                            Text("$armorClass", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAcDialog = false }) { Text(tr("Готово", "Done")) }
            }
        )
    }
}

@Composable
fun CombatStatSquare(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}
