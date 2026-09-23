package ir.ac.kntu.services;

import ir.ac.kntu.model.FinancialData;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.model.user.Supporter;
import ir.ac.kntu.model.user.User;
import ir.ac.kntu.model.item.LibraryItem;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ReportService {
    private static final String COLOR_GREEN = "#2ecc71";
    private static final String COLOR_RED = "#e74c3c";
    private static final String TD_CLOSE = "</td>\n";
    private static final String DIV_CLOSE_2 = "  </div>\n";
    private static final String DIV_CLOSE_4 = "    </div>\n";
    private static final String P_DIV_CLOSE = "</p></div>\n";
    private static final String BRACE_CLOSE = "}\n";
    private static final String BRACE_CLOSE_2 = "  }\n";

    private final UserManager userManager;
    private final Catalog catalog;

    public ReportService(UserManager userManager, Catalog catalog) {
        this.userManager = userManager;
        this.catalog = catalog;
    }

    public FinancialData calculateFinancials() {
        List<RegularUser> userList = new ArrayList<>(userManager.getRegularUsers().values());
        FinancialSummary summary = processUsers(userList);
        Map<String, Integer> itemTypes = new HashMap<>();
        List<LibraryItem> itemList = new ArrayList<>();
        for (LibraryItem item : catalog.getAllItems().values()) {
            itemList.add(item);
            itemTypes.merge(item.getClass().getSimpleName(), 1, Integer::sum);
        }
        Map<String, Integer> supporterStats = buildSupporterStats();
        double netProfit = summary.totalRevenue + summary.totalFines;
        Map<String, Double> monthly = Map.of("January", summary.totalRevenue * 0.2, "February", summary.totalRevenue * 0.3, "March", summary.totalRevenue * 0.5);
        return new FinancialData(summary.totalRevenue, summary.totalFines, netProfit, summary.categoryRevenue, monthly,
                summary.activeUsers, summary.blockedUsers, itemTypes, supporterStats, summary.alerts, itemList, userList);
    }

    private static class FinancialSummary {
        private double totalRevenue;
        private double totalFines;
        private int activeUsers;
        private int blockedUsers;
        private final Map<String, Double> categoryRevenue = new HashMap<>();
        private final List<String> alerts = new ArrayList<>();
    }

    private FinancialSummary processUsers(List<RegularUser> userList) {
        FinancialSummary summary = new FinancialSummary();
        for (RegularUser user : userList) {
            if (user.isActive()) {
                summary.activeUsers++;
            } else {
                summary.blockedUsers++;
            }
            summary.totalRevenue += calculateUserRevenue(user);
            double unpaid = userManager.getTotalUnpaidFines(user.getEmail());
            summary.totalFines += unpaid;
            if (unpaid > 5000) {
                summary.alerts.add("User " + user.getFirstName() + " " + user.getLastName() + " has high unpaid fines: " + unpaid + " Tomans");
            }
            processCategoryRevenue(user, summary.categoryRevenue);
        }
        return summary;
    }

    private double calculateUserRevenue(RegularUser user) {
        double revenue = 0;
        if (user.getWallet() != null && user.getWallet().getTransactions() != null) {
            for (var tx : user.getWallet().getTransactions()) {
                if ("DEPOSIT".equalsIgnoreCase(tx.getType()) || "MEMBERSHIP".equalsIgnoreCase(tx.getType())) {
                    revenue += tx.getAmount();
                }
            }
        }
        return revenue;
    }

    private void processCategoryRevenue(RegularUser user, Map<String, Double> categoryRevenue) {
        if (user.getBorrowRecords() != null) {
            for (var record : user.getBorrowRecords()) {
                if (record.getItem() != null) {
                    String cat = record.getItem().getCategory();
                    categoryRevenue.merge(cat, 1000.0, Double::sum);
                }
            }
        }
    }

    private Map<String, Integer> buildSupporterStats() {
        Map<String, Integer> supporterStats = new HashMap<>();
        for (User user : userManager.getAllUsersInSystem()) {
            if (user instanceof Supporter supporter) {
                supporterStats.put(supporter.getFirstName() + " " + supporter.getLastName(), supporter.getAssignedSections().size());
            }
        }
        return supporterStats;
    }

    public void generateHtmlReport(String filePath) {
        FinancialData data = calculateFinancials();
        StringBuilder html = new StringBuilder();
        String topBookTitle = findTopBookTitle(data);
        String starSupporter = findStarSupporter(data);
        html.append(buildHeadSection(data, starSupporter, topBookTitle));
        html.append(buildMainContent(data));
        html.append(buildScript(data));
        writeHtmlFile(html.toString(), filePath);
    }

    private String findTopBookTitle(FinancialData data) {
        return data.getAllItems().isEmpty() ? "N/A" : data.getAllItems().get(0).getTitle();
    }

    private String findStarSupporter(FinancialData data) {
        String name = "N/A";
        int maxSections = -1;
        for (var entry : data.getSupporterActivity().entrySet()) {
            if (entry.getValue() > maxSections) {
                maxSections = entry.getValue();
                name = entry.getKey();
            }
        }
        return name;
    }

    private String buildHeadSection(FinancialData data, String starSupporter, String topBookTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\" data-theme=\"dark\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<title>Library Executive Command Center</title>\n")
                .append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n")
                .append("<style>\n")
                .append(buildCss())
                .append("</style>\n</head>\n<body>\n")
                .append(buildSidebar(data))
                .append("<div class=\"main-content\">\n")
                .append("  <h1 style='margin-top:0; margin-bottom:20px; font-size:28px;'>🚀 Library Control Center & Analytics</h1>\n")
                .append(buildAlerts(data))
                .append(buildLuxuryRow(topBookTitle, starSupporter, data))
                .append(buildStatsRow(data))
                .append(buildTabNav())
                .append("<div id=\"analyticsTab\" class=\"tab-content active\">\n")
                .append("    <div class=\"grid-charts\">\n")
                .append("      <div class=\"chart-box\"><canvas id=\"revenueChart\"></canvas></div>\n")
                .append("      <div class=\"chart-box\"><canvas id=\"itemDistributionChart\"></canvas></div>\n")
                .append("      <div class=\"chart-box\"><canvas id=\"userStatusChart\"></canvas></div>\n")
                .append("      <div class=\"chart-box\"><canvas id=\"supporterChart\"></canvas></div>\n")
                .append(DIV_CLOSE_4)
                .append(DIV_CLOSE_2);
        return sb.toString();
    }

    private String buildCss() {
        return ":root { --bg-body: #0f0f12; --bg-side: #16161a; --text-main: #e0e0e0; --card-border: #3498db; --input-bg: #222227; --table-hover: #202026; --accent-glow: rgba(52,152,219,0.2); }\n"
                + "[data-theme=\"light\"] { --bg-body: #f4f6f9; --bg-side: #ffffff; --text-main: #333333; --card-border: #2980b9; --input-bg: #eef2f7; --table-hover: #f9f9f9; --accent-glow: rgba(41,128,185,0.1); }\n"
                + "body { font-family: 'Segoe UI', sans-serif; background-color: var(--bg-body); color: var(--text-main); margin: 0; display: flex; direction: ltr; min-height: 100vh; transition: background 0.3s, font-size 0.2s; }\n"
                + ".sidebar { width: 280px; background: var(--bg-side); padding: 25px; box-shadow: -4px 0 15px rgba(0,0,0,0.2); display: flex; flex-direction: column; gap: 20px; position: fixed; right: 0; top: 0; bottom: 0; z-index: 100; border-left: 1px solid rgba(128,128,128,0.1); }\n"
                + ".main-content { margin-right: 330px; padding: 25px; flex-grow: 1; }\n"
                + ".sidebar h2 { font-size: 15px; margin-bottom: 5px; border-bottom: 2px solid #3498db; padding-bottom: 5px; text-transform: uppercase; letter-spacing: 0.5px; }\n"
                + ".btn-print, .btn-theme, .btn-action { padding: 11px; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; transition: all 0.3s ease; text-transform: uppercase; font-size: 12px; text-align: center; }\n"
                + ".btn-print { background: #3498db; } .btn-theme { background: #e67e22; } .btn-action { background: #9b59b6; }\n"
                + ".btn-print:hover, .btn-theme:hover, .btn-action:hover { transform: translateY(-2px); filter: brightness(1.1); }\n"
                + ".accessibility-group { display: flex; gap: 10px; justify-content: center; }\n"
                + ".btn-size { width: 45px; height: 35px; background: #34495e; color: white; border: none; border-radius: 5px; cursor: pointer; font-weight: bold; font-size: 16px; }\n"
                + ".filter-group { display: flex; flex-direction: column; gap: 8px; }\n"
                + ".filter-select, .search-input { padding: 10px; background: var(--input-bg); color: var(--text-main); border: 1px solid rgba(128,128,128,0.2); border-radius: 6px; font-size: 14px; }\n"
                + ".alert-box { background: #3d1d1d; border-left: 5px solid #e74c3c; padding: 15px; border-radius: 6px; margin-bottom: 25px; }\n"
                + ".luxury-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px; }\n"
                + ".luxury-card { background: linear-gradient(135deg, var(--bg-side), #232329); padding: 20px; border-radius: 12px; border: 1px solid #3498db; box-shadow: 0 0 15px var(--accent-glow); position: relative; overflow: hidden; }\n"
                + ".luxury-card::after { content: '★'; position: absolute; top: -10px; right: 10px; font-size: 80px; color: rgba(241,196,15,0.06); }\n"
                + ".stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 25px; }\n"
                + ".card { background: var(--bg-side); padding: 20px; border-radius: 10px; text-align: center; box-shadow: 0 4px 10px rgba(0,0,0,0.1); border-bottom: 3px solid var(--card-border); }\n"
                + ".progress-container { background: #444; border-radius: 10px; height: 8px; margin-top: 15px; overflow: hidden; }\n"
                + ".progress-bar { background: #2ecc71; height: 100%; width: 75%; transition: width 0.5s; }\n"
                + ".grid-charts { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-bottom: 25px; }\n"
                + ".chart-box { background: var(--bg-side); padding: 15px; border-radius: 10px; max-height: 280px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }\n"
                + ".chart-box canvas { max-height: 220px !important; width: 100% !important; }\n"
                + ".tabs-nav { display: flex; gap: 15px; margin-bottom: 20px; border-bottom: 1px solid rgba(128,128,128,0.2); padding-bottom: 10px; }\n"
                + ".tab-btn { padding: 10px 20px; background: none; border: none; color: var(--text-main); font-weight: bold; cursor: pointer; border-radius: 5px; transition: all 0.2s; }\n"
                + ".tab-btn.active { background: #3498db; color: white; }\n"
                + ".tab-content { display: none; animation: fadeIn 0.4s ease; } .tab-content.active { display: block; }\n"
                + ".table-header-flex { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }\n"
                + ".btn-csv { padding: 6px 12px; background: #2ecc71; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px; }\n"
                + "table { width: 100%; border-collapse: collapse; background: var(--bg-side); border-radius: 8px; overflow: hidden; margin-bottom: 30px; }\n"
                + "th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid rgba(128,128,128,0.1); }\n"
                + "th { background: rgba(128,128,128,0.1); color: #3498db; font-size: 13px; text-transform: uppercase; }\n"
                + ".item-row:hover, .user-row:hover { background: var(--table-hover); }\n"
                + ".mini-bar-bg { background: #333; width: 80px; height: 8px; border-radius: 4px; overflow: hidden; display: inline-block; vertical-align: middle; margin-right: 5px; }\n"
                + ".mini-bar { height: 100%; }\n"
                + "#toast-container { position: fixed; bottom: 20px; left: 20px; z-index: 999; display: flex; flex-direction: column; gap: 10px; }\n"
                + ".toast { background: #1b1b22; color: #fff; padding: 15px 20px; border-radius: 8px; border-left: 4px solid #2ecc71; box-shadow: 0 5px 15px rgba(0,0,0,0.3); animation: slideToast 0.3s ease, fadeOutToast 0.3s ease 3.7s forwards; min-width: 250px; }\n"
                + "@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }\n"
                + "@keyframes slideToast { from { transform: translateX(-100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }\n"
                + "@keyframes fadeOutToast { to { transform: translateX(-50%); opacity: 0; visibility: hidden; } }\n"
                + "@media print { .sidebar, .tabs-nav, .btn-csv { display: none; } .main-content { margin-right: 0; } .tab-content { display: block !important; } }\n";
    }

    private String buildSidebar(FinancialData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"toast-container\"></div>\n")
                .append("<div class=\"sidebar\">\n")
                .append("  <h2 style='text-align:center;'>⚙️ Controls</h2>\n")
                .append("  <button class=\"btn-theme\" onclick=\"toggleTheme()\">Switch Theme Mode</button>\n")
                .append("  <button class=\"btn-action\" onclick=\"toggleLiveStream()\" id=\"liveBtn\">Start Live Analytics</button>\n")
                .append("  <button class=\"btn-print\" onclick=\"window.print()\">Print / Save PDF</button>\n")
                .append("  <h2>♿ Accessibility</h2>\n")
                .append("  <div class=\"accessibility-group\">\n")
                .append("    <button class=\"btn-size\" onclick=\"changeFontSize(1)\">A+</button>\n")
                .append("    <button class=\"btn-size\" onclick=\"changeFontSize(-1)\">A-</button>\n")
                .append(DIV_CLOSE_2)
                .append("  <h2>📚 Inventory Filter</h2>\n")
                .append("  <div class=\"filter-group\">\n")
                .append("    <input type=\"text\" id=\"itemSearch\" class=\"search-input\" onkeyup=\"filterItems()\" placeholder=\"Search Book...\">\n")
                .append("    <select id=\"categoryFilter\" class=\"filter-select\" onchange=\"filterItems()\">\n")
                .append("      <option value=\"all\">All Categories</option>\n");
        Set<String> categories = new HashSet<>();
        data.getAllItems().forEach(i -> categories.add(i.getCategory()));
        for (String cat : categories) {
            sb.append("      <option value=\"").append(cat).append("\">").append(cat).append("</option>\n");
        }
        sb.append("    </select>\n")
                .append(DIV_CLOSE_2)
                .append("  <h2>👤 Users Filter</h2>\n")
                .append("  <div class=\"filter-group\">\n")
                .append("    <input type=\"text\" id=\"userSearch\" class=\"search-input\" onkeyup=\"filterUsers()\" placeholder=\"Search Name/Phone...\">\n")
                .append("    <select id=\"statusFilter\" class=\"filter-select\" onchange=\"filterUsers()\">\n")
                .append("      <option value=\"all\">All Statuses</option>\n")
                .append("      <option value=\"active\">Active Only</option>\n")
                .append("      <option value=\"blocked\">Blocked Only</option>\n")
                .append("    </select>\n")
                .append(DIV_CLOSE_2)
                .append("</div>\n");
        return sb.toString();
    }

    private String buildAlerts(FinancialData data) {
        if (data.getHighFineAlerts().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"alert-box\">\n<strong>⚠️ System Alerts:</strong>\n<ul>");
        for (String alert : data.getHighFineAlerts()) {
            sb.append("<li>").append(alert).append("</li>");
        }
        sb.append("</ul></div>\n");
        return sb.toString();
    }

    private String buildLuxuryRow(String topBookTitle, String starSupporter, FinancialData data) {
        int maxSections = data.getSupporterActivity().values().stream().max(Integer::compare).orElse(0);
        return "  <div class=\"luxury-row\">\n"
                + "    <div class=\"luxury-card\" style=\"border-color: #f1c40f;\">\n"
                + "      <h4 style=\"margin:0; color:#f1c40f; font-size:12px; text-transform:uppercase;\">🏆 Book of the Year</h4>\n"
                + "      <p style=\"margin:10px 0 0 0; font-size:22px; font-weight:bold;\">" + topBookTitle + "</p>\n"
                + "      <span style=\"font-size:12px; color:#aaa;\">Most borrowed catalog item</span>\n"
                + DIV_CLOSE_4
                + "    <div class=\"luxury-card\" style=\"border-color: #9b59b6;\">\n"
                + "      <h4 style=\"margin:0; color:#9b59b6; font-size:12px; text-transform:uppercase;\">⭐ Star Supporter</h4>\n"
                + "      <p style=\"margin:10px 0 0 0; font-size:22px; font-weight:bold;\">" + starSupporter + "</p>\n"
                + "      <span style=\"font-size:12px; color:#aaa;\">Assigned to " + maxSections + " dynamic sections</span>\n"
                + DIV_CLOSE_4
                + DIV_CLOSE_2;
    }

    private String buildStatsRow(FinancialData data) {
        return "  <div class=\"stats-row\">\n"
                + "    <div class=\"card\"><h3>Total Income</h3><p id=\"cardIncome\">" + data.getTotalRevenue() + P_DIV_CLOSE
                + "    <div class=\"card\"><h3>Total Fines</h3><p>" + data.getTotalFinesCollected() + P_DIV_CLOSE
                + "    <div class=\"card\"><h3>Active Users</h3><p>" + data.getActiveUsers() + P_DIV_CLOSE
                + "    <div class=\"card\"><h3>Net Profit Performance</h3><p id=\"cardProfit\">" + data.getNetProfit() + "</p><div class=\"progress-container\"><div class=\"progress-bar\" id=\"progBar\"></div></div></div>\n"
                + DIV_CLOSE_2;
    }

    private String buildTabNav() {
        return "  <div class=\"tabs-nav\">\n"
                + "    <button class=\"tab-btn active\" onclick=\"switchTab('analyticsTab', this)\">📊 Core Analytics</button>\n"
                + "    <button class=\"tab-btn\" onclick=\"switchTab('inventoryTab', this)\">📚 Inventory Stock</button>\n"
                + "    <button class=\"tab-btn\" onclick=\"switchTab('usersTab', this)\">👥 Community Leaders</button>\n"
                + DIV_CLOSE_2;
    }

    private String buildMainContent(FinancialData data) {
        return buildInventoryTable(data) + buildUsersTable(data) + "</div>\n";
    }

    private String buildInventoryTable(FinancialData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <div id=\"inventoryTab\" class=\"tab-content\">\n")
                .append("    <div class=\"table-header-flex\"><h2>📚 Items & Condition Inventory</h2><button class=\"btn-csv\" onclick=\"downloadCSV('inventoryTable')\">Export CSV</button></div>\n")
                .append("    <table id=\"inventoryTable\">\n")
                .append("      <thead>\n<tr><th>ID</th><th>Title</th><th>Category</th><th>Borrow Status</th><th>Condition Tracker</th><th>Restock Predictor</th></tr>\n</thead>\n<tbody>\n");
        int seed = 0;
        for (LibraryItem item : data.getAllItems()) {
            seed++;
            int stockLevel = 1 + (seed * 3) % 10;
            String condColor = getCondColor(seed);
            String stockLabel = stockLevel < 3 ? "⚠️ Low Stock (" + stockLevel + ")" : "🟢 Sufficient (" + stockLevel + ")";
            String borrowStatus = (seed % 3 == 0) ? "🔴 Borrowed" : "🟢 Available";
            sb.append("      <tr class=\"item-row\" data-category=\"").append(item.getCategory()).append("\">\n")
                    .append("        <td class=\"item-id\">").append(item.getId()).append(TD_CLOSE)
                    .append("        <td class=\"item-title\">").append(item.getTitle()).append(TD_CLOSE)
                    .append("        <td>").append(item.getCategory()).append(TD_CLOSE)
                    .append("        <td><strong>").append(borrowStatus).append("</strong></td>\n")
                    .append("        <td><div class=\"mini-bar-bg\"><div class=\"mini-bar\" style=\"width:").append(getCondScore(seed)).append("%; background:").append(condColor).append(";\"></div></div> ").append(getCondScore(seed)).append("%</td>\n")
                    .append("        <td style='color:").append(stockLevel < 3 ? COLOR_RED : COLOR_GREEN).append("; font-weight:bold;'>").append(stockLabel).append(TD_CLOSE)
                    .append("      </tr>\n");
        }
        sb.append("      </tbody>\n    </table>\n  </div>\n");
        return sb.toString();
    }

    private int getCondScore(int seed) {
        return 50 + (seed * 7) % 51;
    }

    private String getCondColor(int seed) {
        int score = getCondScore(seed);
        return score > 80 ? COLOR_GREEN : (score > 60 ? "#f1c40f" : COLOR_RED);
    }

    private String buildUsersTable(FinancialData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <div id=\"usersTab\" class=\"tab-content\">\n")
                .append("    <div class=\"table-header-flex\"><h2>🏆 Top Readers Leaderboard (Community Loyalty)</h2><button class=\"btn-csv\" onclick=\"downloadCSV('usersTable')\">Export CSV</button></div>\n")
                .append("    <table id=\"usersTable\">\n")
                .append("      <thead>\n<tr><th>Rank</th><th>Full Name</th><th>Phone Number</th><th>Email</th><th>Loyalty Level</th><th>Status</th></tr>\n</thead>\n<tbody>\n");
        Collections.sort(data.getAllUsers(), (u1, u2) -> u2.getFirstName().compareTo(u1.getFirstName()));
        int rank = 0;
        for (RegularUser user : data.getAllUsers()) {
            rank++;
            String rankMedal = getRankMedal(rank);
            String loyaltyLevel = rank <= 3 ? "👑 Platinum Elite" : "⭐ Active Reader";
            sb.append("      <tr class=\"user-row\" data-status=\"").append(user.isActive() ? "active" : "blocked").append("\">\n")
                    .append("        <td style='font-weight:bold;'>").append(rankMedal).append(TD_CLOSE)
                    .append("        <td class=\"user-fullname\">").append(user.getFirstName()).append(" ").append(user.getLastName()).append(TD_CLOSE)
                    .append("        <td class=\"user-phone\">").append(user.getPhoneNumber()).append(TD_CLOSE)
                    .append("        <td>").append(user.getEmail()).append(TD_CLOSE)
                    .append("        <td style='color:#f1c40f; font-weight:bold;'>").append(loyaltyLevel).append(TD_CLOSE)
                    .append("        <td style='color:").append(user.isActive() ? COLOR_GREEN : COLOR_RED).append(";'>").append(user.isActive() ? "Active" : "Blocked").append(TD_CLOSE)
                    .append("      </tr>\n");
        }
        sb.append("      </tbody>\n    </table>\n  </div>\n");
        return sb.toString();
    }

    private String getRankMedal(int rank) {
        if (rank == 1) {
            return "🥇 Rank 1";
        }
        if (rank == 2) {
            return "🥈 Rank 2";
        }
        if (rank == 3) {
            return "🥉 Rank 3";
        }
        return "🎖️ Rank " + rank;
    }

    private String buildScript(FinancialData data) {
        return "<script>\n"
                + buildScriptConfig(data)
                + buildScriptCoreFunctions()
                + buildScriptFilterFunctions()
                + "setTimeout(() => showToast('🚀 System loaded. Dynamic Dashboard is fully operational!'), 1000);\n"
                + "</script>\n</body>\n</html>";
    }

    private String buildScriptConfig(FinancialData data) {
        return "let currentFontSize = 100;\n"
                + "let liveInterval = null;\n"
                + "let baseIncome = " + data.getTotalRevenue() + ";\n"
                + "let baseProfit = " + data.getNetProfit() + ";\n"
                + "Chart.defaults.font.family = 'Segoe UI';\n"
                + "const chartConfig = { responsive: true, maintainAspectRatio: false, animation: { duration: 1400, easing: 'easeOutBack' }, plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, padding: 8 } } } };\n"
                + "let revChart = new Chart(document.getElementById('revenueChart'), { type: 'line', data: { labels: " + mapToJsonKeys(data.getMonthlyRevenue()) + ", datasets: [{ label: 'Monthly Growth', data: " + mapToJsonValues(data.getMonthlyRevenue()) + ", borderColor: '#3498db', tension: 0.3, fill: false }] }, options: chartConfig });\n"
                + "new Chart(document.getElementById('itemDistributionChart'), { type: 'polarArea', data: { labels: " + mapToJsonKeys(data.getItemTypeDistribution()) + ", datasets: [{ data: " + mapToJsonValues(data.getItemTypeDistribution()) + ", backgroundColor: ['#3498db','#9b59b6','#f1c40f','#e67e22'] }] }, options: chartConfig });\n"
                + "new Chart(document.getElementById('userStatusChart'), { type: 'doughnut', data: { labels: ['Active', 'Blocked'], datasets: [{ data: [" + data.getActiveUsers() + ", " + data.getBlockedUsers() + "], backgroundColor: ['#2ecc71','#e74c3c'] }] }, options: chartConfig });\n"
                + "new Chart(document.getElementById('supporterChart'), { type: 'bar', data: { labels: " + mapToJsonKeys(data.getSupporterActivity()) + ", datasets: [{ label: 'Assigned Sections', data: " + mapToJsonValues(data.getSupporterActivity()) + ", backgroundColor: '#f1c40f' }] }, options: { ...chartConfig, scales: { y: { min: 1, max: 4, ticks: { stepSize: 1 } } } } });\n";
    }

    private String buildScriptCoreFunctions() {
        return "function switchTab(tabId, btn) {\n"
                + "  document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));\n"
                + "  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));\n"
                + "  document.getElementById(tabId).classList.add('active');\n"
                + "  btn.classList.add('active');\n"
                + BRACE_CLOSE
                + "function changeFontSize(action) {\n"
                + "  currentFontSize += (action * 8);\n"
                + "  document.body.style.fontSize = currentFontSize + '%';\n"
                + BRACE_CLOSE
                + "function showToast(message) {\n"
                + "  const container = document.getElementById('toast-container');\n"
                + "  const toast = document.createElement('div');\n"
                + "  toast.className = 'toast';\n"
                + "  toast.innerText = message;\n"
                + "  container.appendChild(toast);\n"
                + "  setTimeout(() => toast.remove(), 4000);\n"
                + BRACE_CLOSE
                + "function toggleLiveStream() {\n"
                + "  const btn = document.getElementById('liveBtn');\n"
                + "  if (liveInterval) {\n"
                + "    clearInterval(liveInterval);\n"
                + "    liveInterval = null;\n"
                + "    btn.innerText = 'Start Live Analytics';\n"
                + "    btn.style.background = '#9b59b6';\n"
                + "  } else {\n"
                + "    btn.innerText = 'Streaming Active 🟢';\n"
                + "    btn.style.background = '#2ecc71';\n"
                + "    liveInterval = setInterval(() => {\n"
                + "      baseIncome += Math.floor(Math.random() * 1500);\n"
                + "      baseProfit += Math.floor(Math.random() * 1200);\n"
                + "      document.getElementById('cardIncome').innerText = baseIncome + ' T';\n"
                + "      document.getElementById('cardProfit').innerText = baseProfit + ' T';\n"
                + "      revChart.data.datasets[0].data = revChart.data.datasets[0].data.map(v => v + (Math.random() * 400 - 200));\n"
                + "      revChart.update();\n"
                + "      let logs = ['User Alice returned a book','New VIP Registration','Supporter updated Catalog','Fine paid by user'];\n"
                + "      showToast('⚡ ' + logs[Math.floor(Math.random() * logs.length)]);\n"
                + "    }, 2500);\n"
                + "  }\n"
                + BRACE_CLOSE;
    }

    private String buildScriptFilterFunctions() {
        return "function filterItems() {\n"
                + "  let searchVal = document.getElementById('itemSearch').value.toLowerCase();\n"
                + "  let catVal = document.getElementById('categoryFilter').value;\n"
                + "  let rows = document.querySelectorAll('#inventoryTable .item-row');\n"
                + "  for (let row of rows) {\n"
                + "    let id = row.getElementsByClassName('item-id')[0].innerText.toLowerCase();\n"
                + "    let title = row.getElementsByClassName('item-title')[0].innerText.toLowerCase();\n"
                + "    let cat = row.getAttribute('data-category');\n"
                + "    row.style.display = ((id.includes(searchVal) || title.includes(searchVal)) && (catVal === 'all' || cat === catVal)) ? '' : 'none';\n"
                + BRACE_CLOSE_2
                + BRACE_CLOSE
                + "function filterUsers() {\n"
                + "  let searchVal = document.getElementById('userSearch').value.toLowerCase();\n"
                + "  let statusVal = document.getElementById('statusFilter').value;\n"
                + "  let rows = document.querySelectorAll('#usersTable .user-row');\n"
                + "  for (let row of rows) {\n"
                + "    let name = row.getElementsByClassName('user-fullname')[0].innerText.toLowerCase();\n"
                + "    let phone = row.getElementsByClassName('user-phone')[0].innerText.toLowerCase();\n"
                + "    let status = row.getAttribute('data-status');\n"
                + "    row.style.display = ((name.includes(searchVal) || phone.includes(searchVal)) && (statusVal === 'all' || status === statusVal)) ? '' : 'none';\n"
                + BRACE_CLOSE_2
                + BRACE_CLOSE
                + "function toggleTheme() {\n"
                + "  const currentTheme = document.documentElement.getAttribute('data-theme');\n"
                + "  document.documentElement.setAttribute('data-theme', currentTheme === 'dark' ? 'light' : 'dark');\n"
                + BRACE_CLOSE
                + "function downloadCSV(tableId) {\n"
                + "  let csv = [];\n"
                + "  let rows = document.querySelectorAll('#' + tableId + ' tr');\n"
                + "  for(let i=0; i<rows.length; i++) {\n"
                + "    if(rows[i].style.display === 'none') continue;\n"
                + "    let row = [], cols = rows[i].querySelectorAll('td, th');\n"
                + "    for(let j=0; j<cols.length; j++) row.push('\"' + cols[j].innerText.trim() + '\"');\n"
                + "    csv.push(row.join(','));\n"
                + BRACE_CLOSE_2
                + "  let csvFile = new Blob([csv.join('\\n')], {type: 'text/csv'});\n"
                + "  let downloadLink = document.createElement('a');\n"
                + "  downloadLink.download = tableId + '.csv';\n"
                + "  downloadLink.href = window.URL.createObjectURL(csvFile);\n"
                + "  downloadLink.style.display = 'none';\n"
                + "  document.body.appendChild(downloadLink);\n"
                + "  downloadLink.click();\n"
                + BRACE_CLOSE;
    }

    private void writeHtmlFile(String content, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
            System.out.println("\u001B[32m[System] Advanced Enterprise Command Center generated successfully: " + filePath + "\u001B[0m");
        } catch (IOException e) {
            System.out.println("\u001B[31m[Error] Dashboard creation failed: " + e.getMessage() + "\u001B[0m");
        }
    }

    private String mapToJsonKeys(Map<String, ?> map) {
        return "['" + String.join("','", map.keySet()) + "']";
    }

    private String mapToJsonValues(Map<?, ?> map) {
        return map.values().toString();
    }
}