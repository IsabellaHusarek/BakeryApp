package org.wit.bakeryapp

import android.content.Context

class SharedPreferences(context: Context) {
    private val preferenceName = "SharedPreferences"

    private val preferenceStringSetPlaner = "PlanerStringSet"
    private val plannedRecipesSet = mutableSetOf<String>()

    private val preferenceStringSetShoppingCart = "ShoppingCartStringSet"
    private val shoppingCartSet = mutableSetOf<String>()

    private val preferenceStringSetCustomImages = "CustomImagesStringSet"
    private val customImagesSet = mutableSetOf<String>()

    private val preference = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    // for Planer
    fun getPlanerStringList(): MutableSet<String>? {
        return preference.getStringSet(preferenceStringSetPlaner, plannedRecipesSet)
    }

    fun setPlanerStringList(plannedRecipe: MutableSet<String>) {
        val editor = preference.edit()
        plannedRecipesSet.clear()
        plannedRecipesSet.addAll(plannedRecipe)
        editor.putStringSet(preferenceStringSetPlaner, plannedRecipesSet)
        editor.apply()
    }

    // for ShoppingCart
    fun getShoppingCartStringList(): MutableSet<String>? {
        return preference.getStringSet(preferenceStringSetShoppingCart, shoppingCartSet)
    }

    fun setShoppingCartStringList(shoppingCartItems: MutableSet<String>) {
        val editor = preference.edit()
        shoppingCartSet.clear()
        shoppingCartSet.addAll(shoppingCartItems)
        editor.putStringSet(preferenceStringSetShoppingCart, shoppingCartSet)
        editor.apply()
    }

    // for CustomImages
    fun getCustomImageURL(recipeId: String): String {
        val tempRecipeString = searchForRecipeWithId(recipeId)

        return if (tempRecipeString.isNotEmpty()) {
            val splits = tempRecipeString.split(';')
            splits[0]
        } else {
            ""
        }
    }

    fun addCustomImagesURL(customImageURL: String, recipeId: String) {
        val editor = preference.edit()

        // delete previous saved Image for this RecipeId
        val tempElementToDelete = searchForRecipeWithId(recipeId)
        if (tempElementToDelete != "") {
            customImagesSet.remove(tempElementToDelete)
        }

        // create string
        val tempString = "$customImageURL;$recipeId"
        customImagesSet.add(tempString)

        editor.putStringSet(preferenceStringSetCustomImages, customImagesSet)
        editor.apply()
    }

    fun deleteCustomImagesURL(recipeId: String) {
        val editor = preference.edit()
        val tempString = searchForRecipeWithId(recipeId)

        if (tempString.isNotEmpty()) {
            customImagesSet.remove(tempString)
        }

        editor.putStringSet(preferenceStringSetCustomImages, customImagesSet)
        editor.apply()
    }

    private fun searchForRecipeWithId(recipeId: String): String {
        preference.getStringSet(preferenceStringSetCustomImages, customImagesSet)?.forEach {
            val splits = it.split(';')
            if (splits[1] == recipeId) {
                return it
            }
        }
        return ""
    }

    companion object {
        const val TAG = "BakeryAppSharedPreferences"
    }
}
