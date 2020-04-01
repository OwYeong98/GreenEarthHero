package com.oymj.greenearthhero

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_volunteer_collection.*

class VolunteerCollectionActivity : AppCompatActivity() {
    private lateinit var myBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var currentBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_collection)

        setupBottomSheet()

    }

    private fun setupBottomSheet(){
        myBottomSheetBehavior = BottomSheetBehavior.from(recycle_bottom_sheet)

        myBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (currentBottomSheetState != newState) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {

                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            showBackToListButton()
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {


                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {

                        }
                        BottomSheetBehavior.STATE_SETTLING -> {

                        }
                    }
                }
            }
        })
    }

    private fun showBackToListButton(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)
        btnBackToList.visibility=View.VISIBLE
        btnBackToList.startAnimation(slideUpAnimation)

    }

}