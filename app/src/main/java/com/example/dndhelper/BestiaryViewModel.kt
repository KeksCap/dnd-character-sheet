package com.example.dndhelper // Убедись, что пакет твой

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dndhelper.data.Monster
import com.example.dndhelper.data.MonsterDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BestiaryViewModel(private val dao: MonsterDao) : ViewModel() {

    // Переменная, которую читает экран (изначально пустой список)
    private val _monsters = MutableStateFlow<List<Monster>>(emptyList())
    val monsters: StateFlow<List<Monster>> = _monsters.asStateFlow()

    init {
        // Как только открыли бестиарий — загружаем монстров из БД
        loadMonsters()
    }

    private fun loadMonsters() {
        // Dispatchers.IO означает, что мы читаем тяжелую базу в фоновом потоке,
        // чтобы интерфейс не завис
        viewModelScope.launch(Dispatchers.IO) {
            val realDataFromDb = dao.getAllMonsters()
            _monsters.value = realDataFromDb
        }
    }
}

// Фабрика (нужна, чтобы передать dao внутрь ViewModel)
class BestiaryViewModelFactory(private val dao: MonsterDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BestiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BestiaryViewModel(dao) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel")
    }
}