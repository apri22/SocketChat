# SocketChat

Chatting App using socket connection.

## This Project uses:
- Websocket Server by Websocket.in ([docs](https://www.websocket.in/docs))
- [Room](https://developer.android.com/topic/libraries/architecture/room)
- [RxKotlin](https://github.com/ReactiveX/RxKotlin)
- [Image Picker](https://github.com/Dhaval2404/ImagePicker)
- [Flow Layout](https://github.com/nex3z/FlowLayout)


## How to use the app
- Input your **Name** (Optional)
- **Channel ID** should be a positive integer **between 1-10000**. (Required)
- Press Start chat button
- Wait until status **Connected**
- After Connected you can received and send chat to other people connected in same channel
- Send Options Action by typing **"actions: {your_options_separated_by_comma}"** example: **actions: How,Good,Yes**


> Received chat will be recorded in local database.
