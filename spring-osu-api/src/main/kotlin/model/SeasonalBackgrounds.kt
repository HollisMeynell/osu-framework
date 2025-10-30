package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class SeasonalBackgrounds(
    @field:JsonProperty("ends_at")
    var endsAt: OffsetDateTime,

    @field:JsonProperty("backgrounds")
    var backgrounds: List<SeasonalBackgroundsItem>,
){
    data class SeasonalBackgroundsItem(
        @field:JsonProperty("url")
        var url: String,

        @field:JsonProperty("user")
        var user: User,
    )
}
