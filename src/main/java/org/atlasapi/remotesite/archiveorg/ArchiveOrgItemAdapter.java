package org.atlasapi.remotesite.archiveorg;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.SimpleHttpClient;

public class ArchiveOrgItemAdapter implements SiteSpecificAdapter<Item>{
    private static final String ITEM_PREFIX = "http://www.archive.org/details/";
    private static final String JSON_OUTPUT_PARAMETER = "&output=json";
    
    private static final String ARCHIVE_ORG_EMBED_TEMPLATE = "<object width=\"640\" height=\"506\" classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\"><param value=\"true\" name=\"allowfullscreen\"/><param value=\"always\" name=\"allowscriptaccess\"/><param value=\"high\" name=\"quality\"/><param value=\"true\" name=\"cachebusting\"/><param value=\"#000000\" name=\"bgcolor\"/><param name=\"movie\" value=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" /><param value=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%1$s_512kb.mp4'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\" name=\"flashvars\"/><embed src=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" type=\"application/x-shockwave-flash\" width=\"640\" height=\"506\" allowfullscreen=\"true\" allowscriptaccess=\"always\" cachebusting=\"true\" bgcolor=\"#000000\" quality=\"high\" flashvars=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%1$s_512kb.mp4'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\"> </embed></object>";
    private static final String ARCHIVE_ORG_DOWNLOAD_TEMPLATE = "http://www.archive.org/download/%1$s";
    
    private final SimpleHttpClient client;
    
    public ArchiveOrgItemAdapter(SimpleHttpClient client) {
        this.client = client;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Item fetch(String uri) {
        try {
            HttpResponse res = client.get(uri + JSON_OUTPUT_PARAMETER);
            String body = res.body();
            //System.out.println(body);
            ObjectMapper jsonMapper = new ObjectMapper();
            Map<String, Object> json = jsonMapper.readValue(body, Map.class);
            Map<String, Object> metadata = (Map<String, Object>) json.get("metadata");
            
            System.out.println(metadata);
            
            String identifier = getFirstValue(metadata.get("identifier"));
            String title = getFirstValue(metadata.get("title"));
            
            Item item = new Item(uri, "arc:" + identifier, Publisher.ICTOMORROW);
            item.setTitle(title);
            Version version = new Version();
            
            Encoding encoding = new Encoding();
            
            Location embedLocation = new Location();
            embedLocation.setTransportType(TransportType.EMBED);
            embedLocation.setAvailable(true);
            embedLocation.setEmbedCode(String.format(ARCHIVE_ORG_EMBED_TEMPLATE, identifier));
            encoding.addAvailableAt(embedLocation);
            
            Location linkLocation = new Location();
            linkLocation.setTransportType(TransportType.LINK);
            linkLocation.setAvailable(true);
            linkLocation.setUri(uri);
            encoding.addAvailableAt(linkLocation);
            
            Map<String, Object> files = (Map<String, Object>) json.get("files");
            boolean firstThumbnail = true;
            for (String fileName : files.keySet()) {
                Map<String, Object> file = (Map<String, Object>) files.get(fileName);
                String format = (String) file.get("format");
                if (format != null) {
                    if (format.contains("MPEG4")) { // || format.equals("MPEG2") || format.equals("Ogg Video")
                        Location downloadLocation = new Location();
                        downloadLocation.setTransportType(TransportType.DOWNLOAD);
                        downloadLocation.setAvailable(true);
                        downloadLocation.setUri(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, identifier) + fileName);
                        encoding.addAvailableAt(downloadLocation);
                    }
                    else if (format.equals("Thumbnail")) {
                        if (!firstThumbnail && item.getThumbnail() == null) {
                            item.setThumbnail(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, identifier) + fileName);
                            item.setImage(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, identifier) + fileName);
                        }
                        else {
                            firstThumbnail = false;
                        }
                    }
                }
            }
            
            version.addManifestedAs(encoding);
            item.setVersions(ImmutableSet.of(version));
            
            return item;
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private String getFirstValue(Object object) {
        List<String> list = (List<String>) object;
        if (!list.isEmpty()) {
            return list.iterator().next();
        }
        
        return null;
    }
    
    public static void main(String[] args) {
        String uri = "http://www.archive.org/details/merry_melodies_falling_hare";
        ArchiveOrgItemAdapter adapter = new ArchiveOrgItemAdapter(HttpClients.webserviceClient());
        if (adapter.canFetch(uri)) {
            adapter.fetch(uri);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return uri.startsWith(ITEM_PREFIX);
    }

}
