package com.oymj.greenearthhero.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.oymj.greenearthhero.data.IntroSlide
import com.oymj.greenearthhero.R

class IntroSliderAdapter(private val introSlide: List<IntroSlide>) :
    RecyclerView.Adapter<IntroSliderAdapter.IntroSlideViewHolder>(){
    //Tips: (Ctrl + i) to check available override function
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroSlideViewHolder {
        return IntroSlideViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.intro_slide_template, parent, false))
    }

    override fun getItemCount(): Int {
        return introSlide.size
    }

    override fun onBindViewHolder(holder: IntroSlideViewHolder, position: Int) {
        holder.bind(introSlide[position])
    }

    inner class IntroSlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle = view.findViewById<TextView>(R.id.slide_title)
        private val textDesc = view.findViewById<TextView>(R.id.slide_description)
        private val imageIcon = view.findViewById<LottieAnimationView>(R.id.slide_icon)

        fun bind(introSlide: IntroSlide) {
            textTitle.text = introSlide.title
            textDesc.text = introSlide.description
            imageIcon.setAnimation(introSlide.icon)
        }
    }
}