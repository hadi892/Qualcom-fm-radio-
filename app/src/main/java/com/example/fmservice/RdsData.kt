package com.example.fmservice

data class RdsData(
    val psName: String = "",
    val radioText: String = "",
    val programType: Int = 0,
    val rssi: Int = -70,
    val isStereo: Boolean = true
) {
    fun getProgramTypeName(): String {
        return when (programType) {
            1 -> "News"
            2 -> "Current Affairs"
            3 -> "Information"
            4 -> "Sports"
            5 -> "Education"
            6 -> "Drama"
            7 -> "Culture"
            8 -> "Science"
            9 -> "Varied"
            10 -> "Pop Music"
            11 -> "Rock Music"
            12 -> "Easy Listening"
            13 -> "Light Classics"
            14 -> "Serious Classics"
            15 -> "Other Music"
            else -> "General / Music"
        }
    }
}
