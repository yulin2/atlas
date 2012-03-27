// This file was automatically generated from applications-widgets-js.soy.
// Please don't edit this file by hand.

goog.provide('atlas.templates.applications.widgets');

goog.require('soy');
goog.require('soy.StringBuilder');


/**
 * @param {Object.<string, *>=} opt_data
 * @param {soy.StringBuilder=} opt_sb
 * @return {string|undefined}
 * @notypecheck
 */
atlas.templates.applications.widgets.applicationContent = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('<h1>Application: ', soy.$$escapeHtml(opt_data.application.title), '</h1><div class="line"><div class="unit size1of2"><h2>API Key</h2><div class="form"><div class="inputHolder mlm"><div>', soy.$$escapeHtml(opt_data.application.credentials.apiKey), '</div></div></div><h2>IP Addresses</h2><ul id="app-ips" class="mod" data-app="', soy.$$escapeHtml(opt_data.application.slug), '" data-ips="', soy.$$escapeHtml(opt_data.application.credentials.ipRanges.length), '">');
  var rangeList12 = opt_data.application.credentials.ipRanges;
  var rangeListLen12 = rangeList12.length;
  if (rangeListLen12 > 0) {
    for (var rangeIndex12 = 0; rangeIndex12 < rangeListLen12; rangeIndex12++) {
      var rangeData12 = rangeList12[rangeIndex12];
      output.append('<li><span>', soy.$$escapeHtml(rangeData12), '</span><span style="display:none;opacity:0">✖</span></li>');
    }
  } else {
    output.append('<li><div class="alert inf mln mrn">No addresses</div></li>');
  }
  output.append('</ul><form id="ipaddress" method="post" action="/admin/applications/', soy.$$escapeHtml(opt_data.application.slug), '/ipranges" data-app="', soy.$$escapeHtml(opt_data.application.slug), '" class="form"><div class="grp"><label for="addIp">IP Address</label><div class="inputHolder"><input type="text" id="addIp" name="ipaddress" placeholder="CIDR" required /></div></div><input type="submit" value="Add" class="btn pos" /></form></div><div class="unit size1of2 lastUnit"><h2>Sources</h2><div id="enable-precendence-div" class="alert inf ', (opt_data.application.configuration.precedence) ? 'hide' : '', '">Publisher precedence not enabled, <a id="enable-precedence" href="#enable">enable</a></div><!--<div class="alert inf">Publisher precedence enabled, <a id="disable-precedence" href="#disable">disable</a></div>--><div id="disable-precendence-div" class="alert inf ', (! opt_data.application.configuration.precedence) ? 'hide' : '', '">Publisher precedence enabled, <a id="disable-precedence" href="#disable">disable</a></div><table id="app-publishers" class="mod simpleTable" data-app="', soy.$$escapeHtml(opt_data.application.slug), '"><thead><tr><th></th><th>Source</th><th>Availability</th><th>Enabled</th></tr></thead><tbody>');
  var publisherList34 = opt_data.application.configuration.publishers;
  var publisherListLen34 = publisherList34.length;
  for (var publisherIndex34 = 0; publisherIndex34 < publisherListLen34; publisherIndex34++) {
    var publisherData34 = publisherList34[publisherIndex34];
    atlas.templates.applications.widgets.publisher({publisher: publisherData34, precedence: opt_data.application.configuration.precedence, slug: opt_data.application.slug}, output);
  }
  output.append('</tbody></table></div></div>');
  if (!opt_sb) return output.toString();
};


/**
 * @param {Object.<string, *>=} opt_data
 * @param {soy.StringBuilder=} opt_sb
 * @return {string|undefined}
 * @notypecheck
 */
atlas.templates.applications.widgets.publisher = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('\t<tr publisher="', soy.$$escapeHtml(opt_data.publisher.key), '"><td class="precedenceControl">', (opt_data.precedence) ? '<a href="#up" class="up">up</a> <a href="#down" class="down" onclick="">down</a>' : '&nbsp;', '</td><td>', soy.$$escapeHtml(opt_data.publisher.title), '</td>', (opt_data.publisher.state == 'available') ? '<td>available</td><td><input class="app-publisher" type="checkbox" name="pubkey" value="' + soy.$$escapeHtml(opt_data.publisher.key) + '" ' + ((opt_data.publisher.enabled) ? 'checked' : '') + '></td>' : (opt_data.publisher.state == 'unavailable') ? '<td><a class="request-link" href="/admin/applications/' + soy.$$escapeHtml(opt_data.slug) + '/publishers/requested?pubkey=' + soy.$$escapeHtml(opt_data.publisher.key) + '">request?</a></td><td><input class="app-publisher" type="checkbox" name="pubkey" value="' + soy.$$escapeHtml(opt_data.publisher.key) + '" disabled></td>' : '<td>' + soy.$$escapeHtml(opt_data.publisher.state) + '</td><td><input class="app-publisher" type="checkbox" name="pubkey" value="' + soy.$$escapeHtml(opt_data.publisher.key) + '" disabled></td>', '</tr>');
  if (!opt_sb) return output.toString();
};


/**
 * @param {Object.<string, *>=} opt_data
 * @param {soy.StringBuilder=} opt_sb
 * @return {string|undefined}
 * @notypecheck
 */
atlas.templates.applications.widgets.applicationLink = function(opt_data, opt_sb) {
  var output = opt_sb || new soy.StringBuilder();
  output.append('\t<a class="media app-link" href="/admin/applications/', soy.$$escapeHtml(opt_data.app.slug), '" data-id="', soy.$$escapeHtml(opt_data.app.slug), '"><span class="img"></span><span class="bd">', soy.$$escapeHtml(opt_data.app.title), '</span></a>');
  if (!opt_sb) return output.toString();
};
