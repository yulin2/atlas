package org.atlasapi.remotesite.channel4.pmlsd;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class C4CategoryGenreMap extends GenreMap {

    static {
        genres.put("http://www.channel4.com/programmes/tags/animals", AtlasGenre.ANIMALS);
        genres.put("http://www.channel4.com/programmes/tags/animation", AtlasGenre.ANIMATION);
        genres.put("http://www.channel4.com/programmes/tags/art-design-and-literature", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/business-and-money", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/chat-shows", AtlasGenre.ENTERTAINMENT);
        genres.put("http://www.channel4.com/programmes/tags/childrens-shows", AtlasGenre.CHILDRENS);
        genres.put("http://www.channel4.com/programmes/tags/comedy", AtlasGenre.COMEDY);
        genres.put("http://www.channel4.com/programmes/tags/disability", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/documentaries", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/drama", AtlasGenre.DRAMA);
        genres.put("http://www.channel4.com/programmes/tags/education-and-learning", AtlasGenre.LEARNING);
        genres.put("http://www.channel4.com/programmes/tags/entertainment", AtlasGenre.ENTERTAINMENT);
        genres.put("http://www.channel4.com/programmes/tags/family-and-parenting", AtlasGenre.LIFESTYLE);
        genres.put("http://www.channel4.com/programmes/tags/fashion-and-beauty", AtlasGenre.LIFESTYLE);
        genres.put("http://www.channel4.com/programmes/tags/film", AtlasGenre.FILM);
        genres.put("http://www.channel4.com/programmes/tags/food", AtlasGenre.LIFESTYLE);
        genres.put("http://www.channel4.com/programmes/tags/health-and-wellbeing", AtlasGenre.LIFESTYLE);
        genres.put("http://www.channel4.com/programmes/tags/history", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/homes-and-gardens", AtlasGenre.LIFESTYLE);
        genres.put("http://www.channel4.com/programmes/tags/lifestyle", AtlasGenre.LIFESTYLE);
        genres.put("http://www.channel4.com/programmes/tags/music", AtlasGenre.MUSIC);
        genres.put("http://www.channel4.com/programmes/tags/news-current-affairs-and-politics", AtlasGenre.NEWS);
        genres.put("http://www.channel4.com/programmes/tags/religion-and-belief", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/science-nature-and-the-environment", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/sex-and-relationships", AtlasGenre.LIFESTYLE);
        genres.put("http://www.channel4.com/programmes/tags/society-and-culture", AtlasGenre.FACTUAL);
        genres.put("http://www.channel4.com/programmes/tags/sports-and-games", AtlasGenre.SPORT);
        genres.put("http://www.channel4.com/programmes/tags/factual", AtlasGenre.NEWS); // looks wrong, but it's a renamed news, current affairs and politics
        genres.put("http://www.channel4.com/programmes/tags/sport", AtlasGenre.SPORT);
    }

}
