package org.wit.bakeryapp.data

data class Recipe(
    var id: String = "",
    var title: String = "",
    var img: String = "",
    var rating: Float = 0.0f,
    var preparationTime: Int = 0,
    var portion: Int = 0,
    var seasonality: String = Season.ALL.name,
    var ingredients: List<String> = emptyList(),
    var preparation: String = "",
    var source: String = ""
)
