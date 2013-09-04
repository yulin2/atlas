goog.require('atlas.templates.applications.widgets');

var termsAndConditions = {};

termsAndConditions["pressassociation.com"] = ' \
<p>Press Association data is made available without any licence fee under the following conditions:</p> \
<ul> \
   <li>This is a personal or research project and has no commercial model at this stage</li> \
   <li>PA will limit the free licence to six months and may agree an extension if appropriate</li> \
   <li>PA receive updates on progress of the work</li> \
   <li>PA have an opportunity to be involved in the commercial exploitation of any product</li> \
   <li>The data is marked Copyright: Press Association</li> \
   <li>PA require us to pass on your contact details, including any company or trading name for their records</li> \
   <li>Any fees payable to Red Bee Media on behalf of ITV are paid by yourself</li> \
</ul> ';

termsAndConditions["summaries.pressassociation.com"] = termsAndConditions["pressassociation.com"];




var updatedPrecedence = false;

$(document).ready(function() {

	//$(".app-link").each(function(){
	//	$(this).attr('href', "#!/"+$(this).attr('href').split("/")[2])
	//});
	
	$("#create").submit(function() {
		var slug = $('input:text[name="slug"]').val();
		$.ajax({
			type:'POST',
			url: "/admin/applications.json",
			data: $(this).serializeArray(),
			success:function(responseData, textStatus, XMLHttpRequest) {
				addApplication(slug, responseData);
				resetCreateForm();
			},
			error:function(textStatus) {
				console.log("fail:", textStatus);
			}
		});	
		return false;
	});
	
	$("#btnAppCancel").click(function() {
		resetCreateForm();
	});
	
	if(app && app.configuration && app.configuration.precedence){
		var originalIndex = null;
		$('.js_draggable').sortable({
			revert: false,
			axis: 'y',
			containment: '.js_draggable',
			update: function(e, u){
				var item = $(u.item).attr('data-publisher');
				var diff = u.position.top - u.originalPosition.top;
				var index = null;
				var newIndex = null;
				for(var i = 0, ii = app.configuration.publishers.length; i<ii; i++){
					var pub = app.configuration.publishers[i];
					if(pub.key === item){
						index = i;
					}
				}
				newIndex = $(u.item).index();
				
				if(index !== null && newIndex !== null){
					var publisher = app.configuration.publishers.splice(index, 1);
					app.configuration.publishers.splice(newIndex, 0, publisher[0]);
				}
				
				updatedPrecedence = true;
			}
		});
		$('.js_draggable').disableSelection();
	}
	
	/*$(window).hashchange( function(){
		var loc = location.hash.substring(2)
		if( loc.length > 0 ){
			$.getJSON('/admin/applications/'+loc+'.json', function(data){
				$.colorbox({
					html: atlas.templates.applications.widgets.applicationContent(data),
					maxWidth:'500px',
					onClosed:function(){
						window.location.hash = "";	
					}
				});
			});
		}
	})
	
	$(window).hashchange();*/
});

function resetCreateForm() {
	$('.overlayBlocker').hide();
	$('input:text[name="slug"]').val("");
	$('input:text[name="title"]').val("");
}

//$("a.app-link").live('click', function(){
//	var parts = $(this).attr('href').split('/');
//	var appName = parts[parts.length-1];
//	window.location.hash = window.location.hash + "!" + appName;
//	return false;
//});

function requestPublisher(slug, pubkey, index){
    $('[name=pubkey]').val(pubkey);
    $('[name=index]').val(index);
    $('#termsAndConditions').html("");
    if (termsAndConditions[pubkey]) {
    	$('#termsAndConditions').html("<h4>Terms and conditions</h4>");
    	$('#termsAndConditions').append("<div id='tac_inner'></div>");
    	$('#tac_inner').html(termsAndConditions[pubkey]);
    }
    $('#publisherRequestForm').height($('body').height());
    $('.overlay').css({top: $('body').scrollTop() + 200});
	$('#publisherRequestForm').show();
	///admin/applications/{$slug}/publishers/requested?pubkey={$publisher.key}
//    var link = $(this);
//    var requestUrl = link.attr('href');
//    $.ajax({
//        type: "post",
//        url: requestUrl,
//        success:function(data, test, req) {
//            link.parent().html("requested");
//        }
//    })
    return false;
}

$("#sendPublisherRequest").live('click', function() {
	var requestUrl = $('#publisherRequest').attr('action');
	var data = {};
	data.pubkey = $('[name=pubkey]').val();
	data.usageType = $('[name=usageType]').val();
	data.appUrl = $('[name=appUrl]').val();
	data.reason = $('[name=reason]').val();
	var index = $('[name=index]').val();
    $.ajax({
      type: "post",
      data: data, 
      url: requestUrl,
      success:function(data, test, req) {
    		var link = $('#request-link-' + index);
    		link.parent().html("requested");
      },
      error:function(error) {
		  console.log(error.statusText);
      }
    })	

	$('#publisherRequestForm').hide();
	return false;
});


$("a.approve-link").live('click', function(){
    var link = $(this);
    var requestUrl = link.attr('href');
    var cell = link.parent();
    $.ajax({
        type: "post",
        url: requestUrl,
        success:function(data, test, req) {
            cell.empty();
        }
    })
    return false;
});

addApplication = function(slug, data) {
	var app = null;
	for(var i = 0, ii = data.applications.length; i<ii; i++){
		if(data.applications[i].slug === slug){
			app = data.applications[i];
			break;
		}
	}
	if(app !== null){
		apps.push(app);
		$('.app-link:last').after(atlas.templates.applications.widgets.applicationLink({app: app}));
	}
	$('.overlayBlocker').hide();
}

$("input.app-publisher").live('change', function(){
	var publisher = $(this).val();
	var checked =  $(this).is(":checked");
	
	for(var i = 0, ii = app.configuration.publishers.length; i<ii; i++){
		var item = app.configuration.publishers[i];
		if(item.key === publisher){
			item.enabled = checked;
		}
	}
	
	return false;
});

$('#saveApplicationSources').live('click', function(){
	var btn = $(this);
	btn.addClass('loading');
	updateEnabled(function(){
		if(updatedPrecedence){
			updatePrecedence(function(){
				btn.removeClass('loading');
			});
		} else {
			btn.removeClass('loading');
		}
	});
	return false;
});

$("form#ipaddress").live('submit', function(){
	var ipAddress = $('#addIp').val();
	$.ajax({
		type: "post",
		url: "/admin/applications/"+$(this).attr("data-app")+"/ipranges.json",
		data: {ipaddress: ipAddress},
		success:function(responseData, textStatus, XMLHttpRequest) {
			if($('#app-ips').attr('data-ips') == '0'){
				$('#app-ips').children().fadeOut(function(){
					$('#app-ips').children().remove();
					appendIp()
				});
			}else{
				appendIp();
			}
		},
		error:function(textStatus) {
			console.log(textStatus)
		}
	});
	return false;
});

appendIp = function(){
	$('#app-ips').append('<li style="display:none"><span>'+$("input[name='ipaddress']").val()+'</span><span style="display:none;opacity:0">✖</span></li>');
	$('#app-ips').children().last().fadeIn();
	$('#app-ips').attr('data-ips', $('#app-ips').children().length);
	$("input[name='ipaddress']").val("");
}

$("#app-ips li span:last-child").live('click', function(){
	var del = $(this).closest('li');
	$.ajax({
		type: "post",
		url: "/admin/applications/"+$(this).closest('ul').attr("data-app")+"/ipranges/delete.json",
		data: ({range:del.children().first().html()}),
		success:function(responseData, textStatus, XMLHttpRequest) {
			del.fadeOut(function(){
				del.remove()
				$('#app-ips').attr('data-ips', $('#app-ips').children().length)
				if($('#app-ips').children().length == 0){
					$('#app-ips').append('<li>No Addresses</li>');
				}
			});
		},
		error:function(textStatus) {
			console.log("failure")
		}
	});
});

var updatePrecedence = function(callback) {
	var precedenceCsv = "";
	for(var i = 0, ii = app.configuration.publishers.length; i<ii; i++){
		if (i > 0) {
			 precedenceCsv += ",";
		}
		precedenceCsv += app.configuration.publishers[i].key;
	};
	var url = "/admin/applications/" + app.slug + "/precedence.json";
	$.ajax({
        type: "post",
        url: url,
        data: ({'precedence': precedenceCsv}),
        success: function(data){
        	if(data.application){
        		page.redraw(data.application);
        	}
        },
        error:function(textStatus) {
            console.log("failure")
        },
        complete: function(){
        	callback();
        }
    });
    return false;
}

var updateEnabled = function(callback){
	var slug = app.slug;
	var count = 0;
	var sources = {'enabled':[], 'disabled':[]};
	var enabledSection = $(".publisherSection[data-section='available-enabled']");
	var disabledSection = $(".publisherSection[data-section='available-disabled']");
	for(var i = 0, ii = app.configuration.publishers.length; i<ii; i++){
		var enabled = app.configuration.publishers[i].enabled;
		var publisher = app.configuration.publishers[i].key;
		var available = app.configuration.publishers[i].state == "available";

		if (!available) {
			continue;
		}
		var newPub = $(".publisherLine[data-publisher='" + publisher + "']");
		if (enabled) {
			sources.enabled.push(publisher);
			// move html to new section
			if (enabledSection.find(".publisherLine[data-publisher='" + publisher + "']").length == 0) {
				enabledSection.append(newPub);
			}
		} else {
			if (disabledSection.find(".publisherLine[data-publisher='" + publisher + "']").length == 0) {
				disabledSection.append(newPub);
			}
			sources.disabled.push(publisher);
		}
	}
	var url = "/admin/applications/"+slug+"/publishers.json";
	
	$.ajax({
	    type: "POST",
		url: url,
		data: JSON.stringify(sources),
		async: false,
		success:function(responseData, textStatus, XMLHttpRequest) {
		    $('#saveApplicationSources').val("Changes saved");
		    $('#applicationLastUpdated').html(responseData.application.lastUpdated);
		    setTimeout(function() {
		       $('#saveApplicationSources').val("Save Changes");
		    }, 2000);
		},
		error:function(textStatus) {
		    console.log("fail:", textStatus);
		},
		complete: function(){
		    callback();
		}
    });
}

var disablePrecedence = function(callback) {
	
	var url = "/admin/applications/" + app.slug + "/precedenceOff.json";
	$.ajax({
        type: "post",
        url: url,
        data: ({}),
        success: function(data){
        	if(data.application){
        		page.redraw(data.application);
        		callback();
        	}
        },
        error:function(textStatus) {
            console.log("failure")
        }
    });
}

$("#enable-precedence").live('click', function(){
   updatePrecedence(function(){
	   $("#enable-precendence-div").addClass("hide");
	   $("#disable-precendence-div").removeClass("hide");
	   window.location.reload();
   });
   return false;
});

$("#disable-precedence").live('click', function(){
   disablePrecedence(function(){
	   $("#enable-precendence-div").removeClass("hide");
	   $("#disable-precendence-div").addClass("hide");
	   window.location.reload();
   });
   return false;
});

$("#app-publishers a.up").live('click', function() {
    /*var $row = $(this).closest('li'); 
    $row.prev().before($row); 
    updatePrecedence();*/
    var pub = $(this).parent().parent().index();
    if(pub !== 0){
    	var current = app.configuration.publishers[pub];
    	var replacement = app.configuration.publishers[pub-1];
		app.configuration.publishers[pub] = replacement;
		app.configuration.publishers[pub-1] = current;
		updatePrecedence();
    }
    return false;
});

$("#app-publishers a.down").live('click', function() {
    /*var $row = $(this).closest('li'); 
    $row.next().after($row)
    updatePrecedence();*/
   	var pub = $(this).parent().parent().index();
    if(pub !== app.configuration.publishers.length-1){
    	var current = app.configuration.publishers[pub];
    	var replacement = app.configuration.publishers[pub+1];
		app.configuration.publishers[pub] = replacement;
		app.configuration.publishers[pub+1] = current;
		updatePrecedence();
    }
    return false;
});

var page = {
	redraw: function(newApp){
		app = newApp;
		var publisherString = '<tbody>';
		for(var i = 0, ii = app.configuration.publishers.length; i<ii; i++){
			var publisher = app.configuration.publishers[i];
			publisherString += atlas.templates.applications.widgets.publisher({publisher: publisher, precedence: app.configuration.precedence, slug: app.slug});
		}
		publisherString += '</tbody>';
		
		$('#app-publishers tbody').replaceWith(publisherString);
	}
};

var runSearch = function() {
	var url = window.location.pathname + "?search=" + encodeURIComponent($("#search").val());	
	window.location.href = url;
};

$("#runSearch").live('click', function() {
	runSearch();
});

$('#search').live('keypress',function(e) {
	if (e.which == 13) {
		runSearch();	
	}
});

$(".addWritable").live('click', function() {
	var appSlug = $(this).attr("data-id");
	var pubId = $(this).attr("data-pub");
	var href = window.location.href;
	var url = "/admin/sources/" + pubId + "/writable/applications/add?application=" + appSlug;
	$.ajax({
		type:'POST',
		url: url,
		success:function(responseData, textStatus, XMLHttpRequest) {
			window.location.href = href;
		},
		error:function(textStatus) {
			console.log("fail:", textStatus);
		}
	});	
	return false;
});

$(".removeWritable").live('click', function() {
	var appSlug = $(this).attr("data-id");
	var pubId = $(this).attr("data-pub");
	var href = window.location.href;
	var url = "/admin/sources/" + pubId + "/writable/applications/remove?application=" + appSlug;
	$.ajax({
		type:'POST',
		url: url,
		success:function(responseData, textStatus, XMLHttpRequest) {
			window.location.href = href;
		},
		error:function(textStatus) {
			console.log("fail:", textStatus);
		}
	});	
	return false;
});
