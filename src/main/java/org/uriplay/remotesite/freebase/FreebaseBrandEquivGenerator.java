package org.uriplay.remotesite.freebase;

import static com.freebase.json.JSON.a;
import static com.freebase.json.JSON.o;

import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Equiv;
import org.uriplay.remotesite.EquivGenerator;

import com.freebase.api.Freebase;
import com.freebase.json.JSON;
import com.google.inject.internal.Lists;

public class FreebaseBrandEquivGenerator implements EquivGenerator<Brand> {

    private static final Pattern HEX = Pattern.compile("(\\$[0-9A-F]{4})");
    protected static final String WIKIPEDIA = "http://en.wikipedia.org/wiki/";
    protected static final String THETVDB = "http://thetvdb.com/?tab=series&id=";
    protected static final String HULU = "http://www.hulu.com/";
    private final Freebase freebase;

    public FreebaseBrandEquivGenerator() {
        this(Freebase.getFreebase());
    }

    public FreebaseBrandEquivGenerator(Freebase freebase) {
        this.freebase = freebase;
    }

    @Override
    public List<Equiv> equivalent(Brand brand) {
        List<Equiv> equivs = Lists.newArrayList();

        JSON query = o("type", "/tv/tv_program", "name", brand.getTitle(), "*", a(o()));

        JSON source = freebase.mqlread(query);
        if (source != null && source.has("result")) {
            JSON result = source.get("result");

            if (result != null) {
                String huluId = string(getSingleValue(result, "hulu_id"), "value");
                addId(equivs, brand, huluId, HULU);
                
                String thetvdbId = string(getSingleValue(result, "thetvdb_id"), "value");
                addId(equivs, brand, thetvdbId, THETVDB);
    
                equivs.addAll(equivalents(brand, wikipediaIds(result)));
            }
        }

        return equivs;
    }
    
    private void addId(List<Equiv> equivs, Brand brand, String id, String prefix) {
        if (id != null && !brand.getCanonicalUri().equals(prefix+id)) {
            equivs.add(new Equiv(brand.getCanonicalUri(), prefix+id));
        }
    }

    private List<String> wikipediaIds(JSON result) {
        List<String> ids = Lists.newArrayList();

        if (result.has("key")) {
            JSON keys = result.get("key");
            if (keys.isArray()) {
                int size = result.get("key").array().size();

                for (int i = 0; i < size; i++) {
                    JSON key = keys.get(i);

                    if (key.has("namespace") && key.get("namespace").string().contains("wikipedia") && key.get("namespace").string().endsWith("en") && key.has("value")) {
                        String id = key.get("value").string();

                        Matcher matcher = HEX.matcher(id);
                        while (matcher.find()) {
                            String replacement = fromHex(matcher.group());
                            id = id.replace(matcher.group(), replacement);
                        }

                        ids.add(WIKIPEDIA+id);
                    }
                }
            }
        }

        return ids;
    }

    private JSON getSingleValue(JSON json, String name) {
        if (json.has(name)) {
            JSON keys = json.get(name);
            if (keys.isArray() && !keys.array().isEmpty()) {
                return keys.get(0);
            }
        }
        return o();
    }

    private String string(JSON json, String key) {
        if (json.has(key)) {
            return json.get(key).string();
        }
        return null;
    }

    private List<Equiv> equivalents(Brand brand, List<String> ids) {
        List<Equiv> equivs = Lists.newArrayList();

        for (String id : ids) {
            equivs.add(new Equiv(brand.getCanonicalUri(), id));
        }

        return equivs;
    }

    private String fromHex(String hex) {
        hex = hex.replace("$", "");
        BigInteger integer = new BigInteger(hex, 16);

        char character = (char) integer.longValue();
        return String.valueOf(character);
    }
}
