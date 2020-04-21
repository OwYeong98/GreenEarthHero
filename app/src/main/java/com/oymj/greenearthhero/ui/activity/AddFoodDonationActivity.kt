package com.oymj.greenearthhero.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemFoodEditable
import com.oymj.greenearthhero.adapters.spinner.DonateLocationSpinnerAdapter
import com.oymj.greenearthhero.adapters.spinner.TimeAvailableSpinnerAdapter
import com.oymj.greenearthhero.data.DonateLocation
import com.oymj.greenearthhero.data.Food
import com.oymj.greenearthhero.data.TomTomPosition
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.ImageStorageManager
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_add_food_donation.*


class AddFoodDonationActivity : AppCompatActivity() {

    companion object{
        const val ADD_DONATE_LOCATION_REQUEST_CODE=1
        const val ADD_FOOD_REQUEST_CODE=2
    }

    lateinit var donateLocationSpinnerAdapter:DonateLocationSpinnerAdapter
    var donateLocationList =  ArrayList<DonateLocation>()

    private lateinit var recyclerViewAdapter: UniversalAdapter
    var foodList =  ArrayList<Any>()

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnDonateLocationEdit ->{
                    var intent = Intent(this@AddFoodDonationActivity,AddDonationLocationActivity::class.java)
                    intent.putExtra("donateLocation",donateLocationSpinner.selectedItem as DonateLocation)
                    startActivityForResult(intent, ADD_DONATE_LOCATION_REQUEST_CODE)
                }
                btnAdd->{
                    var intent = Intent(this@AddFoodDonationActivity,AddFoodActivity::class.java)
                    startActivityForResult(intent,ADD_FOOD_REQUEST_CODE)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food_donation)

        setupSpinner()
        setupRecyclerView()
        setupUI()
        linkAllButtonWithOnClickListener()
        getListOfDonateLocationFromFirebase(null)
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(foodList,this@AddFoodDonationActivity,foodOfferedRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 20
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is Food) {

                    if(clickType == 1){
                        //edit item pressed

                    }else if(clickType == 2){
                        //delete item pressed

                    }
                }
            }

            override fun getItemViewType(position: Int): Int {
                return if(data.get(position)::class.java.simpleName == "Food"){
                    -1
                }else {
                    super.getItemViewType(position)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return if(viewType == -1){
                    RecyclerItemFoodEditable().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }
        }
        foodOfferedRecyclerView.layoutManager = LinearLayoutManager(this)
        foodOfferedRecyclerView.adapter = recyclerViewAdapter
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

        Log.d("wtf","activity result")
        when(requestCode){
            ADD_DONATE_LOCATION_REQUEST_CODE->{
                val donateLocationId = data?.getStringExtra("id")

                getListOfDonateLocationFromFirebase(donateLocationId)
            }
            ADD_FOOD_REQUEST_CODE->{
                var foodName = data?.getStringExtra("name")!!
                var foodDesc = data?.getStringExtra("desc")!!
                var foodQty = data?.getStringExtra("quantity")!!.toInt()
                var foodImageFileName = data?.getStringExtra("foodImageUrl")

                var foodImage = ImageStorageManager.getImgFromInternalStorage(this,foodImageFileName)

                val newFood = Food(foodName,foodDesc,foodQty,"",foodImage!!)
                foodList.add(newFood)
                recyclerViewAdapter.notifyDataSetChanged()

            }

        }


    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            tvTitle,
            btnDonateLocationEdit,
            btnAdd
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

}