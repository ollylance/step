// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class FindMeetingQuery {

    private enum attendeeType {
        MANDATORY, OPTIONAL, NONE
    }

    private static attendeeType attending(Set<String> eventAttendees, Collection<String> requestAttendees, 
            Collection<String> optionalRequestAttendees){
        for (String name : eventAttendees) {
            if (requestAttendees.contains(name)) {
                return attendeeType.MANDATORY;
            } else if (optionalRequestAttendees.contains(name)) {
                return attendeeType.OPTIONAL;
            }
        }
        return attendeeType.NONE;
    }

    private static ArrayList<TimeRange> addOptionalTimes(ArrayList<TimeRange> availableMandatoryTime,
        ArrayList<TimeRange> availableOptionalTime, long duration){
            ArrayList<TimeRange> newAvailableTime = new ArrayList<TimeRange>();
            for (TimeRange mandatoryTime: availableMandatoryTime) {
                for (TimeRange optionalTime : availableOptionalTime) {
                    if(mandatoryTime.contains(optionalTime) && optionalTime.duration() >= duration){
                        newAvailableTime.add(optionalTime);
                    } else if(optionalTime.contains(mandatoryTime) && mandatoryTime.duration() >= duration){
                        newAvailableTime.add(mandatoryTime);
                    }
                }   
            }
            if (!newAvailableTime.isEmpty()) {
                return newAvailableTime;
            } else { 
                return availableMandatoryTime; 
            }
    }

    private static ArrayList<TimeRange> getTimes(ArrayList<TimeRange> eventTimes, long duration){
        ArrayList<TimeRange> availableTimeRanges = new ArrayList<TimeRange>();
        TimeRange prevTimeRange = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY, false);
        TimeRange latestEvent = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY, true);
        if (eventTimes.isEmpty()) {
            availableTimeRanges.add(TimeRange.WHOLE_DAY);
            return availableTimeRanges;
        }
        
        for (int i = 0; i < eventTimes.size(); i++) {
            TimeRange t = eventTimes.get(i);
            if (i == 0) {
                boolean containsStart = (t.start() == 0);
                if (!containsStart) {
                    TimeRange temp = TimeRange.fromStartEnd(prevTimeRange.end(), t.start(), false);
                    if (temp.duration() >= duration) {
                        availableTimeRanges.add(temp);
                    }
                }
                if (latestEvent.end() < t.end()) {
                    latestEvent = t;
                }
                prevTimeRange = t;
            } else if (prevTimeRange.contains(t)) {
                if (latestEvent.end() < t.end()) {
                    latestEvent = t;
                }
            } else if (t.contains(prevTimeRange.end())) {
                prevTimeRange = t;
                if (latestEvent.end() < t.end()) {
                    latestEvent = t;
                }
            } else {
                TimeRange temp = TimeRange.fromStartEnd(prevTimeRange.end(), t.start(), false);
                if (temp.duration() >= duration) {
                    availableTimeRanges.add(temp);
                }
                if (latestEvent.end() < t.end()) {
                    latestEvent = t;
                }
                prevTimeRange = t;
            }
        }
        if (!latestEvent.contains(TimeRange.END_OF_DAY)) {
            TimeRange endTime = TimeRange.fromStartEnd(prevTimeRange.end(), TimeRange.END_OF_DAY, true);
            if(endTime.duration() >= duration) {
                availableTimeRanges.add(endTime);
            }
        }
        return availableTimeRanges;
    }


    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        ArrayList<TimeRange> availableMandatoryRanges = new ArrayList<TimeRange>();
        ArrayList<TimeRange> availableOptionalRanges = new ArrayList<TimeRange>();
        
        if (request.getDuration() > TimeRange.WHOLE_DAY.duration() || request.getDuration() < 0) {
            //if the time requested is longer than 24 hours or 0 minutes or less
            return availableMandatoryRanges;
        } else if (events.isEmpty() || (request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) 
                || request.getDuration() == 0) {
            //if there are no attendees or no events that day
            availableMandatoryRanges.add(TimeRange.WHOLE_DAY);
            return availableMandatoryRanges;
        } 
        ArrayList<TimeRange> mandatoryEventTimes = new ArrayList<TimeRange>();
        ArrayList<TimeRange> optionalEventTimes = new ArrayList<TimeRange>();
        Collection<String> mandatoryAttendees = request.getAttendees();
        Collection<String> optionalAttendees = request.getOptionalAttendees();
        for (Event e : events) {
            attendeeType type = attending(e.getAttendees(), mandatoryAttendees, 
                optionalAttendees);
            switch (type) {
                case MANDATORY:
                    mandatoryEventTimes.add(e.getWhen());
                    break;
                case OPTIONAL:
                    optionalEventTimes.add(e.getWhen());
                    break;
                case NONE:
                    break;
                default:
                    break;
            }
        }

        boolean mandatoryEmpty = mandatoryEventTimes.isEmpty();
        boolean optionalEmpty = optionalEventTimes.isEmpty();
        if (mandatoryEmpty && optionalEmpty) {
            availableMandatoryRanges.add(TimeRange.WHOLE_DAY);
            return availableMandatoryRanges;
        } else if (!mandatoryEmpty && optionalEmpty) {
            Collections.sort(mandatoryEventTimes, TimeRange.ORDER_BY_START);
            availableMandatoryRanges = getTimes(mandatoryEventTimes, request.getDuration());
            return availableMandatoryRanges;
        } else if (mandatoryEmpty && !optionalEmpty) {
            Collections.sort(optionalEventTimes, TimeRange.ORDER_BY_START);
            availableOptionalRanges = getTimes(optionalEventTimes, request.getDuration());
            return availableOptionalRanges; 
        } else {
            Collections.sort(mandatoryEventTimes, TimeRange.ORDER_BY_START);
            availableMandatoryRanges = getTimes(mandatoryEventTimes, request.getDuration());
            Collections.sort(optionalEventTimes, TimeRange.ORDER_BY_START);
            availableOptionalRanges = getTimes(optionalEventTimes, request.getDuration());
            return addOptionalTimes(availableMandatoryRanges, availableOptionalRanges, request.getDuration());
        }
        
    }
}
