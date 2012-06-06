package org.atlasapi.remotesite.metabroadcast;

import java.util.ArrayList;

import org.atlasapi.remotesite.redux.UpdateProgress;

import com.metabroadcast.common.scheduling.ScheduledTask;

public class MagpieUpdaterTask extends ScheduledTask{

	private MetaBroadcastMagpieUpdater magpieUpdater;

	public MagpieUpdaterTask(MetaBroadcastMagpieUpdater magpieUpdater) {
		magpieUpdater.setReporter(this.reporter());
		this.magpieUpdater = magpieUpdater;
	}

	@Override
	protected void runTask() {
		while(shouldContinue()) {
			magpieUpdater.updateTopics(new ArrayList<String>());
		}
	}

}
