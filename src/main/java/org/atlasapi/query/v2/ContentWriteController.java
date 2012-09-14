package org.atlasapi.query.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.input.ModelReader;
import org.atlasapi.input.ModelTransformer;
import org.atlasapi.input.ReadException;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Song;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;

public class ContentWriteController {
    
    private static final Logger log = LoggerFactory.getLogger(ContentWriteController.class);

    private final ApplicationConfigurationFetcher appConfigFetcher;
    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final ModelReader reader;

    private ModelTransformer<Description, Content> transformer;

    public ContentWriteController(ApplicationConfigurationFetcher appConfigFetcher, ContentResolver resolver, ContentWriter writer, ModelReader reader, ModelTransformer<Description, Content> transformer) {
        this.appConfigFetcher = appConfigFetcher;
        this.resolver = resolver;
        this.writer = writer;
        this.reader = reader;
        this.transformer = transformer;
    }
    
    @RequestMapping(value="/3.0/content.json", method = RequestMethod.POST)
    public Void writeContent(HttpServletRequest req, HttpServletResponse resp) {
        
        Maybe<ApplicationConfiguration> possibleConfig = appConfigFetcher.configurationFor(req);
        
        if (possibleConfig.isNothing()) {
            return error(resp, HttpStatus.UNAUTHORIZED.value());
        }
        
        Content content;
        try {
            content = complexify(deserialize(new InputStreamReader(req.getInputStream())));
        } catch (IOException ioe) {
            log.error("Error reading input for request " + req.getRequestURL(), ioe);
            return error(resp, HttpStatusCode.SERVER_ERROR.code());
        } catch (Exception e) {
            log.error("Error reading input for request " + req.getRequestURL(), e);
            return error(resp, HttpStatusCode.BAD_REQUEST.code());
        }
        
        if (!possibleConfig.requireValue().canWrite(content.getPublisher())) {
            return error(resp, HttpStatusCode.FORBIDDEN.code());
        }
        
        if (Strings.isNullOrEmpty(content.getCanonicalUri())) {
            return error(resp, HttpStatusCode.BAD_REQUEST.code());
        }
        
        try {
            content = merge(resolveExisting(content), content);
            if (content instanceof Item) {
                writer.createOrUpdate((Item) content);
            } else {
                writer.createOrUpdate((Container) content);
            }
        } catch (Exception e) {
            log.error("Error reading input for request " + req.getRequestURL(), e);
            return error(resp, HttpStatusCode.SERVER_ERROR.code());
        }
        
        resp.setStatus(HttpStatusCode.OK.code());
        resp.setContentLength(0);
        return null;
    }
    
    private Content merge(Maybe<Identified> possibleExisting, Content posted) {
        if (possibleExisting.isNothing()) {
            return posted;
        }
        Identified existing = possibleExisting.requireValue();
        if (existing instanceof Content) {
            return merge((Content) existing, posted);
        }
        throw new IllegalStateException("Entity for "+posted.getCanonicalUri()+" not Content");
    }

    private Content merge(Content existing, Content posted) {
        existing.setLastUpdated(posted.getLastUpdated());
        existing.setTitle(posted.getTitle());
        existing.setDescription(posted.getDescription());
        existing.setImage(posted.getImage());
        existing.setThumbnail(posted.getThumbnail());
        existing.setMediaType(posted.getMediaType());
        existing.setSpecialization(posted.getSpecialization());
        existing.setTopicRefs(merge(existing.getTopicRefs(), posted.getTopicRefs()));
        existing.setPeople(merge(existing.people(), posted.people()));
        existing.setKeyPhrases(posted.getKeyPhrases());
        if (existing instanceof Item && posted instanceof Item) {
            return mergeItems((Item)existing, (Item) posted);
        }
        return existing;
    }

    private Item mergeItems(Item existing, Item posted) {
        if (!posted.getVersions().isEmpty()) {
            Version existingVersion = Iterables.getFirst(existing.getVersions(), new Version());
            Version postedVersion = Iterables.getOnlyElement(posted.getVersions());
            mergeVersions(existingVersion, postedVersion);
        }
        if (existing instanceof Song && posted instanceof Song) {
            return mergeSongs((Song)existing, (Song)posted);
        }
        return existing;
    }

    private void mergeVersions(Version existing, Version posted) {
        existing.setManifestedAs(posted.getManifestedAs());
        existing.setBroadcasts(posted.getBroadcasts());
    }

    private Song mergeSongs(Song existing, Song posted) {
        existing.setIsrc(posted.getIsrc());
        existing.setDuration(posted.getDuration());
        return existing;
    }

    private <T> List<T> merge(List<T> existing, List<T> posted) {
        return ImmutableSet.copyOf(Iterables.concat(posted, existing)).asList();
    }

    private Maybe<Identified> resolveExisting(Content content) {
        ImmutableSet<String> uris = ImmutableSet.of(content.getCanonicalUri());
        ResolvedContent resolved = resolver.findByCanonicalUris(uris);
        return resolved.get(content.getCanonicalUri());
    }

    private Content complexify(Description inputContent) {
        return transformer.transform(inputContent);
    }

    private Description deserialize(Reader input) throws IOException, ReadException {
        return reader.read(new BufferedReader(input), Description.class);
    }
    
    private Void error(HttpServletResponse response, int code) {
        response.setStatus(code);
        response.setContentLength(0);
        return null;
    }
    
}
