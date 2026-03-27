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
import com.example.dndhelper.data.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = SpellRepository(this)
        val storage = CharacterStorage(this)
        val prefs = getSharedPreferences("dnd_settings", MODE_PRIVATE)

        setContent {
            // Состояния для заклинаний
            var spellList by remember { mutableStateOf(emptyList<Spell>()) }
            var classSpells by remember { mutableStateOf(emptyMap<String, List<String>>()) }

            // Состояние персонажа и настроек
            var selectedCharacter by remember { mutableStateOf<CharacterSaveData?>(null) }
            var selectedLanguage by remember { mutableStateOf<String?>(prefs.getString("language", null)) }
            var selectedRuleset by remember { mutableStateOf(prefs.getString("ruleset", "2024") ?: "2024") }

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
                spellList = repository.loadSpellsFromAssets()
                classSpells = repository.loadClassSpells()
            }
            MaterialTheme {
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
                            onBackToTavern = { selectedCharacter = null }
                        )
                    }
                }
            }
        }
    }
}