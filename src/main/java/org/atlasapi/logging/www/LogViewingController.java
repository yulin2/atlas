package org.atlasapi.logging.www;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.ExceptionSummary;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.logging.LogReader;
import org.joda.time.format.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;

@Controller
public class LogViewingController {

	private static final int MAX_LOG_ENTRIES_TO_SHOW = 500;
	
	private final LogReader log;

	public LogViewingController(LogReader log) {
		this.log = log;
	}
	
	@RequestMapping("/system/log")
	public String showLog(Map<String, Object> model, @RequestParam(required=false) String source, @RequestParam(required=false, value="level", defaultValue="info") String severityString) throws IOException {
		model.put("logEntries", toModel(Iterables.limit(filter(source, toSeverity(severityString)), MAX_LOG_ENTRIES_TO_SHOW)));
		return "system/log/show";
	}

	private static Severity toSeverity(String severityString) {
		if (severityString == null) {
			return null;
		}
		return Severity.valueOf(severityString.toUpperCase());
	}

	private Iterable<AdapterLogEntry> filter(final String source, final Severity severity) {
		
		Predicate<AdapterLogEntry> filter = Predicates.alwaysTrue();
		
		Predicate<AdapterLogEntry> sourceFilter = new Predicate<AdapterLogEntry>() {

			@Override
			public boolean apply(AdapterLogEntry entry) {
				return entry.classNameOfSource() != null && entry.classNameOfSource().startsWith(source);
			}
		};
		
		Predicate<AdapterLogEntry> severityFilter = new Predicate<AdapterLogEntry>() {
			
			@Override
			public boolean apply(AdapterLogEntry entry) {
				return entry.severity().isMoreSevereOrSameAs(severity);
			}
		};
		
		if (source != null) {
			filter = Predicates.and(filter, sourceFilter);
		}
		if (severity != null) {
			filter = Predicates.and(filter, severityFilter);
		}
		return Iterables.filter(log.read(), filter);
	}
	
	@RequestMapping("/system/log/{id}/trace")
	public String showTrace(Map<String, Object> model, @PathVariable String id) throws IOException {
		AdapterLogEntry entry = log.requireById(id);
		model.put("trace", fullTraceFrom(entry));
		return "system/log/trace";
	}

	private List<String> fullTraceFrom(AdapterLogEntry entry) {
		ExceptionSummary cause = entry.exceptionSummary();
		if (cause == null) {
			return null;
		}
		return cause.fullTrace();
	}

	private SimpleModelList toModel(Iterable<AdapterLogEntry> entries) {
		SimpleModelList list = new SimpleModelList();
		for (AdapterLogEntry entry : entries) {
			list.add(toModel(entry));
		}
		return list;
	}

	private SimpleModel toModel(AdapterLogEntry entry) {
		return new SimpleModel()
			.put("id", entry.id())
			.put("source", entry.classNameOfSource())
			.put("description", descriptionOrExceptionMessage(entry))
			.put("uri", entry.uri())
			.put("severity", entry.severity())
			.put("time", entry.timestamp().toString(DateTimeFormat.forPattern("dd/MM HH:mm:ss")));
	}

	private String descriptionOrExceptionMessage(AdapterLogEntry entry) {
		if (entry.description() != null) {
			return entry.description();
		}
		if (entry.exceptionSummary() != null) {
			return entry.exceptionSummary().message();
		}
		return null;
	}
}
