package app.sanmer.message.proxy.viewmodel

import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sanmer.message.proxy.datastore.model.SmtpConfig
import app.sanmer.message.proxy.repository.LogRepository
import app.sanmer.message.proxy.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preference: PreferenceRepository,
    private val log: LogRepository
) : ViewModel() {
    val permissions = listOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS
    )

    val countFlow get() = log.countFlow

    var smtp by mutableStateOf(SmtpConfig.EMPTY)
        private set
    var email by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            preference.data.first().also {
                smtp = it.smtp
                email = it.email
            }
        }
    }

    fun updateSmtp(block: (SmtpConfig) -> SmtpConfig) {
        smtp = block(smtp)
        viewModelScope.launch {
            preference.setSmtp(smtp)
        }
    }

    fun updateEmail(value: String) {
        email = value
        viewModelScope.launch {
            preference.setEmail(value)
        }
    }
}