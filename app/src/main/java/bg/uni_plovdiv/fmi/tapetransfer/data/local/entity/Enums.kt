package bg.uni_plovdiv.fmi.tapetransfer.data.local.entity

enum class UserRole(val displayName: String) {
    ADMIN("Администратор"),
    OPERATOR("Оператор"),
}

enum class MediaType(val displayName: String) {
    VHS("VHS"),
    S_VHS("S-VHS"),
    VHS_C("VHS-C"),
    VIDEO8("Video8"),
    HI8("Hi8"),
    DIGITAL8("Digital8"),
    MINI_DV("MiniDV"),
    AUDIO("Аудио касета"),
}

enum class OutputFormat(val displayName: String) {
    MP4("MP4"),
    MKV("MKV"),
    MP3("MP3"),
}

enum class OrderStatus(val displayName: String) {
    PENDING("Чакаща"),
    IN_PROGRESS("В процес"),
    DONE("Готова"),
}
