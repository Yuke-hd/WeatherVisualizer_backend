package hello;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.DailyUVIndexForecast;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import net.aksingh.owmjapis.model.param.ForecastData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Config {

	private ArrayList<CurrentWeather> currentWeatherList;
	private ArrayList<HourlyWeatherForecast> forecastList;
	private ArrayList<List<DailyUVIndexForecast>> uvList;
	@Scheduled(fixedRate = 3600000)
	public void weather() throws APIException, InterruptedException {
		String[] cities =cities();
		// declaring object of "OWM" class
		OWM owm = new OWM("c31c30bf92f140deeb023ccb57878bec");
		currentWeatherList = new ArrayList<>();
		uvList = new ArrayList<>();
		for (String city : cities) {
			CurrentWeather cwd = owm.currentWeatherByCityName(city);
			currentWeatherList.add(cwd);
		}
		GreetingController.cwd = currentWeatherList;
		System.out.println("Stage1 loaded");
		Thread.sleep(6000);
		for (int x = 0; x < currentWeatherList.size(); x++) {
			double lat = currentWeatherList.get(x).getCoordData().getLatitude();
			double lon = currentWeatherList.get(x).getCoordData().getLongitude();
			List<DailyUVIndexForecast> temp = owm.dailyUVIndexForecastByCoords(lat, lon);
			uvList.add(temp);
		}
		GreetingController.uvIndex = uvList;
		System.out.println("Stage2 loaded");
		Thread.sleep(6000);
		forecastList = new ArrayList<>();
		for (String city : cities) {
			HourlyWeatherForecast forecastData = owm.hourlyWeatherForecastByCityName(city);
			forecastList.add(forecastData);
		}
		GreetingController.forecastList = this.forecastList;
		System.out.println("Stage3 loaded");
	}



	public String[] cities() {
		final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		try (final S3Object s3Object = amazonS3Client.getObject("thistest111",
				"cc2/50cities.json");
		     final InputStreamReader streamReader = new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8);
		     final BufferedReader reader = new BufferedReader(streamReader)) {

			Collection<String> a = reader.lines().collect(Collectors.toSet());
			String[] foos = a.toArray(new String[a.size()]);
//			JSONObject json = new JSONObject(foos[0]);
			JSONArray jsonArray = new JSONArray(foos[0]);
			String[] cities = new String[jsonArray.length()];
			for (int i = 0; i < 49; i++) {
				JSONObject entry = (JSONObject) jsonArray.get(i);
				cities[i]= entry.getString("city");
			}
			System.out.println("City loaded");
			return cities;

		} catch (final IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

}
