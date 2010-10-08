package org.atlasapi.remotesite.ictomorrow;

import nu.xom.Element;
import nu.xom.Elements;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiException;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiHelper;

public class ICTomorrowPlaylistUpdater implements Runnable {
    private static final String ARCHIVE_ORG_EMBED_TEMPLATE = "<object width=\"640\" height=\"506\" classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\"><param value=\"true\" name=\"allowfullscreen\"/><param value=\"always\" name=\"allowscriptaccess\"/><param value=\"high\" name=\"quality\"/><param value=\"true\" name=\"cachebusting\"/><param value=\"#000000\" name=\"bgcolor\"/><param name=\"movie\" value=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" /><param value=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%1$s_512kb.mp4'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\" name=\"flashvars\"/><embed src=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" type=\"application/x-shockwave-flash\" width=\"640\" height=\"506\" allowfullscreen=\"true\" allowscriptaccess=\"always\" cachebusting=\"true\" bgcolor=\"#000000\" quality=\"high\" flashvars=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%1$s_512kb.mp4'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\"> </embed></object>";
    private static final String ARCHIVE_ORG_DOWNLOAD_TEMPLATE = "http://www.archive.org/download/%1$s/%1$s_512kb.mp4";
    
    private final ICTomorrowApiHelper apiHelper;
    private final ContentWriter contentWriter;
    

    public ICTomorrowPlaylistUpdater(ICTomorrowApiHelper apiHelper, ContentWriter contentWriter) {
        this.apiHelper = apiHelper;
        this.contentWriter = contentWriter;
    }
    
    @Override
    public void run() {
        try {
            Element jobElement = apiHelper.getContentMetadata(null, null, null);
            Integer jobId = Integer.valueOf(jobElement.getChildElements("job_id").get(0).getValue());
            
            Element metadataFile = null;
            while (metadataFile == null) {
                Element returnData = apiHelper.getMetadataFile(jobId);
                
                String returnValue = returnData.getFirstChildElement("job_status").getValue();
                if (returnValue.equals("COMPLETE")) {
                    metadataFile = returnData.getFirstChildElement("Download", "http://www.innovateuk.org/testbed/DownloadContent/").getFirstChildElement("Items");
                }
                else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            Elements itemsElement = metadataFile.getChildElements("Item");
            Playlist ictomorrowPlaylist = new Playlist("http://ictomorrow.co.uk", "ict:all", Publisher.ICTOMORROW);
            
            for (int i = 0; i < itemsElement.size(); i++) {
                Item item = null;
                Element itemElement = itemsElement.get(i);
                String uri = itemElement.getFirstChildElement("Key").getValue();
                if (uri.startsWith("http://www.archive.org/")) {
                    String itemName = uri.substring(uri.lastIndexOf("/") + 1);
                    item = new Item();
                    Version version = new Version();
                    Encoding encoding = new Encoding();
                    Location downloadLocation = new Location();
                    downloadLocation.setTransportType(TransportType.DOWNLOAD);
                    downloadLocation.setAvailable(true);
                    downloadLocation.setUri(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, itemName));
                    encoding.addAvailableAt(downloadLocation);
                    
                    Location embedLocation = new Location();
                    embedLocation.setTransportType(TransportType.EMBED);
                    embedLocation.setAvailable(true);
                    embedLocation.setEmbedCode(String.format(ARCHIVE_ORG_EMBED_TEMPLATE, itemName));
                    encoding.addAvailableAt(embedLocation);
                    
                    Location linkLocation = new Location();
                    linkLocation.setTransportType(TransportType.LINK);
                    linkLocation.setAvailable(true);
                    linkLocation.setUri(uri);
                    encoding.addAvailableAt(linkLocation);
                    
                    version.addManifestedAs(encoding);
                    item.setVersions(ImmutableSet.of(version));
                    item.setCanonicalUri(uri);
                    
                    Element titleElement = itemElement.getFirstChildElement("Title");
                    if (titleElement != null) {
                        item.setTitle(titleElement.getValue());
                    }
                }
                if (item != null) {
                    contentWriter.createOrUpdateItem(item);
                    
                    ictomorrowPlaylist.addItem(item);
                }
            }
            
            contentWriter.createOrUpdatePlaylist(ictomorrowPlaylist, true);
            
            /* <Item ContentHandle="2">
                <Key>http://www.innovate10.co.uk/1234</Key>
                <ItemCategory />
                <Characteristic Name="Source" Value="innovate" />
                <BaseXML>Innovate 10 is the leading networking, conference and exhibition event for businesses to meet other businesses, government and academia with the aim of making innovation happen ? creating opportunity and growth for the future.</BaseXML>
                <Title>Visit Innovate</Title>
                <ContentProvider>Technology Strategy Board</ContentProvider>
                <LicenseTemplateName>Generic No Approval</LicenseTemplateName>
            </Item>
            <Item ContentHandle="3">
                <Key>http://www.archive.org/details/Betty_Boop_Judge_For_a_Day_1935</Key>
                <ItemCategory />
                <Characteristic Name="Channel-Title" Value="Internet Archive - Collection: classic_cartoons" />
                <Characteristic Name="Keywords" Value="Betty Boop, Fleischer Studios" />
                <Characteristic Name="Link" Value="http://www.archive.org/details/Betty_Boop_Judge_For_a_Day_1935" />
                <Characteristic Name="Pub-date" Value="Wed, 01 Oct 2008 07:45:08 GMT" />
                <Characteristic Name="Source" Value="Archive.org" />
                <Characteristic Name="WebMaster" Value="info@archive.org (Info Box)" />
                <BaseXML>&lt;img width="160" style="padding-right:3px;float:left;" src="http://www.archive.org/services/get-item-image.php?identifier=Betty_Boop_Judge_For_a_Day_1935&amp;mediatype=movies&amp;collection=classic_cartoons"/&gt;&lt;p&gt;Betty is the cleaner a the local courthouse. On the bus ride to work one day, she grows tired of all the liberties her fellow citizens take. She falls asleep and dreams about what she'd do if she were the judge....&lt;/p&gt;&lt;p&gt;This item belongs to: movies/classic_cartoons.&lt;/p&gt;&lt;p&gt;This item has files of the following types: 512Kb MPEG4, Animated GIF, DivX, Metadata, Ogg Video, Thumbnail&lt;/p&gt;</BaseXML>
                <Title>Betty Boop: Judge For a Day</Title>
                <ContentProvider>Archive.org content</ContentProvider>
                <LicenseTemplateName>Generic No Approval</LicenseTemplateName>
            </Item>
            <Item ContentHandle="4">
                <Key>http://www.archive.org/details/Betty_Boop_a_Language_All_My_Own_1935</Key>
                <ItemCategory />
                <Characteristic Name="Channel-Title" Value="Internet Archive - Collection: classic_cartoons" />
                <Characteristic Name="Keywords" Value="Betty Boop, Fleischer Studios" />
                <Characteristic Name="Link" Value="http://www.archive.org/details/Betty_Boop_a_Language_All_My_Own_1935" />
                <Characteristic Name="Pub-date" Value="Wed, 01 Oct 2008 06:41:06 GMT" />
                <Characteristic Name="Source" Value="Archive.org" />
                <Characteristic Name="WebMaster" Value="info@archive.org (Info Box)" />
                <BaseXML>&lt;img width="160" style="padding-right:3px;float:left;" src="http://www.archive.org/services/get-item-image.php?identifier=Betty_Boop_a_Language_All_My_Own_1935&amp;mediatype=movies&amp;collection=classic_cartoons"/&gt;&lt;p&gt;Betty flies to Japan to do a show, and sings the title number. She then dons a kimono, and sings it again in Japanese. (http://en.wikipedia.org/wiki/A_Language_All_My_Own).&lt;/p&gt;&lt;p&gt;This item belongs to: movies/classic_cartoons.&lt;/p&gt;&lt;p&gt;This item has files of the following types: 512Kb MPEG4, Animated GIF, DivX, Metadata, Ogg Video, Thumbnail&lt;/p&gt;</BaseXML>
                <Title>Betty Boop: A Language All My Own</Title>
                <ContentProvider>Archive.org content</ContentProvider>
                <LicenseTemplateName>Generic No Approval</LicenseTemplateName>
            </Item> */
        } catch (ICTomorrowApiException e) {
            e.printStackTrace();
        }
    }
}


