package com.oymj.greenearthhero.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.ImageStorageManager
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_post_new_item.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PostNewItemActivity : AppCompatActivity() {

    val PICK_IMAGE_FROM_PHONE_REQUEST_CODE = 1
    val TAKE_PICTURE_REQUEST_CODE = 2
    var isImageSelected = false
    var isImageChanged = false
    lateinit var editingItemId:String

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnPost->{
                    if(validate()){
                        if(isImageSelected == false){
                            var errorDialog = ErrorDialog(this@PostNewItemActivity,"Image Cannot be null","Please input Food Image by either select from phone or take picture!")
                            errorDialog.show()
                        }else if((imageSelectedContainer.drawable as BitmapDrawable).bitmap.byteCount > 1024 * 1024 * 10){
                            //if image size larger than 10MB
                            var errorDialog = ErrorDialog(this@PostNewItemActivity,"Image Size is too large","Image Size cannot be more than 10 MegaByte!")
                            errorDialog.show()
                        }else {
                            var name: String = inputItemName.text.toString()
                            var desc: String = inputItemDesc.text.toString()
                            var price: Double = inputPrice.text.toString().toDouble()
                            var image: Bitmap =
                                (imageSelectedContainer.drawable as BitmapDrawable).bitmap

                            if(!::editingItemId.isInitialized)
                                addNewItemToFirebases(name,desc,price,image)
                            else
                                editItemToFirebase(name,desc,price,image)

                        }
                    }
                }
                btnCancel->{
                    finish()
                }
                btnBrowseFromYourPhone->{
                    var intent = Intent(Intent.ACTION_PICK)

                    intent.setType("image/*")

                    var mimeTypes = arrayOf("image/jpeg","image/png")
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes)

                    startActivityForResult(intent,PICK_IMAGE_FROM_PHONE_REQUEST_CODE)
                }
                btnTakePhoto->{
                    var cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, TAKE_PICTURE_REQUEST_CODE)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_new_item)

        setupUI()
        linkAllButtonWithOnClickListener()

        if(intent.getStringExtra("name") != null){
            var id = intent.getStringExtra("id")
            var foodName = intent.getStringExtra("name")!!
            var foodDesc = intent.getStringExtra("desc")!!
            var foodQty = intent.getDoubleExtra("price",0.0)!!
            var foodImageFileName = intent.getStringExtra("foodImageUrl")

            var foodImage = ImageStorageManager.getImgFromInternalStorage(this,foodImageFileName)
            editingItemId = id
            setEditFoodPreviousData(foodName,foodDesc,foodQty,foodImage!!)

        }
    }

    fun setEditFoodPreviousData(name:String,desc:String,quantity:Double,imageBitmap: Bitmap){
        btnPost.text = "Edit Post"
        inputItemName.setText(name)
        inputItemDesc.setText(desc)
        inputPrice.setText(quantity.toString())
        imageSelectedContainer.visibility = View.VISIBLE
        imageSelectedContainer.setImageBitmap(imageBitmap)
        isImageSelected=true

    }

    fun setupUI(){

        inputItemName.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        inputItemDesc.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        inputPrice.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        btnBrowseFromYourPhone.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            Color.parseColor("#C0C4C0"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        btnTakePhoto.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            Color.parseColor("#C0C4C0"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        btnPost.background = RippleUtil.getRippleButtonOutlineDrawable(this,
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

    private fun addNewItemToFirebases(name:String,desc:String,price:Double,imageBitmap: Bitmap){

        var loadingDialog = LoadingDialog(this@PostNewItemActivity)
        loadingDialog.show()
        //create new item in Firebase
        var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8:00")
        val itemData = hashMapOf(
            "datePosted" to dateFormat.format(Date()),
            "postedBy" to FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@PostNewItemActivity),
            "itemName" to name,
            "itemDesc" to desc,
            "itemPrice" to price,
            "imageUrl" to "",
            "boughtBy" to "",
            "courierCompany" to "",
            "trackingNo" to ""
        )

        FirebaseFirestore.getInstance().collection("Second_Hand_Item").add(itemData)
            .addOnSuccessListener {
                    docRef->
                //Upload Image to firebase Storage
                var storageRef = FirebaseStorage.getInstance().reference

                var imgPath = "Second_Hand_Item/ItemImages/${docRef.id}/"+UUID.randomUUID().toString()
                val newRef = storageRef.child(imgPath)

                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                var uploadTask = newRef.putBytes(data)

                uploadTask.addOnSuccessListener {

                    //update image url
                    docRef.update(mapOf("imageUrl" to imgPath)).addOnSuccessListener {
                        loadingDialog.dismiss()
                        var successDialog = SuccessDialog(this@PostNewItemActivity,"Successfully uploaded your Item Post!","The public will be able to view your post and purchase it.",callback = {
                            finish()
                        })
                        successDialog.show()
                    }.addOnFailureListener {
                        loadingDialog.dismiss()
                        var errorDialog = ErrorDialog(this@PostNewItemActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                        errorDialog.show()
                    }
                }.addOnFailureListener {
                    loadingDialog.dismiss()
                    var errorDialog = ErrorDialog(this@PostNewItemActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                    errorDialog.show()
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                var errorDialog = ErrorDialog(this@PostNewItemActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                errorDialog.show()
            }

    }

    private fun editItemToFirebase(name:String,desc:String,price:Double,imageBitmap: Bitmap){

        var loadingDialog = LoadingDialog(this@PostNewItemActivity)
        loadingDialog.show()
        //create new item in Firebase
        var dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8:00")
        val itemData = mapOf(
            "itemName" to name,
            "itemDesc" to desc,
            "itemPrice" to price
        )


        FirebaseFirestore.getInstance().collection("Second_Hand_Item").document(editingItemId).update(itemData)
            .addOnSuccessListener {
                    docRef->

                if(isImageChanged == true){
                    //Upload Image to firebase Storage
                    var storageRef = FirebaseStorage.getInstance().reference

                    var imgPath = "Second_Hand_Item/ItemImages/$editingItemId/"+UUID.randomUUID().toString()
                    val newRef = storageRef.child(imgPath)

                    val baos = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    var uploadTask = newRef.putBytes(data)

                    uploadTask.addOnSuccessListener {

                        //update image url
                        FirebaseFirestore.getInstance().collection("Second_Hand_Item").document(editingItemId).update(mapOf("imageUrl" to imgPath)).addOnSuccessListener {
                            loadingDialog.dismiss()
                            var successDialog = SuccessDialog(this@PostNewItemActivity,"Successfully updated your Item Post!","The public will be able to view your post and purchase it.",callback = {
                                finish()
                            })
                            successDialog.show()
                        }.addOnFailureListener {
                            loadingDialog.dismiss()
                            var errorDialog = ErrorDialog(this@PostNewItemActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                            errorDialog.show()
                        }
                    }.addOnFailureListener {
                        loadingDialog.dismiss()
                        var errorDialog = ErrorDialog(this@PostNewItemActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                        errorDialog.show()
                    }
                }else{
                    loadingDialog.dismiss()
                    var successDialog = SuccessDialog(this@PostNewItemActivity,"Successfully updated your Item Post!","The public will be able to view your post and purchase it.",callback = {
                        finish()
                    })
                    successDialog.show()
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                var errorDialog = ErrorDialog(this@PostNewItemActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                errorDialog.show()
            }

    }


    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnPost,
            btnCancel,
            btnBrowseFromYourPhone,
            btnTakePhoto
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun validate():Boolean{
        var name:String = inputItemName.text.toString()
        var desc: String = inputItemDesc.text.toString().trim()
        var price: String = inputPrice.text.toString()

        var nameError = ""
        var descError = ""
        var priceError = ""

        nameError+= "${FormUtils.isNull("Name",name)?:""}|"
        nameError+= "${FormUtils.isLengthBetween("Name",name,5,20)?:""}|"
        nameError+= "${FormUtils.isOnlyAlphabet("Name",name)?:""}|"

        descError+= "${FormUtils.isNull("Description",desc)?:""}|"
        descError+= "${FormUtils.isLengthBetween("Description",desc,10,100)?:""}|"
        descError+= "${FormUtils.isOnlyAlphabetNumberWithComaAndDotSymbol("Description",desc)?:""}|"

        priceError+= "${FormUtils.isNull("Price",price)?:""}|"
        priceError+= "${FormUtils.isOnlyNumber("Price",price)?:""}|"
        priceError+= "${FormUtils.isNumberBetween("Price",price,1,null)?:""}|"

        if(nameError!=""){
            for(err in nameError.split("|")){
                if(err!=""){
                    inputItemName.error = err
                    break
                }
            }
        }

        if(descError!=""){
            for(err in descError.split("|")){
                if(err!=""){
                    inputItemDesc.error = err
                    break
                }
            }
        }

        if(priceError!=""){
            for(err in priceError.split("|")){
                if(err!=""){
                    inputPrice.error = err
                    break
                }
            }
        }

        var allError = nameError+descError+priceError

        return allError.replace("|","")== ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK){
            when (requestCode){
                PICK_IMAGE_FROM_PHONE_REQUEST_CODE->{
                    isImageSelected=true
                    isImageChanged=true
                    var selectedImage = data?.getData()
                    imageSelectedContainer.setImageURI(selectedImage)
                    imageSelectedContainer.visibility = View.VISIBLE
                }
                TAKE_PICTURE_REQUEST_CODE->{
                    isImageSelected=true
                    isImageChanged=true
                    var imageTaken = data?.getExtras()?.get("data") as Bitmap
                    imageSelectedContainer.setImageBitmap(imageTaken)
                    imageSelectedContainer.visibility = View.VISIBLE

                    var foodImage:Bitmap = (imageSelectedContainer.drawable as BitmapDrawable).bitmap
                    imageSelectedContainer.setImageBitmap(foodImage)
                }

            }
        }
    }
}