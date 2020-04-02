package com.oymj.greenearthhero.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.IntroSliderAdapter
import com.oymj.greenearthhero.models.IntroSlide
import com.oymj.greenearthhero.models.SharedPreference
import kotlinx.android.synthetic.main.activity_intro.*


class IntroActivity : AppCompatActivity() {
    private val introSlideAdapter = IntroSliderAdapter(
        listOf(
            IntroSlide(
                "Green Earth Hero",
                "GreenEarthHero is a platform that promotes 3R (Reduce, Reuse, Recycle) activity.",
                R.raw.greenearthhero_anim
            ),
            IntroSlide(
                "Recycle Material",
                "Our Platform make material recycle easy. Create a request and you are done.",
                R.raw.recycle_material_anim
            ),
            IntroSlide(
                "Food Donation",
                "No More Food Waste! Food donation can be done easily through our platform.",
                R.raw.food_donation_anim
            ),
            IntroSlide(
                "Goods Selling",
                "Unused good can be sold in our platform to reduce the waste of items.",
                R.raw.good_selling_anim
            )
        )
    )
    private val isBackButtonLocked = true // back button should be disable in this intro page
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        intro_viewpager.adapter = introSlideAdapter
        setupViewpagerIndicator()//generate dot in the indicator container
        setCurrentIndicator(0)//initiate active dot at first slide


        intro_viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                val introButtonGradientDrawable = intro_button.getBackground() as GradientDrawable
                if (isFirstTimeUser()) {
                    if (position + 1 < introSlideAdapter.itemCount) {
                        //End of Page not reached yet, change the button to (Skip)
                        introButtonGradientDrawable.setColor(Color.parseColor("#BCBCBC"))
                        intro_button.text = "SKIP"
                    } else {
                        //End Of Page, change the button to (DONE)
                        introButtonGradientDrawable.setColor(Color.parseColor("#37B734"))
                        intro_button.text = "DONE"
                    }
                } else {
                    introButtonGradientDrawable.setColor(Color.parseColor("#37B734"))
                    intro_button.text = "DONE"
                }
            }
        })


        intro_button.setOnClickListener {
            if (isFirstTimeUser()) {
                if (intro_viewpager.currentItem + 1 == introSlideAdapter.itemCount) {
                    //done button
                    doneIntro()
                } else {
                    //skip button
                    skipIntro()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    /**
     * This function is to initiate the viewpager indicator.
     * Mainly for adding dot(ImageView) into the intro_indicator_container(LinearLayout) according to the introSlideAdapter.itemCount
     */
    private fun setupViewpagerIndicator() {
        val indicators = arrayOfNulls<ImageView>(introSlideAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            intro_indicator_container.addView(indicators[i])
        }
    }

    /**
     * This function is to update the viewpager indicator.
     * @param index The Position of the page which is currently on. Used to set the active dot
     */
    private fun setCurrentIndicator(index: Int) {
        val indicatorCount = intro_indicator_container.childCount
        for (i in 0 until indicatorCount) {
            val imageView = intro_indicator_container[i] as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    private fun skipIntro() {

        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        val mySharePreferenceService: SharedPreference =
                            SharedPreference(this)//get shared  / cache
                        mySharePreferenceService.save("isFirstTimeUser", false)

                        var intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to skip this introduction?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()


    }

    private fun doneIntro() {

        val mySharePreferenceService: SharedPreference = SharedPreference(this)//get shared  / cache
        mySharePreferenceService.save("isFirstTimeUser", false)

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }

    private fun isFirstTimeUser(): Boolean {
        val mySharePreferenceService: SharedPreference = SharedPreference(this)//get shared  / cache

        return mySharePreferenceService.getValueBoolean("isFirstTimeUser", true)
    }


    override fun onBackPressed() {
        if (isBackButtonLocked) {
            //do nothing
        } else {
            //if not lock proceed to normal back function
            super.onBackPressed()
        }
    }

}