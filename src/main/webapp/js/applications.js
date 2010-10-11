goog.require('atlas.templates.applications');

$(document).ready(function() {

	$(".app-link").each(function(){
		$(this).attr('href', "#!"+$(this).attr('href'))
	});
	
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
	appList.append('<li style="display:none"><a class="app-link" href="#!/admin/applications/'+slug+'">'+title+'</a></li>');
	appList.children().last().slideDown();
	appList.attr('data-apps', appList.children().length);
}