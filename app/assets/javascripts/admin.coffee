bind_delete_button = () -> 
  $('#manage .btn-danger').each((index, element) ->
    $(element).click -> 
      id = $(this).attr('id').split("-")[0]
      url = '/chatroom/delete/'
      $.ajax(
        url: url+'?chatRoomId='+id
        type: 'DELETE'
        success: (result, status) -> 
          if result ? "error"
            console.log "no error"
            $('#manage .alert-container').append '<div class="alert alert-block alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button>Delete success</div>'
            $('#'+id).remove()
          else
            $('#manage #alert-container').append '<div class="alert alert-error alert-block"><button type="button" class="close" data-dismiss="alert">&times;</button>'+result.error+'</div>'
      )
      return false
  )

bind_update_button = () ->
  $('#manage .btn-primary').each((index, element) ->
    $(element).click ->
      id = $(this).attr('id').split("-")[0]
      url = '/chatroom/update/'
      name = $('#'+id+"-name").val()
      creatorId = $('#'+id+"-creatorId").val()
      chatroom = {
        id: id,
        name: name,
        creatorId: creatorId
      }
      $.ajax(
        url: url+'?chatRoomId='+id
        contentType: "application/json; charset=utf-8"
        type: 'POST'
        data: JSON.stringify(chatroom)
        success: (result, status) ->
          if result ? "error"
            console.log "no error"
            $('#manage .alert-container').append '<div class="alert alert-block alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button>Update success</div>'
            $("#"+id+" .accordion-heading a").text(name)
          else
            $('#manage #alert-container').append '<div class="alert alert-error alert-block"><button type="button" class="close" data-dismiss="alert">&times;</button>'+result.error+'</div>'

      )
      console.log(chatroom)
      return false
  )

$('#create button').click ->
  console.log "clicked"
  url ='/chatroom/create/'
  chatroom_name = $('#create input').val()
  $.ajax(
    url: url+'?name='+chatroom_name
    type: 'PUT'
    success: (result)-> 
      if result ? "error"
        $('#create .alert-container').append '<div class="alert alert-block alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button>'+result.success+'</div>'
        $('#create input').val('')
        template = $('#chatroom-template').html()
        $('#manage #chatrooms').append(Mustache.render(template, result.chatroom))
        bind_delete_button()
        bind_delete_button()
      else
        $('#create #alert-container').append '<div class="alert alert-error alert-block"><button type="button" class="close" data-dismiss="alert">&times;</button>'+result.error+'</div>'
  )
  return false

@renderChatroom = (chatrooms) -> 
  template = $('#chatroom-template').html()
  for chatroom in chatrooms
    chatroom.fullUrl = location.protocol + '//' + location.host+chatroom.inviteUrl
    $('#manage #chatrooms').append(Mustache.render(template, chatroom))
  bind_delete_button()
  bind_update_button()


