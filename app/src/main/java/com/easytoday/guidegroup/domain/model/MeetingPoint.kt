// app/src/main/java/com/easytoday/guidegroup/domain/model/MeetingPoint.kt
package com.easytoday.guidegroup.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

//import com.google.firebase.firestore.DocumentId // 

data class MeetingPoint(
    val groupId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    //val timestamp: Long = System.currentTimeMillis()
    @ServerTimestamp val timestamp: Date? = null // 
)

