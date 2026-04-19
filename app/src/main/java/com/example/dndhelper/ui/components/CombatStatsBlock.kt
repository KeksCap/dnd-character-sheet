package com.example.dndhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import com.example.dndhelper.data.ArmorEntry
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.tr
import com.example.dndhelper.ui.models.DiceRollData
import com.example.dndhelper.ui.models.RollType
import com.example.dndhelper.utils.CombatAssistant
import com.example.dndhelper.utils.OracleAdvice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatStatsBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    armorClass: Int,
    initiativeText: String,
    isEn: Boolean,
    armorList: List<ArmorEntry>,
    onRollRequest: (DiceRollData) -> Unit
) {
    // --- ПЕРЕВОДЫ ---
    val acLabel = tr("КД", "AC")
    val editLabel = tr("изм.", "edit")
    val initLabel = tr("ИНИЦ.", "INIT.")
    val initRollTitle = tr("Инициатива", "Initiative")
    val speedLabel = tr("СКОРОСТЬ", "SPEED")
    val speedEffectLabel = tr("Эффект!", "Effect!")
    val editSpeedTitle = tr("Изменить скорость", "Edit Speed")
    val speedInputLabel = tr("Скорость", "Speed")
    val cancelLabel = tr("Отмена", "Cancel")
    val acCalcTitle = tr("Калькулятор КД", "AC Calculator")
    val selectArmorLabel = tr("Выберите броню:", "Select armor:")
    val shieldLabel = tr("Щит (+2)", "Shield (+2)")
    val magicAcBonusLabel = tr("Магический бонус к броне:", "Magic armor bonus:")
    val finalAcLabel = tr("Итоговый КД:", "Final AC:")
    val doneLabel = tr("Готово", "Done")
    val baseLabel = tr("База:", "Base:")

    val armorTypeLabels = listOf(
        tr("Без брони", "Unarmored"),
        tr("Лёгкий", "Light"),
        tr("Средний", "Medium"),
        tr("Тяжёлый", "Heavy")
    )

    var showAcDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showAddArmorDialog by remember { mutableStateOf(false) }

    val shieldEquipped = character.shieldEquipped
    val selectedArmorIndex = character.selectedArmorIndex
    val magicBonus = character.magicBonus
    val dexMod = (character.stats.find { it.name == "Ловкость" || it.name == "Dexterity" }?.baseScore ?: 10) / 2 - 5
    
    val adviceList = remember(character, isEn) { CombatAssistant.getAdvice(character, isEn) }
    var showOracleDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        // Oracle button / banner line
        if (character.isOracleEnabled) {
            val topAdvice = adviceList.firstOrNull { it.title != "Готовность к бою" && it.title != "Ready for Combat" }
            if (topAdvice != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CombatAssistant.getCategoryColor(topAdvice.category).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, CombatAssistant.getCategoryColor(topAdvice.category).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { showOracleDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val icon = when (topAdvice.category) {
                            com.example.dndhelper.utils.AdviceCategory.CRITICAL -> Icons.Default.Warning
                            com.example.dndhelper.utils.AdviceCategory.TACTICAL -> Icons.Default.Info
                            else -> Icons.Default.Notifications
                        }
                        Icon(icon, null, tint = CombatAssistant.getCategoryColor(topAdvice.category), modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(topAdvice.title, fontWeight = FontWeight.Bold, color = CombatAssistant.getCategoryColor(topAdvice.category), fontSize = 13.sp)
                            Text(topAdvice.description, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(Icons.Default.ArrowForwardIos, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.clickable { showOracleDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = "Oracle", tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(16.dp))
                            Text(tr("Оракул", "Oracle"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
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
                Text(acLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("$armorClass", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (shieldEquipped) Icon(Icons.Default.Done, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(editLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        CombatStatSquare(
            label = initLabel, 
            value = initiativeText, 
            modifier = Modifier.weight(1f),
            onClick = {
                onRollRequest(DiceRollData(
                    title = initRollTitle,
                    rollType = RollType.AbilityCheck,
                    baseModifier = dexMod,
                    icon = Icons.Default.PlayArrow
                ))
            }
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
                Text(speedLabel, style = MaterialTheme.typography.labelSmall, color = if (isSpeedEffected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    text = effectiveSpeed,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSpeedEffected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                if (isSpeedEffected) {
                    Text(speedEffectLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(editLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
    }

    if (showSpeedDialog) {
        var speedInput by remember { mutableStateOf(character.speed) }
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text(editSpeedTitle) },
            text = {
                OutlinedTextField(
                    value = speedInput,
                    onValueChange = { speedInput = it },
                    label = { Text(speedInputLabel) },
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
                TextButton(onClick = { showSpeedDialog = false }) { Text(cancelLabel) }
            }
        )
    }

    if (showAcDialog) {
        val selectedArmor = if (selectedArmorIndex < armorList.size) armorList[selectedArmorIndex] else armorList[0]
        AlertDialog(
            onDismissRequest = { showAcDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Build, null, tint = MaterialTheme.colorScheme.primary)
                    Text(acCalcTitle, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                var searchQuery by remember { mutableStateOf("") }
                
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        placeholder = { Text(tr("Поиск брони...", "Search armor...")) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )

                    Text(selectArmorLabel, fontWeight = FontWeight.Bold)
                    
                    val filteredArmor = armorList.filter { 
                        (if(isEn) it.nameEn else it.nameRu).contains(searchQuery, ignoreCase = true) 
                    }

                    filteredArmor.forEachIndexed { index, armor ->
                        // Находим реальный индекс в исходном списке armorList
                        val realIndex = armorList.indexOf(armor)
                        val isSelected = selectedArmorIndex == realIndex
                        val label = if (isEn) armor.nameEn else armor.nameRu
                        val typeLabel = armorTypeLabels.getOrElse(armor.type) { armorTypeLabels[0] }

                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onCharacterChange(character.copy(selectedArmorIndex = realIndex)) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                else Icon(Icons.Default.RadioButtonChecked, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                    Text("$typeLabel | $baseLabel ${armor.baseAc}", fontSize = 11.sp, color = Color.Gray)
                                }
                                
                                // Кнопка удаления для кастомной брони
                                // Кастомная броня находится в конце списка armorList. 
                                // Но индекс может смещаться. Лучше проверять по имени в character.customArmors.
                                val customArmor = character.customArmors.find { it.name == armor.nameEn && it.baseAc == armor.baseAc && it.type == armor.type }
                                if (customArmor != null) {
                                    IconButton(
                                        onClick = {
                                            val updated = character.customArmors.toMutableList().apply { remove(customArmor) }
                                            onCharacterChange(character.copy(customArmors = updated))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAddArmorDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(tr("Добавить свою", "Add custom"))
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onCharacterChange(character.copy(shieldEquipped = !shieldEquipped)) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (shieldEquipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Done, null, tint = if (shieldEquipped) MaterialTheme.colorScheme.secondary else Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Text(shieldLabel, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Switch(checked = shieldEquipped, onCheckedChange = { onCharacterChange(character.copy(shieldEquipped = it)) })
                        }
                    }

                    Text(magicAcBonusLabel, fontWeight = FontWeight.Bold)
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
                                Text(finalAcLabel, fontWeight = FontWeight.Bold)
                                val dexBonusValue = when (selectedArmor.type) {
                                    0 -> dexMod
                                    1 -> dexMod
                                    2 -> dexMod.coerceAtMost(2)
                                    else -> 0
                                }
                                val shieldPart = if (shieldEquipped) tr(" + щит +2", " + shield +2") else ""
                                val magicPart = if (magicBonus > 0) " + маг +$magicBonus" else ""
                                
                                Text(
                                    text = "${if(isEn) selectedArmor.nameEn else selectedArmor.nameRu} + DEX ${if (dexBonusValue >= 0) "+$dexBonusValue" else "$dexBonusValue"}$shieldPart$magicPart",
                                    fontSize = 11.sp, 
                                    color = Color.Gray
                                )
                            }
                            Text("$armorClass", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAcDialog = false }) { Text(doneLabel) }
            }
        )
    }

    if (showAddArmorDialog) {
        var name by remember { mutableStateOf("") }
        var baseAcStr by remember { mutableStateOf("10") }
        var selectedType by remember { mutableIntStateOf(1) } // 1=Light, 2=Medium, 3=Heavy

        AlertDialog(
            onDismissRequest = { showAddArmorDialog = false },
            title = { Text(tr("Новая броня", "New Armor")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(tr("Название", "Name")) })
                    OutlinedTextField(
                        value = baseAcStr, 
                        onValueChange = { if (it.all { c -> c.isDigit() }) baseAcStr = it }, 
                        label = { Text(tr("Базовый КД", "Base AC")) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Text(tr("Тип брони:", "Armor Type:"), fontWeight = FontWeight.Bold)
                    Column {
                        armorTypeLabels.forEachIndexed { index, label ->
                            if (index == 0) return@forEachIndexed // Пропускаем "Без брони"
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedType = index }) {
                                RadioButton(selected = selectedType == index, onClick = { selectedType = index })
                                Text(label)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        val bac = baseAcStr.toIntOrNull() ?: 10
                        val newArmor = com.example.dndhelper.data.CustomArmor(name, bac, selectedType)
                        onCharacterChange(character.copy(customArmors = character.customArmors + newArmor))
                        showAddArmorDialog = false
                    }
                }) { Text(tr("Добавить", "Add")) }
            },
            dismissButton = { TextButton(onClick = { showAddArmorDialog = false }) { Text(tr("Отмена", "Cancel")) } }
        )
    }

    if (showOracleDialog) {
        AlertDialog(
            onDismissRequest = { showOracleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoFixHigh, null, tint = MaterialTheme.colorScheme.primary)
                    Text(tr("Совет Оракула", "Oracle's Advice"), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (adviceList.isEmpty()) {
                        Text(tr("Оракул молчит. Все хорошо.", "The Oracle is silent. All is well."), color = Color.Gray)
                    } else {
                        adviceList.forEach { advice ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CombatAssistant.getCategoryColor(advice.category).copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CombatAssistant.getCategoryColor(advice.category).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val icon = when (advice.category) {
                                            com.example.dndhelper.utils.AdviceCategory.CRITICAL -> Icons.Default.Warning
                                            com.example.dndhelper.utils.AdviceCategory.TACTICAL -> Icons.Default.Info
                                            com.example.dndhelper.utils.AdviceCategory.REMINDER -> Icons.Default.Notifications
                                        }
                                        Icon(icon, null, tint = CombatAssistant.getCategoryColor(advice.category), modifier = Modifier.size(16.dp))
                                        Text(advice.title, fontWeight = FontWeight.Bold, color = CombatAssistant.getCategoryColor(advice.category))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(advice.description, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showOracleDialog = false }) { Text(doneLabel) }
            }
        )
    }
}

@Composable
fun CombatStatSquare(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
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
