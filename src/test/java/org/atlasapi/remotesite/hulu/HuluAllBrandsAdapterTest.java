package org.atlasapi.remotesite.hulu;

import java.util.concurrent.ExecutorService;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.util.concurrent.MoreExecutors;

@SuppressWarnings("unchecked")
public class HuluAllBrandsAdapterTest extends MockObjectTestCase {
    
	private final SiteSpecificAdapter<Brand> brandAdapter = mock(SiteSpecificAdapter.class);
    private final ExecutorService executor = MoreExecutors.sameThreadExecutor();
    private final AdapterLog log = new NullAdapterLog();
    private final HuluAllBrandsUpdater updater = new HuluAllBrandsUpdater(new HttpBackedHuluClient(HttpClients.webserviceClient(),log), brandAdapter, executor, log);

    public void testShouldGetBrand() throws Exception {
    	
        checking(new Expectations() {{
            allowing(brandAdapter).canFetch(with(any(String.class))); will(returnValue(true));
            atLeast(100).of(brandAdapter).fetch(with(any(String.class)));
        }});

        updater.run();
    }
}
