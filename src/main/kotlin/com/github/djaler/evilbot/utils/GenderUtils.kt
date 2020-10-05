package com.github.djaler.evilbot.utils

import com.github.djaler.evilbot.enums.UserGender

fun UserGender.getFormByGender(male: String, female: String, it: String) =
        when(this){
            UserGender.MALE -> male
            UserGender.FEMALE -> female
            UserGender.IT -> it
        }
