package org.atlasapi.remotesite.youtube.entity;

import org.atlasapi.remotesite.youtube.entity.Permission;

/**
 * Access Controls will have one of 3 values - ALLOWED, DENIED or MODERATED.
 */
public class YouTubeAccessControl {
    Permission comment;
    Permission commentVote;
    Permission videoRespond;
    Permission rate;
    Permission embed;
    Permission list;
    Permission autoPlay;
    Permission syndicate;

    public Permission getComment() {
        return comment;
    }

    public void setComment(Permission comment) {
        this.comment = comment;
    }

    public Permission getCommentVote() {
        return commentVote;
    }

    public void setCommentVote(Permission commentVote) {
        this.commentVote = commentVote;
    }

    public Permission getVideoRespond() {
        return videoRespond;
    }

    public void setVideoRespond(Permission videoRespond) {
        this.videoRespond = videoRespond;
    }

    public Permission getRate() {
        return rate;
    }

    public void setRate(Permission rate) {
        this.rate = rate;
    }

    public Permission getEmbed() {
        return embed;
    }

    public void setEmbed(Permission embed) {
        this.embed = embed;
    }

    public Permission getList() {
        return list;
    }

    public void setList(Permission list) {
        this.list = list;
    }

    public Permission getAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(Permission autoPlay) {
        this.autoPlay = autoPlay;
    }

    public Permission getSyndicate() {
        return syndicate;
    }

    public void setSyndicate(Permission syndicate) {
        this.syndicate = syndicate;
    }

}
