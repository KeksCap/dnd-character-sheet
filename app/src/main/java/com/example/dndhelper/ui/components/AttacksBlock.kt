package com.example.dndhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.CustomWeapon
import com.example.dndhelper.data.StandardEquipment
import com.example.dndhelper.tr
import com.example.dndhelper.ui.models.DiceRollData
import com.example.dndhelper.ui.models.RollType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttacksBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    isEn: Boolean,
    dexMod: Int,
    strMod: Int,
    profBonus: Int,
    hasDisadvantageOnAttacks: Boolean,
    onRollRequest: (DiceRollData) -> Unit
) {
    var showAddAttackDialog by remember { mutableStateOf(false) }
    var showListAttackDialog by remember { mutableStateOf(false) }

    // --- ЕДИНЫЙ БЛОК ПЕРЕВОДОВ ---
    val attacksTitle = tr("АТАКИ", "ATTACKS")
    val noWeaponsText = tr("Нет экипированного оружия", "No equipped weapons")
    val attackPrefix = tr("Атака:", "Attack:")
    val damagePrefix = tr("Урон:", "Damage:")
    val fromListText = tr("Из списка", "From List")
    val customText = tr("Своё", "Custom")
    val strLabel = tr("СИЛ", "STR")
    val dexLabel = tr("ЛОВ", "DEX")
    val closeText = tr("Закрыть", "Close")
    val selectWeaponTitle = tr("Выберите оружие", "Select Weapon")
    val newAttackTitle = tr("Новая атака", "New Attack")
    val nameLabel = tr("Название", "Name")
    val dmgHint = tr("Урон (напр. 1d8+3)", "Damage (e.g. 1d8+3)")
    val abilityTitle = tr("Характеристика:", "Ability:")
    val proficiencyLabel = tr("Владение", "Proficiency")
    val magicBonusLabel = tr("Маг. бонус", "Magic Bonus")
    val addText = tr("Добавить", "Add")
    val cancelText = tr("Отмена", "Cancel")

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))
        Text(attacksTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val weapons = StandardEquipment.getWeapons()
                val equippedStandard = weapons.filter { character.equippedStandardWeapons.contains(it.nameEn) }
                
                if (character.customWeapons.isEmpty() && equippedStandard.isEmpty()) {
                    Text(noWeaponsText, color = Color.Gray, fontSize = 12.sp)
                }

                // Стандартное оружие
                equippedStandard.forEach { weapon ->
                    val weaponName = if (isEn) weapon.nameEn else weapon.nameRu
                    val dmg = (if (isEn) weapon.damageEn else weapon.damage) ?: ""
                    val isDefaultRanged = listOf("Shortbow", "Longbow", "Crossbow", "Sling", "Blowgun", "Dart").any { weapon.nameEn.contains(it, ignoreCase = true) }
                    val ability = character.standardWeaponAbilities[weapon.nameEn] ?: (if (isDefaultRanged) "DEX" else "STR")
                    val isProf = character.standardWeaponProficiencies[weapon.nameEn] ?: true
                    val modValue = if (ability == "DEX") dexMod else strMod
                    val totalHit = modValue + (if (isProf) profBonus else 0)
                    val hitSign = if (totalHit >= 0) "+" else ""

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(weaponName, fontWeight = FontWeight.Bold)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasDisadvantageOnAttacks) {
                                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(2.dp))
                                }
                                Box(modifier = Modifier.clickable {
                                    onRollRequest(DiceRollData(
                                        title = "$attackPrefix $weaponName",
                                        rollType = RollType.AttackHit,
                                        baseModifier = totalHit,
                                        icon = Icons.Default.PlayArrow
                                    ))
                                }) {
                                    Text("$hitSign$totalHit", fontWeight = FontWeight.ExtraBold, color = if (hasDisadvantageOnAttacks) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(Modifier.width(4.dp))
                                Text("(${if (ability == "STR") strLabel else dexLabel} ${if (isProf) "+ $profBonus" else ""})", fontSize = 10.sp, color = Color.Gray)
                                Spacer(Modifier.width(12.dp))
                                
                                Box(modifier = Modifier.clickable {
                                    onRollRequest(parseDamageString(dmg, modValue, "$damagePrefix $weaponName"))
                                }) {
                                    Text(dmg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    val newAbility = if (ability == "STR") "DEX" else "STR"
                                    onCharacterChange(character.copy(standardWeaponAbilities = character.standardWeaponAbilities + (weapon.nameEn to newAbility)))
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(30.dp).widthIn(min = 40.dp)
                            ) {
                                Text(if (ability == "STR") strLabel else dexLabel, fontSize = 10.sp)
                            }
                            
                            IconButton(onClick = { onCharacterChange(character.copy(standardWeaponProficiencies = character.standardWeaponProficiencies + (weapon.nameEn to !isProf))) }, modifier = Modifier.size(30.dp)) {
                                Icon(imageVector = if (isProf) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isProf) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                            IconButton(onClick = { onCharacterChange(character.copy(equippedStandardWeapons = character.equippedStandardWeapons.filter { it != weapon.nameEn })) }, modifier = Modifier.size(30.dp)) { 
                                Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp), tint = Color.Gray) 
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }

                // Кастомное оружие
                character.customWeapons.forEach { weapon ->
                    val modValue = if (weapon.ability == "DEX") dexMod else strMod
                    val totalHit = modValue + (if (weapon.isProficient) profBonus else 0) + weapon.magicBonus
                    val hitSign = if (totalHit >= 0) "+" else ""

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(weapon.name, fontWeight = FontWeight.Bold)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasDisadvantageOnAttacks) {
                                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(2.dp))
                                }
                                Box(modifier = Modifier.clickable {
                                    onRollRequest(DiceRollData(title = "$attackPrefix ${weapon.name}", rollType = RollType.AttackHit, baseModifier = totalHit, icon = Icons.Default.PlayArrow))
                                }) {
                                    Text("$hitSign$totalHit", fontWeight = FontWeight.ExtraBold, color = if (hasDisadvantageOnAttacks) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(Modifier.width(4.dp))
                                val breakdownStr = "(${if (weapon.ability == "STR") strLabel else dexLabel}${if (weapon.isProficient) " + $profBonus" else ""}${if (weapon.magicBonus != 0) " + ${weapon.magicBonus}" else ""})"
                                Text(breakdownStr, fontSize = 10.sp, color = Color.Gray)
                                Spacer(Modifier.width(12.dp))
                                
                                Box(modifier = Modifier.clickable {
                                    onRollRequest(parseDamageString(weapon.damage, modValue, "$damagePrefix ${weapon.name}"))
                                }) {
                                    Text(weapon.damage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        IconButton(onClick = { onCharacterChange(character.copy(customWeapons = character.customWeapons.filter { it != weapon })) }) { 
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp), tint = Color.Gray) 
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showListAttackDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(fromListText, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showAddAttackDialog = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(customText, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showListAttackDialog) {
        AlertDialog(
            onDismissRequest = { showListAttackDialog = false },
            title = { Text(selectWeaponTitle) },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    StandardEquipment.getWeapons().forEach { weapon ->
                        val name = if (isEn) weapon.nameEn else weapon.nameRu
                        val dmg = (if (isEn) weapon.damageEn else weapon.damage) ?: ""
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (!character.equippedStandardWeapons.contains(weapon.nameEn)) {
                                    onCharacterChange(character.copy(equippedStandardWeapons = character.equippedStandardWeapons + weapon.nameEn))
                                }
                                showListAttackDialog = false
                            }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.bodyLarge)
                                Text(dmg, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            if (character.equippedStandardWeapons.contains(weapon.nameEn)) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(Icons.Default.Add, null, tint = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showListAttackDialog = false }) { Text(closeText) } }
        )
    }

    if (showAddAttackDialog) {
        var name by remember { mutableStateOf("") }
        var dmg by remember { mutableStateOf("") }
        var ability by remember { mutableStateOf("STR") }
        var isProf by remember { mutableStateOf(true) }
        var mBonus by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddAttackDialog = false },
            title = { Text(newAttackTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(nameLabel) })
                    OutlinedTextField(value = dmg, onValueChange = { dmg = it }, label = { Text(dmgHint) })
                    Text(abilityTitle)
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = ability == "STR", onClick = { ability = "STR" })
                            Text(strLabel)
                        }
                        Spacer(Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = ability == "DEX", onClick = { ability = "DEX" })
                            Text(dexLabel)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isProf, onCheckedChange = { isProf = it })
                        Text(proficiencyLabel)
                    }
                    OutlinedTextField(value = mBonus, onValueChange = { if (it.all { c -> c.isDigit() || c == '-' }) mBonus = it }, label = { Text(magicBonusLabel) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        val mb = mBonus.toIntOrNull() ?: 0
                        onCharacterChange(character.copy(customWeapons = character.customWeapons + CustomWeapon(name, dmg, ability, isProf, mb)))
                        showAddAttackDialog = false
                    }
                }) { Text(addText) }
            },
            dismissButton = { TextButton(onClick = { showAddAttackDialog = false }) { Text(cancelText) } }
        )
    }
}

private fun parseDamageString(damageStr: String, abilityMod: Int, title: String): DiceRollData {
    val regex = Regex("""(\d+)?d(\d+)\s*([+-]\s*\d+)?""")
    val match = regex.find(damageStr)
    return if (match != null) {
        val diceCount = match.groups[1]?.value?.toIntOrNull() ?: 1
        val diceSides = match.groups[2]?.value?.toIntOrNull() ?: 6
        val staticPart = match.groups[3]?.value?.replace(" ", "")?.replace("+", "")?.toIntOrNull() ?: 0
        DiceRollData(title = title, rollType = RollType.Damage, baseModifier = abilityMod + staticPart, diceCount = diceCount, diceSides = diceSides, icon = Icons.Default.Star)
    } else {
        DiceRollData(title = title, rollType = RollType.Damage, baseModifier = abilityMod, diceCount = 1, diceSides = 6, icon = Icons.Default.Star)
    }
}
