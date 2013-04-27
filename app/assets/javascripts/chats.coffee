sendMessage = () -> 
  chatSocket.send(JSON.stringify(
    {text: $("#talk").val()}
  ))
  $("#talk").val('')

receiveEvent = () ->
  data = JSON.parse(event.data)

  if data.error 
    chatSocket.close()
    $("#onError span").text(data.error)
    $("#onError").show()
    return
  else 
    $("#onChat").show()

  el = $('<div class="message"><span></span><p></p></div>')
  $("span", el).text(data.user)
  $("p", el).text(data.message)
  $(el).addClass(data.kind)
  if data.user == '@user.name' 
    $(el).addClass('me')
  $('#messages').append(el)

  $("#members").html('') 
  $(data.activeMembers).each(()->
    $("#members").append('<li>' + this + '</li>')
  )


handleReturnKey = (e) ->
  if e.charCode == 13 || e.keyCode == 13
    e.preventDefault()
    sendMessage()

$("#talk").keypress(handleReturnKey)  

chatSocket.onmessage = receiveEvent
