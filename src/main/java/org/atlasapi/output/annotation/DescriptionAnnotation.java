package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Song;
import org.atlasapi.output.writers.ItemDisplayTitleWriter;
import org.atlasapi.output.writers.SourceWriter;
import org.atlasapi.query.v4.schedule.EntityWriter;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;
import org.joda.time.Duration;

public class DescriptionAnnotation extends OutputAnnotation<Content> {

    private final EntityWriter<Publisher> publisherWriter = SourceWriter.sourceWriter("source");
    private final ItemDisplayTitleWriter displayTitleWriter = new ItemDisplayTitleWriter();

    public DescriptionAnnotation() {
        super(Content.class);
    }

    @Override
    public void write(Content content, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeObject(publisherWriter, content.getPublisher(), ctxt);
        
        if (content instanceof Episode) {
            Episode episode = (Episode) content;
            writer.writeField("episode_number", episode.getEpisodeNumber());
            writer.writeField("series_number", episode.getSeriesNumber());
        }
        
        if (content instanceof Series) {
            Series series = (Series) content;
            writer.writeField("series_number", series.getSeriesNumber());
            writer.writeField("total_episodes", series.getTotalEpisodes());
        }
        
        writer.writeField("title", content.getTitle());
        if (content instanceof Item) {
            writer.writeObject(displayTitleWriter, (Item) content, ctxt);
        }
        writer.writeField("description", content.getDescription());
        
        if (content instanceof Film) {
            Film film = (Film) content;
            writer.writeField("year", film.getYear());
        }
        
        if (content instanceof Song) {
            Song song = (Song) content;
            writer.writeField("isrc", song.getIsrc());
            Duration duration = song.getDuration();
            writer.writeField("duration", duration != null ? duration.getStandardSeconds()
                                                           : null);
        }
        
        writer.writeField("image", content.getImage());
        writer.writeField("thumbnail", content.getThumbnail());

        writer.writeField("media_type", content.getMediaType());
        writer.writeField("specialization", content.getSpecialization());
    }

}
