package com.ghyinc.finance.domain.notification.dto

import com.ghyinc.finance.domain.notification.enums.ChannelType
import com.ghyinc.finance.domain.notification.enums.SendType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "알림 발송 등록(요청)")
data class NotificationSendRequest(
    @field:Schema(description = "고객Id", example = "1")
    val userId: Long ? = null,

    @field:Schema(description = "채널 타입", example = "SMS")
    val channelType: ChannelType,
    
    @field:Schema(description = "발송 타입", example = "IMMEDIATE")
    val sendType: SendType,
    
    @field:Schema(description = "수신자", example = "윤교희")
    val recipient: String? = null,
    
    @field:Schema(description = "제목", example = "제목1")
    val title: String? = null,
    
    @field:Schema(description = "내용", example = "내용1")
    val content: String? = null,
) {}
