package com.oymj.greenearthhero.adapters.viewpagers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.oymj.greenearthhero.ui.fragment.CurrentPostFragment
import com.oymj.greenearthhero.ui.fragment.FoodDonationHistoryFragment
import com.oymj.greenearthhero.ui.fragment.MyDonationFragment
import com.oymj.greenearthhero.ui.fragment.SalesHistoryFragment

class CurrentPostAndSalesHistoryViewPagerAdapter(var context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null

        when(position){
            0->{
                fragment= CurrentPostFragment()
            }
            1->{
                fragment= SalesHistoryFragment()
            }
        }

        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title:String? = null
        when(position){
            0->{
                title= "Current Post"
            }
            1->{
                title= "Sales History"
            }
        }
        return title
    }
}