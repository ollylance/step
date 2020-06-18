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

    public static String attending(Set<String> eventAttendees, Collection<String> requestAttendees, 
            Collection<String> optionalRequestAttendees){
        for (String name : eventAttendees) {
            if (requestAttendees.contains(name)) {
                return "mandatory";
            } else if (optionalRequestAttendees.contains(name)) {
                return "optional";
            }
        }
        return "none";
    }

    public static ArrayList<TimeRange> addOptionalTimes(ArrayList<TimeRange> availableMandatoryTime,
        ArrayList<TimeRange> availableOptionalTime, int duration){
            ArrayList<TimeRange> newAvailableTime = new ArrayList<TimeRange>();
            for (int i = 0; i < availableMandatoryTime.size(); i++) {
                TimeRange mandatoryTime = availableMandatoryTime.get(i);
                for (int j = 0; j < availableOptionalTime.size(); j++) {
                    TimeRange optionalTime = availableOptionalTime.get(j);
                    if(mandatoryTime.contains(optionalTime) && optionalTime.duration() >= duration){
                        newAvailableTime.add(optionalTime);
                    } else if(optionalTime.contains(mandatoryTime) && mandatoryTime.duration() >= duration){
                        newAvailableTime.add(mandatoryTime);
                    }
                }   
            }
            if (newAvailableTime.size() > 0) return newAvailableTime;
            else { return availableMandatoryTime; }
    }

    public static ArrayList<TimeRange> getTimes(ArrayList<TimeRange> eventTimes, int duration){
        ArrayList<TimeRange> availableTimeRanges = new ArrayList<TimeRange>();
        TimeRange prevTimeRange = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY, false);
        TimeRange latestEvent = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY, true);
        if(eventTimes.size() == 0){
            availableTimeRanges.add(TimeRange.WHOLE_DAY);
            return availableTimeRanges;
        }
        
        for (int i = 0; i < eventTimes.size(); i++) {
            TimeRange t = eventTimes.get(i);
            if (i == 0) {
                boolean containsStart = (t.start() == 0);
                if(!containsStart){
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
            if(endTime.duration() >= duration) availableTimeRanges.add(endTime);
        }
        return availableTimeRanges;
    }


    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        ArrayList<TimeRange> availableMandatoryRanges = new ArrayList<TimeRange>();
        ArrayList<TimeRange> availableOptionalRanges = new ArrayList<TimeRange>();
        
        if (request.getDuration() > TimeRange.WHOLE_DAY.duration() || request.getDuration() < 0) {
            //if the time requested is longer than 24 hours or 0 minutes or less
            return availableMandatoryRanges;
        } else if (events.equals(Collections.emptySet()) || (request.getAttendees().size() == 0 && request.getOptionalAttendees().size() == 0) 
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
            String type = attending(e.getAttendees(), mandatoryAttendees, 
                optionalAttendees);
            switch (type) {
                case "mandatory":
                    mandatoryEventTimes.add(e.getWhen());
                    break;
                case "optional":
                    optionalEventTimes.add(e.getWhen());
                    break;
                case "none":
                    break;
                default:
                    break;
            }
        }

        int mandatorySize = mandatoryEventTimes.size();
        int optionalSize = optionalEventTimes.size();
        if(mandatorySize == 0 && optionalSize == 0) {
            availableMandatoryRanges.add(TimeRange.WHOLE_DAY);
            return availableMandatoryRanges;
        } else if (mandatorySize != 0 && optionalSize == 0) {
            Collections.sort(mandatoryEventTimes, TimeRange.ORDER_BY_START);
            availableMandatoryRanges = getTimes(mandatoryEventTimes, (int) request.getDuration());
            return availableMandatoryRanges;
        } else if (mandatorySize == 0 && optionalSize != 0) {
            Collections.sort(optionalEventTimes, TimeRange.ORDER_BY_START);
            availableOptionalRanges = getTimes(optionalEventTimes, (int) request.getDuration());
            return availableOptionalRanges; 
        } else {
            Collections.sort(mandatoryEventTimes, TimeRange.ORDER_BY_START);
            availableMandatoryRanges = getTimes(mandatoryEventTimes, (int) request.getDuration());
            Collections.sort(optionalEventTimes, TimeRange.ORDER_BY_START);
            availableOptionalRanges = getTimes(optionalEventTimes, (int) request.getDuration());
            return addOptionalTimes(availableMandatoryRanges, availableOptionalRanges, (int) request.getDuration());
        }
        
    }
}
