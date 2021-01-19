import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TimesCLI {
	public static void main(String[] args) {
		TimesAPIRunner runner = new TimesAPIRunner();
		runner.run();

		// try {
		// URL url = new URL("https://api.nytimes.com/svc/topstories/v2/home.json?api-key=");
		// HttpURLConnection request = (HttpURLConnection) url.openConnection();
		// request.setRequestMethod("GET");
		// System.out.println(request.getResponseCode());
		// } catch (Exception e) {System.out.println("Failed");}
	}
}

class TimesAPIRunner {
	private static final String BASE_URL = "https://api.nytimes.com/svc/topstories/v2/";
	private static final String SIGN_UP_URL = "https://developer.nytimes.com/get-started";
	private static final int API_KEY_LENGTH = 32;
	private static final Pattern API_KEY_PATTERN = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
	private static final String PROP_FILENAME = ".TimesCLI.properties";

	private Properties userData;
	private Scanner userInput;

	public TimesAPIRunner() {
		userData = new Properties();
		userInput = new Scanner(System.in);
	}

	public void run() {
		if (!loadUserData()) {
			return;
		}

		setAPIKey();
		userInput.close();
		storeUserData();
	}

	private boolean loadUserData() {
		boolean propertiesLoaded;
		try {
			userData.load(new FileInputStream(PROP_FILENAME));
			propertiesLoaded = true;
		} catch (IOException e) {
			propertiesLoaded = false;
		}

		if (!propertiesLoaded) {
			try {
				new File(PROP_FILENAME).createNewFile();
			} catch (IOException e) {
				System.out.println("ERROR: Unable to create " + PROP_FILENAME + ".");
				System.out.println("Exception message:");
				e.printStackTrace();
			}
		}

		return propertiesLoaded;
	}

	private void storeUserData() {
		try {
			userData.store(new FileWriter(PROP_FILENAME), null);
		} catch (IOException e) {
			System.out.println("ERROR: Unable to save data to " + PROP_FILENAME + ".");
			System.out.println("Exception message:");
			e.printStackTrace();
		}
	}

	private void setAPIKey() {
		if (userData.getProperty("APIKey") != null) {
			return;
		}

		System.out.println("To use TimesCLI, you need an API key from the NYT Dev Portal.\n");
		System.out.println("\t1. Visit " + SIGN_UP_URL + " to create an account.");
		System.out.println("\t2. Create a new app and enable the Top Stories API. Name the app whatever you want.");
		System.out.println("\t3. Copy your app's API key from the portal and paste it below.");

		String APIKeyInput;

		do {
			System.out.print("\nAPI Key: ");
			APIKeyInput = userInput.nextLine().trim();
		} while (!isAPIKeyValid(APIKeyInput));

		userData.setProperty("APIKey", APIKeyInput);
	}

	private boolean isAPIKeyValid(String APIKey) {
		boolean isValid = APIKey.length() == API_KEY_LENGTH;
		if (isValid) {
			isValid = !API_KEY_PATTERN.matcher(APIKey).find();
		}

		if (!isValid) {
			System.out.println("Invalid key. Please try again.");
		}

		return isValid;
	}
}
