package com.example.dndhelper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dndhelper.data.Monster

// --- СЛОВАРИ ПЕРЕВОДА ---
fun translateSize(size: String?): String {
    return when (size?.lowercase()) {
        "tiny" -> "Крошечный"
        "small" -> "Маленький"
        "medium" -> "Средний"
        "large" -> "Большой"
        "huge" -> "Огромный"
        "gargantuan" -> "Колоссальный"
        else -> size ?: "?"
    }
}

fun translateTerms(text: String?): String {
    if (text == null) return "?"
    return text.lowercase()
        // Типы
        .replace("humanoid", "гуманоид")
        .replace("beast", "зверь")
        .replace("fiend", "исчадие")
        .replace("undead", "нежить")
        .replace("dragon", "дракон")
        .replace("monstrosity", "чудовище")
        .replace("aberration", "аберрация")
        .replace("construct", "конструкт")
        .replace("elemental", "элементаль")
        .replace("fey", "фея")
        .replace("celestial", "небожитель")
        // Мировоззрение
        .replace("chaotic evil", "хаотично-злой")
        .replace("chaotic good", "хаотично-добрый")
        .replace("chaotic neutral", "хаотично-нейтральный")
        .replace("lawful evil", "законно-злой")
        .replace("lawful good", "законно-добрый")
        .replace("lawful neutral", "законно-нейтральный")
        .replace("neutral evil", "нейтрально-злой")
        .replace("neutral good", "нейтрально-добрый")
        .replace("true neutral", "истинно нейтральный")
        .replace("neutral", "нейтральный")
        .replace("unaligned", "без мировоззрения")
        .replace("any alignment", "любое мировоззрение")
}

@Composable
fun MonsterCard(monster: Monster) {
    // Эта переменная запоминает, раскрыта карточка или нет
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }, // КЛИК: меняем состояние туда-сюда
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = monster.name ?: "Неизвестный",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Применяем наши функции перевода
            val sizeRu = translateSize(monster.size)
            val typeRu = translateTerms(monster.type)
            val alignmentRu = translateTerms(monster.alignment)

            Text(
                text = "$sizeRu $typeRu, $alignmentRu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Анимированное появление статов (скрыто по умолчанию)
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "🛡️ КД: ${monster.armorClass ?: "-"}", fontWeight = FontWeight.Bold)
                        Text(text = "❤️ ОЗ: ${monster.hitPoints ?: "-"}", fontWeight = FontWeight.Bold)
                        Text(text = "💀 Опасность: ${monster.cr ?: "-"}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}