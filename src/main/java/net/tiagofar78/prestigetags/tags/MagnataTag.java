package net.tiagofar78.prestigetags.tags;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.tiagofar78.prestigetags.PrestigeTags;
import net.tiagofar78.prestigetags.managers.ConfigManager;
import net.tiagofar78.prestigetags.objects.Payment;

public class MagnataTag extends PrestigeTag {
	
	private static final int TICKS_IN_SECOND = 20;
	
	private String _serverSecretKey;
	
	@Override
	public void registerTag() {
		_serverSecretKey = getServerSecretKey();
		if (_serverSecretKey == null) {
			Bukkit.getLogger().info("THERE WAS A PROBLEM TRYING TO REGISTER MAGNATA TAG! Write your server secret key in the ServerSecretKey.txt file! You may have to create it");
			return;
		}
		
		ConfigManager config = ConfigManager.getInstance();
		
		runScheduler(config.getMagnataUpdateTimeSeconds());
	}
	
	private void runScheduler(final int delay) {
		Bukkit.getScheduler().runTaskLater(PrestigeTags.getPrestigeTags(), new Runnable() {
			
			@Override
			public void run() {
				updateTagHolder();
				
				runScheduler(delay);
			}
		}, delay * TICKS_IN_SECOND);
	}
	
	@Override
	public void updateTagHolder() {		
		String magnataName = getMagnata();
		if (magnataName == null) {
			return;
		}
		
		ConfigManager config = ConfigManager.getInstance();
		
		String previousMagnata = config.getPreviousMagnataName();
		config.setNewMagnata(magnataName);
		
		if (!config.shouldUpdateMagnataForSameHolder() && previousMagnata.equals(magnataName)) {
			return;
		}
		
		List<String> commands = config.getMagnataUpdateCommands();
		
		Server server = Bukkit.getServer();
		for (String command : commands) {
			server.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{PLAYER}", magnataName).replace("{PREVPLAYER}", previousMagnata));
		}
	}
	
	public String getMagnata() {
		List<Payment> payments = getAllPayments();
		if (payments == null) {
			Bukkit.getLogger().info("THERE WAS A PROBLEM TRYING TO CONNECT TO TEBEX API! Make sure you placed the right secret key in your ServerSecretKey.txt file!");
			return null;
		}
		
		List<Object> currentMonthPayments = payments.stream().filter(wasMadeInCurrentMonth).collect(Collectors.toList());
		
		Hashtable<String, Double> playersPaymentsAmount = new Hashtable<String, Double>();
		for (Object paymentO : currentMonthPayments) {
			Payment payment = (Payment) paymentO;
			
			String buyerName = payment.getBuyerName();
			double currentSpentAmount = playersPaymentsAmount.containsKey(buyerName) ? playersPaymentsAmount.get(buyerName) : 0;
			
			playersPaymentsAmount.put(buyerName, currentSpentAmount + payment.getAmount());
		}
		
		String currentMagnata = null;
		double currentMagnataMoney = 0;
		for (Entry<String, Double> entry : playersPaymentsAmount.entrySet()) {
			if (entry.getValue() > currentMagnataMoney) {
				currentMagnata = entry.getKey();
				currentMagnataMoney = entry.getValue();
			}
		}
		
		return currentMagnata;
	}
	
	private List<Payment> getAllPayments() {
		List<Payment> payments = new ArrayList<Payment>();
		int lastPage = 2;
		int pageNumber = 1;
		
		try {
            String apiUrl = "https://plugin.tebex.io/payments";

            while (pageNumber <= lastPage) {
            	URL url = new URL(apiUrl + "?paged=" + pageNumber);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                connection.setRequestProperty("X-Tebex-Secret", _serverSecretKey);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return null;
                }
                
                JSONObject pagePayments = getPagePayments(connection);
                lastPage = pagePayments.getInt("last_page");
                
                int paymentsPerPage = pagePayments.getInt("per_page");
                int pagePaymentsAmount = pageNumber == lastPage ? pagePayments.getInt("total") - paymentsPerPage*(lastPage - 1) : paymentsPerPage;

                payments.addAll(getPaymentsDetails(pagePaymentsAmount, pagePayments.getJSONArray("data")));
                
                connection.disconnect();

                pageNumber++;
            }
            
        } catch (IOException | JSONException e ) {
            e.printStackTrace();
        }
		
		return payments;
	}
	
	private List<Payment> getPaymentsDetails(int paymentsToProcess, JSONArray array) throws JSONException {
		List<Payment> payments = new ArrayList<Payment>();
		
		for (int i = 0; i < paymentsToProcess; i++) {
			JSONObject JSONPayment = array.getJSONObject(i);
			
			String buyerName = JSONPayment.getJSONObject("player").getString("name");
			double amount = JSONPayment.getDouble("amount");
			String date = JSONPayment.getString("date");
			
			payments.add(new Payment(amount, buyerName, date));
		}
		
		return payments;
	}
	
	private JSONObject getPagePayments(HttpURLConnection connection) throws JSONException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        
        return new JSONObject(response.toString());
	}
	
	private Predicate<Payment> wasMadeInCurrentMonth = new Predicate<Payment>() {
		@Override
		public boolean test(Payment payment) {
			GregorianCalendar calendar = new GregorianCalendar();
			int currentMonth = calendar.get(Calendar.MONTH) + 1;
			int currentYear = calendar.get(Calendar.YEAR);
			
			return payment.getDate().getMonth().getValue() == currentMonth && payment.getDate().getYear() == currentYear;
		}
	};
	
	private String getServerSecretKey() {
		File file = new File(PrestigeTags.getPrestigeTags().getDataFolder(), "ServerSecretKey.txt");
		if (!file.exists()) {
			return null;
		}
		
        Scanner myReader = null;
		try {
			myReader = new Scanner(file);
	        if (!myReader.hasNextLine()) {
		        myReader.close();
	        	return null;
	        }
	        
	        myReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
        
		String secretKey = myReader.nextLine();
		
        return secretKey;
	}

}
