package com.example.dndhelper.ui.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.platform.LocalContext
import com.example.dndhelper.data.BackgroundRepository
import com.example.dndhelper.data.CharacterSaveData
import com.example.dndhelper.tr

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BiographyBlock(
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    isEn: Boolean
) {
    val context = LocalContext.current
    val backgroundRepo = remember { BackgroundRepository(context) }
    val backgroundList = remember { backgroundRepo.loadBackgrounds() }

    var isEditing by remember { mutableStateOf(false) }
    
    // Временные состояния для редактирования всех полей
    var tempAge by remember(character) { mutableStateOf(character.age) }
    var tempHeight by remember(character) { mutableStateOf(character.height) }
    var tempWeight by remember(character) { mutableStateOf(character.weight) }
    var tempEyes by remember(character) { mutableStateOf(character.eyes) }
    var tempSkin by remember(character) { mutableStateOf(character.skin) }
    var tempHair by remember(character) { mutableStateOf(character.hair) }
    
    var tempBackground by remember(character) { mutableStateOf(character.background) }
    var tempAllies by remember(character) { mutableStateOf(character.allies) }
    
    var tempTraits by remember(character) { mutableStateOf(character.personalityTraits) }
    var tempIdeals by remember(character) { mutableStateOf(character.ideals) }
    var tempBonds by remember(character) { mutableStateOf(character.bonds) }
    var tempFlaws by remember(character) { mutableStateOf(character.flaws) }
    
    var tempAlignment by remember(character) { mutableStateOf(character.alignment) }
    var tempLanguages by remember(character) { mutableStateOf(character.languages) }

    val matchedBackground = remember(tempBackground, isEn) {
        backgroundList.find {
            if (isEn) it.nameEn.equals(tempBackground, ignoreCase = true)
            else it.nameRu.equals(tempBackground, ignoreCase = true)
        }
    }

    val alignmentListData = listOf(
        tr("Законное Доброе (ЗД)", "Lawful Good (LG)") to tr(
            "Стремятся поступать правильно, как того ожидает общество. Борются с несправедливостью и защищают невинных.",
            "Lawful Good creatures endeavor to do the right thing as expected by society. Someone who fights injustice and protects the innocent without hesitation is probably Lawful Good."
        ),
        tr("Нейтральное Доброе (НД)", "Neutral Good (NG)") to tr(
            "Делают всё возможное, работая в рамках правил, но не чувствуя себя связанными ими. Помогают другим по нужде.",
            "Neutral Good creatures do the best they can, working within rules but not feeling bound by them. A kindly person who helps others according to their needs is probably Neutral Good."
        ),
        tr("Хаотичное Доброе (ХД)", "Chaotic Good (CG)") to tr(
            "Действуют по велению совести, мало заботясь об ожиданиях окружающих. Помогают бедным вопреки законам.",
            "Chaotic Good creatures act as their conscience directs with little regard for what others expect. A rebel who waylays a cruel baron's tax collectors and uses the stolen money to help the poor is probably Chaotic Good."
        ),
        tr("Законное Нейтральное (ЗН)", "Lawful Neutral (LN)") to tr(
            "Действуют в соответствии с законом, традицией или личными кодексами. Ценят дисциплину и порядок.",
            "Lawful Neutral individuals act in accordance with law, tradition, or personal codes. Someone who follows a disciplined rule of life—and isn't swayed either by the demands of those in need or by the temptations of evil—is probably Lawful Neutral."
        ),
        tr("Нейтральное (Н)", "Neutral (N)") to tr(
            "Предпочитают избегать моральных вопросов и не принимать чью-либо сторону, поступая по ситуации.",
            "Neutral is the alignment of those who prefer to avoid moral questions and don't take sides, doing what seems best at the time. Someone who's bored by moral debate is probably Neutral."
        ),
        tr("Хаотичное Нейтральное (ХН)", "Chaotic Neutral (CN)") to tr(
            "Следуют своим прихотям, ценят личную свободу превыше всего. Живут своим умом.",
            "Chaotic Neutral creatures follow their whims, valuing their personal freedom above all else. A scoundrel who wanders the land living by their wits is probably Chaotic Neutral."
        ),
        tr("Законное Злое (ЗЗ)", "Lawful Evil (LE)") to tr(
            "Методично берут то, что хотят, в рамках кодекса традиций, верности или порядка.",
            "Lawful Evil creatures methodically take what they want within the limits of a code of tradition, loyalty, or order. An aristocrat exploiting citizens while scheming for power is probably Lawful Evil."
        ),
        tr("Нейтральное Злое (НЗ)", "Neutral Evil (NE)") to tr(
            "Их не беспокоит вред, причиняемый другим в погоне за своими желаниями. Преступники и эгоисты.",
            "Neutral Evil is the alignment of those who are untroubled by the harm they cause as they pursue their desires. A criminal who rains and murders as they please is probably Neutral Evil."
        ),
        tr("Хаотичное Злое (ХЗ)", "Chaotic Evil (CE)") to tr(
            "Действуют произвольно и жестоко, движимые ненавистью или кровожадностью. Месть и разрушение.",
            "Chaotic Evil creatures act with arbitrary violence, spurred by their hatred or bloodlust. A villain pursuing schemes of vengeance and havoc is probably Chaotic Evil."
        ),
        tr("Без мировоззрения", "Unaligned") to tr(
            "У существ, не способных к разумному мышлению (например, хищные животные).",
            "Most creatures that lack the capacity for rational thought don't have alignments; they are unaligned. Sharks are savage predators, for example, but they aren't evil; they are unaligned."
        )
    )

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tr("ЛИЧНОСТЬ И БИОГРАФИЯ", "PERSONALITY & BIOGRAPHY"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = {
                if (isEditing) {
                    onCharacterChange(character.copy(
                        age = tempAge,
                        height = tempHeight,
                        weight = tempWeight,
                        eyes = tempEyes,
                        skin = tempSkin,
                        hair = tempHair,
                        background = tempBackground,
                        allies = tempAllies,
                        personalityTraits = tempTraits,
                        ideals = tempIdeals,
                        bonds = tempBonds,
                        flaws = tempFlaws,
                        alignment = tempAlignment,
                        languages = tempLanguages
                    ))
                }
                isEditing = !isEditing
            }) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- СЕКЦИЯ 1: ВНЕШНОСТЬ ---
        BioSectionCard(title = tr("Внешность", "Appearance"), icon = Icons.Default.Face) {
            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BioEditField(tr("Возраст", "Age"), tempAge, { tempAge = it }, Modifier.weight(1f))
                        BioEditField(tr("Рост", "Height"), tempHeight, { tempHeight = it }, Modifier.weight(1f))
                        BioEditField(tr("Вес", "Weight"), tempWeight, { tempWeight = it }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BioEditField(tr("Глаза", "Eyes"), tempEyes, { tempEyes = it }, Modifier.weight(1f))
                        BioEditField(tr("Кожа", "Skin"), tempSkin, { tempSkin = it }, Modifier.weight(1f))
                        BioEditField(tr("Волосы", "Hair"), tempHair, { tempHair = it }, Modifier.weight(1f))
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BioDisplayField(tr("Возраст", "Age"), character.age, Modifier.weight(1f))
                        BioDisplayField(tr("Рост", "Height"), character.height, Modifier.weight(1f))
                        BioDisplayField(tr("Вес", "Weight"), character.weight, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BioDisplayField(tr("Глаза", "Eyes"), character.eyes, Modifier.weight(1f))
                        BioDisplayField(tr("Кожа", "Skin"), character.skin, Modifier.weight(1f))
                        BioDisplayField(tr("Волосы", "Hair"), character.hair, Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- СЕКЦИЯ 2: ЛИЧНОСТЬ ---
        BioSectionCard(title = tr("Личные качества", "Personality"), icon = Icons.Default.Psychology) {
             if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BioEditField(
                        label = tr("Черты характера", "Personality Traits"),
                        value = tempTraits,
                        onValueChange = { tempTraits = it },
                        modifier = Modifier.fillMaxWidth(),
                        isMultiLine = true,
                        trailingIcon = matchedBackground?.let { bg ->
                            val list = if (isEn) bg.traitsEn else bg.traits
                            if (!list.isNullOrEmpty()) {
                                {
                                    IconButton(onClick = { tempTraits = list.random() }) {
                                        Icon(Icons.Default.Casino, contentDescription = "Randomize", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else null
                        }
                    )
                    BioEditField(
                        label = tr("Идеалы", "Ideals"),
                        value = tempIdeals,
                        onValueChange = { tempIdeals = it },
                        modifier = Modifier.fillMaxWidth(),
                        isMultiLine = true,
                        trailingIcon = matchedBackground?.let { bg ->
                            val list = if (isEn) bg.idealsEn else bg.ideals
                            if (!list.isNullOrEmpty()) {
                                {
                                    IconButton(onClick = { tempIdeals = list.random() }) {
                                        Icon(Icons.Default.Casino, contentDescription = "Randomize", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else null
                        }
                    )
                    BioEditField(
                        label = tr("Привязанности", "Bonds"),
                        value = tempBonds,
                        onValueChange = { tempBonds = it },
                        modifier = Modifier.fillMaxWidth(),
                        isMultiLine = true,
                        trailingIcon = matchedBackground?.let { bg ->
                            val list = if (isEn) bg.bondsEn else bg.bonds
                            if (!list.isNullOrEmpty()) {
                                {
                                    IconButton(onClick = { tempBonds = list.random() }) {
                                        Icon(Icons.Default.Casino, contentDescription = "Randomize", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else null
                        }
                    )
                    BioEditField(
                        label = tr("Слабости", "Flaws"),
                        value = tempFlaws,
                        onValueChange = { tempFlaws = it },
                        modifier = Modifier.fillMaxWidth(),
                        isMultiLine = true,
                        trailingIcon = matchedBackground?.let { bg ->
                            val list = if (isEn) bg.flawsEn else bg.flaws
                            if (!list.isNullOrEmpty()) {
                                {
                                    IconButton(onClick = { tempFlaws = list.random() }) {
                                        Icon(Icons.Default.Casino, contentDescription = "Randomize", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else null
                        }
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BioDisplayField(tr("Черты характера", "Personality Traits"), character.personalityTraits, Modifier.fillMaxWidth())
                    BioDisplayField(tr("Идеалы", "Ideals"), character.ideals, Modifier.fillMaxWidth())
                    BioDisplayField(tr("Привязанности", "Bonds"), character.bonds, Modifier.fillMaxWidth())
                    BioDisplayField(tr("Слабости", "Flaws"), character.flaws, Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- СЕКЦИЯ 3: ИСТОРИЯ ---
        BioSectionCard(title = tr("Предыстория и Союзники", "Background & Allies"), icon = Icons.Default.HistoryEdu) {
            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    var expandedBackground by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedBackground,
                        onExpandedChange = { expandedBackground = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tempBackground,
                            onValueChange = { tempBackground = it; expandedBackground = true },
                            label = { Text(tr("Предыстория", "Background"), fontSize = 12.sp) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBackground) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            )
                        )
                        
                        val filteredList = backgroundList.filter { 
                            val name = if (isEn) it.nameEn else it.nameRu
                            name.contains(tempBackground, ignoreCase = true)
                        }
                        
                        if (filteredList.isNotEmpty() && expandedBackground) {
                            ExposedDropdownMenu(
                                expanded = expandedBackground,
                                onDismissRequest = { expandedBackground = false }
                            ) {
                                filteredList.forEach { bg ->
                                    val bgName = if (isEn) bg.nameEn else bg.nameRu
                                    DropdownMenuItem(
                                        text = { Text(bgName) },
                                        onClick = {
                                            tempBackground = bgName
                                            expandedBackground = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    BioEditField(tr("Союзники и организации", "Allies"), tempAllies, { tempAllies = it }, Modifier.fillMaxWidth(), true)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BioDisplayField(tr("Предыстория", "Background"), character.background, Modifier.fillMaxWidth())
                    BioDisplayField(tr("Союзники и организации", "Allies"), character.allies, Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- СЕКЦИЯ 4: МИРОВОЗЗРЕНИЕ И ЯЗЫКИ ---
        BioSectionCard(title = tr("Мировоззрение и Языки", "Alignment & Languages"), icon = Icons.Default.Public) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Мировоззрение
                Text(tr("Мировоззрение", "Alignment"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (isEditing) {
                    var showAlignmentDialog by remember { mutableStateOf(false) }
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().clickable { showAlignmentDialog = true },
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = if (tempAlignment.isEmpty()) tr("Выбрать мировоззрение...", "Select alignment...") else tempAlignment,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (showAlignmentDialog) {
                        AlertDialog(
                            onDismissRequest = { showAlignmentDialog = false },
                            title = { Text(tr("Мировоззрение", "Alignment")) },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    alignmentListData.forEach { (name, desc) ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    tempAlignment = name
                                                    showAlignmentDialog = false
                                                }
                                                .padding(vertical = 12.dp, horizontal = 4.dp)
                                        ) {
                                            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                            Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    }
                                }
                            },
                            confirmButton = { TextButton(onClick = { showAlignmentDialog = false }) { Text("OK") } }
                        )
                    }
                } else {
                    Text(
                        text = if (character.alignment.isBlank()) "—" else character.alignment,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Языки
                Text(tr("Языки", "Languages"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                
                // Отображение списка языков (чипсы)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentDisplayList = if (isEditing) tempLanguages else character.languages
                    currentDisplayList.forEach { lang ->
                        FilterChip(
                            selected = true,
                            onClick = { if (isEditing) tempLanguages = tempLanguages - lang },
                            label = { Text(lang, fontSize = 12.sp) },
                            trailingIcon = { if (isEditing) Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                        )
                    }
                    if (isEditing) {
                        var showAddLanguageDialog by remember { mutableStateOf(false) }
                        var customLang by remember { mutableStateOf("") }
                        val commonLanguages = listOf(
                            tr("Общий", "Common"),
                            tr("Общий язык жестов", "Common Sign Language"),
                            tr("Драконий", "Draconic"),
                            tr("Дварфский", "Dwarvish"),
                            tr("Эльфийский", "Elvish"),
                            tr("Великаний", "Giant"),
                            tr("Гномий", "Gnomish"),
                            tr("Гоблинский", "Goblin"),
                            tr("Полуросликов", "Halfling"),
                            tr("Орочий", "Orc"),
                            tr("Бездны", "Abyssal"),
                            tr("Небесный", "Celestial"),
                            tr("Инфернальный", "Infernal")
                        )

                        AssistChip(
                            onClick = { showAddLanguageDialog = true },
                            label = { Text(tr("Добавить", "Add")) },
                            leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) }
                        )

                        if (showAddLanguageDialog) {
                            AlertDialog(
                                onDismissRequest = { showAddLanguageDialog = false },
                                title = { Text(tr("Добавить язык", "Add Language")) },
                                text = {
                                    Column(Modifier.verticalScroll(rememberScrollState())) {
                                        OutlinedTextField(
                                            value = customLang,
                                            onValueChange = { customLang = it },
                                            placeholder = { Text(tr("Свой язык...", "Custom language...")) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(tr("Или выберите из списка:", "Or select from list:"), style = MaterialTheme.typography.labelSmall)
                                        Spacer(Modifier.height(8.dp))
                                        
                                        commonLanguages.forEach { l ->
                                            if (!tempLanguages.contains(l)) {
                                                TextButton(
                                                    onClick = {
                                                        tempLanguages = tempLanguages + l
                                                        showAddLanguageDialog = false
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(l, modifier = Modifier.fillMaxWidth())
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        if (customLang.isNotBlank()) tempLanguages = tempLanguages + customLang
                                        customLang = ""
                                        showAddLanguageDialog = false
                                    }) { Text(tr("Добавить", "Add")) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showAddLanguageDialog = false }) { Text(tr("Отмена", "Cancel")) }
                                }
                            )
                        }
                    }
                }
                if (!isEditing && character.languages.isEmpty()) {
                    Text("—", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun BioSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun BioDisplayField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            text = if (value.isBlank()) "—" else value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.isBlank()) Color.Gray else Color.Unspecified
        )
    }
}

@Composable
fun BioEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isMultiLine: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier,
        singleLine = !isMultiLine,
        minLines = if (isMultiLine) 2 else 1,
        maxLines = if (isMultiLine) 5 else 1,
        textStyle = MaterialTheme.typography.bodyMedium,
        trailingIcon = trailingIcon
    )
}
