# Twitch Talking Enemies Zomboid

[![showcase](https://github.com/mlem/twitch-talking-enemies-zomboid/raw/0d1b8698c5927dcccad8153c666fb1e109692014/twitch-talking-enemies-zomboid.gif)](https://www.twitch.tv/mlem86)

This is a mod for Project Zomboid. It works only with storm v0.2.7 or above.
To install storm, go to https://github.com/mlem/storm and download the latest [release](https://github.com/mlem/storm/releases).

To manually install this mod, download the latest [release](https://github.com/mlem/twitch-talking-enemies-zomboid/releases) and extract it into your `%USER_HOME%\Zomboid\mods` folder.


e.g. 
```
unzip twitch-talking-enemies-0.0.3.zip -d ~/Zomboid/mods
```

Currently, this mod isn't available in Steam Workshop.

## Configuration

The mod will create an `app.properties` in `%USER_PROFILE%\Zomboid`.

The mod will ask you for your permission to read your chat, if you haven't done it yet or your token is expired. We are using the [Twitch Authentication Method](https://dev.twitch.tv/docs/authentication) for it.

The `app.properties` have a structure like this:
```
debug=false
botName=hello
channelName=https://twitch.tv/mlem86
oauthToken=<someRandomCharacters>
blacklist=someBot,anotherBot,...
```

Except the blacklist, every other property will be generated for you.

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

## Last words

You can see me playing with this mod sometimes at [my twitch channel](https://www.twitch.tv/mlem86)

