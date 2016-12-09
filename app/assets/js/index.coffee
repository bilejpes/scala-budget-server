$ ->
    $.get "/api/pairs", (keys, values) ->
        $.each keys, (key, value) ->
            $("#records").append $("<li>").text "#{key} -> #{value}"