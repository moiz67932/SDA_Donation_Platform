package com.crowdaid.repository.interfaces;

import com.crowdaid.model.donation.Donation;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository interface for Donation entity operations.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public interface DonationRepository {
    
    /**
     * Finds a donation by ID.
     * 
     * @param id the donation ID
     * @return the donation, or null if not found
     * @throws SQLException if database error occurs
     */
    Donation findById(Long id) throws SQLException;
    
    /**
     * Finds all donations for a campaign.
     * 
     * @param campaignId the campaign ID
     * @return list of donations
     * @throws SQLException if database error occurs
     */
    List<Donation> findByCampaign(Long campaignId) throws SQLException;
    
    /**
     * Finds all donations by a donor.
     * 
     * @param donorId the donor's user ID
     * @return list of donations
     * @throws SQLException if database error occurs
     */
    List<Donation> findByDonor(Long donorId) throws SQLException;
    
    /**
     * Gets total donation amount by donor to a specific campaign.
     * 
     * @param donorId the donor's user ID
     * @param campaignId the campaign ID
     * @return total donation amount
     * @throws SQLException if database error occurs
     */
    double getTotalDonationByDonorToCampaign(Long donorId, Long campaignId) throws SQLException;
    
    /**
     * Saves a new donation.
     * 
     * @param donation the donation to save
     * @return the saved donation with generated ID
     * @throws SQLException if database error occurs
     */
    Donation save(Donation donation) throws SQLException;
    
    /**
     * Gets top donors for a campaign.
     * 
     * @param campaignId the campaign ID
     * @param limit the maximum number of donors to return
     * @return list of donations representing top donors
     * @throws SQLException if database error occurs
     */
    List<Donation> getTopDonors(Long campaignId, int limit) throws SQLException;
}
