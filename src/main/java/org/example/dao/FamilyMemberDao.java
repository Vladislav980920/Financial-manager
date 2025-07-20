package org.example.dao;

import org.example.model.FamilyMember;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FamilyMemberDao {
    private final Connection connection;

    public FamilyMemberDao() {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * Добавляет нового члена семьи в базу данных
     * @param member объект члена семьи для добавления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void addMember(FamilyMember member) throws SQLException {
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
        }
    }

    /**
     * Получает члена семьи по ID
     * @param id идентификатор члена семьи
     * @return объект члена семьи или null, если не найден
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public FamilyMember getMemberById(int id) throws SQLException {
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
        }
        return null;
    }

    /**
     * Получает всех членов семьи
     * @param familyId идентификатор семьи
     * @return список членов семьи
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<FamilyMember> getMembersByFamily(int familyId) throws SQLException {
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
        }
        return members;
    }

    /**
     * Получает все семьи, в которых состоит пользователь
     * @param userId идентификатор пользователя
     * @return список членств в семьях
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<FamilyMember> getFamiliesByUser(int userId) throws SQLException {
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
        }
        return members;
    }

    /**
     * Обновляет информацию о члене семьи
     * @param member объект члена семьи с обновленными данными
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void updateMember(FamilyMember member) throws SQLException {
        String sql = "UPDATE family_members SET family_id = ?, user_id = ?, role = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, member.getFamilyId());
            statement.setInt(2, member.getUserId());
            statement.setString(3, member.getRole());
            statement.setInt(4, member.getId());

            statement.executeUpdate();
        }
    }

    /**
     * Удаляет члена семьи по ID
     * @param id идентификатор члена семьи для удаления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void deleteMember(int id) throws SQLException {
        String sql = "DELETE FROM family_members WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Проверяет существование члена семьи
     * @param familyId идентификатор семьи
     * @param userId идентификатор пользователя
     * @return true если член семьи существует, иначе false
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public boolean isMemberExists(int familyId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM family_members WHERE family_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Получает роль пользователя в семье
     * @param familyId идентификатор семьи
     * @param userId идентификатор пользователя
     * @return роль пользователя или null, если не является членом
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public String getMemberRole(int familyId, int userId) throws SQLException {
        String sql = "SELECT role FROM family_members WHERE family_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                }
            }
        }
        return null;
    }

    /**
     * Получает количество членов семьи
     * @param familyId идентификатор семьи
     * @return количество членов семьи
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public int getMembersCount(int familyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM family_members WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Получает членов семьи с определенной ролью
     * @param familyId идентификатор семьи
     * @param role роль для поиска
     * @return список членов семьи с указанной ролью
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<FamilyMember> getMembersByRole(int familyId, String role) throws SQLException {
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
        }
        return members;
    }

    /**
     * Удаляет всех членов семьи (при удалении семьи)
     * @param familyId идентификатор семьи
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public void deleteAllMembersForFamily(int familyId) throws SQLException {
        String sql = "DELETE FROM family_members WHERE family_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, familyId);
            statement.executeUpdate();
        }
    }
}

