var list = {
	items: [],
	init: function(){
		list.filter = $('#filter');
		list.extra = $('#filter').siblings('.extra');
		
		list.filter.bind('keyup', function(){
			list.reduce();
		});
		
		list.extra.click(function(){
			list.filter.val('');
			list.reduce();
		});
	},
	reduce: function(){
		var val = list.filter.val();
		var valLength = val.length;
		
		var filterTo = [];
		for(var i = 0, ii = list.items.length; i<ii; i++){
			if(list.items[i].title.substr(0,valLength).toLowerCase() === val.toLowerCase()){
				if(list.items[i].key){
					filterTo.push(list.items[i].key);
				} else if(list.items[i].slug){
					filterTo.push(list.items[i].slug);
				}
			}
		}
		
		$('.app-link').hide();
		for(var i = 0, ii = filterTo.length; i<ii; i++){
			$('.app-link[data-id="'+filterTo[i]+'"]').show();
		}
		
		if(valLength > 0){
			list.extra.show();
		} else {
			list.extra.hide();
		}
	},
	keyCodeMap: {
		32: {char: '&nbsp;'},
		48: {char: '0'},
		49: {char: '1'},
		50: {char: '2'},
		51: {char: '3'},
		52: {char: '4'},
		53: {char: '5'},
		54: {char: '6'},
		55: {char: '7'},
		56: {char: '8'},
		57: {char: '9'},
		65: {char: 'a'},
		66: {char: 'b'},
		67: {char: 'c'},
		68: {char: 'd'},
		69: {char: 'e'},
		70: {char: 'f'},
		71: {char: 'g'},
		72: {char: 'h'},
		73: {char: 'i'},
		74: {char: 'j'},
		75: {char: 'k'},
		76: {char: 'l'},
		77: {char: 'm'},
		78: {char: 'n'},
		79: {char: 'o'},
		80: {char: 'p'},
		81: {char: 'q'},
		82: {char: 'r'},
		83: {char: 's'},
		84: {char: 't'},
		85: {char: 'u'},
		86: {char: 'v'},
		87: {char: 'w'},
		88: {char: 'x'},
		89: {char: 'y'},
		90: {char: 'z'},
		188: {char: ','},
		222: {char: '\''}
	}
};

if(apps){
	list.items = apps;
} else if(sources !== undefined){
	list.items = sources;
}

list.init();