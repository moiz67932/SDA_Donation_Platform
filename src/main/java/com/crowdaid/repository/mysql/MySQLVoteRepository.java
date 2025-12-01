package com.crowdaid.repository.mysql;

import com.crowdaid.config.DBConnection;
import com.crowdaid.model.voting.Vote;
import com.crowdaid.model.voting.VoteType;
import com.crowdaid.repository.interfaces.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL implementation of VoteRepository.
 * 
 * @author CrowdAid Development Team
 * @version 1.0.0
 */
public class MySQLVoteRepository implements VoteRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(MySQLVoteRepository.class);
    
    @Override
    public Vote findById(Long id) throws SQLException {
        String sql = "SELECT * FROM votes WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVote(rs);
            }
            return null;
        }
    }
    
    @Override
    public List<Vote> findByMilestone(Long milestoneId) throws SQLException {
        String sql = "SELECT * FROM votes WHERE milestone_id = ?";
        List<Vote> votes = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, milestoneId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                votes.add(mapResultSetToVote(rs));
            }
            return votes;
        }
    }
    
    @Override
    public List<Vote> findByDonor(Long donorId) throws SQLException {
        String sql = "SELECT * FROM votes WHERE donor_id = ?";
        List<Vote> votes = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                votes.add(mapResultSetToVote(rs));
            }
            return votes;
        }
    }
    
    @Override
    public boolean hasVoted(Long donorId, Long milestoneId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM votes WHERE donor_id = ? AND milestone_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, donorId);
            stmt.setLong(2, milestoneId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }
    
    @Override
    public Vote save(Vote vote) throws SQLException {
        String sql = "INSERT INTO votes (donor_id, milestone_id, vote_type, weight, created_at, comment) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, vote.getDonorId());
            stmt.setLong(2, vote.getMilestoneId());
            stmt.setString(3, vote.getVoteType().name());
            stmt.setDouble(4, vote.getWeight());
            stmt.setTimestamp(5, Timestamp.valueOf(vote.getCreatedAt()));
            stmt.setString(6, vote.getComment());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating vote failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vote.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating vote failed, no ID obtained.");
                }
            }
            
            logger.info("Vote created: id={}, milestoneId={}, donorId={}, type={}", 
                       vote.getId(), vote.getMilestoneId(), vote.getDonorId(), vote.getVoteType());
            return vote;
        }
    }
    
    @Override
    public Map<VoteType, Double> getVoteTally(Long milestoneId) throws SQLException {
        String sql = "SELECT vote_type, SUM(weight) as total_weight FROM votes " +
                     "WHERE milestone_id = ? GROUP BY vote_type";
        Map<VoteType, Double> tally = new HashMap<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, milestoneId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                VoteType voteType = VoteType.valueOf(rs.getString("vote_type"));
                double totalWeight = rs.getDouble("total_weight");
                tally.put(voteType, totalWeight);
            }
            
            // Ensure both vote types are present
            tally.putIfAbsent(VoteType.APPROVE, 0.0);
            tally.putIfAbsent(VoteType.REJECT, 0.0);
            
            return tally;
        }
    }
    
    @Override
    public double getApprovalPercentage(Long milestoneId) throws SQLException {
        Map<VoteType, Double> tally = getVoteTally(milestoneId);
        
        double approveWeight = tally.get(VoteType.APPROVE);
        double rejectWeight = tally.get(VoteType.REJECT);
        double totalWeight = approveWeight + rejectWeight;
        
        if (totalWeight == 0) {
            return 0.0;
        }
        
        return (approveWeight / totalWeight) * 100.0;
    }
    
    @Override
    public Map<String, Object> getVoteStatistics(Long milestoneId) throws SQLException {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        String sql = "SELECT vote_type, COUNT(*) as count FROM votes WHERE milestone_id = ? GROUP BY vote_type";
        int approveCount = 0;
        int rejectCount = 0;
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, milestoneId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String voteType = rs.getString("vote_type");
                int count = rs.getInt("count");
                
                if ("APPROVE".equals(voteType)) {
                    approveCount = count;
                } else if ("REJECT".equals(voteType)) {
                    rejectCount = count;
                }
            }
        }
        
        int totalVotes = approveCount + rejectCount;
        
        stats.put("approveCount", approveCount);
        stats.put("rejectCount", rejectCount);
        stats.put("totalVotes", totalVotes);
        stats.put("approvalPercentage", totalVotes > 0 ? (approveCount * 100.0 / totalVotes) : 0.0);
        
        return stats;
    }
    
    /**
     * Maps a ResultSet row to a Vote object.
     * 
     * @param rs the ResultSet
     * @return the Vote object
     * @throws SQLException if database error occurs
     */
    private Vote mapResultSetToVote(ResultSet rs) throws SQLException {
        Vote vote = new Vote();
        vote.setId(rs.getLong("id"));
        vote.setDonorId(rs.getLong("donor_id"));
        vote.setMilestoneId(rs.getLong("milestone_id"));
        vote.setVoteType(VoteType.valueOf(rs.getString("vote_type")));
        vote.setWeight(rs.getDouble("weight"));
        vote.setComment(rs.getString("comment"));
        vote.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        vote.setUpdatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return vote;
    }
}
