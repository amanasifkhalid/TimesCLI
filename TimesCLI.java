import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TimesCLI {
	public static void main(String[] args) {
		TimesAPIRunner runner = new TimesAPIRunner();
		runner.run();
	}
}

class TimesAPIRunner {
	private static final String BASE_URL = "https://api.nytimes.com/svc/topstories/v2/";
	private static final String SIGN_UP_URL = "https://developer.nytimes.com/get-started";
	private static final int API_KEY_LENGTH = 32;
	private static final Pattern API_KEY_PATTERN = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
	private static final String PROP_FILENAME = ".TimesCLI.properties";
	private static final String[] NYT_SECTIONS = {"Home"};

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

		if (userData.getProperty("APIKey") == null) {
			setAPIKey();
		}

		while (true) {
			String choice = selectNYTSection();
			userInput.nextLine();
			
			if (choice.isEmpty()) {
				break;
			} else if (choice.equals("reset")) {
				setAPIKey();
				continue;
			}

			int responseCode = getNYTSection(choice);
			if (responseCode != 200) {
				if (responseCode != 0) {
					System.out.println("Failed to retrieve section. Double-check your API key and try again.");
					System.out.println("Response Code: " + responseCode);
				}

				continue;
			}

			System.out.println();
		}

		finish();	
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

	private void setAPIKey() {
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
		System.out.println("Key saved.\n");
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

	private String selectNYTSection() {
		System.out.println("Please select a section to read, or reset your API key:\n");
		for (int i = 0; i < NYT_SECTIONS.length; ++i) {
			System.out.println("\t" + (i + 1) + ". " + NYT_SECTIONS[i]);
		}

		System.out.println("\t" + (NYT_SECTIONS.length + 1) + ". Reset API Key");
		System.out.println("\t" + (NYT_SECTIONS.length + 2) + ". Exit");
		int selection;

		do {
			System.out.print("\nEnter your selection [1-" + (NYT_SECTIONS.length + 2) + "]: ");

			try {
				selection = userInput.nextInt() - 1;
				if (selection == NYT_SECTIONS.length + 1) {
					return "";
				} else if (selection == NYT_SECTIONS.length) {
					return "reset";
				}else if (selection >= 0 && selection < NYT_SECTIONS.length) {
					break;
				}
			} catch (InputMismatchException e) {
				userInput.nextLine();
			}

			System.out.println("Invalid selection. Please try again.");
		} while (true);

		return NYT_SECTIONS[selection].toLowerCase();
	}

	private int getNYTSection(String section) {
		String APIKey = userData.getProperty("APIKey");
		String urlBuilder = BASE_URL + section + ".json?api-key=" + APIKey;

		try {
			URL url = new URL(urlBuilder);
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.setRequestMethod("GET");
			return request.getResponseCode();
		} catch (Exception e) {
			System.out.println("ERROR: Unable to make API call.");
			System.out.println("Exception message:");
			e.printStackTrace();
		}

		return 0;
	}

	private void finish() {
		System.out.println("\nExiting...");
		userInput.close();
		storeUserData();
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
}
