package com.oymj.greenearthhero.ui.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.ImageStorageManager
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_add_food.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AddFoodActivity : AppCompatActivity() {

    val PICK_IMAGE_FROM_PHONE_REQUEST_CODE = 1
    val TAKE_PICTURE_REQUEST_CODE = 2
    var isImageSelected = false

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnAdd->{
                    if(validate()){
                        if(isImageSelected == false){
                            var errorDialog = ErrorDialog(this@AddFoodActivity,"Image Cannot be null","Please input Food Image by either select from phone or take picture!")
                            errorDialog.show()
                        }else if((imageSelectedContainer.drawable as BitmapDrawable).bitmap.byteCount > 1024 * 1024 * 10){
                            //if image size larger than 10MB
                            var errorDialog = ErrorDialog(this@AddFoodActivity,"Image Size is too large","Image Size cannot be more than 10 MegaByte!")
                            errorDialog.show()
                        }else{
                            var name:String = inputFoodName.text.toString()
                            var desc: String = inputFoodDesc.text.toString()
                            var quantity: String = inputFoodQuantity.text.toString()
                            var foodImage:Bitmap = (imageSelectedContainer.drawable as BitmapDrawable).bitmap


                            var filename= "tempfoodimage.png"

                            var loadingDialog = LoadingDialog(this@AddFoodActivity)
                            loadingDialog.show()
                            ImageStorageManager.saveImgToInternalStorage(this@AddFoodActivity,foodImage,filename,callback = {
                                absolutePath ->
                                loadingDialog.dismiss()

                                var intent = Intent()
                                intent.putExtra("name",name)
                                intent.putExtra("desc",desc)
                                intent.putExtra("quantity",quantity)
                                intent.putExtra("foodImageUrl",filename)
                                setResult(Activity.RESULT_OK,intent)
                                finish()
                            })
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
        setContentView(R.layout.activity_add_food)

        setupUI()
        linkAllButtonWithOnClickListener()

        if(intent.getStringExtra("name") != null){
            var foodName = intent.getStringExtra("name")!!
            var foodDesc = intent.getStringExtra("desc")!!
            var foodQty = intent.getStringExtra("quantity")!!.toInt()
            var foodImageFileName = intent.getStringExtra("foodImageUrl")

            var foodImage = ImageStorageManager.getImgFromInternalStorage(this,foodImageFileName)
            setEditFoodPreviousData(foodName,foodDesc,foodQty,foodImage!!)

        }
    }

    fun setEditFoodPreviousData(name:String,desc:String,quantity:Int,imageBitmap: Bitmap){
        btnAdd.text = "Edit"
        inputFoodName.setText(name)
        inputFoodDesc.setText(desc)
        inputFoodQuantity.setText(quantity.toString())
        imageSelectedContainer.visibility = View.VISIBLE
        imageSelectedContainer.setImageBitmap(imageBitmap)
        isImageSelected=true

    }

    fun setupUI(){

        inputFoodName.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        inputFoodDesc.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)

        inputFoodQuantity.background = RippleUtil.getRippleButtonOutlineDrawable(this,
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

        btnAdd.background = RippleUtil.getRippleButtonOutlineDrawable(this,
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

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnAdd,
            btnCancel,
            btnBrowseFromYourPhone,
            btnTakePhoto
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun validate():Boolean{
        var name:String = inputFoodName.text.toString()
        var desc: String = inputFoodDesc.text.toString().trim()
        var quantity: String = inputFoodQuantity.text.toString()

        var nameError = ""
        var descError = ""
        var quantityError = ""

        nameError+= "${FormUtils.isNull("Name",name)?:""}|"
        nameError+= "${FormUtils.isLengthBetween("Name",name,5,20)?:""}|"
        nameError+= "${FormUtils.isOnlyAlphabet("Name",name)?:""}|"

        descError+= "${FormUtils.isNull("Description",desc)?:""}|"
        descError+= "${FormUtils.isLengthBetween("Description",desc,10,100)?:""}|"
        descError+= "${FormUtils.isOnlyAlphabet("Description",desc)?:""}|"

        quantityError+= "${FormUtils.isNull("Quantity",quantity)?:""}|"
        quantityError+= "${FormUtils.isOnlyNumber("Quantity",quantity)?:""}|"
        quantityError+= "${FormUtils.isNumberBetween("Quantity",quantity,1,null)?:""}|"

        if(nameError!=""){
            for(err in nameError.split("|")){
                if(err!=""){
                    inputFoodName.error = err
                    break
                }
            }
        }

        if(descError!=""){
            for(err in descError.split("|")){
                if(err!=""){
                    inputFoodDesc.error = err
                    break
                }
            }
        }

        if(quantityError!=""){
            for(err in quantityError.split("|")){
                if(err!=""){
                    inputFoodQuantity.error = err
                    break
                }
            }
        }

        var allError = nameError+descError+quantityError

        return allError.replace("|","")== ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK){
            when (requestCode){
                PICK_IMAGE_FROM_PHONE_REQUEST_CODE->{
                    isImageSelected=true
                    var selectedImage = data?.getData()
                    imageSelectedContainer.setImageURI(selectedImage)
                    imageSelectedContainer.visibility = View.VISIBLE
                }
                TAKE_PICTURE_REQUEST_CODE->{
                    isImageSelected=true
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