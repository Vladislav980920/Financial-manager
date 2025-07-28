package org.example.services;

import org.example.dao.FinancialGoalDao;
import org.example.model.FinancialGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GoalService {
    private static final Logger logger = LoggerFactory.getLogger(GoalService.class);
    private final FinancialGoalDao goalDao;

    public GoalService(FinancialGoalDao goalDao) {
        this.goalDao = goalDao;
        logger.debug("GoalService initialized");
    }

    public void createGoal(int familyId, String name, String description,
                           BigDecimal targetAmount, LocalDate targetDate, String priority) {
        try {
            logger.info("Creating goal for familyId: {}, name: {}, targetAmount: {}",
                    familyId, name, targetAmount);
            FinancialGoal goal = new FinancialGoal(0, familyId, name, description,
                    targetAmount, BigDecimal.ZERO, targetDate, priority);
            goalDao.addGoal(goal);
            logger.info("Goal created successfully");
        } catch (Exception e) {
            logger.error("Error creating goal for familyId: " + familyId, e);
            throw new RuntimeException("Failed to create goal", e);
        }
    }

    public void contributeToGoal(int goalId, BigDecimal amount) {
        try {
            logger.info("Contributing to goalId: {}, amount: {}", goalId, amount);
            FinancialGoal goal = goalDao.getGoalById(goalId);
            if (goal != null) {
                goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
                goalDao.updateGoal(goal);
                logger.info("Contribution to goalId {} successful", goalId);
            } else {
                logger.warn("Goal not found for goalId: {}", goalId);
            }
        } catch (Exception e) {
            logger.error("Error contributing to goalId: " + goalId, e);
            throw new RuntimeException("Failed to contribute to goal", e);
        }
    }

    public List<FinancialGoal> getFamilyGoals(int id, int familyId) {
        logger.debug("Getting goals for familyId: {}", familyId);
        try {
            List<FinancialGoal> goals = goalDao.getGoalsByFamily(familyId);
            logger.debug("Found {} goals for familyId: {}", goals.size(), familyId);
            return goals;
        } catch (Exception e) {
            logger.error("Error getting goals for familyId: " + familyId, e);
            throw new RuntimeException("Failed to get goals", e);
        }
    }

    public BigDecimal calculateGoalProgress(int goalId) {
        logger.debug("Calculating progress for goalId: {}", goalId);
        try {
            FinancialGoal goal = goalDao.getGoalById(goalId);
            if (goal != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = goal.getCurrentAmount().divide(goal.getTargetAmount(), 2, BigDecimal.ROUND_HALF_UP);
                logger.info("Progress for goalId {}: {}", goalId, progress);
                return progress;
            }
            logger.warn("Goal not found or target amount is zero for goalId: {}", goalId);
            return BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error calculating progress for goalId: " + goalId, e);
            throw new RuntimeException("Failed to calculate goal progress", e);
        }
    }
}