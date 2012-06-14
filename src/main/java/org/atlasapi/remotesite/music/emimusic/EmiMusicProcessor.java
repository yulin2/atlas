package org.atlasapi.remotesite.music.emimusic;

import com.metabroadcast.common.base.Maybe;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Song;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.s3.S3Client;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 */
public class EmiMusicProcessor {

    private static final String SONG_URI_PREFIX = "http://emimusic.com/recordings/";
    private static final String GENRES_URI_PREFIX = "http://emimusic.com/genres/";
    private static final String CURIE_PREFIX = "emimusic:";
    private static final PeriodFormatterBuilder periodBuilder = new PeriodFormatterBuilder().appendLiteral("PT").appendHours().appendLiteral("H").appendMinutes().appendLiteral("M").appendSeconds().appendLiteral("S");

    public void process(S3Client client, ContentWriter contentWriter, AdapterLog log) throws Exception {
        Iterable<String> files = client.list(null, "xml");
        for (String file : files) {
            try {
                Document doc = loadDocument(client, file);
                Nodes songs = doc.query("//ResourceList/SoundRecording");
                for (int i = 0; i < songs.size(); i++) {
                    String uri = null;
                    try {
                        Node node = songs.get(i);
                        uri = getSingleNodeValue(node, "SoundRecordingId/ProprietaryId").replaceFirst("DTI:", "");
                        //
                        String isrc = getSingleNodeValue(node, "SoundRecordingId/ISRC");
                        String title = getSingleNodeValue(node, "SoundRecordingDetailsByTerritory/Title[@TitleType='DisplayTitle']/TitleText");
                        String year = getSingleNodeValue(node, "SoundRecordingDetailsByTerritory/PLine/Year");
                        String duration = getSingleNodeValue(node, "Duration");
                        String genre = getSingleNodeValue(node, "SoundRecordingDetailsByTerritory/Genre/GenreText");
                        //
                        Song song = new Song(SONG_URI_PREFIX + uri, CURIE_PREFIX + uri, Publisher.EMI_MUSIC);
                        song.setIsrc(isrc);
                        song.setTitle(title);
                        song.setDuration(!duration.isEmpty() ? periodBuilder.toFormatter().parsePeriod(duration).toStandardDuration() : null);
                        song.setGenres(!genre.isEmpty() ? Arrays.asList(GENRES_URI_PREFIX + genre) : Collections.EMPTY_LIST);
                        //
                        Nodes artists = node.query("SoundRecordingDetailsByTerritory/DisplayArtist");
                        for (int j = 0; j < artists.size(); j++) {
                            Element artist = (Element) artists.get(j);
                            if (artist.getAttribute("LanguageAndScriptCode") == null || artist.getAttribute("LanguageAndScriptCode").getValue().equals("en")) {
                                String name = getSingleNodeValue(artist, "PartyName/FullName");
                                String role = getSingleNodeValue(artist, "ArtistRole").equals("MainArtist") ? "artist" : "contributor";
                                song.addPerson(new CrewMember().withName(name).withRole(CrewMember.Role.fromKey(role)));
                            }
                        }
                        Nodes contributors = node.query("SoundRecordingDetailsByTerritory/ResourceContributor");
                        for (int j = 0; j < contributors.size(); j++) {
                            Element contributor = (Element) contributors.get(j);
                            if (contributor.getAttribute("LanguageAndScriptCode") == null || contributor.getAttribute("LanguageAndScriptCode").getValue().equals("en")) {
                                String name = getSingleNodeValue(contributor, "PartyName/FullName");
                                String role = getSingleNodeValue(contributor, "ResourceContributorRole").equals("MainArtist") ? "artist" : "contributor";
                                song.addPerson(new CrewMember().withName(name).withRole(CrewMember.Role.fromKey(role)));
                            }
                        }
                        contentWriter.createOrUpdate(song);
                    } catch (Exception ex) {
                        log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withDescription("Failed writing song: " + uri).withSource(getClass()));
                    }
                }
            } catch (Exception ex) {
                log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withDescription("Failed reading file: " + file).withSource(getClass()));
            }
        }
    }

    private Document loadDocument(S3Client client, String name) throws IOException, ParsingException {
        File file = File.createTempFile(EmiMusicUpdater.S3_BUCKET, name);
        client.getAndSaveIfUpdated(name, file, Maybe.<File>nothing());
        return new Builder().build(file);
    }

    private String getSingleNodeValue(Node source, String path) {
        Nodes nodes = source.query(path);
        if (nodes.size() == 1) {
            return nodes.get(0).getValue();
        } else {
            return "";
        }
    }
}
