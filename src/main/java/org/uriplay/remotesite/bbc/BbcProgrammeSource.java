package org.uriplay.remotesite.bbc;

import org.uriplay.remotesite.BaseSource;

public class BbcProgrammeSource extends BaseSource {

	private final SlashProgrammesEpisodeRdf episode;
	private final SlashProgrammesVersionRdf version;
	private boolean available = true;
	private final String slashProgrammesUri;

	public BbcProgrammeSource(String uri, String slashProgrammesUri, SlashProgrammesEpisodeRdf episode, SlashProgrammesVersionRdf version) {
		super(uri);
		this.slashProgrammesUri = slashProgrammesUri;
		this.episode = episode;
		this.version = version;
	}
	
	public SlashProgrammesEpisodeRdf episode() {
		return episode;
	}
	
	public SlashProgrammesVersionRdf version() {
		return version;
	}
	
	public String getSlashProgrammesUri() {
		return slashProgrammesUri;
	}
	
	public BbcProgrammeSource forAnUnavailableProgramme() {
		this.available = false;
		return this;
	}
	
	public boolean isAvailable() {
		return available;
	}
}
