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
	
	$(window).hashchange( function(){
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
	
	$(window).hashchange();
});

$("a.app-link").live('click', function(){
	var parts = $(this).attr('href').split('/');
	var appName = parts[parts.length-1];
	window.location.hash = window.location.hash + "!" + appName;
	return false;
});

addApplication = function(slug, data) {
	var app = null;
	$.each(data.applications, function(i,appl) {
		if (appl.slug == slug) {
			app = appl;
		}
	});
	var slug = app.slug;
	var title= app.title;
	var appList = $('#applications-list')
	if (appList.attr('data-apps') == 0) {
		appList.children().slideUp().remove();
	}
	appList.append('<li style="display:none"><a class="app-link" href="/admin/applications/'+slug+'">'+title+'</a></li>');
	appList.children().last().slideDown();
	appList.attr('data-apps', appList.children().length);
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
	$.ajax({
		type: "post",
		url: "/admin/applications/"+$(this).attr("data-app")+"/ipranges.json",
		data: $(this).serializeArray(),
		success:function(responseData, textStatus, XMLHttpRequest) {
			if($('#app-ips').attr('data-ips') == '0'){
				$('#app-ips').children().fadeOut(function(){
					$('#app-ips').children().remove();
					appendIp()
				});
			}else{
				appendIp();
			}
			$("input[name='ipaddress']").val("");
		},
		error:function(textStatus) {
			console.log("failure")
		}
	});
	return false;
});

appendIp = function(){
	$('#app-ips').append('<li style="display:none"><span>'+$("input[name='ipaddress']").val()+'</span><span style="display:none;opacity:0">✖</span></li>');
	$('#app-ips').children().last().fadeIn();
	$('#app-ips').attr('data-ips', $('#app-ips').children().length)
}

$("#app-ips li").live('mouseover', function(){
	$(this).css({padding: '5px'});
	$(this).children().last().css({display:"inline"}).stop().animate({opacity:1});
});

$("#app-ips li").live('mouseout', function(){
	var cont = $(this);
	$(this).children().last().stop().animate({opacity:0}, function(){
		$(this).css({display:"none"});
		cont.css({padding: '5px 21px 5px 5px'});
	});
});

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