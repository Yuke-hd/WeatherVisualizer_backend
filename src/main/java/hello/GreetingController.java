package hello;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.DailyUVIndexForecast;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import net.aksingh.owmjapis.model.param.Main;
import net.aksingh.owmjapis.model.param.WeatherData;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
	public static ArrayList<CurrentWeather> cwd;
	public static ArrayList<List<DailyUVIndexForecast>> uvIndex;
	public static ArrayList<HourlyWeatherForecast> forecastList;
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	private String plainText = "";
	private String cipherText = "";
	private ArrayList<Company> companys = new ArrayList<>();

	private long last = 1571195761L;

	@CrossOrigin(origins = "http://localhost:9000")
	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(required = false, defaultValue = "World") String name) {
		System.out.println("==== in greeting ====");
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@GetMapping("/encrypt")
	public TextResponse encrypt(@RequestParam(required = true) String plainText) {
		this.cipherText = plainText + "encrypted";
		return new TextResponse(plainText);
	}


	@GetMapping("/decrypt")
	public TextResponse decrypt() {
		System.out.println(this.cipherText);
		return new TextResponse(this.cipherText);
	}

	@GetMapping("/greeting-javaconfig")
	public Greeting greetingWithJavaconfig(@RequestParam(required = false, defaultValue = "World") String name) {
		System.out.println("==== in greeting ====");
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@RequestMapping(value = "/readcompany", method = RequestMethod.GET)
	public ModelAndView httpServicePostJSONDataExample(ModelMap model) {
		for (int i = 0; i < companys.size(); i++) {
			System.out.println(companys.get(i).toString());
		}
		return new ModelAndView("httpservice_post_json");
	}

	@RequestMapping(value = "/savecompany_json", method = RequestMethod.POST)
	public @ResponseBody
	void saveCompany_JSON(@RequestBody Company company) {
		//
		// Code processing the input parameters
		//
		companys.add(company);
		System.out.println("JSON: The company name: " + company.getName() + ", Employees count: " + company.getEmployees() + ", Headoffice: " + company.getHeadoffice());
		//return "JSON: The company name: " + company.getName() + ", Employees count: " + company.getEmployees() + ", Headoffice: " + company.getHeadoffice();
	}

	@RequestMapping(value = "/time", method = RequestMethod.GET)
	public TextResponse time() throws APIException {
		long currentTime = System.currentTimeMillis() / 1000L;
		if (currentTime - last > 60L) {
			last = currentTime;
		}
		String s = String.valueOf(last);
		System.out.println(String.valueOf(last));
		System.out.println(String.valueOf(currentTime));

		return new TextResponse(s);
	}

	@RequestMapping(value = "/weather", method = RequestMethod.GET)
	public ArrayList<CurrentWeather> weather() {
		return cwd;
	}

	@RequestMapping(value = "/uvforecast/{city}", method = RequestMethod.GET)
	public List<DailyUVIndexForecast> uvforecast(@PathVariable String city) {
		Coordinate target = null;
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

		DynamoDB dynamoDB = new DynamoDB(client);

		String TableName = "50cities";


		//QueryResult queryResult = client.query(queryRequest);
		ScanRequest scanRequest = new ScanRequest()
				.withTableName(TableName);

		ScanResult result1 = client.scan(scanRequest);
		for (Map<String, AttributeValue> item : result1.getItems()) {
			if (item.get("city").toString().toUpperCase().contains(city.toUpperCase())) {
				double lat = Double.parseDouble(String.valueOf(item.get("latitude").getN()));
				double lon = Double.parseDouble(String.valueOf(item.get("longtitude").getN()));
				target = new Coordinate(lat, lon);
			}
		}
		//System.out.println(queryResult);

		System.out.println(target.toString());

		List<DailyUVIndexForecast> result = null;
		for (int i = 0; i < uvIndex.size(); i++) {
			List<DailyUVIndexForecast> forecast = uvIndex.get(i);
			for (int j = 0; j < forecast.size(); j++) {
				if (Math.abs(forecast.get(j).getLatitude() - target.getLatitude()) < 1) {
					result = forecast;
				}
			}
		}
		return result;
	}

	@RequestMapping(value = "/weatherforecast/{city}", method = RequestMethod.GET)
	public HourlyWeatherForecast weatherForecast(@PathVariable String city) {
		HourlyWeatherForecast result = null;
		for (HourlyWeatherForecast hourlyWeatherForecast : forecastList) {
			if (hourlyWeatherForecast.getCityData().getName().equalsIgnoreCase(city)) {
				result = hourlyWeatherForecast;
			}
		}
		return result;
	}

	@RequestMapping(value = "/forecastscore", method = RequestMethod.GET)
	public ArrayList forecastScore() {
		ArrayList<ForecastScore> result = new ArrayList<>();
		for (HourlyWeatherForecast hourlyWeatherForecast : forecastList) {
			List<WeatherData> weatherDataList = hourlyWeatherForecast.getDataList();
			assert weatherDataList != null;
			double avg = 0.0;
			double humidity = 0.0;
			double min = 0.0;
			double max = -273.15;
			String rain = "";
			for (WeatherData weatherData : weatherDataList) {

				Main mainData = weatherData.getMainData();
				double currentMax = mainData.getTempMax();
				double currentMin = mainData.getTempMin();
				double currentAvg = mainData.getTemp();

				max = Math.max(currentMax, max);
				min = Math.min(currentMin, min);
				avg += currentAvg;

				humidity += mainData.getHumidity();

				rain = weatherData.getWeatherList().get(0).getDescription().contains("rain") ? rain + "rain" : rain;
			}
			Integer score = calcScore(max, min, humidity, 0.0, rain);
			ForecastScore fs = new ForecastScore(hourlyWeatherForecast.getCityData().getName(), score, 0.0);
			result.add(fs);
		}
		return result;
	}

	@RequestMapping(value = "/weatherscore", method = RequestMethod.GET)
	public HashMap<String, Integer> weatherScore() {
		HashMap<String, Integer> result = new HashMap<>();
		for (CurrentWeather currentWeather : cwd) {
			Integer score = 100;
			String city = currentWeather.getCityName();
			String desc = currentWeather.getWeatherList().get(0).getDescription();
			double min = currentWeather.getMainData().getTempMin();
			double max = currentWeather.getMainData().getTempMax();
			double humidity = currentWeather.getMainData().getHumidity();
			System.out.println(city);
			List<DailyUVIndexForecast> uvforecast = uvforecast(city);
			double uvindex = 0;
			try {
				uvindex = uvforecast.get(0).getValue();
			} catch (Exception e) {

			}
			score = calcScore(max, min, humidity, uvindex, desc);
			result.put(city, score);
		}
		return result;
	}

	private Integer calcScore(double max, double min, double humidity, double uvindex, String desc) {
		int score = 100;
		if (uvindex >= 11) {
			score -= 10;
		} else if (uvindex >= 8) {
			score -= 8;
		} else if (uvindex >= 6) {
			score -= 5;
		} else if (uvindex >= 3) {
			score -= 2;
		} else if (uvindex >= 0) {
			score -= 0;
		}

		if (!desc.contains(" ")) {
			String findStr = "rain";
			int lastIndex = 0;
			int rainCount = 0;

			while (lastIndex != -1) {
				lastIndex = desc.indexOf(findStr, lastIndex);
				if (lastIndex != -1) {
					rainCount++;
					lastIndex += findStr.length();
				}
			}
			if (rainCount / 8.0 == 0) {
				score -= 0;
			} else if (rainCount / 8.0 < 0.2) {
				score -= 5;
			} else if (rainCount / 8.0 < 0.5) {
				score -= 10;
			} else {
				score -= 20;
			}
		} else if (desc.contains("raid")) {
			score -= 20;
		}


		if ((max - min) > 15) {//gap between max and min temprature
			score = score - 20;
		} else if ((max - min) > 10) {
			score = score - 10;
		}

		if (max < 27) {//rules for temprature within 27 celcius
			if (min <= 0) {
				score = score - 24;
			} else if (min <= 9) {
				score = score - 15;
			} else if (min <= 17) {
				score = score - 5;
			}
		}

		if (max >= 27) {//heat index
			if (max == 27) {
				score = score - 10;
			} else if (max == 28) {
				if (humidity <= 85) {
					score = score - 10;
				} else {
					score = score - 15;
				}
			} else if (max == 29) {
				if (humidity <= 70) {
					score = score - 10;
				} else {
					score = score - 15;
				}
			} else if (max == 30) {
				if (humidity <= 55) {
					score = score - 10;
				} else if (humidity <= 85) {
					score = score - 15;
				} else {
					score = score - 20;
				}
			} else if (max == 31) {
				if (humidity <= 45) {
					score = score - 10;
				} else if (humidity <= 75) {
					score = score - 15;
				} else {
					score = score - 20;
				}
			} else if (max == 32) {
				if (humidity <= 65) {
					score = score - 15;
				} else if (humidity <= 90) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 33) {
				if (humidity <= 55) {
					score = score - 15;
				} else if (humidity <= 80) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 34) {
				if (humidity <= 50) {
					score = score - 15;
				} else if (humidity <= 75) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 36) {
				if (humidity <= 40) {
					score = score - 15;
				} else if (humidity <= 65) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 37) {
				if (humidity <= 60) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 38) {
				if (humidity <= 55) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 39) {
				if (humidity <= 50) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 40) {
				if (humidity <= 45) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else if (max == 41) {
				if (humidity <= 40) {
					score = score - 20;
				} else {
					score = score - 26;
				}
			} else {
				score = score - 26;
			}
		}
		return score;
	}

}
