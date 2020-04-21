package com.oymj.greenearthhero.utils

import java.lang.Exception
import java.util.regex.Pattern

object FormUtils {

    fun isNull(name:String,input:String): String?{
        var error:String? = null
        if(input.isNullOrEmpty()){
            error = "$name cannot be empty!"
            return error
        }else{
            return ""
        }
    }

    fun isLengthBetween(name:String,input:String,minLength:Int,maxLength:Int): String?{
        var error:String? = null
        if(input.length < minLength || input.length > maxLength){
            error = "$name must have character amount between $minLength and $maxLength!"
            return error
        }else{
            return ""
        }
    }

    fun isOnlyAlphabet(name:String,input:String): String?{
        var error:String? = null

        var onlyAlphabetRegex = Regex("^[A-Za-z ]+\$")
        if(!onlyAlphabetRegex.matches(input)){
            error = "$name can only contain alphabet character"
            return error
        }else{
            return ""
        }
    }

    fun isOnlyNumber(name:String,input:String): String?{
        var error:String? = null

        var onlyNumberRegex = Regex("^[0-9]*\$")
        if(!onlyNumberRegex.matches(input)){
            error = "$name can only contain number"
            return error
        }else{
            return ""
        }
    }

    fun isEmail(input:String): String?{
        var error:String? = null

        if(!(input.contains("@") && input.contains("."))){
            error = "Email is not valid"
            return error
        }else{
            return ""
        }
    }

    fun isMatchRegex(input:String, regex:String, errorMessage:String): String?{
        return if(!Pattern.compile(regex).matcher(input).matches()){
            errorMessage
        }else{
            ""
        }
    }

    fun isNumberBetween(name:String,input:String,min:Int?,max:Int?): String?{
        var error:String? = null

        if(min ==null && max == null){
            throw FormUtilIsNumberBetweenMustHaveMinOrMaxException()
        }
        try{
            var number = input.toInt()

            var smallerThanMin=false
            var largerThanMax=false

            if(min != null && number < min){
                smallerThanMin = true
            }

            if(max != null && number > max){
                largerThanMax = true
            }

            if(smallerThanMin || largerThanMax){
                if(min!=null && max!=null){
                    error= "$name must be a number between $min and $max!"
                }else if(min!=null && max==null){
                    error= "$name must be more than $min!"
                }else if(min==null && max!=null){
                    error= "$name must be less than $max!"
                }
            }else{
                error= ""
            }
        }catch (e:Exception){
            error= "$name must be a number!"
        }
        return error
    }

}

class FormUtilIsNumberBetweenMustHaveMinOrMaxException:Exception()