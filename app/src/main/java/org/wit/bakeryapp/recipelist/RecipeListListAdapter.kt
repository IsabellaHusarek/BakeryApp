package org.wit.bakeryapp.recipeList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.wit.bakeryapp.R
import org.wit.bakeryapp.data.Recipe
import org.wit.bakeryapp.databinding.ItemCardviewBinding
import org.wit.bakeryapp.sharedPreferences
import kotlin.math.floor



class RecipeListListAdapter(private val data: List<Recipe>, val itemClick: (Int) -> Unit) :
    RecyclerView.Adapter<RecipeListListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val view =ItemCardviewBinding.inflate(layoutInflater, parent, false)



        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // read recipeList elements
        val recipeData: Recipe = data[position]
        val recipeTitle: String = recipeData.title
        val recipePortion: Int = recipeData.portion
        val recipeRating: Float = recipeData.rating
        val recipePreparationTime: Int = recipeData.preparationTime

        val customImagePath = sharedPreferences.getCustomImageURL(recipeData.id)

        // if customImagePath is empty -> use path from database
        val recipeImageURL: String = if (customImagePath.isNotEmpty()) {
            customImagePath
        } else {
            recipeData.img
        }
        // Strings for CardView: added description for data
        val recipePortionString = (portionString(recipePortion))
        val recipeDurationString = (preparationTimeString(recipePreparationTime))

        // import recipe in CardView
        holder.recipeCardName.text = recipeTitle
        holder.recipeCardPortion.text = recipePortionString
        holder.recipeDuration.text = recipeDurationString

        holder.recipeCardRatingStars.rating = recipeRating


        // glide for loading external images from source
        if(recipeImageURL.isNotEmpty()){
            Glide.with(holder.itemView)
                .load(recipeImageURL)
                .centerCrop()
                .placeholder(R.drawable.ic_image_loading_placeholder)
                .error(R.drawable.ic_image_broken_error)
                .into(holder.recipeCardImage)
        } else {
            Glide.with(holder.itemView)
                .load(R.drawable.ic_image_loading_placeholder)
                .into(holder.recipeCardImage)
        }


        // OnClickListener for CardView
        holder.itemView.setOnClickListener {
            itemClick(position)
        }
    }

    inner class ViewHolder(private var binding: ItemCardviewBinding) : RecyclerView.ViewHolder(binding.root) {
        // find elements from CardView

            var recipeCardName =binding.recipeName
            var recipeCardPortion = binding.recipePortion
            var recipeDuration = binding.recipeDuration
            var recipeCardRatingStars = binding.recipeRatingStars
            var recipeCardImage = binding.recipeImage



    }

    // timeString for showing on Card: separate hours and minutes
    private fun preparationTimeString(time: Int): String {
        var hours = 0
        val minutes: Int
        val minutesPerHour = 60 // for detekt
        if (time >= minutesPerHour) {
            hours = floor(time.toDouble() / minutesPerHour).toInt()
        }
        minutes = time - (hours * minutesPerHour)

        return if (hours != 0 && minutes != 0) {
            "duration: $hours h $minutes min"
        } else if (hours != 0 && minutes == 0) {
            "duration: $hours"
        } else if (hours == 0 && minutes != 0) {
            "duration: $minutes min"
        } else {
            "duration: -"
        }
    }

    private fun portionString(portions: Int): String {
        if (portions > 1) {
            return "$portions portions"
        }
        return "$portions portion"
    }
}
