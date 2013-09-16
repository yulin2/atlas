package org.atlasapi.application;

import static org.atlasapi.application.auth.TwitterAuthController.CALLBACK_URL;
import static org.atlasapi.application.auth.TwitterAuthController.LOGIN_FAILED_URL;
import static org.atlasapi.application.auth.TwitterAuthController.LOGIN_URL;
import static org.atlasapi.application.auth.TwitterAuthController.LOGOUT;

import java.util.List;
import java.util.Map;

import org.atlas.application.notification.NotifierModule;
import org.atlasapi.application.OldApplicationStore;
import org.atlasapi.application.OldMongoApplicationStore;
import org.atlasapi.application.auth.AdminAuthenticationInterceptor;
import org.atlasapi.application.auth.AuthCallbackHandler;
import org.atlasapi.application.auth.LoginController;
import org.atlasapi.application.auth.TwitterAuthController;
import org.atlasapi.application.auth.UserAuthCallbackHandler;
import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.SourceReadEntry;
import org.atlasapi.application.model.deserialize.IdDeserializer;
import org.atlasapi.application.model.deserialize.PublisherDeserializer;
import org.atlasapi.application.model.deserialize.SourceReadEntryDeserializer;
import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.application.persistence.MongoApplicationStore;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.query.IpCheckingApiKeyConfigurationFetcher;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.application.users.MongoUserStore;
import org.atlasapi.application.users.NewUserSupplier;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.application.writers.ApplicationListWriter;
import org.atlasapi.application.writers.ApplicationQueryResultWriter;
import org.atlasapi.application.www.ApplicationWebModule;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.input.GsonModelReader;
import org.atlasapi.input.ModelReader;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.persistence.ids.MongoSequentialIdGenerator;
import org.atlasapi.query.annotation.ResourceAnnotationIndex;
import org.atlasapi.query.common.AttributeCoercers;
import org.atlasapi.query.common.IndexAnnotationsExtractor;
import org.atlasapi.query.common.QueryAtomParser;
import org.atlasapi.query.common.QueryAttributeParser;
import org.atlasapi.query.common.QueryContextParser;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.Resource;
import org.atlasapi.query.common.StandardQueryParser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.metabroadcast.common.social.anonymous.AnonymousUserProvider;
import com.metabroadcast.common.social.anonymous.CookieBasedAnonymousUserProvider;
import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.DESUserRefKeyEncrypter;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;
import com.metabroadcast.common.social.auth.UserRefEncrypter;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.auth.credentials.MongoDBCredentialsStore;
import com.metabroadcast.common.social.auth.facebook.AccessTokenChecker;
import com.metabroadcast.common.social.twitter.TwitterApplication;
import com.metabroadcast.common.social.user.AccessTokenProcessor;
import com.metabroadcast.common.social.user.FixedAppIdUserRefBuilder;
import com.metabroadcast.common.social.user.TwitterOAuth1AccessTokenChecker;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.webapp.serializers.JodaDateTimeSerializer;

@Configuration
@Import({ ApplicationWebModule.class, NotifierModule.class })
@ImportResource("atlas-applications.xml")
public class ApplicationModule {

    private static final String SALT = "saltthatisofareasonablelength";
    private static final String APP_NAME = "atlas";
    private static final String COOKIE_NAME = "atlastw";
    
    private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final SourceIdCodec sourceIdCodec = new SourceIdCodec(idCodec);
    private final JsonDeserializer<Id> idDeserializer = new IdDeserializer(idCodec);
    private final JsonDeserializer<DateTime> datetimeDeserializer = new JodaDateTimeSerializer();
    private final JsonDeserializer<SourceReadEntry> readsDeserializer = new SourceReadEntryDeserializer();
    private final JsonDeserializer<Publisher> publisherDeserializer = new PublisherDeserializer();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, datetimeDeserializer)
            .registerTypeAdapter(Id.class, idDeserializer)
            .registerTypeAdapter(SourceReadEntry.class, readsDeserializer)
            .registerTypeAdapter(Publisher.class, publisherDeserializer)
            .create();
    
    private @Autowired @Qualifier(value = "adminMongo") DatabasedMongo adminMongo;
    private @Autowired ViewResolver viewResolver;
    private @Autowired RequestScopedAuthenticationProvider authProvider;

    @Value("${twitter.auth.consumerKey}") String consumerKey;
    @Value("${twitter.auth.consumerSecret}") String consumerSecret;
    @Value("${local.host.name}") String host;

    public @Bean
    ApplicationConfigurationFetcher configFetcher() {
        return new IpCheckingApiKeyConfigurationFetcher(applicationStore());
    }

    public @Bean
    OldApplicationStore applicationStore() {
        return new OldMongoApplicationStore(adminMongo);
    }

    public @Bean
    UserStore userStore() {
        return new MongoUserStore(adminMongo);
    }

    public @Bean
    CredentialsStore credentialsStore() {
        return new MongoDBCredentialsStore(adminMongo);
    }

    public @Bean
    AccessTokenProcessor accessTokenProcessor() {
        AccessTokenChecker accessTokenChecker = new TwitterOAuth1AccessTokenChecker(
                userRefBuilder(),
                consumerKey,
                consumerSecret);
        return new AccessTokenProcessor(accessTokenChecker, credentialsStore());
    }

    public @Bean
    FixedAppIdUserRefBuilder userRefBuilder() {
        return new FixedAppIdUserRefBuilder(APP_NAME);
    }

    public @Bean
    AnonymousUserProvider anonymousUserProvider() {
        return new CookieBasedAnonymousUserProvider(cookieTranslator(), userRefBuilder());
    }

    public @Bean
    TwitterAuthController authController() {
        AuthCallbackHandler handler = new UserAuthCallbackHandler(userStore(), new NewUserSupplier(
                new MongoSequentialIdGenerator(adminMongo, "users")));
        return new TwitterAuthController(
                new TwitterApplication(consumerKey, consumerSecret),
                accessTokenProcessor(),
                cookieTranslator(),
                handler,
                host);
    }

    public @Bean
    CookieTranslator cookieTranslator() {
        return new CookieTranslator(new DESUserRefKeyEncrypter(SALT), COOKIE_NAME, SALT);
    }

    public @Bean
    UserRefEncrypter userRefEncrypter() {
        return new UserRefEncrypter(
                new DESUserRefKeyEncrypter(SALT),
                SALT,
                Optional.<String> absent(),
                false,
                new SystemClock());
    }

    public @Bean
    DefaultAnnotationHandlerMapping controllerMappings() {
        DefaultAnnotationHandlerMapping controllerClassNameHandlerMapping = new DefaultAnnotationHandlerMapping();
        Object[] interceptors = { getAuthenticationInterceptor() };
        controllerClassNameHandlerMapping.setInterceptors(interceptors);
        return controllerClassNameHandlerMapping;
    }

    public @Bean
    AdminAuthenticationInterceptor getAuthenticationInterceptor() {
        Map<String, List<String>> methodToPath = Maps.newHashMap();

        methodToPath.put("GET", ImmutableList.of("/admin"));
        methodToPath.put("POST", ImmutableList.of("/admin"));
        methodToPath.put("PUT", ImmutableList.of("/admin"));
        methodToPath.put("DELETE", ImmutableList.of("/admin"));

        List<String> exceptions = ImmutableList.of(LoginController.ADMIN_LOGIN,
                LOGIN_URL,
                CALLBACK_URL,
                LOGIN_FAILED_URL,
                LOGOUT,
                "/includes/javascript");

        AdminAuthenticationInterceptor authenticationInterceptor = new AdminAuthenticationInterceptor();
        authenticationInterceptor.setViewResolver(viewResolver);
        authenticationInterceptor.setLoginView("redirect:" + LoginController.ADMIN_LOGIN);
        authenticationInterceptor.setAuthService(authProvider);
        authenticationInterceptor.setAuthenticationRequiredByMethod(methodToPath);
        authenticationInterceptor.setExceptions(exceptions);
        authenticationInterceptor.setUserStore(userStore());
        return authenticationInterceptor;
    }

    @Bean
    @Qualifier(value = "deerApplicationsStore")
    protected ApplicationStore deerApplicationsStore() {
        return new MongoApplicationStore(adminMongo);
    }
    
    @Bean 
    protected ApplicationUpdater applicationUpdater() {
        IdGenerator idGenerator = new MongoSequentialIdGenerator(adminMongo, "application");
        return new ApplicationUpdater(deerApplicationsStore(),
                idGenerator, adminHelper());
    }
    
    @Bean AdminHelper adminHelper() {
        return new AdminHelper(idCodec, sourceIdCodec);
    }
    
    private StandardQueryParser<Application> applicationQueryParser() {
        QueryContextParser contextParser = new QueryContextParser(configFetcher(),
                new IndexAnnotationsExtractor(applicationAnnotationIndex()), selectionBuilder());

        return new StandardQueryParser<Application>(Resource.APPLICATION,
                new QueryAttributeParser(ImmutableList.of(
                        QueryAtomParser.valueOf(Attributes.ID,
                                AttributeCoercers.idCoercer(idCodec))
                        )),
                idCodec, contextParser);
    }
    
    @Bean
    public ApplicationAdminController applicationAdminController() {
        return new ApplicationAdminController(
                applicationQueryParser(),
                applicationQueryExecutor(),
                new ApplicationQueryResultWriter(applicationListWriter()),
                gsonModelReader(),
                applicationUpdater(),
                adminHelper());
    }
    
    @Bean 
    public SourcesController sourcesController() {
        return new SourcesController(applicationUpdater(), adminHelper());
    }

    @Bean
    protected ModelReader gsonModelReader() {
        return new GsonModelReader(gson);
    }

    @Bean
    protected EntityListWriter<Application> applicationListWriter() {
        return new ApplicationListWriter(idCodec, sourceIdCodec);
    }

    @Bean
    ResourceAnnotationIndex applicationAnnotationIndex() {
        return ResourceAnnotationIndex.builder(Resource.APPLICATION, Annotation.all()).build();
    }

    @Bean
    protected QueryExecutor<Application> applicationQueryExecutor() {
        return new ApplicationQueryExecutor(deerApplicationsStore());
    }

    @Bean
    SelectionBuilder selectionBuilder() {
        return Selection.builder().withDefaultLimit(50).withMaxLimit(100);
    }

}
