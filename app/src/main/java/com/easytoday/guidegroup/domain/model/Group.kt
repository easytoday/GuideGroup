// app/src/main/java/com/easytoday/guidegroup/domain/model/Group.kt
package com.easytoday.guidegroup.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Group(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val creatorId: String = "",
    val memberIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null,
    val lastMessageTimestamp: Date? = null
)

