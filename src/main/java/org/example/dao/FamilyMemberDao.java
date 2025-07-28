package org.example.dao;

import lombok.SneakyThrows;
import org.example.model.FamilyMember;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FamilyMemberDao {
    private final Connection connection;

    @SneakyThrows
    public FamilyMemberDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void addMember(FamilyMember member) {
        String sql = "INSERT INTO family_members (family_id, user_id, role) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, member.getFamilyId());
            statement.setInt(2, member.getUserId());
            statement.setString(3, member.getRole());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    member.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding family member", e);
        }
    }

    public FamilyMember getMemberById(int id) {
        String sql = "SELECT * FROM family_members WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new FamilyMember(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting family member by id", e);
        }
        return null;
    }

    public List<FamilyMember> getMembersByFamily(int familyId) {
        List<FamilyMember> members = new ArrayList<>();
        String sql = "SELECT * FROM family_members WHERE family_id = ? ORDER BY role";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(new FamilyMember(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("role")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting members by family", e);
        }
        return members;
    }

    public List<FamilyMember> getFamiliesByUser(int userId) {
        List<FamilyMember> members = new ArrayList<>();
        String sql = "SELECT * FROM family_members WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(new FamilyMember(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("role")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting families by user", e);
        }
        return members;
    }

    public void updateMember(FamilyMember member) {
        String sql = "UPDATE family_members SET family_id = ?, user_id = ?, role = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, member.getFamilyId());
            statement.setInt(2, member.getUserId());
            statement.setString(3, member.getRole());
            statement.setInt(4, member.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating family member", e);
        }
    }

    public void deleteMember(int id) {
        String sql = "DELETE FROM family_members WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting family member", e);
        }
    }

    public boolean isMemberExists(int familyId, int userId) {
        String sql = "SELECT 1 FROM family_members WHERE family_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if member exists", e);
        }
    }

    public String getMemberRole(int familyId, int userId) {
        String sql = "SELECT role FROM family_members WHERE family_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting member role", e);
        }
        return null;
    }

    public int getMembersCount(int familyId) {
        String sql = "SELECT COUNT(*) FROM family_members WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting members count", e);
        }
        return 0;
    }

    public List<FamilyMember> getMembersByRole(int familyId, String role) {
        List<FamilyMember> members = new ArrayList<>();
        String sql = "SELECT * FROM family_members WHERE family_id = ? AND role = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setString(2, role);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(new FamilyMember(
                            resultSet.getInt("id"),
                            resultSet.getInt("family_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("role")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting members by role", e);
        }
        return members;
    }

    public void deleteAllMembersForFamily(int familyId) {
        String sql = "DELETE FROM family_members WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all members for family", e);
        }
    }
}