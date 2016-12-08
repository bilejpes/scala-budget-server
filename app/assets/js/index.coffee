$ ->
    $.get "/pairs", (keys, values) ->
        $.each keys, (key, value) ->
            $("#keys").append $("<li>").text "#{key} -> #{value}"