package com.example.dndhelper.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.tr

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
fun BiographyBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit
) {
    var isEditingBio by remember { mutableStateOf(false) }
    var tempBio by remember(character) { mutableStateOf(character.biography) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(tr("БИОГРАФИЯ", "BIOGRAPHY"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = {
                if (isEditingBio) {
                    onCharacterChange(character.copy(biography = tempBio))
                }
                isEditingBio = !isEditingBio
            }) {
                Icon(if (isEditingBio) Icons.Default.Check else Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
            }
        }
        
        if (isEditingBio) {
            OutlinedTextField(
                value = tempBio,
                onValueChange = { tempBio = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        } else {
            Text(
                text = if (character.biography.isNullOrBlank()) tr("Биография не заполнена...", "No biography yet...") else character.biography,
                style = MaterialTheme.typography.bodyMedium,
                color = if (character.biography.isNullOrBlank()) Color.Gray else Color.Unspecified
            )
        }
    }
}
