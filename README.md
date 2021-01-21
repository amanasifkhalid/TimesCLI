# TimesCLI
Perhaps you already live in your terminal window, like me. Why waste precious CPU cycles by opening your browser just to get a sense of the news? TimesCLI delivers the top headlines on *The New York Times* front page straight to your terminal.
## Prereqs
Your computer must have Java installed in order to run TimesCLI. You may install Java from [here](https://www.java.com/en/download/).
To check if Java is already installed, run the following command in your terminal:
```
java -version
```
If a version number is outputted, you're ready to install TimesCLI!

If you're sure Java is installed but the above command doesn't work, you may need to add Java to your PATH. There are multiple guides like [this one](https://explainjava.com/java-path/) to help you do this.
## Installing and Running
1. Download the two `.jar` files from the latest release on the right. You may also clone the entire repository if you wish to read the source code.
2. In a terminal window, navigate to the directory containing the downloaded files.
3. Run the following command:
```
java -jar timescli.jar
```
4. Follow the directions provided by the CLI.
## API Key Setup
In order to use TimesCLI, you must first register an API key through the [New York Times Dev Portal](https://developer.nytimes.com/get-started). The process is simple:
1. Navigate to the above link and create an account.
2. Create a new app and enable the Top Stories API. Name the app whatever you want.
3. Copy your app's API key from the portal. The first time you run TimesCLI, you will be prompted to paste your key in.
At this point, you're good to go!
## FAQ
**Can I read full NYT articles with TimesCLI?**

No. Like all readers, you need a subscription to read the *Times*. TimesCLI is not and does not attempt to be a loophole around this. Support your journalists!

**I've noticed some grammar issues (like missing apostrophes) in article titles and abstracts. Are these mistakes?**

No, the NYT did not goof up. :)

To avoid garbling text when outputting API data, TimesCLI removes certain characters (like apostrophes) from its output. These omissions usually aren't significant enough to change any meaning, but you can always view original articles online via their corresponding links.
## License
[MIT](https://choosealicense.com/licenses/mit/)
