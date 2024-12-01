package velocorner.backend.model

data class Athlete(
    val id: Long,
    val resourceState: Int,
    val firstname: String?,
    val lastname: String?,
    val profileMedium: String?, // URL to a 62x62 pixel profile picture
    val city: String?,
    val country: String?,
    val bikes: List<Gear>?,
    val shoes: List<Gear>?
)
