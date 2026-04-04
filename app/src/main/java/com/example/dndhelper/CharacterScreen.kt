package com.example.dndhelper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    magicItems: List<MagicItem>
) {
    var activeTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        StandardEquipment.initialize(context)
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
            }
        }
    ) { padding ->
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(tr("Настройки", "Settings"), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
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
                    magicItems = magicItems
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
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange,
                    language = language,
                    magicItems = magicItems,
                    character = character,
                    onCharacterChange = onCharacterChange
                )
            }
        }
    }
}
