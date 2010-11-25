package org.atlasapi.remotesite.bbc;

import java.util.Set;

import org.atlasapi.remotesite.BaseSource;

public class BbcProgrammeSource extends BaseSource {

	private final SlashProgrammesRdf episode;
	private final SlashProgrammesVersionRdf version;
	private final String slashProgrammesUri;
    private final Set<ClipAndVersion> clips;

	public BbcProgrammeSource(String uri, String slashProgrammesUri, SlashProgrammesRdf episode, SlashProgrammesVersionRdf version, Set<ClipAndVersion> clips) {
		super(uri);
		this.slashProgrammesUri = slashProgrammesUri;
		this.episode = episode;
		this.version = version;
        this.clips = clips;
	}
	
	public SlashProgrammesRdf episode() {
		return episode;
	}
	
	public SlashProgrammesVersionRdf version() {
		return version;
	}
	
	public String getSlashProgrammesUri() {
		return slashProgrammesUri;
	}
	
	public Set<ClipAndVersion> clips() {
	    return clips;
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
