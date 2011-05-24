goog.require('atlas.templates.equivalence.widgets');

$("a.probeEdit").live('click', function(){
    var a = $(this)
    $.getJSON('/system/equivalence/probes/update.json?uri='+a.closest("table").attr("id"), function(data){
        data.hideId = true;
        a.parent().html(atlas.templates.equivalence.widgets.updateProbe(data));
    });
    return false;
});

$("input[type='submit']").live('click', function(){
    var li = $(this).closest("li");
    $.ajax({
            type:"POST",
            url: "/system/equivalence/probes/update.json",
            data: $(this).closest("form").serialize(),
            success: function(data) {
                li.html(atlas.templates.equivalence.widgets.probe(data));
            }
    });
    return false;
});