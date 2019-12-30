/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotx.features.home.room.detail.timeline.helper

import im.vector.matrix.android.api.session.events.model.EventType
import im.vector.matrix.android.api.session.room.timeline.TimelineEvent
import im.vector.riotx.core.extensions.localDateTime

object TimelineDisplayableEvents {

    val DISPLAYABLE_TYPES = listOf(
            EventType.MESSAGE,
            EventType.STATE_ROOM_NAME,
            EventType.STATE_ROOM_TOPIC,
            EventType.STATE_ROOM_MEMBER,
            EventType.STATE_ROOM_ALIASES,
            EventType.STATE_ROOM_CANONICAL_ALIAS,
            EventType.STATE_ROOM_HISTORY_VISIBILITY,
            EventType.CALL_INVITE,
            EventType.CALL_HANGUP,
            EventType.CALL_ANSWER,
            EventType.ENCRYPTED,
            EventType.ENCRYPTION,
            EventType.STATE_ROOM_THIRD_PARTY_INVITE,
            EventType.STICKER,
            EventType.STATE_ROOM_CREATE,
            EventType.STATE_ROOM_TOMBSTONE,
            EventType.STATE_ROOM_JOIN_RULES,
            EventType.KEY_VERIFICATION_DONE,
            EventType.KEY_VERIFICATION_CANCEL
    )

    val DEBUG_DISPLAYABLE_TYPES = DISPLAYABLE_TYPES + listOf(
            EventType.REDACTION,
            EventType.REACTION,
            EventType.KEY_VERIFICATION_ACCEPT,
            EventType.KEY_VERIFICATION_START,
            EventType.KEY_VERIFICATION_MAC,
            EventType.KEY_VERIFICATION_KEY,
            EventType.KEY_VERIFICATION_READY
    )
}

fun TimelineEvent.canBeMerged(): Boolean {
    return root.getClearType() == EventType.STATE_ROOM_MEMBER
}

fun List<TimelineEvent>.nextSameTypeEvents(index: Int, minSize: Int): List<TimelineEvent> {
    if (index >= size - 1) {
        return emptyList()
    }
    val timelineEvent = this[index]
    val nextSubList = subList(index + 1, size)
    val indexOfNextDay = nextSubList.indexOfFirst {
        val date = it.root.localDateTime()
        val nextDate = timelineEvent.root.localDateTime()
        date.toLocalDate() != nextDate.toLocalDate()
    }
    val nextSameDayEvents = if (indexOfNextDay == -1) {
        nextSubList
    } else {
        nextSubList.subList(0, indexOfNextDay)
    }
    val indexOfFirstDifferentEventType = nextSameDayEvents.indexOfFirst { it.root.getClearType() != timelineEvent.root.getClearType() }
    val sameTypeEvents = if (indexOfFirstDifferentEventType == -1) {
        nextSameDayEvents
    } else {
        nextSameDayEvents.subList(0, indexOfFirstDifferentEventType)
    }
    if (sameTypeEvents.size < minSize) {
        return emptyList()
    }
    return sameTypeEvents
}

fun List<TimelineEvent>.prevSameTypeEvents(index: Int, minSize: Int): List<TimelineEvent> {
    val prevSub = subList(0, index + 1)
    return prevSub
            .reversed()
            .nextSameTypeEvents(0, minSize)
            .reversed()
}

fun List<TimelineEvent>.nextOrNull(index: Int): TimelineEvent? {
    return if (index >= size - 1) {
        null
    } else {
        subList(index + 1, this.size).firstOrNull()
    }
}
