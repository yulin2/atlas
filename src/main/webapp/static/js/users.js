var runSearch = function() {
	var url = window.location.pathname + "?search=" + encodeURIComponent($("#search").val());	
	window.location.href = url;
};

var redirectToApplications = function(userId) {
	var url = '/admin/users/' + userId + '/applications';	
	window.location.href = url;
};

$(document).ready(function() {
	$('.user-link').click(function() {
		redirectToApplications($(this).attr('data-userid'));
	});
	
	$("#runSearch").click(function() {
		runSearch();
	});

	$('#search').live('keypress',function(e) {
		if (e.which == 13) {
			runSearch();	
		}
	});
});