package org.wit.bakeryapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import org.wit.bakeryapp.data.Recipe
import org.wit.bakeryapp.data.Season
import org.wit.bakeryapp.databinding.ActivityEditRecipeBinding
import org.wit.bakeryapp.databinding.ActivityLoadScreenBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var inputTitle: TextInputEditText
    private lateinit var inputRating: RatingBar
    private lateinit var inputPreparationTime: TextInputEditText
    private lateinit var dropdownSeasonality: AutoCompleteTextView
    private lateinit var inputPortion: TextInputEditText
    private lateinit var inputIngredients: TextInputEditText
    private lateinit var inputPreparationDirections: TextInputEditText
    private lateinit var inputSource: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var addImageButton: Button
    private lateinit var deleteImageButton: ImageButton
    private lateinit var imageRecipe: ImageView
    private lateinit var recipe: Recipe
    private lateinit var recipeId: String
    private lateinit var recipeViewModel: RecipeViewModel
    private var newRecipeMode: Boolean = false
    private var deleteShrdPrefImg: Boolean = false
    private var recipeImgUrl = ""

    private lateinit var binding: ActivityEditRecipeBinding
    // camera function
    private var currentPath: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditRecipeBinding.inflate(layoutInflater) //view binding
        setContentView(binding.root)

        getComponents()

        val adapter = ArrayAdapter(this, R.layout.item_edit_season, Season.values())
        dropdownSeasonality.setAdapter(adapter)
        dropdownSeasonality.keyListener = null

        // get RecipeViewModel
        this.recipeViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)
        // get id from activity
        recipeId = intent.getStringExtra(EXTRA_RECIPE_ID)!!
        // check if id is empty or not and set mode
        if (recipeId.isEmpty()) {
            newRecipeMode = true
        }


        if (newRecipeMode) {
            setUpToolbar(resources.getString(R.string.title_new_recipe))
            // set standard seasonality
            dropdownSeasonality.setText(Season.ALL.name, false)
            // create recipe id
            recipeId = recipeViewModel.createRecipeId()
        } else {
            setUpToolbar(resources.getString(R.string.title_edit_recipe))
            // get recipe
            this.recipeViewModel.getRecipeById(recipeId).observe(this, Observer<Recipe> { recipe ->
                setAttributes(recipe)
            })
        }

        // set delete image button visibility
        if (sharedPreferences.getCustomImageURL(recipeId).isEmpty()) {
            deleteImageButton.visibility = View.INVISIBLE
        } else {
            deleteImageButton.visibility = View.VISIBLE
        }

        addImageButton.setOnClickListener {
            // dialog for choice between add img from camera or files
            val items = arrayOf("Kamera", "Bild auswählen")

            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.button_add_image))
                .setItems(items) { _, which ->
                    if (which == 0) {
                        dispatchCameraIntent()
                    } else {
                        pickFromGallery()
                    }
                }
                .show()

            deleteShrdPrefImg = false
        }

        deleteImageButton.setOnClickListener {
            if (recipeImgUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(recipeImgUrl)
                    .placeholder(R.drawable.ic_image_loading_placeholder)
                    .error(R.drawable.ic_image_broken_error)
                    .into(imageRecipe)
            } else {
                Glide.with(this)
                    .load(R.drawable.ic_image_loading_placeholder)
                    .into(imageRecipe)
            }
            deleteShrdPrefImg = true
            deleteImageButton.visibility = View.INVISIBLE
        }

        saveButton.setOnClickListener {
            save()
        }
    }

    // get user input, safe recipe to database and start SingleRecipeActivity
    private fun save() {
        getUserInput()

        if (newRecipeMode) {
            if (recipe.title.isNotEmpty()) {
                // add recipe to database
                recipeViewModel.addRecipe(recipeId, recipe)
                if (imageUri != null) {
                    // safe new custom image into shared preferences
                    sharedPreferences.addCustomImagesURL(imageUri.toString(), recipeId)
                }
                // start SingleRecipeActivity and pass the document id
                startSingleRecipeActivity()
            } else {
                Snackbar.make(
                    findViewById(R.id.edit_recipe_scrollview),
                    "Title cannot be empty", Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        } else {
            // delete custom image in shared preferences
            if (deleteShrdPrefImg) {
                sharedPreferences.deleteCustomImagesURL(recipeId)
            }
            // update recipe in DB
            recipeViewModel.updateRecipe(recipeId, recipe)
            if (imageUri != null) {
                // safe new custom image into shared preferences
                sharedPreferences.addCustomImagesURL(imageUri.toString(), recipeId)
            }
            // go back to SingleRecipeActivity
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun getComponents() {
        inputTitle = binding.inputTitle
        inputRating = binding.rating
        inputPreparationTime = binding.inputPreparationTime
        dropdownSeasonality = binding.seasonality
        inputPortion = binding.inputPortion
        inputIngredients = binding.inputIngredients
        inputPreparationDirections = binding.inputPreparationDirections
        inputSource = binding.inputSource
        saveButton = binding.saveButton
        deleteImageButton = binding.ibDeleteImage
        addImageButton = binding.addImageButton
        imageRecipe = binding.imgRecipe
    }

    private fun setAttributes(recipe: Recipe) {
        inputTitle.setText(recipe.title)
        inputRating.rating = recipe.rating
        dropdownSeasonality.setText(recipe.seasonality, false)
        inputPreparationDirections.setText(recipe.preparation)
        inputSource.setText(recipe.source)

        if (recipe.preparationTime == 0) {
            inputPreparationTime.setText("")
        } else {
            inputPreparationTime.setText(recipe.preparationTime.toString())
        }

        if (recipe.portion == 0) {
            inputPortion.setText("")
        } else {
            inputPortion.setText(recipe.portion.toString())
        }

        // list to string
        val ingredientsList = recipe.ingredients
        // create new line for each ingredient
        val ingredientsText = TextUtils.join("\n", ingredientsList)
        inputIngredients.setText(ingredientsText)

        // set imageUrl from database
        recipeImgUrl = recipe.img
        // set image in imageview
        setImageIntoView()
    }

    private fun getUserInput(): Recipe {
        val id = recipeId
        val title = inputTitle.text.toString()
        val img = recipeImgUrl
        val rating = inputRating.rating
        val seasonality = dropdownSeasonality.text.toString()
        val preparation = inputPreparationDirections.text.toString()
        val source = inputSource.text.toString()

        var preparationTime = 0
        val preparationText = inputPreparationTime.text.toString()
        if (preparationText.isNotEmpty()) {
            preparationTime = preparationText.toInt()
        }

        var portion = 0
        val portionText = inputPortion.text.toString()
        if (portionText.isNotEmpty()) {
            portion = portionText.toInt()
        }

        val ingredientsText = inputIngredients.text.toString()
        val ingredientsList: List<String> = ingredientsText.trim().splitToSequence('\n')
            .filter { it.isNotEmpty() }.toList()
        val ingredients = ArrayList(ingredientsList)

        recipe = Recipe(id, title, img, rating, preparationTime, portion, seasonality, ingredients, preparation, source)

        return recipe
    }

    private fun setUpToolbar(title: String) {
        // add toolbar

        val toolbar: Toolbar = binding.tbEditRecipe

        setSupportActionBar(toolbar)
        toolbar.title = title
        // add back button to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    // back button menubar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // inflater for single recipe menu bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_actionbar_edit_recipe_activity, menu)
        return true
    }

    // react to click on menu icons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemView = item.itemId

        if (itemView == R.id.ab_save) {
            save()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setImageIntoView() {
        // set image
        val customImage: String = sharedPreferences.getCustomImageURL(recipeId)
        if (customImage.isEmpty() && recipeImgUrl.isNotEmpty()) {
            // set image from database
            Glide.with(this)
                .load(recipeImgUrl)
                .placeholder(R.drawable.ic_image_loading_placeholder)
                .error(R.drawable.ic_image_broken_error)
                .into(imageRecipe)
        } else if (customImage.isNotEmpty()) {
            // set image from gallery
            Glide.with(this)
                .load(customImage)
                .placeholder(R.drawable.ic_image_loading_placeholder)
                .error(R.drawable.ic_image_broken_error)
                .into(imageRecipe)
        } else {
            Glide.with(this)
                .load(R.drawable.ic_image_loading_placeholder)
                .into(imageRecipe)
        }
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

    // set image with glide from uri
    private fun setImageFromUri(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_image_loading_placeholder)
            .error(R.drawable.ic_image_broken_error)
            .into(imageRecipe)
        imageUri = uri
        deleteImageButton.visibility = View.VISIBLE
    }

    // react to gallery/camera function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // camera event
            ReqCodes.CameraRequestCode.reqCodes -> {
                if (resultCode == Activity.RESULT_OK) {
                    val file = File(currentPath)
                    Log.d(TAG, "$file currentpath for gallery")
                    val uri = Uri.fromFile(file)
                    setImageFromUri(uri)
                } else {
                    Log.e(TAG, "Image from camera error")
                }
            }
            // gallery event
            ReqCodes.GalleryRequestCode.reqCodes -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        //launchImageCrop(uri)
                    }
                } else {
                    Log.e(TAG, "Image selection error: Couldn't select that image from memory.")
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



    // start SingleRecipeActivity and pass the document id
    private fun startSingleRecipeActivity() {
        val intent = Intent(this, SingleRecipeActivity::class.java).apply {
            putExtra(EXTRA_RECIPE_ID, recipeId)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val TAG = "EditRecipeActivity"
    }
}
