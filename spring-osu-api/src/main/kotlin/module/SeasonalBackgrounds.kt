package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class SeasonalBackgrounds(
    @JsonProperty("ends_at")
    var endsAt: OffsetDateTime,

    @JsonProperty("backgrounds")
    var backgrounds: List<SeasonalBackgroundsItem>,
){
    data class SeasonalBackgroundsItem(
        @JsonProperty("url")
        var url: String,

        @JsonProperty("user")
        var user: User,
    )
}
