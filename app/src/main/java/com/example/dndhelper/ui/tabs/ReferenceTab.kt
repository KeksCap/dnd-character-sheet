package com.example.dndhelper.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndhelper.MonsterDetailDialog
import com.example.dndhelper.data.*
import com.example.dndhelper.tr
import com.example.dndhelper.translateSizeAndType
import com.example.dndhelper.ui.dialogs.AddCustomMagicItemDialog
import com.example.dndhelper.ui.dialogs.MagicItemDetailDialog
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import com.example.dndhelper.R

@Composable
fun ReferenceTab(
    monsters: List<Monster>,
    races: List<Race>,
    classes: List<DndClass>,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit,
    language: String,
    magicItems: List<MagicItem>,
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf(
        tr("Бестиарий", "Bestiary"), 
        tr("Расы", "Races"), 
        tr("Классы", "Classes"),
        tr("Маг. предметы", "Magic Items")
    )

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val filteredMonsters = monsters.filter { monster ->
        val matchesSearch = monster.name?.contains(searchQuery, ignoreCase = true) == true
        val matchesEdition = if (language == "en" || language == "english") {
            if (currentRuleset == "2014") monster.document == "wotc-srd" else monster.document != "wotc-srd"
        } else true
        matchesSearch && matchesEdition
    }
    var monsterForDetail by remember { mutableStateOf<Monster?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedCategory) {
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedCategory == index,
                    onClick = { selectedCategory = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedCategory) {
                0 -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(tr("Поиск...", "Search...")) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Тумблер 2014/2024
                            RulesetToggle(currentRuleset, onRulesetChange)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredMonsters, key = { it.id }) { monster ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { monsterForDetail = monster },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(monster.name ?: (if (language == "en") "Unknown" else "Неизвестный"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = translateSizeAndType(monster.size, monster.type, language),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            DraggableScrollbar(listState, filteredMonsters.size, Modifier.align(Alignment.CenterEnd))
                        }
                    }
                }
                1 -> RacesList(
                    races = races, 
                    language = language, 
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange
                )
                2 -> ClassesList(
                    classes = classes,
                    language = language,
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange
                )
                3 -> MagicItemsDirectoryTab(
                    isEn = (language == "en" || language == "english"),
                    magicItems = magicItems,
                    character = character,
                    onCharacterChange = onCharacterChange,
                    currentRuleset = currentRuleset,
                    onRulesetChange = onRulesetChange
                )
            }
        }
    }

    monsterForDetail?.let { monster ->
        MonsterDetailDialog(monster = monster, language = language, onDismiss = { monsterForDetail = null })
    }
}

@Composable
fun RulesetToggle(current: String, onToggle: (String) -> Unit) {
    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        listOf("2014", "2024").forEach { ver ->
            Text(
                text = ver,
                modifier = Modifier
                    .clickable { onToggle(ver) }
                    .background(if (current == ver) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (current == ver) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold, fontSize = 12.sp
            )
        }
    }
}

@Composable
fun DraggableScrollbar(listState: LazyListState, listSize: Int, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    if (listSize == 0) return
    val offsetProgress = if (listSize > 0) listState.firstVisibleItemIndex.toFloat() / listSize else 0f
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxHeight().width(32.dp)) {
        val trackHeight = constraints.maxHeight.toFloat()
        val handleHeightPx = with(density) {
            (trackHeight / (listSize / 5f).coerceAtLeast(1f)).coerceIn(40.dp.toPx(), trackHeight / 4f)
        }
        val maxOffset = trackHeight - handleHeightPx
        val currentOffset = offsetProgress * maxOffset

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = currentOffset.toInt()) }
                .width(8.dp)
                .height(with(density) { handleHeightPx.toDp() })
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                .pointerInput(listSize) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val newProgress = ((currentOffset + dragAmount) / maxOffset).coerceIn(0f, 1f)
                            listState.scrollToItem((newProgress * listSize).toInt())
                        }
                    }
                }
        )
    }
}

@Composable
fun RacesList(
    races: List<Race>,
    language: String,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit
) {
    var raceForDetail by remember { mutableStateOf<Race?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            RulesetToggle(currentRuleset, onRulesetChange)
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(races, key = { it.nameEn }) { race ->
                val name = if (language == "en" || language == "english") race.nameEn else race.nameRu
                val iconRes = getRaceIcon(race.nameEn)
                
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { raceForDetail = race },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr("Перейти к деталям", "View details"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    raceForDetail?.let { race ->
        RaceDetailDialog(race = race, language = language, onDismiss = { raceForDetail = null })
    }
}

fun getRaceIcon(nameEn: String): Int {
    return when (nameEn) {
        "Aasimar" -> R.drawable.ic_race_aasimar
        "Dragonborn" -> R.drawable.ic_race_dragonborn
        "Dwarf" -> R.drawable.ic_race_dwarf
        "Elf" -> R.drawable.ic_race_elf
        "Gnome" -> R.drawable.ic_race_gnome
        "Goliath" -> R.drawable.ic_race_goliath
        "Half-Elf" -> R.drawable.ic_race_half_elf
        "Half-Orc" -> R.drawable.ic_race_half_orc
        "Halfling" -> R.drawable.ic_race_halfling
        "Human" -> R.drawable.ic_race_human
        "Orc" -> R.drawable.ic_race_orc
        "Tiefling" -> R.drawable.ic_race_tiefling
        else -> R.drawable.ic_launcher_foreground
    }
}

@Composable
fun RaceDetailDialog(race: Race, language: String, onDismiss: () -> Unit) {
    val isEn = language == "en" || language == "english"
    val name = if (isEn) race.nameEn else race.nameRu
    val traits = if (isEn) race.traitsEn else race.traitsRu

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Лор / Описание расы
                val lore = if (isEn) race.loreEn else race.loreRu
                if (!lore.isNullOrEmpty()) {
                    Text(
                        text = lore,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                // Очертания (Traits)
                Text(tr("Особенности расы", "Traits"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                traits.forEach { trait ->
                    if (trait.name.isNotEmpty()) {
                        Text(trait.name + ".", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Text(trait.desc)
                    Spacer(Modifier.height(8.dp))
                }

                // Этнические группы (если есть)
                val ethnicities = if (isEn) race.ethnicitiesEn else race.ethnicitiesRu
                if (!ethnicities.isNullOrEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(tr("Этнические группы", "Ethnic Groups"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    
                    ethnicities.forEach { eth ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(eth.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(eth.description, style = MaterialTheme.typography.bodySmall)
                                if (!eth.names.isNullOrEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(tr("Имена: ", "Names: ") + eth.names, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }

                // Имена (Общие примеры)
                val names = if (isEn) race.namesEn else race.namesRu
                if (!names.isNullOrEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(tr("Имена", "Names"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(names, style = MaterialTheme.typography.bodyMedium)
                }

                // Подрасы
                if (race.subraces.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(tr("Подрасы", "Subraces"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    
                    race.subraces.forEach { subrace ->
                        val subName = if (isEn) subrace.nameEn else subrace.nameRu
                        val subTraits = if (isEn) subrace.traitsEn else subrace.traitsRu
                        val subLore = if (isEn) subrace.loreEn else subrace.loreRu
                        
                        Text(subName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                        if (!subLore.isNullOrEmpty()) {
                            Text(subLore, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        
                        subTraits.forEach { trait ->
                            if (trait.name.isNotEmpty()) {
                                Text(trait.name + ".", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(trait.desc)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(tr("Закрыть", "Close")) }
        }
    )
}

@Composable
fun ClassesList(
    classes: List<DndClass>,
    language: String,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit
) {
    var selectedClass by remember { mutableStateOf<DndClass?>(null) }

    if (selectedClass != null) {
        ClassDetailScreen(
            dndClass = selectedClass!!,
            language = language,
            onBack = { selectedClass = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                RulesetToggle(currentRuleset, onRulesetChange)
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(classes, key = { it.nameEn }) { dndClass ->
                    val name = if (language == "en" || language == "english") dndClass.nameEn else dndClass.nameRu
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedClass = dndClass },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(tr("Кость хитов: ", "Hit Die: ") + dndClass.hitDie, color = Color.Gray, fontSize = 14.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassDetailScreen(
    dndClass: DndClass,
    language: String,
    onBack: () -> Unit
) {
    val isEn = language == "en" || language == "english"
    val name = if (isEn) dndClass.nameEn else dndClass.nameRu
    val traits = if (isEn) dndClass.traitsEn else dndClass.traitsRu
    val features = if (isEn) dndClass.featuresEn else dndClass.featuresRu
    val subclasses = if (isEn) dndClass.subclassesEn else dndClass.subclassesRu

    // Полноэкранный контейнер
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                // Основные черты
                Text(tr("ОСНОВНЫЕ ЧЕРТЫ", "CORE FEATURES"), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        traits.forEach { (key, value) ->
                           if (key != "---") {
                               Row(modifier = Modifier.fillMaxWidth()) {
                                   Text(key + ": ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp), fontSize = 13.sp)
                                   Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                               }
                           }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Способности по уровням
                Text(tr("УМЕНИЯ КЛАССА", "CLASS FEATURES"), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                features.forEach { feature ->
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${feature.level}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(feature.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(feature.desc, modifier = Modifier.padding(top = 4.dp, start = 32.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Подклассы
                if (subclasses.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(tr("ПОДКЛАССЫ", "SUBCLASSES"), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))

                    subclasses.forEach { subclass ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(subclass.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                                if (subclass.desc.isNotEmpty()) {
                                    Text(subclass.desc, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                }
                                
                                subclass.features.forEach { sf ->
                                    Spacer(Modifier.height(8.dp))
                                    Text("${tr("Уровень", "Level")} ${sf.level}: ${sf.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(sf.desc, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MagicItemsDirectoryTab(
    isEn: Boolean,
    magicItems: List<MagicItem>,
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    currentRuleset: String,
    onRulesetChange: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRarity by remember { mutableStateOf("Все") }
    var selectedItem by remember { mutableStateOf<MagicItem?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    val rarities = listOf("Все") + magicItems.mapNotNull { it.rarity }.distinct().sorted()

    val filteredItems = magicItems.filter { item ->
        val name = if (isEn) item.nameEn else item.nameRu
        
        val matchesSearch = name.contains(searchQuery, ignoreCase = true)
        val matchesRarity = selectedRarity == "Все" || item.rarity == selectedRarity
        
        val matchesEdition = if (currentRuleset == "2014") {
            item.document == "2014" || item.document == "wotc-srd"
        } else {
            item.document != "2014" && item.document != "wotc-srd"
        }
        
        matchesSearch && matchesRarity && matchesEdition
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(tr("Поиск...", "Search...")) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            RulesetToggle(currentRuleset, onRulesetChange)
        }

        LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            item {
                IconButton(onClick = { showAddCustomDialog = true }, modifier = Modifier.padding(end = 8.dp).size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить", tint = MaterialTheme.colorScheme.primary)
                }
            }
            items(rarities) { rarity ->
                FilterChip(
                    selected = selectedRarity == rarity,
                    onClick = { selectedRarity = rarity },
                    label = { Text(rarity, fontSize = 12.sp) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredItems) { item ->
                val name = if (isEn) item.nameEn else item.nameRu
                val type = if (isEn) item.typeEn else item.typeRu
                val isFavorite = character.magicItems.any { it.slug == item.slug && it.nameEn == item.nameEn }

                ListItem(
                    headlineContent = { Text(name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { 
                        Text("${item.rarity ?: ""} • $type", fontSize = 12.sp, color = Color.Gray) 
                    },
                    trailingContent = {
                        if (isFavorite) Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.clickable { selectedItem = item }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }

    selectedItem?.let { item ->
        MagicItemDetailDialog(
            item = item,
            character = character,
            onCharacterChange = onCharacterChange,
            onDismiss = { selectedItem = null },
            isEn = isEn
        )
    }

    if (showAddCustomDialog) {
        AddCustomMagicItemDialog(
            isEn = isEn,
            onDismiss = { showAddCustomDialog = false },
            onAdd = { newItem ->
                onCharacterChange(character.copy(magicItems = character.magicItems + newItem))
                showAddCustomDialog = false
            }
        )
    }
}
