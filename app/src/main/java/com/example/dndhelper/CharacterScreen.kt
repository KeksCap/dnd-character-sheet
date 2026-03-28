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
import com.example.dndhelper.data.Race
import com.example.dndhelper.data.DndClass
import com.example.dndhelper.data.ClassFeature
import com.example.dndhelper.data.Subclass
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.launch


var charLevel by mutableStateOf("5")
val knownSpells = mutableStateListOf<SpellInfo>()

@Composable
fun trStat(name: String): String {
    val isEn = LocalAppLanguage.current == "en" || LocalAppLanguage.current == "english"
    if (!isEn) return name
    return when(name) {
        "Сила", "Strength" -> "Strength"
        "Ловкость", "Dexterity" -> "Dexterity"
        "Тело", "Constitution" -> "Constitution"
        "Инт", "Intelligence" -> "Intelligence"
        "Мудр", "Wisdom" -> "Wisdom"
        "Хар", "Charisma" -> "Charisma"
        // Skills
        "Атлетика" -> "Athletics"
        "Акробатика" -> "Acrobatics"
        "Ловкость рук" -> "Sleight of Hand"
        "Скрытность" -> "Stealth"
        "Анализ" -> "Investigation"
        "История" -> "History"
        "Магия" -> "Arcana"
        "Природа" -> "Nature"
        "Восприятие" -> "Perception"
        "Выживание" -> "Survival"
        "Убеждение" -> "Persuasion"
        "Обман" -> "Deception"
        else -> name
    }
}

// --- ЦВЕТ ШКОЛЫ МАГИИ ---
fun schoolColor(school: String?): Color {
    return when (school?.lowercase()?.trim()) {
        "conjuration", "призыв", "призыв" -> Color(0xFF7B1FA2)  // Фиолетовый
        "abjuration", "ограждение" -> Color(0xFF1565C0)          // Синий
        "necromancy", "некромантия" -> Color(0xFF37474F)            // Тёмно-серый
        "evocation", "проявление" -> Color(0xFFBF360C)             // Огненный
        "enchantment", "очарование" -> Color(0xFFAD1457)           // Розовый
        "transmutation", "преобразование" -> Color(0xFF2E7D32)     // Зелёный
        "illusion", "иллюзия" -> Color(0xFF00695C)                // Бирюзовый
        "divination", "прорицание" -> Color(0xFFF57F17)           // Янтарный
        else -> Color(0xFF546E7A)                                  // Серо-синий
    }
}

fun sourceName(source: String?): String {
    return when (source?.trim()?.uppercase()) {
        "PHB" -> "Player's Handbook"
        "XGTE" -> "Xanathar's Guide"
        "TCOE", "TASHA" -> "Tasha's Cauldron"
        "SCAG" -> "Sword Coast AG"
        "EGW" -> "Explorer's Guide"
        "FTD" -> "Fizban's Treasury"
        "SCC" -> "Strixhaven"
        "AI" -> "Acquisitions Inc."
        "TOEE" -> "Temple of Evil"
        "HB" -> "Homebrew"
        else -> source ?: ""
    }
}

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
    races: List<Race>,
    classes: List<DndClass>,
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
                    text = "${tr("Имя:", "Name:")} ${character.name}",
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
                NavigationBarItem(selected = activeTab == 0, onClick = { activeTab = 0 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text(tr("Лист", "Sheet")) })
                NavigationBarItem(selected = activeTab == 1, onClick = { activeTab = 1 }, icon = { Icon(Icons.Default.Shield, null) }, label = { Text(tr("Снаряжение", "Inventory")) })
                // Переименовали в "Заклинания" и дали иконку палочки
                NavigationBarItem(selected = activeTab == 2, onClick = { activeTab = 2 }, icon = { Icon(Icons.Default.AutoFixHigh, null) }, label = { Text(tr("Заклинания", "Spells")) })
                // Новая 4-я вкладка
                NavigationBarItem(selected = activeTab == 3, onClick = { activeTab = 3 }, icon = { Icon(Icons.Default.MenuBook, null) }, label = { Text(tr("Справочник", "Reference")) })
            }
        }
    ) { padding ->
        // --- НАШЕ НОВОЕ ВСПЛЫВАЮЩЕЕ ОКНО ---
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(tr("Настройки", "Settings"), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(tr("Язык базы данных и заклинаний:", "Database and spell language:"))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = { onLanguageChange("ru"); showSettingsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (language == "ru") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text(tr("Русский", "Russian")) }

                            Button(
                                onClick = { onLanguageChange("en"); showSettingsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (language == "en") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text(tr("English", "English")) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) { Text(tr("Закрыть", "Close")) }
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
                    races = races,
                    classes = classes,
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange,
                    language = language
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
            items(sortedSpells) { spell ->
                val finalInfo = if (isRussian) spell.ru else spell.en

                if (finalInfo != null) {
                    val schoolCol = schoolColor(finalInfo.school)
                    ListItem(
                        headlineContent = { Text(finalInfo.name ?: "Без названия", fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            val levelText = if (finalInfo.level == "0") {
                                if (isRussian) "Заговор" else "Cantrip"
                            } else {
                                if (isRussian) "${finalInfo.level} уровень" else "Level ${finalInfo.level}"
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                // Чип Школы
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = schoolCol.copy(alpha = 0.15f)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Canvas(modifier = Modifier.size(7.dp)) { drawCircle(color = schoolCol) }
                                        Spacer(Modifier.width(4.dp))
                                        Text(finalInfo.school ?: "", fontSize = 11.sp, color = schoolCol, fontWeight = FontWeight.Bold)
                                    }
                                }
                                // Уровень
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(levelText, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        },
                        trailingContent = {
                            Text(spell.en?.source ?: "", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        },
                        modifier = Modifier.clickable { selectedSpell = finalInfo }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    selectedSpell?.let { spell ->
        val schoolCol = schoolColor(spell.school)
        AlertDialog(
            onDismissRequest = { selectedSpell = null },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(spell.name ?: "", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        val isFavorite = character.knownSpells.contains(spell)
                        IconButton(onClick = {
                            val updatedSpells = if (isFavorite) character.knownSpells - spell else character.knownSpells + spell
                            onCharacterChange(character.copy(knownSpells = updatedSpells))
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                            )
                        }
                    }
                    // Чипы: школа и источник
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                        // Школа магии
                        if (!spell.school.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = schoolCol
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(12.dp), tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text(spell.school, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        // Источник или "Гомебрю"
                        if (!spell.source.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    sourceName(spell.source),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(if (isRussian) "Время: ${spell.castingTime}" else "Casting Time: ${spell.castingTime}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Дистанция: ${spell.range}" else "Range: ${spell.range}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Компоненты: ${spell.components}" else "Components: ${spell.components}", style = MaterialTheme.typography.labelLarge)
                    Text(if (isRussian) "Длительность: ${spell.duration}" else "Duration: ${spell.duration}", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    val cleanText = spell.text?.replace("<br>", "\n\n") ?: (if (isRussian) "Описание отсутствует." else "No description available.")
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

    val dexValue = stats.find { it.name == "Ловкость" || it.name == "Dexterity" }?.baseScore ?: 10
    val dexMod = (dexValue - 10) / 2

    // Инициатива (текст)
    val initiativeText = if (dexMod >= 0) "+$dexMod" else "$dexMod"

    // --- КД КАЛЬКУЛЯТОР ---
    // Список доспехов: Triple(Название, Базовый КД, Тип: 0=без, 1=лёгкий, 2=средний, 3=тяжёлый)
    data class ArmorEntry(val nameRu: String, val nameEn: String, val baseAc: Int, val type: Int)
    val armorList = listOf(
        ArmorEntry("Без брони", "Unarmored", 10, 0),
        ArmorEntry("Кожаный (лёгкий)", "Leather (Light)", 11, 1),
        ArmorEntry("Клёпаная кожа (лёгкий)", "Studded Leather (Light)", 12, 1),
        ArmorEntry("Шкурный (средний)", "Hide (Medium)", 12, 2),
        ArmorEntry("Кольчужная рубаха (средний)", "Chain Shirt (Medium)", 13, 2),
        ArmorEntry("Чешуйчатый (средний)", "Scale Mail (Medium)", 14, 2),
        ArmorEntry("Кираса (средний)", "Breastplate (Medium)", 14, 2),
        ArmorEntry("Пластинчатый (средний)", "Half Plate (Medium)", 15, 2),
        ArmorEntry("Кольчуга (тяжёлый)", "Ring Mail (Heavy)", 14, 3),
        ArmorEntry("Кольчужный (тяжёлый)", "Chain Mail (Heavy)", 16, 3),
        ArmorEntry("Чешуйчатый дракона (тяжёлый)", "Splint (Heavy)", 17, 3),
        ArmorEntry("Латы (тяжёлый)", "Plate (Heavy)", 18, 3)
    )
    var selectedArmorIndex by remember { mutableIntStateOf(0) }
    var shieldEquipped by remember { mutableStateOf(false) }
    var magicBonus by remember { mutableIntStateOf(0) }
    var showAcDialog by remember { mutableStateOf(false) }

    val selectedArmor = armorList[selectedArmorIndex]
    val dexBonus = when (selectedArmor.type) {
        0 -> dexMod            // Без брони: полный DEX
        1 -> dexMod            // Лёгкий: полный DEX
        2 -> dexMod.coerceAtMost(2) // Средний: DEX не более +2
        else -> 0              // Тяжёлый: DEX не добавляется
    }
    val armorClass = selectedArmor.baseAc + dexBonus + (if (shieldEquipped) 2 else 0) + magicBonus

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
                        onValueChange = { if (it.all { c -> c.isDigit() }) maxHpInput = it },
                        label = { Text(tr("Макс. ХП", "Max HP")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(150.dp).padding(bottom = 8.dp)
                    )
                } else {
                    Text("$currentHp / $maxHp", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                }

                val progress = (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
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
                                        if (scrollDelta < 0) currentHp = (currentHp + 1).coerceAtMost(maxHp)
                                        else currentHp = (currentHp - 1).coerceAtLeast(0)
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                        },
                    color = if (progress > 0.5f) Color(0xFF4CAF50) else Color(0xFFF44336),
                    trackColor = Color(0xFFFF5252)
                )

                // --- Кнопки УРОН и ЛЕЧЕНИЕ по +1 и +5 ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- УРОН ---
                    val damageRed = Color(0xFFB71C1C)
                    Button(
                        onClick = { currentHp = (currentHp - 5).coerceAtLeast(0) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = damageRed.copy(alpha = 0.75f)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("-5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { currentHp = (currentHp - 1).coerceAtLeast(0) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = damageRed),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("-1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(Modifier.width(4.dp))

                    // --- ЛЕЧЕНИЕ ---
                    val healGreen = Color(0xFF2E7D32)
                    Button(
                        onClick = { currentHp = (currentHp + 1).coerceAtMost(maxHp) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = healGreen.copy(alpha = 0.75f)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("+1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { currentHp = (currentHp + 5).coerceAtMost(maxHp) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = healGreen),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("+5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 3. Боевые параметры
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // КД — кликабельный, открывает калькулятор
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
            CombatStatSquare(tr("ИНИЦ.", "INIT."), initiativeText, Modifier.weight(1f))
            CombatStatSquare(tr("СКОРОСТЬ", "SPEED"), tr("30фт", "30ft"), Modifier.weight(1f))
        }

        // --- Диалог Калькулятора КД ---
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

                        // Тип брони
                        Text(tr("Выберите броню:", "Select armor:"), fontWeight = FontWeight.Bold)
                        armorList.forEachIndexed { index, armor ->
                            val isSelected = selectedArmorIndex == index
                            val label = if (LocalAppLanguage.current == "en") armor.nameEn else armor.nameRu
                            val typeLabel = when (armor.type) {
                                0 -> tr("Без брони", "Unarmored")
                                1 -> tr("Лёгкий", "Light")
                                2 -> tr("Средний", "Medium")
                                else -> tr("Тяжёлый", "Heavy")
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { selectedArmorIndex = index },
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

                        // Щит
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { shieldEquipped = !shieldEquipped },
                            shape = RoundedCornerShape(8.dp),
                            color = if (shieldEquipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, null, tint = if (shieldEquipped) MaterialTheme.colorScheme.secondary else Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Text(tr("Щит (+2)", "Shield (+2)"), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                Switch(checked = shieldEquipped, onCheckedChange = { shieldEquipped = it })
                            }
                        }

                        // Магический бонус
                        Text(tr("Магический бонус к броне:", "Magic armor bonus:"), fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { if (magicBonus > 0) magicBonus-- }, modifier = Modifier.size(40.dp)) { Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text("+$magicBonus", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                            }
                            IconButton(onClick = { if (magicBonus < 5) magicBonus++ }, modifier = Modifier.size(40.dp)) { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                        }

                        // Итоговый КД
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

        Text(tr("Характеристики", "Attributes"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

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
                text = tr("ИЗВЕСТНЫЕ ЗАКЛИНАНИЯ", "KNOWN SPELLS"),
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
                    title = { Text(tr("Создать заклинание", "Create Spell")) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text(tr("Название", "Name")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
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
            Text(tr("Пока нет известных заклинаний. Добавьте их из Справочника или создайте своё!", "No known spells yet. Add them from Reference or create one!"), color = Color.Gray)
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
                            val levelText = if (spell.level == "0") tr("Заговор", "Cantrip") else "${tr("Уровень:", "Level:")} ${spell.level}"
                            Text("$levelText | ${spell.castingTime}")
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                // Удаление заклинания из чемодана
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
    } // Это закрывающая скобка Column

    // 6. ВСПЛЫВАЮЩЕЕ ОКНО ИНФОРМАЦИИ
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
                    Spacer(modifier = Modifier.height(12.dp))

                    val cleanText = spell.text?.replace("<br>", "\n\n") ?: (if (isRussian) "Описание отсутствует." else "No description available.")
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
                OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text(tr("Имя", "Name")) }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(value = tempRace, onValueChange = { tempRace = it }, label = { Text(tr("Раса", "Race")) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = tempClass, onValueChange = { tempClass = it }, label = { Text(tr("Класс", "Class")) }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = tempLevel, onValueChange = { tempLevel = it }, label = { Text(tr("Уровень (1-20)", "Level (1-20)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            } else {
                Text(character.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${character.race} • ${character.charClass} • ${tr("Ур.", "Lv.")} ${character.level}", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
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
            Text(trStat(stat.name), fontWeight = FontWeight.Bold)

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

                    Text(
                        text = trStat(skill),
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
        Text(tr("Снаряжение", "Inventory"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // Оружие
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(tr("Оружие", "Weapons"), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(value = newWepName, onValueChange = { newWepName = it }, label = { Text(tr("Название", "Name")) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = newWepDamage, onValueChange = { newWepDamage = it }, label = { Text(tr("Урон", "Damage")) }, modifier = Modifier.weight(0.7f))
                    IconButton(onClick = { if (newWepName.isNotBlank()) { weapons.add(Weapon(weapons.size + 1, newWepName, newWepDamage)); newWepName = ""; newWepDamage = "" } }) { Icon(Icons.Default.Add, null) }
                }
                weapons.forEach { weapon ->
                    ListItem(headlineContent = { Text(weapon.name) }, supportingContent = { Text("${tr("Урон", "Damage")}: ${weapon.damage}") }, trailingContent = { IconButton(onClick = { weapons.remove(weapon) }) { Icon(Icons.Default.Delete, null) } })
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
    val currentLang = LocalAppLanguage.current
    // Загружаем всех персонажей из памяти телефона
    var characterList by remember { mutableStateOf(storage.getAllCharacters()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = tr("Таверна", "Tavern"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Кнопка создания нового персонажа
        Button(
            onClick = {
                val isEn = currentLang == "en"
                fun l(ru: String, en: String) = if (isEn) en else ru
                // Создаем болванку с базовыми статами
                val defaultStats = listOf(
                    StatSaveData(l("Сила", "Strength"), 10, emptyMap()),
                    StatSaveData(l("Ловкость", "Dexterity"), 10, emptyMap()),
                    StatSaveData(l("Телосложение", "Constitution"), 10, emptyMap()),
                    StatSaveData(l("Интеллект", "Intelligence"), 10, emptyMap()),
                    StatSaveData(l("Мудрость", "Wisdom"), 10, emptyMap()),
                    StatSaveData(l("Харизма", "Charisma"), 10, emptyMap())
                )
                val newChar = CharacterSaveData(
                    name = l("Новый искатель приключений", "New adventurer"),
                    race = l("Человек", "Human"),
                    charClass = l("Воин", "Fighter"),
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
            Text(tr("Создать нового персонажа", "Create new character"))
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
                            Icon(Icons.Default.Delete, contentDescription = tr("Удалить", "Delete"), tint = Color.Red)
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
    races: List<Race>,
    classes: List<DndClass>,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit,
    language: String
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf(
        tr("Бестиарий", "Bestiary"), 
        tr("Расы", "Races"), 
        tr("Классы", "Classes")
    )

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val filteredMonsters = monsters.filter { monster ->
        val matchesSearch = monster.name?.contains(searchQuery, ignoreCase = true) == true
        val matchesEdition = if (language == "en" || language == "english") {
            if (currentRuleset == "2014") monster.document == "wotc-srd" else monster.document != "wotc-srd"
        } else true
        matchesSearch && matchesEdition
    }
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
                                label = { Text(tr("Поиск...", "Search...")) },
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
                                            Text(monster.name ?: (if (language == "en") "Unknown" else "Неизвестный"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = translateSizeAndType(monster.size, monster.type, language),
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
                1 -> RacesList(
                    races = races, 
                    language = language, 
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange
                )
                2 -> ClassesList(
                    classes = classes,
                    language = language,
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange
                )
            }
        }
    }

    monsterForDetail?.let { monster ->
        MonsterDetailDialog(monster = monster, language = language, onDismiss = { monsterForDetail = null })
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

@Composable
fun RacesList(
    races: List<Race>,
    language: String,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit
) {
    var raceForDetail by remember { mutableStateOf<Race?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            RulesetToggle(currentRuleset, onRulesetChange)
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(races, key = { it.nameEn }) { race ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { raceForDetail = race },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val name = if (language == "en" || language == "english") race.nameEn else race.nameRu
                        Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    raceForDetail?.let { race ->
        RaceDetailDialog(race = race, language = language, onDismiss = { raceForDetail = null })
    }
}

@Composable
fun RaceDetailDialog(race: Race, language: String, onDismiss: () -> Unit) {
    val isEn = language == "en" || language == "english"
    val name = if (isEn) race.nameEn else race.nameRu
    val traits = if (isEn) race.traitsEn else race.traitsRu

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                traits.forEach { trait ->
                    if (trait.name.isNotEmpty()) {
                        Text(trait.name + ".", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Text(trait.desc)
                    Spacer(Modifier.height(8.dp))
                }

                if (race.subraces.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(tr("Подрасы", "Subraces"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    
                    race.subraces.forEach { subrace ->
                        val subName = if (isEn) subrace.nameEn else subrace.nameRu
                        val subTraits = if (isEn) subrace.traitsEn else subrace.traitsRu
                        
                        Text(subName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        
                        subTraits.forEach { trait ->
                            if (trait.name.isNotEmpty()) {
                                Text(trait.name + ".", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(trait.desc)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(tr("Закрыть", "Close")) }
        }
    )
}

@Composable
fun ClassesList(
    classes: List<DndClass>,
    language: String,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit
) {
    var selectedClass by remember { mutableStateOf<DndClass?>(null) }

    if (selectedClass != null) {
        ClassDetailScreen(
            dndClass = selectedClass!!,
            language = language,
            onBack = { selectedClass = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                RulesetToggle(currentRuleset, onRulesetChange)
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(classes, key = { it.nameEn }) { dndClass ->
                    val name = if (language == "en" || language == "english") dndClass.nameEn else dndClass.nameRu
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedClass = dndClass },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(tr("Кость хитов: ", "Hit Die: ") + dndClass.hitDie, color = Color.Gray, fontSize = 14.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassDetailScreen(
    dndClass: DndClass,
    language: String,
    onBack: () -> Unit
) {
    val isEn = language == "en" || language == "english"
    val name = if (isEn) dndClass.nameEn else dndClass.nameRu
    val traits = if (isEn) dndClass.traitsEn else dndClass.traitsRu
    val features = if (isEn) dndClass.featuresEn else dndClass.featuresRu
    val subclasses = if (isEn) dndClass.subclassesEn else dndClass.subclassesRu

    // Полноэкранный контейнер
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                // Основные черты
                Text(tr("ОСНОВНЫЕ ЧЕРТЫ", "CORE FEATURES"), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        traits.forEach { (key, value) ->
                           if (key != "---") {
                               Row(modifier = Modifier.fillMaxWidth()) {
                                   Text(key + ": ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp), fontSize = 13.sp)
                                   Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                               }
                           }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Способности по уровням
                Text(tr("УМЕНИЯ КЛАССА", "CLASS FEATURES"), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                features.forEach { feature ->
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${feature.level}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(feature.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(feature.desc, modifier = Modifier.padding(top = 4.dp, start = 32.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Подклассы
                if (subclasses.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(tr("ПОДКЛАССЫ", "SUBCLASSES"), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))

                    subclasses.forEach { subclass ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(subclass.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                                if (subclass.desc.isNotEmpty()) {
                                    Text(subclass.desc, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                }
                                
                                subclass.features.forEach { sf ->
                                    Spacer(Modifier.height(8.dp))
                                    Text("${tr("Уровень", "Level")} ${sf.level}: ${sf.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(sf.desc, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}