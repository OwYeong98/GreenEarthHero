package com.oymj.greenearthhero.ui.fragment

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.spinner.DonateLocationSpinnerAdapter
import com.oymj.greenearthhero.data.DonateLocation
import com.oymj.greenearthhero.data.TomTomPosition
import com.oymj.greenearthhero.ui.activity.AddDonationLocationActivity
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_buyer_view_item_detail.*

class PurchaseItemSelectLocationAndPaymentFragment : Fragment() {

    companion object{
        const val ADD_DONATE_LOCATION_REQUEST_CODE = 1
    }

    lateinit var locationSpinnerAdapter:DonateLocationSpinnerAdapter
    var locationList =  ArrayList<DonateLocation>()

    lateinit var spinner:Spinner
    lateinit var btnEditLocation:ImageView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_purchase_item_select_location_and_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner = view.findViewById<Spinner>(R.id.locationSpinner)
        btnEditLocation = view.findViewById<ImageView>(R.id.btnLocationEdit)

        setupSpinner()
        getListOfDonateLocationFromFirebase(null)
        btnEditLocation.setOnClickListener {
            var intent = Intent(context!!,AddDonationLocationActivity::class.java)
            intent.putExtra("donateLocation",spinner.selectedItem as DonateLocation)
            activity!!.startActivityForResult(intent, ADD_DONATE_LOCATION_REQUEST_CODE)
        }
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
        DonateLocation.getDonateLocationListOfUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!,callback = {
                success,message,data->

            if(success){
                locationList.clear()
                locationList.addAll(data!!)

                locationList.add(0,DonateLocation("-2","null","Please select a location","null", TomTomPosition()))
                locationList.add(DonateLocation("-1","null","add new location ...","null", TomTomPosition()))

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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