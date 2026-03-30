package com.example.dndhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.CustomWeapon
import com.example.dndhelper.data.StandardEquipment
import com.example.dndhelper.tr

@Composable
fun AttacksBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    isEn: Boolean,
    dexMod: Int,
    strMod: Int,
    profBonus: Int,
    hasDisadvantageOnAttacks: Boolean
) {
    var showAddAttackDialog by remember { mutableStateOf(false) }
    var showListAttackDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))
        Text(tr("АТАКИ", "ATTACKS"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val equippedStandard = StandardEquipment.getWeapons().filter { character.equippedStandardWeapons.contains(it.nameEn) }
                
                if (character.customWeapons.isEmpty() && equippedStandard.isEmpty()) {
                    Text(tr("Нет экипированного оружия", "No equipped weapons"), color = Color.Gray, fontSize = 12.sp)
                }

                // Стандартное оружие
                equippedStandard.forEach { weapon ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isEn) weapon.nameEn else weapon.nameRu, fontWeight = FontWeight.Bold)
                            val dmg = if (isEn) weapon.damageEn else weapon.damage
                            
                            val isDefaultRanged = listOf("Shortbow", "Longbow", "Crossbow", "Sling", "Blowgun", "Dart").any { weapon.nameEn.contains(it, ignoreCase = true) }
                            val ability = character.standardWeaponAbilities[weapon.nameEn] ?: (if (isDefaultRanged) "DEX" else "STR")
                            val isProf = character.standardWeaponProficiencies[weapon.nameEn] ?: true
                            
                            val modValue = if (ability == "DEX") dexMod else strMod
                            val totalHit = modValue + (if (isProf) profBonus else 0)
                            val hitSign = if (totalHit >= 0) "+" else ""
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasDisadvantageOnAttacks) {
                                    Icon(Icons.Filled.Warning, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(2.dp))
                                }
                                Text("$hitSign$totalHit", fontWeight = FontWeight.ExtraBold, color = if (hasDisadvantageOnAttacks) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(4.dp))
                                Text("(${if (ability == "STR") tr("СИЛ", "STR") else tr("ЛОВ", "DEX")} ${if (isProf) "+ $profBonus" else ""})", fontSize = 10.sp, color = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Text(dmg ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val ability = character.standardWeaponAbilities[weapon.nameEn] ?: (if (listOf("Shortbow", "Longbow", "Crossbow", "Sling", "Blowgun", "Dart").any { weapon.nameEn.contains(it, ignoreCase = true) }) "DEX" else "STR")
                            TextButton(
                                onClick = {
                                    val newAbility = if (ability == "STR") "DEX" else "STR"
                                    onCharacterChange(character.copy(standardWeaponAbilities = character.standardWeaponAbilities + (weapon.nameEn to newAbility)))
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(30.dp).widthIn(min = 40.dp)
                            ) {
                                Text(if (ability == "STR") tr("СИЛ", "STR") else tr("ЛОВ", "DEX"), fontSize = 10.sp)
                            }
                            
                            val isProf = character.standardWeaponProficiencies[weapon.nameEn] ?: true
                            IconButton(
                                onClick = {
                                    onCharacterChange(character.copy(standardWeaponProficiencies = character.standardWeaponProficiencies + (weapon.nameEn to !isProf)))
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isProf) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isProf) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }

                            IconButton(onClick = {
                                val updated = character.equippedStandardWeapons.filter { it != weapon.nameEn }
                                onCharacterChange(character.copy(equippedStandardWeapons = updated))
                            }, modifier = Modifier.size(30.dp)) { 
                                Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp), tint = Color.Gray) 
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }

                // Кастомное оружие
                character.customWeapons.forEach { weapon ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(weapon.name, fontWeight = FontWeight.Bold)
                            
                            val modValue = if (weapon.ability == "DEX") dexMod else strMod
                            val totalHit = modValue + (if (weapon.isProficient) profBonus else 0) + weapon.magicBonus
                            val hitSign = if (totalHit >= 0) "+" else ""

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasDisadvantageOnAttacks) {
                                    Icon(Icons.Filled.Warning, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(2.dp))
                                }
                                Text("$hitSign$totalHit", fontWeight = FontWeight.ExtraBold, color = if (hasDisadvantageOnAttacks) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(4.dp))
                                val breakdown = "(${if (weapon.ability == "STR") tr("СИЛ", "STR") else tr("ЛОВ", "DEX")}${if (weapon.isProficient) " + $profBonus" else ""}${if (weapon.magicBonus != 0) " + ${weapon.magicBonus}" else ""})"
                                Text(breakdown, fontSize = 10.sp, color = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Text(weapon.damage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = {
                            val updated = character.customWeapons.toMutableList().apply { remove(weapon) }
                            onCharacterChange(character.copy(customWeapons = updated))
                        }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp), tint = Color.Gray) }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showListAttackDialog = true }) {
                        Icon(Icons.Default.List, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr("Из списка", "From List"), fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showAddAttackDialog = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr("Своё", "Custom"), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showListAttackDialog) {
        AlertDialog(
            onDismissRequest = { showListAttackDialog = false },
            title = { Text(tr("Выберите оружие", "Select Weapon")) },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    StandardEquipment.getWeapons().forEach { weapon ->
                        val name = if (isEn) weapon.nameEn else weapon.nameRu
                        val dmg = if (isEn) weapon.damageEn else weapon.damage
                        ListItem(
                            modifier = Modifier.clickable {
                                if (!character.equippedStandardWeapons.contains(weapon.nameEn)) {
                                    onCharacterChange(character.copy(equippedStandardWeapons = character.equippedStandardWeapons + weapon.nameEn))
                                }
                                showListAttackDialog = false
                            },
                            headlineContent = { Text(name) },
                            supportingContent = { Text(dmg ?: "") },
                            trailingContent = {
                                if (character.equippedStandardWeapons.contains(weapon.nameEn)) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showListAttackDialog = false }) { Text(tr("Закрыть", "Close")) } }
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
            title = { Text(tr("Новая атака", "New Attack")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(tr("Название", "Name")) })
                    OutlinedTextField(value = dmg, onValueChange = { dmg = it }, label = { Text(tr("Урон (напр. 1d8+3)", "Damage (e.g. 1d8+3)")) })
                    
                    Text(tr("Характеристика:", "Ability:"))
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = ability == "STR", onClick = { ability = "STR" })
                            Text(tr("СИЛ", "STR"))
                        }
                        Spacer(Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = ability == "DEX", onClick = { ability = "DEX" })
                            Text(tr("ЛОВ", "DEX"))
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isProf, onCheckedChange = { isProf = it })
                        Text(tr("Владение", "Proficiency"))
                    }
                    
                    OutlinedTextField(
                        value = mBonus, 
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '-' }) mBonus = it }, 
                        label = { Text(tr("Маг. бонус / Доп.", "Magic / Misc Bonus")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        val mb = mBonus.toIntOrNull() ?: 0
                        onCharacterChange(character.copy(customWeapons = character.customWeapons + CustomWeapon(name, dmg, ability, isProf, mb)))
                        showAddAttackDialog = false
                    }
                }) { Text(tr("Добавить", "Add")) }
            },
            dismissButton = { TextButton(onClick = { showAddAttackDialog = false }) { Text(tr("Отмена", "Cancel")) } }
        )
    }
}
