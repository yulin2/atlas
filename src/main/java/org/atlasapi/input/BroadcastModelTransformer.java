package org.atlasapi.input;

import org.atlasapi.media.entity.Broadcast;
import org.joda.time.DateTime;


public class BroadcastModelTransformer {

    public Broadcast transform(org.atlasapi.media.entity.simple.Broadcast simple) {
        Broadcast complex = new Broadcast(simple.getBroadcastOn(), 
                new DateTime(simple.getTransmissionTime()), 
                new DateTime(simple.getTransmissionEndTime()))
                .withId(simple.getId());
          complex.setActualTransmissionTime(simple.getActualTransmissionTime());
          complex.setActualTransmissionEndTime(simple.getActualTransmissionEndTime());
          complex.setScheduleDate(simple.getScheduleDate());
          complex.setRepeat(simple.getRepeat());
          complex.setSubtitled(simple.getSubtitled());
          complex.setSigned(simple.getSigned());
          complex.setAudioDescribed(simple.getAudioDescribed());
          complex.setHighDefinition(simple.getHighDefinition());
          complex.setWidescreen(simple.getWidescreen());
          complex.setSurround(simple.getSurround());
          complex.setLive(simple.getLive());
          // TODO ALIASES
        return complex;
    }

}
