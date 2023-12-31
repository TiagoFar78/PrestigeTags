package net.tiagofar78.prestigetags.objects;

import java.time.LocalDateTime;

public class Payment {
	
	private double _amount;
	private String _buyerName;
	private LocalDateTime _date;
	
	public Payment(double amount, String buyerName, String date) {
		_amount = amount;
		_buyerName = buyerName;
		_date = toDate(date);
	}
	
	private LocalDateTime toDate(String s) {
		return LocalDateTime.parse(s.substring(0, 19));
	}
	
	public double getAmount() {
		return _amount;
	}
	
	public String getBuyerName() {
		return _buyerName;
	}
	
	public LocalDateTime getDate() {
		return _date;
	}

}
