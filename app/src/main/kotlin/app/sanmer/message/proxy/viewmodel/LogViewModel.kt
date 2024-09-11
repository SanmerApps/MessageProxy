package app.sanmer.message.proxy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sanmer.message.proxy.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val log: LogRepository
) : ViewModel() {
    val allFlow get() = log.allFlow

    fun deleteAll() {
        viewModelScope.launch {
            log.deleteAll()
        }
    }
}