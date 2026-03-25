package com.example.dndhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.dndhelper.data.Spell
import com.example.dndhelper.data.SpellRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = SpellRepository(this)

        setContent {
            var spellList by remember { mutableStateOf(emptyList<Spell>()) }
            var classSpells by remember { mutableStateOf(emptyMap<String, List<String>>()) }
            var selectedLanguage by remember { mutableStateOf<String?>(null) }

            // Загружаем данные один раз при старте
            LaunchedEffect(Unit) {
                spellList = repository.loadSpellsFromAssets()
                classSpells = repository.loadClassSpells()
                android.util.Log.d("DND_LOG", "Загружено: ${spellList.size}, Классов: ${classSpells.size}")
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Используем локальную переменную для безопасности типов (умное приведение типов)
                    val currentLanguage = selectedLanguage

                    if (currentLanguage == null) {
                        LanguageSelectionScreen { lang ->
                            selectedLanguage = lang
                        }
                    } else {
                        MainGameContent(
                            spells = spellList,
                            classSpells = classSpells, // Теперь сюда реально попадут данные из LaunchedEffect
                            language = currentLanguage
                        )
                    }
                }
            }
        }
    }
}