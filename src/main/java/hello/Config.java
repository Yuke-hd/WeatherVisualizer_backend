package hello;

import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.DailyUVIndexForecast;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import net.aksingh.owmjapis.model.param.ForecastData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Config {
	private final String arr[] = {"Sydney", "Melbourne", "Cairns", "Adelaide", "Brisbane", "Perth", "Canberra", "Darwin",
			"Hobart", "Gold Coast"};
	private ArrayList<CurrentWeather> currentWeatherList;
	private ArrayList<HourlyWeatherForecast> forecastList;
	private ArrayList<List<DailyUVIndexForecast>> uvList;
	@Scheduled(fixedRate = 300000)
	public void weather() throws APIException, InterruptedException {
		// declaring object of "OWM" class
		OWM owm = new OWM("c31c30bf92f140deeb023ccb57878bec");
		currentWeatherList = new ArrayList<>();
		uvList = new ArrayList<>();
		for (String city : arr) {
			CurrentWeather cwd = owm.currentWeatherByCityName(city);
			currentWeatherList.add(cwd);
		}
		GreetingController.cwd = currentWeatherList;
		Thread.sleep(30000);
		for (int x= 0;x<currentWeatherList.size();x++) {
			double lat = currentWeatherList.get(x).getCoordData().getLatitude();
			double lon = currentWeatherList.get(x).getCoordData().getLongitude();
			List<DailyUVIndexForecast> temp = owm.dailyUVIndexForecastByCoords(lat,lon);
			uvList.add(temp);
		}
		GreetingController.uvIndex=uvList;
		Thread.sleep(30000);
		forecastList = new ArrayList<>();
		for (String city : arr) {
			HourlyWeatherForecast forecastData = owm.hourlyWeatherForecastByCityName(city);
			forecastList.add(forecastData);
		}
		GreetingController.forecastList=this.forecastList;
		System.out.println(forecastList);
	}
}
