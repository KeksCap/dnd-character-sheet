package com.example.dndhelper.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.LocalAppLanguage
import com.example.dndhelper.data.*
import com.example.dndhelper.tr
import com.example.dndhelper.utils.CharacterSerializer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.UUID

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
    val context = LocalContext.current
    
    val msgImportSuccess = tr("Персонаж успешно импортирован!", "Character successfully imported!")
    val msgImportQrInvalid = tr("Неверный QR-код персонажа.", "Invalid character QR code.")
    val copySuffix = tr(" (Копия)", " (Copy)")
    val msgCloned = tr("Персонаж склонирован", "Character cloned")
    val copyPromptStr = tr("Наведите камеру на QR код", "Point camera at QR code")
    val msgSuccess = tr("Успешно!", "Success!")
    val msgInvalidCode = tr("Неверный код.", "Invalid code.")
    val msgCopied = tr("Код скопирован!", "Code copied!")
    
    var characterList by remember { mutableStateOf(storage.getAllCharacters()) }

    var characterToShare by remember { mutableStateOf<CharacterSaveData?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCode by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val char = CharacterSerializer.decodeCharacter(result.contents)
            if (char != null) {
                storage.saveCharacter(char.copy(id = UUID.randomUUID().toString()))
                characterList = storage.getAllCharacters()
                Toast.makeText(context, msgImportSuccess, Toast.LENGTH_SHORT).show()
                showImportDialog = false
                importCode = ""
            } else {
                Toast.makeText(context, msgImportQrInvalid, Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = tr("Таверна", "Tavern"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
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
                }
            ) {
                Text(tr("Создать", "Create"))
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = { showImportDialog = true }
            ) {
                Text(tr("Импорт (Код/QR)", "Import (Code/QR)"))
            }
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
                            val newChar = char.copy(
                                id = UUID.randomUUID().toString(),
                                name = char.name + copySuffix
                            )
                            storage.saveCharacter(newChar)
                            characterList = storage.getAllCharacters()
                            Toast.makeText(context, msgCloned, Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = tr("Клонировать", "Clone"), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { characterToShare = char }) {
                            Icon(Icons.Default.QrCode, contentDescription = tr("Поделиться", "Share"), tint = MaterialTheme.colorScheme.secondary)
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

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(tr("Импорт персонажа", "Import Character")) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        onClick = {
                            scanLauncher.launch(ScanOptions().apply {
                                setPrompt(copyPromptStr)
                                setBeepEnabled(false)
                                setOrientationLocked(false)
                            })
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.QrCodeScanner, null)
                            Text(tr("Сканировать QR", "Scan QR"))
                        }
                    }
                    Text(tr("Или вставьте текстовый код:", "Or paste text code:"), fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = importCode,
                        onValueChange = { importCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(tr("Код персонажа", "Character code")) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (importCode.isNotBlank()) {
                        val char = CharacterSerializer.decodeCharacter(importCode.trim())
                        if (char != null) {
                            storage.saveCharacter(char.copy(id = UUID.randomUUID().toString()))
                            characterList = storage.getAllCharacters()
                            Toast.makeText(context, msgSuccess, Toast.LENGTH_SHORT).show()
                            showImportDialog = false
                            importCode = ""
                        } else {
                            Toast.makeText(context, msgInvalidCode, Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text(tr("Импортировать", "Import")) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text(tr("Отмена", "Cancel")) }
            }
        )
    }

    if (characterToShare != null) {
        val charStr = remember(characterToShare) { CharacterSerializer.encodeCharacter(characterToShare!!) }
        var qrBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
        
        LaunchedEffect(charStr) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val bmp = CharacterSerializer.generateQrBitmap(charStr, 600)
                qrBitmap = bmp
            }
        }

        AlertDialog(
            onDismissRequest = { characterToShare = null },
            title = { Text(tr("Поделиться: ${characterToShare?.name}", "Share: ${characterToShare?.name}")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(250.dp)
                        )
                        Text(tr("Отсканируйте с другого устройства", "Scan from another device"), fontSize = 12.sp, color = Color.Gray)
                    } else {
                        CircularProgressIndicator()
                    }
                    
                    OutlinedTextField(
                        value = charStr,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(tr("Или скопируйте текстовый код", "Or copy text code")) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("D&D Character", charStr))
                    Toast.makeText(context, msgCopied, Toast.LENGTH_SHORT).show()
                }) { Text(tr("Копировать код", "Copy code")) }
            },
            dismissButton = {
                TextButton(onClick = { characterToShare = null }) { Text(tr("Закрыть", "Close")) }
            }
        )
    }
}
