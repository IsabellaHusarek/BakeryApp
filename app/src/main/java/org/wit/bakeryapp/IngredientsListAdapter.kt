package org.wit.bakeryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.bakeryapp.data.IngredientsClass
import org.wit.bakeryapp.databinding.ItemCardviewBinding
import org.wit.bakeryapp.databinding.ItemIngredientsBinding
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class IngredientsListAdapter(
    private val ingredientsClass: IngredientsClass
) :
    RecyclerView.Adapter<IngredientsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        //val view = layoutInflater.inflate(R.layout.item_ingredients, parent, false)
        val view = ItemIngredientsBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ingredientsClass.quantity.size
    }

    inner class ViewHolder(private var binding: ItemIngredientsBinding) : RecyclerView.ViewHolder(binding.root) {
        var quantityView = binding.quantityIngredients
        var descriptionView = binding.descriptionIngredients
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val description: String = ingredientsClass.description[position]
        val quantity: Float = ingredientsClass.quantity[position]
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        if (quantity == 0F) {
            // do not show text field if quantity is 0
            holder.quantityView.text = ""
        } else {
            // do not show ending 0s
            if ((quantity % 1) == 0F) {
                holder.quantityView.text = quantity.roundToInt().toString()
            } else {
                // round to 2 decimal places
                holder.quantityView.text = df.format(quantity).toString()
            }
        }
        holder.descriptionView.text = description
    }
}
