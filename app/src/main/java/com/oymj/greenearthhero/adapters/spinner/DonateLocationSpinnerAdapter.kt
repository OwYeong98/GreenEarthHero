package com.oymj.greenearthhero.adapters.spinner

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.Location
import com.oymj.greenearthhero.utils.RippleUtil

class DonateLocationSpinnerAdapter(val context: Context, var data: ArrayList<Location>) : BaseAdapter() {


    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.spinneritem_simple, parent, false)
            vh = ItemRowHolder(view,position)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        vh.initialize(data[position])



        return view
    }

    override fun getItem(position: Int): Any? {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun isEnabled(position: Int): Boolean {
        return data[position].id != "-2"
    }

    inner class ItemRowHolder(var view: View?, var position:Int) {

        fun initialize(data:Location){
            var label = view?.findViewById(R.id.tvTitle) as TextView
            var mainContainer = view?.findViewById(R.id.mainContainer) as FrameLayout

            if(data.id == "-2"){
                label.setTextColor(Color.parseColor("#7A7C79"))
            }

            label.text = data.name

            mainContainer.background = RippleUtil.getRippleButtonOutlineDrawable(view!!.context!!,
                view!!.context!!.resources.getColor(R.color.darkgrey),
                view!!.context!!.resources.getColor(R.color.transparent_pressed),
                view!!.context!!.resources.getColor(R.color.transparent),
                30f,0)

        }
    }
}