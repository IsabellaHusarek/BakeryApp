package org.wit.bakeryapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import org.wit.bakeryapp.data.Recipe

class RecipeViewModel : ViewModel() {

    private var firestoreRepository = FirestoreRepository()
    private var recipes: MutableLiveData<List<Recipe>> = MutableLiveData()
    var recipe: MutableLiveData<Recipe> = MutableLiveData()

    fun getRecipes(): LiveData<List<Recipe>> {
        firestoreRepository.readRecipes()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, error ->
                if (error != null) {
                    Log.d(TAG, "listen failed.", error)
                    recipes.value = null
                    return@EventListener
                }

                val recipeList: MutableList<Recipe> = mutableListOf()
                for (doc in value!!) {
                    val recipe = doc.toObject(Recipe::class.java)
                    recipeList.add(recipe)
                }
                recipes.value = recipeList
            })
        return recipes
    }

    fun getRecipeById(id: String): LiveData<Recipe> {
        val docRef = firestoreRepository.readRecipes().document(id)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            recipe.value = documentSnapshot.toObject(Recipe::class.java)!!
        }
        return recipe
    }

    fun createRecipeId(): String {
        return firestoreRepository.readRecipes().document().id
    }

    fun addRecipe(id: String, recipe: Recipe) {
        recipe.id = id
        firestoreRepository.readRecipes().document(recipe.id).set(recipe)
    }

    fun addRecipeFromWebsite(id: String, recipe: Recipe) {
        var recipeExists = false
        // check if recipe from specific url already exists
        firestoreRepository.readRecipes()
            .whereEqualTo("source", recipe.source).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.exists()) {
                        recipeExists = true
                    }
                }
                if (!recipeExists) {
                    addRecipe(id, recipe)
                }
            }
    }

    fun updateRecipe(id: String, recipe: Recipe) {
        firestoreRepository.readRecipes().document(id)
            .set(recipe)
            .addOnSuccessListener { Log.d(TAG, "recipe successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating recipe", e) }
    }

    fun deleteRecipe(id: String) {
        firestoreRepository.readRecipes().document(id)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "recipe successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    companion object {
        const val TAG = "RecipeViewModel"
    }
}
