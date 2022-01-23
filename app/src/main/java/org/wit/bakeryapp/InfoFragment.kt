package org.wit.bakeryapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.wit.bakeryapp.databinding.FragmentInfoBinding
import org.wit.bakeryapp.databinding.FragmentRecipeListBinding
import org.wit.bakeryapp.recipeList.RecipeListListAdapter


class InfoFragment : Fragment() {

    private lateinit var _binding: FragmentInfoBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment-XML
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val view= binding.root





        return view
    }

}
