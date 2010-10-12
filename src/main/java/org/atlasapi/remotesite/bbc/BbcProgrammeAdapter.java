/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.bbc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;

public class BbcProgrammeAdapter implements SiteSpecificAdapter<Content> {

    static final Pattern SLASH_PROGRAMMES_URL_PATTERN = Pattern.compile("^http://www\\.bbc\\.co\\.uk/programmes/(b00[^/\\.]+)$");

    private final BbcSlashProgrammesEpisodeRdfClient episodeClient;
    private final ContentExtractor<BbcProgrammeSource, Item> itemExtractor;
    private final BbcBrandExtractor brandExtractor;
    
    private final BbcSlashProgrammesVersionRdfClient versionClient;

    private final Log oldLog = LogFactory.getLog(getClass());

    public BbcProgrammeAdapter(AdapterLog log) {
        this(new BbcSlashProgrammesEpisodeRdfClient(), new BbcSlashProgrammesVersionRdfClient(), new BbcProgrammeGraphExtractor(new SeriesFetchingBbcSeriesNumberResolver(),
                new BbcProgrammesPolicyClient()), log);
    }

    public BbcProgrammeAdapter(BbcSlashProgrammesEpisodeRdfClient episodeClient, BbcSlashProgrammesVersionRdfClient versionClient, ContentExtractor<BbcProgrammeSource, Item> propertyExtractor, AdapterLog log) {
        this.versionClient = versionClient;
        this.episodeClient = episodeClient;
        this.itemExtractor = propertyExtractor;
        this.brandExtractor = new BbcBrandExtractor(this, log);
    }

    public boolean canFetch(String uri) {
        Matcher matcher = SLASH_PROGRAMMES_URL_PATTERN.matcher(uri);
        return matcher.matches();
    }

    public Content fetch(String uri) {
        try {
            SlashProgrammesRdf content = readSlashProgrammesDataForEpisode(uri);
            if (content == null) {
                return null;
            }

            if (content.episode() != null) {
                SlashProgrammesVersionRdf version = null;
                if (content.episode().versions() != null && !content.episode().versions().isEmpty()) {
                    version = readSlashProgrammesDataForVersion(content.episode().versions().get(0));
                }
                BbcProgrammeSource source = new BbcProgrammeSource(uri, uri, content, version);
                return itemExtractor.extract(source);
            }
            if (content.brand() != null) {
                return brandExtractor.extract(content.brand());
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private SlashProgrammesVersionRdf readSlashProgrammesDataForVersion(SlashProgrammesVersion slashProgrammesVersion) {
        try {
            return versionClient.get(slashProgrammesUri(slashProgrammesVersion));
        } catch (Exception e) {
            oldLog.warn(e);
            return null;
        }
    }

    private SlashProgrammesRdf readSlashProgrammesDataForEpisode(String episodeUri) {
        try {
            return episodeClient.get(episodeUri + ".rdf");
        } catch (Exception e) {
            oldLog.warn(e);
            return null;
        }
    }

    private String slashProgrammesUri(SlashProgrammesVersion slashProgrammesVersion) {
        return "http://www.bbc.co.uk" + slashProgrammesVersion.resourceUri().replace("#programme", "") + ".rdf";
    }
}
