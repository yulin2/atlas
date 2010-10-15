package org.atlasapi.remotesite.bbc.atoz;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;

import com.google.common.collect.ImmutableList;

public class BbcSlashProgrammesAtoZUpdater implements Runnable {
	
    private static final String ATOZ_BASE = "http://www.bbc.co.uk/%s/programmes/a-z/all.rdf";
    private static final String SLASH_PROGRAMMES_BASE_URI = "http://www.bbc.co.uk/programmes/";

    private final RemoteSiteClient<SlashProgrammesAtoZRdf> client;
    private final BbcProgrammeAdapter fetcher;
    private final List<String> channels = ImmutableList.of("bbcone", "bbctwo", "bbcthree", "bbcfour", "bbchd", "radio1", "radio2", "radio3", "radio4");
	private final ContentWriter writer;
	
	private final AdapterLog log;
    
    public BbcSlashProgrammesAtoZUpdater(ContentWriter writer, AdapterLog log) {
        this(new BbcSlashProgrammesAtoZRdfClient(), new BbcProgrammeAdapter(log), writer, log);
    }

    public BbcSlashProgrammesAtoZUpdater(RemoteSiteClient<SlashProgrammesAtoZRdf> client, BbcProgrammeAdapter fetcher, ContentWriter writer, AdapterLog log) {
        this.client = client;
        this.fetcher = fetcher;
		this.writer = writer;
		this.log = log;
    }

    @Override
    public void run() {
    	ExecutorService executor = Executors.newFixedThreadPool(7);
        for (String channel : channels) {
            String uri = String.format(ATOZ_BASE, channel);
            try {
                SlashProgrammesAtoZRdf atoz = client.get(uri);
                for (final String pid : atoz.programmeIds()) {
                    executor.execute(new Runnable() {
						@Override
						public void run() {
							loadAndSave(pid);							
						}
                    });
                }
            } catch (Exception e) {
            	log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withUri(uri).withDescription("Failed to load BBC atoz page: " + uri).withSource(getClass()));
            }
        }
        executor.shutdown();
    }

	private void loadAndSave(String pid) {
		String uri = SLASH_PROGRAMMES_BASE_URI + pid;
		try {
			Content content = fetcher.fetch(uri);
		    if (content != null) {
		    	if (content instanceof Item) {
		    		writer.createOrUpdateItem((Item) content);
		    	} else if (content instanceof Playlist) {
		        	writer.createOrUpdatePlaylist((Playlist) content, true);
		    	} else {
		            throw new IllegalArgumentException("Could not persist content (unknown type): " + content);
		    	}
		    }
		} catch (Exception e) {
        	log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withUri(uri).withSource(getClass()));
		}
	}
}
