package org.atlasapi.remotesite.youtube.entity;

/**
 * Access Controls will have one of 3 values - ALLOWED, DENIED or MODERATED.
 */
public class YouTubeAccessControl {
    Integer comment;
    Integer commentVote;
    Integer videoRespond;
    Integer rate;
    Integer embed;
    Integer list;
    Integer autoPlay;
    Integer syndicate;

    public Integer getComment() {
        return comment;
    }

    public void setComment(Integer comment) {
        this.comment = comment;
    }

    public Integer getCommentVote() {
        return commentVote;
    }

    public void setCommentVote(Integer commentVote) {
        this.commentVote = commentVote;
    }

    public Integer getVideoRespond() {
        return videoRespond;
    }

    public void setVideoRespond(Integer videoRespond) {
        this.videoRespond = videoRespond;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getEmbed() {
        return embed;
    }

    public void setEmbed(Integer embed) {
        this.embed = embed;
    }

    public Integer getList() {
        return list;
    }

    public void setList(Integer list) {
        this.list = list;
    }

    public Integer getAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(Integer autoPlay) {
        this.autoPlay = autoPlay;
    }

    public Integer getSyndicate() {
        return syndicate;
    }

    public void setSyndicate(Integer syndicate) {
        this.syndicate = syndicate;
    }

}
