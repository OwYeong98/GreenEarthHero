package com.oymj.greenearthhero.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.spinner.DonateLocationSpinnerAdapter
import com.oymj.greenearthhero.api.ApisImplementation
import com.oymj.greenearthhero.data.Location
import com.oymj.greenearthhero.data.TomTomPosition
import com.oymj.greenearthhero.ui.activity.AddDonationLocationActivity
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.view.CardInputWidget
import kotlinx.android.synthetic.main.fragment_purchase_item_select_location_and_payment.*

class PurchaseItemSelectLocationAndPaymentFragment : Fragment() {

    companion object{
        const val ADD_DONATE_LOCATION_REQUEST_CODE = 2
    }

    lateinit var locationSpinnerAdapter:DonateLocationSpinnerAdapter
    var locationList =  ArrayList<Location>()

    lateinit var spinner:Spinner
    lateinit var btnEditLocation:ImageView
    lateinit var cardInputWidget:CardInputWidget
    lateinit var tvTotalAmount:TextView

    lateinit var loadingDialog: LoadingDialog

    lateinit var currentViewingItemId:String

    //stripe
    lateinit var stripe:Stripe

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_purchase_item_select_location_and_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stripe = Stripe(context!!, resources.getString(R.string.stripe_publishable_key))

        spinner = view.findViewById<Spinner>(R.id.locationSpinner)
        cardInputWidget = view.findViewById<CardInputWidget>(R.id.cardInputWidget)
        tvTotalAmount = view.findViewById<TextView>(R.id.tvTotalAmount)
        btnEditLocation = view.findViewById<ImageView>(R.id.btnLocationEdit)
        var btnPayNow = view.findViewById<TextView>(R.id.btnPayNow)

        setupSpinner()
        getListOfDonateLocationFromFirebase(null)
        btnEditLocation.setOnClickListener {
            var intent = Intent(context!!,AddDonationLocationActivity::class.java)
            intent.putExtra("donateLocation",spinner.selectedItem as Location)
            startActivityForResult(intent, ADD_DONATE_LOCATION_REQUEST_CODE)
        }

        btnPayNow.setOnClickListener {
//            var intent = Intent(context!!,StripePaymentActivity::class.java)
//            startActivity(intent)
            var selectedLoc=locationSpinner.selectedItem as Location
            if(selectedLoc.id == "-1" || selectedLoc.id == "-2"){
                var errorDialog = ErrorDialog(context!!,"Location Must be selected","Please select delivery location!!");
                errorDialog.show()
            }else{
                startCheckout(selectedLoc)
            }
        }
    }
    fun setItemId(itemId:String){
        currentViewingItemId = itemId
    }
    fun setTotalAmount(amount:Double){
        tvTotalAmount.text = "RM "+ String.format("%.2f",amount)
    }

    private fun setupSpinner(){
        locationSpinnerAdapter = DonateLocationSpinnerAdapter(context!!, locationList)
        spinner.adapter = locationSpinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(locationList.get(position).id == "-1" || locationList.get(position).id == "-2"){
                    btnEditLocation.visibility = View.GONE

                    if(locationList.get(position).id == "-1"){
                        spinner.setSelection(0)
                        //if add new location is selected
                        var intent = Intent(context!!, AddDonationLocationActivity::class.java)
                        startActivityForResult(intent, ADD_DONATE_LOCATION_REQUEST_CODE)
                    }

                }else{
                    btnEditLocation.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    fun getListOfDonateLocationFromFirebase(selectedDonationLocationId: String?){
        Location.getLocationListOfUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!,callback = {
                success,message,data->

            if(success){
                locationList.clear()
                locationList.addAll(data!!)

                locationList.add(0,Location("-2","null","Please select a location","null", TomTomPosition()))
                locationList.add(Location("-1","null","add new location ...","null", TomTomPosition()))

                if(selectedDonationLocationId!= null){
                    var selectedPosition=0

                    for(index in 0..locationList.size){
                        if(locationList!![index].id == selectedDonationLocationId){
                            selectedPosition= index
                            break
                        }
                    }
                    spinner.setSelection(selectedPosition)
                }else{
                    spinner.setSelection(0)
                }

                locationSpinnerAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun startCheckout(selectedLocation:Location) {

        if(cardInputWidget.paymentMethodCreateParams != null){
            loadingDialog = LoadingDialog(context!!)
            loadingDialog.show()

            FirebaseAuth.getInstance().currentUser?.getIdToken(true)
                ?.addOnCompleteListener {
                    task->

                    if(task.isSuccessful){
                        var idToken = task.result?.token

                        // Request a PaymentIntent from your server and store its client secret in paymentIntentClientSecret
                        ApisImplementation().initializePaymentIntent(context!!,idToken!!,currentViewingItemId,selectedLocation.id, callback = {
                                success,response->

                            if(success){
                                var secret = response?.content?.data?.secretKey!!
                                Log.d("wtf","Success. Secret: ${response?.content?.data?.secretKey}")
                                val params = cardInputWidget.paymentMethodCreateParams!!

                                PaymentConfiguration.init(context!!, resources.getString(R.string.stripe_publishable_key))

                                val confirmParams = ConfirmPaymentIntentParams
                                    .createWithPaymentMethodCreateParams(params, secret)
                                var stripe = Stripe(activity!!.applicationContext, PaymentConfiguration.getInstance(activity!!.applicationContext).publishableKey)
                                stripe.confirmPayment(this, confirmParams)

                            }else{
                                loadingDialog.dismiss()
                                var errorDialog = ErrorDialog(context!!,"Some Error occured while connection to server","This may due to server are having heavy connection. Error ${response?.content?.error}")
                                errorDialog.show()
                            }
                        })
                    }else{
                        loadingDialog.dismiss()
                        var errorDialog = ErrorDialog(context!!,"Some Error occured while connection to server","We could not get logged in firebase id token, make sure you are logged in.")
                        errorDialog.show()
                    }
                }
        }else{
            var errorDialog = ErrorDialog(context!!,"Card Detail Not Valid","Please fill in your credit/debit card detail.")
            errorDialog.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                loadingDialog.dismiss()
                val paymentIntent = result.intent
                val status = paymentIntent.status
                if (status == StripeIntent.Status.Succeeded) {
                    val gson = GsonBuilder().setPrettyPrinting().create()

                    Log.d("wtf", "Payment succeed. Gson: ${gson}")

                    var successDialog = SuccessDialog(context!!,"Purchased Successfully","Your Purchase is processed successfully. You can keep track of your purchase on My Purchase page.")
                    successDialog.show()
                } else {
                    Log.d("wtf", "Payment Failed")
                    var errorDialog = ErrorDialog(context!!,"Payment Gateway error!","Stripe encountered failure when processing your credit/debit card!")
                    errorDialog.show()
                }
            }

            override fun onError(e: Exception) {
                loadingDialog.dismiss()
                Log.d("wtf", "Payment Failed With Error: "+e.toString())
                var errorDialog = ErrorDialog(context!!,"Payment Gateway error!","Stripe encountered failure when processing your credit/debit card!")
                errorDialog.show()
            }
        })
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                ADD_DONATE_LOCATION_REQUEST_CODE -> {
                    val donateLocationId = data?.getStringExtra("id")
                    getListOfDonateLocationFromFirebase(donateLocationId)
                }
            }
        }
    }




}