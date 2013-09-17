package org.atlasapi.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;

import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.time.DateTimeZones;

import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public class ApplicationUpdaterTest {

  private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
  private final SourceIdCodec sourceIdCodec = new SourceIdCodec(idCodec);
  private final String slug = "app-5000";
  private final String title = "test application";
  private final DateTime created = new DateTime(DateTimeZones.UTC)
         .withDate(2013, 9, 13)
         .withTime(15, 13, 0, 0);
  private final SourceReadEntry testEntry1 = new SourceReadEntry(Publisher.BBC, SourceStatus.AVAILABLE_ENABLED);
  private final SourceReadEntry testEntry2 = new SourceReadEntry(Publisher.NETFLIX, SourceStatus.UNAVAILABLE);

  @Test
  public void testAddIdAndApiKey() {
      IdGenerator idGenerator = mock(IdGenerator.class);
      when(idGenerator.generateRaw()).thenReturn(5000L);
      List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(reads)
              .withWrites(writes)
              .build();
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("").build();
      
      Application application = Application.builder()
              .withId(null)
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      Application modified = updater.addIdAndApiKey(application);
      assertEquals(Id.valueOf(5000), modified.getId());
      assertFalse(modified.getCredentials().getApiKey().isEmpty());
  }
  
  @Test 
  public void testRepaceSources() {
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("abc123").build();
      List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(reads)
              .withWrites(writes)
              .build();
      Application application = Application.builder()
              .withId(Id.valueOf(5000))
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      IdGenerator idGenerator = mock(IdGenerator.class);
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      List<SourceReadEntry> modifiedReads = ImmutableList.of(
         new SourceReadEntry(Publisher.YOUTUBE, Publisher.YOUTUBE.getDefaultSourceStatus()),
         new SourceReadEntry(Publisher.NETFLIX, Publisher.NETFLIX.getDefaultSourceStatus())
      );

      List<Publisher> modfiedWrites = ImmutableList.of(
              Publisher.YOUTUBE, 
              Publisher.ARCHIVE_ORG
      );
      ApplicationSources modifiedSources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(modifiedReads)
              .withWrites(modfiedWrites)
              .build();
      Application modified = updater.replaceSources(application, modifiedSources);
      assertEquals(2, modified.getSources().getReads().size());
      assertEquals(Publisher.NETFLIX, modified.getSources().getReads().get(1).getPublisher());
      assertEquals(Publisher.ARCHIVE_ORG, modified.getSources().getWrites().get(1));
  }
  
  @Test
  public void testChangeReadSourceState() {
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("abc123").build();
      List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(reads)
              .withWrites(writes)
              .build();
      Application application = Application.builder()
              .withId(Id.valueOf(5000))
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      IdGenerator idGenerator = mock(IdGenerator.class);
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      Application modified = updater.changeReadSourceState(application, Publisher.NETFLIX, SourceState.REQUESTED);
      
      assertEquals(2, modified.getSources().getReads().size());
      assertEquals(SourceState.REQUESTED, modified.getSources().getReads().get(1).getSourceStatus().getState());
  }
  
  @Test
  public void testEnableAndDisableSource() {
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("abc123").build();
      SourceReadEntry bbcDisabled = new SourceReadEntry(Publisher.BBC, SourceStatus.AVAILABLE_DISABLED);
      
      List<SourceReadEntry> reads = ImmutableList.of(bbcDisabled, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(reads)
              .withWrites(writes)
              .build();
      Application application = Application.builder()
              .withId(Id.valueOf(5000))
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      IdGenerator idGenerator = mock(IdGenerator.class);
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      Application modified = updater.enableSource(application, Publisher.BBC);
      assertTrue(modified.getSources().getReads().get(0).getSourceStatus().isEnabled());
      modified = updater.disableSource(application, Publisher.BBC);
      assertFalse(modified.getSources().getReads().get(0).getSourceStatus().isEnabled());
  }
  
  @Test
  public void testAddWriteSource() {
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("abc123").build();
      List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(reads)
              .withWrites(writes)
              .build();
      Application application = Application.builder()
              .withId(Id.valueOf(5000))
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      IdGenerator idGenerator = mock(IdGenerator.class);
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      Application modified = updater.addWrites(application, Publisher.YOUTUBE);
      assertTrue(modified.getSources().getWrites().contains(Publisher.YOUTUBE));
  }
  
  @Test
  public void testRemoveWriteSource() {
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("abc123").build();
      List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(reads)
              .withWrites(writes)
              .build();
      Application application = Application.builder()
              .withId(Id.valueOf(5000))
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      IdGenerator idGenerator = mock(IdGenerator.class);
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      Application modified = updater.removeWrites(application, Publisher.KANDL_TOPICS);
      assertFalse(modified.getSources().getWrites().contains(Publisher.KANDL_TOPICS));
  }
  
  @Test
  public void testAddPrecedence() {
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("abc123").build();
      List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(false)
              .withReads(reads)
              .withWrites(writes)
              .build();
      Application application = Application.builder()
              .withId(Id.valueOf(5000))
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      IdGenerator idGenerator = mock(IdGenerator.class);
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      Application modified = updater.setPrecendenceOrder(application, ImmutableList.of(Publisher.NETFLIX, Publisher.BBC));
      assertTrue(modified.getSources().isPrecedenceEnabled());
      assertEquals(Publisher.NETFLIX, modified.getSources().getReads().get(0).getPublisher());
  }
  
  @Test
  public void testRemovePrecedence() {
      ApplicationCredentials credentials = ApplicationCredentials.builder()
              .withApiKey("abc123").build();
      List<SourceReadEntry> reads = ImmutableList.of(testEntry1, testEntry2);
      List<Publisher> writes = ImmutableList.of(Publisher.KANDL_TOPICS, Publisher.DBPEDIA);
      ApplicationSources sources = ApplicationSources.builder()
              .withPrecedence(true)
              .withReads(reads)
              .withWrites(writes)
              .build();
      Application application = Application.builder()
              .withId(Id.valueOf(5000))
              .withSlug(slug)
              .withTitle(title)
              .withCreated(created)
              .withCredentials(credentials)
              .withSources(sources)
              .build();
      IdGenerator idGenerator = mock(IdGenerator.class);
      ApplicationUpdater updater = new ApplicationUpdater(null, idGenerator, new AdminHelper(idCodec, sourceIdCodec));
      Application modified = updater.disablePrecendence(application);
      assertFalse(modified.getSources().isPrecedenceEnabled());
  }
}
