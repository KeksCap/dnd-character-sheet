package com.example.dndhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.example.dndhelper.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- ГЛОБАЛЬНЫЕ ИНСТРУМЕНТЫ ЛОКАЛИЗАЦИИ ---
val LocalAppLanguage = compositionLocalOf { "ru" }

@Composable
fun tr(ru: String, en: String): String {
    val currentLang = LocalAppLanguage.current.lowercase()
    val isEn = currentLang == "en" || currentLang == "english"
    return if (isEn) en else ru
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = SpellRepository(this)
        val raceRepository = RaceRepository(this)
        val classRepository = ClassRepository(this)
        val magicItemRepository = MagicItemRepository(this)
        val storage = CharacterStorage(this)
        val prefs = getSharedPreferences("dnd_settings", MODE_PRIVATE)

        setContent {
            // Состояния для заклинаний и рас
            var spellList by remember { mutableStateOf(emptyList<Spell>()) }
            var classSpells by remember { mutableStateOf(emptyMap<String, List<String>>()) }
            var raceList2014 by remember { mutableStateOf(emptyList<Race>()) }
            var raceList2024 by remember { mutableStateOf(emptyList<Race>()) }
            var classList2014 by remember { mutableStateOf(emptyList<DndClass>()) }
            var classList2024 by remember { mutableStateOf(emptyList<DndClass>()) }
            var magicItemList by remember { mutableStateOf(emptyList<MagicItem>()) }


            // Состояние персонажа и настроек
            var selectedCharacter by remember { mutableStateOf<CharacterSaveData?>(null) }
            var selectedLanguage by remember { mutableStateOf<String?>(prefs.getString("language", null)) }
            var selectedRuleset by remember { mutableStateOf(prefs.getString("ruleset", "2024") ?: "2024") }
            val raceList = if (selectedRuleset == "2024") raceList2024 else raceList2014
            val classList = if (selectedRuleset == "2024") classList2024 else classList2014

            // Определение имени файла БД на основе выбора пользователя
            val dbName = when {
                selectedLanguage == "en" -> "monsters.db"
                selectedRuleset == "2024" -> "monsters_ru_2024.db"
                else -> "monsters_ru_2014.db"
            }

            // Инициализация базы данных и ViewModel.
            // remember(dbName) заставит Compose пересоздать их при смене файла.
            val database = remember(dbName) { AppDatabase.getDatabase(this, dbName) }
            val bestiaryViewModel: BestiaryViewModel = viewModel(
                key = dbName,
                factory = BestiaryViewModelFactory(database.monsterDao())
            )
            val bestiaryList by bestiaryViewModel.monsters.collectAsState()

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val loadedSpells = repository.loadSpellsFromDb()
                    val loadedClassSpells = repository.loadClassSpells()
                    val loadedRaces14 = raceRepository.loadRaces("2014")
                    val loadedRaces24 = raceRepository.loadRaces("2024")
                    val loadedClasses14 = classRepository.loadClasses("2014")
                    val loadedClasses24 = classRepository.loadClasses("2024")
                    val loadedMagicItems = magicItemRepository.loadMagicItems()
                    spellList = loadedSpells
                    classSpells = loadedClassSpells
                    raceList2014 = loadedRaces14
                    raceList2024 = loadedRaces24
                    classList2014 = loadedClasses14
                    classList2024 = loadedClasses24
                    magicItemList = loadedMagicItems
                }
            }
            MaterialTheme {
                CompositionLocalProvider(LocalAppLanguage provides (selectedLanguage ?: "ru")) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (selectedLanguage == null) {
                            LanguageSelectionScreen { lang ->
                            prefs.edit().putString("language", lang).apply()
                            selectedLanguage = lang
                        }
                    } else if (selectedCharacter == null) {
                        TavernScreen(
                            storage = storage,
                            onCharacterSelected = { char -> selectedCharacter = char }
                        )
                    } else {
                        MainGameContent(
                            spells = spellList,
                            classSpells = classSpells,
                            races = raceList,
                            classes = classList,
                            language = selectedLanguage!!,
                            character = selectedCharacter!!,
                            storage = storage,
                            bestiaryList = bestiaryList,
                            bestiaryViewModel = bestiaryViewModel,
                            // Передача параметров редакции правил
                            currentRuleset = selectedRuleset,
                            onRulesetChange = { newRuleset ->
                                prefs.edit().putString("ruleset", newRuleset).apply()
                                selectedRuleset = newRuleset
                            },
                            onCharacterChange = { updatedChar ->
                                storage.saveCharacter(updatedChar)
                                selectedCharacter = updatedChar
                            },
                            onBackToTavern = { selectedCharacter = null },
                                    onLanguageChange = { newLang ->
                                    prefs.edit().putString("language", newLang).apply()
                                    selectedLanguage = newLang
                                },
                                magicItems = magicItemList
                            )
                        }
                    }
                }
            }
        }
    }
}