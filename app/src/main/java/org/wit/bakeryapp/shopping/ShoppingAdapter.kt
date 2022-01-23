package org.wit.bakeryapp.shopping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import org.wit.bakeryapp.R
import org.wit.bakeryapp.data.ShoppingItem
import org.wit.bakeryapp.databinding.ItemCardviewBinding
import org.wit.bakeryapp.databinding.ItemShoppingCheckboxBinding

class ShoppingCartListAdapter(private val data: List<ShoppingItem>, val itemClick: (Int, Int) -> Unit) :
    RecyclerView.Adapter<ShoppingCartListAdapter.ViewHolder>() {
    // extends RecyclerView.Adapter<ListAdapter.ViewHolder>()
    // itemClick:
    // first Int for Position
    // second Int for Action (0 = noAction, 1 = CheckBox Changed, 2 = Item removed),
    // Boolean for isChecked Status

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemShoppingCheckboxBinding.inflate(layoutInflater, parent, false)
        

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemClass: ShoppingItem = data[position]

        val itemTitle = itemClass.itemTitle
        val itemChecked = itemClass.checkedStatus

        holder.checkBox.text = itemTitle
        holder.checkBox.isChecked = itemChecked

        // onClickListener on CheckBox because onCheckedChangeListener get's triggered by changing value of checkedStatus
        holder.checkBox.setOnClickListener {
            itemClick(position, 1)
        }

        // onClickListener for removeButton of a single Item
        holder.checkBoxRemoveButton.setOnClickListener {
            itemClick(position, 2)
        }
    }

    inner class ViewHolder(private var binding: ItemShoppingCheckboxBinding) : RecyclerView.ViewHolder(binding.root) {
        var checkBox = binding.shoppingCartListItem
        var checkBoxRemoveButton = binding.shoppingCartRemoveItem
    }
}
