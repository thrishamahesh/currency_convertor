import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.DecimalFormat;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Date;
import java.util.*;
import java.time.LocalDate;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

class SimpleJSON {
    private String jsonString;
    
    public SimpleJSON(String json) {
        this.jsonString = json.trim();
    }
    
    public double getDouble(String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = jsonString.indexOf(searchKey);
        if (startIndex == -1) return 0.0;
        
        startIndex += searchKey.length();
        while (startIndex < jsonString.length() && Character.isWhitespace(jsonString.charAt(startIndex))) {
            startIndex++;
        }
        
        int endIndex = startIndex;
        while (endIndex < jsonString.length()) {
            char c = jsonString.charAt(endIndex);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) break;
            endIndex++;
        }
        
        try {
            return Double.parseDouble(jsonString.substring(startIndex, endIndex));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    public boolean getBoolean(String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = jsonString.indexOf(searchKey);
        if (startIndex == -1) return false;
        
        return jsonString.substring(startIndex).contains("true");
    }
}

public class ConverterFinal {
    private JTextArea resultArea;
    private JTextField amountTextField;
    private JComboBox<String> fromCurrencyComboBox;
    private JComboBox<String> toCurrencyComboBox;
    private JLabel currentRateLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConverterFinal().createUI());
    }

    private void createUI() {
        JFrame frame = new JFrame("Currency Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder("Currency Conversion"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Amount input
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountTextField = new JTextField("100", 15);
        amountTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(amountTextField, gbc);

        // From currency
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1;
        String[] currencies = {"USD", "INR", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF"};
        fromCurrencyComboBox = new JComboBox<>(currencies);
        fromCurrencyComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(fromCurrencyComboBox, gbc);

        // To currency
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 1;
        toCurrencyComboBox = new JComboBox<>(currencies);
        toCurrencyComboBox.setSelectedIndex(1); // Default to INR
        toCurrencyComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(toCurrencyComboBox, gbc);

        // Current rate display
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        currentRateLabel = new JLabel("Current Rate: --");
        currentRateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        currentRateLabel.setForeground(Color.BLUE);
        inputPanel.add(currentRateLabel, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton convertButton = new JButton("Convert");
        convertButton.setFont(new Font("Arial", Font.BOLD, 14));
        convertButton.setBackground(new Color(70, 130, 180));
        convertButton.setForeground(Color.WHITE);
        
        JButton clearButton = new JButton("Clear Results");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton refreshRateButton = new JButton("Refresh Rate");
        refreshRateButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton chartButton = new JButton("Show Trend Graph");
        chartButton.setFont(new Font("Arial", Font.PLAIN, 14));
        chartButton.setBackground(new Color(34, 139, 34));
        chartButton.setForeground(Color.WHITE);

        buttonPanel.add(convertButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshRateButton);
        buttonPanel.add(chartButton);

        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(new TitledBorder("Conversion Results"));
        
        resultArea = new JTextArea(12, 50);
        resultArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(248, 248, 255));
        resultArea.setText("Welcome to Currency Converter!\n" +
                          "=================================\n" +
                          "Enter amount and select currencies, then click Convert.\n\n");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        convertButton.addActionListener(e -> performConversion());
        clearButton.addActionListener(e -> {
            resultArea.setText("Results cleared.\n" +
                             "================\n\n");
            currentRateLabel.setText("Current Rate: --");
        });
        refreshRateButton.addActionListener(e -> updateCurrentRate());
        chartButton.addActionListener(e -> showTrendGraph());
        
        // Auto-update rate when currencies change
        fromCurrencyComboBox.addActionListener(e -> updateCurrentRate());
        toCurrencyComboBox.addActionListener(e -> updateCurrentRate());

        // Layout
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(resultsPanel);

        frame.add(mainPanel, BorderLayout.CENTER);
        
        // Status bar
        JLabel statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Initial rate update
        updateCurrentRate();
    }

    private void performConversion() {
        try {
            double amount = Double.parseDouble(amountTextField.getText().trim());
            if (amount <= 0) {
                appendToResults("Error: Amount must be greater than 0\n\n");
                return;
            }
            
            String from = fromCurrencyComboBox.getSelectedItem().toString();
            String to = toCurrencyComboBox.getSelectedItem().toString();

            if (from.equals(to)) {
                appendToResults("Error: Source and target currencies cannot be the same\n\n");
                return;
            }

            appendToResults("Converting " + amount + " " + from + " to " + to + "...\n");
            
            double rate = getRealTimeRate(from, to);
            if (rate <= 0) {
                appendToResults("Error: Could not fetch exchange rate\n\n");
                return;
            }
            
            double result = amount * rate;
            
            // Insert record to database (commented out to avoid DB dependency issues)
            // insertConversionRecord(from, to, amount, result);

            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormat rateFormat = new DecimalFormat("#,##0.0000");
            
            appendToResults("Exchange Rate: 1 " + from + " = " + rateFormat.format(rate) + " " + to + "\n");
            appendToResults("Result: " + df.format(amount) + " " + from + " = " + df.format(result) + " " + to + "\n");
            appendToResults("Conversion completed at: " + new Date() + "\n");
            appendToResults("----------------------------------------\n\n");
            
        } catch (NumberFormatException ex) {
            appendToResults("Error: Please enter a valid number for amount\n\n");
        } catch (Exception ex) {
            appendToResults("Error: " + ex.getMessage() + "\n\n");
            ex.printStackTrace();
        }
    }

    private void updateCurrentRate() {
        String from = fromCurrencyComboBox.getSelectedItem().toString();
        String to = toCurrencyComboBox.getSelectedItem().toString();
        
        if (from.equals(to)) {
            currentRateLabel.setText("Current Rate: 1.0000");
            return;
        }
        
        SwingWorker<Double, Void> worker = new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {
                return getRealTimeRate(from, to);
            }
            
            @Override
            protected void done() {
                try {
                    double rate = get();
                    DecimalFormat df = new DecimalFormat("#,##0.0000");
                    currentRateLabel.setText("Current Rate: 1 " + from + " = " + df.format(rate) + " " + to);
                } catch (Exception e) {
                    currentRateLabel.setText("Current Rate: Error fetching rate");
                }
            }
        };
        worker.execute();
    }

    private void appendToResults(String text) {
        resultArea.append(text);
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    public static double getRealTimeRate(String from, String to) {
        try {
            String apiKey = "28af3abeb2b5de2c91b56450";
            String urlStr = "https://v6.exchangerate-api.com/v6/" + apiKey + "/pair/" + from + "/" + to;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            int code = conn.getResponseCode();
            if (code != 200) throw new RuntimeException("HTTP Error: " + code);

            Scanner sc = new Scanner(url.openStream());
            StringBuilder json = new StringBuilder();
            while (sc.hasNext()) json.append(sc.nextLine());
            sc.close();

            SimpleJSON obj = new SimpleJSON(json.toString());
            return obj.getDouble("conversion_rate");
        } catch (Exception e) {
            System.err.println("Error fetching exchange rate: " + e.getMessage());
            return 0.0;
        }
    }

    private void showTrendGraph() {
        String from = fromCurrencyComboBox.getSelectedItem().toString();
        String to = toCurrencyComboBox.getSelectedItem().toString();
        
        if (from.equals(to)) {
            JOptionPane.showMessageDialog(null, "Please select different currencies for trend analysis.");
            return;
        }
        
        appendToResults("Fetching historical data for " + from + "/" + to + " trend...\n");
        
        SwingWorker<Map<String, Double>, Void> worker = new SwingWorker<Map<String, Double>, Void>() {
            @Override
            protected Map<String, Double> doInBackground() throws Exception {
                return getHistoricalRates(from, to);
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Double> historicalRates = get();
                    if (historicalRates.isEmpty()) {
                        appendToResults("Error: Could not fetch historical data\n\n");
                        return;
                    }
                    
                    new CurrencyTrendChart(historicalRates, from, to).setVisible(true);
                    appendToResults("Trend graph displayed successfully\n\n");
                } catch (Exception e) {
                    appendToResults("Error creating trend graph: " + e.getMessage() + "\n\n");
                }
            }
        };
        worker.execute();
    }

    private Map<String, Double> getHistoricalRates(String from, String to) {
        Map<String, Double> rates = new LinkedHashMap<>();
        
        try {
            // Get data for the last 30 days
            for (int i = 29; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.toString();
                
                String apiKey = "28af3abeb2b5de2c91b56450";
                String urlStr = "https://v6.exchangerate-api.com/v6/" + apiKey + "/history/" + from + "/" + date.getYear() + "/" + date.getMonthValue() + "/" + date.getDayOfMonth();
                
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(3000);
                    conn.connect();

                    int code = conn.getResponseCode();
                    if (code == 200) {
                        Scanner sc = new Scanner(url.openStream());
                        StringBuilder json = new StringBuilder();
                        while (sc.hasNext()) json.append(sc.nextLine());
                        sc.close();

                        // Parse the JSON response to get the rate for the target currency
                        String jsonStr = json.toString();
                        String searchPattern = "\"" + to + "\":";
                        int startIndex = jsonStr.indexOf(searchPattern);
                        
                        if (startIndex != -1) {
                            startIndex += searchPattern.length();
                            while (startIndex < jsonStr.length() && Character.isWhitespace(jsonStr.charAt(startIndex))) {
                                startIndex++;
                            }
                            
                            int endIndex = startIndex;
                            while (endIndex < jsonStr.length()) {
                                char c = jsonStr.charAt(endIndex);
                                if (c == ',' || c == '}' || Character.isWhitespace(c)) break;
                                endIndex++;
                            }
                            
                            if (endIndex > startIndex) {
                                double rate = Double.parseDouble(jsonStr.substring(startIndex, endIndex));
                                rates.put(dateStr, rate);
                            }
                        }
                    }
                    
                    // Small delay to avoid rate limiting
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.println("Error fetching data for date " + dateStr + ": " + e.getMessage());
                    // Continue with next date
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error in getHistoricalRates: " + e.getMessage());
        }
        
        return rates;
    }

}

// Custom Chart Panel Class
class CurrencyTrendChart extends JFrame {
    private Map<String, Double> data;
    private String fromCurrency;
    private String toCurrency;
    private double minRate;
    private double maxRate;
    
    public CurrencyTrendChart(Map<String, Double> data, String from, String to) {
        this.data = data;
        this.fromCurrency = from;
        this.toCurrency = to;
        
        // Calculate min and max rates
        Collection<Double> rates = data.values();
        this.minRate = Collections.min(rates);
        this.maxRate = Collections.max(rates);
        
        setupUI();
    }
    
    private void setupUI() {
        setTitle(fromCurrency + "/" + toCurrency + " Exchange Rate Trend (Last 30 Days)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Chart panel
        ChartPanel chartPanel = new ChartPanel();
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.setBackground(Color.WHITE);
        
        DecimalFormat df = new DecimalFormat("#,##0.0000");
        double currentRate = data.values().stream().reduce((first, second) -> second).orElse(0.0);
        double changeFromStart = currentRate - data.values().iterator().next();
        double changePercent = (changeFromStart / data.values().iterator().next()) * 100;
        
        JLabel infoLabel = new JLabel(String.format(
            "<html><b>Current Rate:</b> %s | <b>30-Day Range:</b> %s - %s | <b>Change:</b> %s (%.2f%%)</html>",
            df.format(currentRate),
            df.format(minRate),
            df.format(maxRate),
            (changeFromStart >= 0 ? "+" : "") + df.format(changeFromStart),
            changePercent
        ));
        
        infoLabel.setForeground(changeFromStart >= 0 ? new Color(34, 139, 34) : Color.RED);
        infoPanel.add(infoLabel);
        
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    // Custom panel for drawing the chart
    class ChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (data.isEmpty()) return;
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int padding = 60;
            int graphWidth = width - 2 * padding;
            int graphHeight = height - 2 * padding;
            
            // Draw background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            // Draw grid
            g2d.setColor(new Color(230, 230, 230));
            g2d.setStroke(new BasicStroke(1));
            
            // Horizontal grid lines
            for (int i = 0; i <= 10; i++) {
                int y = padding + (graphHeight * i / 10);
                g2d.drawLine(padding, y, width - padding, y);
            }
            
            // Vertical grid lines
            int dataSize = data.size();
            for (int i = 0; i <= 6; i++) {
                int x = padding + (graphWidth * i / 6);
                g2d.drawLine(x, padding, x, height - padding);
            }
            
            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
            g2d.drawLine(padding, padding, padding, height - padding); // Y-axis
            
            // Draw data points and line
            if (dataSize > 1) {
                g2d.setColor(new Color(70, 130, 180));
                g2d.setStroke(new BasicStroke(3));
                
                Double[] rates = data.values().toArray(new Double[0]);
                String[] dates = data.keySet().toArray(new String[0]);
                
                int[] xPoints = new int[dataSize];
                int[] yPoints = new int[dataSize];
                
                for (int i = 0; i < dataSize; i++) {
                    xPoints[i] = padding + (graphWidth * i / (dataSize - 1));
                    yPoints[i] = height - padding - (int)((rates[i] - minRate) / (maxRate - minRate) * graphHeight);
                }
                
                // Draw line
                for (int i = 0; i < dataSize - 1; i++) {
                    g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
                }
                
                // Draw points
                g2d.setColor(new Color(255, 69, 0));
                for (int i = 0; i < dataSize; i++) {
                    g2d.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
                }
            }
            
            // Draw labels
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            
            // Y-axis labels (rates)
            DecimalFormat df = new DecimalFormat("#0.0000");
            for (int i = 0; i <= 10; i++) {
                double rate = minRate + (maxRate - minRate) * i / 10;
                int y = height - padding - (graphHeight * i / 10);
                g2d.drawString(df.format(rate), 10, y + 4);
            }
            
            // X-axis labels (dates)
            String[] dates = data.keySet().toArray(new String[0]);
            for (int i = 0; i < Math.min(7, dates.length); i++) {
                int index = i * (dates.length - 1) / 6;
                if (index < dates.length) {
                    String date = dates[index];
                    String shortDate = date.substring(5); // MM-DD format
                    int x = padding + (graphWidth * i / 6);
                    g2d.drawString(shortDate, x - 15, height - padding + 15);
                }
            }
            
            // Draw title
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String title = fromCurrency + " to " + toCurrency + " Exchange Rate Trend";
            FontMetrics fm = g2d.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2d.drawString(title, (width - titleWidth) / 2, 25);
            
            // Draw axis labels
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Date", width / 2 - 10, height - 5);
            
            // Rotate for Y-axis label
            Graphics2D g2dRotated = (Graphics2D) g2d.create();
            g2dRotated.rotate(-Math.PI / 2, 15, height / 2);
            g2dRotated.drawString("Exchange Rate (" + toCurrency + ")", -30, height / 2);
            g2dRotated.dispose();
        }
    }
}