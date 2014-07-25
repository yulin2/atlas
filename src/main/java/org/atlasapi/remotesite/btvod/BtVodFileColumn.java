package org.atlasapi.remotesite.btvod;

import static com.google.common.base.Preconditions.checkNotNull;

public enum BtVodFileColumn {

    PRODUCT_ID("ProductId"),
    PRODUCT_TITLE("ProductTitle"),
    TRAILER("Trailer"),
    PROMOTION("Promotion"),
    PRICE("Price"),
    AVAILABILITY_START("PAvailailityStartDate"),
    AVAILABILITY_END("PAvailailityEndDate"),
    PACKSHOT("Packshot"),
    BACKGROUND("Background"),
    ENTITLEMENT_PERIOD("EntitlementPeriod"),
    PARTICPANT_ROLE_NAME("ParticipantRoleName"),
    ASSET_ID("AssetId"),
    IS_DUPLICATE_ASSET("IsDuplicateAsset"),
    CLIENT_ASSET_ID("ClientAssetId"),
    ASSET_TITLE("AssetTitle"),
    ASSET_AVAILABILITY_START_DATE("AssetAvailabilityStartDate"),
    ASSET_AVAILABILITY_END_DATE("AssetAvailabilityEndDate"),
    CATEGORY("Category"),
    RELEASE_YEAR("ReleaseYear"),
    SERIES_TITLE("SeriesTitle(Asset)"),
    SERIES_NUMBER("SeriesNumber"),
    EPISODE_TITLE("EpisodeTitle"),
    EPISODE_NUMBER("EpisodeNumber"),
    MEDIA_ID("MediaId"),
    CAR_RELEASE_PID("CAR_ReleasePID"),
    CAR_RELEASE_ID("CAR_ReleaseID"),
    CAR_ASPECT_RATIO("CAR_AspectRatio"),
    YOU_RELEASE_PID("YOU_ReleasePID"),
    YOU_RELEASE_ID("YOU_ReleaseID"),
    YOU_ASPECT_RATIO("YOU_AspectRatio"),
    SERVICE_FORMAT("ServiceFormat"),
    HD_FLAG("HDFlag"),
    CONTENT_PROVIDER_NAME("ContentProviderName"),
    SCHEDULER_CHANNEL("SchedulerChannel(Asset)"),
    DIRECTOR_ROLE("DirectorRole"),
    DIRECTOR_NAME("DirectorName"),
    ACTOR_ROLE("ActorRole"),
    ACTOR_NAME("ActorName"),
    PRESENTER_ROLE("PresenterRole"),
    PRESENTER_NAME("PresenterName"),
    CAST_CREW_ROLE("Castcrewrole"),
    CAST_CREW_NAME("Castcrewname"),
    GENRE("Genre"),
    SYNOPSIS("Synopsis"),
    RATING("Rating"),
    METADATA_DURATION("MetadataDuration"),
    SUBSCRIPTIONS("Subscriptions"),
    SERVICES("Services"),
    IS_CAR("IsCAR"),
    IS_YOUVIEW("ISYouView"),
    IS_IETV("IsIETV"),
    IS_YOUVIEW_CCO("IsYouViewCCO"),
    CATEGORY_ID("CategoryID"),
    SUB_GENRE("SubGenre"),
    TITLE_ID("TitleID"),
    OFFER_START_DATE("OfferStartdate"),
    OFFER_END_DATE("OfferEnddate"),
    DOWNLOAD_PLAY("Downloadplay"),
    CONTENT_PROVIDER_ID("ContentProviderId"),
    BRANDIA_ID("BrandIA_ID"),
    BRAND_ID("BrandID"),
    BRAND_TITLE("BrandTitle"),
    DATE("Date"),
    PACK_TITLE("PackTitle"),
    IS_SERIES("IsSeries"),
    PRODUCT_CAR_DURATION("ProductCAR_Duration"),
    PRODUCT_YOUVIEW_DURATION("ProductYouView_Duration"),
    SERIES_GENRE("SeriesGenre"),
    SERIES_TITLE_PRODUCT("SeriesTitle(Product)"),
    INFERRED_SERIES_TITLE("InferredSeriesTitle"),
    SERIES_DESCRIPTION("SeriesDescription"),
    SERIES_SUB_GENRE("SeriesSubGenre"),
    SCHEDULER_CHANNEL_PRODUCT("SchedulerChannel(Product)");
    
    private String key;

    private BtVodFileColumn(String key) {
        this.key = checkNotNull(key);
    }
    
    public String key() {
        return key;
    }
}
