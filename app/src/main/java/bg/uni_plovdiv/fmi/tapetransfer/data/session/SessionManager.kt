package bg.uni_plovdiv.fmi.tapetransfer.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val currentUserId: Flow<Int?> = dataStore.data.map { prefs -> prefs[KEY_USER_ID] }

    suspend fun saveSession(userId: Int) {
        dataStore.edit { prefs -> prefs[KEY_USER_ID] = userId }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs -> prefs.remove(KEY_USER_ID) }
    }

    companion object {
        private val KEY_USER_ID = intPreferencesKey("current_user_id")
    }
}
