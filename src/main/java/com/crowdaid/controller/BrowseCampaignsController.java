package com.crowdaid.controller;

import com.crowdaid.model.campaign.Campaign;
import com.crowdaid.model.campaign.CampaignCategory;
import com.crowdaid.repository.interfaces.CampaignRepository;
import com.crowdaid.repository.mysql.MySQLCampaignRepository;
import com.crowdaid.utils.AlertUtil;
import com.crowdaid.utils.ViewLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for browsing campaigns (UC6).
 */
public class BrowseCampaignsController {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowseCampaignsController.class);
    
    private final CampaignRepository campaignRepository;
    private final ViewLoader viewLoader;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<CampaignCategory> categoryComboBox;
    @FXML private Button searchButton;
    @FXML private TableView<Campaign> campaignsTable;
    @FXML private TableColumn<Campaign, String> titleColumn;
    @FXML private TableColumn<Campaign, String> categoryColumn;
    @FXML private TableColumn<Campaign, Double> goalColumn;
    @FXML private TableColumn<Campaign, Double> collectedColumn;
    @FXML private TableColumn<Campaign, String> statusColumn;
    @FXML private Button viewDetailsButton;
    @FXML private Button backButton;
    
    private ObservableList<Campaign> campaigns;
    
    public BrowseCampaignsController() {
        this.campaignRepository = new MySQLCampaignRepository();
        this.viewLoader = ViewLoader.getInstance();
        this.campaigns = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        // Setup category filter
        categoryComboBox.getItems().add(null); // "All Categories"
        categoryComboBox.getItems().addAll(CampaignCategory.values());
        categoryComboBox.setValue(null);
        
        // Setup table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        goalColumn.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        collectedColumn.setCellValueFactory(new PropertyValueFactory<>("collectedAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        campaignsTable.setItems(campaigns);
        
        // Load all active campaigns initially
        loadCampaigns();
        
        logger.info("Browse campaigns screen initialized");
    }
    
    /**
     * Load campaigns from database.
     */
    private void loadCampaigns() {
        try {
            List<Campaign> campaignList = campaignRepository.findAllActive();
            campaigns.clear();
            campaigns.addAll(campaignList);
            logger.info("Loaded {} active campaigns", campaignList.size());
        } catch (SQLException e) {
            logger.error("Error loading campaigns", e);
            AlertUtil.showError("Database Error", "Failed to load campaigns: " + e.getMessage());
        }
    }
    
    /**
     * Handle search button click.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText();
        CampaignCategory category = categoryComboBox.getValue();
        
        try {
            List<Campaign> results;
            
            if (keyword == null || keyword.trim().isEmpty()) {
                if (category == null) {
                    results = campaignRepository.findAllActive();
                } else {
                    results = campaignRepository.search("", category);
                }
            } else {
                results = campaignRepository.search(keyword, category);
            }
            
            campaigns.clear();
            campaigns.addAll(results);
            
            logger.info("Search results: {} campaigns found", results.size());
            
        } catch (SQLException e) {
            logger.error("Error searching campaigns", e);
            AlertUtil.showError("Search Error", "Failed to search campaigns: " + e.getMessage());
        }
    }
    
    /**
     * Handle view details button click.
     */
    @FXML
    private void handleViewDetails(ActionEvent event) {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a campaign to view details.");
            return;
        }
        
        // Store selected campaign in session for details view
        com.crowdaid.utils.SessionManager.getInstance().setAttribute("selectedCampaign", selected);
        
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/campaign_details.fxml", "CrowdAid - Campaign Details");
    }
    
    /**
     * Handle back button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        viewLoader.loadView(viewLoader.getPrimaryStage(), 
            "/fxml/donor_dashboard.fxml", "CrowdAid - Donor Dashboard");
    }
}
