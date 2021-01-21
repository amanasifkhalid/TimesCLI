package timescli;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.StringBuilder;

import java.net.HttpURLConnection;
import java.net.URL;

import java.time.LocalDate;

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
	private static final String PROP_FILENAME = ".TimesCLI.properties";
	private static final int API_KEY_LENGTH = 32;
	private static final int REQUEST_TIMEOUT = 15000;
	private static final Pattern API_KEY_PATTERN = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
	private static final String[] NYT_SECTIONS = {
		"Home", "Arts", "Automobiles", "Books", "Business", "Fashion", "Food", "Health",
		"Magazine", "Movies", "Opinion", "Politics", "Science", "Sports", "Technology",
		"Theater", "Travel", "US", "World"
	};

	private HttpURLConnection connection;
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

		System.out.println();

		if (userData.getProperty("APIKey") == null) {
			setAPIKey();
		}

		if (userData.getProperty("articleLimit") == null) {
			setArticleLimit();
		}

		while (true) {
			String choice = selectNYTSection();
			userInput.nextLine();
			
			if (choice.isEmpty()) {
				break;
			} else if (choice.equals("reset")) {
				setAPIKey();
				continue;
			} else if (choice.equals("limit")) {
				setArticleLimit();
				continue;
			}

			String currDate = LocalDate.now().toString();
			int responseCode = 200;

			if (!currDate.equals(userData.getProperty(choice + "LastCallDate"))) {
				responseCode = getNYTSection(choice, currDate);
			}

			if (responseCode != 200) {
				continue;
			}

			System.out.println();
			readArticles(choice);
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

	private void setArticleLimit() {
		System.out.println("Please enter the number of articles to display per call, or enter 0 for no limit.");
		int userLimit;

		do {
			System.out.print("\nLimit: ");

			try {
				userLimit = userInput.nextInt();
			} catch (InputMismatchException e) {
				userLimit = -1;
				userInput.nextLine();
			}

			if (userLimit >= 0) {
				break;
			}

			System.out.println("Invalid limit. Please try again.");
		} while (true);

		userData.setProperty("articleLimit", "" + userLimit);
	}

	private String selectNYTSection() {
		System.out.println("Please select a section to read, or configure a setting:\n");
		for (int i = 0; i < NYT_SECTIONS.length; ++i) {
			System.out.println("\t" + (i + 1) + ". " + NYT_SECTIONS[i]);
		}

		System.out.println("\n\t" + (NYT_SECTIONS.length + 1) + ". Reset API Key");
		System.out.println("\t" + (NYT_SECTIONS.length + 2) + ". Set Article Limit");
		System.out.println("\t" + (NYT_SECTIONS.length + 3) + ". Exit");
		int selection;

		do {
			System.out.print("\nEnter your selection [1-" + (NYT_SECTIONS.length + 3) + "]: ");

			try {
				selection = userInput.nextInt() - 1;
				if (selection == NYT_SECTIONS.length + 2) {
					return "";
				} else if (selection == NYT_SECTIONS.length + 1) {
					return "limit";
				} else if (selection == NYT_SECTIONS.length) {
					return "reset";
				} else if (selection >= 0 && selection < NYT_SECTIONS.length) {
					break;
				}
			} catch (InputMismatchException e) {
				userInput.nextLine();
			}

			System.out.println("Invalid selection. Please try again.");
		} while (true);

		return NYT_SECTIONS[selection].toLowerCase();
	}

	private int getNYTSection(String section, String date) {
		String APIKey = userData.getProperty("APIKey");
		String urlBuilder = BASE_URL + section + ".json?api-key=" + APIKey;
		int response;

		try {
			URL url = new URL(urlBuilder);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(REQUEST_TIMEOUT);
			connection.connect();

			response = connection.getResponseCode();
		} catch (Exception e) {
			System.out.println("ERROR: Unable to make API call.");
			System.out.println("Exception message:");
			e.printStackTrace();
			return -1;
		}

		if (response != 200) {
			System.out.println("Failed to retrieve section. Double-check your API key and try again.");
			System.out.println("Response Code: " + response);
		} else {
			try {
				storeNYTSection(section, date);
			} catch (IOException e) {
				System.out.println("ERR: Unable to parse returned data.");
				System.out.println("Exception message:");
				e.printStackTrace();
				response = -1;
			}
		}

		return response;
	}

	private void storeNYTSection(String section, String date) throws IOException {
		userData.setProperty(section + "LastCallDate", date);
		Scanner jsonReader = new Scanner(new InputStreamReader(connection.getInputStream()));
		StringBuilder builder = new StringBuilder();

		while (jsonReader.hasNextLine()) {
			builder.append(jsonReader.nextLine());
		}

		jsonReader.close();
		userData.setProperty(section, builder.toString());
	}

	private void readArticles(String section) {
		JSONObject jsonData = (JSONObject) JSONValue.parse(userData.getProperty(section));
		JSONArray articles = (JSONArray) jsonData.get("results");

		String sectionTitle = section.substring(0, 1).toUpperCase() + section.substring(1);
		System.out.println(userData.getProperty(section + "LastCallDate"));
		System.out.println("Today's Headlines in " + sectionTitle + ":\n");
		JSONObject currArticle;
		String output;

		int articleLimit = Integer.parseInt(userData.getProperty("articleLimit"));
		int numArticles;
		if (articleLimit == 0) {
			numArticles = articles.size();
		} else {
			numArticles = articles.size() < articleLimit ? articles.size() : articleLimit;
		}

		for (int i = 0; i < numArticles; ++i) {
			System.out.print("\t" + (i + 1) + ". ");

			currArticle = (JSONObject) articles.get(i);
			output = ((String) currArticle.get("title")).replaceAll("\\P{Print}", "");

			if (output.length() > 70) {
				System.out.println(output.substring(0, 70) + "...\n");
			} else {
				System.out.println(output + "\n");
			}
		}

		System.out.println("\t" + (++numArticles) + ". Back");

		int userChoice;

		do {
			System.out.print("\nChoose an article for more info, or go back [1-" + numArticles + "]: ");

			try {
				userChoice = userInput.nextInt();
			} catch (InputMismatchException e) {
				userInput.nextLine();
				userChoice = 0;
			}

			if (userChoice < 1 || userChoice > numArticles) {
				System.out.println("Invalid selection. Please try again.");
			} else if (userChoice == numArticles) {
				break;
			} else {
				currArticle = (JSONObject) articles.get(userChoice - 1);

				output = ((String) currArticle.get("title")).replaceAll("\\P{Print}", "");
				System.out.println("\nTitle: " + output);

				output = ((String) currArticle.get("byline")).replaceAll("\\P{Print}", "");
				System.out.println(output);

				System.out.println("Full Article: " + currArticle.get("short_url"));

				output = ((String) currArticle.get("abstract")).replaceAll("\\P{Print}", "");
				System.out.println("\nAbstract:\n" + output);
			}
		} while (true);
	}

	private void finish() {
		System.out.println("\nExiting...");
		userInput.close();
		storeUserData();

		if (connection != null) {
			connection.disconnect();
		}
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
