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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemFoodEditable
import com.oymj.greenearthhero.adapters.spinner.DonateLocationSpinnerAdapter
import com.oymj.greenearthhero.adapters.spinner.TimeAvailableSpinnerAdapter
import com.oymj.greenearthhero.data.DonateLocation
import com.oymj.greenearthhero.data.Food
import com.oymj.greenearthhero.data.TomTomPosition
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.ImageStorageManager
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_add_food_donation.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddFoodDonationActivity : AppCompatActivity() {

    companion object{
        const val ADD_DONATE_LOCATION_REQUEST_CODE=1
        const val ADD_FOOD_REQUEST_CODE=2
        const val EDIT_FOOD_REQUEST_CODE=3
    }

    lateinit var donateLocationSpinnerAdapter:DonateLocationSpinnerAdapter
    var donateLocationList =  ArrayList<DonateLocation>()


    private lateinit var recyclerViewAdapter: UniversalAdapter
    var foodList =  ArrayList<Any>()
    lateinit var currentEditingFoodRef:Food

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
                btnDonateNow->{
                    if(foodList.size == 0){
                        var errorDialog = ErrorDialog(this@AddFoodDonationActivity, "No Food Added","You must at least donate some food.")
                        errorDialog.show()
                    }else if((donateLocationSpinner.selectedItem as DonateLocation).id == "-1" || (donateLocationSpinner.selectedItem as DonateLocation).id == "-2"){
                        var errorDialog = ErrorDialog(this@AddFoodDonationActivity, "Donate Location not Selected","Please select donate location for your donation!")
                        errorDialog.show()
                    }else{
                        addDonationRequestToFirebase()
                    }
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
                        //save referece so we can edit it when the user edit it
                        currentEditingFoodRef = data

                        //edit item pressed
                        var intent = Intent(this@AddFoodDonationActivity,AddFoodActivity::class.java)
                        intent.putExtra("name",data.foodName)
                        intent.putExtra("desc",data.foodDesc)
                        intent.putExtra("quantity",data.foodQuantity.toString())

                        var filename= "tempfoodimage.png"
                        intent.putExtra("foodImageUrl",filename)

                        var loadingDialog = LoadingDialog(this@AddFoodDonationActivity)
                        loadingDialog.show()

                        ImageStorageManager.saveImgToInternalStorage(this@AddFoodDonationActivity,data.imageBitmap,filename,callback = {
                            loadingDialog.dismiss()

                            startActivityForResult(intent, EDIT_FOOD_REQUEST_CODE)
                        })



                    }else if(clickType == 2){
                        //delete item pressed
                        foodList.remove(data)
                        recyclerViewAdapter.notifyDataSetChanged()
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

        if(resultCode == Activity.RESULT_OK){
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
                EDIT_FOOD_REQUEST_CODE->{
                    var foodName = data?.getStringExtra("name")!!
                    var foodDesc = data?.getStringExtra("desc")!!
                    var foodQty = data?.getStringExtra("quantity")!!.toInt()
                    var foodImageFileName = data?.getStringExtra("foodImageUrl")

                    var foodImage = ImageStorageManager.getImgFromInternalStorage(this,foodImageFileName)
                    ImageStorageManager.deleteImgFromInternalStorage(this,foodImageFileName)

                    currentEditingFoodRef.foodName = foodName
                    currentEditingFoodRef.foodDesc = foodDesc
                    currentEditingFoodRef.foodQuantity = foodQty
                    currentEditingFoodRef.imageBitmap = foodImage!!

                    recyclerViewAdapter.notifyDataSetChanged()

                }

            }
        }
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            tvTitle,
            btnDonateLocationEdit,
            btnAdd,
            btnDonateNow
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun addDonationRequestToFirebase(){

        var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8:00")

        var minutesAvailable = (timeAvailableSpinner.selectedItem as String).replace("hours","").trim().toInt() * 60
        var donateLocationId = (donateLocationSpinner.selectedItem as DonateLocation).id

        val donationData = hashMapOf(
            "datePosted" to dateFormat.format(Date()),
            "donateLocationId" to donateLocationId,
            "donatorUserId" to FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this),
            "minutesAvailable" to minutesAvailable
        )

        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        FirebaseFirestore.getInstance().collection("Food_Donation").add(donationData)
            .addOnSuccessListener {
                docRef->

                var docId=docRef.id

                var doneUploadedFood = Array<Boolean>(foodList.size,{x-> false})

                //add food to subcollection
                for(index in foodList.indices){
                    if(foodList[index] is Food){
                        var food = foodList[index] as Food

                        var storageRef = FirebaseStorage.getInstance().reference

                        var imgPath = "FoodDonation/FoodImg/${docId}/"+UUID.randomUUID().toString()
                        val newRef = storageRef.child(imgPath)

                        val baos = ByteArrayOutputStream()
                        food.imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        var uploadTask = newRef.putBytes(data)
                        uploadTask.addOnSuccessListener {
                            // Handle unsuccessful uploads
                            var foodName = food.foodName
                            var foodDesc = food.foodDesc
                            var foodQuantity = food.foodQuantity

                            val foodImageData = hashMapOf(
                                "foodName" to foodName,
                                "foodDesc" to foodDesc,
                                "foodQuantity" to foodQuantity,
                                "imageUrl" to imgPath
                            )

                            FirebaseFirestore.getInstance().collection("Food_Donation/$docId/Food_List").add(foodImageData)
                                .addOnSuccessListener {
                                    doneUploadedFood[index] = true

                                    var isAllTrue = doneUploadedFood.foldRight(true){previous,current-> previous&&current}

                                    if(isAllTrue){
                                        //if all is true means all food image and document is already upload to firebase
                                        loadingDialog.dismiss()

                                        var successDialog = SuccessDialog(this,"Successfully uploading your food donation!","The public will be able to view your donation and approach your location to claim your donation",callback = {
                                            finish()
                                        })
                                        successDialog.show()

                                    }
                                }
                                .addOnFailureListener {

                                }
                        }.addOnFailureListener {
                            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                            // ...
                        }
                    }
                }
            }
            .addOnFailureListener {

            }

    }


}