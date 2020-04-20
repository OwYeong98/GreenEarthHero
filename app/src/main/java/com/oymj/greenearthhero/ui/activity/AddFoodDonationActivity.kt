package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.spinner.DonateLocationSpinnerAdapter
import com.oymj.greenearthhero.adapters.spinner.TimeAvailableSpinnerAdapter
import com.oymj.greenearthhero.data.DonateLocation
import com.oymj.greenearthhero.data.TomTomPosition
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_add_food_donation.*


class AddFoodDonationActivity : AppCompatActivity() {

    lateinit var donateLocationSpinnerAdapter:DonateLocationSpinnerAdapter
    var donateLocationList =  ArrayList<DonateLocation>()
    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnDonateLocationEdit ->{
                    var intent = Intent(this@AddFoodDonationActivity,AddDonationLocationActivity::class.java)
                    intent.putExtra("donateLocation",donateLocationSpinner.selectedItem as DonateLocation)
                    startActivityForResult(intent,2)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food_donation)

        setupSpinner()
        setupUI()
        linkAllButtonWithOnClickListener()
        getListOfDonateLocationFromFirebase(null)
    }

    fun setupSpinner(){
        donateLocationSpinnerAdapter = DonateLocationSpinnerAdapter(this, donateLocationList)
        donateLocationSpinner.adapter = donateLocationSpinnerAdapter
        donateLocationSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(donateLocationList.get(position).id == "-1" || donateLocationList.get(position).id == "-2"){
                    btnDonateLocationEdit.visibility = View.GONE

                    if(donateLocationList.get(position).id == "-1"){
                        //if add new location is selected
                        var intent = Intent(this@AddFoodDonationActivity,AddDonationLocationActivity::class.java)
                        startActivityForResult(intent,2)
                    }

                }else{
                    btnDonateLocationEdit.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        var timeAvailable = arrayListOf<String>()
        for(hour in 1..10){
            timeAvailable.add("$hour hours")
        }
        timeAvailableSpinner.adapter = TimeAvailableSpinnerAdapter(this,timeAvailable)
    }

    fun setupUI(){
        donateLocationSpinner.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            40f,0)

        timeAvailableSpinner.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            40f,0)

        btnDonateNow.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            Color.parseColor("#37B734"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        btnCancel.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.lightgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

    }

    private fun getListOfDonateLocationFromFirebase(selectedDonationLocationId: String?){
        DonateLocation.getDonateLocationListOfUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!,callback = {
            success,message,data->

            if(success){
                donateLocationList.clear()
                donateLocationList.addAll(data!!)

                donateLocationList.add(0,DonateLocation("-2","null","Please select a location","null", TomTomPosition()))
                donateLocationList.add(DonateLocation("-1","null","add new location ...","null", TomTomPosition()))

                if(selectedDonationLocationId!= null){
                    var selectedPosition=0

                    for(index in 0..donateLocationList.size){
                        if(donateLocationList!![index].id == selectedDonationLocationId){
                            selectedPosition= index
                            break
                        }
                    }
                    donateLocationSpinner.setSelection(selectedPosition)
                }else{
                    donateLocationSpinner.setSelection(0)
                }

                donateLocationSpinnerAdapter.notifyDataSetChanged()

            }
        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2) {
            val donateLocationId = data?.getStringExtra("id")

            getListOfDonateLocationFromFirebase(donateLocationId)
        }
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            tvTitle,
            btnDonateLocationEdit
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

}