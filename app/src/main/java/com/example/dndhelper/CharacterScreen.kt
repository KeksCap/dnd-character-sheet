package com.example.dndhelper

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ (Видны во всем файле) ---
var charName by mutableStateOf("Гендальф")
var charRace by mutableStateOf("Человек")
var charClass by mutableStateOf("Маг")
var charLevel by mutableStateOf("5")
var isEditingHeader by mutableStateOf(false)

// --- МОДЕЛИ ДАННЫХ ---
data class AbilityScore(
    val name: String,
    val baseScore: Int,
    val icon: ImageVector,
    val skills: List<String> = emptyList(),
    val skillProficiencies: MutableMap<String, Int> = mutableStateMapOf()
)
data class InventoryItem(val id: Int, var name: String, var count: Int)
data class Weapon(val id: Int, val name: String, val damage: String)
data class Armor(val id: Int, val name: String, val ac: Int)

@Composable
fun MainGameContent() {
    var activeTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = activeTab == 0, onClick = { activeTab = 0 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Лист") })
                NavigationBarItem(selected = activeTab == 1, onClick = { activeTab = 1 }, icon = { Icon(Icons.Default.Shield, null) }, label = { Text("Снаряжение") })
                NavigationBarItem(selected = activeTab == 2, onClick = { activeTab = 2 }, icon = { Icon(Icons.Default.MenuBook, null) }, label = { Text("Справочник") })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (activeTab) {
                0 -> CharacterSheetTab()
                1 -> EquipmentTab()
                2 -> SpellsDirectoryTab()
            }
        }
    }
}

@Composable
fun CharacterSheetTab() {
    // Состояния здоровья и характеристик
    var maxHpInput by remember { mutableStateOf("45") }
    val maxHp = maxHpInput.toIntOrNull() ?: 1
    var currentHp by remember { mutableIntStateOf(39) }

    var stats by remember { mutableStateOf(listOf(
        AbilityScore("Сила", 16, Icons.Default.FitnessCenter, listOf("Атлетика")),
        AbilityScore("Ловкость", 7, Icons.Default.DirectionsRun, listOf("Акробатика", "Ловкость рук", "Скрытность")),
        AbilityScore("Тело", 15, Icons.Default.Favorite),
        AbilityScore("Инт", 10, Icons.Default.MenuBook, listOf("Анализ", "История", "Магия", "Природа")),
        AbilityScore("Мудр", 12, Icons.Default.Visibility, listOf("Восприятие", "Выживание")),
        AbilityScore("Хар", 8, Icons.Default.SelfImprovement, listOf("Убеждение", "Обман"))
    ))}

    val dexValue = stats.find { it.name == "Ловкость" }?.baseScore ?: 10
    val dexMod = (dexValue - 10) / 2

    // Инициатива (текст)
    val initiativeText = if (dexMod >= 0) "+$dexMod" else "$dexMod"

    // АВТОМАТИЧЕСКИЙ КД: 10 + Ловкость (позже добавим учет брони из вкладки снаряжения)
    val armorClass = 10 + dexMod

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

        // 1. Блок Информации (Header)
        HeaderInfoBlock()

        Spacer(Modifier.height(16.dp))

        // --- БЛОК ЗДОРОВЬЯ ---
        var isEditingHp by remember { mutableStateOf(false) } // Состояние для редактирования макс. ХП

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(48.dp)) // Для центровки заголовка
            Text(
                text = "ЗДОРОВЬЕ",
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
                        onValueChange = { if (it.all { c -> c.isDigit() }) maxHpInput = it },
                        label = { Text("Макс. ХП") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(150.dp).padding(bottom = 8.dp)
                    )
                } else {
                    Text("$currentHp / $maxHp", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                }

                val progress = (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape),
                    color = if (progress > 0.5f) Color(0xFF4CAF50) else Color(0xFFF44336),
                    trackColor = Color(0xFFFF5252)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { if (currentHp > 0) currentHp-- }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))) { Text("УРОН") }
                    Button(onClick = { if (currentHp < maxHp) currentHp++ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("ЛЕЧЕНИЕ") }
                }
            }
        }

        // 3. Боевые параметры
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // КД теперь берется из переменной armorClass
            CombatStatSquare("КД", "$armorClass", Modifier.weight(1f))
            CombatStatSquare("ИНИЦ.", initiativeText, Modifier.weight(1f))
            CombatStatSquare("СКОРОСТЬ", "30фт", Modifier.weight(1f))
        }

        Text("Характеристики", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        // 4. Сетка характеристик
        stats.chunked(2).forEach { rowData ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowData.forEach { statData ->
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            stat = statData,
                            onValueChange = { newValue ->
                                stats = stats.map { if (it.name == statData.name) it.copy(baseScore = newValue) else it }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderInfoBlock() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватарка
        Box(
            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isEditingHeader) {
                OutlinedTextField(
                    value = charName,
                    onValueChange = { charName = it.filter { !it.isDigit() } },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(value = charRace, onValueChange = { charRace = it }, label = { Text("Раса") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = charClass, onValueChange = { charClass = it }, label = { Text("Класс") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = charLevel,
                    onValueChange = { if (it.isEmpty() || (it.all { c -> c.isDigit() } && it.toInt() <= 20)) charLevel = it },
                    label = { Text("Уровень (1-20)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            } else {
                Text(charName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("$charRace • $charClass • Ур. $charLevel", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            }
        }

        IconButton(onClick = { isEditingHeader = !isEditingHeader }, modifier = Modifier.align(Alignment.Top)) {
            Icon(if (isEditingHeader) Icons.Default.Check else Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun StatCard(stat: AbilityScore, onValueChange: (Int) -> Unit) {
    val modifier = (stat.baseScore - 10) / 2
    val profBonus = getProficiencyBonus(charLevel)

    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(stat.icon, null, tint = Color(0xFF6750A4), modifier = Modifier.size(24.dp))
            Text(stat.name, fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onValueChange(stat.baseScore - 1) }) { Text("-", fontSize = 24.sp) }
                Text("${stat.baseScore}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onValueChange(stat.baseScore + 1) }) { Text("+", fontSize = 24.sp) }
            }

            Surface(color = Color(0xFFEADDFF), shape = RoundedCornerShape(4.dp)) {
                Text(if (modifier >= 0) "+$modifier" else "$modifier", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            stat.skills.forEach { skill ->
                val state = stat.skillProficiencies[skill] ?: 0
                // Рассчитываем итоговый бонус (Модификатор + Бонус мастерства, если выбран кружок)
                val profBonus = getProficiencyBonus(charLevel)
                val totalSkillBonus = modifier + (state * profBonus)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            // Переключаем состояние: пусто -> мастер -> эксперт
                            stat.skillProficiencies[skill] = (state + 1) % 3
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when(state) {
                            1 -> Icons.Default.CheckCircle // Мастер
                            2 -> Icons.Default.Stars       // Эксперт
                            else -> Icons.Default.RadioButtonUnchecked // Пусто
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (state > 0) Color(0xFF6750A4) else Color.Gray
                    )

                    // ВОТ ЭТОТ КУСОЧЕК, ПРО КОТОРЫЙ ТЫ СПРАШИВАЛ:
                    Text(
                        text = skill,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = if (state > 0) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = if (totalSkillBonus >= 0) "+$totalSkillBonus" else "$totalSkillBonus",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CombatStatSquare(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(75.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun EquipmentTab() {
    val weapons = remember { mutableStateListOf(Weapon(1, "Длинный меч", "1d8+3")) }
    val armors = remember { mutableStateListOf(Armor(1, "Кольчуга", 16)) }

    var newWepName by remember { mutableStateOf("") }
    var newWepDamage by remember { mutableStateOf("") }
    var newArmName by remember { mutableStateOf("") }
    var newArmAc by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Снаряжение", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // Оружие
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Оружие", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(value = newWepName, onValueChange = { newWepName = it }, label = { Text("Название") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = newWepDamage, onValueChange = { newWepDamage = it }, label = { Text("Урон") }, modifier = Modifier.weight(0.7f))
                    IconButton(onClick = { if (newWepName.isNotBlank()) { weapons.add(Weapon(weapons.size + 1, newWepName, newWepDamage)); newWepName = ""; newWepDamage = "" } }) { Icon(Icons.Default.Add, null) }
                }
                weapons.forEach { weapon ->
                    ListItem(headlineContent = { Text(weapon.name) }, supportingContent = { Text("Урон: ${weapon.damage}") }, trailingContent = { IconButton(onClick = { weapons.remove(weapon) }) { Icon(Icons.Default.Delete, null) } })
                }
            }
        }
    }
}

@Composable
fun SpellsDirectoryTab() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AutoStories, modifier = Modifier.size(64.dp), contentDescription = null)
        Text("Справочник dnd.su", style = MaterialTheme.typography.headlineSmall)
    }
}

fun getProficiencyBonus(level: String): Int {
    val lvl = level.toIntOrNull() ?: 1
    return when {
        lvl in 1..4 -> 2
        lvl in 5..8 -> 3
        lvl in 9..12 -> 4
        lvl in 13..16 -> 5
        lvl in 17..20 -> 6
        else -> 2
    }
}
@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    val languages = listOf(
        "Русский" to "RU",
        "English" to "EN",
        "Español" to "ES",
        "日本語" to "JA",
        "中文" to "ZH"
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Выберите язык",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        languages.forEach { (name, code) ->
            Button(
                onClick = { onLanguageSelected(code) },
                modifier = Modifier.fillMaxWidth(0.7f).padding(4.dp)
            ) {
                Text(text = name)
            }
        }
    }
}