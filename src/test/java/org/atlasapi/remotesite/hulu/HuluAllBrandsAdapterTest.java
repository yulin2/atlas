//package org.atlasapi.remotesite.hulu;
//
//import java.util.concurrent.ExecutorService;
//
//import org.atlasapi.media.entity.Brand;
//import org.atlasapi.remotesite.HttpClients;
//import org.atlasapi.remotesite.SiteSpecificAdapter;
//import org.jmock.Expectations;
//import org.jmock.integration.junit3.MockObjectTestCase;
//
//@SuppressWarnings("unchecked")
//public class HuluAllBrandsAdapterTest extends MockObjectTestCase {
//    
//	private final SiteSpecificAdapter<Brand> brandAdapter = mock(SiteSpecificAdapter.class);
//    
//    private final ExecutorService executor = mock(ExecutorService.class);
//    
//    private final HuluAllBrandsUpdater adapter = new HuluAllBrandsUpdater(HttpClients.webserviceClient(), brandAdapter, executor);
//
//    public void testShouldGetBrand() throws Exception {
//    	
//        checking(new Expectations() {{
//            allowing(brandAdapter).canFetch((String) with(anything())); will(returnValue(true));
//            atLeast(100).of(executor).execute((Runnable) with(anything()));
//        }});
//
//        adapter.run();
//    }
//}
