package com.cliffordlab.amoss.models

import com.google.gson.annotations.SerializedName

class RxNormResponse {

    @SerializedName("suggestionGroup")
    var suggestionGroup: SuggestionGroup? = null

    class SuggestionGroup {
        @SerializedName("suggestionList")
        var suggestionList: SuggestionList? = null

        class SuggestionList {
            @SerializedName("suggestion")
            lateinit var suggestion: List<String>

        }

    }
}
