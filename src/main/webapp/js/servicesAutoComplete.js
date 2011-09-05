/**
 * uses jquery-ui autocomplete in fetching all service names
 */
Array.prototype.unique = function(){
    var result = [];
    var n = this.length;
    for (var i = 0; i < n; i++) {
        for (var j = i + 1; j < n; j++)
            if (this[i] === this[j])
                j = ++i;
        result.push(this[i]);
    }
    return result;

};

function split(val){
    return val.split(/,\s*/);
};

function extractLast(term){
    return split(term).pop();
};

$(document).ready(function(){

    var availableServices = allAppsDuplicates.unique();


    $("#services") // don't navigate away from the field on tab when selecting an item
.bind("keydown", function(event){
        if (event.keyCode === $.ui.keyCode.TAB &&
        $(this).data("autocomplete").menu.active) {
            event.preventDefault();
        }
    }).autocomplete({
        minLength: 0,
        source: function(request, response){
            // delegate back to autocomplete, but extract the last term
            response($.ui.autocomplete.filter(availableServices, extractLast(request.term)));
        },
        focus: function(){
            // prevent value inserted on focus
            return false;
        },
        select: function(event, ui){
            var terms = split(this.value);
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push(ui.item.value);
            // add placeholder to get the comma-and-space at the end
            buildTagsAutoComplete(terms);
            terms.push("");
            this.value = terms.join(",");
            return false;
        }
    });


});



function buildTagsAutoComplete(selectedServices){

    var chosenTagsAll = new Array();

    $.each(gluJson.entries, function(jsonKey, jsonService){
        $.each(selectedServices, function(servicesKey, chosenService){
            if (jsonService.initParameters.app == chosenService) {
                $.each(jsonService.tags, function(tagsKey, tag){
                    chosenTagsAll.push(tag);
                });
            }
        });
    });

    var chosenTags = chosenTagsAll.unique();

    $("#tags") // don't navigate away from the field on tab when selecting an item
.bind("keydown", function(event){
        if (event.keyCode === $.ui.keyCode.TAB &&
        $(this).data("autocomplete").menu.active) {
            event.preventDefault();
        }
    }).autocomplete({
        minLength: 0,
        source: function(request, response){
            // delegate back to autocomplete, but extract the last term
            response($.ui.autocomplete.filter(chosenTags, extractLast(request.term)));
        },
        focus: function(){
            // prevent value inserted on focus
            return false;
        },
        select: function(event, ui){
            var terms = split(this.value);
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push(ui.item.value);
            terms.push("");
            this.value = terms.join(",");
            return false;
        }
    });

};
function removeServicesLastComma(){


    var selectedServices = $('input[name="serviceName"]').val();
    var lastCharServices = selectedServices.charAt(selectedServices.length - 1);
    if (lastCharServices == ',') {
        $('input[name="serviceName"]').val(selectedServices.substring(0, selectedServices.length - 1));
    }

    var selectedTags = $('input[name="tags"]').val();
    var lastCharTags = selectedTags.charAt(selectedTags.length - 1);
    if (lastCharTags == ',') {
        $('input[name="tags"]').val(selectedTags.substring(0, selectedTags.length - 1));
    }
};






