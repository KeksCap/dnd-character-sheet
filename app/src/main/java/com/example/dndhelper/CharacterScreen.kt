package com.example.dndhelper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.data.*
import com.example.dndhelper.ui.tabs.*
import com.example.dndhelper.ui.components.*
import com.example.dndhelper.ui.theme.AppTheme

@Composable
fun MainGameContent(
    spells: List<Spell>,
    classSpells: Map<String, List<String>>,
    races: List<Race>,
    classes: List<DndClass>,
    backgrounds: List<Background>,
    language: String,
    character: CharacterSaveData,
    storage: CharacterStorage,
    onCharacterChange: (CharacterSaveData) -> Unit,
    onBackToTavern: () -> Unit,
    bestiaryList: List<Monster>,
    bestiaryViewModel: BestiaryViewModel,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    selectedTheme: AppTheme,
    magicItems: List<MagicItem>,
    gameLogStorage: GameLogStorage
) {
    var activeTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        StandardEquipment.initialize(context)
    }

    // --- Игровой журнал: состояние хранится отдельно от персонажа ---
    var gameLogEntries by remember(character.id) {
        mutableStateOf(gameLogStorage.getLog(character.id))
    }
    val addLogEntry: (GameLogEntry) -> Unit = { entry ->
        val updated = (listOf(entry) + gameLogEntries).take(com.example.dndhelper.utils.GameLogManager.MAX_LOG_SIZE)
        gameLogEntries = updated
        gameLogStorage.saveLog(character.id, updated)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToTavern) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "В Таверну")
                }
                Text(
                    text = "${tr("Имя:", "Name:")} ${character.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Настройки")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = activeTab == 0, onClick = { activeTab = 0 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text(tr("Лист", "Sheet")) })
                NavigationBarItem(selected = activeTab == 1, onClick = { activeTab = 1 }, icon = { Icon(Icons.Default.Shield, null) }, label = { Text(tr("Снаряжение", "Inventory")) })
                NavigationBarItem(selected = activeTab == 2, onClick = { activeTab = 2 }, icon = { Icon(Icons.Default.AutoFixHigh, null) }, label = { Text(tr("Заклинания", "Spells")) })
                NavigationBarItem(selected = activeTab == 3, onClick = { activeTab = 3 }, icon = { Icon(Icons.Default.MenuBook, null) }, label = { Text(tr("Справочник", "Reference")) })
                NavigationBarItem(selected = activeTab == 4, onClick = { activeTab = 4 }, icon = { Icon(Icons.Default.EditNote, null) }, label = { Text(tr("Журнал", "Log")) })
            }
        }
    ) { padding ->
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(tr("Настройки", "Settings"), fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(tr("Язык базы данных и заклинаний:", "Database and spell language:"), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = { onLanguageChange("ru") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (language == "ru") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text(tr("Русский", "Russian")) }

                            Button(
                                onClick = { onLanguageChange("en") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (language == "en") MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text(tr("English", "English")) }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(tr("Тема оформления:", "App Theme:"), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onThemeChange(AppTheme.DEFAULT) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTheme == AppTheme.DEFAULT) MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text(tr("Стандартная", "Default")) }

                            Button(
                                onClick = { onThemeChange(AppTheme.PARCHMENT) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTheme == AppTheme.PARCHMENT) MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text(tr("Пергамент", "Parchment")) }

                            Button(
                                onClick = { onThemeChange(AppTheme.INFERNAL) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTheme == AppTheme.INFERNAL) MaterialTheme.colorScheme.primary else Color.Gray)
                            ) { Text(tr("Адская", "Infernal")) }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(tr("Дополнительные функции:", "Additional features:"), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoFixHigh, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tr("Боевой Оракул", "Combat Oracle"), fontWeight = FontWeight.Bold)
                                    Text(tr("Интеллектуальные подсказки в бою", "Smart combat suggestions"), fontSize = 11.sp, color = Color.Gray)
                                }
                                Switch(
                                    checked = character.isOracleEnabled,
                                    onCheckedChange = { onCharacterChange(character.copy(isOracleEnabled = it)) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // --- О ПРИЛОЖЕНИИ ---
                        Text(tr("О приложении", "About"), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("D&D Helper", fontWeight = FontWeight.Bold)
                                        Text(tr("Версия 1.0", "Version 1.0"), fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    tr(
                                        "Помощник по настольной ролевой игре Dungeons & Dragons. Управление листом персонажа, заклинаниями, инвентарём и справочником.",
                                        "A tabletop RPG companion for Dungeons & Dragons. Manage character sheets, spells, inventory and reference materials."
                                    ),
                                    fontSize = 12.sp, color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- ПОЛИТИКА КОНФИДЕНЦИАЛЬНОСТИ ---
                        var showPrivacy by remember { mutableStateOf(false) }
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().clickable { showPrivacy = !showPrivacy }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tr("Политика конфиденциальности", "Privacy Policy"), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Icon(
                                        if (showPrivacy) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        null, tint = Color.Gray
                                    )
                                }
                                if (showPrivacy) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        tr(
                                            "1. Сбор данных\nПриложение НЕ собирает, НЕ передаёт и НЕ хранит на серверах никаких персональных данных пользователя. Все данные (персонажи, заклинания, инвентарь) хранятся исключительно на вашем устройстве.\n\n2. Камера\nДоступ к камере используется только для сканирования QR-кодов при импорте персонажей. Изображения с камеры не сохраняются и не передаются.\n\n3. Интернет\nИнтернет-соединение используется исключительно для загрузки справочных данных (бестиарий, заклинания) из открытых источников SRD 5.1 и SRD 5.2.\n\n4. Реклама\nПриложение может содержать рекламу, предоставляемую сервисом Яндекс Реклама (Yandex Ads). Рекламный SDK может собирать обезличенные технические данные (идентификатор устройства, версия ОС) для показа релевантных объявлений. Подробнее — в политике конфиденциальности Яндекса.\n\n5. Данные персонажей\nОбмен данными персонажей (экспорт/импорт) происходит только по инициативе пользователя и только между устройствами, которые пользователь выбирает самостоятельно.",
                                            "1. Data Collection\nThis app does NOT collect, transmit, or store any personal user data on servers. All data (characters, spells, inventory) is stored exclusively on your device.\n\n2. Camera\nCamera access is used solely for scanning QR codes when importing characters. Camera images are not saved or transmitted.\n\n3. Internet\nInternet connection is used exclusively for downloading reference data (bestiary, spells) from open SRD 5.1 and SRD 5.2 sources.\n\n4. Advertising\nThe app may contain ads provided by Yandex Ads. The advertising SDK may collect anonymized technical data (device ID, OS version) to display relevant ads. See Yandex privacy policy for details.\n\n5. Character Data\nCharacter data exchange (export/import) occurs only at the user's initiative and only between devices chosen by the user."
                                        ),
                                        fontSize = 11.sp, color = Color.Gray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            tr(
                                "Контент основан на материалах SRD 5.1 и SRD 5.2, опубликованных на условиях Creative Commons Attribution 4.0 International License.",
                                "Content is based on SRD 5.1 and SRD 5.2 materials, published under the Creative Commons Attribution 4.0 International License."
                            ),
                            fontSize = 10.sp, color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) { Text(tr("Закрыть", "Close")) }
                }
            )
        }
        val isEn = language == "en" || language == "english"
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (activeTab) {
                0 -> CharacterSheetTab(
                    isEn = isEn,
                    character = character,
                    onCharacterChange = onCharacterChange,
                    magicItems = magicItems,
                    onAddLog = addLogEntry
                )
                1 -> EquipmentTab(
                    isEn = isEn,
                    character = character,
                    onCharacterChange = onCharacterChange
                )
                2 -> SpellsDirectoryTab(
                    isEn = isEn,
                    spells = spells,
                    classSpells = classSpells,
                    language = language,
                    character = character,
                    onCharacterChange = onCharacterChange
                )
                3 -> ReferenceTab(
                    monsters = bestiaryList,
                    races = races,
                    classes = classes,
                    backgrounds = backgrounds,
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange,
                    language = language,
                    magicItems = magicItems,
                    character = character,
                    onCharacterChange = onCharacterChange
                )
                4 -> GameLogTab(
                    isEn = isEn,
                    logEntries = gameLogEntries,
                    onAddLog = addLogEntry,
                    onClearLog = {
                        gameLogEntries = emptyList()
                        gameLogStorage.clearLog(character.id)
                    }
                )
            }
        }
    }
}
