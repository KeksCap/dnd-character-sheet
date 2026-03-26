package com.example.dndhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.data.CharacterStorage
import com.example.dndhelper.data.Spell
import com.example.dndhelper.data.SpellRepository
import com.example.dndhelper.data.AppDatabase
import com.example.dndhelper.data.Monster

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = SpellRepository(this)
        val storage = CharacterStorage(this) // Инициализируем хранилище

        setContent {
            var spellList by remember { mutableStateOf(emptyList<Spell>()) }
            var classSpells by remember { mutableStateOf(emptyMap<String, List<String>>()) }
            var selectedLanguage by remember { mutableStateOf<String?>(null) }

            // НОВОЕ: Стейт для хранения выбранного персонажа
            var selectedCharacter by remember { mutableStateOf<CharacterSaveData?>(null) }

            var bestiaryList by remember { mutableStateOf(emptyList<Monster>()) }
            val db = remember { AppDatabase.getDatabase(this@MainActivity) }

            LaunchedEffect(Unit) {
                // 1. Загружаем заклинания (как и раньше)
                spellList = repository.loadSpellsFromAssets()
                classSpells = repository.loadClassSpells()

                // 2. Слушаем изменения в базе монстров
                db.monsterDao().getAllMonsters().collect { monstersFromDb ->
                    if (monstersFromDb.isEmpty()) {
                        // Теперь нам не нужен Thread {}.start()!
                        // LaunchedEffect — это и так корутина.
                        val testMonsters = listOf(
                            Monster(name = "Гоблин", type = "Гуманоид", size = "Маленький", challengeRating = "1/4", xp = 50, imageUrl = null, detailsJson = "{}"),
                            Monster(name = "Зомби", type = "Нежить", size = "Средний", challengeRating = "1/4", xp = 50, imageUrl = null, detailsJson = "{}"),
                            Monster(name = "Взрослый красный дракон", type = "Дракон", size = "Огромный", challengeRating = "17", xp = 18000, imageUrl = null, detailsJson = "{}")
                        )
                        // Просто вызываем функцию, она сама подождет выполнения (suspend)
                        db.monsterDao().insertAll(testMonsters)
                    } else {
                        bestiaryList = monstersFromDb
                    }
                }
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ЛОГИКА ПЕРЕХОДОВ МЕЖДУ ЭКРАНАМИ:
                    if (selectedLanguage == null) {
                        LanguageSelectionScreen { lang -> selectedLanguage = lang }
                    } else if (selectedCharacter == null) {
                        // ПОКАЗЫВАЕМ ТАВЕРНУ
                        TavernScreen(
                            storage = storage,
                            onCharacterSelected = { char -> selectedCharacter = char }
                        )
                    } else {
                        // Вызываем наш главный экран игры
                        MainGameContent(
                            spells = spellList,
                            classSpells = classSpells,
                            language = selectedLanguage!!,
                            character = selectedCharacter!!, // Передаем данные персонажа
                            storage = storage,
                            bestiaryList = bestiaryList,     // <--- ДОБАВИЛИ НАШУ БАЗУ МОНСТРОВ СЮДА!
                            onCharacterChange = { updatedChar ->
                                storage.saveCharacter(updatedChar) // Сохраняем в память
                                selectedCharacter = updatedChar    // Обновляем на экране
                            },
                            onBackToTavern = { selectedCharacter = null } // Кнопка "Назад"
                        )
                    } // Это закрывает блок else (когда персонаж выбран)
                } // Это закрывает блок if/else (выбор языка) или Surface
            } // Это закрывает твою тему (MaterialTheme / DnDTheme)
        } // Это закрывает блок setContent { ... }
    } // Это закрывает функцию override fun onCreate(...)
} // Это закрывает весь класс MainActivity