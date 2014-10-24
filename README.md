# Twitter Hashtags
## Summary
This is a pretty basic Java program that'll connect to the free Twitter sample stream (that gives a random sample of
all tweets) for a few minutes, and then prints out a list of all the hashtags it's seen. It's intended to show how
easy it is to get a simple social media app up and running, so it's stylistically not great, but hopefully gets the
point across.
## Running
### Maven
You're going to need to have Maven installed to build and run this (ok, technically you don't _need_ to have it, but I'm
only giving instructions based on having it). http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html 
gives a decent intro.
### Getting Twitter Credentials
The app logs into Twitter using OAuth credentials, which you can get by registering at https://apps.twitter.com/new.
On the "manage keys and access tokens you can get the Consumer Key and Consumer Secret, and then click on the
"Create my access token" button down the bottom to get the Access Token and Access Token secret.
### Properties
Put those four strings into config.properties.
### Actually Running
`mvn clean package exec:java`

That'll compile the code, and run the `main` method in the `Main` class. If you want to compile without running then
it's just `mvn clean package`, and if you just want to run without compiling (because you've already compiled it and
haven't made any changes) then it's just `mvn exec:java`.