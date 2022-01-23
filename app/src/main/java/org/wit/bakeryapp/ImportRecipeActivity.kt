package org.wit.bakeryapp

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.wit.bakeryapp.data.Recipe
import org.wit.bakeryapp.data.Season
import org.wit.bakeryapp.databinding.ActivityImportRecipeBinding
import org.wit.bakeryapp.databinding.ActivityLoadScreenBinding
import java.io.IOException

class ImportRecipeActivity : AppCompatActivity() {

    private lateinit var layout: View
    private lateinit var inputUrl: EditText
    private lateinit var inputButton: Button
    private lateinit var recipe: Recipe

    private lateinit var binding: ActivityImportRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportRecipeBinding.inflate(layoutInflater) //view binding
        setContentView(binding.root)

        // get layout
        layout = findViewById(android.R.id.content)

        // finding components
        inputUrl = binding.inputUrl
        inputButton = binding.btnImport

        setUpToolbar()

        // get RecipeViewModel
        val recipeViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)

        inputButton.setOnClickListener {
            // get the user input
            val url = inputUrl.text.toString()
            // get recipe from Website
            if (URLUtil.isValidUrl(url)) {
                val task = GetWebsiteTask()
                recipe = task.execute(url).get()
                // create recipe id and add recipe to database
                val recipeId = recipeViewModel.createRecipeId()
                recipeViewModel.addRecipeFromWebsite(recipeId, recipe)
                finish()
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Enter a valid URL", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setUpToolbar() {
        // add toolbar

        val toolbar: Toolbar = binding.tbImportRecipe
        setSupportActionBar(toolbar)

        // add back button to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    companion object {
        const val TAG = "ImportRecipeActivity"

        // create AsyncTask to import recipe from website
        class GetWebsiteTask : AsyncTask<String, String, Recipe>() {
            private lateinit var recipe: Recipe

            override fun doInBackground(vararg params: String?): Recipe? {
                try {
                    // get the recipe link and store it in url
                    val url = params[0].toString()
                    // document to get the full HTML of the recipe link
                    val document: Document = Jsoup.connect(url).get()
                    // get recipe title
                    val title: String = document.getElementsByTag("h1").text().toString()
                    // get image src
                    val image = document.select("div.recipe-image-carousel-slide a amp-img")
                        .first().attr("src").toString()
                    // set rating to 0
                    val rating = 0.0F
                    // get portion of recipe
                    val portion = document.select("input[name=portionen]").attr("value").toInt()
                    // set standard saisonality
                    val seasonality = Season.ALL.name
                    // get all ingredients by rows of table
                    val ingredients = document.select(".ingredients tbody tr")
                    // add ingredients to ArrayList
                    val ingredientsList = ArrayList<String>()
                    if (ingredients.isNotEmpty()) {
                        for (i in 0 until ingredients.size) {
                            ingredientsList.add(i, ingredients[i].text())
                        }
                    }
                    // get total preparation time text
                    val prepTime = document.select(".rds-recipe-meta span").last().text()
                    var totalTime = 0
                    // convert hours to minutes
                    totalTime = if (prepTime.contains("Stunde")) {
                        val hours = prepTime.substringBefore("Stunde")
                            .filter { it.isDigit() }.toInt()
                        val minutes = prepTime.substringAfter("Stunde")
                            .filter { it.isDigit() }.toInt()
                        minutes + hours * 60
                    } else {
                        prepTime.filter { it.isDigit() }.toInt()
                    }

                    // get preparation text
                    val preparation = document.select("article:has(h2) div:has(br)").text()
                    // add data to recipe object
                    recipe = Recipe("", title, image, rating, totalTime, portion, seasonality, ingredientsList, preparation, url)

                } catch (e: IOException) {
                    e.printStackTrace()
                }

                return recipe
            }
        }
    }
}
