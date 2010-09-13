package org.atlasapi.logging.www;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.LogReader;
import org.atlasapi.persistence.logging.AdapterLogEntry.ExceptionSummary;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
	public String showLog(Map<String, Object> model) throws IOException {
		model.put("logEntries", toModel(Iterables.limit(log.read(), MAX_LOG_ENTRIES_TO_SHOW)));
		return "system/log/show";
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
			.put("description", entry.description())
			.put("uri", entry.uri())
			.put("severity", entry.severity())
			.put("time", entry.timestamp().toString());
	}
}
