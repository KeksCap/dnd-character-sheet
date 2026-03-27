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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.foundation.layout.Column
import com.example.dndhelper.data.Spell
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.AsyncImage
import com.example.dndhelper.data.SpellInfo
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Delete
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.CharacterStorage
import com.example.dndhelper.data.StatSaveData
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.ElevatedCard
import com.example.dndhelper.data.Monster
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch


var charLevel by mutableStateOf("5")
val knownSpells = mutableStateListOf<SpellInfo>()

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
fun MainGameContent(
    spells: List<Spell>,
    classSpells: Map<String, List<String>>,
    language: String,
    // НОВЫЕ ПАРАМЕТРЫ:
    character: CharacterSaveData,
    storage: CharacterStorage,
    onCharacterChange: (CharacterSaveData) -> Unit,
    onBackToTavern: () -> Unit,
    bestiaryList: List<Monster>,
    bestiaryViewModel: BestiaryViewModel,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToTavern) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "В Таверну")
                }
                // Добавили modifier = Modifier.weight(1f), чтобы текст отодвинул шестеренку вправо
                Text(
                    text = "Имя: ${character.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                // --- ДОБАВЛЯЕМ КНОПКУ НАСТРОЕК ---
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Настройки")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = activeTab == 0, onClick = { activeTab = 0 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Лист") })
                NavigationBarItem(selected = activeTab == 1, onClick = { activeTab = 1 }, icon = { Icon(Icons.Default.Shield, null) }, label = { Text("Снаряжение") })
                // Переименовали в "Заклинания" и дали иконку палочки
                NavigationBarItem(selected = activeTab == 2, onClick = { activeTab = 2 }, icon = { Icon(Icons.Default.AutoFixHigh, null) }, label = { Text("Заклинания") })
                // Новая 4-я вкладка
                NavigationBarItem(selected = activeTab == 3, onClick = { activeTab = 3 }, icon = { Icon(Icons.Default.MenuBook, null) }, label = { Text("Справочник") })
            }
        }
    ) { padding ->
        // --- НАШЕ НОВОЕ ВСПЛЫВАЮЩЕЕ ОКНО ---
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Настройки", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Язык базы данных и заклинаний:")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = { onLanguageChange("ru"); showSettingsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (language == "ru") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text("Русский") }

                            Button(
                                onClick = { onLanguageChange("en"); showSettingsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (language == "en") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text("English") }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) { Text("Закрыть") }
                }
            )
        }
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (activeTab) {
                0 -> CharacterSheetTab(language = language, character = character, onCharacterChange = onCharacterChange)
                1 -> EquipmentTab()
                2 -> SpellsDirectoryTab(spells = spells, classSpells = classSpells, language = language, character = character, onCharacterChange = onCharacterChange)
                3 -> ReferenceTab(
                    monsters = bestiaryList,
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange
                )
            }
        }
    }
}

@Composable
fun SpellsDirectoryTab(
    spells: List<Spell>,
    classSpells: Map<String, List<String>>,
    language: String,
    character: CharacterSaveData, // <--- НОВОЕ
    onCharacterChange: (CharacterSaveData) -> Unit // <--- НОВОЕ
) {
    var selectedSpell by remember { mutableStateOf<SpellInfo?>(null) }
    var selectedClass by remember { mutableStateOf("Все") }
    var searchQuery by remember { mutableStateOf("") }

    // ДОБАВИЛИ: Состояние сортировки (0 - нет, 1 - по возрастанию, 2 - по убыванию)
    var sortOrder by remember { mutableIntStateOf(0) }

    val isRussian = language.lowercase().let { it == "ru" || it == "русский" || it == "russian" }

    // Умный фильтр по классу и тексту
    val filteredSpells = spells.filter { spell ->
        val matchesClass = if (selectedClass == "Все") {
            true
        } else {
            val allowedSpells = classSpells[selectedClass] ?: emptyList()
            val spellNameEn = spell.en?.name?.trim()?.lowercase() ?: ""
            allowedSpells.any { it.trim().lowercase() == spellNameEn }
        }

        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            val spellName = (if (isRussian) spell.ru?.name else spell.en?.name) ?: ""
            spellName.contains(searchQuery, ignoreCase = true)
        }

        matchesClass && matchesSearch
    }

    // ДОБАВИЛИ: Логика сортировки по уровню
    val sortedSpells = when (sortOrder) {
        1 -> filteredSpells.sortedBy { spell ->
            val lvl = if (isRussian) spell.ru?.level else spell.en?.level
            lvl.toString().toIntOrNull() ?: 0 // Заговоры (0) будут первыми
        }
        2 -> filteredSpells.sortedByDescending { spell ->
            val lvl = if (isRussian) spell.ru?.level else spell.en?.level
            lvl.toString().toIntOrNull() ?: 0 // Заклинания 9 уровня будут первыми
        }
        else -> filteredSpells // Как в оригинале
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (isRussian) "Справочник заклинаний" else "Spells Directory",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // ДОБАВИЛИ: Строка поиска и кнопка сортировки в одном ряду
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(if (isRussian) "Поиск заклинания..." else "Search spell...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                modifier = Modifier.weight(1f), // Поиск занимает всё свободное место
                singleLine = true
            )

            // КНОПКА СОРТИРОВКИ
            IconButton(
                onClick = { sortOrder = (sortOrder + 1) % 3 },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                val sortIcon = when (sortOrder) {
                    1 -> Icons.Default.ArrowUpward // По возрастанию
                    2 -> Icons.Default.ArrowDownward // По убыванию
                    else -> Icons.Default.Sort // По умолчанию
                }
                Icon(sortIcon, contentDescription = "Сортировка", tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            val classList = listOf("Все") + classSpells.keys.toList()
            items(classList) { className ->
                val isSelected = selectedClass == className
                Button(
                    onClick = { selectedClass = className },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(className)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // ИСПОЛЬЗУЕМ ОТСОРТИРОВАННЫЙ СПИСОК
            items(sortedSpells) { spell ->
                val finalInfo = if (isRussian) spell.ru else spell.en

                if (finalInfo != null) {
                    ListItem(
                        headlineContent = { Text(finalInfo.name ?: "Без названия") },
                        supportingContent = {
                            // Учитываем язык для справочника (Русский/Английский)
                            val levelText = if (finalInfo.level == "0") {
                                if (isRussian) "Заговор" else "Cantrip"
                            } else {
                                if (isRussian) "${finalInfo.level} уровень" else "Level ${finalInfo.level}"
                            }
                            Text("$levelText | ${finalInfo.school}")
                        },
                        overlineContent = { Text(finalInfo.range ?: "") },
                        modifier = Modifier.clickable { selectedSpell = finalInfo }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    selectedSpell?.let { spell ->
        AlertDialog(
            onDismissRequest = { selectedSpell = null },
            // ДОБАВИЛИ: Заголовок теперь со звездочкой!
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(spell.name ?: "", modifier = Modifier.weight(1f))

                    // КНОПКА ЗВЕЗДОЧКИ (ТЕПЕРЬ РАБОТАЕТ С ЧЕМОДАНОМ)
                    val isFavorite = character.knownSpells.contains(spell)
                    IconButton(onClick = {
                        val updatedSpells = if (isFavorite) {
                            character.knownSpells - spell // Удаляем
                        } else {
                            character.knownSpells + spell // Добавляем
                        }
                        onCharacterChange(character.copy(knownSpells = updatedSpells)) // Сохраняем в память
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "В избранное",
                            tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Время: ${spell.castingTime}", style = MaterialTheme.typography.labelLarge)
                    Text("Дистанция: ${spell.range}", style = MaterialTheme.typography.labelLarge)
                    Text("Компоненты: ${spell.components}", style = MaterialTheme.typography.labelLarge)
                    Text("Длительность: ${spell.duration}", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(12.dp))

                    val cleanText = spell.text?.replace("<br>", "\n\n") ?: "Описание отсутствует."
                    Text(cleanText, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSpell = null }) {
                    Text(if (isRussian) "Закрыть" else "Close")
                }
            }
        )
    }
}

@Composable
fun CharacterSheetTab(
    language: String,
    character: CharacterSaveData, // <--- НОВОЕ
    onCharacterChange: (CharacterSaveData) -> Unit // <--- НОВОЕ
) {
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

    // АВТОМАТИЧЕСКИЙ КД: 10 + Ловкость
    val armorClass = 10 + dexMod

    // Стейт для всплывающего окна заклинания
    var selectedKnownSpellInfo by remember { mutableStateOf<SpellInfo?>(null) }
    val isRussian = language.lowercase().let { it == "ru" || it == "русский" || it == "russian" }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

        // 1. Блок Информации (Header)
        HeaderInfoBlock(
            character = character,
            onCharacterChange = onCharacterChange
        )
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

        // 5. ИЗВЕСТНЫЕ ЗАКЛИНАНИЯ
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ИЗВЕСТНЫЕ ЗАКЛИНАНИЯ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // КНОПКА СОЗДАНИЯ КАСТОМНОГО ЗАКЛИНАНИЯ
            var showCreateSpellDialog by remember { mutableStateOf(false) }
            IconButton(onClick = { showCreateSpellDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать", tint = MaterialTheme.colorScheme.primary)
            }

            // --- ВСПЛЫВАЮЩЕЕ ОКНО ФОРМЫ СОЗДАНИЯ ---
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
                    title = { Text("Создать заклинание") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = newLevel, onValueChange = { newLevel = it }, label = { Text("Уровень (0-9)") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                OutlinedTextField(value = newTime, onValueChange = { newTime = it }, label = { Text("Время (напр. 1 действие)") }, modifier = Modifier.weight(1f), singleLine = true)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = newRange, onValueChange = { newRange = it }, label = { Text("Дистанция") }, modifier = Modifier.weight(1f), singleLine = true)
                                OutlinedTextField(value = newDuration, onValueChange = { newDuration = it }, label = { Text("Длительность") }, modifier = Modifier.weight(1f), singleLine = true)
                            }
                            OutlinedTextField(value = newComponents, onValueChange = { newComponents = it }, label = { Text("Компоненты (В, С, М)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text("Описание заклинания") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    // Собираем новое заклинание из текста
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
                                    // Добавляем в чемодан и сохраняем
                                    val updatedSpells = character.knownSpells + customSpell
                                    onCharacterChange(character.copy(knownSpells = updatedSpells))
                                    showCreateSpellDialog = false
                                }
                            }
                        ) { Text("Сохранить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateSpellDialog = false }) { Text("Отмена") }
                    }
                )
            }
        }

        // ВЫВОД ЗАКЛИНАНИЙ
        if (character.knownSpells.isEmpty()) {
            Text("Пока нет известных заклинаний. Добавьте их из Справочника или создайте своё!", color = Color.Gray)
        } else {
            character.knownSpells.forEach { spell ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedKnownSpellInfo = spell } // Клик для открытия информации
                ) {
                    ListItem(
                        headlineContent = { Text(spell.name ?: "") },
                        supportingContent = {
                            // Если уровень 0, пишем "Заговор", иначе "Уровень: X"
                            val levelText = if (spell.level == "0") "Заговор" else "Уровень: ${spell.level}"
                            Text("$levelText | ${spell.castingTime}")
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                // Удаление заклинания из чемодана
                                val updatedSpells = character.knownSpells.toMutableList().apply { remove(spell) }
                                onCharacterChange(character.copy(knownSpells = updatedSpells))
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Gray)
                            }
                        }
                    )
                }
            }
        }
    } // Это закрывающая скобка Column

    // 6. ВСПЛЫВАЮЩЕЕ ОКНО ИНФОРМАЦИИ
    selectedKnownSpellInfo?.let { spell ->
        AlertDialog(
            onDismissRequest = { selectedKnownSpellInfo = null },
            title = { Text(spell.name ?: "") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Время: ${spell.castingTime}", style = MaterialTheme.typography.labelLarge)
                    Text("Дистанция: ${spell.range}", style = MaterialTheme.typography.labelLarge)
                    Text("Компоненты: ${spell.components}", style = MaterialTheme.typography.labelLarge)
                    Text("Длительность: ${spell.duration}", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(12.dp))

                    val cleanText = spell.text?.replace("<br>", "\n\n") ?: "Описание отсутствует."
                    Text(cleanText, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedKnownSpellInfo = null }) {
                    Text(if (isRussian) "Закрыть" else "Close")
                }
            }
        )
    }
}
@Composable
fun HeaderInfoBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit
) {
    var isEditingHeader by remember { mutableStateOf(false) }

    // Временные переменные для полей ввода (берут старт из чемодана)
    var tempName by remember(character) { mutableStateOf(character.name) }
    var tempRace by remember(character) { mutableStateOf(character.race) }
    var tempClass by remember(character) { mutableStateOf(character.charClass) }
    var tempLevel by remember(character) { mutableStateOf(character.level) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Если выбрали фото, сразу перезаписываем чемодан
            onCharacterChange(character.copy(imageUri = uri.toString()))
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Аватарка
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (character.imageUri != null) {
                AsyncImage(
                    model = Uri.parse(character.imageUri), // Читаем URI из чемодана
                    contentDescription = "Аватар персонажа",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isEditingHeader) {
                OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(value = tempRace, onValueChange = { tempRace = it }, label = { Text("Раса") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = tempClass, onValueChange = { tempClass = it }, label = { Text("Класс") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = tempLevel, onValueChange = { tempLevel = it }, label = { Text("Уровень (1-20)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            } else {
                Text(character.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${character.race} • ${character.charClass} • Ур. ${character.level}", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            }
        }

        IconButton(
            modifier = Modifier.align(Alignment.Top),
            onClick = {
                if (isEditingHeader) {
                    // КОГДА НАЖАЛИ ГАЛОЧКУ: упаковываем новые данные в копию чемодана и отправляем наверх
                    val updatedCharacter = character.copy(
                        name = tempName,
                        race = tempRace,
                        charClass = tempClass,
                        level = tempLevel
                    )
                    onCharacterChange(updatedCharacter)
                }
                isEditingHeader = !isEditingHeader
            }
        ) {
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

@Composable
fun TavernScreen(
    storage: CharacterStorage,
    onCharacterSelected: (CharacterSaveData) -> Unit
) {
    // Загружаем всех персонажей из памяти телефона
    var characterList by remember { mutableStateOf(storage.getAllCharacters()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Таверна",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Кнопка создания нового персонажа
        Button(
            onClick = {
                // Создаем болванку с базовыми статами
                val defaultStats = listOf(
                    StatSaveData("Сила", 10, emptyMap()),
                    StatSaveData("Ловкость", 10, emptyMap()),
                    StatSaveData("Телосложение", 10, emptyMap()),
                    StatSaveData("Интеллект", 10, emptyMap()),
                    StatSaveData("Мудрость", 10, emptyMap()),
                    StatSaveData("Харизма", 10, emptyMap())
                )
                val newChar = CharacterSaveData(
                    name = "Новый искатель приключений",
                    race = "Человек",
                    charClass = "Воин",
                    level = "1",
                    maxHp = 10,
                    currentHp = 10,
                    imageUri = null,
                    stats = defaultStats,
                    knownSpells = emptyList()
                )
                storage.saveCharacter(newChar) // Сохраняем в сейф
                characterList = storage.getAllCharacters() // Обновляем экран
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Создать нового персонажа")
        }

        // Список существующих персонажей
        LazyColumn {
            items(characterList) { char ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { onCharacterSelected(char) } // При клике открываем его лист
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(char.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${char.race} • ${char.charClass} • Ур. ${char.level}", color = Color.Gray)
                        }
                        // Кнопка удаления (если сожрал дракон)
                        IconButton(onClick = {
                            storage.deleteCharacter(char.id)
                            characterList = storage.getAllCharacters()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferenceTab(
    monsters: List<Monster>,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("Бестиарий", "Расы", "Классы")

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val filteredMonsters = monsters.filter { it.name?.contains(searchQuery, ignoreCase = true) == true }
    var monsterForDetail by remember { mutableStateOf<Monster?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedCategory) {
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedCategory == index,
                    onClick = { selectedCategory = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedCategory) {
                0 -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Поиск...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Тумблер 2014/2024
                            RulesetToggle(currentRuleset, onRulesetChange)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredMonsters, key = { it.id }) { monster ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { monsterForDetail = monster },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(monster.name ?: "Неизвестный", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = translateSizeAndType(monster.size, monster.type),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            DraggableScrollbar(listState, filteredMonsters.size, Modifier.align(Alignment.CenterEnd))
                        }
                    }
                }
                1 -> Box(Modifier.fillMaxSize()) { Text("Эльфы, Дворфы, Тифлинги...", Modifier.align(Alignment.Center), Color.Gray) }
                2 -> Box(Modifier.fillMaxSize()) { Text("Воины, Плуты, Барды...", Modifier.align(Alignment.Center), Color.Gray) }
            }
        }
    }

    monsterForDetail?.let { monster ->
        MonsterDetailDialog(monster = monster, onDismiss = { monsterForDetail = null })
    }
}

@Composable
fun RulesetToggle(current: String, onToggle: (String) -> Unit) {
    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        listOf("2014", "2024").forEach { ver ->
            Text(
                text = ver,
                modifier = Modifier
                    .clickable { onToggle(ver) }
                    .background(if (current == ver) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (current == ver) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold, fontSize = 12.sp
            )
        }
    }
}

@Composable
fun DraggableScrollbar(listState: LazyListState, listSize: Int, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    if (listSize == 0) return
    val offsetProgress = if (listSize > 0) listState.firstVisibleItemIndex.toFloat() / listSize else 0f

    BoxWithConstraints(modifier = modifier.fillMaxHeight().width(32.dp)) {
        val trackHeight = constraints.maxHeight.toFloat()
        val currentOffsetY = (offsetProgress * trackHeight).coerceIn(0f, trackHeight - 100f)

        Box(
            modifier = Modifier
                .offset { IntOffset(0, currentOffsetY.toInt()) }
                .width(6.dp).height(40.dp).clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                .align(Alignment.TopEnd)
                .pointerInput(listSize) {
                    detectVerticalDragGestures { change, _ ->
                        val newProgress = (change.position.y / trackHeight).coerceIn(0f, 1f)
                        coroutineScope.launch { listState.scrollToItem((newProgress * listSize).toInt().coerceIn(0, listSize - 1)) }
                    }
                }
        )
    }
}