package org.atlasapi.remotesite.knowledgemotion.topics;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Objects;

public class WikipediaKeyword {

    private final String wikipediaUrl;
    private final String articleTitle;

    public static Builder builder() {
        return new Builder();
    }

    private WikipediaKeyword(String wikipediaUrl, String articleTitle) {
        this.wikipediaUrl = checkNotNull(emptyToNull(wikipediaUrl));
        this.articleTitle = checkNotNull(emptyToNull(articleTitle));
    }


    public String getWikipediaUrl() {
        return wikipediaUrl;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("wikipediaUrl", wikipediaUrl)
                .add("articleTitle", articleTitle)
                .toString();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof WikipediaKeyword) {
            WikipediaKeyword other = (WikipediaKeyword) that;
            return wikipediaUrl.equals(other.wikipediaUrl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wikipediaUrl);
    }

    public static class Builder {
        private String wikipediaUrl;
        private String articleTitle;

        public WikipediaKeyword build() {
            return new WikipediaKeyword(wikipediaUrl, articleTitle);
        }

        public Builder from(WikipediaKeyword keyword) {
            this.wikipediaUrl = keyword.getWikipediaUrl();
            this.articleTitle = keyword.getArticleTitle();
            return this;
        }

        public Builder withWikipediaUrl(String wikipediaUrl) {
            this.wikipediaUrl = wikipediaUrl;
            return this;
        }

        public Builder withArticleTitle(String articleTitle) {
            this.articleTitle = articleTitle;
            return this;
        }
    }

}
