package `in`.sendbirdpoc.model

data class LoginResponse(
    val token: String,
    val user: User
) {
    data class User(
        val created_at: CreatedAt,
        val email: String,
        val first_name: String,
        val id: String,
        val is_active: Boolean,
        val last_login: LastLogin,
        val last_name: String,
        val phone: String,
        val role: String,
        val send_bird_accessId: String,
        val send_bird_id: String,
        val updated_at: UpdatedAt
    ) {
        data class CreatedAt(
            val _nanoseconds: Int,
            val _seconds: Int
        )

        data class LastLogin(
            val _nanoseconds: Int,
            val _seconds: Int
        )

        data class UpdatedAt(
            val _nanoseconds: Int,
            val _seconds: Int
        )
    }
}