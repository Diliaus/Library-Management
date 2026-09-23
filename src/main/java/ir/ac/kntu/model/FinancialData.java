package ir.ac.kntu.model;

import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.user.RegularUser;
import java.util.Map;
import java.util.List;

public class FinancialData {
    private final double totalRevenue;
    private final double totalFinesCollected;
    private final double netProfit;
    private final Map<String, Double> revenueByCategory;
    private final Map<String, Double> monthlyRevenue;
    private final int activeUsers;
    private final int blockedUsers;
    private final Map<String, Integer> itemTypeDistribution;
    private final Map<String, Integer> supporterActivity;
    private final List<String> highFineAlerts;
    private final List<LibraryItem> allItems;
    private final List<RegularUser> allUsers;

    public FinancialData(double totalRevenue, double totalFinesCollected, double netProfit,
                         Map<String, Double> revenueByCategory, Map<String, Double> monthlyRevenue,
                         int activeUsers, int blockedUsers, Map<String, Integer> itemTypeDistribution,
                         Map<String, Integer> supporterActivity, List<String> highFineAlerts,
                         List<LibraryItem> allItems, List<RegularUser> allUsers) {
        this.totalRevenue = totalRevenue;
        this.totalFinesCollected = totalFinesCollected;
        this.netProfit = netProfit;
        this.revenueByCategory = revenueByCategory;
        this.monthlyRevenue = monthlyRevenue;
        this.activeUsers = activeUsers;
        this.blockedUsers = blockedUsers;
        this.itemTypeDistribution = itemTypeDistribution;
        this.supporterActivity = supporterActivity;
        this.highFineAlerts = highFineAlerts;
        this.allItems = allItems;
        this.allUsers = allUsers;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTotalFinesCollected() {
        return totalFinesCollected;
    }

    public double getNetProfit() {
        return netProfit;
    }

    public Map<String, Double> getRevenueByCategory() {
        return revenueByCategory;
    }

    public Map<String, Double> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public int getBlockedUsers() {
        return blockedUsers;
    }

    public Map<String, Integer> getItemTypeDistribution() {
        return itemTypeDistribution;
    }

    public Map<String, Integer> getSupporterActivity() {
        return supporterActivity;
    }

    public List<String> getHighFineAlerts() {
        return highFineAlerts;
    }

    public List<LibraryItem> getAllItems() {
        return allItems;
    }

    public List<RegularUser> getAllUsers() {
        return allUsers;
    }
}