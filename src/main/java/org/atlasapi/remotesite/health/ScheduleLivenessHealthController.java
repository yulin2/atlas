package org.atlasapi.remotesite.health;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.security.HttpBasicAuthChecker;
import com.metabroadcast.common.security.UsernameAndPassword;
import com.metabroadcast.common.webapp.health.HealthController;

@Controller
public class ScheduleLivenessHealthController {

	private HealthController main;
	private HttpBasicAuthChecker checker;

	public ScheduleLivenessHealthController(HealthController main, String username, String password) {
		this.main = main;
		if (!Strings.isNullOrEmpty(password)) {
			this.checker = new HttpBasicAuthChecker(
					ImmutableList.of(new UsernameAndPassword(username, password)));
		} else {
			this.checker = null;
		}
	}

	@RequestMapping("feeds/pa/schedule-liveness")
	public String scheduleLiveness(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (checker == null) {
			response.setContentType(MimeType.TEXT_PLAIN.toString());
			response.getOutputStream().print(
					"No password set up, health page cannot be viewed");
			return null;
		}
		boolean allowed = checker.check(request);
		if (allowed) {
			return main.showHealthPageForSlugs(response, ScheduleLivenessHealthProbe.SCHEDULE_HEALTH_PROBE_SLUG);
		}
		HttpBasicAuthChecker.requestAuth(response, "Heath Page");
		return null;
	}
}
