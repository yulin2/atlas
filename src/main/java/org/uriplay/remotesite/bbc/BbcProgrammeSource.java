package org.uriplay.remotesite.bbc;

import org.uriplay.remotesite.BaseSource;

public class BbcProgrammeSource extends BaseSource {

	private final SlashProgrammesRdf episode;
	private final SlashProgrammesVersionRdf version;
	private final String slashProgrammesUri;

	public BbcProgrammeSource(String uri, String slashProgrammesUri, SlashProgrammesRdf episode, SlashProgrammesVersionRdf version) {
		super(uri);
		this.slashProgrammesUri = slashProgrammesUri;
		this.episode = episode;
		this.version = version;
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
}
