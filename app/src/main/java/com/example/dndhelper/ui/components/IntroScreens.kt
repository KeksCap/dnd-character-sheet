package com.example.dndhelper.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.LocalAppLanguage
import com.example.dndhelper.data.*
import com.example.dndhelper.tr

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
            text = "Выберите язык / Select Language",
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
    var characterList by remember { mutableStateOf(storage.getAllCharacters()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = tr("Таверна", "Tavern"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                val isEn = currentLang.lowercase() == "en" || currentLang.lowercase() == "english"
                fun l(ru: String, en: String) = if (isEn) en else ru
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
                    stats = defaultStats
                )
                storage.saveCharacter(newChar)
                characterList = storage.getAllCharacters()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text(tr("Создать нового персонажа", "Create new character"))
        }

        LazyColumn {
            items(characterList) { char ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { onCharacterSelected(char) }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(char.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${char.race} • ${char.charClass} • ${tr("Ур.", "Lvl.")} ${char.level}", color = Color.Gray)
                        }
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
