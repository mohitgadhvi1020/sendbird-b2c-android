package `in`.sendbirdpoc.model

data class PropertyListResponse(
    val pagination: Pagination,
    val properties: List<Property>
) {
    data class Pagination(
        val current_page: Int,
        val per_page: Int,
        val total_count: Int,
        val total_pages: Int
    )

    data class Property(
        val agent_email: String,
        val agent_first_name: String,
        val agent_last_name: String,
        val agent_phone: String,
        val agent_send_bird_id: String,
        val agent_user_id: String,
        val created_at: CreatedAt,
        val description: String,
        val features: Features?,
        val id: String,
        val images: List<String>,
        val location: Location,
        val price: Int,
        val property_type: String,
        val send_bird_accessId_agent: String,
        val status: String,
        val title: String,
        var archived: Boolean = false,
        var starred: Boolean = false,
        val updated_at: UpdatedAt
    ) {
        data class CreatedAt(
            val _nanoseconds: Int,
            val _seconds: Int
        )

        data class Features(
            val bathrooms: Int,
            val bedrooms: Int? = 0,
            val garden: Boolean,
            val parking: Boolean,
            val square_feet: Int
        )

        data class Location(
            val address: String,
            val city: String,
            val latitude: Double,
            val longitude: Double,
            val state: String,
            val zip_code: String
        )

        data class UpdatedAt(
            val _nanoseconds: Int,
            val _seconds: Int
        )
    }
}