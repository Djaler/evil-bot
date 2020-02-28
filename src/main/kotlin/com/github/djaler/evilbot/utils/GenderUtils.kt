package com.github.djaler.evilbot.utils

import com.github.djaler.evilbot.entity.User

fun User.getFormByGender(male: String, female: String) = if (this.male) male else female
