package org.atlasapi.remotesite.youview;

import java.util.Map;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class YouViewChannelResolver {
    private static final Map<String, String> channelUriMapping = ImmutableMap.<String, String>builder()
            .put("1171", "http://ref.atlasapi.org/channels/absoluteradio")
            .put("1080", "http://ref.atlasapi.org/channels/aljazeeraenglish")
            .put("37397", "http://ref.atlasapi.org/channels/bbcalba")
            .put("1104", "http://www.bbc.co.uk/services/asiannetwork")
            .put("1047", "http://www.bbc.co.uk/services/bbcfour")
            .put("1181", "http://www.bbc.co.uk/services/bbchd")
            .put("1076", "http://www.bbc.co.uk/services/bbcnews")
            .put("1127", "http://www.bbc.co.uk/services/bbcone/cambridge")
            .put("25494", "http://www.bbc.co.uk/services/bbcone/channel_islands")
            .put("1121", "http://www.bbc.co.uk/services/bbcone/east")
            .put("1120", "http://www.bbc.co.uk/services/bbcone/east_midlands")
            .put("1182", "http://www.bbc.co.uk/services/bbcone/hd")
            .put("1040", "http://www.bbc.co.uk/services/bbcone/london")
            .put("1130", "http://www.bbc.co.uk/services/bbcone/ni")
            .put("1123", "http://www.bbc.co.uk/services/bbcone/north_east")
            .put("1122", "http://www.bbc.co.uk/services/bbcone/north_west")
            .put("1125", "http://www.bbc.co.uk/services/bbcone/oxford")
            .put("1129", "http://www.bbc.co.uk/services/bbcone/scotland")
            .put("1118", "http://www.bbc.co.uk/services/bbcone/south")
            .put("1126", "http://www.bbc.co.uk/services/bbcone/south_east")
            .put("1117", "http://www.bbc.co.uk/services/bbcone/south_west")
            .put("1131", "http://www.bbc.co.uk/services/bbcone/wales")
            .put("1116", "http://www.bbc.co.uk/services/bbcone/west")
            .put("1119", "http://www.bbc.co.uk/services/bbcone/west_midlands")
            .put("1124", "http://www.bbc.co.uk/services/bbcone/yorkshire")
            .put("1128", "http://www.bbc.co.uk/services/bbcone/east_yorkshire")
            .put("1077", "http://www.bbc.co.uk/services/parliament")
            .put("41596", "http://www.bbc.co.uk/services/radiocymru")
            .put("41222", "http://www.bbc.co.uk/services/radiofoyle")
            .put("2276993", "http://www.bbc.co.uk/services/radionangaidheal")
            .put("40643", "http://www.bbc.co.uk/services/radioscotland/fm")
            .put("40985", "http://www.bbc.co.uk/services/radioulster")
            .put("41463", "http://www.bbc.co.uk/services/radiowales/fm")
            .put("1095", "http://www.bbc.co.uk/services/radio1/england")
            .put("1096", "http://www.bbc.co.uk/services/1xtra")
            .put("1097", "http://www.bbc.co.uk/services/radio2")
            .put("1098", "http://www.bbc.co.uk/services/radio3")
            .put("1103", "http://www.bbc.co.uk/services/radio4extra")
            .put("1099", "http://www.bbc.co.uk/services/radio4/fm")
            .put("1100", "http://www.bbc.co.uk/services/5live")
            .put("1101", "http://www.bbc.co.uk/services/5livesportsextra")
            .put("1102", "http://www.bbc.co.uk/services/6music")
            .put("1046", "http://www.bbc.co.uk/services/bbcthree")
            .put("1041", "http://www.bbc.co.uk/services/bbctwo/england")
            .put("1144", "http://www.bbc.co.uk/services/bbctwo/ni")
            .put("1143", "http://www.bbc.co.uk/services/bbctwo/scotland")
            .put("1145", "http://www.bbc.co.uk/services/bbctwo/wales")
            .put("1105", "http://www.bbc.co.uk/services/worldservice")
            .put("1060", "http://ref.atlasapi.org/channels/bidtv")
            .put("1173", "http://ref.atlasapi.org/channels/capitalfm")
            .put("1073", "http://www.bbc.co.uk/services/cbbc")
            .put("1074", "http://www.bbc.co.uk/services/cbeebies")
            .put("1177", "http://ref.atlasapi.org/channels/challenge")
            .put("1184", "http://ref.atlasapi.org/channels/channel4hd")
            .put("1044", "http://www.five.tv")
            .put("1964773", "http://www.five.tv/plus1")
            .put("1075", "http://www.itv.com/channels/citv")
            .put("1192", "http://ref.atlasapi.org/channels/thecommunitychannel")
            .put("1043", "http://www.channel4.com")
            .put("1051", "http://www.channel4.com/cchannel4plus1")
            .put("1057", "http://ref.atlasapi.org/channels/dave")
            .put("1190", "http://ref.atlasapi.org/channels/davejavu")
            .put("1068", "http://ref.atlasapi.org/channels/espn")
            .put("1063", "http://www.e4.com")
            .put("1064", "http://www.e4.com/plus1")
            .put("1053", "http://film4.com")
            .put("15907", "http://ref.atlasapi.org/channels/foodnetwork")
            .put("1175", "http://ref.atlasapi.org/channels/gemstv")
            .put("6405350", "http://ref.atlasapi.org/channels/godchannel")
            .put("1055", "http://ref.atlasapi.org/channels/gold")
            .put("1108", "http://ref.atlasapi.org/channels/heatradio")
            .put("1062", "http://ref.atlasapi.org/channels/home")
            .put("51526", "http://www.itv.com/channels/itv1#plus1")
            .put("1045", "http://www.itv.com/channels/itv2")
            .put("1180", "http://www.itv.com/channels/itv2#plus1")
            .put("1048", "http://www.itv.com/channels/itv3")
            .put("1061", "http://www.itv.com/channels/itv4")
            .put("1112", "http://ref.atlasapi.org/channels/kerrang")
            .put("1107", "http://ref.atlasapi.org/channels/kiss")
            .put("1109", "http://ref.atlasapi.org/channels/magic")
            .put("1052", "http://www.channel4.com/more4")
            .put("20968", "http://ref.atlasapi.org/channels/movies4men")
            .put("718085", "http://ref.atlasapi.org/channels/movies4men2")
            .put("718208", "http://ref.atlasapi.org/channels/movies4men2plus1")
            .put("717956", "http://ref.atlasapi.org/channels/movies4menplus1")
            .put("1049", "http://ref.atlasapi.org/channels/sky3")
            .put("1188", "http://ref.atlasapi.org/channels/sky3plus1")
            .put("1203", "http://ref.atlasapi.org/channels/j6")
            .put("1070", "http://ref.atlasapi.org/channels/pricedroptv")
            .put("1110", "http://ref.atlasapi.org/channels/q")
            .put("1071", "http://ref.atlasapi.org/channels/quest")
            .put("1054", "http://ref.atlasapi.org/channels/qvc")
            .put("2960520", "http://ref.atlasapi.org/channels/racinguk")
            .put("15999", "http://ref.atlasapi.org/channels/really")
            .put("6738476", "http://ref.atlasapi.org/channels/rte1")
            .put("6738746", "http://ref.atlasapi.org/channels/mg")
            .put("6738477", "http://ref.atlasapi.org/channels/rte2")
            .put("1078", "http://ref.atlasapi.org/channels/skynews")
            .put("1186", "http://ref.atlasapi.org/channels/skysports1")
            .put("1187", "http://ref.atlasapi.org/channels/skysports2")
            .put("1197", "http://ref.atlasapi.org/channels/smashhitsradio")
            .put("1111", "http://ref.atlasapi.org/channels/g")
            .put("10441", "http://ref.atlasapi.org/channels/stvhd")
            .put("9942", "http://ref.atlasapi.org/channels/s4cclirlun")
            .put("56251", "http://ref.atlasapi.org/channels/s4c")
            .put("1113", "http://ref.atlasapi.org/channels/talksportradio")
            .put("1571", "http://ref.atlasapi.org/channels/teleg")
            .put("1081", "http://ref.atlasapi.org/channels/televisionx")
            .put("6738747", "http://ref.atlasapi.org/channels/tg4")
            .put("1106", "http://ref.atlasapi.org/channels/nk")
            .put("1167", "http://ref.atlasapi.org/channels/ulster")
            .put("7272537", "http://ref.atlasapi.org/channels/ulsterhd")
            .put("53916", "http://www.itv.com/channels/itv1/utv#plus1")
            .put("1058", "http://ref.atlasapi.org/channels/viva")
            .put("1050", "http://ref.atlasapi.org/channels/yesterday")
            .put("1056", "http://www.4music.com")
            .put("4209268", "http://www.channel4.com/4seven")
            .put("1066", "http://www.five.tv/channels/five-usa")
            .put("1065", "http://www.five.tv/channels/fiver")
            .put("1042", "http://www.itv.com/channels/itv1/london")
            .build();
    
    public static Map<String, Channel> generateChannelMapping(final ChannelResolver channelResolver) {
        return Maps.transformValues(channelUriMapping, new Function<String, Channel>() {
           @Override
           public Channel apply(String input) {
               return channelResolver.fromUri(input).requireValue();
           }
        });
    }
}
