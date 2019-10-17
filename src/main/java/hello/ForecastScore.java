package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ForecastScore {
	private String city;
	private double score;
	private double std;

	public ForecastScore() {
	}

	public ForecastScore(String city, double score, double std) {
		this.city = city;
		this.score = score;
		this.std = std;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public double getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public double getStd() {
		return std;
	}

	public void setStd(double std) {
		this.std = std;
	}
}
