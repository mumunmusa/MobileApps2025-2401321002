package bg.uni_plovdiv.fmi.tapetransfer

import android.app.Application
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.AuthRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.repository.PriceRepository
import bg.uni_plovdiv.fmi.tapetransfer.data.settings.ThemeManager
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

    @Inject
    lateinit var priceRepository: PriceRepository

    @Inject
    lateinit var themeManager: ThemeManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        themeManager.applySavedTheme()
        appScope.launch {
            authRepository.seedDefaultUsersIfNeeded()
            priceRepository.seedDefaultsIfNeeded()
        }
    }
}
