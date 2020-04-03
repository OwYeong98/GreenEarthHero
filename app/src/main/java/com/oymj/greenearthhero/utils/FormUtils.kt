package com.oymj.greenearthhero.utils

import java.util.regex.Pattern

object FormUtils {

    fun isNull(name:String,input:String): String?{
        var error:String? = null
        if(input.isNullOrEmpty()){
            error = "$name cannot be empty!"
            return error
        }else{
            return null
        }
    }

    fun isLengthBetween(name:String,input:String,minLength:Int,maxLength:Int): String?{
        var error:String? = null
        if(input.length < minLength || input.length > maxLength){
            error = "$name must have character amount between $minLength and $maxLength!"
            return error
        }else{
            return null
        }
    }

    fun isOnlyAlphabet(name:String,input:String): String?{
        var error:String? = null

        var onlyAlphabetRegex = Regex("/^[A-Za-z]+\$/")
        if(!onlyAlphabetRegex.matches(input)){
            error = "$name can only contain alphabet character"
            return error
        }else{
            return null
        }
    }

    fun isOnlyNumber(name:String,input:String): String?{
        var error:String? = null

        var onlyNumberRegex = Regex("/^[0-9]+\$/")
        if(!onlyNumberRegex.matches(input)){
            error = "$name can only contain number"
            return error
        }else{
            return null
        }
    }

    fun isEmail(input:String): String?{
        var error:String? = null

        if(!(input.contains("@") && input.contains("."))){
            error = "Email is not valid"
            return error
        }else{
            return null
        }
    }

    fun isMatchRegex(input:String, regex:String, errorMessage:String): String?{
        return if(!Pattern.compile(regex).matcher(input).matches()){
            errorMessage
        }else{
            null
        }
    }



}