package org.example.services;

import org.example.dao.FamilyMemberDao;
import org.example.model.FamilyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FamilyMemberService {
    private static final Logger logger = LoggerFactory.getLogger(FamilyMemberService.class);
    private final FamilyMemberDao familyMemberDao;

    public FamilyMemberService(FamilyMemberDao familyMemberDao) {
        this.familyMemberDao = familyMemberDao;
        logger.debug("FamilyMemberService initialized");
    }


    public FamilyMember addMember(FamilyMember member) {
        try {
            logger.info("Adding new family member: familyId={}, userId={}, role={}",
                    member.getFamilyId(), member.getUserId(), member.getRole());

            if (familyMemberDao.isMemberExists(member.getFamilyId(), member.getUserId())) {
                throw new ServiceException("Member already exists in this family");
            }

            familyMemberDao.addMember(member);
            logger.info("Family member added successfully with ID: {}", member.getId());
            return member;
        } catch (Exception e) {
            logger.error("Error adding family member", e);
            throw new ServiceException("Failed to add family member", e);
        }
    }


    public FamilyMember getMemberById(int id) {
        try {
            logger.debug("Getting family member by ID: {}", id);
            FamilyMember member = familyMemberDao.getMemberById(id);
            if (member == null) {
                logger.warn("Family member not found with ID: {}", id);
            }
            return member;
        } catch (Exception e) {
            logger.error("Error getting family member by ID: {}", id, e);
            throw new ServiceException("Failed to get family member", e);
        }
    }


    public List<FamilyMember> getMembersByFamily(int familyId) {
        try {
            logger.debug("Getting members for family ID: {}", familyId);
            List<FamilyMember> members = familyMemberDao.getMembersByFamily(familyId);
            logger.info("Found {} members for family ID: {}", members.size(), familyId);
            return members;
        } catch (Exception e) {
            logger.error("Error getting members for family ID: {}", familyId, e);
            throw new ServiceException("Failed to get family members", e);
        }
    }


    public List<FamilyMember> getFamiliesByUser(int userId) {
        try {
            logger.debug("Getting families for user ID: {}", userId);
            List<FamilyMember> memberships = familyMemberDao.getFamiliesByUser(userId);
            logger.info("User ID: {} is member in {} families", userId, memberships.size());
            return memberships;
        } catch (Exception e) {
            logger.error("Error getting families for user ID: {}", userId, e);
            throw new ServiceException("Failed to get user's families", e);
        }
    }


    public FamilyMember updateMember(FamilyMember member) {
        try {
            logger.info("Updating family member ID: {}", member.getId());

            if (!familyMemberDao.isMemberExists(member.getFamilyId(), member.getUserId())) {
                logger.warn("Family member not found for update, ID: {}", member.getId());
                throw new NotFoundException("Family member not found");
            }

            familyMemberDao.updateMember(member);
            logger.info("Family member updated successfully, ID: {}", member.getId());
            return member;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating family member ID: {}", member.getId(), e);
            throw new ServiceException("Failed to update family member", e);
        }
    }


    public void deleteMember(int id) {
        try {
            logger.info("Deleting family member ID: {}", id);

            FamilyMember member = familyMemberDao.getMemberById(id);
            if (member == null) {
                throw new NotFoundException("Family member not found");
            }

            familyMemberDao.deleteMember(id);
            logger.info("Family member deleted successfully, ID: {}", id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting family member ID: {}", id, e);
            throw new ServiceException("Failed to delete family member", e);
        }
    }


    public boolean isMemberExists(int familyId, int userId) {
        try {
            logger.debug("Checking if member exists: familyId={}, userId={}", familyId, userId);
            return familyMemberDao.isMemberExists(familyId, userId);
        } catch (Exception e) {
            logger.error("Error checking member existence", e);
            throw new ServiceException("Failed to check member existence", e);
        }
    }


    public String getMemberRole(int familyId, int userId) {
        try {
            logger.debug("Getting role for member: familyId={}, userId={}", familyId, userId);
            return familyMemberDao.getMemberRole(familyId, userId);
        } catch (Exception e) {
            logger.error("Error getting member role", e);
            throw new ServiceException("Failed to get member role", e);
        }
    }


    public int getMembersCount(int familyId) {
        try {
            logger.debug("Getting members count for family ID: {}", familyId);
            return familyMemberDao.getMembersCount(familyId);
        } catch (Exception e) {
            logger.error("Error getting members count", e);
            throw new ServiceException("Failed to get members count", e);
        }
    }


    public List<FamilyMember> getMembersByRole(int familyId, String role) {
        try {
            logger.debug("Getting members by role: familyId={}, role={}", familyId, role);
            return familyMemberDao.getMembersByRole(familyId, role);
        } catch (Exception e) {
            logger.error("Error getting members by role", e);
            throw new ServiceException("Failed to get members by role", e);
        }
    }


    public void deleteAllMembersForFamily(int familyId) {
        try {
            logger.info("Deleting all members for family ID: {}", familyId);
            familyMemberDao.deleteAllMembersForFamily(familyId);
            logger.info("All members deleted for family ID: {}", familyId);
        } catch (Exception e) {
            logger.error("Error deleting all members for family ID: {}", familyId, e);
            throw new ServiceException("Failed to delete all family members", e);
        }
    }


    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }

        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}