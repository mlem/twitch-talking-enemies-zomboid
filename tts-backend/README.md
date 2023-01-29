# Text-to-speech backend

## Description

In order to reduce the load on the mod and remove any cross library problems and library order loading problems,
we've created a seperate backend service, which starts up a small webserver on your machine.

This server is implementing one method, for fetching a machine generated voice message.

## Google cloud

The products I've tested, google cloud was the most appealing. It's nice to have a seperate backend.
If you don't want to use google cloud, you can create another module with another implementation.

It just needs to implement the /tts call correctly. Everything else should work out of the box (from mod perspective).

## Setup + Authentication

To use it yourself, you need to create a google cloud account and set an environment variable.

Follow the instructions [on google cloud documentation.](https://cloud.google.com/text-to-speech/docs/libraries?hl=de#setting_up_authentication) (Link is to german version. It's pretty straight forward with this instructions. If I change the language to en, it brings you to a much more confusing passage with just links to more confusing stuff. At least that's what I've felt)

After you have done this, you should have done something like this:
```
export GOOGLE_APPLICATION_CREDENTIALS="/home/user/Downloads/service-account-file.json"
```

in your .bashrc or in your windows system variables.


### Note

Google cloud is providing this service with prices. Make sure to visit their prices page.
I have create a free trial account with a bit of money on it - to test this thing.

After 3 months this account expires and my fund gets deleted. Just to let you know, this is something what you have to pay for.
