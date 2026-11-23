package bg.uni_plovdiv.fmi.tapetransfer.util

object Validators {

    const val MIN_USERNAME_LENGTH = 3
    const val MIN_PASSWORD_LENGTH = 6

    private val usernameRegex = Regex("^[a-zA-Z0-9_.]+$")
    private val phoneRegex = Regex("^\\+?[0-9 ]{6,15}$")

    fun isValidUsername(username: String): Boolean {
        return username.length >= MIN_USERNAME_LENGTH && usernameRegex.matches(username)
    }

    fun isValidPassword(password: String): Boolean {
        val hasDigit = password.any { it.isDigit() }
        val hasLetter = password.any { it.isLetter() }
        return password.length >= MIN_PASSWORD_LENGTH && hasDigit && hasLetter
    }

    fun isValidClientName(name: String): Boolean = name.isNotBlank()

    fun isValidPrice(price: Double?): Boolean = price != null && price > 0.0

    fun isValidQuantity(quantity: Int?): Boolean = quantity != null && quantity in 1..999

    fun isValidDiscount(percent: Int?): Boolean = percent != null && percent in 0..90

    fun isValidDuration(minutes: Int?): Boolean = minutes != null && minutes > 0

    fun isValidPhone(phone: String): Boolean {
        return phone.isBlank() || phoneRegex.matches(phone.trim())
    }
}
