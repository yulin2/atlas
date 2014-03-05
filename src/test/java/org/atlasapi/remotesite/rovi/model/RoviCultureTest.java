package org.atlasapi.remotesite.rovi.model;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;


public class RoviCultureTest {

    @Test
    public void testLocaleConsistency() {
        for (String culture: RoviCulture.cultures()) {
            Locale locale = RoviCulture.localeFromCulture(culture);
            assertEquals(culture, RoviCulture.cultureFromLocale(locale));
        }
    }
    
    @Test
    public void testLanguageTagConsistency() {
        for (String culture: RoviCulture.cultures()) {
            Locale locale = RoviCulture.localeFromCulture(culture);
            assertEquals(locale, Locale.forLanguageTag(locale.toLanguageTag()));
        }        
    }
    
}
