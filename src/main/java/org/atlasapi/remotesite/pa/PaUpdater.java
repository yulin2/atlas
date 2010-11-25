package org.atlasapi.remotesite.pa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.atlasapi.genres.GenreMap;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.pa.bindings.Billing;
import org.atlasapi.remotesite.pa.bindings.Category;
import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.xml.sax.XMLReader;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

public class PaUpdater implements Runnable {

    private static final String PA_BASE_URL = "http://pressassociation.com";
    private static final FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith("_tvdata.xml");
        }
    };

    private static FTPFileFilter ftpFilenameFilter = new FTPFileFilter() {
        @Override
        public boolean accept(FTPFile file) {
            return file.isFile() && !file.getName().endsWith(".md5");
        }
    };

    private final PaChannelMap channelMap = new PaChannelMap();
    private final GenreMap genreMap = new PaGenreMap();
    private final ContentWriters contentWriter;
    private final Set<String> writtenBrands = Sets.newHashSet();
    private final AdapterLog log;
    private final File localFolder;
    private boolean isRunning = false;
    private final ContentResolver contentResolver;
    private final String ftpHost;
    private final String ftpFilesPath;
    private final String ftpUsername;
    private final String ftpPassword;

    public PaUpdater(ContentWriters contentWriter, ContentResolver contentResolver, String ftpHost, String ftpUsername, String ftpPassword, String ftpFilesPath, String localFilesPath,
            AdapterLog log) {
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        Preconditions.checkNotNull(contentWriter);
        Preconditions.checkNotNull(contentResolver);
        Preconditions.checkNotNull(log);
        Preconditions.checkNotNull(localFilesPath);

        this.contentWriter = contentWriter;
        this.contentResolver = contentResolver;
        this.ftpHost = ftpHost;
        this.ftpFilesPath = ftpFilesPath;
        this.log = log;
        this.localFolder = new File(localFilesPath);
        if (!localFolder.exists()) {
            localFolder.mkdir();
        }
        if (!localFolder.isDirectory()) {
            throw new IllegalArgumentException("Files path is not a directory: " + localFilesPath);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void updateFilesFromFtpSite() throws IOException {
        if (Strings.isNullOrEmpty(ftpHost) || Strings.isNullOrEmpty(ftpUsername) || Strings.isNullOrEmpty(ftpPassword) || Strings.isNullOrEmpty(ftpFilesPath)) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(PaUpdater.class).withDescription("FTP details incomplete / missing, skipping FTP update"));
            return;
        }

        FTPClient client = new FTPClient();
        try {
            client.connect(ftpHost);
            client.enterLocalPassiveMode();
            if (! client.login(ftpUsername, ftpPassword)) {
                throw new Exception("Unable to connect to "+ftpHost+" with username: "+ftpUsername+" and password...");
            }
            if (! client.changeWorkingDirectory(ftpFilesPath)) {
                throw new Exception("Unable to change working directory to "+ftpFilesPath);
            }
            FTPFile[] listFiles = client.listFiles(ftpFilesPath, ftpFilenameFilter);

            for (FTPFile file : listFiles) {
                FileOutputStream fos = null;
                try {
                    String name = file.getName();

                    Maybe<File> existingFile = findExistingFile(name);
                    File fileToWrite;
                    if (existingFile.hasValue()) {
                        Date date = new Date(existingFile.requireValue().lastModified());

                        if (file.getSize() == existingFile.requireValue().length() && file.getTimestamp().getTime().before(date)) {
                            continue;
                        }

                        fileToWrite = existingFile.requireValue();
                    } else {
                        fileToWrite = new File(localFolder, name);
                    }

                    fos = new FileOutputStream(fileToWrite);
                    client.retrieveFile(name, fos);
                    fos.close();
                } catch (Exception e) {
                    if (fos != null) {
                        fos.close();
                    }
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(PaUpdater.class).withCause(e).withDescription("Error copying file " + file.getName()));
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaUpdater.class).withDescription("Error when trying to copy files from FTP"));
        } finally {
            client.disconnect();
        }
    }

    private Maybe<File> findExistingFile(String fileName) {
        for (File file : localFolder.listFiles()) {
            if (fileName.equals(file.getName())) {
                return Maybe.just(file);
            }
        }

        return Maybe.nothing();
    }

    @Override
    public void run() {
        if (isRunning) {
            throw new IllegalStateException("Already running");
        }

        isRunning = true;
        try {
            updateFilesFromFtpSite();
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("Error when updating files from the PA FTP site").withSource(PaUpdater.class));
        }

        try {
            writtenBrands.clear();
            JAXBContext context = JAXBContext.newInstance("org.atlasapi.remotesite.pa.bindings");
            Unmarshaller unmarshaller = context.createUnmarshaller();

            unmarshaller.setListener(new Unmarshaller.Listener() {
                public void beforeUnmarshal(Object target, Object parent) {
                }

                public void afterUnmarshal(Object target, Object parent) {
                    if (target instanceof ProgData) {
                        processProgData((ProgData) target, (ChannelData) parent);
                    }
                }
            });

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            reader.setContentHandler(unmarshaller.getUnmarshallerHandler());

            for (File file : localFolder.listFiles(filenameFilter)) {
                try {
                    reader.parse(file.toURI().toString());
                } catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaUpdater.class).withDescription("Error processing file " + file.toString()));
                }
            }
            writtenBrands.clear();
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaUpdater.class));
        } finally {
            isRunning = false;
        }
    }

    private void processProgData(ProgData progData, ChannelData channelData) {
        try {
            Maybe<Brand> brand = getBrand(progData);
            Maybe<Series> series = getSeries(progData);
            Maybe<Episode> episode = getEpisode(progData, channelData);

            if (episode.hasValue()) {
                if (series.hasValue()) {
                    series.requireValue().addItem(episode.requireValue());
                }
                if (brand.hasValue()) {
                    if (writtenBrands.contains(getBrandIdFromCurie(brand.requireValue().getCurie()))) {
                        episode.requireValue().setBrand(brand.requireValue());
                        contentWriter.createOrUpdateDefinitiveItem(episode.requireValue());
                    } else {
                        brand.requireValue().addItem(episode.requireValue());
                        contentWriter.createOrUpdateDefinitivePlaylist(brand.requireValue());
                        writtenBrands.add(getBrandIdFromCurie(brand.requireValue().getCurie()));
                    }
                } else {
                    contentWriter.createOrUpdateDefinitiveItem(episode.requireValue());
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaUpdater.class));
        }
    }

    private String getBrandIdFromCurie(String curie) {
        return curie.substring("pa:b-".length());
    }

    private Maybe<Brand> getBrand(ProgData progData) {
        String brandId = progData.getSeriesId();
        if (brandId == null || brandId.trim().isEmpty()) {
            return Maybe.nothing();
        }

        Brand brand = new Brand(PA_BASE_URL + "/brands/" + brandId, "pa:b-" + brandId, Publisher.PA);
        brand.setTitle(progData.getTitle());

        brand.setGenres(genreMap.map(ImmutableSet.copyOf(Iterables.transform(progData.getCategory(), Category.TO_GENRE_URIS))));

        /*
         * Pictures currently have no path List<Picture> pictures =
         * progData.getPicture(); for (Picture picture : pictures) {
         * picture.getvalue(); }
         */

        return Maybe.just(brand);
    }

    private Maybe<Series> getSeries(ProgData progData) {
        if (progData.getSeriesNumber() != null) {
            Series series = new Series(PA_BASE_URL + "/series/" + progData.getSeriesId() + "-" + progData.getSeriesNumber(), "pa:s-" + progData.getSeriesId() + "-" + progData.getSeriesNumber());

            series.setPublisher(Publisher.PA);

            series.setGenres(genreMap.map(ImmutableSet.copyOf(Iterables.transform(progData.getCategory(), Category.TO_GENRE_URIS))));

            return Maybe.just(series);
        }
        return Maybe.nothing();
    }

    private Maybe<Episode> getEpisode(ProgData progData, ChannelData channelData) {
        String channelUri = channelMap.getChannelUri(Integer.valueOf(channelData.getChannelId()));
        if (channelUri == null) {
            return Maybe.nothing();
        }

        String episodeUri = PA_BASE_URL + "/episodes/" + progData.getProgId();
        Content resolvedContent = contentResolver.findByUri(episodeUri);
        Episode episode;
        if (resolvedContent instanceof Episode) {
            episode = (Episode) resolvedContent;
        } else {
            episode = getBasicEpisode(progData);
        }

        Version version = findBestVersion(episode.getVersions());

        Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));

        String dateString = progData.getDate() + "-" + progData.getTime();
        DateTime transmissionTime = DateTimeFormat.forPattern("dd/MM/yyyy-HH:mm").withZone(DateTimeZones.LONDON).parseDateTime(dateString);
        Broadcast broadcast = new Broadcast(channelUri, transmissionTime, duration);
        version.addBroadcast(broadcast);

        return Maybe.just(episode);
    }

    private Version findBestVersion(Iterable<Version> versions) {
        for (Version version : versions) {
            if (version.getProvider() == Publisher.PA) {
                return version;
            }
        }

        return versions.iterator().next();
    }

    private Episode getBasicEpisode(ProgData progData) {
        Episode episode = new Episode(PA_BASE_URL + "/episodes/" + progData.getProgId(), "pa:e-" + progData.getProgId(), Publisher.PA);

        if (progData.getEpisodeTitle() != null) {
            episode.setTitle(progData.getEpisodeTitle());
        } else {
            episode.setTitle(progData.getTitle());
        }

        try {
            if (progData.getEpisodeNumber() != null) {
                episode.setEpisodeNumber(Integer.valueOf(progData.getEpisodeNumber()));
            }
            if (progData.getSeriesNumber() != null) {
                episode.setSeriesNumber(Integer.valueOf(progData.getSeriesNumber()));
            }
        } catch (NumberFormatException e) {
            // sometimes we don't get valid numbers
        }

        if (progData.getBillings() != null) {
            for (Billing billing : progData.getBillings().getBilling()) {
                if (billing.getType().equals("synopsis")) {
                    episode.setDescription(billing.getvalue());
                    break;
                }
            }
        }

        episode.setGenres(genreMap.map(ImmutableSet.copyOf(Iterables.transform(progData.getCategory(), Category.TO_GENRE_URIS))));

        // episode.setImage(image);
        // episode.setThumbnail(thumbnail);

        Version version = new Version();
        version.setProvider(Publisher.PA);
        episode.addVersion(version);

        Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));
        version.setDuration(duration);

        episode.addVersion(version);

        return episode;
    }
}
