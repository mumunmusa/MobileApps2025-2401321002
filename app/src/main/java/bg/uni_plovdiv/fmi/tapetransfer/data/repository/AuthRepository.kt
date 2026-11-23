package bg.uni_plovdiv.fmi.tapetransfer.data.repository

import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.UserDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserRole
import bg.uni_plovdiv.fmi.tapetransfer.data.session.SessionManager
import bg.uni_plovdiv.fmi.tapetransfer.util.PasswordHasher
import bg.uni_plovdiv.fmi.tapetransfer.util.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class LoginResult {
    SUCCESS,
    INVALID_CREDENTIALS,
    NOT_APPROVED,
}

enum class RegisterResult {
    SUCCESS,
    INVALID_USERNAME,
    INVALID_PASSWORD,
    USERNAME_TAKEN,
}

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val hasher: PasswordHasher,
    private val sessionManager: SessionManager,
) {

    val pendingUsers: Flow<List<UserEntity>> = userDao.observePendingUsers()

    val approvedUsers: Flow<List<UserEntity>> = userDao.observeApprovedUsers()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> = sessionManager.currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else userDao.observeById(userId)
    }

    suspend fun hasActiveSession(): Boolean {
        val user = getCurrentUser() ?: return false
        return user.isApproved
    }

    suspend fun getCurrentUser(): UserEntity? {
        val userId = sessionManager.currentUserId.first() ?: return null
        return userDao.observeById(userId).first()
    }

    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.observeById(userId).first()
    }

    suspend fun login(username: String, password: String): LoginResult {
        val user = userDao.getByUsername(username.trim()) ?: return LoginResult.INVALID_CREDENTIALS

        val passwordOk = withContext(Dispatchers.Default) {
            hasher.verify(password, user.salt, user.passwordHash)
        }
        if (!passwordOk) return LoginResult.INVALID_CREDENTIALS
        if (!user.isApproved) return LoginResult.NOT_APPROVED

        sessionManager.saveSession(user.id)
        return LoginResult.SUCCESS
    }

    suspend fun register(username: String, fullName: String, password: String): RegisterResult {
        val cleanUsername = username.trim()

        if (!Validators.isValidUsername(cleanUsername)) return RegisterResult.INVALID_USERNAME
        if (!Validators.isValidPassword(password)) return RegisterResult.INVALID_PASSWORD
        if (userDao.getByUsername(cleanUsername) != null) return RegisterResult.USERNAME_TAKEN

        val newUser = createUser(cleanUsername, fullName.trim(), password, UserRole.OPERATOR, approved = false)
        userDao.insert(newUser)
        return RegisterResult.SUCCESS
    }

    suspend fun approveUser(user: UserEntity) {
        userDao.update(user.copy(isApproved = true))
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.delete(user)
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun seedDefaultUsersIfNeeded() {
        if (userDao.count() > 0) return
        userDao.insert(createUser("admin", "Администратор", "Admin123", UserRole.ADMIN, approved = true))
        userDao.insert(createUser("operator", "Оператор", "Oper123", UserRole.OPERATOR, approved = true))
    }

    private suspend fun createUser(
        username: String,
        fullName: String,
        password: String,
        role: UserRole,
        approved: Boolean,
    ): UserEntity = withContext(Dispatchers.Default) {
        val salt = hasher.generateSalt()
        UserEntity(
            username = username,
            fullName = fullName.ifBlank { username },
            passwordHash = hasher.hash(password, salt),
            salt = salt,
            role = role,
            isApproved = approved,
        )
    }
}
