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
  output.append('<h2>Application: ', soy.$$escapeHtml(opt_data.application.title), '</h2><h3>API Key</h3><p style="margin-left:48px">', soy.$$escapeHtml(opt_data.application.credentials.apiKey), '</p><h3>IP Addresses</h3><ul id="app-ips" class="mod" data-app="', soy.$$escapeHtml(opt_data.application.slug), '" data-ips="', soy.$$escapeHtml(opt_data.application.credentials.ipRanges.length), '">');
  var rangeList12 = opt_data.application.credentials.ipRanges;
  var rangeListLen12 = rangeList12.length;
  if (rangeListLen12 > 0) {
    for (var rangeIndex12 = 0; rangeIndex12 < rangeListLen12; rangeIndex12++) {
      var rangeData12 = rangeList12[rangeIndex12];
      output.append('<li><span>', soy.$$escapeHtml(rangeData12), '</span><span style="display:none;opacity:0">✖</span></li>');
    }
  } else {
    output.append('<li>No Addresses</li>');
  }
  output.append('</ul><form id="ipaddress" method="post" action="/admin/applications/', soy.$$escapeHtml(opt_data.application.slug), '/ipranges" data-app="', soy.$$escapeHtml(opt_data.application.slug), '"><p><label>IP Address: <input type="text" name="ipaddress" placeholder="CIDR" required /></label><input type="submit" value="Add" /></p></form><h3>Publishers</h3><ul id="app-publishers" class="mod" data-app="', soy.$$escapeHtml(opt_data.application.slug), '">', (! opt_data.application.configuration.precedence) ? '<p>Publisher precedence not enabled, <a id="enable-precedence" href="#enable" onclick="$(\'.precedenceControl\').css({\'visibility\': \'visible\'}); return false;">enable</a></p>' : '');
  var publisherList29 = opt_data.application.configuration.publishers;
  var publisherListLen29 = publisherList29.length;
  for (var publisherIndex29 = 0; publisherIndex29 < publisherListLen29; publisherIndex29++) {
    var publisherData29 = publisherList29[publisherIndex29];
    output.append('<li publisher="', soy.$$escapeHtml(publisherData29.key), '"><label style="width: 150px; float: left;"><input class="app-publisher" type="checkbox" name="pubkey" value="', soy.$$escapeHtml(publisherData29.key), '" ', (publisherData29.enabled) ? 'checked' : '', '>', soy.$$escapeHtml(publisherData29.title), '</label><p class="precedenceControl" style="', (! opt_data.application.configuration.precedence) ? 'visibility: hidden; ' : '', '"><a href="#up" class="up">up</a> <a href="#down" class="down" onclick="">down</a></p><div style="clear: both;"/></li>');
  }
  output.append('</ul>');
  if (!opt_sb) return output.toString();
};
