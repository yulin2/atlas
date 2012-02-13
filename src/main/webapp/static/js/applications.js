goog.require('atlas.templates.applications.widgets');

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
			},
			error:function(textStatus) {
				console.log("fail:", textStatus);
			}
		});	
		return false;
	});
	
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

/*$("a.app-link").live('click', function(){
	var parts = $(this).attr('href').split('/');
	var appName = parts[parts.length-1];
	window.location.hash = window.location.hash + "!" + appName;
	return false;
});*/

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
	var checkbox = $(this);
	var checked =  $(this).is(":checked")
	$.ajax({
		type: checked ? "post" : "delete",
		url: "/admin/applications/"+checkbox.closest('ul').attr("data-app")+"/publishers"+(checked?"":"/"+$(this).attr("value"))+".json",
		data: ({pubkey : $(this).attr("value")}),
		success:function(responseData, textStatus, XMLHttpRequest) {
			checkbox.closest('label').stop().animate({opacity: 0.25}, 500, function() {
				checkbox.closest('label').animate({opacity: 1});
			});
		},
		error:function(textStatus) {
			checkbox.attr("checked", !checked);
			console.log("fail:", textStatus);
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

var updatePrecedence = function() {
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
        }
    });
}

$("#enable-precedence").live('click', function(){
   updatePrecedence();
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
