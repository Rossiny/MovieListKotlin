package br.com.rossiny.domain

import com.google.gson.annotations.SerializedName
import java.util.*

class Result {

    @SerializedName("page")
    var page: Int? = null

    @SerializedName("results")
    var results: List<Movie> = ArrayList()

    @SerializedName("total_results")
    var totalResults: Int? = null

    @SerializedName("total_pages")
    var totalPages: Int? = null
}