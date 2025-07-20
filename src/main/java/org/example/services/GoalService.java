package org.example.services;

import org.example.dao.FinancialGoalDao;
import org.example.model.FinancialGoal;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class GoalService {
    private FinancialGoalDao goalDao;

    public GoalService(FinancialGoalDao goalDao) {
        this.goalDao = goalDao;
    }

    public void createGoal(int familyId, String name, String description,
                           BigDecimal targetAmount, LocalDate targetDate, String priority) throws SQLException {
        FinancialGoal goal = new FinancialGoal(0, familyId, name, description,
                targetAmount, BigDecimal.ZERO, targetDate, priority);
        goalDao.addGoal(goal);
    }

    public void contributeToGoal(int goalId, BigDecimal amount) throws SQLException {
        FinancialGoal goal = goalDao.getGoalById(goalId);
        if (goal != null) {
            goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
            goalDao.updateGoal(goal);
        }
    }

    public List<FinancialGoal> getFamilyGoals(int familyId) throws SQLException {
        return goalDao.getGoalsByFamily(familyId);
    }

    public BigDecimal calculateGoalProgress(int goalId) throws SQLException {
        FinancialGoal goal = goalDao.getGoalById(goalId);
        if (goal != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            return goal.getCurrentAmount().divide(goal.getTargetAmount(), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
