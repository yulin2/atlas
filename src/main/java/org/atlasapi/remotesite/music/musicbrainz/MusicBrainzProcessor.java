package org.atlasapi.remotesite.music.musicbrainz;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.s3.S3Client;

/**
 */
public class MusicBrainzProcessor {

    private static final String S3_BUCKET = "music-brainz";
    //
    static final String RECORDINGS = "recording";
    static final String ISRCS = "isrc";
    static final String TRACKS = "track_name";
    static final String ARTIST_RECORDING = "l_artist_recording";
    static final String ARTISTS = "artist";
    static final String NAMES = "artist_name";

    public void process(S3Client client, ContentWriter contentWriter, ItemsPeopleWriter peopleWriter) throws Exception {
        BufferedReader recordingsFile = getReader(client, RECORDINGS);
        RandomAccessFile isrcsFile = getRaf(client, ISRCS);
        RandomAccessFile tracksFile = getRaf(client, TRACKS);
        RandomAccessFile artistRecordingFile = getRaf(client, ARTIST_RECORDING);
        RandomAccessFile artistsFile = getRaf(client, ARTISTS);
        RandomAccessFile namesFile = getRaf(client, NAMES);
        Map<String, Long> isrcsIndex = buildIndex(isrcsFile, 1);
        Map<String, Long> tracksIndex = buildIndex(tracksFile, 0);
        Map<String, Long> artistRecordingIndex = buildIndex(artistRecordingFile, 3);
        Map<String, Long> artistsIndex = buildIndex(artistsFile, 0);
        Map<String, Long> namesIndex = buildIndex(namesFile, 0);

        String currentRecording = recordingsFile.readLine();
        while (currentRecording != null) {
            Iterable<String> currentRecordingData = Splitter.on(" ").split(currentRecording);
            String recordUri = Iterables.get(currentRecordingData, 1);
            Long duration = TimeUnit.SECONDS.convert(Long.parseLong(Iterables.get(currentRecordingData, 4)), TimeUnit.MILLISECONDS);
            //
            String currentIsrc = readData(isrcsFile, isrcsIndex, Iterables.get(currentRecordingData, 0));
            Iterable<String> currentIsrcData = Splitter.on(" ").split(currentIsrc);
            String isrc = Iterables.get(currentIsrcData, 2);
            //
            String currentTrack = readData(tracksFile, tracksIndex, Iterables.get(currentRecordingData, 2));
            Iterable<String> currentTrackData = Splitter.on(" ").split(currentTrack);
            String title = Iterables.get(currentTrackData, 1);
            //
            String currentArtistRecording = readData(artistRecordingFile, artistRecordingIndex, Iterables.get(currentRecordingData, 3));
            Iterable<String> currentArtistRecordingData = Splitter.on(" ").split(currentArtistRecording);
            String currentArtist = readData(artistsFile, artistsIndex, Iterables.get(currentArtistRecordingData, 2));
            Iterable<String> currentArtistData = Splitter.on(" ").split(currentArtist);
            String artistUri = Iterables.get(currentArtistData, 1);
            String currentName = readData(namesFile, namesIndex, Iterables.get(currentArtistData, 2));
            Iterable<String> currentNameData = Splitter.on(" ").split(currentName);
            String artistName = Iterables.get(currentNameData, 1);

            //..

            currentRecording = recordingsFile.readLine();
        }
    }

    private BufferedReader getReader(S3Client client, String fileName) throws IOException {
        File localFile = File.createTempFile(S3_BUCKET, fileName);
        client.getAndSaveIfUpdated(fileName, localFile, Maybe.<File>nothing());

        BufferedReader reader = new BufferedReader(new FileReader(localFile));
        return reader;
    }

    private RandomAccessFile getRaf(S3Client client, String fileName) throws IOException {
        File localFile = File.createTempFile(S3_BUCKET, fileName);
        client.getAndSaveIfUpdated(fileName, localFile, Maybe.<File>nothing());

        RandomAccessFile reader = new RandomAccessFile(localFile, "r");
        return reader;
    }

    private Map<String, Long> buildIndex(RandomAccessFile raf, int pos) throws IOException {
        Map<String, Long> index = new HashMap<String, Long>();
        Long pointer = raf.getFilePointer();
        String line = raf.readLine();
        while (line != null) {
            Iterable<String> data = Splitter.on(" ").split(line);
            String key = Iterables.get(data, pos);
            index.put(key, pointer);
            pointer = raf.getFilePointer();
            line = raf.readLine();
        }
        raf.seek(0);
        return index;
    }

    private String readData(RandomAccessFile file, Map<String, Long> index, String key) throws IOException {
        Long pos = index.get(key);
        file.seek(pos);
        return file.readLine();
    }
}
