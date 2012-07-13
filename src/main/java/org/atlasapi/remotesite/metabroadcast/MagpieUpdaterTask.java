package org.atlasapi.remotesite.metabroadcast;

import java.util.ArrayList;

import com.metabroadcast.common.scheduling.ScheduledTask;

public class MagpieUpdaterTask extends ScheduledTask{

	private MetaBroadcastMagpieUpdater magpieUpdater;

	public MagpieUpdaterTask(MetaBroadcastMagpieUpdater magpieUpdater) {
		this.magpieUpdater = magpieUpdater;
	}

	@Override
	protected void runTask() {
		magpieUpdater.updateTopics(new ArrayList<String>());
	}

}
