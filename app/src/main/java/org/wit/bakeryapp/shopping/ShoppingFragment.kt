package org.wit.bakeryapp.shopping

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.wit.bakeryapp.R
import org.wit.bakeryapp.data.Recipe
import org.wit.bakeryapp.recipeList.RecipeListFragment
import org.wit.bakeryapp.recipeList.RecipeListFragment.Companion.hideKeyboard
import java.util.*
import kotlin.collections.ArrayList
import org.wit.bakeryapp.data.ShoppingItem
import org.wit.bakeryapp.databinding.FragmentRecipeListBinding
import org.wit.bakeryapp.databinding.FragmentShoppingBinding
import org.wit.bakeryapp.sharedPreferences

class ShoppingFragment : Fragment() {

    private lateinit var _binding: FragmentShoppingBinding
    private val binding get() = _binding

    private var tempShoppingCartItemClasses: MutableList<ShoppingItem> = ArrayList()

    private var recipeList: MutableList<Recipe> = ArrayList()

    companion object {
        const val TAG = "ShoppingCartFragment"
        var shoppingCartItemClasses: MutableList<ShoppingItem> = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment-XML
        _binding = FragmentShoppingBinding.inflate(inflater, container, false)
        val view= binding.root

        recipeList = RecipeListFragment.recipeList

        binding.shoppingCartRecyclerview.layoutManager = LinearLayoutManager(context) // set LayoutManager
        binding.shoppingCartRecyclerview.adapter =
            ShoppingCartListAdapter(
                shoppingCartItemClasses
            ) { position, action ->
                if (action == 1) { // checkBox changed
                    onCheckBoxChangeListener(position)
                } else if (action == 2) { // item deleted
                    onDeleteButtonPressedListener(position)
                }
            }

        getDataFromSharedPreferences()

        binding.shoppingCartTextInputLayout.isEndIconVisible = false

        // EndIcon invisible til Text get's changed
        binding.shoppingCartEditText.doOnTextChanged { text, _, _, _ ->
            binding.shoppingCartTextInputLayout.isEndIconVisible = text?.isNotBlank()!!
        }

        // listen to Click on EndIcon
        binding.shoppingCartTextInputLayout.setEndIconOnClickListener {
            onClickAddIngredient()
        }

        // listen to Enter on Keyboard
        binding.shoppingCartEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Action when EndIcon is pressed
                onClickAddIngredient()
                true
            } else {
                false
            }
        }

        return view
    }


    // enable options menu
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    // inflate menu & hide unused menu items
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_actionbar_startup_activity, menu)

        // hide action bar menu groups
        menu.setGroupVisible(R.id.recipe_list_action_bar_menu, false)
        menu.setGroupVisible(R.id.planer_action_bar_menu, false)
        menu.setGroupVisible(R.id.shopping_cart_action_bar_menu, true)

        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == R.id.add_recipe_to_shopping_list) {
            onClickShowRecipeListForAdding()
        }
        if (id == R.id.remove_all_ingredients_from_shopping_list) {
            onClickRemoveIngredientsButton()
        }

        return super.onOptionsItemSelected(item)
    }

    // convert Elements to checkListItem
    fun convertIngredientListToCheckListItems(ingredientList: List<String>): MutableList<ShoppingItem> {
        val tempCheckedListItemClass: MutableList<ShoppingItem> = ArrayList()

        ingredientList.forEach {
            if (it.isNotBlank()) {
                tempCheckedListItemClass.add(ShoppingItem(it, false))
            }
        }

        return tempCheckedListItemClass
    }

    // Data added
    private fun addDataToShoppingCart(ingredientsList: List<ShoppingItem>) {
        if (tempShoppingCartItemClasses.isNotEmpty()) {
            tempShoppingCartItemClasses.clear()
        }
        tempShoppingCartItemClasses.addAll(shoppingCartItemClasses)

        shoppingCartItemClasses.clear()
        shoppingCartItemClasses.addAll(ingredientsList)
        shoppingCartItemClasses.addAll(tempShoppingCartItemClasses)

        setDataInSharedPreferences(shoppingCartItemClasses)

        updateAdapter()
    }

    // Data deleted
    private fun deleteDataFromShoppingCart() {
        if (tempShoppingCartItemClasses.isNotEmpty()) { // Save Data to return it
            tempShoppingCartItemClasses.clear()
        }
        tempShoppingCartItemClasses.addAll(shoppingCartItemClasses)
        shoppingCartItemClasses.clear()

        setDataInSharedPreferences(shoppingCartItemClasses)

        updateAdapter()
    }

    // Return after deleting the Data
    private fun returnDataToShoppingCartAfterDeleting() {
        shoppingCartItemClasses.addAll(tempShoppingCartItemClasses)

        setDataInSharedPreferences(shoppingCartItemClasses)

        updateAdapter()
    }

    // OnClick Function for Delete Button
    private fun onClickRemoveIngredientsButton() {
        // Dialog for Choice: Are you sure to delete everything?
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.title_dialog_delete_ingredients))
                .setNegativeButton(resources.getString(R.string.label_dialog_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.label_dialog_accept)) { _, _ ->
                    deleteDataFromShoppingCart()
                    Snackbar
                        .make(binding.shoppingCartRecyclerview, "Alle Einzelteile gelöscht", Snackbar.LENGTH_LONG)
                        .setAction("Rückgängig") {
                            returnDataToShoppingCartAfterDeleting()
                            showSnackBar("Daten wiederhergestellt")
                        }
                        .setAnchorView(R.id.bottom_navigation_bar)
                        .show()
                }
                .show()
        }
    }

    // OnClick Function to Add Ingredient
    private fun onClickAddIngredient() {
        if (binding.shoppingCartEditText.text?.isNotBlank()!!) {
            addDataToShoppingCart(convertIngredientListToCheckListItems(listOf(binding.shoppingCartEditText.text.toString())))
            binding.shoppingCartEditText.setText("", TextView.BufferType.EDITABLE) // delete current input
        }
        hideKeyboard()
        binding.shoppingCartEditText.clearFocus()
        binding.shoppingCartTextInputLayout.isEndIconVisible = false
    }

    // OnClick Function for Add Recipe Button
    private fun onClickShowRecipeListForAdding() {
        val listItems = Array(recipeList.size) { "" }
        var counter = 0
        var checkedItem = -1 // to determine which item's been chosen

        recipeList.forEach {
            listItems[counter] = it.title
            counter++
        }

        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.title_dialog_add_recipe))
                .setNegativeButton(resources.getString(R.string.label_dialog_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("hinzufügen") { _, _ ->
                    if (checkedItem == -1) {
                        Snackbar
                            .make(binding.shoppingCartRecyclerview, "Kein Rezept ausgewählt.", Snackbar.LENGTH_SHORT)
                            .setAnchorView(R.id.bottom_navigation_bar)
                            .show()
                    } else {
                        addRecipeIngredients(recipeList[checkedItem].id)
                    }
                }
                .setSingleChoiceItems(listItems, checkedItem) { _, which ->
                    checkedItem = which
                }
                .show()
        }
    }

    private fun addRecipeIngredients(recipeId: String) {
        var recipe = Recipe()

        recipeList.forEach {
            if (it.id == recipeId) {
                recipe = it
            }
        }

        val ingredientsListOfRecipe = filterOutUsefulIngredients(recipe)

        addDataToShoppingCart(convertIngredientListToCheckListItems(ingredientsListOfRecipe))

        showSnackBar("Zutaten hinzugefügt: " + recipe.title)
    }

    // checkBox Change Listener
    private fun onCheckBoxChangeListener(position: Int) {
        if (position < shoppingCartItemClasses.size) {
            shoppingCartItemClasses[position].checkedStatus = !shoppingCartItemClasses[position].checkedStatus
            setDataInSharedPreferences(shoppingCartItemClasses)
        } else {
            showSnackBar("Error on checking Item!")
            Log.d(TAG, "CheckBox Error! Out of Bounds!\n" +
                    "Current Position: $position, Size of List: " + shoppingCartItemClasses.size)
        }
    }

    // delete single Item
    private fun onDeleteButtonPressedListener(position: Int) {
        if (position < shoppingCartItemClasses.size) {
            shoppingCartItemClasses.removeAt(position)

            setDataInSharedPreferences(shoppingCartItemClasses)

            updateAdapter()
        } else {
            showSnackBar("Error on Deleting Item!")
            Log.d(TAG, "Delete Error! Out of Bounds!\nCurrent Position: $position," +
                    " Size of List: " + shoppingCartItemClasses.size)
        }
    }

    fun filterOutUsefulIngredients(recipe: Recipe): MutableList<String> {
        val ingredientsListOfRecipe: MutableList<String> = ArrayList()

        recipe.ingredients.forEach {
            if (!(it.toLowerCase(Locale.ROOT).contains("salz") ||
                        it.toLowerCase(Locale.ROOT).contains("pfeffer") ||
                        it.toLowerCase(Locale.ROOT).contains("öl") ||
                        it.toLowerCase(Locale.ROOT).contains("zucker"))
            ) {
                ingredientsListOfRecipe.add(it)
            }
        }

        return ingredientsListOfRecipe
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(binding.shoppingCartRecyclerview, text, Snackbar.LENGTH_SHORT).setAnchorView(R.id.bottom_navigation_bar).show()
    }

    private fun updateAdapter() {
        binding.shoppingCartRecyclerview.adapter?.notifyDataSetChanged()
    }

    // write Data in shared Preferences -> convert them to a String & save in a mutableSet
    fun setDataInSharedPreferences(shoppingCartItemClasses: MutableList<ShoppingItem>) {
        var tempString: String
        var tempCheckedStatus: Int
        val tempSetShoppingCartItems = mutableSetOf<String>()

        shoppingCartItemClasses.forEach {
            tempCheckedStatus = if (it.checkedStatus) {
                1
            } else {
                0
            }
            tempString = it.itemTitle + ";" + tempCheckedStatus.toString()
            tempSetShoppingCartItems.add(tempString)
        }

        sharedPreferences.setShoppingCartStringList(tempSetShoppingCartItems)
    }

    private fun getDataFromSharedPreferences() {
        val tempSetShoppingCartItems = sharedPreferences.getShoppingCartStringList()
        val tempItemsFromSharedPreferences = ArrayList<ShoppingItem>()

        tempSetShoppingCartItems?.forEach {
            val stringSplits = it.split(';')
            val tempCheckedStatus = stringSplits[1].toInt() == 1
            val shoppingCartItem = ShoppingItem(stringSplits[0], tempCheckedStatus)
            tempItemsFromSharedPreferences.add(shoppingCartItem)
        }

        val tempCheckedItems = ArrayList<ShoppingItem>()
        val tempUncheckedItem = ArrayList<ShoppingItem>()

        tempItemsFromSharedPreferences.forEach {
            if (it.checkedStatus) {
                tempCheckedItems.add(it)
            } else {
                tempUncheckedItem.add(it)
            }
        }

        shoppingCartItemClasses.clear()
        shoppingCartItemClasses.addAll(tempUncheckedItem)
        shoppingCartItemClasses.addAll(tempCheckedItems)

        updateAdapter()
    }
}
