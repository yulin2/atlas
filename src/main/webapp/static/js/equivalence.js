goog.require('atlas.templates.equivalence.widgets');

$("a.probeEdit").live('click', function(){
    var a = $(this)
    $.getJSON('/system/equivalence/probes/update.json?uri='+a.closest("table").attr("id"), function(data){
        data.hideId = true;
        a.parent().html(atlas.templates.equivalence.widgets.updateProbe(data));
    });
    return false;
});

$("input.probeUpdate").live('click', function(){
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

$("input.resultProbeUpdate").live('click', function(){
    var button = $(this);
    button.closest("th").css("background-color","#f00");
    var update = {"expect":"","notExpect":""};
    $(this).closest("table").find("tbody").children().each(function(index,row){
        var selected = $(row).find("input:checked");
        update[selected.attr("value")] += selected.attr("name") + ","
    });
    var data = "uri="+button.attr("id");
    $.each(update, function(k,v){
        if(k != "unkown") {
            data += "&" +  k + "=" + v.substring(0, v.length - 1);
        }
    });
    console.log(update);
    $.ajax({
        type:"POST",
        url: "/system/equivalence/probes/update.json",
        data: data,
        success: function(data) {
            button.closest("th").css("background-color","#fff");
        }
    });
    return false;
});

printDesc = function(desc) {
    console.log(desc)
}