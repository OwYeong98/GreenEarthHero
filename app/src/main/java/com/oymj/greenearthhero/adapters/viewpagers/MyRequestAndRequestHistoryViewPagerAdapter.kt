package com.oymj.greenearthhero.adapters.viewpagers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.oymj.greenearthhero.ui.fragment.CurrentRequestFragment
import com.oymj.greenearthhero.ui.fragment.RecycleRequestHistoryFragment
import com.oymj.greenearthhero.ui.fragment.SearchAddressResultFragment

class MyRequestAndRequestHistoryViewPagerAdapter(var context: Context, fm:FragmentManager) : FragmentPagerAdapter(fm){

    override fun getCount(): Int {
        return 2
    }


    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null

        when(position){
            0->{
                fragment= CurrentRequestFragment()
            }
            1->{
                fragment= RecycleRequestHistoryFragment()
            }
        }

        return fragment!!
    }


    override fun getPageTitle(position: Int): CharSequence? {
        var title:String? = null
        when(position){
            0->{
                title= "Current Request"
            }
            1->{
                title= "History"
            }
        }
        return title
    }
}