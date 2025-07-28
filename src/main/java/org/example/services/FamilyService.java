package org.example.services;

import org.example.dao.FamilyDao;
import org.example.dao.FamilyMemberDao;
import org.example.model.Family;
import org.example.model.FamilyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FamilyService {
    private static final Logger logger = LoggerFactory.getLogger(FamilyService.class);
    private final FamilyDao familyDao;
    private final FamilyMemberDao familyMemberDao;

    public FamilyService(FamilyDao familyDao, FamilyMemberDao familyMemberDao) {
        this.familyDao = familyDao;
        this.familyMemberDao = familyMemberDao;
        logger.debug("FamilyService initialized");
    }

    public Family addFamily(Family family) {
        try {
            logger.info("Creating new family: {}", family.getName());
            familyDao.addFamily(family);
            logger.info("Family created successfully with ID: {}", family.getId());
            return family;
        } catch (Exception e) {
            logger.error("Error creating family", e);
            throw new ServiceException("Failed to create family", e);
        }
    }


    public Family getFamilyById(int id) {
        try {
            logger.debug("Getting family by ID: {}", id);
            Family family = familyDao.getFamilyByName(String.valueOf(id));
            if (family == null) {
                logger.warn("Family not found with ID: {}", id);
            }
            return family;
        } catch (Exception e) {
            logger.error("Error getting family by ID: {}", id, e);
            throw new ServiceException("Failed to get family", e);
        }
    }

    public List<Family> getAllFamilies() {
        try {
            logger.debug("Getting all families");
            List<Family> families = familyDao.getAllFamilies();
            logger.info("Found {} families", families.size());
            return families;
        } catch (Exception e) {
            logger.error("Error getting all families", e);
            throw new ServiceException("Failed to get families", e);
        }
    }

    public Family updateFamily(Family family) {
        try {
            logger.info("Updating family with ID: {}", family.getId());

            if (!familyDao.familyExists(family.getId())) {
                logger.warn("Family not found for update, ID: {}", family.getId());
                throw new NotFoundException("Family not found");
            }

            familyDao.updateFamily(family);
            logger.info("Family updated successfully, ID: {}", family.getId());
            return family;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating family with ID: {}", family.getId(), e);
            throw new ServiceException("Failed to update family", e);
        }
    }


    public void deleteFamily(int id) {
        try {
            logger.info("Deleting family with ID: {}", id);

            if (!familyDao.familyExists(id)) {
                logger.warn("Family not found for deletion, ID: {}", id);
                throw new NotFoundException("Family not found");
            }

            familyMemberDao.deleteAllMembersForFamily(id);
            logger.debug("Deleted all members for family ID: {}", id);

            familyDao.deleteFamily(id);
            logger.info("Family deleted successfully, ID: {}", id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting family with ID: {}", id, e);
            throw new ServiceException("Failed to delete family", e);
        }
    }

    public List<Family> searchFamiliesByName(String namePart) {
        try {
            logger.debug("Searching families by name: {}", namePart);
            List<Family> families = familyDao.searchFamiliesByName(namePart);
            logger.info("Found {} families matching '{}'", families.size(), namePart);
            return families;
        } catch (Exception e) {
            logger.error("Error searching families by name: {}", namePart, e);
            throw new ServiceException("Failed to search families", e);
        }
    }

    public int getMembersCount(int familyId) {
        try {
            logger.debug("Getting members count for family ID: {}", familyId);
            int count = familyDao.getFamilyMembersCount(familyId);
            logger.debug("Family ID: {} has {} members", familyId, count);
            return count;
        } catch (Exception e) {
            logger.error("Error getting members count for family ID: {}", familyId, e);
            throw new ServiceException("Failed to get members count", e);
        }
    }

    public boolean familyExists(int id) {
        try {
            logger.debug("Checking if family exists, ID: {}", id);
            return familyDao.familyExists(id);
        } catch (Exception e) {
            logger.error("Error checking if family exists, ID: {}", id, e);
            throw new ServiceException("Failed to check family existence", e);
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