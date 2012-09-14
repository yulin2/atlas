package org.atlasapi.remotesite.space;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 */
// TODO: enable with proper maven profile
@Ignore
public class TheSpaceUpdaterTest {

    @Test
    public void testRunTask() throws Exception {
        AdapterLog log = mock(AdapterLog.class);
        ContentResolver contentResolver = mock(ContentResolver.class);
        ContentWriter contentWriter = mock(ContentWriter.class);
        ContentGroupResolver groupResolver = mock(ContentGroupResolver.class);
        ContentGroupWriter groupWriter = mock(ContentGroupWriter.class);

        final AtomicInteger contentCounter = new AtomicInteger(0);
        final AtomicInteger playlistCounter = new AtomicInteger(0);

        when(contentResolver.findByCanonicalUris(anyCollection())).thenReturn(new ResolvedContent(Collections.EMPTY_MAP));
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                System.out.println(ToStringBuilder.reflectionToString(invocation.getArguments()[0], ToStringStyle.MULTI_LINE_STYLE));
                contentCounter.incrementAndGet();
                return null;
            }
        }).when(contentWriter).createOrUpdate(any(Container.class));
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                System.out.println(ToStringBuilder.reflectionToString(invocation.getArguments()[0], ToStringStyle.MULTI_LINE_STYLE));
                contentCounter.incrementAndGet();
                return null;
            }
        }).when(contentWriter).createOrUpdate(any(Item.class));

        when(groupResolver.findByCanonicalUris(anyCollection())).thenReturn(new ResolvedContent(Collections.EMPTY_MAP));
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                System.out.println(ToStringBuilder.reflectionToString(invocation.getArguments()[0], ToStringStyle.MULTI_LINE_STYLE));
                playlistCounter.incrementAndGet();
                return null;
            }
        }).when(groupWriter).createOrUpdate(any(ContentGroup.class));

        TheSpaceUpdater updater = new TheSpaceUpdater(contentResolver, contentWriter, groupResolver, groupWriter, log, null, null, "http://thespace.org");
        updater.runTask();

        System.out.println("Total contents: " + contentCounter.get());
        System.out.println("Total playlists: " + playlistCounter.get());

    }
}
