package org.atlasapi.remotesite.bbc;

import java.util.List;
import java.util.Set;

import org.atlasapi.remotesite.BaseSource;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesTopic;

import com.google.common.collect.ImmutableMap;

public class BbcProgrammeSource extends BaseSource {

	private final SlashProgrammesRdf episode;
	private final List<SlashProgrammesVersionRdf> versions;
	private final String slashProgrammesUri;
    private final Set<ClipAndVersion> clips;
    private final ImmutableMap<SlashProgrammesTopic, String> topics;

	public BbcProgrammeSource(String uri, String slashProgrammesUri, SlashProgrammesRdf episode, List<SlashProgrammesVersionRdf> versions, Set<ClipAndVersion> clips, ImmutableMap<SlashProgrammesTopic, String> topics) {
		super(uri);
		this.slashProgrammesUri = slashProgrammesUri;
		this.episode = episode;
		this.versions = versions;
        this.clips = clips;
        this.topics = topics;
	}
	
	public SlashProgrammesRdf episode() {
		return episode;
	}
	
	public List<SlashProgrammesVersionRdf> versions() {
		return versions;
	}
	
	public String getSlashProgrammesUri() {
		return slashProgrammesUri;
	}
	
	public Set<ClipAndVersion> clips() {
	    return clips;
	}
	
	public ImmutableMap<SlashProgrammesTopic, String> topics() {
        return topics;
    }

    static class ClipAndVersion {
	    private final SlashProgrammesRdf clip;
        private final SlashProgrammesVersionRdf version;

        public ClipAndVersion(SlashProgrammesRdf clip, SlashProgrammesVersionRdf version) {
            this.clip = clip;
            this.version = version;
        }
        
        public SlashProgrammesRdf clip() {
            return clip;
        }
        
        public SlashProgrammesVersionRdf version() {
            return version;
        }
	}
}
