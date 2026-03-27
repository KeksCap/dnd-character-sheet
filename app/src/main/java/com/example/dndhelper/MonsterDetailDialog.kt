package com.example.dndhelper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dndhelper.data.AbilityAction
import com.example.dndhelper.data.Monster

// --- ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ДЛЯ ДИЗАЙНА ---

@Composable
fun StatItem(name: String, value: Int?, save: Int? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, fontWeight = FontWeight.Bold, color = Color(0xFF8B0000))
        val mod = value?.let { (it - 10) / 2 }?.let { if (it >= 0) "+$it" else "$it" } ?: "?"
        Text(text = "$mod ($value)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        save?.let {
            Text(text = "Спас: ${if (it >= 0) "+$it" else "$it"}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ActionItem(action: AbilityAction, isLegendary: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        // Название жирным + курсивом
        Text(text = "${action.name}.", fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
        // Описание атаки/пассивки
        Text(text = action.desc, modifier = Modifier.padding(start = 8.dp))
    }
}

// Заголовки разделов (Абилки, Действия, Описание)
@Composable
fun StatHeader(text: String) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF8B0000))
        HorizontalDivider(color = Color(0xFF8B0000), thickness = 2.dp)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// --- ГЛАВНОЕ ОКНО ДЕТАЛИЗАЦИИ ---

@Composable
fun MonsterDetailDialog(
    monster: Monster,
    onDismiss: () -> Unit
) {
    // Парсим rawData один раз при открытии
    val fullData = remember(monster) { parseMonsterRawData(monster.rawData) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Почти на весь экран
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // СКРОЛЛ
            ) {
                // ИМЯ
                Text(
                    text = monster.name ?: "Неизвестный",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B0000)
                )

                // ТИП, РАЗМЕР, МИРОВОЗЗРЕНИЕ (Уже согласовано грамматически)
                val typeRu = translateSizeAndType(monster.size, monster.type)
                val alignmentRu = translateAlignment(monster.alignment)
                Text(text = "$typeRu, $alignmentRu", fontStyle = FontStyle.Italic, color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF8B0000), thickness = 2.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // КД, ХП, СКОРОСТЬ
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "🛡️ КД: ${monster.armorClass ?: "-"}", fontWeight = FontWeight.Bold)
                    Text(text = "❤️ ОЗ: ${monster.hitPoints ?: "-"}", fontWeight = FontWeight.Bold)

                    val speedRu = fullData?.speed?.toList()?.joinToString(", ") { "${it.first}: ${it.second}" } ?: "?"
                    Text(text = "🏃 $speedRu", color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF8B0000), thickness = 2.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // --- ШЕСТЬ ХАРАКТЕРИСТИК И СПАСБРОСКИ ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem("СИЛ", fullData?.strength, fullData?.strength_save)
                    StatItem("ЛОВ", fullData?.dexterity, fullData?.dexterity_save)
                    StatItem("ТЕЛ", fullData?.constitution, fullData?.constitution_save)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem("ИНТ", fullData?.intelligence, fullData?.intelligence_save)
                    StatItem("МУД", fullData?.wisdom, fullData?.wisdom_save)
                    StatItem("ХАР", fullData?.charisma, fullData?.charisma_save)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF8B0000), thickness = 2.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // --- ЧУВСТВА, ЯЗЫКИ, ОПАСНОСТЬ ---
                fullData?.senses?.let { Text(text = "Чувства: $it") }
                fullData?.languages?.let { Text(text = "Языки: $it") }
                Text(text = "Опасность: ${monster.cr ?: "?"} (${(monster.cr ?: 0.0) * 200} ОП)", fontWeight = FontWeight.Bold)

                // --- СПОСОБНОСТИ (ПАССИВКИ) ---
                fullData?.special_abilities?.ifEmpty { null }?.let { abilities ->
                    StatHeader("Особенности")
                    abilities.forEach { ActionItem(it) }
                }

                // --- ДЕЙСТВИЯ (АТАКИ) ---
                fullData?.actions?.ifEmpty { null }?.let { actions ->
                    StatHeader("Действия")
                    actions.forEach { ActionItem(it) }
                }

                // --- ЛЕГЕНДАРНЫЕ ДЕЙСТВИЯ ---
                fullData?.legendary_actions?.ifEmpty { null }?.let { actions ->
                    StatHeader("Легендарные действия")
                    actions.forEach { ActionItem(it) }
                }

                // --- ЛОР И ОПИСАНИЕ (BOT IT!) ---
                fullData?.desc?.let {
                    StatHeader("Описание")
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // КНОПКА ЗАКРЫТЬ
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Закрыть")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}