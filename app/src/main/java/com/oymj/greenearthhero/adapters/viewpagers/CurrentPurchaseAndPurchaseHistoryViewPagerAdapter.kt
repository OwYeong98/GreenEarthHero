package com.oymj.greenearthhero.adapters.viewpagers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.oymj.greenearthhero.ui.fragment.CurrentPurchaseFragment
import com.oymj.greenearthhero.ui.fragment.PurchaseHistoryFragment

class CurrentPurchaseAndPurchaseHistoryViewPagerAdapter(var context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm){

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null

        when(position){
            0->{
                fragment= CurrentPurchaseFragment()
            }
            1->{
                fragment= PurchaseHistoryFragment()
            }
        }

        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title:String? = null
        when(position){
            0->{
                title= "Current Purchase"
            }
            1->{
                title= "Purchase History"
            }
        }
        return title
    }
}