# Twitch Talking Enemies Zomboid

[![showcase](https://github.com/mlem/twitch-talking-enemies-zomboid/raw/0d1b8698c5927dcccad8153c666fb1e109692014/twitch-talking-enemies-zomboid.gif)](https://www.twitch.tv/mlem86)

This is a mod for Project Zomboid. It works only with storm v0.2.5 or above.
To install storm, go to https://github.com/mlem/storm and download the latest [release](https://github.com/mlem/storm/releases).

To install this mod, download the latest [release](https://github.com/mlem/twitch-talking-enemies-zomboid/releases) and extract it into your `%USER_HOME%\Zomboid\mods` folder.


e.g. 
```
unzip twitch-talking-enemies-0.0.3.zip -d ~/Zomboid/mods
```

You can see me playing with this mod sometimes at [my twitch channel](https://www.twitch.tv/mlem86)

## Configuration

In the extracted mod folder you have a `app.properties` file.
Enter your `channelName` and `oauthToken`.

To get an oauthToken, visit in a browser the url [https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=f1sjgf9y0ytdx2re1oapwfs7l11lh3&redirect_uri=http://localhost&scope=chat%3Aread+chat%3Aedit](https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=f1sjgf9y0ytdx2re1oapwfs7l11lh3&redirect_uri=http://localhost&scope=chat%3Aread+chat%3Aedit) to get an access token.

You will be redirected to an empty page, but in the url you can find a parameter called `access_token`.

e.g. 
```
http://localhost/#access_token=<someRandomCharacters>&scope=chat%3Aread+chat%3Aedit&token_type=bearer
```

The `oauthToken` is needed for the chatbot to read your chat on your behalf.

The `app.properties` will look like this then.
```
debug=false
botName=hello
channelName=https://twitch.tv/mlem86
oauthToken=<someRandomCharacters>
blacklist=someBot,anotherBot,...
```

## Properties explained

### debug

 Zombies say some debug output, debug in logging is enabled.
 
### botName

The IRC Name which is chosen, has no influence on the twitch-chat.

### channelName

The channel name this mod is listening to.

### oauthToken

Used to authenticate yourself with the IRC Channel.

### blacklist

A comma seperated list of blacklisted names. If a user or bot with such a name writes something in chat, it will not
be sayed by a zombie.