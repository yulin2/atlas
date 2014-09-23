package org.atlasapi.remotesite.btvod;

import java.io.File;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class BtVodModule {

    private static final String MUSIC_CATEGORY = "Music";
    private static final String FILM_CATEGORY = "Film";
    private static final String TV_CATEGORY = "TV";
    private static final String KIDS_CATEGORY = "Kids";
    private static final String SPORT_CATEGORY = "Sport";
    private static final String BUY_TO_OWN_CATEGORY = "BuyToOwn";
    private static final String TV_BOX_SETS_CATEGORY = "TvBoxSets";
    private static final String BOX_OFFICE_CATEGORY = "BoxOffice";
    private static final String CZN_CONTENT_PROVIDER_ID = "CHC";
    
    private static final String URI_PREFIX = "http://vod.bt.com/";
    
    @Autowired
    private SimpleScheduler scheduler;
    @Autowired
    private ContentResolver contentResolver;
    @Autowired
    private ContentWriter contentWriter;
    @Autowired
    private ContentGroupResolver contentGroupResolver;
    @Autowired
    private ContentGroupWriter contentGroupWriter;
    @Value("${bt.vod.file}")
    private String filename;
    @Value("${bt.portal.baseUri}")
    private String btPortalBaseUri;
    
    @Bean
    public BtVodUpdater btVodUpdater() {
        return new BtVodUpdater(contentResolver, 
                contentWriter, btVodData(), URI_PREFIX, btVodContentGroupUpdater(), 
                describedFieldsExtractor(), Publisher.BT_VOD);
    }
    
    @Bean
    public BtVodDescribedFieldsExtractor describedFieldsExtractor() {
        return new BtVodDescribedFieldsExtractor(btPortalImageUriProvider());
    }
    
    @Bean
    public BtVodContentGroupUpdater btVodContentGroupUpdater() {
        return new BtVodContentGroupUpdater(contentGroupResolver, contentGroupWriter, 
                contentGroupsAndCriteria(), URI_PREFIX, Publisher.BT_VOD);
    }
    
    @Bean
    public BtPortalImageUriProvider btPortalImageUriProvider() {
        return new BtPortalImageUriProvider(new SimpleHttpClientBuilder().build(), btPortalBaseUri);
    }
    
    private BtVodData btVodData() {
        return new BtVodData(Files.asCharSource(new File(filename), Charsets.UTF_8));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Predicate<VodDataAndContent>> contentGroupsAndCriteria() {
        return ImmutableMap.<String, Predicate<VodDataAndContent>> builder()
                .put(MUSIC_CATEGORY.toLowerCase(), BtVodContentGroupUpdater.categoryPredicate(MUSIC_CATEGORY))
                .put(FILM_CATEGORY.toLowerCase(), 
                        Predicates.and(
                                BtVodContentGroupUpdater.categoryPredicate(FILM_CATEGORY),
                                Predicates.not(BtVodContentGroupUpdater.buyToOwnPredicate()),
                                Predicates.not(BtVodContentGroupUpdater.cznPredicate())
                        )
                    )
                .put(TV_CATEGORY.toLowerCase(), BtVodContentGroupUpdater.categoryPredicate(TV_CATEGORY))
                .put(KIDS_CATEGORY.toLowerCase(), BtVodContentGroupUpdater.categoryPredicate(KIDS_CATEGORY))
                .put(SPORT_CATEGORY.toLowerCase(), BtVodContentGroupUpdater.categoryPredicate(SPORT_CATEGORY))
                .put(CZN_CONTENT_PROVIDER_ID.toLowerCase(), BtVodContentGroupUpdater.cznPredicate())
                .put(BUY_TO_OWN_CATEGORY.toLowerCase(), BtVodContentGroupUpdater.buyToOwnPredicate())
                .put(BOX_OFFICE_CATEGORY.toLowerCase(), BtVodContentGroupUpdater.boxOfficePredicate())
                .put(TV_BOX_SETS_CATEGORY.toLowerCase(), BtVodContentGroupUpdater.tvBoxSetsPredicate())
                .build();
    }
    
    @PostConstruct
    public void scheduleTask() {
        scheduler.schedule(btVodUpdater(), RepetitionRules.NEVER);
    }
}
