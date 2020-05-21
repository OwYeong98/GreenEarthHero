package com.oymj.greenearthhero.ui.customxmllayout

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import com.oymj.greenearthhero.R
import org.w3c.dom.Text
import java.lang.Exception
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

class CustomNumpad : FrameLayout {

    companion object{
        const val BUTTON_NUM_0 = 0
        const val BUTTON_NUM_1 = 1
        const val BUTTON_NUM_2 = 2
        const val BUTTON_NUM_3 = 3
        const val BUTTON_NUM_4 = 4
        const val BUTTON_NUM_5 = 5
        const val BUTTON_NUM_6 = 6
        const val BUTTON_NUM_7 = 7
        const val BUTTON_NUM_8 = 8
        const val BUTTON_NUM_9 = 9
        const val BUTTON_OK = 10
        const val BUTTON_DELETE = 11
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ initializeNumpad(context,attrs)}

    private lateinit var myNumpadRootView:View
    private lateinit var resultKeyContainer:LinearLayout
    private lateinit var btnDelete:ImageView
    private lateinit var btnOk:TextView
    private var numberButtonList = ArrayList<TextView>()
    private var resultKeyList = ArrayList<TextView>()
    private var resultKeyUnderLineList = ArrayList<View>()

    var onOkClickListener:(resultKey:String)->Unit = {}
    var onAllKeyEnteredListener:(resultKey:String)->Unit = {}
    var onKeyPressedListener:(key:Int)->Unit = {}
    private var whenAllNumpadUIReady:()->Unit = {}

    @ColorInt private var buttonTextColor:Int = Color.GRAY
    @ColorInt private var buttonBackgroundGradientStartColor:Int = Color.WHITE
    @ColorInt private var buttonBackgroundGradientEndColor:Int = Color.WHITE
    @ColorInt private var buttonOutlineGradientStartColor:Int = Color.parseColor("#EF5656")
    @ColorInt private var buttonOutlineGradientEndColor:Int = Color.parseColor("#D81B60")
    @ColorInt private var buttonPressedColor:Int = Color.parseColor("#c4c2bc")
    private var textTypeface:Typeface? = null
    private var textStyle:Int? = null
    private var buttonCornerRadius:Float = 200f
    private var buttonOutlineThickness:Int = 2
    private var buttonTextSizeInPercentage = 25
    private var buttonTextSizeInSp = 13f
    private var deleteButtonSizeInPercent = 25
    private var resultKeyTextSizeInPercentage = 20
    private var resultKeyTextSizeInSp = 13f
    private var resultKeyContainerHeight = 20
    private var numberOfResultKey = 4
    @ColorInt private var resultKeyNormalColor:Int = Color.parseColor("#c4c2bc")
    @ColorInt private var resultKeyEnteredColor:Int = Color.parseColor("#D81B60")


    private var isKeyReadyList = Array<Boolean>(12,{false})
    var isAllKeyReady = false

    private fun initializeNumpad(context: Context, attrs: AttributeSet?) {
        myNumpadRootView = LayoutInflater.from(context).inflate(R.layout.userinterface_numpad, this, true)
        resultKeyContainer = myNumpadRootView.findViewById<LinearLayout>(R.id.resultContainer)
        getAllAttributeFromXml(attrs)
        locateAllButtonRef()

        generateResultKeys(numberOfResultKey)
        initializeUI()

    }

    private fun getAllAttributeFromXml(attrs: AttributeSet?){
        val myAttribute = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomNumpad,
            0, 0)

        try {
            var customFont = myAttribute.getString(R.styleable.CustomNumpad_textTypeFace)
            try {
                textTypeface = Typeface.createFromAsset(context.getAssets(), customFont)
            } catch ( e:Exception) {
                textTypeface = null
            }

            textStyle = if(myAttribute.getInt(R.styleable.CustomNumpad_textFontStyle,-1) == -1) null else myAttribute.getInt(R.styleable.CustomNumpad_textFontStyle,-1)
            buttonTextColor = myAttribute.getColor(R.styleable.CustomNumpad_buttonTextColor, Color.GRAY)
            buttonBackgroundGradientStartColor = myAttribute.getColor(R.styleable.CustomNumpad_buttonBackgroundGradientStartColor, Color.WHITE)
            buttonBackgroundGradientEndColor = myAttribute.getColor(R.styleable.CustomNumpad_buttonBackgroundGradientEndColor, Color.WHITE)
            buttonOutlineGradientStartColor = myAttribute.getColor(R.styleable.CustomNumpad_buttonOutlineGradientStartColor, Color.parseColor("#EF5656"))
            buttonOutlineGradientEndColor = myAttribute.getColor(R.styleable.CustomNumpad_buttonOutlineGradientEndColor, Color.parseColor("#D81B60"))
            buttonPressedColor = myAttribute.getColor(R.styleable.CustomNumpad_buttonPressedColor, Color.parseColor("#c4c2bc"))
            buttonCornerRadius = myAttribute.getFloat(R.styleable.CustomNumpad_buttonCornerRadius, 200f)
            buttonOutlineThickness = myAttribute.getInt(R.styleable.CustomNumpad_buttonOutlineThickness, 2)
            buttonTextSizeInPercentage = myAttribute.getInt(R.styleable.CustomNumpad_buttonTextSizeInPercentage, 25)
            buttonTextSizeInSp = myAttribute.getFloat(R.styleable.CustomNumpad_buttonTextSizeInSp, 13f)
            deleteButtonSizeInPercent = myAttribute.getInt(R.styleable.CustomNumpad_deleteButtonSizeInPercent, 25)
            numberOfResultKey = myAttribute.getInt(R.styleable.CustomNumpad_numberOfResultKey, 4)
            resultKeyTextSizeInPercentage = myAttribute.getInt(R.styleable.CustomNumpad_resultKeyTextSizeInPercentage, 30)
            resultKeyTextSizeInSp = myAttribute.getFloat(R.styleable.CustomNumpad_resultKeyTextSizeInSp, 13f)
            resultKeyContainerHeight = myAttribute.getInt(R.styleable.CustomNumpad_resultKeyContainerHeight, 0)
            resultKeyNormalColor = myAttribute.getColor(R.styleable.CustomNumpad_resultKeyNormalColor, Color.parseColor("#c4c2bc"))
            resultKeyEnteredColor = myAttribute.getColor(R.styleable.CustomNumpad_resultKeyEnteredColor, Color.parseColor("#D81B60"))
        }
        finally {
            myAttribute.recycle()
        }

    }


    private fun locateAllButtonRef(){
        btnDelete = myNumpadRootView.findViewById<ImageView>(R.id.btnDelete)
        btnOk = myNumpadRootView.findViewById<TextView>(R.id.btnOk)
        var num0Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum0)
        var num1Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum1)
        var num2Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum2)
        var num3Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum3)
        var num4Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum4)
        var num5Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum5)
        var num6Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum6)
        var num7Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum7)
        var num8Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum8)
        var num9Button = myNumpadRootView.findViewById<TextView>(R.id.btnNum9)

        numberButtonList.add(num0Button)
        numberButtonList.add(num1Button)
        numberButtonList.add(num2Button)
        numberButtonList.add(num3Button)
        numberButtonList.add(num4Button)
        numberButtonList.add(num5Button)
        numberButtonList.add(num6Button)
        numberButtonList.add(num7Button)
        numberButtonList.add(num8Button)
        numberButtonList.add(num9Button)
    }


    fun generateResultKeys(numOfKey:Int){

        for(x in 0 ..numOfKey-1){
            var newResultKeyContainer = LinearLayout(context)
            newResultKeyContainer.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1f)
            newResultKeyContainer.orientation = LinearLayout.VERTICAL

            var textView = TextView(context)
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1f)
            textView.gravity = Gravity.CENTER
            textView.text = ""

            if(textTypeface != null){
                if(textStyle != null)
                    textView.setTypeface(textTypeface,textStyle!!)
                else
                    textView.setTypeface(textTypeface)
            }else{
                if(textStyle != null){
                    textView.setTypeface(null,textStyle!!)
                }
            }



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            }

            var view = View(context)
            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,2)
            view.setBackgroundColor(Color.parseColor("#000000"))

            resultKeyList.add(textView)
            resultKeyUnderLineList.add(view)

            newResultKeyContainer.addView(textView)
            newResultKeyContainer.addView(view)

            resultKeyContainer.addView(newResultKeyContainer)

            newResultKeyContainer.post{
                textView.post{
                    if(resultKeyTextSizeInPercentage != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        var width = textView.width
                        var percentageOfTextSize = (100 - resultKeyTextSizeInPercentage)/2
                        var padding = width * percentageOfTextSize /100

                        textView.updatePadding(padding,padding,padding,padding)
                        textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)

                    }else{
                        textView.updatePadding(0,0,0,0)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
                        }

                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,resultKeyTextSizeInSp)
                    }
                }
            }


            var space = Space(context)
            space.layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0.3f)
            resultKeyContainer.addView(space)
        }
    }

    fun initializeUI(){
        if(resultKeyContainerHeight != 0 ){
            resultKeyContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(resultKeyContainerHeight*context.resources.displayMetrics.density).toInt())
        }

        for(index in 0..numberButtonList.size-1){
            var btn = numberButtonList[index]

            if(textTypeface != null){
                if(textStyle != null)
                    btn.setTypeface(textTypeface,textStyle!!)
                else
                    btn.setTypeface(textTypeface)
            }else{
                if(textStyle != null){
                    btn.setTypeface(null,textStyle!!)
                }
            }

            btn.post{
                if(buttonTextSizeInPercentage !=0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    var width = btn.width
                    var percentageOfTextSize = (100 - buttonTextSizeInPercentage)/2
                    var padding = width * percentageOfTextSize /100

                    btn.updatePadding(padding,padding,padding,padding)
                }else{
                    btn.updatePadding(0,0,0,0)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        btn.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
                    }

                    btn.setTextSize(TypedValue.COMPLEX_UNIT_SP,buttonTextSizeInSp)
                }
                updateKeyReady(index)
            }

            btn.setTextColor(buttonTextColor)
            btn.background = getGradientRippleButtonOutlineDrawable(context,buttonBackgroundGradientStartColor,buttonBackgroundGradientEndColor,buttonPressedColor,buttonOutlineGradientStartColor,buttonOutlineGradientEndColor,buttonCornerRadius,buttonOutlineThickness)
            btn.setOnClickListener {
                handleButtonClick(index)
            }
        }


        btnOk.post {
            if(textTypeface != null){
                if(textStyle != null)
                    btnOk.setTypeface(textTypeface,textStyle!!)
                else
                    btnOk.setTypeface(textTypeface)
            }else{
                if(textStyle != null){
                    btnOk.setTypeface(null,textStyle!!)
                }
            }

            if(buttonTextSizeInPercentage != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                var width = btnOk.width
                var percentageOfTextSize = (100 - buttonTextSizeInPercentage)/2
                var padding = width * percentageOfTextSize /100
                btnOk.updatePadding(padding,padding,padding,padding)
            }else{
                btnOk.updatePadding(0,0,0,0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    btnOk.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
                }

                btnOk.setTextSize(TypedValue.COMPLEX_UNIT_SP,buttonTextSizeInSp)
            }

            updateKeyReady(BUTTON_OK)
        }

        btnOk.background = getGradientRippleButtonOutlineDrawable(context,buttonBackgroundGradientStartColor,buttonBackgroundGradientEndColor,buttonPressedColor,buttonOutlineGradientStartColor,buttonOutlineGradientEndColor,buttonCornerRadius,buttonOutlineThickness)
        btnOk.setTextColor(buttonTextColor)


        btnOk.setOnClickListener {
            handleButtonClick(BUTTON_OK)
        }

        btnDelete.post{
            var width = btnDelete.width
            var percentageOfTextSize = (100 - deleteButtonSizeInPercent)/2
            var padding = width * percentageOfTextSize /100
            btnDelete.updatePadding(padding,padding,padding,padding)

            updateKeyReady(BUTTON_DELETE)
        }

        btnDelete.background = getGradientRippleButtonOutlineDrawable(context,buttonBackgroundGradientStartColor,buttonBackgroundGradientEndColor,buttonPressedColor,buttonOutlineGradientStartColor,buttonOutlineGradientEndColor,buttonCornerRadius,buttonOutlineThickness)
        btnDelete.setOnClickListener {
            handleButtonClick(BUTTON_DELETE)
        }
    }

    private fun updateKeyReady(button:Int){
        isKeyReadyList[button] = true
        isAllKeyReady = isKeyReadyList.foldRight(true,{prev,curr-> prev&&curr})
        if(isAllKeyReady)
            whenAllNumpadUIReady()
    }

    fun setIsAllKeyReadyListener(callback:()->Unit){
        if(isAllKeyReady)
            callback()
        else
            whenAllNumpadUIReady = callback
    }

    fun getButtonReference(buttonInConstant: Int):View{
        return when(buttonInConstant){
            BUTTON_NUM_0-> numberButtonList[0]
            BUTTON_NUM_1-> numberButtonList[1]
            BUTTON_NUM_2-> numberButtonList[2]
            BUTTON_NUM_3-> numberButtonList[3]
            BUTTON_NUM_4-> numberButtonList[4]
            BUTTON_NUM_5-> numberButtonList[5]
            BUTTON_NUM_6-> numberButtonList[6]
            BUTTON_NUM_7-> numberButtonList[7]
            BUTTON_NUM_8-> numberButtonList[8]
            BUTTON_NUM_9-> numberButtonList[9]
            BUTTON_DELETE-> btnDelete
            BUTTON_OK-> btnOk

            else -> throw ButtonNotFoundException()
        }
    }

    private fun handleButtonClick(button:Int){
        if(button == BUTTON_DELETE){
            for(index in resultKeyList.size-1 downTo 0){
                var resultKey = resultKeyList[index]

                if(resultKey.text != ""){
                    resultKey.text = ""
                    break
                }
            }

            refreshResultKeyEnteredOrNotEnteredColor()

        }else if (button == BUTTON_OK){
            var resultKeyString = ""
            for(resultKey in resultKeyList){
                resultKeyString += resultKey.text.toString()
            }
            onOkClickListener(resultKeyString)
        }else{

            for(resultKey in resultKeyList){
                if(resultKey.text == ""){
                    resultKey.text = button.toString()
                    break
                }
            }

            refreshResultKeyEnteredOrNotEnteredColor()

            var isAllResultKeyEntered = resultKeyList.fold(true,{prevValue,textView-> prevValue && textView.text.toString() != ""})

            if(isAllResultKeyEntered){
                var resultKeyString = ""
                for(resultKey in resultKeyList){
                    resultKeyString += resultKey.text.toString()
                }
                onAllKeyEnteredListener(resultKeyString)
            }
        }

        onKeyPressedListener(button)
    }

    private fun refreshResultKeyEnteredOrNotEnteredColor(){
        for(index in 0..resultKeyList.size-1){
            if(resultKeyList[index].text == ""){
                resultKeyList[index].setTextColor(resultKeyNormalColor)
                resultKeyUnderLineList[index].setBackgroundColor(resultKeyNormalColor)
            }else{
                resultKeyList[index].setTextColor(resultKeyEnteredColor)
                resultKeyUnderLineList[index].setBackgroundColor(resultKeyEnteredColor)
            }
        }
    }

    fun setAllResultKeyHeight(heightInSp:Int){
        resultKeyContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(heightInSp*context.resources.displayMetrics.density).toInt())
    }

    fun setResultKeyColor(@ColorInt normalColor: Int,@ColorInt enteredColor:Int){
        resultKeyNormalColor = normalColor
        resultKeyEnteredColor = enteredColor
    }

    fun setDeleteButtonImageSizeInPercentage(sizePercentage:Int){
        var width = btnDelete.width
        var percentageOfTextSize = (100 - sizePercentage)/2
        var padding = width * percentageOfTextSize /100

        btnDelete.updatePadding(padding,padding,padding,padding)
    }

    fun setAllButtonTextSizeInSP(size:Float){
        numberButtonList.forEach{
            btn->

            btn.updatePadding(0,0,0,0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                btn.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
            }

            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP,size)
        }

        btnOk.updatePadding(0,0,0,0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            btnOk.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
        }

        btnOk.setTextSize(TypedValue.COMPLEX_UNIT_SP,size)
    }

    fun setSpecificButtonTextSizeInSP(button:Int,size:Float){
        var buttonRef = getButtonReference(button)

        if(buttonRef is TextView){
            buttonRef.updatePadding(0,0,0,0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buttonRef.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE)
            }

            buttonRef.setTextSize(TypedValue.COMPLEX_UNIT_SP,size)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun setAllButtonTextSizeInPercentage(sizePercentage:Int){
        buttonTextSizeInPercentage = sizePercentage
        numberButtonList.forEach{ btn->
            var width = btn.width
            var percentageOfTextSize = (100 - buttonTextSizeInPercentage)/2
            var padding = width * percentageOfTextSize /100

            btn.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)

            btn.updatePadding(padding,padding,padding,padding)
        }

        var width = btnOk.width
        var percentageOfTextSize = (100 - buttonTextSizeInPercentage)/2
        var padding = width * percentageOfTextSize /100

        btnOk.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)

        btnOk.updatePadding(padding,padding,padding,padding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setSpecificButtonTextSizeInPercentage(button:Int,sizePercentage:Int){
        var buttonRef = getButtonReference(button)

        if(buttonRef is TextView){
            var width = buttonRef.width
            var percentageOfTextSize = (100 - sizePercentage)/2
            var padding = width * percentageOfTextSize /100

            buttonRef.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)

            buttonRef.updatePadding(padding,padding,padding,padding)
        }
    }

    fun setAllButtonSize(width:Int,height:Int){
        numberButtonList.forEach{
                btn->
            var param = LinearLayout.LayoutParams(
                (width *context.resources.displayMetrics.density).toInt(),
                (height * context.resources.displayMetrics.density).toInt()
            )
            btn.layoutParams = param
        }
    }

    fun setSpecificButtonSize(button:Int,width:Int,height:Int){
        var buttonRef = getButtonReference(button)
        var param = LinearLayout.LayoutParams(
            (width *context.resources.displayMetrics.density).toInt(),
            (height * context.resources.displayMetrics.density).toInt()
        )
        buttonRef.layoutParams = param
    }

    fun setAllButtonTextColor(@ColorInt color:Int){
        numberButtonList.forEach{
                btn->
            btn.setTextColor(color)
        }
        btnOk.setTextColor(color)
    }

    fun setSpecificButtonTextColor(button:Int,@ColorInt color:Int){
        var buttonRef = getButtonReference(button)
        if(buttonRef is TextView){
            buttonRef.setTextColor(color)
        }
    }

    fun setAllButtonStyle(@ColorInt buttonBackgroundGradientStartColor:Int,@ColorInt buttonBackgroundGradientEndColor:Int,@ColorInt buttonPressedColor:Int,@ColorInt buttonOutlineGradientStartColor:Int, @ColorInt buttonOutlineGradientEndColor:Int,buttonCornerRadius:Float,buttonOutlineThickness:Int){
        numberButtonList.forEach{
            btn->
            btn.background = getGradientRippleButtonOutlineDrawable(context,buttonBackgroundGradientStartColor,buttonBackgroundGradientEndColor,buttonPressedColor,buttonOutlineGradientStartColor,buttonOutlineGradientEndColor,buttonCornerRadius,buttonOutlineThickness)
        }
        btnOk.background = getGradientRippleButtonOutlineDrawable(context,buttonBackgroundGradientStartColor,buttonBackgroundGradientEndColor,buttonPressedColor,buttonOutlineGradientStartColor,buttonOutlineGradientEndColor,buttonCornerRadius,buttonOutlineThickness)
        btnDelete.background = getGradientRippleButtonOutlineDrawable(context,buttonBackgroundGradientStartColor,buttonBackgroundGradientEndColor,buttonPressedColor,buttonOutlineGradientStartColor,buttonOutlineGradientEndColor,buttonCornerRadius,buttonOutlineThickness)
    }

    fun setSpecificButtonStyle(button:Int,@ColorInt buttonBackgroundGradientStartColor:Int,@ColorInt buttonBackgroundGradientEndColor:Int,@ColorInt buttonPressedColor:Int,@ColorInt buttonOutlineGradientStartColor:Int, @ColorInt buttonOutlineGradientEndColor:Int,buttonCornerRadius:Float,buttonOutlineThickness:Int){
        var buttonRef = getButtonReference(button)
        buttonRef.background = getGradientRippleButtonOutlineDrawable(context,buttonBackgroundGradientStartColor,buttonBackgroundGradientEndColor,buttonPressedColor,buttonOutlineGradientStartColor,buttonOutlineGradientEndColor,buttonCornerRadius,buttonOutlineThickness)
    }


    private fun getGradientRippleButtonOutlineDrawable(context : Context, @ColorInt gradientColor1: Int, gradientColor2 : Int, @ColorInt rippleColorWhenPress : Int, @ColorInt outlineStartColor : Int, @ColorInt outlineEndColor : Int, cornerRadiusValue : Float = 0f, outlineThicknessValue : Int = 3, orientation : GradientDrawable.Orientation = GradientDrawable.Orientation.BL_TR) : Drawable {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return RippleDrawable(
                ColorStateList.valueOf(rippleColorWhenPress),
                getGradientOutlineDrawable(gradientColor1, gradientColor2, outlineStartColor, outlineEndColor, GradientDrawable.RECTANGLE, outlineThicknessValue, orientation, cornerRadiusValue),
                getRippleMask(ContextCompat.getColor(context, R.color.white), cornerRadiusValue, GradientDrawable.RECTANGLE)
            )
        }
        else{
            return getStateListDrawableOutline(
                GradientDrawable.RECTANGLE,
                gradientColor1,
                rippleColorWhenPress,
                outlineStartColor,
                cornerRadiusValue,
                outlineThicknessValue
            )
        }
    }

    private fun getGradientOutlineDrawable(@ColorInt backgroundStartColor: Int, backgroundEndColor : Int, @ColorInt outlineStartColor: Int, @ColorInt outlineEndColor: Int, shape: Int, outlineThickness: Int, angle : GradientDrawable.Orientation, cornerRadius: Float) : Drawable{
        val backgroundDrawable = GradientDrawable(angle, intArrayOf(backgroundStartColor, backgroundEndColor))
        val outlineDrawable = GradientDrawable(angle, intArrayOf(outlineStartColor, outlineEndColor))

        when(shape){
            GradientDrawable.OVAL ->{
                outlineDrawable.shape = GradientDrawable.OVAL
                backgroundDrawable.shape = GradientDrawable.OVAL
            }
            else->{
                outlineDrawable.shape = GradientDrawable.RECTANGLE
                outlineDrawable.cornerRadius = cornerRadius
                backgroundDrawable.cornerRadius = cornerRadius - outlineThickness
            }
        }

        val layerDrawable = LayerDrawable(arrayOf(outlineDrawable, backgroundDrawable))

        if(outlineThickness >= 0) {
            layerDrawable.setLayerInset(
                1,
                outlineThickness,
                outlineThickness,
                outlineThickness,
                outlineThickness
            )
        }
        return layerDrawable
    }

    private fun getStateListDrawableOutline(shape : Int, @ColorInt normalColor: Int, @ColorInt pressedColor: Int, @ColorInt outlineColor: Int, cornerRadius : Float = 0f, outlineThickness: Int = 3, dashWidth: Int = 0, dashGap : Int = 0): StateListDrawable {
        val states = StateListDrawable()

        val pressedDrawable = GradientDrawable()
        val normalDrawable = GradientDrawable()

        pressedDrawable.setColor(pressedColor)
        normalDrawable.setColor(normalColor)

        if(shape == GradientDrawable.OVAL){
            pressedDrawable.shape = GradientDrawable.OVAL
            normalDrawable.shape = GradientDrawable.OVAL
        }
        else{
            pressedDrawable.shape = GradientDrawable.RECTANGLE
            normalDrawable.shape = GradientDrawable.RECTANGLE
            pressedDrawable.cornerRadius = cornerRadius
            normalDrawable.cornerRadius = cornerRadius
        }

        if(dashWidth <= 0 && dashGap <= 0) {
            pressedDrawable.setStroke(outlineThickness, outlineColor)
            normalDrawable.setStroke(outlineThickness, outlineColor)
        }
        else{
            pressedDrawable.setStroke(outlineThickness, outlineColor, dashWidth.toFloat(), dashGap.toFloat())
            normalDrawable.setStroke(outlineThickness, outlineColor, dashWidth.toFloat(), dashGap.toFloat())
        }

        states.addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
        states.addState(intArrayOf(android.R.attr.state_focused), pressedDrawable)
        states.addState(intArrayOf(android.R.attr.state_activated), pressedDrawable)
        states.addState(intArrayOf(), normalDrawable)
        return states
    }

    private fun getRippleMask(color: Int, cornerRadiusValue : Float = 0f, shapeType : Int = GradientDrawable.RECTANGLE): Drawable {

        val radius = FloatArray(8)
        Arrays.fill(radius, cornerRadiusValue)

        val shape = when(shapeType){
            GradientDrawable.RECTANGLE-> RoundRectShape(radius, null, null)
            GradientDrawable.OVAL -> OvalShape()
            else -> RoundRectShape(radius, null, null)
        }

        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = color
        return shapeDrawable
    }

    inner class ButtonNotFoundException : Exception(){
        override val message: String?
            get() = "Have you use the constant for button ref declared in CustomNumber class? For Example CustomNumpad.BUTTON_NUM_0"
    }


}