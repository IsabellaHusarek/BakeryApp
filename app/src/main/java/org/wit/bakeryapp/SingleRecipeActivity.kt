package org.wit.bakeryapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.wit.bakeryapp.data.IngredientsClass
import org.wit.bakeryapp.data.Recipe
import org.wit.bakeryapp.databinding.ActivitySingleRecipeBinding
import org.wit.bakeryapp.shopping.AddIngredientsToShoppingCart
import java.io.File
import java.io.IOException
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.util.*

class SingleRecipeActivity : AppCompatActivity() {
    companion object {
        // for debugging
        private const val TAG: String = "AppDebug"
        private const val REQ_CODE_EDIT: Int = 234
    }

    // camera function
    private var currentPath: String? = null

    // declare variables from database
    private var ingredientsQuantity: ArrayList<Float> = arrayListOf()
    private var ingredientsQuantityForOne: ArrayList<Float> = arrayListOf()
    private var ingredientsDescription: ArrayList<String> = arrayListOf()
    private lateinit var idDB: String
    private lateinit var imageUrlDB: String
    private var ingredientsListDB: Array<String> = arrayOf()
    private var portionDB: Int = 1
    private lateinit var preparationDB: String
    private var preparationTimeDB: Int = 0
    private var ratingDB: Float = 0F
    private lateinit var sourceDB: String
    private lateinit var titleDB: String
    private lateinit var portionNew: String
    private lateinit var portionOld: String
    private lateinit var recipeViewModel: ViewModel
    private lateinit var idFromMainPage: String
    private lateinit var ingredientsClass: IngredientsClass

    // declare variables from layout
    /*private lateinit var nameDish: TextView
    private lateinit var portionDish: EditText
    private lateinit var preparationTime: TextView
    private lateinit var instructionsDish: TextView
    private lateinit var ratingStars: RatingBar
    private lateinit var sourceRecipe: TextView
    private lateinit var preparationTimeNumber: TextView
    private lateinit var imageDish: ImageView
    private lateinit var singleRecipeScroll: ScrollView
    private lateinit var singleRecipeToolbar: Toolbar*/

    private lateinit var binding: ActivitySingleRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingleRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Id from Startup Activity
        if (intent.getStringExtra(EXTRA_RECIPE_ID)!!.isNotEmpty()) {
            idFromMainPage = intent.getStringExtra(EXTRA_RECIPE_ID)!!
        }


        // Toolbar/Appbar
        setSupportActionBar(binding.singleRecipeToolbar)

        // add back button to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        binding.singleRecipeToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // get database data via ViewModel
        recipeViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)
        loadAllData()

        // Gallery listener
        binding.imageDish.setOnClickListener {
            pickFromGallery()
        }

        // Portion calculator listener
        binding.portionDish.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                portionOld = portionNew
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                portionOld = binding.portionDish.text.toString()
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                portionNew = if (s.toString().isNotBlank()) s.toString() else portionOld
                if (ingredientsQuantityForOne.isNotEmpty() && ingredientsQuantityForOne.size == ingredientsClass.quantity.size) {
                    for (i in ingredientsQuantityForOne.indices) {
                        val newCalculatedQuantity: Float = portionNew.toFloat() * ingredientsQuantityForOne[i]
                        if (!(newCalculatedQuantity.isInfinite()) || !(newCalculatedQuantity.isNaN())) {
                            ingredientsClass.quantity[i] = newCalculatedQuantity
                        }
                    }
                }
                setupIngredients(ingredientsClass)
            }
        })
        ingredientsClass = IngredientsClass(ingredientsQuantity, ingredientsDescription)
    }

    // Load all data from database via ViewModel
    private fun loadAllData() {
        (recipeViewModel as RecipeViewModel).getRecipeById(idFromMainPage).observe(this, Observer<Recipe> { recipe ->
            idDB = recipe.id
            imageUrlDB = recipe.img
            ingredientsListDB = recipe.ingredients.toTypedArray()
            portionDB = recipe.portion
            preparationDB = recipe.preparation
            preparationTimeDB = recipe.preparationTime
            ratingDB = recipe.rating
            sourceDB = recipe.source
            titleDB = recipe.title
            separateQuantity(ingredientsListDB, portionDB)
            setBasicAttributes(portionDB, preparationTimeDB, preparationDB, ratingDB, sourceDB, titleDB, imageUrlDB)
        })
    }


    // Divide time into hours and minutes
    private fun preparationTimeString(time: Int): String {
        var hours = 0
        val minutes: Int
        if (time >= 60) {
            hours = kotlin.math.floor((time.toDouble() / 60)).toInt()
        }
        minutes = time - (hours * 60)

        if (hours > 0) {
            return "$hours h $minutes min"
        }
        return "$minutes min"
    }

    // loads data into views
    private fun setBasicAttributes(
        portionDB: Int,
        preparationTimeDB: Int,
        preparationDB: String,
        ratingDB: Float,
        sourceDB: String,
        titleDB: String,
        imageUrlDB: String
    ) {
        binding.nameDish.text = titleDB
        if (binding.portionDish.text.toString() != portionDB.toString() || binding.portionDish.text.isEmpty()) {
            binding.portionDish.setText(portionDB.toString(), TextView.BufferType.EDITABLE)
        } else {
            Log.d(TAG, "portion has not changed")

        }
        binding.preperationTimeNumber.text = preparationTimeString(preparationTimeDB)
        binding.instructionsDish.text = preparationDB
        binding.ratingStars.rating = ratingDB
        binding.sourceRecipe.text = sourceDB
        val customImage: String = sharedPreferences.getCustomImageURL(idFromMainPage)
        if (customImage == "") {
            Log.d("TAG", "no custom image found")
            // set Image from Database
            Glide.with(this)
                .load(imageUrlDB)
                .placeholder(R.drawable.ic_image_loading_placeholder)
                .error(R.drawable.ic_image_broken_error)
                .into(binding.imageDish)
        } else {
            Log.d("TAG", "custom image found")
            // set custom image from shared preferences
            Glide.with(this)
                .load(customImage)
                .placeholder(R.drawable.ic_image_loading_placeholder)
                .error(R.drawable.ic_image_broken_error)
                .into(binding.imageDish)
        }
    }

    // Portion for 1 person as base for portion calculator
    private fun setPortion(ingredients: IngredientsClass, portionDB: Int): ArrayList<Float> {
        for (i in ingredients.quantity.indices) {
            if (portionDB != 0) {
                ingredientsQuantityForOne.add(i, (ingredients.quantity[i] / portionDB.toFloat()))
            }
        }
        return ingredientsQuantityForOne
    }

    // Separate quantity from description in ingredientsList from Firestore
    private fun separateQuantity(ingredientsListDB: Array<String>, portionDB: Int) {
        var quantity = 0F
        var description = ""

        // clear lists before adding
        ingredientsQuantityForOne.clear()
        ingredientsQuantity.clear()
        ingredientsDescription.clear()

        for (i in ingredientsListDB.indices) {
            val str = ingredientsListDB[i]
            val separate2 = str.split(" ", "/").map { it.trim() }
            println(separate2)
            for (i in separate2.indices) {
                var numeric = true
                try {
                    parseDouble(separate2[i])
                } catch (e: NumberFormatException) {
                    numeric = false
                }
                if (numeric) {
                    quantity = separate2[i].toFloat()
                    println("${separate2[i]} is a number")
                } else {
                    // filter out special chars
                    when (separate2[i][0].toInt()) {
                        188 -> quantity = 0.25F
                        189 -> quantity = 0.5F
                        190 -> quantity = 0.75F
                        else -> description += separate2[i] + " "
                    }
                }
            }
            ingredientsDescription.add(i, description)
            description = ""
            ingredientsQuantity.add(i, quantity)
            quantity = 0F
        }
        ingredientsClass = IngredientsClass(ingredientsQuantity, ingredientsDescription)
        setupIngredients(ingredientsClass)
        setPortion(ingredientsClass, portionDB)
    }

    // set up the ingredients in the recyclerview
    private fun setupIngredients(ingredients: IngredientsClass) {
        // Recycler View
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = IngredientsListAdapter(ingredients)
    }

    // inflater for single recipe menu bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_actionbar_single_recipe, menu)
        return true
    }


    // Add Recipe to Shopping List
    private fun sendToShoppingList(recipeID: String) {
        AddIngredientsToShoppingCart(recipeID, binding.singleRecipeScroll).addIngredientsFromRecipe()
    }

    // give ID to Edit Recipe Activity
    private fun sendToEdit(recipeID: String) {
        val intent = Intent(this, EditRecipeActivity::class.java)
        intent.putExtra(EXTRA_RECIPE_ID, recipeID)
        startActivityForResult(intent, REQ_CODE_EDIT)
    }

    private fun deleteRecipe(recipeID: String) {
        (recipeViewModel as RecipeViewModel).deleteRecipe(recipeID)
        sharedPreferences.deleteCustomImagesURL(recipeID)
        finish()
    }

    // react to click on menu icons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_photo -> dispatchCameraIntent()
            R.id.add_to_list -> sendToShoppingList(idFromMainPage)
            R.id.edit -> sendToEdit(idFromMainPage)
            R.id.delete_recipe -> deleteRecipe(idFromMainPage)

        }
        return false
    }

    // react to gallery/camera function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ReqCodes.CameraRequestCode.reqCodes -> {
                if (resultCode == Activity.RESULT_OK) {
                    val file = File(currentPath)
                    val uri = Uri.fromFile(file)
                    setImage(uri)
                } else {
                    Log.e(TAG, "Image from camera error")
                }
            }

            ReqCodes.GalleryRequestCode.reqCodes -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        //launchImageCrop(uri)
                    }
                } else {
                    Log.e(TAG, "Image selection error: Couldn't select that image from memory.")
                }
            }


            REQ_CODE_EDIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    loadAllData()
                } else {
                    Log.e(TAG, "Error while editing")
                }
            }
        }
    }

    // camera intent
    private fun dispatchCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (photoFile != null) {
                // create content provider matching the authority
                val photoUri = FileProvider.getUriForFile(this, "com.example.coogit.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, ReqCodes.CameraRequestCode.reqCodes)
            }
        }
    }

    // create image for camera function
    private fun createImage(): File {
        val timeStamp = SimpleDateFormat("yyyMMdd_HHmmss", Locale.GERMAN).format(Date())
        val imageName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }

    // set image with glide from uri
    private fun setImage(uri: Uri) {
    // safe new custom image into shared preferences
        sharedPreferences.addCustomImagesURL(uri.toString(), idFromMainPage)
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_image_loading_placeholder)
            .error(R.drawable.ic_image_broken_error)
            .into(binding.imageDish)
}


    // pick a picture from gallery intent
    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, ReqCodes.GalleryRequestCode.reqCodes)
    }
}
