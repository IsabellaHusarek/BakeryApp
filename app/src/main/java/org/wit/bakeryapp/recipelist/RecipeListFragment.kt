package org.wit.bakeryapp.recipeList

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import org.wit.bakeryapp.*
import org.wit.bakeryapp.data.Recipe
import org.wit.bakeryapp.data.Season
import org.wit.bakeryapp.databinding.FragmentRecipeListBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class RecipeListFragment : Fragment() {

    private lateinit var _binding: FragmentRecipeListBinding
    private val binding get() = _binding




    // private Variables
    private var displayList: MutableList<Recipe> = ArrayList() // for displaying SearchResult or filtered Recipes
    private var randomRecipe = 0
    private var recipeListEmptyTextViewText: String = ""

    private val filterCategories = arrayOf(
        "Names A-Z",
        "Rating descending",
        "duration ascending",
        "Filter season"
    )

    private val seasons = arrayOf(
        Season.ALL.name,
        Season.SPRING.name,
        Season.SUMMER.name,
        Season.AUTUM.name,
        Season.WINTER.name
    )

    private var currentSeason = determineSeason() // Indicator for current Season

    companion object {
        // hideKeyboard Function for every Fragment
        fun Fragment.hideKeyboard() {
            view?.let { activity?.hideKeyboard(it) }
        }

        private fun Context.hideKeyboard(view: View) {
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        val recipeList: MutableList<Recipe> = ArrayList() // Filled with Database Elements

        const val TAG = "RecipeListFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment-XML
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        val view= binding.root

        binding.recipeListRecyclerview.layoutManager = LinearLayoutManager(context) // set LayoutManager

        recipeListEmptyTextViewText = "\n" +
                "No recipes found!"

        loadData()

        // adapterDefinition with onClickListener
        binding.recipeListRecyclerview.adapter = RecipeListListAdapter(displayList) { position ->
            val recipeId = displayList[position].id

            if (recipeId.isEmpty()) {
                Snackbar
                    .make(binding.recipeListRecyclerview, "Database error, recipe invalid!", Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.bottom_navigation_bar)
                    .show()
            } else {
                val intent = Intent(activity, SingleRecipeActivity::class.java)

                intent.putExtra(EXTRA_RECIPE_ID, recipeId)

                startActivity(intent)
            }
        }

        // Define FloatingActionButton OnClickFunction: open Dialog for Choice between Import and add manually
        binding.actionButtonNewRecipe.setOnClickListener {
            onClickActionButtonNewRecipe(it)
        }

        //  Define FloatingActionButton OnClickFunction: remove Filter that's currently displayed
        binding.actionButtonRemoveFilter.setOnClickListener {
            onClickActionButtonRemoveFilter()
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
        menu.setGroupVisible(R.id.recipe_list_action_bar_menu, true)
        menu.setGroupVisible(R.id.planer_action_bar_menu, false)
        menu.setGroupVisible(R.id.shopping_cart_action_bar_menu, false)

        val recipeOfTheDay = menu.findItem(R.id.show_recipe_of_the_day)

        // change Icon dependant on current season
        when (currentSeason) {
            0 -> recipeOfTheDay.setIcon(R.drawable.ic_floating_action_button_clear) // if determineSeason failed
            1 -> recipeOfTheDay.setIcon(R.drawable.ab_recipe_of_the_day_spring)
            2 -> recipeOfTheDay.setIcon(R.drawable.ab_recipe_of_the_day_summer)
            3 -> recipeOfTheDay.setIcon(R.drawable.ab_recipe_of_the_day_fall)
            4 -> recipeOfTheDay.setIcon(R.drawable.ab_recipe_of_the_day_winter)
        }

        // setup SearchBar
        val searchItem = menu.findItem(R.id.search_for_recipe)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.queryHint = "\n" +
                    "Looking for recipes..."
            searchView.isIconified = false

            recipeListEmptyTextViewText = "\n" +
                    "No recipe found for current search!"

            // SearchText
            val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                searchText.setTextColor(resources.getColor(R.color.white, null))
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean { // submit button pressed on keyboard
                    if (query!!.isNotEmpty() && displayList.isEmpty()) {
                        emptyTextViewFiller("")
                        showUpdatedDisplayList(recipeList)
                        showSnackBar("Search result was empty!")
                    } else {
                        showActionButtonRemoveFilter()
                    }
                    hideKeyboard()
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean { // text is changed in searchView
                    if (newText!!.isNotEmpty()) {
                        displayList.clear()

                        val search = newText.toLowerCase(Locale.ROOT)
                        recipeList.forEach {
                            if (it.title.toLowerCase(Locale.ROOT).contains(search)) {
                                displayList.add(it)
                            }
                        }

                        // Message if SearchResult is empty
                        if (displayList.isEmpty()) {
                            emptyTextViewFiller(recipeListEmptyTextViewText)
                        } else {
                            emptyTextViewFiller("")
                        }

                        binding.recipeListRecyclerview.adapter?.notifyDataSetChanged()
                    } else {
                        emptyTextViewFiller("")

                        showUpdatedDisplayList(recipeList)
                    }

                    return true
                }
            })
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    // handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == R.id.filter_recipes) {
            onClickFilterRecipeIcon()
        }
        if (id == R.id.show_recipe_of_the_day) {
            showRecipeOfTheDay()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadData() {
        activity?.let {
            BakeryStartpageActivity.recipesViewModel.getRecipes()
                .observe(it, androidx.lifecycle.Observer<List<Recipe>> { recipes ->
                    recipeList.clear()
                    recipeList.addAll(recipes)

                    showUpdatedDisplayList(recipeList)

                    emptyTextViewFiller(recipeListEmptyTextViewText)
                })
        }
    }

    // OnClick Function for FloatingActionButton New Recipe
    private fun onClickActionButtonNewRecipe(view: View) {
        // Dialog for choice between different Activities
        val items = arrayOf("import recipe", "\n" +
                "Add recipe manually")

        // Rotation on Click
        val rotate = RotateAnimation(
            0F, 90F,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        val duration = 300
        rotate.duration = duration.toLong()
        rotate.interpolator = LinearInterpolator()
        view.startAnimation(rotate)

        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.title_dialog_add_recipe))
                .setItems(items) { _, which ->
                    if (which == 0) {
                        val intent = Intent(context, ImportRecipeActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(context, EditRecipeActivity::class.java)
                        intent.putExtra(EXTRA_RECIPE_ID, "")
                        startActivity(intent)
                    }
                }
                .setOnDismissListener {
                    // rotate back on dismissing dialog
                    val rotateBack = RotateAnimation(
                        90F, 0F,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                    )
                    rotateBack.duration = duration.toLong()
                    rotateBack.interpolator = LinearInterpolator()
                    view.startAnimation(rotateBack)
                }
                .show()
        }
    }

    // Filter Function
    private fun onClickFilterRecipeIcon() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.title_dialog_filter_recipes))
                .setItems(filterCategories) { _, which ->
                    if (which == filterCategories.size - 1) {
                        filterRecipesBySeason(filterCategories[which])
                    } else {
                        filterRecipes(which)
                    }
                }
                .show()
        }
    }

    private fun filterRecipes(type: Int) {
        val tempRecipeList = ArrayList<Recipe>()

        when (type) {
            0 -> { // A-Z
                recipeList.forEach {
                    var i = 0
                    while (i < tempRecipeList.size) {
                        if (it.title < tempRecipeList[i].title) {
                            tempRecipeList.add(i, it)
                            break
                        }
                        i++
                    }
                    if (i == tempRecipeList.size) {
                        tempRecipeList.add(it)
                    }
                }
            }
            1 -> { // sort rating
                recipeList.forEach {
                    var i = 0
                    while (i < tempRecipeList.size) {
                        if (it.rating > tempRecipeList[i].rating) {
                            tempRecipeList.add(i, it)
                            break
                        }
                        i++
                    }
                    if (i == tempRecipeList.size) {
                        tempRecipeList.add(it)
                    }
                }
            }
            2 -> { // sort duration
                recipeList.forEach {
                    var i = 0
                    while (i < tempRecipeList.size) {
                        if (it.preparationTime < tempRecipeList[i].preparationTime) {
                            tempRecipeList.add(i, it)
                            break
                        }
                        i++
                    }
                    if (i == tempRecipeList.size) {
                        tempRecipeList.add(it)
                    }
                }
            }
        }
        showActionButtonRemoveFilter()
        showUpdatedDisplayList(tempRecipeList)
    }

    // filter recipeList and show only recipes of current season
    private fun filterRecipesBySeason(title: String) {
        var checkedItem = -1

        val filterSeasons = Array(seasons.size) { "" }
        var counter = 0

        // format seasonality class to be capitalized
        seasons.forEach {
            var letterCounter = 0
            var tempString = ""
            while (letterCounter < it.length) {
                tempString += if (letterCounter == 0) {
                    it[letterCounter].toUpperCase()
                } else {
                    it[letterCounter].toLowerCase()
                }
                letterCounter++
            }

            filterSeasons[counter] = tempString

            counter++
        }

        val tempRecipeList = ArrayList<Recipe>()

        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(title)
                .setNegativeButton(resources.getString(R.string.label_dialog_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("filter") { _, _ ->
                    if (checkedItem == -1) {
                        filterRecipesBySeason(title)
                        showSnackBar("Choose a season")
                    } else {
                        recipeList.forEach {
                            if (it.seasonality == seasons[checkedItem]) {
                                tempRecipeList.add(it)
                            }
                        }
                        showActionButtonRemoveFilter()
                        showUpdatedDisplayList(tempRecipeList)

                        if (tempRecipeList.isEmpty()) {
                            emptyTextViewFiller("No recipes available for current selection")
                        }
                    }
                }
                .setSingleChoiceItems(filterSeasons, checkedItem) { _, which ->
                    checkedItem = which
                }
                .show()
        }
    }

    // OnClick Function for FloatingActionButton Remove Filter
    private fun onClickActionButtonRemoveFilter() {
        showUpdatedDisplayList(recipeList)

        binding.recipeListRecyclerview.setPadding(0, 0, 0, 0)

        binding.actionButtonRemoveFilter.isVisible = false
    }

    private fun showActionButtonRemoveFilter() {
        binding.recipeListRecyclerview.setPadding(0, 154, 0, 0)

        binding.actionButtonRemoveFilter.isVisible = true
    }

    private fun showRecipeOfTheDay() {
        val prevRandomRecipe = randomRecipe
        val tempRecipeList = ArrayList<Recipe>()

        recipeList.forEach {
            if (it.seasonality == seasons[currentSeason]) {
                tempRecipeList.add(it)
            }
        }

        if (tempRecipeList.isEmpty()) {
            recipeList.forEach {
                if (it.seasonality == seasons[0]) {
                    tempRecipeList.add(it)
                }
            }
        }

        if (tempRecipeList.size > 1) {
            while (randomRecipe == prevRandomRecipe) {
                randomRecipe = floor(Math.random() * tempRecipeList.size).toInt()
            }
        } else {
            randomRecipe = 0
        }
        showActionButtonRemoveFilter()
        showUpdatedDisplayList(mutableListOf(tempRecipeList[randomRecipe]))
    }

    private fun showSnackBar(text: String) {
        Snackbar
            .make(binding.recipeListRecyclerview, text, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.bottom_navigation_bar)
            .show()
    }

    private fun showUpdatedDisplayList(list: MutableList<Recipe>) {
        displayList.clear()
        displayList.addAll(list)

        binding.recipeListRecyclerview.adapter?.notifyDataSetChanged()
    }

    private fun emptyTextViewFiller(text: String) {
        if (displayList.isEmpty()) {
            binding.textNoRecipesFound.text = text
        } else {
            binding.textNoRecipesFound.text = ""
        }
    }

    // determine season of current Date
    private fun determineSeason(): Int { // returns Int: position of season in filterSeasons - Array
        val calendar = Calendar.getInstance()
        val month = calendar[Calendar.MONTH]
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]

        val easyComparison = month * 100 + dayOfMonth // value is date in form MMDD

        return if (easyComparison in 220..520) {
            1
        } else if (easyComparison in 521..822) {
            2
        } else if (easyComparison in 823..1120) {
            3
        } else if (easyComparison >= 1121 || easyComparison < 220) {
            4
        } else {
            showSnackBar("Phone date invalid!")
            Log.d(TAG, "determine Season failed!")
            0
        }
    }
}
