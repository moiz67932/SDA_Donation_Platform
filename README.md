# CrowdAid - Online Fundraising Platform

## Project Overview

CrowdAid is a comprehensive JavaFX 21 desktop application for online fundraising with features including:
- One-time donations and subscription-based support
- Escrow-based fund management with milestone voting
- Virtual credits and reward shop system
- Role-based dashboards (Donor, Campaigner, Administrator)
- MySQL database backend

## Technology Stack

- **Java**: 21 (LTS)
- **UI Framework**: JavaFX 21
- **Build Tool**: Maven
- **Database**: MySQL 8.0+
- **Architecture**: MVC (Model-View-Controller)
- **Password Hashing**: BCrypt
- **Logging**: SLF4J

## Project Structure

```
fundraiser-platform/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/crowdaid/
│   │   │   ├── MainApp.java
│   │   │   ├── config/
│   │   │   │   └── DBConnection.java
│   │   │   ├── model/
│   │   │   │   ├── common/ (BaseEntity, Role)
│   │   │   │   ├── user/ (User, Donor, Campaigner, Administrator, Credit, Wallet, BankInfo)
│   │   │   │   ├── campaign/ (Campaign, Milestone, Evidence, CampaignUpdate, VotingPeriod, CampaignStatus, etc.)
│   │   │   │   ├── donation/ (Donation, Subscription, EscrowAccount, Transaction, etc.)
│   │   │   │   ├── voting/ (Vote, VoteType)
│   │   │   │   ├── reward/ (Reward, Redemption, etc.)
│   │   │   │   └── payment/ (PaymentGateway, SimulatedPaymentGateway)
│   │   │   ├── repository/
│   │   │   │   ├── interfaces/ (All repository interfaces)
│   │   │   │   └── mysql/ (MySQL implementations)
│   │   │   ├── service/ (Business logic services - TO BE CREATED)
│   │   │   ├── controller/ (JavaFX controllers - TO BE CREATED)
│   │   │   ├── utils/
│   │   │   │   ├── SessionManager.java
│   │   │   │   ├── ViewLoader.java
│   │   │   │   ├── Validator.java
│   │   │   │   └── AlertUtil.java
│   │   │   └── exception/
│   │   │       ├── ValidationException.java
│   │   │       └── BusinessException.java
│   │   └── resources/
│   │       ├── schema.sql
│   │       ├── fxml/ (FXML view files - TO BE CREATED)
│   │       └── css/ (CSS styling - TO BE CREATED)
│   └── test/ (Unit tests - TO BE CREATED)
```

## Database Setup

1. **Create Database**:
   ```bash
   mysql -h 192.168.100.10 -u root -p < src/main/resources/schema.sql
   ```

2. **Update Credentials**:
   Edit `DBConnection.java` and update:
   - DB_USER
   - DB_PASSWORD

3. **Default Admin Account**:
   - Email: admin@crowdaid.com
   - Password: admin123

## Building and Running

### Prerequisites
- JDK 21 or higher
- Maven 3.8+
- MySQL 8.0+
- JavaFX 21

### Build Project
```bash
mvn clean install
```

### Run Application
```bash
mvn javafx:run
```

## Implemented Features

### ✅ Completed Components

1. **Project Configuration**
   - pom.xml with all dependencies
   - Maven build configuration
   - JavaFX integration

2. **Domain Model (Complete)**
   - All entity classes with proper relationships
   - Enumerations for statuses and categories
   - Base entity with timestamps

3. **Database Layer**
   - Complete MySQL schema with all tables
   - Repository interfaces for all entities
   - Sample MySQL repository implementations (User, Campaign)

4. **Utilities**
   - SessionManager for user session handling
   - ViewLoader for FXML navigation
   - Validator for input validation
   - AlertUtil for user notifications

5. **Payment System**
   - PaymentGateway interface
   - SimulatedPaymentGateway implementation

### 🚧 Remaining Components to Implement

The following components need to be created to complete the application:

#### 1. **Repository Implementations** (MySQL package)
Files needed in `src/main/java/com/crowdaid/repository/mysql/`:
- MySQLMilestoneRepository.java
- MySQLDonationRepository.java
- MySQLSubscriptionRepository.java
- MySQLRewardRepository.java
- MySQLVoteRepository.java
- MySQLEscrowRepository.java
- MySQLTransactionRepository.java
- MySQLRedemptionRepository.java
- MySQLCreditRepository.java

#### 2. **Service Layer**
Files needed in `src/main/java/com/crowdaid/service/`:
- AuthenticationService.java (UC1, UC2)
- UserService.java
- CampaignService.java (UC3, UC6, UC11)
- MilestoneService.java (UC4, UC5)
- DonationService.java (UC7)
- SubscriptionService.java (UC8)
- VoteService.java (UC9)
- EscrowService.java
- RewardService.java (UC10, UC12)
- CreditService.java
- TransactionService.java
- NotificationService.java

#### 3. **Controllers**
Files needed in `src/main/java/com/crowdaid/controller/`:
- AuthController.java (login + registration)
- DonorDashboardController.java
- CampaignerDashboardController.java
- AdminDashboardController.java
- BrowseCampaignsController.java
- CampaignDetailsController.java
- DonationDialogController.java
- SubscriptionDialogController.java
- MyDonationsController.java
- MySubscriptionsController.java
- RewardShopController.java
- RedeemRewardController.java
- VotingRequestsController.java
- CreateCampaignController.java
- MilestoneManagementController.java
- SubmitMilestoneController.java
- CampaignApprovalController.java
- RewardManagementController.java

#### 4. **FXML Views**
Files needed in `src/main/resources/fxml/`:
- login.fxml
- register.fxml
- donor_dashboard.fxml
- campaigner_dashboard.fxml
- admin_dashboard.fxml
- browse_campaigns.fxml
- campaign_details.fxml
- donation_dialog.fxml
- subscription_dialog.fxml
- my_donations.fxml
- my_subscriptions.fxml
- reward_shop.fxml
- redeem_reward.fxml
- voting_requests.fxml
- create_campaign.fxml
- milestone_management.fxml
- submit_milestone.fxml
- campaign_approval.fxml
- reward_management.fxml

#### 5. **CSS Styling**
File needed in `src/main/resources/css/`:
- styles.css

## Use Case Implementation Guide

### UC1: Register Account
- **Service**: AuthenticationService.registerDonor() / registerCampaigner()
- **View**: register.fxml
- **Controller**: AuthController

### UC2: Login to Platform
- **Service**: AuthenticationService.login()
- **View**: login.fxml
- **Controller**: AuthController

### UC3: Create Fundraising Campaign
- **Service**: CampaignService.createCampaign()
- **View**: create_campaign.fxml
- **Controller**: CreateCampaignController

### UC4: Set Campaign Milestones
- **Service**: MilestoneService.defineMilestones()
- **View**: milestone_management.fxml
- **Controller**: MilestoneManagementController

### UC5: Submit Milestone Completion
- **Service**: MilestoneService.submitMilestoneCompletion()
- **View**: submit_milestone.fxml
- **Controller**: SubmitMilestoneController

### UC6: Browse Campaigns
- **Service**: CampaignService.searchCampaigns()
- **View**: browse_campaigns.fxml
- **Controller**: BrowseCampaignsController

### UC7: Make One-Time Donation
- **Service**: DonationService.donate()
- **View**: donation_dialog.fxml
- **Controller**: DonationDialogController

### UC8: Subscribe to Campaign
- **Service**: SubscriptionService.createSubscription()
- **View**: subscription_dialog.fxml
- **Controller**: SubscriptionDialogController

### UC9: Vote on Milestone
- **Service**: VoteService.castVote()
- **View**: voting_requests.fxml
- **Controller**: VotingRequestsController

### UC10: Redeem Credits in Shop
- **Service**: RewardService.redeemReward()
- **View**: redeem_reward.fxml
- **Controller**: RedeemRewardController

### UC11: Approve Campaign
- **Service**: CampaignService.approveCampaign()
- **View**: campaign_approval.fxml
- **Controller**: CampaignApprovalController

### UC12: Edit Reward Shop
- **Service**: RewardService.createOrUpdateReward()
- **View**: reward_management.fxml
- **Controller**: RewardManagementController

## Development Guidelines

### Service Layer Pattern
```java
@Override
public Donation donate(Donor donor, Campaign campaign, double amount, boolean anonymous, String message) 
        throws BusinessException {
    // 1. Validate inputs
    // 2. Process payment via PaymentGateway
    // 3. Create donation record
    // 4. Update campaign collected amount
    // 5. Add funds to escrow
    // 6. Grant credits if applicable
    // 7. Create transaction record
    // 8. Send notifications
    // 9. Return donation object
}
```

### Controller Pattern
```java
@FXML
private void handleDonateButton(ActionEvent event) {
    try {
        // 1. Get input values
        // 2. Validate inputs
        // 3. Call service method
        // 4. Show success message
        // 5. Navigate to appropriate view
    } catch (ValidationException e) {
        AlertUtil.showError("Validation Error", e.getMessage());
    } catch (BusinessException e) {
        AlertUtil.showError("Business Error", e.getMessage());
    }
}
```

### FXML Structure
```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox xmlns="http://javafx.com/javafx" xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.crowdaid.controller.SomeController"
      stylesheets="@../css/styles.css">
    <!-- UI Elements -->
</VBox>
```

## Testing

### Unit Tests
Create tests for:
- Repository methods
- Service business logic
- Validation logic
- Payment gateway

### Integration Tests
Test:
- End-to-end use case flows
- Database transactions
- Multi-step processes

## Security Considerations

1. **Password Hashing**: Use BCrypt for all passwords
2. **SQL Injection**: Use prepared statements
3. **Input Validation**: Validate all user inputs
4. **Session Management**: Clear session on logout
5. **Role-Based Access**: Check user role before operations

## Future Enhancements

- Real payment gateway integration (Stripe, PayPal)
- Email notification service
- Image upload for campaigns
- Advanced analytics dashboard
- Mobile application
- Web portal
- Social media integration

## Contributors

CrowdAid Development Team

## License

Proprietary - All Rights Reserved
