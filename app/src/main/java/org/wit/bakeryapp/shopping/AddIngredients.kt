package org.wit.bakeryapp.shopping


import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.wit.bakeryapp.data.Recipe
import org.wit.bakeryapp.data.ShoppingItem
import org.wit.bakeryapp.recipeList.RecipeListFragment.Companion.recipeList
import kotlin.collections.ArrayList

class AddIngredientsToShoppingCart(private val recipeId: String, private val snackBarView: View) {
    private val shoppingCartItems = ShoppingFragment.shoppingCartItemClasses

    private val shoppingCartFragment = ShoppingFragment()

    fun addIngredientsFromRecipe() {
        var recipe = Recipe()

        recipeList.forEach {
            if (it.id == recipeId) {
                recipe = it
            }
        }

        val ingredientsListOfRecipe = shoppingCartFragment.filterOutUsefulIngredients(recipe)

        addDataToShoppingCart(shoppingCartFragment.convertIngredientListToCheckListItems(ingredientsListOfRecipe))

        val splitRecipeTitle = recipe.title.split(" ")
        val splitRecipeTitleTemp = splitRecipeTitle[0] + if (splitRecipeTitle.size > 1) {
            "..."
        } else {
            ""
        }

        Snackbar
            .make(snackBarView, "$splitRecipeTitleTemp added to shopping list", Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun addDataToShoppingCart(ingredientsList: List<ShoppingItem>) {
        val tempShoppingCartItems = ArrayList<ShoppingItem>()
        tempShoppingCartItems.addAll(shoppingCartItems)

        shoppingCartItems.clear()
        shoppingCartItems.addAll(ingredientsList)
        shoppingCartItems.addAll(tempShoppingCartItems)

        shoppingCartFragment.setDataInSharedPreferences(shoppingCartItems)
    }
}
