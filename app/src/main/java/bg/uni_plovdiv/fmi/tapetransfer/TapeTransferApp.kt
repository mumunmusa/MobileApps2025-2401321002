package bg.uni_plovdiv.fmi.tapetransfer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TapeTransferApp : Application() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        appScope.launch { authRepository.seedDefaultUsersIfNeeded() }
    }
}
