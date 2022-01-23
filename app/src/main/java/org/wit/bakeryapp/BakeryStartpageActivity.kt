package org.wit.bakeryapp

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.wit.bakeryapp.databinding.ActivityBakeryStartpageBinding
import org.wit.bakeryapp.recipeList.RecipeListFragment
import org.wit.bakeryapp.shopping.ShoppingFragment


const val EXTRA_RECIPE_ID = "org.wit.bakeryapp.DOCUMENT_ID"

enum class ReqCodes(val reqCodes: Int) {
    CameraRequestCode(1234),
    GalleryRequestCode(5678)
}

lateinit var sharedPreferences: SharedPreferences

class BakeryStartpageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBakeryStartpageBinding
    private lateinit var bakeryStartpageActivityActionBar: MaterialToolbar
    private lateinit var bakeryStartpageActivityBottomNavBar : BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout

    companion object {
        const val TAG = "BakeryStartpageActivity"

        lateinit var recipesViewModel: RecipeViewModel

        lateinit var startpageActivityContext: Activity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBakeryStartpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.actionBarMainActivity
       //binding.bottomNavBar
        //binding.container
       // binding.navHostFragment

        startpageActivityContext = this

        viewCreation()

        // Setup Appbar
        setSupportActionBar(bakeryStartpageActivityActionBar)

        // Setup BottomNavBar
        bakeryStartpageActivityBottomNavBar.setOnNavigationItemSelectedListener(navListener)

        // select Fragment for Start -> don't add to backStack
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(fragmentContainer.id, RecipeListFragment())
            .commit()
        supportActionBar?.title = "Rezeptliste"

        // Setup ViewModel for Database-Connection
        recipesViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)

        // Shared Preferences
        sharedPreferences = SharedPreferences(this)

    }

    // OnClickListener for BottomNavBar
    private var navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        lateinit var selectedFragment: Fragment
        when (item.itemId) {
            R.id.navigation_recipelist -> {
                selectedFragment = RecipeListFragment()
                supportActionBar?.title = "Recipeslist"
            }
            R.id.navigation_info -> {
                selectedFragment = InfoFragment()
                supportActionBar?.title = "Info"
            }

            R.id.navigation_shoppingcart -> {
                selectedFragment = ShoppingFragment()
                supportActionBar?.title = "Shopping"
            }
        }

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(fragmentContainer.id, selectedFragment)
            .addToBackStack(null)
            .commit()

        true
    }

    private fun viewCreation() {
        bakeryStartpageActivityActionBar = binding.actionBarBakeryStartpageActivity
        bakeryStartpageActivityBottomNavBar = binding.bottomNavigationBar
        fragmentContainer = binding.navHostFragment
    }

}