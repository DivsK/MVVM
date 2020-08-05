package com.example.serviceyou.ui.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.serviceyou.R
import com.example.serviceyou.databinding.HomeActivityBinding
import kotlinx.android.synthetic.main.home_activity.*

/**
 * @author Divya Khanduri
 */

class HomeActivity : AppCompatActivity() {

    private var homeFragment: HomeFragment? = null
    private var fragmentTransaction: FragmentTransaction? = null

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val homeActivityBinding: HomeActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.home_activity)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeActivityBinding.viewModel = viewModel
        homeActivityBinding.lifecycleOwner = this

        setSupportActionBar(toolbar)

        viewModel.onFragmentAddClick.observe(this, Observer { clickEvent ->
            if (clickEvent) {
                fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction!!.add(R.id.container, homeFragment!!, "homeFragment")
                fragmentTransaction!!.addToBackStack("homeFragment")
                fragmentTransaction!!.commit()
            }
        })

        homeFragment = HomeFragment.newInstance()
    }
}
