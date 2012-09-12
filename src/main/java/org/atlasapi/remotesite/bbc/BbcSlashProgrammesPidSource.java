package org.atlasapi.remotesite.bbc;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.atoz.SlashProgrammesAtoZRdf;

import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;

public class BbcSlashProgrammesPidSource {
    
    private static final String ATOZ_BASE = "http://www.bbc.co.uk/%s/programmes/a-z/all.rdf";

    private RemoteSiteClient<SlashProgrammesAtoZRdf> client;

    private List<String> channels;
    
    public BbcSlashProgrammesPidSource(RemoteSiteClient<SlashProgrammesAtoZRdf> client, List<String> channels) {
        this.client = client;
        this.channels = channels;
    }
    
    public Iterable<ChannelAndPid> listPids(final ChannelAndPid restoredProgress) {
        return new Iterable<ChannelAndPid>() {
            @Override
            public Iterator<ChannelAndPid> iterator() {
                return new PidIterator(restoredProgress);
            }
        };
    }
    
    private class PidIterator extends AbstractIterator<ChannelAndPid> {

        private final Iterator<String> remainingChannels;
        private final String lastPid;

        private Iterator<String> remainingPids = Iterators.emptyIterator();
        private boolean usedPid = false;
        private String curChan;
        
        public PidIterator(ChannelAndPid restoredProgress) {
            this.remainingChannels = remainingChannels(restoredProgress);
            this.lastPid = lastCompletedPid(restoredProgress);
        }

        @Override
        protected ChannelAndPid computeNext() {
            while (!remainingPids.hasNext()) {
                if (!remainingChannels.hasNext()) {
                    return endOfData();
                }
                curChan = remainingChannels.next();
                remainingPids = fetchBatch(curChan, lastPid);
            }
            return new ChannelAndPid(curChan, remainingPids.next());
        }
        
        private Iterator<String> fetchBatch(String channel, String lastPid) {
            final String channelAzUri = String.format(ATOZ_BASE, channel);
            try {
                SlashProgrammesAtoZRdf atoz = client.get(channelAzUri);
                Iterator<String> pids = sortedPids(atoz).iterator();
                if (!usedPid) {
                    usedPid = true;
                    fastForward(pids, lastPid);
                }
                return pids;
            } catch (Throwable e) {
                throw Throwables.propagate(e);
            }
        }

        private ImmutableList<String> sortedPids(SlashProgrammesAtoZRdf atoz) {
            return Ordering.natural().immutableSortedCopy(atoz.programmeIds());
        }

        private void fastForward(Iterator<String> pids, String lastPid) {
            if (lastPid != null) {
                while(pids.hasNext() && lastPid.compareTo(pids.next()) > 0);
            }
        }

        private String lastCompletedPid(ChannelAndPid progress) {
            return progress == null ? null : progress.pid();
        }
        
        private Iterator<String> remainingChannels(ChannelAndPid progress) {
            int start = progress == null ? 0 : channels.indexOf(progress.channel());
            return channels.subList(start, channels.size()).iterator();
        }

    };


}
