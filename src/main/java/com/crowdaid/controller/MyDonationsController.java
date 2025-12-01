package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.user.Donor;
import com.crowdaid.model.user.User;
import com.crowdaid.config.DBConnection;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.SessionManager;
import com.crowdaid.utils.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class MyDonationsController {
    
    private static final Logger logger = LoggerFactory.getLogger(MyDonationsController.class);
    
    private final ViewLoader viewLoader;
    private final CampaignRepository campaignRepository;
    private Donor currentDonor;
    
    @FXML private TableView<DonationDisplay> donationsTable;
    @FXML private TableColumn<DonationDisplay, LocalDateTime> dateColumn;
    @FXML private TableColumn<DonationDisplay, String> campaignColumn;
    @FXML private TableColumn<DonationDisplay, Double> amountColumn;
    @FXML private TableColumn<DonationDisplay, String> donorNameColumn;
    @FXML private TableColumn<DonationDisplay, String> anonymousColumn;
    @FXML private TableColumn<DonationDisplay, String> messageColumn;
    @FXML private Label totalDonatedLabel;
    @FXML private Button backButton;
    @FXML private Button viewCampaignButton;
    
    public MyDonationsController() {
        this.viewLoader = ViewLoader.getInstance();
        this.campaignRepository = new MySQLCampaignRepository();
    }
    
    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        
        if (user == null || !(user instanceof Donor)) {
            AlertUtil.showError("Access Denied", "You must be logged in as a donor.");
            viewLoader.loadView(viewLoader.getPrimaryStage(), "/fxml/login.fxml", "CrowdAid - Login");
            return;
        }
        
        currentDonor = (Donor) user;
        
        // Setup table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        campaignColumn.setCellValueFactory(new PropertyValueFactory<>("campaignTitle"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        donorNameColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));
        anonymousColumn.setCellValueFactory(new PropertyValueFactory<>("anonymous"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        
        loadDonations();
    }
    
    private void loadDonations() {
        try {
            String sql = "SELECT d.*, c.title as campaign_title FROM donations d " +
                        "JOIN campaigns c ON d.campaign_id = c.id " +
                        "WHERE d.donor_id = ? ORDER BY d.created_at DESC";
            
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, currentDonor.getId());
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<DonationDisplay> donations = FXCollections.observableArrayList();
            double total = 0.0;
            
            while (rs.next()) {
                boolean isAnonymous = rs.getBoolean("is_anonymous");
                String donorName = isAnonymous ? "Anonymous" : currentDonor.getName();
                
                DonationDisplay donation = new DonationDisplay(
                    rs.getLong("id"),
                    rs.getLong("campaign_id"),
                    rs.getString("campaign_title"),
                    rs.getDouble("amount"),
                    donorName,
                    isAnonymous,
                    rs.getString("message"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                donations.add(donation);
                total += donation.getAmount();
            }
            
            donationsTable.setItems(donations);
            totalDonatedLabel.setText(String.format("Total Donated: $%.2f", total));
            
            rs.close();
            stmt.close();
            conn.close();
            
            logger.info("Loaded {} donations for donor: {}", donations.size(), currentDonor.getEmail());
        } catch (SQLException e) {
            logger.error("Error loading donations", e);
            AlertUtil.showError("Database Error", "Failed to load donations: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewCampaign(ActionEvent event) {
        DonationDisplay selected = donationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a donation to view its campaign.");
            return;
        }
        
        try {
            Campaign campaign = campaignRepository.findById(selected.getCampaignId());
            if (campaign != null) {
                SessionManager.getInstance().setAttribute("selectedCampaign", campaign);
                viewLoader.loadView(viewLoader.getPrimaryStage(), 
                    "/fxml/campaign_details.fxml", "CrowdAid - Campaign Details");
            } else {
                AlertUtil.showError("Error", "Campaign not found.");
            }
        } catch (SQLException e) {
            logger.error("Error loading campaign", e);
            AlertUtil.showError("Database Error", "Failed to load campaign: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/donor_dashboard.fxml", "CrowdAid - Donor Dashboard");
    }
    
    // Inner class for table display
    public static class DonationDisplay {
        private final Long id;
        private final Long campaignId;
        private final String campaignTitle;
        private final Double amount;
        private final String donorName;
        private final Boolean isAnonymous;
        private final String message;
        private final LocalDateTime createdAt;
        
        public DonationDisplay(Long id, Long campaignId, String campaignTitle, Double amount, 
                             String donorName, Boolean isAnonymous, String message, LocalDateTime createdAt) {
            this.id = id;
            this.campaignId = campaignId;
            this.campaignTitle = campaignTitle;
            this.amount = amount;
            this.donorName = donorName;
            this.isAnonymous = isAnonymous;
            this.message = message;
            this.createdAt = createdAt;
        }
        
        public Long getId() { return id; }
        public Long getCampaignId() { return campaignId; }
        public String getCampaignTitle() { return campaignTitle; }
        public Double getAmount() { return amount; }
        public String getDonorName() { return donorName; }
        public String getAnonymous() { return isAnonymous ? "Yes" : "No"; }
        public String getMessage() { return message != null ? message : ""; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
