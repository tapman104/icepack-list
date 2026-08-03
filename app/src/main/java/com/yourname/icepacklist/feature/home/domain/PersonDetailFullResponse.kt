package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.runtime.Immutable

@Immutable
@JsonClass(generateAdapter = true)
data class PersonDetailFullResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "biography") val biography: String?,
    @Json(name = "profile_path") val profilePath: String?,
    @Json(name = "birthday") val birthday: String?,
    @Json(name = "place_of_birth") val placeOfBirth: String?,
    @Json(name = "known_for_department") val knownForDepartment: String?,
    @Json(name = "deathday") val deathday: String?,
    @Json(name = "gender") val gender: Int?,
    @Json(name = "also_known_as") val alsoKnownAs: List<String> = emptyList(),
    @Json(name = "combined_credits") val combinedCreditsResponse: CombinedCreditsResponse? = null,
    @Json(name = "images") val imagesResponse: PersonImagesResponse? = null
) {
    fun toPersonDetail(): PersonDetail {
        return PersonDetail(
            id = id,
            name = name,
            biography = biography,
            profilePath = profilePath,
            birthday = birthday,
            placeOfBirth = placeOfBirth,
            knownForDepartment = knownForDepartment,
            deathday = deathday,
            gender = gender,
            alsoKnownAs = alsoKnownAs
        )
    }
}
