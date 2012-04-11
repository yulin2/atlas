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
  output.append('<h1>Application: ', soy.$$escapeHtml(opt_data.application.title), '</h1><div class="line"><div class="unit size1of2"><h2>Your API Key</h2><div class="h3">', soy.$$escapeHtml(opt_data.application.credentials.apiKey), '</div></div><div class="unit size1of2 lastUnit"><h2>Allowed IP Addresses</h2><div class="alert inf mln mrn">If you would like to restrict usage of your API key to certain IP addresses or ranges you can enter them here.</div><ul id="app-ips" class="mod" data-app="', soy.$$escapeHtml(opt_data.application.slug), '" data-ips="', soy.$$escapeHtml(opt_data.application.credentials.ipRanges.length), '">');
  var rangeList12 = opt_data.application.credentials.ipRanges;
  var rangeListLen12 = rangeList12.length;
  for (var rangeIndex12 = 0; rangeIndex12 < rangeListLen12; rangeIndex12++) {
    var rangeData12 = rangeList12[rangeIndex12];
    output.append('<li><span>', soy.$$escapeHtml(rangeData12), '</span><span style="display:none;opacity:0">✖</span></li>');
  }
  output.append('</ul><form id="ipaddress" method="post" action="/admin/applications/', soy.$$escapeHtml(opt_data.application.slug), '/ipranges" data-app="', soy.$$escapeHtml(opt_data.application.slug), '" class="form"><div class="grp"><label for="addIp">IP Address</label><div class="inputHolder"><input type="text" id="addIp" name="ipaddress" placeholder="CIDR" required /></div></div><input type="submit" value="Add" class="btn pos" /></form></div></div><h2>Sources</h2><!--<div id="enable-precendence-div" class="alert inf ', (opt_data.application.configuration.precedence) ? 'hide' : '', '">Publisher precedence not enabled, <a id="enable-precedence" href="#enable">enable</a></div>--><div class="line mbl"><div class="unit size4of5"><div class="alert inf mbm">Atlas automatically matches the same programmes across different sources. With equivalence precedence turned off, we\'ll return all equivalent content as separate items in the response. With precedence turned on we\'ll merge the best data from every source into a single item, based on the order you set here.<br /><em>Hint: If you have access to Press Association data, you probably want it at the top.</em></div></div><div class="unit size1of5 lastUnit tac">', (opt_data.application.configuration.precedence) ? '<input type="button" id="disable-precedence" class="btn neg mna" value="Disable Precedence" />' : '<input type="button" id="enable-precedence" class="btn pos mna" value="Enable Precedence" />', '</div></div><input type="button" id="saveApplicationSources" class="pos btn fr" value="Save Changes" /><div id="app-publishers" class="line" data-app="', soy.$$escapeHtml(opt_data.application.slug), '" style="padding-left: 8px;"><div class="unit size1of3"><span class="h3">Source</span></div><div class="unit size1of3"><span class="h3" style="padding-left: 22px;">Availability</span></div><div class="unit size1of3 lastUnit"><span class="h3" style="padding-left: 44px;">Enabled</span></div></div><div class="js_draggable form cb">');
  var publisherList34 = opt_data.application.configuration.publishers;
  var publisherListLen34 = publisherList34.length;
  for (var publisherIndex34 = 0; publisherIndex34 < publisherListLen34; publisherIndex34++) {
    var publisherData34 = publisherList34[publisherIndex34];
    atlas.templates.applications.widgets.publisher({publisher: publisherData34, precedence: opt_data.application.configuration.precedence, slug: opt_data.application.slug, index: publisherIndex34}, output);
  }
  output.append('</div><div id="publisherRequestForm" class="overlayBlocker" style="display: none;"><div class="overlay"><a href="#" class="closeOverlay"></a><h2 class="mlm">Request access to publisher</h2><form id="publisherRequest" action="/admin/applications/', soy.$$escapeHtml(opt_data.application.slug), '/publishers/requested" method="post" class="form cf"><div class="grp"><label for="email">Your email address</label><div class="inputHolder"><input id="email" name="email" type="text" placeholder="me@example.com"  /></div></div><div class="grp"><label for="reason">Reason for request</label><div class="inputHolder"><input id="reason" name="reason" type="text" placeholder="Why you would like to access this publisher." /></div></div><input type="hidden" name="pubkey" /><input type="hidden" name="index" /><input type="submit" id="sendPublisherRequest" class="pos btn" value="Send" /><input type="button" class="neg btn fr mrm" value="Cancel" onclick="$(\'#publisherRequestForm\').hide();" /></form></div></div>');
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
  output.append('\t<div class="line pr dragItem" data-publisher="', soy.$$escapeHtml(opt_data.publisher.key), '">', (opt_data.precedence) ? '<span class="extra inf"><span class="icn move"></span></span>' : '', '<div class="dragItemContent"><div class="unit size1of3 b">', soy.$$escapeHtml(opt_data.publisher.title), '</div>', (opt_data.publisher.state == 'available') ? '<div class="unit size1of3">Available</div><div class="unit size1of3 lastUnit"><input class="app-publisher" type="checkbox" name="pubkey" value="' + soy.$$escapeHtml(opt_data.publisher.key) + '" ' + ((opt_data.publisher.enabled) ? 'checked' : '') + '></div>' : (opt_data.publisher.state == 'unavailable') ? '<div class="unit size1of3"><a id="request-link-' + opt_data.index + '" class="request-link" href="javascript:requestPublisher(\'' + soy.$$escapeHtml(opt_data.slug) + '\',\'' + soy.$$escapeHtml(opt_data.publisher.key) + '\', ' + soy.$$escapeHtml(opt_data.index) + ')">Request Access</a></div><div class="unit size1of3 lastUnit"><input class="app-publisher" type="checkbox" name="pubkey" value="' + soy.$$escapeHtml(opt_data.publisher.key) + '" disabled></div>' : '<div class="unit size1of3">' + soy.$$escapeHtml(opt_data.publisher.state) + '</div><div class="unit size1of3 lastUnit"><input class="app-publisher" type="checkbox" name="pubkey" value="' + soy.$$escapeHtml(opt_data.publisher.key) + '" disabled></div>', '</div></div>');
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
  output.append('\t<div class="line app-link"><div class="unit size1of4"><a href="/admin/applications/', soy.$$escapeHtml(opt_data.app.slug), '" data-id="', soy.$$escapeHtml(opt_data.app.slug), '">', soy.$$escapeHtml(opt_data.app.title), '</a></div><div class="unit size1of4">', soy.$$escapeHtml(opt_data.app.slug), '</div><div class="unit size1of4">', soy.$$escapeHtml(opt_data.app.credentials.apiKey), '</div><div class="unit size1of4 lastUnit">-</div></div><!--<a class="media app-link" href="/admin/applications/', soy.$$escapeHtml(opt_data.app.slug), '" data-id="', soy.$$escapeHtml(opt_data.app.slug), '"><span class="img"></span><span class="bd">', soy.$$escapeHtml(opt_data.app.title), '</span></a>-->');
  if (!opt_sb) return output.toString();
};
